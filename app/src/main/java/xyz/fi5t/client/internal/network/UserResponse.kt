package xyz.fi5t.client.internal.network

import com.squareup.moshi.JsonClass


@JsonClass(generateAdapter = true)
class UserResponse(val userName: String)
