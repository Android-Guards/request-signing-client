package xyz.fi5t.client.internal.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass


@JsonClass(generateAdapter = true)
data class LoginRequest(
    val username: String,
    val password: String,

    @Json(name = "public_key")
    val publicKey: String
)
