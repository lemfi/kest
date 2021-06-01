package com.github.lemfi.kest.samplehttp.multiplescenarios.scenariosextracted

import com.github.lemfi.kest.junit5.runner.`play scenarios`
import com.github.lemfi.kest.samplehttp.sampleapi.startSampleApi
import com.github.lemfi.kest.samplehttp.sampleapi.stopSampleApi
import org.junit.jupiter.api.TestFactory

class TestHttpServer {

    @TestFactory
    fun `http server hello`() = `play scenarios`(
        `api says hello and remembers it!`,

        beforeEach = { startSampleApi() },
        afterEach = { stopSampleApi() }
    )

    @TestFactory
    fun `http server goodbye`() = `play scenarios`(
        `api says goodbye and forgets people!`,

        beforeEach = { startSampleApi() },
        afterEach = { stopSampleApi() }
    )

    @TestFactory
    fun `http server hello and goodbye`() = `play scenarios`(
        `api says hello and remembers it!`,
        `api says goodbye and forgets people!`,

        beforeEach = { startSampleApi() },
        afterEach = { stopSampleApi() }
    )


    @TestFactory
    fun `otp flows`() = `play scenarios`(
        `get and validate correct otp`,
        `get and validate wrong otp`,

        beforeEach = { startSampleApi() },
        afterEach = { stopSampleApi() }
    )
}
