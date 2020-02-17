package com.relevant.programmer

data class Todo(
           val text: String,
           val id: Int=0,
           val isComplete: Boolean = false,
           val isDeleted: Boolean = false)


class TodoWrapper(val todo: Todo)