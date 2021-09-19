package dev.forcecodes.truckme.core.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CoroutinesModule {

  @DefaultDispatcher
  @Provides
  fun providesDefaultDispatcher(): CoroutineDispatcher = Dispatchers.Default

  @IoDispatcher
  @Provides
  fun providesIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

  @MainDispatcher
  @Provides
  fun providesMainDispatcher(): CoroutineDispatcher = Dispatchers.Main

  @MainImmediateDispatcher
  @Provides
  fun providesMainImmediateDispatcher(): CoroutineDispatcher = Dispatchers.Main.immediate

  @ApplicationScope
  @Singleton
  @Provides
  fun providesApplicationScope(
    @DefaultDispatcher defaultDispatcher: CoroutineDispatcher
  ): CoroutineScope = CoroutineScope(SupervisorJob() + defaultDispatcher)
}