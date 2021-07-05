package dev.hashnode.danielwaiguru.repos

import dev.hashnode.danielwaiguru.database.DatabaseFactory.dbQuery
import dev.hashnode.danielwaiguru.database.Tasks
import dev.hashnode.danielwaiguru.models.Task
import dev.hashnode.danielwaiguru.models.TaskDomain
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.statements.InsertStatement

class TaskRepoImpl: TaskRepo {
    override suspend fun storeTask(uid: Int, title: String, description: String, done: Boolean): Task? {
        var statement: InsertStatement<Number>? = null
        dbQuery {
            statement = Tasks.insert { task ->
                task[Tasks.uid] = uid
                task[Tasks.title] = title
                task[Tasks.description] = description
                task[Tasks.done] = done
            }
        }
        return rowToTask(statement?.resultedValues?.get(0))
    }

    override suspend fun getTasks(uid: Int): List<Task> = dbQuery {
        Tasks.select { Tasks.uid.eq(uid) }.mapNotNull { rowToTask(it) }
    }

    override suspend fun deleteTask(userId: Int, taskId: Int): Int = dbQuery {
        Tasks.deleteWhere { (Tasks.uid.eq(userId)) and (Tasks.taskId.eq(taskId)) }
    }

    override suspend fun updateTask(userId: Int, taskId: Int, task: TaskDomain): Int = dbQuery {
        Tasks.update({ Tasks.uid.eq(userId) and Tasks.taskId.eq(taskId) }) {
            it[title] = task.title
            it[description] = task.description
            it[done] = task.done
        }
    }

    private fun rowToTask(row: ResultRow?): Task? {
        if (row == null) {
            return null
        }
        return Task(
            taskId = row[Tasks.taskId],
            uid = row[Tasks.uid],
            title = row[Tasks.title],
            description = row[Tasks.description],
            done = row[Tasks.done]
        )
    }
}