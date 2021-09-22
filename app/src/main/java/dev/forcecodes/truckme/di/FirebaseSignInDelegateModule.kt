package dev.forcecodes.truckme.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.forcecodes.truckme.core.di.ApplicationScope
import dev.forcecodes.truckme.core.domain.signin.ObserveAuthStateUseCase
import dev.forcecodes.truckme.ui.auth.signin.FirebaseSignInViewModelDelegate
import dev.forcecodes.truckme.ui.auth.signin.SignInViewModelDelegate
import kotlinx.coroutines.CoroutineScope
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object FirebaseSignInDelegateModule {

  @Singleton
  @Provides
  fun providesSignInViewModelDelegate(
    dataSource: ObserveAuthStateUseCase,
    @ApplicationScope applicationScope: CoroutineScope
  ): SignInViewModelDelegate {
    return FirebaseSignInViewModelDelegate(
      dataSource, applicationScope
    )
  }
}