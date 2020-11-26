package com.github.lemfi.kest.sample.stepsextracted

import com.github.lemfi.kest.core.cli.`assert that`
import com.github.lemfi.kest.core.cli.eq
import com.github.lemfi.kest.core.cli.scenario
import com.github.lemfi.kest.executor.http.cli.`given http call`
import com.github.lemfi.kest.junit5.runner.`run scenarios`
import com.github.lemfi.kest.sample.sampleapi.startSampleApi
import com.github.lemfi.kest.sample.sampleapi.stopSampleApi
import org.junit.jupiter.api.TestFactory

class TestHttpServer {

    @TestFactory
    fun `http server hello`() = `run scenarios`(
            scenario {

                name = "api says hello and remembers it!"

                `say hello`("Darth Vader")
                `say hello`("Han Solo")

                `get greeted`("Darth Vader", "Han Solo")
            },
            beforeEach = { startSampleApi() },
            afterEach = { stopSampleApi() }
    )

    @TestFactory
    fun `http server goodbye`() = `run scenarios`(
            scenario {

                name = "api says goodbye and forgets people!"

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
}
