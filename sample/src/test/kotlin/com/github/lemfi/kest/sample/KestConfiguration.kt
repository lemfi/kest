package com.github.lemfi.kest.sample

import com.github.lemfi.kest.json.cli.JsonMatcher

@Suppress("unused")
class KestConfiguration {

    init {
        JsonMatcher.addMatcher("{{error}}", Error::class)
    }
}

