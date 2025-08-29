package org.ghost.musify.di

import android.content.ComponentName
import android.content.Context
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.internal.Contexts.getApplication
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.ghost.musify.service.MusicService
import javax.inject.Singleton

//import androidx.media3.session.ComponentName


@Module
@InstallIn(SingletonComponent::class) // Lives as long as the app
object MediaControllerModule {
    @Provides
    @Singleton // We only want one instance of the controller
    fun provideSessionToken(@ApplicationContext context: Context): SessionToken {
        return SessionToken(
            context,
            ComponentName(
                getApplication(context),
                MusicService::class.java
            )
        )
    }

    @Provides
    @Singleton
    fun provideMediaController(
        @ApplicationContext context: Context,
        sessionToken: SessionToken
    ): ListenableFuture<MediaController> {
        // MediaController.Builder returns a ListenableFuture
        return MediaController.Builder(context, sessionToken).buildAsync()

        // Use .await() from kotlinx-coroutines-guava to suspend until the connection is complete
    }

}