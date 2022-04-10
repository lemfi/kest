package com.github.lemfi.kest.samplehttp

import com.github.lemfi.kest.core.cli.scenario
import com.github.lemfi.kest.core.cli.step
import com.github.lemfi.kest.samplehttp.sampleapi.startSampleApi
import com.github.lemfi.kest.samplehttp.sampleapi.stopSampleApi

fun startSampleApi() = scenario("start sample API") {

    step("start sample API") {
        startSampleApi()
    }
}

fun stopSampleApi() = scenario("stop sample API") {
    step("stop sample API") {
        stopSampleApi()
    }
}