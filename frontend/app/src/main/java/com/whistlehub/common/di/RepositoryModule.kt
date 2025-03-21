package com.whistlehub.common.di

import com.whistlehub.common.data.remote.api.*
import com.whistlehub.common.data.repository.*
import com.whistlehub.common.util.TokenRefresh
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideTokenRefresh(authApi: AuthApi, tokenManager: com.whistlehub.common.util.TokenManager): TokenRefresh {
        return TokenRefresh(tokenManager, AuthService(authApi))
    }

    @Provides
    @Singleton
    fun provideAuthService(authApi: AuthApi, tokenRefresh: TokenRefresh): AuthService {
        return AuthService(authApi, tokenRefresh)
    }

    @Provides
    @Singleton
    fun provideProfileService(profileApi: ProfileApi, tokenRefresh: TokenRefresh): ProfileService {
        return ProfileService(profileApi, tokenRefresh)
    }

    @Provides
    @Singleton
    fun providePlaylistService(playlistApi: PlaylistApi, tokenRefresh: TokenRefresh): PlaylistService {
        return PlaylistService(playlistApi, tokenRefresh)
    }

    @Provides
    @Singleton
    fun provideTrackService(trackApi: TrackApi, tokenRefresh: TokenRefresh): TrackService {
        return TrackService(trackApi, tokenRefresh)
    }

    @Provides
    @Singleton
    fun provideWorkstationService(workstationApi: WorkstationApi, tokenRefresh: TokenRefresh): WorkstationService {
        return WorkstationService(workstationApi, tokenRefresh)
    }

    @Provides
    @Singleton
    fun provideRankingService(rankingApi: RankingApi, tokenRefresh: TokenRefresh): RankingService {
        return RankingService(rankingApi, tokenRefresh)
    }
}