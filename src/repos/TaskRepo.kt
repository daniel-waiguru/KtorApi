package dev.hashnode.danielwaiguru.repos

import dev.hashnode.danielwaiguru.models.Task
import dev.hashnode.danielwaiguru.models.TaskDomain

interface TaskRepo {
    suspend fun storeTask(uid: Int, title: String, description: String, done: Boolean): Task?
    suspend fun getTasks(uid: Int): List<Task>
    suspend fun deleteTask(userId: Int, taskId: Int): Int
    suspend fun updateTask(userId: Int, taskId: Int, task: TaskDomain): Int
}