package com.github.lemfi.kest.samplehttp.multiplescenarios.stepsextracted

import com.github.lemfi.kest.core.cli.assertThat
import com.github.lemfi.kest.core.cli.nestedScenario
import com.github.lemfi.kest.core.cli.scenario
import com.github.lemfi.kest.http.cli.givenHttpCall
import com.github.lemfi.kest.json.cli.json
import com.github.lemfi.kest.json.cli.matches
import com.github.lemfi.kest.json.cli.validator
import com.github.lemfi.kest.json.model.JsonMap
import com.github.lemfi.kest.junit5.runner.playScenarios
import com.github.lemfi.kest.samplehttp.multiplescenarios.scenariosextracted.`validate otp`
import com.github.lemfi.kest.samplehttp.startSampleApi
import com.github.lemfi.kest.samplehttp.stopSampleApi
import org.junit.jupiter.api.TestFactory

class TestHttpServer {

    @TestFactory
    fun `http server hello`() = playScenarios(
        scenario(name = "api says hello and remembers it!") {

            `say hello`("Darth Vader")
            `say hello`("Han Solo")

            `get greeted`("Darth Vader", "Han Solo")
        },
        beforeEach = ::startSampleApi,
        afterEach = ::stopSampleApi
    )

    @TestFactory
    fun `http server goodbye`() = playScenarios(
        scenario(name = "api says goodbye and forgets people!") {

            `say hello`("Darth Vader")
            `say hello`("Han Solo")

            givenHttpCall<String> {

                url = "http://localhost:8080/hello?who=Darth Vader"
                method = "DELETE"
                headers["Authorization"] = "Basic aGVsbG86d29ybGQ="

            } assertThat { stepResult ->

                stepResult.status isEqualTo 201
                stepResult.body isEqualTo "Goodbye Darth Vader!"
            }

            `get greeted`("Han Solo")

        },
        beforeEach = ::startSampleApi,
        afterEach = ::stopSampleApi
    )


    @TestFactory
    fun `otp flows`() = playScenarios(
        scenario(name = "get and validate correct otp") {

            val generateOtps = `generate otps`()

            nestedScenario("validate OTPs") {
                val otps = generateOtps()
                `validate otp` { generateOtps().first() }
                `validate otp` { generateOtps().last() }
                (otps.indices).forEach {
                    `validate otp`(otps[it])
                }
            }
        },
        scenario(name = "get and validate wrong otp") {

            `get otp`()

            givenHttpCall<JsonMap>(name = "validate wrong OTP") {

                url = "http://localhost:8080/otp"
                method = "POST"
                headers["Authorization"] = "Basic aGVsbG86d29ybGQ="
                body = "whatever"
                contentType = "text/plain"

            } assertThat { stepResult ->

                stepResult.status isEqualTo 400
                json(stepResult.body) matches validator { "{{error}}" }
            }

        },
        beforeEach = ::startSampleApi,
        afterEach = ::stopSampleApi
    )
}

