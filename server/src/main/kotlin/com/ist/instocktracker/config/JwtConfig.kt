package com.ist.instocktracker.config

import io.ktor.server.application.*

data class JwtConfig(
    val secret: String,
    val issuer: String,
    val audience: String,
    val realm: String,
    val accessTokenTtlSec: Long,
    val refreshTokenTtlSec: Long
) {
    companion object {
        fun fromEnvironment(environment: ApplicationEnvironment): JwtConfig {
            val cfg = environment.config
            return JwtConfig(
                secret = cfg.propertyOrNull("app.jwt.secret")?.getString() ?: System.getenv("JWT_SECRET")
                ?: "dev-secret-change-me",
                issuer = cfg.propertyOrNull("app.jwt.issuer")?.getString() ?: System.getenv("JWT_ISSUER")
                ?: "instocktracker-server",
                audience = cfg.propertyOrNull("app.jwt.audience")?.getString() ?: System.getenv("JWT_AUDIENCE")
                ?: "instocktracker-clients",
                realm = cfg.propertyOrNull("app.jwt.realm")?.getString() ?: System.getenv("JWT_REALM")
                ?: "instocktracker",
                accessTokenTtlSec = cfg.propertyOrNull("app.jwt.accessTokenTtlSec")?.getString()?.toLongOrNull()
                    ?: 15 * 60L,
                refreshTokenTtlSec = cfg.propertyOrNull("app.jwt.refreshTokenTtlSec")?.getString()?.toLongOrNull()
                    ?: 30L * 24 * 60 * 60
            )
        }
    }
}