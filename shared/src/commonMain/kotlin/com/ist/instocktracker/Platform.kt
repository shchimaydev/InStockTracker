package com.ist.instocktracker

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform