package com.github.lemfi.kest.sample.allinone

import com.github.lemfi.kest.core.cli.`assert that`
import com.github.lemfi.kest.core.cli.eq
import com.github.lemfi.kest.core.cli.scenario
import com.github.lemfi.kest.executor.http.cli.`given http call`
import com.github.lemfi.kest.json.cli.jsonMatchesObject
import com.github.lemfi.kest.json.model.JsonMap
import com.github.lemfi.kest.junit5.runner.`run scenarios`
import com.github.lemfi.kest.sample.sampleapi.startSampleApi
import com.github.lemfi.kest.sample.sampleapi.stopSampleApi
import org.junit.jupiter.api.TestFactory

class TestHttpServer {

    @TestFactory
    fun `http server hello`() = `run scenarios`(
            scenario {

                name = "api says hello and remembers it!"

                `given http call`<String> {

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

                `given http call`<String> {

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

                `given http call`<List<String>> {

                    url = "http://localhost:8080/hello"
                    method = "GET"
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
    fun `http server goodbye`() = `run scenarios`(
            scenario {

                name = "api says goodbye and forgets people!"

                `given http call`<String> {

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

                `given http call`<String> {

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

                `given http call`<String> {

                    url = "http://localhost:8080/hello?who=Darth Vader"
                    method = "DELETE"
                    headers["Authorization"] = "Basic aGVsbG86d29ybGQ="

                } `assert that` { stepResult ->

                    eq(201, stepResult.status)
                    eq("Goodbye Darth Vader!", stepResult.body)
                }

                `given http call`<List<String>> {

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
    fun `http server error`() = `run scenarios`(
            scenario {

                name = "when wrong api is called an error is raised"

                `given http call`<JsonMap> {

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
                    jsonMatchesObject("{{error}}", stepResult.body)
                }

            },
            beforeEach = { startSampleApi() },
            afterEach = { stopSampleApi() }
    )
}
