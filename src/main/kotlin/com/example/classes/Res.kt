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
sealed class FailureCode2 {
    data class NotFound() : FailureCode2()
    data class NotAuthorized() : FailureCode2()
    data class BadRequest(val messages: List<ValidationMessage>) : FailureCode2()
}

data class WithMetadata<T>(val metadata:Metadata, val data:T)
data class Metadata(val location:String?, val asynchronous: Boolean)

data class ValidationMessage(val message:String, val code:String, val severity:Severity)
enum class Severity { Warning, Error }


fun FailureCode.toResult() = Res.Failure(this)
fun <T> T.toResult() = Res.Success(this)
fun <T> T.toResult(statusCode: StatusCode) = Res.Success(this, statusCode)