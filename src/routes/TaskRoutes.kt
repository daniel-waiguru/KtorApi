package dev.hashnode.danielwaiguru.routes

import dev.hashnode.danielwaiguru.auth.UserSession
import dev.hashnode.danielwaiguru.repos.TaskRepo
import dev.hashnode.danielwaiguru.repos.UserRepo
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.sessions.*

fun Route.tasks(taskRepo: TaskRepo, userRepo: UserRepo) {
    authenticate("jwt") {
        route("/$API_VERSION/tasks") {
            post("/create") {
                val taskParams = call.receive<Parameters>()
                val title = taskParams["title"] ?: return@post call.respond(
                    HttpStatusCode.BadRequest, "Title field is required")
                val desc = taskParams["description"] ?: ""
                val done = taskParams["done"] ?: "false"
                val user = call.sessions.get<UserSession>()?.let {
                    userRepo.getUser(it.uid)
                }
                if (user == null) {
                    call.respond(
                        HttpStatusCode.Unauthorized, "Un authorized user"
                    )
                    return@post
                }
                try {
                    val currentTask = taskRepo.storeTask(
                        uid = user.uid,
                        title = title,
                        description = desc,
                        done = done.toBoolean())
                    currentTask?.taskId?.let {
                        call.respond(
                            HttpStatusCode.Created, currentTask
                        )
                    }
                } catch (e: Throwable) {
                    application.log.error("Error adding a task", e)
                    call.respond(HttpStatusCode.BadRequest, "Failed to add a task")
                }
            }
            get {
                val user = call.sessions.get<UserSession>()?.let {
                    userRepo.getUser(it.uid)
                }
                if (user == null) {
                    call.respond(
                        HttpStatusCode.Unauthorized, "Un authorized user"
                    )
                    return@get
                }
                try {
                    val tasks = taskRepo.getTasks(user.uid)
                    call.respond(tasks)
                } catch (e: Throwable) {
                    application.log.error("Failed to get all tasks", e)
                    call.respond(
                        HttpStatusCode.BadRequest, "Failed to get tasks"
                    )
                }
            }
        }
    }
}
fun Application.registerTaskRoutes(userRepo: UserRepo, taskRepo: TaskRepo) {
    routing {
        tasks(taskRepo, userRepo)
    }
}