package com.whistlehub.playlist.di

import com.whistlehub.common.data.local.room.UserRepository
import com.whistlehub.common.data.repository.PlaylistService
import com.whistlehub.playlist.viewmodel.PlaylistViewModel
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PlaylistModule {
    @Provides
    @Singleton
    fun providePlaylistViewModel(
        playlistService: PlaylistService,
        userRepository: UserRepository
    ): PlaylistViewModel {
        return PlaylistViewModel(playlistService, userRepository)
    }
}