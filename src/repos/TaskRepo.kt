package dev.hashnode.danielwaiguru.repos

import dev.hashnode.danielwaiguru.models.Task

interface TaskRepo {
    suspend fun storeTask(uid: Int, title: String, description: String, done: Boolean): Task?
    suspend fun getTasks(uid: Int): List<Task>
}