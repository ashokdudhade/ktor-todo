package com.relevant.programmer.dao

import org.jetbrains.exposed.sql.Table

object Todos : Table() {
    val id = integer("id").primaryKey().autoIncrement()
    val text = varchar("text", 1024)
    val isComplete = bool( "is_complete").default(false)
    val isDeleted = bool("is_deleted").default(false)
}

object Users: Table(){
    val id = varchar("id", 50).primaryKey()
}

object UserTodos: Table(){
    val id = integer("id").primaryKey().autoIncrement()
    val userId = (varchar("userId", 50) references  Users.id)
    val todoId = (integer("todoId") references Todos.id)

}

object UserTodoLabels: Table(){
    val id = integer("id").primaryKey().autoIncrement()
    val userId = (varchar("userId", 50) references Users.id)
    val label = varchar("label", 50)
}

object TodoLabels: Table(){
    val id = integer("id").primaryKey().autoIncrement()
    val labelId = integer("labelId") references UserTodoLabels.id
    val todoId = (integer("todoId") references Todos.id)
}



