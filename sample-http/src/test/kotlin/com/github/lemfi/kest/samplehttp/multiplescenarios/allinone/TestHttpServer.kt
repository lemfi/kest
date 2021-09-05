package com.github.lemfi.kest.samplehttp.multiplescenarios.allinone

import com.github.lemfi.kest.core.cli.`assert that`
import com.github.lemfi.kest.core.cli.eq
import com.github.lemfi.kest.core.cli.scenario
import com.github.lemfi.kest.core.model.`delayed by`
import com.github.lemfi.kest.core.model.ms
import com.github.lemfi.kest.core.model.times
import com.github.lemfi.kest.executor.http.cli.`given http call`
import com.github.lemfi.kest.json.cli.jsonMatches
import com.github.lemfi.kest.json.model.JsonMap
import com.github.lemfi.kest.junit5.runner.`play scenarios`
import com.github.lemfi.kest.samplehttp.sampleapi.startSampleApi
import com.github.lemfi.kest.samplehttp.sampleapi.stopSampleApi
import org.junit.jupiter.api.TestFactory

class TestHttpServer {

    @TestFactory
    fun `http server hello`() = `play scenarios`(
        scenario {

            name { "api says hello and remembers it!" }

            `given http call`<String>("Darth Vader says hello") {

                url = "http://localhost:8080/hello"
                method = "POST"
                headers["Authorization"] = "Basic aGVsbG86d29ybGQ="
                body = """
                        {
                            "who": "Darth Vader"
                        }
                        """
            } `assert that` { stepResult ->

                eq(201, stepResult.status) { "Saying Hello should return a 202, was ${stepResult.status}!" }
                eq("Hello Darth Vader!", stepResult.body)
            }

            `given http call`<String>("Han Solo says hello") {

                url = "http://localhost:8080/hello"
                method = "POST"
                headers["Authorization"] = "Basic aGVsbG86d29ybGQ="
                body = """
                        {
                            "who": "Han Solo"
                        }
                    """
            } `assert that` { stepResult ->

                eq(201, stepResult.status)
                eq("Hello Han Solo!", stepResult.body)
            }

            `given http call`<List<String>>("get list of greeted people") {

                url = "http://localhost:8080/hello"
                method = "GET"
                headers["Authorization"] = "Basic aGVsbG86d29ybGQ="

            } `assert that` { stepResult ->

                eq(200, stepResult.status)
                eq(listOf("Darth Vader", "Han Solo"), stepResult.body)
            }

            `given http call`<List<String>>("when a redirect happens it can be avoided") {

                url = "http://localhost:8080/hello-redirect"
                method = "GET"
                followRedirect = false
                headers["Authorization"] = "Basic aGVsbG86d29ybGQ="

            } `assert that` { stepResult ->

                eq(302, stepResult.status)
                eq(listOf("http://localhost:8080/hello"), stepResult.headers["Location"])
            }

            `given http call`<List<String>>("when a redirect happens it can be followed") {

                url = "http://localhost:8080/hello-redirect"
                method = "GET"
                followRedirect = true
                headers["Authorization"] = "Basic aGVsbG86d29ybGQ="

            } `assert that` { stepResult ->

                eq(200, stepResult.status)
                eq(listOf("Darth Vader", "Han Solo"), stepResult.body)
            }
        },
        beforeEach = { startSampleApi() },
        afterEach = { stopSampleApi() }
    )

