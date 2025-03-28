package com.whistlehub.common.di

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 AppModule은 Dagger Hilt를 사용하여 앱 전역에서 필요한 의존성 객체를 제공하는 모듈입니다.
 여기서는 보안이 강화된 SharedPreferences 인스턴스를 제공하여,
 앱 내에서 토큰과 같은 민감한 데이터를 안전하게 저장할 수 있도록 합니다.
 */

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    /**
     provideSharedPreferences 함수는 암호화된 SharedPreferences 인스턴스를 생성하고 제공합니다.
     이 함수는 애플리케이션 컨텍스트를 주입받아 EncryptedSharedPreferences를 설정합니다.

     암호화된 SharedPreferences 사용 이유:
     - 일반 SharedPreferences는 저장되는 데이터를 평문으로 관리하기 때문에 보안에 취약합니다.
     - EncryptedSharedPreferences는 내부적으로 데이터를 암호화하여 저장하므로, 민감한 정보를 안전하게 보관할 수 있습니다.

     MasterKey:
     - EncryptedSharedPreferences를 사용하기 위해 필요한 키를 생성하는데 사용됩니다.
     - 여기서는 AES256_GCM 알고리즘을 사용하는 MasterKey를 생성합니다.

     * @param context 애플리케이션 컨텍스트를 주입받아 SharedPreferences를 초기화합니다.
     * @return 암호화된 SharedPreferences 인스턴스를 반환합니다.
     */
    @Provides
    @Singleton
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        val masterKeyBuilder = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        return EncryptedSharedPreferences.create(
            context,
            "WhistleHub",
            masterKeyBuilder,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }
}