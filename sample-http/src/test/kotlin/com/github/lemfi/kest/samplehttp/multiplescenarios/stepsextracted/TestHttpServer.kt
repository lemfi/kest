package com.github.lemfi.kest.samplehttp.multiplescenarios.stepsextracted

import com.github.lemfi.kest.core.cli.`assert that`
import com.github.lemfi.kest.core.cli.eq
import com.github.lemfi.kest.core.cli.scenario
import com.github.lemfi.kest.core.cli.nestedScenario
import com.github.lemfi.kest.executor.http.cli.`given http call`
import com.github.lemfi.kest.json.cli.jsonMatchesObject
import com.github.lemfi.kest.json.model.JsonMap
import com.github.lemfi.kest.junit5.runner.`play scenarios`
import com.github.lemfi.kest.samplehttp.sampleapi.startSampleApi
import com.github.lemfi.kest.samplehttp.sampleapi.stopSampleApi
import com.github.lemfi.kest.samplehttp.multiplescenarios.scenariosextracted.`validate otp`
import org.junit.jupiter.api.TestFactory

class TestHttpServer {

    @TestFactory
    fun `http server hello`() = `play scenarios`(
        scenario {

            name { "api says hello and remembers it!" }

            `say hello`("Darth Vader")
            `say hello`("Han Solo")

            `get greeted`("Darth Vader", "Han Solo")
        },
        beforeEach = { startSampleApi() },
        afterEach = { stopSampleApi() }
    )

    @TestFactory
    fun `http server goodbye`() = `play scenarios`(
        scenario {

            name { "api says goodbye and forgets people!" }

            `say hello`("Darth Vader")
            `say hello`("Han Solo")

            `given http call`<String> {

                url = "http://localhost:8080/hello?who=Darth Vader"
                method = "DELETE"
                headers["Authorization"] = "Basic aGVsbG86d29ybGQ="

            } `assert that` { stepResult ->

                eq(201, stepResult.status)
                eq("Goodbye Darth Vader!", stepResult.body)
            }

            `get greeted`("Han Solo")

        },
        beforeEach = { startSampleApi() },
        afterEach = { stopSampleApi() }
    )


    @TestFactory
    fun `otp flows`() = `play scenarios`(
        scenario {

            name { "get and validate correct otp" }

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
        scenario {

            name { "get and validate wrong otp" }

            `get otp`()

            `given http call`<JsonMap> {

                url = "http://localhost:8080/otp"
                method = "POST"
                headers["Authorization"] = "Basic aGVsbG86d29ybGQ="
                body = "whatever"
                contentType = "text/plain"

            } `assert that` { stepResult ->

                eq(400, stepResult.status)
                jsonMatchesObject("{{error}}", stepResult.body)
            }

        },
        beforeEach = { startSampleApi() },
        afterEach = { stopSampleApi() }
    )
}

