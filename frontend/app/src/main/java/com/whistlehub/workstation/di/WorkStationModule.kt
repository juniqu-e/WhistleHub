package com.whistlehub.workstation.di

import com.whistlehub.workstation.view.WorkStationBottom
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
abstract class WorkStationModule {
    @Binds
    @Singleton
    abstract fun bindBottomBarProvider(
        workStationBottom: WorkStationBottom
    ): WorkStationBottomBarProvider
}