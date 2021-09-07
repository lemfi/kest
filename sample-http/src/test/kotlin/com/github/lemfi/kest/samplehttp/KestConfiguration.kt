package com.github.lemfi.kest.samplehttp

import com.github.lemfi.kest.json.cli.`add json matcher`

@Suppress("unused")
class KestConfiguration {

    init {
        `add json matcher`("{{error}}", Error::class)
    }
}