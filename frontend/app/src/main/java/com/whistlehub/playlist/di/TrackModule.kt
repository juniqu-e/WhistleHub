package com.whistlehub.playlist.di

import android.content.Context
import androidx.media3.exoplayer.ExoPlayer
import com.whistlehub.common.data.local.room.UserRepository
import com.whistlehub.common.data.repository.ProfileService
import com.whistlehub.common.data.repository.TrackService
import com.whistlehub.playlist.viewmodel.TrackPlayViewModel
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TrackModule {
    @Provides
    @Singleton
    fun provideExoPlayer(@ApplicationContext context: Context): ExoPlayer {
        return ExoPlayer.Builder(context).build()
    }

    @Provides
    @Singleton
    fun provideTrackPlayViewModel(
        @ApplicationContext context: Context,
        trackService: TrackService,
        profileService: ProfileService,
        userRepository: UserRepository,
    ): TrackPlayViewModel {
        return TrackPlayViewModel(context, trackService, profileService, userRepository)
    }
}