package com.ist.instocktracker.utils

fun String.capitalizeFirstLetter(): String = replaceFirstChar { it.uppercase() }
fun String.capitalizeWords(): String {
    return this.split(" ").joinToString(" ") { word ->
        word.replaceFirstChar { char ->
            if (char.isLowerCase()) char.titlecase() else char.toString()
        }
    }
}