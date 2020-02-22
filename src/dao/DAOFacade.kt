package com.relevant.programmer.dao

import com.relevant.programmer.entities.Todo

interface DAOFacade {
    fun init()
    fun createTodo(userId: String, text: String, labelIds: List<Int>): Todo
    fun findTodo(id: Int): Todo
    fun findAllTodos(userId: String): List<Todo>
    fun updateTodo(todo: Todo): Todo
    fun deleteTodo(id: Int): Boolean

}