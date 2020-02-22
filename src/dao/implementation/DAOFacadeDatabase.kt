package com.relevant.programmer.dao.implementation

import com.relevant.programmer.dao.*
import com.relevant.programmer.entities.Todo
import com.relevant.programmer.entities.TodoLabel
import org.jetbrains.exposed.sql.*
import java.io.File


class DAOFacadeDatabase(val db: Database = Database.connect("jdbc:h2:mem:test", driver = "org.h2.Driver")) :
    DAOFacade {
    override fun deleteTodo(id: Int): Boolean {
        return db.transaction {
            Todos.update({ (Todos.id.eq(id)) }) {
                it[isDeleted] = true
            }
            true
        }
    }

    override fun updateTodo(todo: Todo): Todo {
        return db.transaction {
            Todos.update({ (Todos.id.eq(todo.id)) }) {
                it[text] = todo.text
                it[isComplete] = todo.isComplete
                it[isDeleted] = todo.isDeleted
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

    override fun findAllTodos(userId: String): List<Todo> {
        return db.transaction {
            val rows = (Todos innerJoin UserTodos).slice(Todos.id, Todos.text, Todos.isComplete, Todos.isDeleted)
                .select { (Todos.id.eq(UserTodos.todoId)) and (UserTodos.userId.eq(userId)) }
            rows.map {
                Todo(
                    id = it[Todos.id],
                    text = it[Todos.text],
                    isComplete = it[Todos.isComplete],
                    isDeleted = it[Todos.isDeleted],
                    labels = getLables(it[Todos.id])
                )
            }
        }

    }

    private fun getLables(todoId: Int): List<TodoLabel> {
        return db.transaction {
            (UserTodoLabels innerJoin  TodoLabels).slice(TodoLabels.todoId, UserTodoLabels.label )
                .select{UserTodoLabels.id.eq(TodoLabels.labelId) and TodoLabels.todoId.eq(todoId)}
                .map {
                TodoLabel(id=it[TodoLabels.id], name=it[UserTodoLabels.label])
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
            create(Users)
            create(UserTodos)
            create(UserTodoLabels)
            create(TodoLabels)

        }

    }

    override fun createTodo(userId: String, text: String, lableIds: List<Int>): Todo {
        return db.transaction {
            val count = Users.select { Users.id.eq(userId) }.count()
            if (count == 0) {
                Users.insert {
                    it[id] = userId
                }
            }
            val id = Todos.insert {
                it[Todos.text] = text
            }.generatedKey ?: throw IllegalStateException("No generated key returned")

            UserTodos.insert {
                it[UserTodos.userId] = userId
                it[UserTodos.todoId] = id

            }

            TodoLabels.batchInsert(lableIds){
                lableId ->
                this[TodoLabels.labelId] = lableId
                this[TodoLabels.todoId] = id
            }


            getTodo(id)

        }
    }
}


