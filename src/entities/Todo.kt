package com.relevant.programmer.entities

data class Todo(
           val text: String,
           val id: Int=0,
           val isComplete: Boolean = false,
           val isDeleted: Boolean = false,
           val labels: List<TodoLabel> = listOf())

data class TodoLabel(val id: Int=0, val name: String)

class TodoWrapper(val todo: Todo)