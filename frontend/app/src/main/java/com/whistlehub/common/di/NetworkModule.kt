package com.whistlehub.common.di

import com.whistlehub.common.data.remote.api.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * 네트워크 통신 의존성 주입 모듈
 * 이 파일은 Dagger Hilt를 활용하여 애플리케이션 전역에서 사용할 네트워크 관련 의존성들을 관리하는 모듈을 정의합니다.
 * 주요 기능:
 * - OkHttpClient 생성:
 *   - 일반 API용 클라이언트: AuthInterceptor를 포함하여 인증 토큰을 헤더에 자동 추가하며, HttpLoggingInterceptor를 통해 요청/응답 로그를 기록합니다.
 *   - 인증 API용 클라이언트: 인증 관련 요청에 사용되며, AuthInterceptor 없이 생성되어 토큰 헤더가 추가되지 않습니다.
 *
 * - Retrofit 인스턴스 생성:
 *   - 일반 API 호출용 Retrofit: 인증이 필요한 API 호출에 사용되는 OkHttpClient를 활용합니다.
 *   - 인증 API 호출용 Retrofit: 인증 전용 OkHttpClient를 사용하여 인증 관련 API 호출 시 액세스 토큰이 자동으로 포함되지 않도록 구성합니다.
 *
 * - API 인터페이스 제공:
 *   - AuthApi: 인증 관련 API 호출(로그인, 회원가입 등)을 위한 인터페이스.
 *   - ProfileApi, PlaylistApi, TrackApi, WorkstationApi, RankingApi: 각각 사용자 프로필, 플레이리스트, 트랙, 워크스테이션, 랭킹 관련 API 호출을 위한 인터페이스.
 *
 * 이 모듈은 SingletonComponent에 설치되어 애플리케이션 전체에서 싱글턴으로 관리되며, 네트워크 통신의 설정과 관리를 중앙 집중화하여
 * 코드의 재사용성과 유지보수성을 향상시키는 역할을 합니다.
 */

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    /**
     * 일반 API용 OkHttpClient는 AuthInterceptor를 포함하여,
     * 액세스 토큰이 필요한 API 호출에 사용됩니다.
     */
    @Provides
    @Singleton
    fun provideOkHttpClient(authInterceptor: AuthInterceptor): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(authInterceptor) // 토큰에 헤더 추가
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    /**
     * 인증 전용 Retrofit 인스턴스: AuthApi 호출 시 사용되는 Retrofit 인스턴스
     */
    @Provides
    @Singleton
    fun provideAuthRetrofit(authOkHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://j12c104.p.ssafy.io/api/")
            .client(authOkHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    /**
     * AuthApi는 인증 관련 요청을 처리하므로 AuthInterceptor 없이 별도의 Retrofit 인스턴스를 사용합니다.
     */
    @Provides
    @Singleton
    fun provideAuthApi(authRetrofit: Retrofit): AuthApi {
        return authRetrofit.create(AuthApi::class.java)
    }

    /**
     * ProfileApi, PlaylistApi, TrackApi, WorkstationApi, RankingApi 등 토큰이 필요한 API들은
     * 기본 Retrofit 인스턴스를 사용합니다.
     */
    @Provides
    @Singleton
    fun provideProfileApi(retrofit: Retrofit): ProfileApi {
        return retrofit.create(ProfileApi::class.java)
    }

    @Provides
    @Singleton
    fun providePlaylistApi(retrofit: Retrofit): PlaylistApi {
        return retrofit.create(PlaylistApi::class.java)
    }

    @Provides
    @Singleton
    fun provideTrackApi(retrofit: Retrofit): TrackApi {
        return retrofit.create(TrackApi::class.java)
    }

    @Provides
    @Singleton
    fun provideWorkstationApi(retrofit: Retrofit): WorkstationApi {
        return retrofit.create(WorkstationApi::class.java)
    }

    @Provides
    @Singleton
    fun provideRankingApi(retrofit: Retrofit): RankingApi {
        return retrofit.create(RankingApi::class.java)
    }
}