package com.github.lemfi.kest.samplehttp

import com.github.lemfi.kest.json.cli.JsonMatcher

@Suppress("unused")
class KestConfiguration {

    init {
        JsonMatcher.addMatcher("{{error}}", Error::class)
    }
}