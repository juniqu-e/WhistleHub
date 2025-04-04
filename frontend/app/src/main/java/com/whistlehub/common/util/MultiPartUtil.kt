package com.whistlehub.common.util

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

fun createRequestBody(value: String): RequestBody =
    value.toRequestBody("text/plain".toMediaType())

fun createMultipart(
    file: File,
    partName: String,
    mimeType: String = "audio/wav" //기본 오디오, 파라미터 받아서 생성가능
): MultipartBody.Part =
    MultipartBody.Part.createFormData(
        partName,
        file.name,
        file.asRequestBody(mimeType.toMediaType())
    )