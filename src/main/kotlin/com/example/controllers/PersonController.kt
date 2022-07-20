package com.example.controllers

import com.example.classes.FailureCode
import com.example.classes.Res
import com.example.classes.StatusCode
import com.example.classes.toResult
import com.example.models.Person
import kotlin.random.Random

val persons = listOf(
    Person("1", "Joost", "Morsink"),
    Person("2", "Thomas", "Brandhorst"),
    Person("3", "Dave", "Tak"),
    Person("4", "Koen", "Zwijnenburg"),
    Person("5", "Daniël", "Groen"),
).associateBy { it.id }.toMutableMap();

class PersonController : Controller<Person> {
    override fun get() = persons.toList().map { it.second }.toResult()

    override fun getOne(id: String?): Res<Person> {
        if (!persons.contains(id)) {
            return Res.Failure(FailureCode.NotFound)
        }

        return persons[id]!!.toResult()
    }

    override fun delete(id: String?): Res<Unit> {
        if (!persons.contains(id)) {
            return Res.Failure(FailureCode.NotFound)
        }

        persons.remove(id).toResult()

        return Unit.toResult(StatusCode.NoContent)
    }

    override fun create(item: Person): Res<Person> {
        val id = Random.nextInt(5, 30).toString() // Demonstration purposes
        val person = Person(id, item.firstName, item.lastName)
        persons[id] = person;

        return person.toResult(StatusCode.Created)
    }

    override fun put(id: String?, item: Person): Res<Person> {
        if (!persons.contains(id)) {
            return Res.Failure(FailureCode.NotFound)
        }

        /**
         * There's probably a better way of doing this :/
         */
        val person = persons[id]!!.copy()
        person.firstName = item.firstName
        person.lastName = item.lastName

        persons[id] = person;

        return person.toResult()
    }
}