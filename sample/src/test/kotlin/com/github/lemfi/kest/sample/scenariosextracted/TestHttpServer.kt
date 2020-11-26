package com.github.lemfi.kest.sample.scenariosextracted

import com.github.lemfi.kest.junit5.runner.`run scenarios`
import com.github.lemfi.kest.sample.sampleapi.startSampleApi
import com.github.lemfi.kest.sample.sampleapi.stopSampleApi
import org.junit.jupiter.api.TestFactory

class TestHttpServer {

    @TestFactory
    fun `http server hello`() = `run scenarios`(
            `api says hello and remembers it!`,

            beforeEach = { startSampleApi() },
            afterEach = { stopSampleApi() }
    )

    @TestFactory
    fun `http server goodbye`() = `run scenarios`(
           `api says goodbye and forgets people!`,

            beforeEach = { startSampleApi() },
            afterEach = { stopSampleApi() }
    )

    @TestFactory
    fun `http server hello and goodbye`() = `run scenarios`(
           `api says goodbye and forgets people!`,
            `api says goodbye and forgets people!`,

            beforeEach = { startSampleApi() },
            afterEach = { stopSampleApi() }
    )
}
