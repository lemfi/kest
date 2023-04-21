package com.github.lemfi.kest.samplehttp.multiplescenarios.allinone

import com.github.lemfi.kest.core.cli.assertThat
import com.github.lemfi.kest.core.cli.scenario
import com.github.lemfi.kest.core.model.byIntervalsOf
import com.github.lemfi.kest.core.model.ms
import com.github.lemfi.kest.core.model.times
import com.github.lemfi.kest.http.cli.`given http call`
import com.github.lemfi.kest.http.model.fileDataPart
import com.github.lemfi.kest.http.model.multipartBody
import com.github.lemfi.kest.json.cli.json
import com.github.lemfi.kest.json.cli.matches
import com.github.lemfi.kest.json.cli.validator
import com.github.lemfi.kest.json.model.JsonArray
import com.github.lemfi.kest.json.model.JsonMap
import com.github.lemfi.kest.junit5.runner.`play scenarios`
import com.github.lemfi.kest.samplehttp.startSampleApi
import com.github.lemfi.kest.samplehttp.stopSampleApi
import org.junit.jupiter.api.TestFactory
import java.nio.charset.Charset

class TestHttpServer {

    @TestFactory
    fun `http server hello`() = `play scenarios`(
        scenario(name = "api says hello and remembers it!") {

            `given http call`<String>("Darth Vader says hello") {

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

            `given http call`<String>("Han Solo says hello") {

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

            `given http call`<List<String>>("get list of greeted people") {

                url = "http://localhost:8080/hello"
                method = "GET"
                headers["Authorization"] = "Basic aGVsbG86d29ybGQ="

            } assertThat { stepResult ->

                stepResult.status isEqualTo 200
                stepResult.body isEqualTo listOf("Darth Vader", "Han Solo")
            }

            `given http call`<List<String>>("when a redirect happens it can be avoided") {

                url = "http://localhost:8080/hello-redirect"
                method = "GET"
                followRedirect = false
                headers["Authorization"] = "Basic aGVsbG86d29ybGQ="

            } assertThat { stepResult ->

                stepResult.status isEqualTo 302
                stepResult.headers["Location"] isEqualTo listOf("http://localhost:8080/hello")
            }

            `given http call`<List<String>>("when a redirect happens it can be followed") {

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
        afterAll = ::stopSampleApi,
    )

    @TestFactory
    fun `http server goodbye`() = `play scenarios`(
        scenario(name = "api says goodbye and forgets people!") {

            `given http call`<String>("Darth Vader says hello") {

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

            `given http call`<String>("Han Solo says hello") {

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

            `given http call`<String>("Darth Vader says goodbye") {

                url = "http://localhost:8080/hello?who=Darth Vader"
                method = "DELETE"
                headers["Authorization"] = "Basic aGVsbG86d29ybGQ="

            } assertThat { stepResult ->

                stepResult.status isEqualTo 201
                stepResult.body isEqualTo "Goodbye Darth Vader!"
            }

            `given http call`<List<String>>("Han Solo is on his own") {

                url = "http://localhost:8080/hello"
                method = "GET"
                headers["Authorization"] = "Basic aGVsbG86d29ybGQ="

            } assertThat { stepResult ->

                stepResult.status isEqualTo 200
                stepResult.body isEqualTo listOf("Han Solo")
            }
        },
        beforeAll = ::startSampleApi,
        afterAll = ::stopSampleApi,
    )

    @TestFactory
    fun `http server error`() = `play scenarios`(
        scenario(name = "when wrong api is called an error is raised") {

            `given http call`<JsonMap>("PATCH method is not allowed") {

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
        afterAll = ::stopSampleApi,
    )

    @TestFactory
    fun `otp flows`() = `play scenarios`(
        scenario(name = "get and validate correct otp") {

            val otp = `given http call`<JsonMap>("get an OTP") {

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

            `given http call`<JsonMap>("validate an OTP") {

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

            `given http call`<JsonMap>("get an OTP") {

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

            `given http call`<JsonMap>("validate an invalid OTP") {

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
    fun `retryable steps`() = `play scenarios`(
        scenario(name = "a step should be retried as specified when not passing") {

            `given http call`<String>(
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
        afterEach = ::stopSampleApi,
    )

    @TestFactory
    fun inventory() = `play scenarios`(
        scenario(name = "get inventory as JsonArray") {

            `given http call`<JsonArray>("get inventory") {

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

            `given http call`<List<Inventory>>("get inventory") {

                url = "http://localhost:8080/inventory"
                method = "GET"
                headers["Authorization"] = "Basic aGVsbG86d29ybGQ="

            } assertThat { stepResult ->

                stepResult.status isEqualTo 200
                stepResult.body isEqualTo listOf (
                    Inventory("weapon", "lightsaber"),
                    Inventory("vehicle", "landspeeder"),
                )
            }
        },
        beforeAll = ::startSampleApi,
        afterAll = ::stopSampleApi,
    )


    @TestFactory
    fun deathStarPlans() = `play scenarios`(
        scenario(name = "give Rebel Alliance Death Star plans") {

            `given http call`<String>("death star plans are not available") {

                url = "http://localhost:8080/death-star-secret-plans"
                method = "GET"
                headers["Authorization"] = "Basic aGVsbG86d29ybGQ="
            } assertThat {
                it.status isEqualTo 404
                it.body isEqualTo "Waiting for Rogue one..."
            }

            `given http call`<String>("give death star plans") {

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

            `given http call`<String>("death star plans are available") {

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
        afterAll = ::stopSampleApi,
    )

    data class Inventory(
        val kind: String,
        val name: String
    )
}
