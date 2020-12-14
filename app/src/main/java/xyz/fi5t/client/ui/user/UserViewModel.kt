package xyz.fi5t.client.ui.user

import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.crypto.tink.KeysetHandle
import kotlinx.coroutines.launch
import retrofit2.HttpException
import xyz.fi5t.client.internal.network.Api
import xyz.fi5t.client.internal.network.UserResponse


class UserViewModel @ViewModelInject constructor(
    private val api: Api,
    private val preferences: SharedPreferences,
) : ViewModel() {
    init {
        loadUserInfo()
    }

    val username = MutableLiveData<String>()
    val error = MutableLiveData<String>()
    val showLogin = MutableLiveData<Boolean>()

    private fun loadUserInfo() {
        viewModelScope.launch {
            try {
                val response = api.getUser()
                username.value = response.userName
            } catch (e: Exception) {
                error.value = when (e) {
                    is HttpException -> e.response()?.errorBody()?.string()
                    else -> "Unknown error: ${e.localizedMessage}"
                }
                Log.e("REQUEST ERROR", e.localizedMessage)
            }
        }
    }

    fun logout() {
        preferences.edit { clear() }
        showLogin.value = true
    }
}
