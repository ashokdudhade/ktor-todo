package com.relevant.programmer.service

import com.google.inject.Inject
import com.relevant.programmer.entities.Todo
import com.relevant.programmer.dao.DAOFacade

class TodoService @Inject constructor(var dao: DAOFacade) {
    fun getAllTodos(userId: String): List<Todo>{
        return dao.findAllTodos(userId)
    }
    fun createTodo(userId: String, text: String, labelIds: List<Int>): Todo {
        return dao.createTodo(userId,  text, labelIds)
    }
    fun updateTodo(todo: Todo): Todo {
        return dao.updateTodo(todo)
    }

    fun deleteTodo(id: Int): Boolean {
        return dao.deleteTodo(id)

    }
}