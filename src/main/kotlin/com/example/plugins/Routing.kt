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
//    val controller = PersonController()
//    addController(controller, "/person")
}

interface Controller<T> {
    fun get() : Res<List<T>>
    fun getOne(id: String) : Res<T>
    fun put(item: T) : Res<T>
    fun delete(id: String) : Res<Unit>
}
enum class StatusCode {
    Ok, NotFound, ValidationError, Unauthorized
}enum class FailureCode {
    NotFound, ValidationError, Unauthorized
}
class Result<T>(status: StatusCode, result:T?) {

}

sealed class Res<out T> {
    data class Success<T>(val value: T) : Res<T>()
    data class Failure(val failure: FailureCode) : Res<Nothing>()
    fun <U> map(f: (T) -> U)
        = when(this) {
            is Success<T> -> Success(f(value))
            is Failure -> this
        }
    fun filter(f: (T) -> Boolean)
        = when (this) {
            is Success -> if(f(value)) this else Failure(FailureCode.ValidationError)
            is Failure -> this
        }
}

fun FailureCode.toResult() = Res.Failure(this)
fun <T> T.toResult() = Res.Success(this)

fun main() {
//    val lst = listOf(1,2,3)
//    val res = lst.map { it + 1 }
//    val res = lst.filter { it % 2 == 1}
    val r = 3.toResult()
    val s = r.filter { it % 2 == 1}
   println(s)
}

suspend inline fun <reified T : Any> ApplicationCall.respondResult(res: Res<T>)
{
    when(res){
        is Res.Success -> respond(res.value)
        is Res.Failure -> respond(when(res.failure){
            FailureCode.NotFound -> HttpStatusCode.NotFound
            FailureCode.Unauthorized -> HttpStatusCode.Unauthorized
            FailureCode.ValidationError -> HttpStatusCode.BadRequest
        })
    }
}

inline fun <reified T : Any, C> Routing.addController(controller: C, path: String) where C : Controller<T> {
    get(path) {
        val res = controller.get()
        call.respondResult(res)
    }
    put(path) {
        val item = call.receive<T>()
        controller.put(item)
        call.respond(HttpStatusCode.OK)
    }
}
//class PersonController() : Controller<Person> {
//    var person = Person("Joost", "Morsink")
//    override fun get() = person
//    override fun put(newPerson: Person) {
//        person = newPerson
//    }
//
//}
@Serializable
data class Person(val firstName:String, val lastName:String) {

}
