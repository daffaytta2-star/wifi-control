package com.axiom.wificontrol

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

object SpeedTest {

    data class Result(
        val pingMs: Long = 0,
        val downloadMbps: Double = 0.0,
        val uploadMbps: Double = 0.0,
        val error: String? = null
    )

    // Ping: ambil latency dari koneksi HTTP HEAD
    suspend fun ping(): Long = withContext(Dispatchers.IO) {
        try {
            val start = System.currentTimeMillis()
            val url = URL("https://www.google.com")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "HEAD"
            conn.connectTimeout = 5000
            conn.readTimeout = 5000
            conn.connect()
            val elapsed = System.currentTimeMillis() - start
            conn.disconnect()
            elapsed
        } catch (_: Exception) {
            0L
        }
    }

    // Download: ambil file test, ukur kecepatan
    suspend fun download(): Double = withContext(Dispatchers.IO) {
        try {
            val url = URL("https://speed.cloudflare.com/__down?bytes=10000000") // 10 MB
            val conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = 10000
            conn.readTimeout = 30000
            val start = System.currentTimeMillis()
            val input = conn.inputStream
            val buffer = ByteArray(8192)
            var total = 0L
            var read: Int
            while (input.read(buffer).also { read = it } != -1) {
                total += read
            }
            input.close()
            conn.disconnect()
            val elapsedSec = (System.currentTimeMillis() - start) / 1000.0
            if (elapsedSec > 0) (total * 8.0) / elapsedSec / 1_000_000.0 else 0.0
        } catch (_: Exception) {
            0.0
        }
    }

    // Upload: kirim data ke server, ukur kecepatan
    suspend fun upload(): Double = withContext(Dispatchers.IO) {
        try {
            val url = URL("https://speed.cloudflare.com/__up")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.doOutput = true
            conn.connectTimeout = 10000
            conn.readTimeout = 30000
            val data = ByteArray(5_000_000) // 5 MB
            val start = System.currentTimeMillis()
            conn.outputStream.use { it.write(data) }
            val code = conn.responseCode
            conn.disconnect()
            val elapsedSec = (System.currentTimeMillis() - start) / 1000.0
            if (elapsedSec > 0 && code == 200) (data.size * 8.0) / elapsedSec / 1_000_000.0 else 0.0
        } catch (_: Exception) {
            0.0
        }
    }

    suspend fun runAll(
        onProgress: (String) -> Unit = {}
    ): Result = withContext(Dispatchers.IO) {
        try {
            onProgress("Mengukur ping...")
            val ping = ping()
            onProgress("Mengukur download...")
            val dl = download()
            onProgress("Mengukur upload...")
            val ul = upload()
            Result(ping, dl, ul)
        } catch (e: Exception) {
            Result(error = e.message)
        }
    }
}
