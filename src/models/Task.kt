package dev.hashnode.danielwaiguru.models
data class Task(
    val uid: Int,
    val taskId: Int,
    val title: String,
    val description: String,
    val done: Boolean
)