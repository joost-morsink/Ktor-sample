package com.example

import com.example.classes.FailureCode
import com.example.classes.StatusCode

interface Monoid<T> {
    val zero : T
    fun append(left:T, right:T) : T
    // append(zero, x) = x
    // append(x, zero) = x
    // append(x, append(y,z)) == append(append(x,y), z)
    // x + (y + z) == (x + y) + z           // associativiteit
}

object IntegersAdd : Monoid<Int> {
    override val zero: Int = 0

    override fun append(left: Int, right: Int)
        = left + right
}

fun Iterable<Int>.sum()
    = this.aggregate(IntegersAdd)

fun main() {
    println(listOf(1,2,3,4,5).sum())
}

object IntegersMul : Monoid<Int> {
    override val zero: Int = 1

    override fun append(left: Int, right: Int)
        = left * right
}

fun Iterable<Int>.product()
    = this.aggregate(IntegersMul)

object Strings : Monoid<String> {
    override val zero = ""
    override fun append(left: String, right: String)
        = left + right
}

fun Iterable<String>.concatAll()
    = this.aggregate(Strings)

class Lists<T>() : Monoid<List<T>> {
    override val zero = emptyList<T>()
    override fun append(left: List<T>, right: List<T>)
        = left + right
}

fun <T> Iterable<List<T>>.concatAll()
    = this.aggregate(Lists())

object BooleansAnd : Monoid<Boolean> {
    override val zero = true
    override fun append(left: Boolean, right: Boolean)
            = left && right

}
object BooleansOr : Monoid<Boolean> {
    override val zero = false
    override fun append(left: Boolean, right: Boolean)
            = left || right
}

fun <T> Iterable<T>.aggregate(monoid: Monoid<T>) : T
{
    var result = monoid.zero
    for(x in this)
        result = monoid.append(result, x)
    return result
}
fun <T> Iterable<T>.aggregate2(monoid: Monoid<T>) : T
{
    var result = monoid.zero
    for(x in this)
        result = monoid.append(x, result)
    return result
}

fun <T> List<T>.aggregate3(monoid: Monoid<T>) : T
{
    return when(this.size){
        0 -> monoid.zero
        1 -> this[0]
        else -> monoid.append(this.take(this.size/2).aggregate3(monoid), this.drop(this.size/2).aggregate3(monoid))
    }
}

class Validation1<T> : Monoid<(T) -> Boolean>{
    override val zero: (T) -> Boolean
        = { true }

    override fun append(left: (T) -> Boolean, right: (T) -> Boolean) : (T) -> Boolean
        = { left(it) && right(it) }
}

open class Functions<T,U>(val outputMonoid: Monoid<U>) : Monoid<(T) -> U> {
    override val zero: (T) -> U
        = { outputMonoid.zero }

    override fun append(left: (T) -> U, right: (T) -> U): (T) -> U
        = { outputMonoid.append(left(it), right(it)) }
}

class Validation1b<T>() : Functions<T,Boolean>(BooleansAnd) { }

class Validation2<T> : Monoid<(T) -> List<String>> {
    override val zero: (T) -> List<String>
        = { emptyList() }

    override fun append(left: (T) -> List<String>, right: (T) -> List<String>): (T) -> List<String>
        = { left(it) + right(it) }
}

class Validation2b<T>() : Functions<T, List<String>>(Lists())

class Person(val firstName:String, val lastName:String) {

}
fun validatePerson(p: Person) : List<String>
    = validateNotEmpty(p.firstName) + validateNotEmpty(p.lastName)

fun validateNotEmpty(s:String) = if (s.isNullOrBlank()) listOf("String is empty") else emptyList()

fun validatePerson2(p: Person) : Res<Person> {
    val errors = validatePerson(p)
    if(errors.size==0)
        return Res.Success(p)
    else
        return Res.Failure(errors)
}

fun validateNotEmpty2(s:String, name: String) : Res<String>
    = if(s.isNullOrBlank()) Res.Failure(listOf("String $name is empty.")) else Res.Success(s)

fun validatePerson3(p: Person) : Res<Person>
    = validateNotEmpty2(p.firstName, "firstName").app(validateNotEmpty2(p.lastName, "lastName"))
    {
            t, u -> Person(t, u)
    }

sealed class Res<out T> {
    data class Success<T>(val value: T, val statusCode: StatusCode = StatusCode.Ok) : Res<T>()
    data class Failure(val errors: List<String>) : Res<Nothing>()

    fun <U> map(f: (T) -> U) = when (this) {
        is Success<T> -> Success(f(value))
        is Failure -> this
    }

    fun filter(f: (T) -> Boolean) = when (this) {
        is Success -> if (f(value)) this else Failure(listOf("Fout."))
        is Failure -> this
    }
}


fun <T,U,V> Res<T>.app(u: Res<U>, f: (T,U) -> V)
    = when (this) {
        is Res.Success -> when(u) {
            is Res.Success -> Res.Success(f(this.value, u.value))
            is Res.Failure -> Res.Failure(u.errors)
        }
        is Res.Failure -> when(u) {
            is Res.Success -> Res.Failure(this.errors)
            is Res.Failure -> Res.Failure(Lists<String>().append(this.errors, u.errors))
    }
}

class ResMonoid<T>(val monoid: Monoid<T>) : Monoid<Res<T>> {
    override val zero = Res.Success (monoid.zero)

    override fun append(left: Res<T>, right: Res<T>): Res<T>
        = left.app(right) { t,u -> monoid.append(t,u) }

}