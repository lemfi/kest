package com.github.lemfi.kest.executor.http.builder

import com.github.lemfi.kest.executor.http.model.FilePart
import com.github.lemfi.kest.executor.http.model.ParameterPart
import java.io.File

class FilePartBuilder {

    lateinit var filename: String

    lateinit var name: String

    lateinit var contentType: String

    lateinit var file: File

    fun build(): FilePart {
        return FilePart(name, filename, file, contentType)
    }
}

class ParameterPartBuilder {

    lateinit var name: String

    lateinit var value: String

    fun build(): ParameterPart {
        return ParameterPart(name, value)
    }
}
