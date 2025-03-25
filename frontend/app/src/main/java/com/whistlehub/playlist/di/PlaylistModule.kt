package com.whistlehub.playlist.di

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
    fun providePlaylistViewModel(): PlaylistViewModel {
        return PlaylistViewModel()
    }
}