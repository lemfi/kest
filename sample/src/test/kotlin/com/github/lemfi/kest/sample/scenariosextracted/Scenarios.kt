package com.github.lemfi.kest.sample.scenariosextracted

import com.github.lemfi.kest.core.cli.`assert that`
import com.github.lemfi.kest.core.cli.eq
import com.github.lemfi.kest.core.cli.scenario
import com.github.lemfi.kest.core.cli.steps
import com.github.lemfi.kest.executor.http.cli.`given http call`
import com.github.lemfi.kest.json.cli.jsonMatchesObject
import com.github.lemfi.kest.json.model.JsonMap
import com.github.lemfi.kest.sample.stepsextracted.`validate otp`
import com.github.lemfi.kest.sample.stepsextracted.`generate otps`

val `api says hello and remembers it!` = scenario {

    name = "api says hello and remembers it!"

    `say hello`("Darth Vader")
    `say hello`("Han Solo")

    `get greeted`("Darth Vader", "Han Solo")
}

val `api says goodbye and forgets people!` = scenario {

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

}

val `get and validate correct otp` =
        scenario {

            name = "get and validate correct otp"

            lateinit var otps: List<String>
            steps {
                steps {
                    `generate otps` {
                        otps = this
                    }
                }
            }

            steps {
                steps {
                    (otps.indices).forEach {
                        `validate otp`(otps[it])
                    }
                }
            }

        }

val `get and validate wrong otp` = scenario {

    name = "get and validate wrong otp"

    `given http call`<JsonMap> {

        url = "http://localhost:8080/otp"
        method = "GET"
        headers["Authorization"] = "Basic aGVsbG86d29ybGQ="

    } `assert that` { stepResult ->

        eq(201, stepResult.status)
        jsonMatchesObject("""
                        {
                            "otp": "{{string}}" 
                        }
                    """.trimIndent(), stepResult.body)
    }

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

}