package dev.hashnode.danielwaiguru.database

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table

object Users: Table() {
    val uid: Column<Int> = integer("uid").autoIncrement().primaryKey()
    val username = varchar("username", 256)
    val email = varchar("email", 256).uniqueIndex()
    val password = varchar("password", 64)
}