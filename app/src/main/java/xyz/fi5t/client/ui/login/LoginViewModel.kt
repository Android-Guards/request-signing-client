package xyz.fi5t.client.ui.login

import android.content.SharedPreferences
import android.util.Base64
import android.util.Log
import androidx.core.content.edit
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.crypto.tink.BinaryKeysetWriter
import com.google.crypto.tink.KeysetHandle
import kotlinx.coroutines.launch
import retrofit2.HttpException
import xyz.fi5t.client.R
import xyz.fi5t.client.internal.network.Api
import xyz.fi5t.client.internal.network.LoginRequest
import xyz.fi5t.client.internal.viewmodel.SingleLiveEvent
import java.io.ByteArrayOutputStream


class LoginViewModel @ViewModelInject constructor(
    private val api: Api,
    private val preferences: SharedPreferences,
    private val keysetHandle: KeysetHandle
) : ViewModel() {
    val isLoading = MutableLiveData<Boolean>()
    val error = MutableLiveData<String>()
    val showUser = MutableLiveData<Boolean>()

    fun signIn(username: String, password: String) {
        viewModelScope.launch {
            isLoading.value = true

            val bos = ByteArrayOutputStream()
            val w = BinaryKeysetWriter.withOutputStream(bos)

            keysetHandle.publicKeysetHandle.writeNoSecret(w)

            try {
                val response = api.login(
                    LoginRequest(
                        username,
                        password,
                        Base64.encodeToString(bos.toByteArray(), Base64.DEFAULT)
                    )
                )

                bos.close()

                preferences.edit {
                    putString("access_token", response.token)
                }.also {
                    showUser.value = true
                }
            } catch (e: Exception) {
                error.value = when (e) {
                    is HttpException -> e.response()?.errorBody()?.string()
                    else -> "Unknown error: ${e.localizedMessage}"
                }
                Log.e("REQUEST ERROR", e.localizedMessage)
            } finally {
                isLoading.value = false
            }
        }
    }
}
