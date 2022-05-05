package com.github.lemfi.kest.http.builder

import com.github.lemfi.kest.http.model.FileDataPart
import com.github.lemfi.kest.http.model.FilePart
import com.github.lemfi.kest.http.model.ParameterPart
import java.io.File

class FilePartBuilder {

    @Suppress("MemberVisibilityCanBePrivate")
    lateinit var filename: String

    @Suppress("MemberVisibilityCanBePrivate")
    lateinit var name: String

    @Suppress("MemberVisibilityCanBePrivate")
    lateinit var contentType: String

    @Suppress("MemberVisibilityCanBePrivate")
    lateinit var file: File

    fun build(): FilePart {
        return FilePart(name, filename, file, contentType)
    }
}

class FileDataPartBuilder {

    @Suppress("MemberVisibilityCanBePrivate")
    lateinit var filename: String

    @Suppress("MemberVisibilityCanBePrivate")
    lateinit var name: String

    @Suppress("MemberVisibilityCanBePrivate")
    lateinit var contentType: String

    @Suppress("MemberVisibilityCanBePrivate")
    lateinit var data: ByteArray

    fun build(): FileDataPart {
        return FileDataPart(name, filename, data, contentType)
    }
}

class ParameterPartBuilder {

    @Suppress("MemberVisibilityCanBePrivate")
    lateinit var name: String

    @Suppress("MemberVisibilityCanBePrivate")
    lateinit var value: String

    fun build(): ParameterPart {
        return ParameterPart(name, value)
    }
}
