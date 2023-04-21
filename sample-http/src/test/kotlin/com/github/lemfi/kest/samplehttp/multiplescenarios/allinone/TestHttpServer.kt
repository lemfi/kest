package com.github.lemfi.kest.samplehttp.multiplescenarios.allinone

import com.github.lemfi.kest.core.cli.assertThat
import com.github.lemfi.kest.core.cli.scenario
import com.github.lemfi.kest.core.model.byIntervalsOf
import com.github.lemfi.kest.core.model.ms
import com.github.lemfi.kest.core.model.times
import com.github.lemfi.kest.http.cli.givenHttpCall
import com.github.lemfi.kest.http.model.fileDataPart
import com.github.lemfi.kest.http.model.multipartBody
import com.github.lemfi.kest.json.cli.json
import com.github.lemfi.kest.json.cli.matches
import com.github.lemfi.kest.json.cli.validator
import com.github.lemfi.kest.json.model.JsonArray
import com.github.lemfi.kest.json.model.JsonMap
import com.github.lemfi.kest.junit5.runner.playScenarios
import com.github.lemfi.kest.samplehttp.startSampleApi
import com.github.lemfi.kest.samplehttp.stopSampleApi
import org.junit.jupiter.api.TestFactory
import java.nio.charset.Charset

class TestHttpServer {

    @TestFactory
    fun `http server hello`() = playScenarios(
        scenario(name = "api says hello and remembers it!") {

            givenHttpCall<String>("Darth Vader says hello") {

                url = "http://localhost:8080/hello"
                method = "POST"
                headers["Authorization"] = "Basic aGVsbG86d29ybGQ="
                body = """
                            {
                                "who": "Darth Vader"
                            }
                            """
            } assertThat { stepResult ->

                stepResult.status isEqualTo 201 { "Saying Hello should return a 201, was ${stepResult.status}!" }
                stepResult.body isEqualTo "Hello Darth Vader!"
            }

            givenHttpCall<String>("Han Solo says hello") {

                url = "http://localhost:8080/hello"
                method = "POST"
                headers["Authorization"] = "Basic aGVsbG86d29ybGQ="
                body = """
                            {
                                "who": "Han Solo"
                            }
                        """
            } assertThat { stepResult ->

                stepResult.status isEqualTo 201
                stepResult.body isEqualTo "Hello Han Solo!"
            }

            givenHttpCall<List<String>>("get list of greeted people") {

                url = "http://localhost:8080/hello"
                method = "GET"
                headers["Authorization"] = "Basic aGVsbG86d29ybGQ="

            } assertThat { stepResult ->

                stepResult.status isEqualTo 200
                stepResult.body isEqualTo listOf("Darth Vader", "Han Solo")
            }

            givenHttpCall<List<String>>("when a redirect happens it can be avoided") {

                url = "http://localhost:8080/hello-redirect"
                method = "GET"
                followRedirect = false
                headers["Authorization"] = "Basic aGVsbG86d29ybGQ="

            } assertThat { stepResult ->

                stepResult.status isEqualTo 302
                stepResult.headers["Location"] isEqualTo listOf("http://localhost:8080/hello")
            }

            givenHttpCall<List<String>>("when a redirect happens it can be followed") {

                url = "http://localhost:8080/hello-redirect"
                method = "GET"
                followRedirect = true
                headers["Authorization"] = "Basic aGVsbG86d29ybGQ="

            } assertThat { stepResult ->

                stepResult.status isEqualTo 200
                stepResult.body isEqualTo listOf("Darth Vader", "Han Solo")
            }
        },
        beforeAll = ::startSampleApi,
        afterAll = ::stopSampleApi
    )

