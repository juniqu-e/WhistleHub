package com.whistlehub.playlist.di

import android.content.Context
import androidx.media3.exoplayer.ExoPlayer
import com.whistlehub.common.data.repository.TrackService
import com.whistlehub.playlist.data.CommentRepository
import com.whistlehub.playlist.data.CommentRepositoryImpl
import com.whistlehub.playlist.viewmodel.TrackCommentViewModel
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
        exoPlayer: ExoPlayer, trackService: TrackService
    ): TrackPlayViewModel {
        return TrackPlayViewModel(exoPlayer, trackService)
    }

    @Provides
    @Singleton
    fun provideCommentRepository(@ApplicationContext context: Context): CommentRepository {
        return CommentRepositoryImpl()
    }

    @Provides
    @Singleton
    fun provideTrackCommentViewModel(commentRepository: CommentRepository): TrackCommentViewModel {
        return TrackCommentViewModel(commentRepository)
    }
}