package xyz.fi5t.client.internal.network

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST


interface Api {
    @POST("login")
    suspend fun login(@Body loginRequest: LoginRequest): TokenResponse

    @POST("refresh")
    suspend fun refresh(): TokenResponse

    @GET("user")
    suspend fun getUser(): UserResponse

    @POST("user")
    suspend fun changeUserName(@Body user: UserResponse): UserResponse
}
