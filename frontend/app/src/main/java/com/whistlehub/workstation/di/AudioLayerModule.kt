package com.whistlehub.workstation.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AudioLayerModule {
    @Binds
    @Singleton
    abstract fun bindAudioLayerPlayer(
        impl: AudioLayerPlayerImpl
    ): AudioLayerPlayer
}