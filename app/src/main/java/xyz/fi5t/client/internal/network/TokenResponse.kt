package xyz.fi5t.client.internal.network

import com.squareup.moshi.JsonClass


@JsonClass(generateAdapter = true)
data class TokenResponse(val token: String)
