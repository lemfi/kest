package com.github.lemfi.kest.samplehttp

import com.github.lemfi.kest.json.cli.pattern

@Suppress("unused")
class KestConfiguration {

    init {
        pattern("error") definedBy Error::class
    }
}