    @TestFactory
    fun `http server goodbye`() = `play scenarios`(
        scenario {

            name { "api says goodbye and forgets people!" }

            `given http call`<String>("Darth Vader says hello") {

                url = "http://localhost:8080/hello"
                method = "POST"
                headers["Authorization"] = "Basic aGVsbG86d29ybGQ="
                body = """
                        {
                            "who": "Darth Vader"
                        }
                        """
            } `assert that` { stepResult ->

                eq(201, stepResult.status)
                eq("Hello Darth Vader!", stepResult.body)
            }

            `given http call`<String>("Han Solo says hello") {

                url = "http://localhost:8080/hello"
                method = "POST"
                headers["Authorization"] = "Basic aGVsbG86d29ybGQ="
                body = """
                        {
                            "who": "Han Solo"
                        }
                    """
            } `assert that` { stepResult ->

                eq(201, stepResult.status)
                eq("Hello Han Solo!", stepResult.body)
            }

            `given http call`<String>("Darth Vader says goodbye") {

                url = "http://localhost:8080/hello?who=Darth Vader"
                method = "DELETE"
                headers["Authorization"] = "Basic aGVsbG86d29ybGQ="

            } `assert that` { stepResult ->

                eq(201, stepResult.status)
                eq("Goodbye Darth Vader!", stepResult.body)
            }

            `given http call`<List<String>>("Han Solo is on his own") {

                url = "http://localhost:8080/hello"
                method = "GET"
                headers["Authorization"] = "Basic aGVsbG86d29ybGQ="

            } `assert that` { stepResult ->

                eq(200, stepResult.status)
                eq(listOf("Han Solo"), stepResult.body)
            }
        },
        beforeEach = { startSampleApi() },
        afterEach = { stopSampleApi() }
    )

    @TestFactory
    fun `http server error`() = `play scenarios`(
        scenario {

            name { "when wrong api is called an error is raised" }

            `given http call`<JsonMap>("PATCH method is not allowed") {

                url = "http://localhost:8080/hello"
                method = "PATCH"
                headers["Authorization"] = "Basic aGVsbG86d29ybGQ="
                body = """
                        {
                            "who": "Darth Vader"
                        }
                        """
            } `assert that` { stepResult ->

                eq(405, stepResult.status)
                jsonMatches("{{error}}", stepResult.body)
            }

        },
        beforeEach = { startSampleApi() },
        afterEach = { stopSampleApi() }
    )

    @TestFactory
    fun `otp flows`() = `play scenarios`(
        scenario {

            name { "get and validate correct otp" }

            val otp = `given http call`<JsonMap>("get an OTP") {

                url = "http://localhost:8080/otp"
                method = "GET"
                headers["Authorization"] = "Basic aGVsbG86d29ybGQ="

            } `map result to` {
                it.body["otp"] as String
            } `assert that` { stepResult ->

                eq(201, stepResult.status)
                jsonMatches(
                    """
                        {
                            "otp": "{{string}}" 
                        }
                    """.trimIndent(), stepResult.body
                )
            }

            `given http call`<JsonMap>("validate an OTP") {

                url = "http://localhost:8080/otp"
                method = "POST"
                headers["Authorization"] = "Basic aGVsbG86d29ybGQ="
                body = otp()
                contentType = "text/plain"

            } `assert that` { stepResult ->

                eq(204, stepResult.status)
            }

        },
        scenario {

            name { "get and validate wrong otp" }

            `given http call`<JsonMap>("get an OTP") {

                url = "http://localhost:8080/otp"
                method = "GET"
                headers["Authorization"] = "Basic aGVsbG86d29ybGQ="

            } `assert that` { stepResult ->

                eq(201, stepResult.status)
                jsonMatches(
                    """
                        {
                            "otp": "{{string}}" 
                        }
                    """.trimIndent(), stepResult.body
                )
            }

            `given http call`<JsonMap>("validate an invalid OTP") {

                url = "http://localhost:8080/otp"
                method = "POST"
                headers["Authorization"] = "Basic aGVsbG86d29ybGQ="
                body = "whatever"
                contentType = "text/plain"

            } `assert that` { stepResult ->

                eq(400, stepResult.status)
                jsonMatches("{{error}}", stepResult.body)
            }

        },
        beforeEach = { startSampleApi() },
        afterEach = { stopSampleApi() }
    )

    @TestFactory
    fun `retryable steps`() = `play scenarios`(
        scenario {
            name { "a step should be retried as specified when not passing" }

            `given http call`<String>(name = "Sometimes retrying makes it pass!", retry = 100.times `delayed by` 10.ms) {
                url = "http://localhost:8080/oh-if-you-retry-it-shall-pass"
                method = "GET"
            } `assert that` {
                eq("You called me 98 times!", it.body)
            }

        },
        beforeEach = { startSampleApi() },
        afterEach = { stopSampleApi() }
    )
}
