package com.github.lemfi.kest.sample.scenariobyscenario

import com.github.lemfi.kest.junit5.runner.`play scenario`
import com.github.lemfi.kest.sample.sampleapi.startSampleApi
import com.github.lemfi.kest.sample.sampleapi.stopSampleApi
import com.github.lemfi.kest.sample.multiplescenarios.scenariosextracted.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestFactory

class TestHttpServer {

    @BeforeEach
    fun beforeEach() = startSampleApi()

    @AfterEach
    fun afterEach() = stopSampleApi()

    @TestFactory
    fun `api says hello and remembers it! (unwrapped)`() = `play scenario`(`api says hello and remembers it!`)

    @TestFactory
    fun `api says hello and remembers it! (wrapped)`() = `play scenario`(`api says hello and remembers it!`, unwrap = false)

    @TestFactory
    fun `api says goodbye and forgets people!`() = `play scenario`(`api says goodbye and forgets people!`)

    @TestFactory
    fun `get and validate correct otp`() = `play scenario`(`get and validate correct otp`)

    @TestFactory
    fun `get and validate wrong otp`() = `play scenario`(`get and validate wrong otp`)

    @TestFactory
    fun `build scenario directly, not calling built-in scenario`() = `play scenario` {

        name { "Darth Vader and Han Solo say hello!" }

        `say hello`("Darth Vader")
        `say hello`("Han Solo")

        `get greeted`("Darth Vader", "Han Solo")
    }

}