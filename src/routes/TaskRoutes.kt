package dev.hashnode.danielwaiguru.routes

import dev.hashnode.danielwaiguru.auth.UserSession
import dev.hashnode.danielwaiguru.models.TaskDomain
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
            put("/{id}") {
                val taskParams = call.receive<Parameters>()
                val title = taskParams["title"] ?: return@put call.respond(
                    HttpStatusCode.BadRequest, "Title field is required")
                val desc = taskParams["description"] ?: ""
                val done = taskParams["done"] ?: "false"
                val task = TaskDomain(title, desc, done.toBoolean())
                val user = call.sessions.get<UserSession>()?.let {
                    userRepo.getUser(it.uid)
                }
                if (user == null) {
                    call.respond(HttpStatusCode.BadRequest, "Un authorized user")
                    return@put
                }
                try {
                    application.log.info(user.toString())

                    val taskId = call.parameters["id"] ?: return@put call.respond(
                        HttpStatusCode.BadRequest, "Unknown task id"
                    )
                    application.log.info(taskId)
                    val updatedTaskId = taskRepo.updateTask(
                        user.uid,
                        taskId.toInt(),
                        task)
                    if (updatedTaskId < 1) {
                        call.respond(
                            status = HttpStatusCode.BadRequest,
                            message = "Update not successful"
                        )
                    } else {
                        call.respond(
                            status = HttpStatusCode.OK,
                            message = "Update successful"
                        )
                    }

                } catch (e: Exception) {
                    application.log.error("Failed to update a task", e)
                    call.respond(HttpStatusCode.BadRequest, "Task update failed")
                }
            }
            delete("/{id}") {
                val user = call.sessions.get<UserSession>()?.let {
                    userRepo.getUser(it.uid)
                }
                application.log.info(user.toString())
                call.application.environment.log.info(user.toString())
                call.application.environment.log.debug(user.toString())
                if (user == null) {
                    call.respond(
                        HttpStatusCode.Unauthorized, "Un authorized user"
                    )
                    return@delete
                }
                try {
                    val id = call.parameters["id"] ?: return@delete call.respond(
                        HttpStatusCode.BadRequest, "Unknown task id"
                    )
                    application.log.info(id)
                    val deletedTask = taskRepo.deleteTask(user.uid, id.toInt())
                    if (deletedTask < 1) {
                        call.respond(
                            HttpStatusCode.BadRequest, "Task deletion failed!"
                        )
                    } else {
                        call.respond(
                            HttpStatusCode.OK, "Task id $id deleted"
                        )
                    }

                } catch (e: Throwable) {
                    application.log.error("Failed to delete", e)
                    call.respond(
                        HttpStatusCode.BadRequest, "Failed to delete a task"
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