package com.github.noamm9.nvgrenderer


import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URI

object WebUtils {
    private const val PRIVATE_USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
    private val SUCCESS_RANGE = 200 .. 299

    suspend fun downloadBytes(url: String): ByteArray = withContext(Dispatchers.IO) {
        val connection = URI(url).toURL().openConnection() as HttpURLConnection
        connection.setRequestProperty("User-Agent", PRIVATE_USER_AGENT)
        connection.requestMethod = "GET"

        val code = connection.responseCode
        val stream = if (code in SUCCESS_RANGE) connection.inputStream else connection.errorStream
        if (code !in SUCCESS_RANGE) throw IllegalStateException("HTTP $code")

        stream.use { it.readBytes() }
    }
}