package xyz.fi5t.client.internal.network

import android.content.SharedPreferences
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class AuthInterceptor @Inject constructor(private val preferences: SharedPreferences) :
    Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()

        if (original.url.encodedPath.contains("/login") && original.method == "POST") {
            return chain.proceed(original)
        }

        val token = preferences.getString("access_token", "")
        val request = original.newBuilder()
            .addHeader("Authorization", "Bearer $token")
            .build()

        return chain.proceed(request)
    }
}
