package com.whistlehub.search.di

import com.whistlehub.common.data.repository.TrackService
import com.whistlehub.search.viewmodel.SearchViewModel
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SearchModule {
    @Provides
    @Singleton
    fun provideSearchViewModel(
        trackService: TrackService
    ): SearchViewModel {
        return SearchViewModel(trackService)
    }
}