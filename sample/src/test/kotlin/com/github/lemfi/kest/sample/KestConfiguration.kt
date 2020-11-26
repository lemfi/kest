package com.github.lemfi.kest.sample

import com.github.lemfi.kest.json.cli.JsonMatcher

class KestConfiguration {

    init {
        JsonMatcher.addMatcher("{{error}}", Error::class)
    }
}

