package com.github.noamm9.nvgrenderer.nvg

import com.github.noamm9.nvgrenderer.WebUtils
import kotlinx.coroutines.runBlocking
import org.lwjgl.system.MemoryUtil
import java.io.File
import java.io.FileNotFoundException
import java.nio.ByteBuffer
import java.nio.file.Files

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
            return if (trimmedPath.startsWith("http")) runBlocking { WebUtils.downloadBytes(trimmedPath) }
            else {
                val file = File(trimmedPath)
                if (file.exists() && file.isFile) Files.newInputStream(file.toPath()).use { it.readBytes() }
                else this::class.java.getResourceAsStream(trimmedPath)?.use { it.readBytes() } ?: throw FileNotFoundException(trimmedPath)
            }
        }
    }
}