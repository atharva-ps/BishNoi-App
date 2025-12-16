package com.app.bishnoi.utils

object ValidationHelper {

    fun validateUsername(username: String): String? {
        return when {
            username.isEmpty() -> "Username is required"
            username.length < 3 -> "Username must be at least 3 characters"
            username.length > 16 -> "Username must be less than 16 characters"
            !username.matches(Regex("^[a-zA-Z0-9_]+$")) -> "Username can only contain letters, numbers, and underscores"
            !username.first().isLowerCase() || !username.first().isLetter()->
                "start with a lowercase letter"
            else -> null
        }
    }

    fun validateMobileNumber(mobile: String): String? {
        return when {
            mobile.isEmpty() -> null // Optional field
            mobile.length < 10 -> "Invalid mobile number"
            !mobile.matches(Regex("^[+]?[0-9]{10,15}$")) -> "Invalid mobile number format"
            else -> null
        }
    }

    fun validatePincode(pincode: String): String? {
        return when {
            pincode.isEmpty() -> null // Optional field
            pincode.length != 6 -> "Pincode must be 6 digits"
            !pincode.matches(Regex("^[0-9]{6}$")) -> "Invalid pincode format"
            else -> null
        }
    }

    fun validateUrl(url: String): String? {
        if (url.isEmpty()) return null // Optional field

        val urlPattern = Regex(
            "^(https?://)?" +
                    "([a-zA-Z0-9]([a-zA-Z0-9\\-]{0,61}[a-zA-Z0-9])?\\.)+[a-zA-Z]{2,}" +
                    "(/.*)?$"
        )

        return if (urlPattern.matches(url)) null else "Invalid URL format"
    }

    fun validateEmail(email: String): String? {
        return when {
            email.isEmpty() -> "Email is required"
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "Invalid email format"
            else -> null
        }
    }

    fun validateName(name: String, fieldName: String = "Name"): String? {
        return when {
            name.isEmpty() -> null // Optional field
            name.length < 2 -> "$fieldName must be at least 2 characters"
            name.length > 50 -> "$fieldName must be less than 50 characters"
            else -> null
        }
    }
}
