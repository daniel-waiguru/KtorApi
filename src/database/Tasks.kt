package dev.hashnode.danielwaiguru.database

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table

object Tasks: Table() {
    val taskId: Column<Int> = integer("task_id").autoIncrement()
    val uid: Column<Int> = integer("uid").references(Users.uid)
    val title = varchar("title", 256)
    val description = varchar("description", 512)
    val done = bool("done")
}