package com.github.noamm9.nvgrenderer.nvg

import com.github.noamm9.nvgrenderer.WebUtils
import kotlinx.coroutines.runBlocking
import org.lwjgl.system.MemoryUtil
import java.io.File
import java.io.FileNotFoundException
import java.nio.ByteBuffer

class Image(
    val location: String,
    var isSVG: Boolean = false,
    var bytes: ByteArray = getBytes(location),
    private var buffer: ByteBuffer? = null
) {
    init {
        isSVG = location.endsWith(".svg", true)
    }

    fun buffer(): ByteBuffer {
        if (buffer == null) buffer = MemoryUtil.memAlloc(bytes.size).put(bytes).flip() as ByteBuffer
        return buffer ?: throw IllegalStateException("Image has no data")
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Image) return false
        return location == other.location
    }

    override fun hashCode(): Int {
        return location.hashCode()
    }

    companion object {
        private fun getBytes(path: String): ByteArray {
            val trimmedPath = path.trim()

            if (trimmedPath.startsWith("http")) return runBlocking { WebUtils.downloadBytes(trimmedPath) }
            File(trimmedPath).takeIf { it.exists() && it.isFile }?.let { return it.readBytes() }

            val resourcePath = trimmedPath.removePrefix("/")
            val stream = Thread.currentThread().contextClassLoader.getResourceAsStream(resourcePath)
                ?: Image::class.java.getResourceAsStream("/$resourcePath")

            return stream?.use { it.readBytes() } ?: throw FileNotFoundException("Could not find: $trimmedPath")
        }
    }
}