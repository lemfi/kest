package com.github.lemfi.kest.samplehttp

import com.github.lemfi.kest.core.cli.scenario
import com.github.lemfi.kest.core.cli.step
import com.github.lemfi.kest.samplehttp.sampleapi.startSampleApi
import com.github.lemfi.kest.samplehttp.sampleapi.stopSampleApi

fun startSampleApi() = scenario {
    name { "start sample API" }
    step {
        startSampleApi()
    }
}

fun stopSampleApi() = scenario {
    name { "stop sample API" }
    step {
        stopSampleApi()
    }
}