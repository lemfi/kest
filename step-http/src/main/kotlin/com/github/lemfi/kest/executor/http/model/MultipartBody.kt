package com.github.lemfi.kest.executor.http.model

import com.github.lemfi.kest.executor.http.builder.FilePartBuilder
import com.github.lemfi.kest.executor.http.builder.ParameterPartBuilder
import java.io.File

data class MultipartBody(
        val parts: List<Part>
)

sealed class Part {
    abstract val name: String
}

data class FilePart(
        override val name: String,
        val filename: String,
        val file: File,
        val contentType: String?,
): Part()

data class ParameterPart(
        override val name: String,
        val value: String,
): Part()

fun multipartBody(vararg parts: Part): MultipartBody {
    return MultipartBody(parts.toList())
}

fun filePart(l: FilePartBuilder.()->Unit): FilePart {
    return FilePartBuilder().apply(l).build()
}

fun parameterPart(l: ParameterPartBuilder.()->Unit): ParameterPart {
    return ParameterPartBuilder().apply(l).build()
}