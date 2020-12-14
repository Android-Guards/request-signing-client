package xyz.fi5t.client.internal.di

import android.app.Application
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.crypto.tink.KeysetHandle
import com.google.crypto.tink.PublicKeySign
import com.google.crypto.tink.integration.android.AndroidKeysetManager
import com.google.crypto.tink.signature.EcdsaSignKeyManager
import com.google.crypto.tink.signature.SignatureConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import xyz.fi5t.client.internal.network.Api
import xyz.fi5t.client.internal.network.AuthInterceptor
import xyz.fi5t.client.internal.network.SigningInterceptor
import javax.inject.Singleton


@Module
@InstallIn(ApplicationComponent::class)
class MainModule {
    companion object {
        private const val KEYSET_NAME = "master_signature_keyset"
        private const val PREFERENCE_FILE = "master_signature_key_preference"
        private const val MASTER_KEY_URI = "android-keystore://master_signature_key"
    }

    //TODO: Шифрованные префы не нужны
    @Singleton
    @Provides
    fun providePreferences(application: Application): SharedPreferences {
        val masterKey = MasterKey.Builder(application)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        return EncryptedSharedPreferences.create(
            application,
            "main",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    @Singleton
    @Provides
    fun provideHttpClient(
        preferences: SharedPreferences,
        authInterceptor: AuthInterceptor,
        signingInterceptor: SigningInterceptor
    ): OkHttpClient {
        val logging = HttpLoggingInterceptor()
        logging.setLevel(HttpLoggingInterceptor.Level.BODY)
        logging.redactHeader("Authorization")

        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .addInterceptor(authInterceptor)
            .addNetworkInterceptor(signingInterceptor)
            .build()
    }

//    @Binds
//    fun provideAuthInterceptor(preferences: SharedPreferences): Interceptor {
//        return Interceptor { chain ->
//            val request = chain.request()
//                .newBuilder()
//                .apply {
//                    preferences.getString("access_token", null)?.let { token ->
//                        addHeader("Authorization", "Bearer $token")
//                    }
//                }.build()
//
//            chain.proceed(request)
//        }
//    }
//
//    @Binds
//    fun provideSigningInterceptor(): Interceptor {
//        return Interceptor { chain ->
//            val request = chain.request()
//                .newBuilder()
//                .build()
//
//            chain.proceed(request)
//        }
//    }

    @Singleton
    @Provides
    fun provideApi(httpClient: OkHttpClient): Api {
        return Retrofit.Builder()
            .baseUrl("http://192.168.14.36:8080/")
            .client(httpClient)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(Api::class.java)
    }

    @Singleton
    @Provides
    fun providePrivateKeysetHandle(application: Application): KeysetHandle {
        SignatureConfig.register()

        return AndroidKeysetManager.Builder()
            .withSharedPref(application, KEYSET_NAME, PREFERENCE_FILE)
            .withKeyTemplate(EcdsaSignKeyManager.ecdsaP256Template())
            .withMasterKeyUri(MASTER_KEY_URI)
            .build()
            .keysetHandle
    }

    @Singleton
    @Provides
    fun providePublicKeySign(keysetHandle: KeysetHandle): PublicKeySign {
        return keysetHandle.getPrimitive(PublicKeySign::class.java)
    }
}