    @TestFactory
    fun `http server goodbye`() = playScenarios(
        scenario(name = "api says goodbye and forgets people!") {

            givenHttpCall<String>("Darth Vader says hello") {

                url = "http://localhost:8080/hello"
                method = "POST"
                headers["Authorization"] = "Basic aGVsbG86d29ybGQ="
                body = """
                            {
                                "who": "Darth Vader"
                            }
                            """
            } assertThat { stepResult ->

                stepResult.status isEqualTo 201
                stepResult.body isEqualTo "Hello Darth Vader!"
            }

            givenHttpCall<String>("Han Solo says hello") {

                url = "http://localhost:8080/hello"
                method = "POST"
                headers["Authorization"] = "Basic aGVsbG86d29ybGQ="
                body = """
                            {
                                "who": "Han Solo"
                            }
                        """
            } assertThat { stepResult ->

                stepResult.status isEqualTo 201
                stepResult.body isEqualTo "Hello Han Solo!"
            }

            givenHttpCall<String>("Darth Vader says goodbye") {

                url = "http://localhost:8080/hello?who=Darth Vader"
                method = "DELETE"
                headers["Authorization"] = "Basic aGVsbG86d29ybGQ="

            } assertThat { stepResult ->

                stepResult.status isEqualTo 201
                stepResult.body isEqualTo "Goodbye Darth Vader!"
            }

            givenHttpCall<List<String>>("Han Solo is on his own") {

                url = "http://localhost:8080/hello"
                method = "GET"
                headers["Authorization"] = "Basic aGVsbG86d29ybGQ="

            } assertThat { stepResult ->

                stepResult.status isEqualTo 200
                stepResult.body isEqualTo listOf("Han Solo")
            }
        },
        beforeAll = ::startSampleApi,
        afterAll = ::stopSampleApi
    )

    @TestFactory
    fun `http server error`() = playScenarios(
        scenario(name = "when wrong api is called an error is raised") {

            givenHttpCall<JsonMap>("PATCH method is not allowed") {

                url = "http://localhost:8080/hello"
                method = "PATCH"
                headers["Authorization"] = "Basic aGVsbG86d29ybGQ="
                body = """
                            {
                                "who": "Darth Vader"
                            }
                            """
            } assertThat { stepResult ->

                stepResult.status isEqualTo 405
                json(stepResult.body) matches validator { "{{error}}" }
            }

        },
        beforeAll = ::startSampleApi,
        afterAll = ::stopSampleApi
    )

