package com.thetomcatmind.soundlauncher

import com.google.firebase.Timestamp


// Definimos una clase de datos para el usuario
data class User(
    val username: String,
    val email: String,
    val name: String,
    val surname: String,
    val phone: Int?,
    val birthday: Timestamp?
)