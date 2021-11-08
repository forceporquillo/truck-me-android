package dev.forcecodes.truckme.core.di

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SecondaryFirebaseModule {

  @SecondaryFirebaseAuth
  @Singleton
  @Provides
  fun providesSecondaryFirebase(
    @ApplicationContext context: Context
  ): FirebaseAuth {

    val options = FirebaseOptions.Builder()
      .setApiKey("AIzaSyDBmrHfnsw-8VNXWDrL93qnXwK6kKrqYJ4")
      .setApplicationId("1:902271608154:android:0b342c555710ff7878ff63")
      .setDatabaseUrl("https://truckme-debug-default-rtdb.firebaseio.com/")
      .build()

    val firebaseApp = FirebaseApp.initializeApp(context, options, "secondary_firebase")

    return FirebaseAuth.getInstance(firebaseApp)
  }
}

@Retention(AnnotationRetention.BINARY)
@Qualifier
annotation class SecondaryFirebaseAuth