    @TestFactory
    fun `otp flows`() = playScenarios(
        scenario(name = "get and validate correct otp") {

            val otp = givenHttpCall<JsonMap>("get an OTP") {

                url = "http://localhost:8080/otp"
                method = "GET"
                headers["Authorization"] = "Basic aGVsbG86d29ybGQ="

            } mapResultTo {
                it.body["otp"] as String
            } assertThat { stepResult ->

                stepResult.status isEqualTo 201
                json(stepResult.body) matches validator {
                    """
                            {
                                "otp": "{{string}}" 
                            }
                        """
                }
            }

            givenHttpCall<JsonMap>("validate an OTP") {

                url = "http://localhost:8080/otp"
                method = "POST"
                headers["Authorization"] = "Basic aGVsbG86d29ybGQ="
                body = otp()
                contentType = "text/plain"

            } assertThat { stepResult ->

                stepResult.status isEqualTo 204
            }

        },
        scenario(name = "get and validate wrong otp") {

            givenHttpCall<JsonMap>("get an OTP") {

                url = "http://localhost:8080/otp"
                method = "GET"
                headers["Authorization"] = "Basic aGVsbG86d29ybGQ="

            } assertThat { stepResult ->

                stepResult.status isEqualTo 201
                json(stepResult.body) matches validator {
                    """
                            {
                                "otp": "{{string}}" 
                            }
                        """.trimIndent()
                }
            }

            givenHttpCall<JsonMap>("validate an invalid OTP") {

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
        beforeAll = ::startSampleApi,
        afterAll = ::stopSampleApi
    )

    @TestFactory
    fun `retryable steps`() = playScenarios(
        scenario(name = "a step should be retried as specified when not passing") {

            givenHttpCall<String>(
                name = "Sometimes retrying makes it pass!",
                retry = 100.times byIntervalsOf 10.ms
            ) {
                url = "http://localhost:8080/oh-if-you-retry-it-shall-pass"
                method = "GET"
            } assertThat {
                it.body isEqualTo "You called me 98 times!"
            }

        },
        beforeEach = ::startSampleApi,
        afterEach = ::stopSampleApi
    )

    @TestFactory
    fun inventory() = playScenarios(
        scenario(name = "get inventory as JsonArray") {

            givenHttpCall<JsonArray>("get inventory") {

                url = "http://localhost:8080/inventory"
                method = "GET"
                headers["Authorization"] = "Basic aGVsbG86d29ybGQ="

            } assertThat { stepResult ->

                stepResult.status isEqualTo 200
                json(stepResult.body) matches validator {
                    """
                            [
                                {
                                    "kind": "weapon",
                                    "name": "lightsaber"
                                },
                                {
                                    "kind": "vehicle",
                                    "name": "landspeeder"
                                }
                            ]
                        """.trimIndent()
                }
            }


        },
        scenario(name = "get inventory as List<Inventory>") {

            givenHttpCall<List<Inventory>>("get inventory") {

                url = "http://localhost:8080/inventory"
                method = "GET"
                headers["Authorization"] = "Basic aGVsbG86d29ybGQ="

            } assertThat { stepResult ->

                stepResult.status isEqualTo 200
                stepResult.body isEqualTo listOf(
                    Inventory("weapon", "lightsaber"),
                    Inventory("vehicle", "landspeeder"),
                )
            }
        },
        beforeAll = ::startSampleApi,
        afterAll = ::stopSampleApi
    )


    @TestFactory
    fun deathStarPlans() = playScenarios(
        scenario(name = "give Rebel Alliance Death Star plans") {

            givenHttpCall<String>("death star plans are not available") {

                url = "http://localhost:8080/death-star-secret-plans"
                method = "GET"
                headers["Authorization"] = "Basic aGVsbG86d29ybGQ="
            } assertThat {
                it.status isEqualTo 404
                it.body isEqualTo "Waiting for Rogue one..."
            }

            givenHttpCall<String>("give death star plans") {

                url = "http://localhost:8080/death-star-secret-plans"
                method = "POST"
                headers["Authorization"] = "Basic aGVsbG86d29ybGQ="
                body = multipartBody(
                    fileDataPart {
                        name = "death_star_plan"
                        filename = "death_star_plan.txt"
                        contentType = "text/plain"
                        data = """
                                
                                █  ██████████████████████████████████████████████████████████
                                █     █        █           █  █     █     █           █  █  █
                                ████  █  ███████  █  ████  █  █  █  █  █  █  ███████  █  █  █
                                █     █        █  █  █        █  █  █  █           █        █
                                █  █  ███████  █  ███████  ████  █  █████████████  ███████  █
                                █  █  █  █  █           █     █  █  █  █     █  █  █        █
                                ████  █  █  ████  ████  ████  █  ████  █  ████  ████  ████  █
                                █        █  █     █        █     █        █  █  █     █     █
                                ███████  █  █  █  ████  ████  ███████  ████  █  ███████  ████
                                █  █        █  █  █  █  █        █           █        █     █
                                █  ███████  █  ████  ██████████  ███████  ██████████  ████  █
                                █        █     █  █     █     █  █  █           █     █     █
                                ████  ███████  █  ████  █  ████  █  █  █  ████  █  ███████  █
                                █     █     █  █     █     █  █     █  █  █     █           █
                                █  █  ████  █  ████  █  ████  ████  ██████████  █  ███████  █
                                █  █  █     █           █              █           █     █  █
                                ████  █  █  ██████████  ████  ████  ██████████  ███████  █  █
                                █     █  █     █     █  █        █     █        █           █
                                █  █  █  ███████  █  █  ████  ████  █████████████  ███████  █
                                █  █              █     █        █        █        █  █  █  █
                                ████  █  ████  ██████████  ██████████  █  █  ████  █  █  ████
                                █     █     █           █     █     █  █        █        █  █
                                ███████  ████  █  ████████████████  ███████  █████████████  █
                                █     █     █  █        █  █     █     █     █  █     █  █  █
                                █  █  █  ███████  ███████  █  █  ████  ████  █  █  ████  █  █
                                █  █           █     █     █  █        █                    █
                                ██████████  ████  ███████  █  ███████  █  █  █  ██████████  █
                                █     █  █  █  █  █     █     █           █  █        █  █  █
                                █  ████  █  █  ███████  █  █  ████  ████  █  ███████  █  ████
                                █                    █  █  █  █        █  █  █     █     █  █
                                ████  ████  ███████  █  ███████  ████  ███████  ████  █  █  █
                                █  █  █  █     █              █  █  █     █        █  █     █
                                █  █  █  ████████████████  ████  █  █  █  █  ████  ████  █  █
                                █  █              █  █  █     █  █     █  █  █  █     █  █  █
                                █  █  █  ██████████  █  █  █  █  ███████  ████  ████  ████  █
                                █     █  █  █     █        █  █  █     █  █  █     █        █
                                ███████  █  █  ███████  ███████  █  ███████  ████  █  ████  █
                                █  █        █     █     █  █  █  █           █  █        █  █
                                █  ███████  █  █  █  ████  █  █  █  ████  █  █  █  ███████  █
                                █              █  █           █     █     █     █     █     
                                █████████████████████████████████████████████████████████████  
    
                            """.trimIndent().toByteArray(Charset.defaultCharset())
                    }
                )
            } assertThat { stepResult ->

                stepResult.status isEqualTo 201
                stepResult.body isEqualTo "May the Force be with you!"
            }

            givenHttpCall<String>("death star plans are available") {

                url = "http://localhost:8080/death-star-secret-plans"
                method = "GET"
                headers["Authorization"] = "Basic aGVsbG86d29ybGQ="
            } assertThat {
                it.status isEqualTo 200
                it.body isEqualTo """ 
                        
                            █  ██████████████████████████████████████████████████████████
                            █     █        █           █  █     █     █           █  █  █
                            ████  █  ███████  █  ████  █  █  █  █  █  █  ███████  █  █  █
                            █     █        █  █  █        █  █  █  █           █        █
                            █  █  ███████  █  ███████  ████  █  █████████████  ███████  █
                            █  █  █  █  █           █     █  █  █  █     █  █  █        █
                            ████  █  █  ████  ████  ████  █  ████  █  ████  ████  ████  █
                            █        █  █     █        █     █        █  █  █     █     █
                            ███████  █  █  █  ████  ████  ███████  ████  █  ███████  ████
                            █  █        █  █  █  █  █        █           █        █     █
                            █  ███████  █  ████  ██████████  ███████  ██████████  ████  █
                            █        █     █  █     █     █  █  █           █     █     █
                            ████  ███████  █  ████  █  ████  █  █  █  ████  █  ███████  █
                            █     █     █  █     █     █  █     █  █  █     █           █
                            █  █  ████  █  ████  █  ████  ████  ██████████  █  ███████  █
                            █  █  █     █           █              █           █     █  █
                            ████  █  █  ██████████  ████  ████  ██████████  ███████  █  █
                            █     █  █     █     █  █        █     █        █           █
                            █  █  █  ███████  █  █  ████  ████  █████████████  ███████  █
                            █  █              █     █        █        █        █  █  █  █
                            ████  █  ████  ██████████  ██████████  █  █  ████  █  █  ████
                            █     █     █           █     █     █  █        █        █  █
                            ███████  ████  █  ████████████████  ███████  █████████████  █
                            █     █     █  █        █  █     █     █     █  █     █  █  █
                            █  █  █  ███████  ███████  █  █  ████  ████  █  █  ████  █  █
                            █  █           █     █     █  █        █                    █
                            ██████████  ████  ███████  █  ███████  █  █  █  ██████████  █
                            █     █  █  █  █  █     █     █           █  █        █  █  █
                            █  ████  █  █  ███████  █  █  ████  ████  █  ███████  █  ████
                            █                    █  █  █  █        █  █  █     █     █  █
                            ████  ████  ███████  █  ███████  ████  ███████  ████  █  █  █
                            █  █  █  █     █              █  █  █     █        █  █     █
                            █  █  █  ████████████████  ████  █  █  █  █  ████  ████  █  █
                            █  █              █  █  █     █  █     █  █  █  █     █  █  █
                            █  █  █  ██████████  █  █  █  █  ███████  ████  ████  ████  █
                            █     █  █  █     █        █  █  █     █  █  █     █        █
                            ███████  █  █  ███████  ███████  █  ███████  ████  █  ████  █
                            █  █        █     █     █  █  █  █           █  █        █  █
                            █  ███████  █  █  █  ████  █  █  █  ████  █  █  █  ███████  █
                            █              █  █           █     █     █     █     █     
                            █████████████████████████████████████████████████████████████  
                            """.trimIndent().trim()
            }
        },
        beforeAll = ::startSampleApi,
        afterAll = ::stopSampleApi
    )

    data class Inventory(
        val kind: String,
        val name: String
    )
}
