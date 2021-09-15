package dev.forcecodes.truckme.util

import android.content.Context
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.load.DecodeFormat.PREFER_RGB_565
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.cache.InternalCacheDiskCacheFactory
import com.bumptech.glide.load.engine.cache.LruResourceCache
import com.bumptech.glide.module.AppGlideModule
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.signature.ObjectKey
import java.util.*

@GlideModule
class GlideModule : AppGlideModule() {
    override fun applyOptions(context: Context, builder: GlideBuilder) {
        super.applyOptions(context, builder)
        builder.apply {
            setMemoryCache(LruResourceCache(memoryCacheSizeBytes))
            setDiskCache(InternalCacheDiskCacheFactory(context, memoryCacheSizeBytes))
            setDefaultRequestOptions(requestOptions)
        }
    }

    companion object {
        private val requestOptions: RequestOptions
            get() {
                return RequestOptions()
                    .dontAnimate()
                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                    .format(PREFER_RGB_565)
                    .skipMemoryCache(false)
            }
    }
}

private const val memoryCacheSizeBytes = 1024 * 1024 * 100L // 10 MB

private fun charSetToString(id: Long): String? {
    var buffer = id
    val buf = CharArray(32)
    val x64Bit = 64
    var x86Bit = 32
    val bit = x64Bit - 1.toLong()
    do {
        buf[--x86Bit] = DIGITS66[(buffer and bit).toInt()]
        buffer = buffer ushr 6
    } while (buffer != 0L)
    return String(buf, x86Bit, 32 - x86Bit)
}

private val DIGITS66 = charArrayOf(
    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
    'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j',
    'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't',
    'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D',
    'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N',
    'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X',
    'Y', 'Z', '-', '.', '_', '~'
)