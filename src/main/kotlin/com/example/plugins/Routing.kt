package com.example.plugins

import io.ktor.routing.*
import io.ktor.http.*
import io.ktor.application.*
import io.ktor.response.*
import io.ktor.request.*
import kotlinx.serialization.Serializable

fun Application.configureRouting() {
    routing {
        homePage()
        helpPage()
        personPage()
    }
}

fun Routing.homePage(){
    get("/") {
        call.respondText("Hello World!")
    }
}
fun Routing.helpPage() {
    get("/help") {
        call.respondText("<h1>HELP!</h1>", ContentType.parse("text/html"))
    }
}
fun Routing.personPage() {
    val controller = PersonController()
    addController(controller, "/person")
}

interface Controller<T> {
    fun get() : T
    fun put(item: T) : Unit
}

inline fun <reified T : Any, C> Routing.addController(controller: C, path: String) where C : Controller<T> {
    get(path) {
        call.respond(controller.get())
    }
    put(path) {
        val item = call.receive<T>()
        controller.put(item)
        call.respond(HttpStatusCode.OK)
    }
}
class PersonController() : Controller<Person> {
    var person = Person("Joost", "Morsink")
    override fun get() = person
    override fun put(newPerson: Person) {
        person = newPerson
    }

}
@Serializable
data class Person(val firstName:String, val lastName:String) {

}
