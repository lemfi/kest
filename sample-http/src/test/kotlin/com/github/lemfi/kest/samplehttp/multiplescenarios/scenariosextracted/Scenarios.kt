@file:Suppress("ObjectPropertyName")

package com.github.lemfi.kest.samplehttp.multiplescenarios.scenariosextracted

import com.github.lemfi.kest.core.cli.assertThat
import com.github.lemfi.kest.core.cli.nestedScenario
import com.github.lemfi.kest.core.cli.scenario
import com.github.lemfi.kest.http.cli.`given http call`
import com.github.lemfi.kest.json.cli.json
import com.github.lemfi.kest.json.cli.matches
import com.github.lemfi.kest.json.cli.validator
import com.github.lemfi.kest.json.model.JsonMap

val `api says hello and remembers it!` = scenario(name = "api says hello and remembers it!") {

    `say hello`("Darth Vader")
    `say hello`("Han Solo")

    `get greeted`("Darth Vader", "Han Solo")
}

val `api says goodbye and forgets people!` = scenario(name = "api says goodbye and forgets people!") {

    `say hello`("Darth Vader")
    `say hello`("Han Solo")

    `given http call`<String>("say goodbye to Darth Vader") {

        url = "http://localhost:8080/hello?who=Darth Vader"
        method = "DELETE"
        headers["Authorization"] = "Basic aGVsbG86d29ybGQ="

    } assertThat { stepResult ->

        stepResult.status isEqualTo 201
        stepResult.body isEqualTo "Goodbye Darth Vader!"
    }

    `get greeted`("Han Solo")

}

val `get and validate correct otp` =
    scenario(name = "get and validate correct otp") {

        val generatedOtps = generateOtps()

        nestedScenario("validate OTPs") {
            val otps = generatedOtps()
            (otps.indices).forEach {
                `validate otp` { otps[it] }
            }
        }

    }

val `get and validate wrong otp` = scenario(name = "get and validate wrong otp") {

    `given http call`<JsonMap>("get OTP") {

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

    `given http call`<JsonMap>("try to validate wrong otp") {

        url = "http://localhost:8080/otp"
        method = "POST"
        headers["Authorization"] = "Basic aGVsbG86d29ybGQ="
        body = "whatever"
        contentType = "text/plain"

    } assertThat { stepResult ->

        stepResult.status isEqualTo 400
        json(stepResult.body) matches validator { "{{error}}" }
    }

}