package com.whistlehub.playlist.di

import android.content.Context
import androidx.media3.exoplayer.ExoPlayer
import com.whistlehub.playlist.data.TrackRepository
import com.whistlehub.playlist.data.TrackRepositoryImpl
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
    fun provideTrackRepository(@ApplicationContext context: Context): TrackRepository {
        return TrackRepositoryImpl(context)
    }
}