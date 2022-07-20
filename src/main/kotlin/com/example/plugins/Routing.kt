package com.example.plugins

import com.example.classes.FailureCode
import com.example.classes.Res
import com.example.classes.StatusCode
import com.example.controllers.Controller
import com.example.controllers.PersonController
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*

fun Application.configureRouting() {
    routing {
        person()
    }
}

suspend inline fun <reified T : Any> ApplicationCall.respondResult(res: Res<T>) {
    when (res) {
        is Res.Success -> respond(
            when (res.statusCode) {
                StatusCode.Ok -> HttpStatusCode.OK
                StatusCode.NoContent -> HttpStatusCode.NoContent
                StatusCode.Created -> HttpStatusCode.Created
            }, res.value
        )
        is Res.Failure -> respond(
            when (res.failure) {
                FailureCode.NotFound -> HttpStatusCode.NotFound
                FailureCode.Unauthorized -> HttpStatusCode.Unauthorized
                FailureCode.ValidationError -> HttpStatusCode.BadRequest
            },
            /**
             * FIXME: I want to create some sort of mapping elsewhere, so it can automatically resolve the messages.
             *
             * Also, maybe I want to not only return a string but something with a bit more info;
             *
             * {
             *    "statusCode": 401,
             *    "errorCode": "UNAUTHORIZED_ERROR",
             *    "message": "Signature is not valid"
             * }
             *
             */
            when (res.failure) {
                FailureCode.NotFound -> "Not Found, are you looking good enough?"
                FailureCode.Unauthorized -> "Unauthorized, go away!"
                FailureCode.ValidationError -> "Bad Request, you screwed up"
            }
        )
    }
}

fun Routing.person() {
    val controller = PersonController()
    addController(controller, "/person")
}

inline fun <reified T : Any, C> Routing.addController(controller: C, path: String) where C : Controller<T> {
    get(path) {
        call.respondResult(controller.get())
    }

    get("$path/{id}") {
        call.respondResult(controller.getOne(call.parameters["id"]))
    }

    post(path) {
        call.respondResult(controller.create(call.receive()))
    }

    put("$path/{id}") {
        call.respondResult(controller.put(call.parameters["id"], call.receive()))
    }

    delete("$path/{id}") {
        call.respondResult(controller.delete(call.parameters["id"]))
    }
}

