package xyz.fi5t.client.internal.network

import android.util.Base64
import com.google.crypto.tink.PublicKeySign
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.security.MessageDigest
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class SigningInterceptor @Inject constructor(val signer: PublicKeySign) : Interceptor {
    private val headerNames =
        mutableListOf("authorization", "user-agent", "date") // The order is matter!

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        if (originalRequest.url.encodedPath.contains("/login") && originalRequest.method == "POST") {
            return chain.proceed(originalRequest)
        }

        val additionalHeaders = mutableMapOf(
            "Date" to LocalDateTime.now().toString() // It must be synchronized with a your backend!
        )

        originalRequest.body?.let {
            val body = okio.Buffer()
            val digest = MessageDigest.getInstance("SHA-512").apply { reset() }

            it.writeTo(body)

            headerNames += "digest"
            additionalHeaders["Digest"] = digest.digest(body.readByteArray()).joinToString("") { "%02x".format(it) }
        }

//TODO: Помитмать это все дело с charles

        val headersToSign = headersToSign(originalRequest, additionalHeaders)

        val requestTarget =
            "(request-target): ${originalRequest.method.toLowerCase()} ${originalRequest.url.encodedPath}\n"

        val signatureData =
            requestTarget + headerNames.joinToString("\n") {
                "$it: ${headersToSign[it]}"
            }

        val signature =
            Base64.encodeToString(signer.sign(signatureData.toByteArray()), Base64.NO_WRAP)

        val request = originalRequest.newBuilder().apply {
            additionalHeaders.forEach {
                addHeader(it.key, it.value)
            }
        }.addHeader("X-Signed-Headers", headerNames.joinToString(separator = " "))
            .addHeader("X-Signature", signature)

        return chain.proceed(request.build())
    }

    private fun headersToSign(
        request: Request,
        additionalHeaders: Map<String, String>
    ): Map<String, String> {
        val requestHeaders = request.headers.filter { headerNames.contains(it.first.toLowerCase()) }
            .associateBy({ it.first.toLowerCase() }, { it.second })

        return requestHeaders + additionalHeaders.mapKeys { it.key.toLowerCase() }
    }
}
