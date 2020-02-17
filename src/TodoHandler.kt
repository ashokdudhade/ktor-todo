package com.relevant.programmer

import com.mchange.v2.c3p0.ComboPooledDataSource
import com.relevant.programmer.dao.Todos
import io.ktor.application.call
import io.ktor.request.receive
import io.ktor.request.receiveParameters
import io.ktor.response.respond
import io.ktor.routing.*
import org.h2.Driver
import org.jetbrains.exposed.sql.*
import java.io.File

val dir = File("build/db")


val pool = ComboPooledDataSource().apply {
    driverClass = Driver::class.java.name
    jdbcUrl = "jdbc:h2:file:${dir.canonicalFile.absolutePath}"
    user = ""
    password = ""
}


val dao: DAOFacade = DAOFacadeDatabase(Database.connect(pool))

fun Route.todo(){
    dao.init()


    get("/todo"){
        call.respond("todo get")
    }
    get("/todos"){
        val todos = dao.findAllTodos()
        call.respond(todos)
    }
    put("/todo"){

        val todo = call.receive<Todo>()
        val todo1 = dao.createTodo(todo.text)

        call.respond(todo1)
    }

    post("/todo"){
        call.respond("todo post")
    }

    patch("/todo"){
        val todo = call.receive<Todo>()

        val updatedTodo = dao.updateTodo(todo)
        call.respond(updatedTodo)
    }

    delete("/todo/{id}") {
        val params = call.parameters
        val id = params.get("id")!!.toInt()
        val status = dao.deleteTodo(id)
        call.respond(status)


    }
}

interface DAOFacade {
    fun init()
    fun createTodo(text: String): Todo
    fun findTodo(id: Int): Todo
    fun findAllTodos(): List<Todo>
    fun updateTodo(todo: Todo): Todo
    fun deleteTodo(id: Int): Boolean


}

class DAOFacadeDatabase(val db: Database = Database.connect("jdbc:h2:mem:test", driver = "org.h2.Driver")): DAOFacade {
    override fun deleteTodo(id: Int): Boolean {
        return db.transaction {
            Todos.update({(Todos.id.eq(id))}){
                it[isDeleted] = true
            }
            true
        }
    }

    override fun updateTodo(todo: Todo): Todo {
        return db.transaction {
            Todos.update({ (Todos.id.eq(todo.id))}) {
                it[Todos.text] = todo.text
                it[Todos.isComplete] = todo.isComplete
                it[Todos.isDeleted] = todo.isDeleted
            }

            getTodo(todo.id)
        }
    }

    override fun findTodo(id: Int): Todo {
        return db.transaction {
            getTodo(id)


        }

    }

    private fun getTodo(id: Int): Todo {
        val row = Todos.select { Todos.id.eq(id) }.single()
        return Todo(
            id = row[Todos.id],
            text = row[Todos.text],
            isComplete = row[Todos.isComplete],
            isDeleted = row[Todos.isDeleted]
        )
    }

    override fun findAllTodos(): List<Todo> {
        return db.transaction {
            val rows = Todos.selectAll()
            rows.map {
                Todo(
                    id = it[Todos.id],
                    text = it[Todos.text],
                    isComplete = it[Todos.isComplete],
                    isDeleted = it[Todos.isDeleted]
                )
            }
        }

    }

    constructor(dir: File) : this(
        Database.connect(
            "jdbc:h2:file:${dir.canonicalFile.absolutePath}",
            driver = "org.h2.Driver"
        )
    )

    override fun init() {
        db.transaction {
            create(Todos)
        }

    }

    override fun createTodo(text: String): Todo {
        return db.transaction {
            val id = Todos.insert {
                it[Todos.text] = text
            }.generatedKey ?: throw IllegalStateException("No generated key returned")

            getTodo(id)

        }
    }
}


