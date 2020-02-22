package com.relevant.programmer.routes

import com.google.inject.Inject
import com.relevant.programmer.entities.Todo
import com.relevant.programmer.service.TodoService
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.header
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.*

class TodoRoutes @Inject constructor(application: Application, todoService: TodoService) {
    init {
        application.routing {
            get("/todo") {
                call.respond("todo get")
            }
            get("/todos") {
                val userId = call.request.header("userId")
                if (userId != null) {
                    val todos = todoService.getAllTodos(userId)


                    call.respond(todos)
                } else {
                    call.respond(HttpStatusCode.BadRequest, "Bad request")
                }


            }
            put("/todo") {
                val todo = call.receive<Todo>()
                val userId = call.request.header("userId")
                if (userId != null) {
                    val todo1 = todoService.createTodo(userId, todo.text, todo.labels.map { it.id })


                    call.respond(todo1)
                } else {
                    call.respond(HttpStatusCode.BadRequest, "Bad request")
                }
            }

            post("/todo") {
                call.respond("todo post")
            }

            patch("/todo") {
                val todo = call.receive<Todo>()

                val updatedTodo = todoService.updateTodo(todo)
                call.respond(updatedTodo)
            }

            delete("/todo/{id}") {
                val params = call.parameters
                val id = params.get("id")!!.toInt()
                val status = todoService.deleteTodo(id)
                call.respond(status)


            }
        }
    }
}