package com.relevant.programmer.dao

import org.jetbrains.exposed.sql.Table

object Todos : Table() {
    val id = integer("id").primaryKey().autoIncrement()
    val text = varchar("text", 1024)
    val isComplete = bool( "is_complete").default(false)
    val isDeleted = bool("is_deleted").default(false)
}