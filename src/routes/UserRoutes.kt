package dev.hashnode.danielwaiguru.routes

import dev.hashnode.danielwaiguru.auth.JwtService
import dev.hashnode.danielwaiguru.auth.UserSession
import dev.hashnode.danielwaiguru.repos.UserRepo
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.sessions.*
import org.apache.commons.mail.DefaultAuthenticator
import org.apache.commons.mail.SimpleEmail

fun Route.users(db: UserRepo, jwtService: JwtService, hashFunction: (String) -> String) {
    route("/$API_VERSION/user") {
        post("/register") {
            val signupParameters = call.receive<Parameters>()
            val password = signupParameters["password"]
                ?: return@post call.respond(
                    HttpStatusCode.Unauthorized, "Missing Fields")
            val displayName = signupParameters["username"]
                ?: return@post call.respond(
                    HttpStatusCode.Unauthorized, "Missing Fields")
            val email = signupParameters["email"]
                ?: return@post call.respond(
                    HttpStatusCode.Unauthorized, "Missing Fields")
            val hash = hashFunction(password)
            try {
                val newUser = db.storeUser(displayName, email, hash)
                /*SimpleEmail().apply {
                    hostName = "smtp.googlemail.com"
                    setSmtpPort(587)
                    setAuthenticator(DefaultAuthenticator("danielirungu002@gmail.com", "acekeed254"))
                    isSSLOnConnect = true
                    setFrom("admin@gmail.com")
                    subject = "Test"
                    setMsg("Hello")
                    addTo(email)
                    send()
                }*/
                newUser?.uid?.let {
                    call.sessions.set(UserSession(it))
                    call.respondText(
                        jwtService.generateToken(newUser),
                        status = HttpStatusCode.Created
                    )
                }
            } catch (e: Throwable) {
                application.log.error("Failed to register user", e)
                call.respond(HttpStatusCode.BadRequest, "User registration failed")
            }
        }
        post("/login") {
            val signinParameters = call.receive<Parameters>()
            val email = signinParameters["email"] ?: return@post call.respond(
                HttpStatusCode.Unauthorized, "Missing Fields"
            )
            val password = signinParameters["password"] ?: return@post call.respond(
                HttpStatusCode.Unauthorized, "Missing Fields"
            )
            val hash = hashFunction(password)
            try {
                val currentUser = db.getUserByEmail(email)
                currentUser?.uid?.let {
                    if (currentUser.password == hash) {
                        call.sessions.set(UserSession(it))
                        call.respondText(jwtService.generateToken(currentUser))
                    } else {
                        call.respond(
                            HttpStatusCode.BadRequest, "Invalid credentials"
                        )
                    }
                }
            } catch (e: Throwable) {
                application.log.error("Failed to login", e)
                call.respond(HttpStatusCode.BadRequest, "User login failed")
            }
        }
    }
}

fun Application.registerUserRoutes(db: UserRepo, jwtService: JwtService, hashFunction: (String) -> String) {
    routing {
        users(db, jwtService, hashFunction)
    }
}


const val API_VERSION = "v1"

