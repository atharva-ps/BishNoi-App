package com.justbaat.mybishnoiapp.utils

import android.util.Patterns

fun String.isValidUsername(): Boolean {
    if (this.length !in 3..16) return false

    // Must start with a lowercase letter
    if (!this.first().isLowerCase() || !this.first().isLetter()) return false

    // Can only contain lowercase letters, numbers, and underscores
    val usernameRegex = "^[a-z][a-z0-9_]*$".toRegex()
    return this.matches(usernameRegex)
}

fun String.isValidEmail(): Boolean {
    return this.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(this).matches()
}

fun String.isValidPassword(): Boolean {
    return this.length >= Constants.MIN_PASSWORD_LENGTH
}

fun String.isValidName(): Boolean {
    return this.length >= Constants.MIN_NAME_LENGTH
}
