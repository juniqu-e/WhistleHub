package com.whistlehub.common.util

import android.content.Context
import android.net.Uri
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream

// URI를 MultipartBody.Part로 변환하는 함수
fun uriToMultipartBodyPart(context: Context, uri: Uri): MultipartBody.Part {
    val file = createTempFileFromUri(context, uri)
    val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
    return MultipartBody.Part.createFormData("image", file.name, requestFile)
}

// URI에서 임시 파일 생성
fun createTempFileFromUri(context: Context, uri: Uri): File {
    val inputStream = context.contentResolver.openInputStream(uri)
    val fileName = getFileNameFromUri(context, uri) ?: "temp_image.jpg"
    val tempFile = File(context.cacheDir, fileName)

    FileOutputStream(tempFile).use { fileOutputStream ->
        inputStream?.copyTo(fileOutputStream)
    }

    inputStream?.close()
    return tempFile
}

// URI에서 파일 이름 가져오기
fun getFileNameFromUri(context: Context, uri: Uri): String? {
    val cursor = context.contentResolver.query(uri, null, null, null, null)
    cursor?.use {
        if (it.moveToFirst()) {
            val displayNameIndex = it.getColumnIndex("_display_name")
            if (displayNameIndex != -1) {
                return it.getString(displayNameIndex)
            }
        }
    }
    return null
}