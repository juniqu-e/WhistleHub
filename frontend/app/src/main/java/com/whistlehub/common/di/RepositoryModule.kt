package com.whistlehub.common.di

import com.whistlehub.common.data.remote.api.*
import com.whistlehub.common.data.repository.*
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
    fun provideAuthService(authApi: AuthApi): AuthService {
        return AuthService(authApi)
    }

    @Provides
    @Singleton
    fun provideProfileService(profileApi: ProfileApi): ProfileService {
        return ProfileService(profileApi)
    }

    @Provides
    @Singleton
    fun providePlaylistService(playlistApi: PlaylistApi): PlaylistService {
        return PlaylistService(playlistApi)
    }

    @Provides
    @Singleton
    fun provideTrackService(trackApi: TrackApi): TrackService {
        return TrackService(trackApi)
    }

    @Provides
    @Singleton
    fun provideWorkstationService(workstationApi: WorkstationApi): WorkstationService {
        return WorkstationService(workstationApi)
    }

    @Provides
    @Singleton
    fun provideRankingService(rankingApi: RankingApi): RankingService {
        return RankingService(rankingApi)
    }
}