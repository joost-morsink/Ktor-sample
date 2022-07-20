package com.example.models

import kotlinx.serialization.Serializable

@Serializable
data class Person(val id: String? = "", var firstName: String, var lastName: String) {
}