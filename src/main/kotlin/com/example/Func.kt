package com.example

import com.example.classes.FailureCode
import com.example.classes.Res
import com.example.classes.toResult
import kotlin.reflect.jvm.internal.impl.load.java.components.JavaPropertyInitializerEvaluator

fun <T, U> Res<T>.map(f: (T) -> U) =
    when(this){
        is Res.Success<T> -> f(value).toResult()
        is Res.Failure -> this
    }

fun <T,U> Res<T>.flatMap(f: (T) -> Res<U>) =
    when(this){
        is Res.Success<T> -> f(value)
        is Res.Failure -> this
    }
// List<T> , (T -> U) -> List<U>
// Res<T> , (T -> U) -> Res<U>
// List<T> , (T -> List<U>) -> List<U>
// Res<T>, (T -> Res<U>) -> Res<U>

//M<T> , (T -> M<U>) -> M<U>

fun main(){
    val x = Lazy<Int>{ 3.also { println("eval1")} }
    val y = x.flatMap { Lazy { it+4 } }
    println(y.value)
}

class Lazy<T>(private val evaluator: () -> T){
    private var innerValue : T? = null
    val value: T
        get () {
            innerValue = innerValue ?: evaluator()
            return innerValue!!
        }
}

// Lazy<T> , (T->U) -> Lazy<U>
fun <T,U> Lazy<T>.map(f: (T) -> U)
    = Lazy { f(value) }

// Lazy<T> , (T->Lazy<U>) -> Lazy<U>
fun <T,U> Lazy<T>.flatMap(f: (T) -> Lazy<U>)
    = Lazy { f(value).value }

// SideEffectful<T> , ((T) -> SideEffectful<U> ) -> SideEffectful<U>

class SideEffectful<T>(private val value: T) {
    fun <U> sequence(f: (T) -> SideEffectful<U>) : SideEffectful<U>
            = f(this.value)

    companion object {
        fun <T,U,V> SideEffectful<(T,U) -> V>.apply(first : SideEffectful<T>, second:SideEffectful<U>) : SideEffectful<V> {
            val f = this.value
            val x = first.value
            val y = second.value
            return SideEffectful(f(x,y))
        }
    }
}

fun getValueFromApi() : SideEffectful<Int> {
    return SideEffectful(42)
}

fun realPrintln(str: String) : SideEffectful<Unit>
{
    kotlin.io.println(str)
    return SideEffectful(Unit)


}

fun test() {
    val x = getValueFromApi().sequence { realPrintln(it.toString()) }
}

//  SideEffectful((T,U) -> V), SideEffectful<T> , SideEffectful<U> -> SideEffectful<V>

