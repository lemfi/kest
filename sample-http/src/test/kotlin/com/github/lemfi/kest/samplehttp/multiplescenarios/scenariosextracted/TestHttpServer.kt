package com.github.lemfi.kest.samplehttp.multiplescenarios.scenariosextracted

import com.github.lemfi.kest.junit5.runner.playScenarios
import com.github.lemfi.kest.samplehttp.startSampleApi
import com.github.lemfi.kest.samplehttp.stopSampleApi
import org.junit.jupiter.api.TestFactory

class TestHttpServer {

    @TestFactory
    fun `http server hello`() = playScenarios(
        `api says hello and remembers it!`,
        beforeEach = ::startSampleApi,
        afterEach = ::stopSampleApi
    )

    @TestFactory
    fun `http server goodbye`() = playScenarios(
        `api says goodbye and forgets people!`,
        beforeEach = ::startSampleApi,
        afterEach = ::stopSampleApi
    )

    @TestFactory
    fun `http server hello and goodbye`() = playScenarios(
        `api says hello and remembers it!`,
        `api says goodbye and forgets people!`,
        beforeEach = ::startSampleApi,
        afterEach = ::stopSampleApi
    )


    @TestFactory
    fun `otp flows`() = playScenarios(
        `get and validate correct otp`,
        `get and validate wrong otp`,
        beforeEach = ::startSampleApi,
        afterEach = ::stopSampleApi
    )
}
