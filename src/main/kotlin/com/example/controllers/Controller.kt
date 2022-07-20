package com.example.controllers

import com.example.classes.Res

interface Controller<T> {
    fun get() : Res<List<T>>
    fun getOne(id: String?) : Res<T>
    fun put(id: String?, item: T) : Res<T>
    fun create(item: T): Res<T>
    fun delete(id: String?) : Res<Unit>
}