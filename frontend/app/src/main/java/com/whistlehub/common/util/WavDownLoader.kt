package com.whistlehub.common.util

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

suspend fun downloadWavFromS3Url(
    context: Context,
    s3Url: String,
    fileName: String
): File = withContext(Dispatchers.IO) {
    val connection = URL(s3Url).openConnection() as HttpURLConnection
    connection.requestMethod = "GET"
    connection.connectTimeout = 10000
    connection.readTimeout = 15000
    // 연결 확인
    if (connection.responseCode != HttpURLConnection.HTTP_OK) {
        throw Exception("S3 다운로드 실패: HTTP ${connection.responseCode}")
    }
    val inputStream = connection.inputStream
    val outputFile = File(context.filesDir, fileName)
    val outputStream = FileOutputStream(outputFile)

    inputStream.copyTo(outputStream)

    inputStream.close()
    outputStream.close()
    connection.disconnect()

    Log.d("Search", "S3 Download , $outputFile")

    outputFile
}