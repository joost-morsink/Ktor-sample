package com.example.classes

sealed class Res<out T> {
    data class Success<T>(val value: T, val statusCode: StatusCode = StatusCode.Ok) : Res<T>()
    data class Failure(val failure: FailureCode) : Res<Nothing>()

    fun <U> map(f: (T) -> U) = when (this) {
        is Success<T> -> Success(f(value))
        is Failure -> this
    }

    fun filter(f: (T) -> Boolean) = when (this) {
        is Success -> if (f(value)) this else Failure(FailureCode.ValidationError)
        is Failure -> this
    }
}

enum class StatusCode {
    Ok, Created, NoContent
}

enum class FailureCode {
    NotFound, ValidationError, Unauthorized
}

fun FailureCode.toResult() = Res.Failure(this)
fun <T> T.toResult() = Res.Success(this)
fun <T> T.toResult(statusCode: StatusCode) = Res.Success(this, statusCode)