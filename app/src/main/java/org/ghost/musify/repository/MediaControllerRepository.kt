package org.ghost.musify.repository

import android.util.Log
import androidx.media3.session.MediaController
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.guava.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * A repository to manage the connection to the MediaSessionService.
 *
 * This class is a Singleton, meaning there will be only one instance of it
 * for the entire application lifecycle. It safely handles the asynchronous
 * connection to the MediaController.
 *
 * @param controllerFuture The ListenableFuture provided by the Hilt module.
 */
@Singleton
class MediaControllerRepository @Inject constructor(
    private val controllerFuture: ListenableFuture<MediaController>
) {
    /**
     * A Flow that emits the connected MediaController once it's ready.
     *
     * ViewModels can collect this Flow to get the controller instance safely.
     * The `flow` builder combined with `.await()` handles the suspension
     * until the connection to the service is complete.
     */
    val mediaController: Flow<MediaController> = flow {
        emit(controllerFuture.await())
        Log.d("MediaControllerRepository", "MediaController connected")
    }
}
