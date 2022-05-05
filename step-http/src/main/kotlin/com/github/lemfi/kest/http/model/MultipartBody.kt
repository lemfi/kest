@file:Suppress("unused")

package com.github.lemfi.kest.http.model

import com.github.lemfi.kest.http.builder.FileDataPartBuilder
import com.github.lemfi.kest.http.builder.FilePartBuilder
import com.github.lemfi.kest.http.builder.ParameterPartBuilder
import java.io.File

data class MultipartBody(
    val parts: List<Part>
) {
    override fun toString(): String {
        return """MultipartBody(
            |parts: 
            |${parts.joinToString("\n------\n")}
            |)
        """.trimMargin()
    }
}

sealed class Part {
    abstract val name: String
}

data class FilePart(
    override val name: String,
    val filename: String,
    val file: File,
    val contentType: String?,
) : Part() {
    override fun toString(): String {
        return """
            name: $name
            filename: $filename
            contentType: $contentType
            file: File(${file.path})
        """.trimIndent()
    }
}

@Suppress("ArrayInDataClass")
data class FileDataPart(
    override val name: String,
    val filename: String,
    val data: ByteArray,
    val contentType: String?,
) : Part() {
    override fun toString(): String {
        return """
            name: $name
            filename: $filename
            contentType: $contentType
            data: ByteArray(${data.size})
        """.trimIndent()
    }
}

data class ParameterPart(
    override val name: String,
    val value: String,
) : Part() {
    override fun toString(): String {
        return """
            name: $name
            value: $value
        """.trimIndent()
    }
}

fun multipartBody(vararg parts: Part): MultipartBody {
    return MultipartBody(parts.toList())
}

fun filePart(l: FilePartBuilder.() -> Unit): FilePart {
    return FilePartBuilder().apply(l).build()
}

fun fileDataPart(l: FileDataPartBuilder.() -> Unit): FileDataPart {
    return FileDataPartBuilder().apply(l).build()
}

fun parameterPart(l: ParameterPartBuilder.() -> Unit): ParameterPart {
    return ParameterPartBuilder().apply(l).build()
}