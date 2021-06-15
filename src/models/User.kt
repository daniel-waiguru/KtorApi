package dev.hashnode.danielwaiguru.models


import io.ktor.auth.*
import java.io.Serializable


data class User(
    val uid: Int,
    val username: String,
    val email: String,
    val password: String
): Serializable, Principal