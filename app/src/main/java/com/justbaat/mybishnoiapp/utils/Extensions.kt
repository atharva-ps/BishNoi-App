package com.justbaat.mybishnoiapp.utils

import android.util.Patterns

fun String.isValidEmail(): Boolean {
    return this.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(this).matches()
}

fun String.isValidPassword(): Boolean {
    return this.length >= Constants.MIN_PASSWORD_LENGTH
}

fun String.isValidName(): Boolean {
    return this.length >= Constants.MIN_NAME_LENGTH
}
