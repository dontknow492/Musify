package org.ghost.musify

import android.app.Application
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.decode.BitmapFactoryDecoder
import coil3.util.DebugLogger
import dagger.hilt.android.HiltAndroidApp


@HiltAndroidApp
class MyApplication : Application(), SingletonImageLoader.Factory {
    override fun newImageLoader(context: PlatformContext): ImageLoader {
        return ImageLoader.Builder(context)
            .components {
                add(BitmapFactoryDecoder.Factory())
            }
            .logger(DebugLogger())
            .build()
    }

}