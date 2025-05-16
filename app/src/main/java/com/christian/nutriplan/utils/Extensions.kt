// File: app/src/main/java/com/christian/nutriplan/utils/Extensions.kt
package com.christian.nutriplan.utils

fun String.isValidEmail(): Boolean {
    return android.util.Patterns.EMAIL_ADDRESS.matcher(this).matches()
}