package com.github.lemfi.kest.samplehttp.multiplescenarios.stepsextracted

import com.github.lemfi.kest.core.builder.ScenarioBuilder
import com.github.lemfi.kest.core.cli.assertThat
import com.github.lemfi.kest.core.cli.nestedScenario
import com.github.lemfi.kest.http.cli.givenHttpCall
import com.github.lemfi.kest.http.model.NoContent
import com.github.lemfi.kest.json.cli.json
import com.github.lemfi.kest.json.cli.matches
import com.github.lemfi.kest.json.cli.validator
import com.github.lemfi.kest.json.model.JsonMap

fun ScenarioBuilder.`say hello`(who: String) {
    givenHttpCall<String>("$who says hello") {

        url = "http://localhost:8080/hello"
        method = "POST"
        headers["Authorization"] = "Basic aGVsbG86d29ybGQ="
        body = """
            {
                "who": "$who"
            }
            """
    } assertThat { stepResult ->

        stepResult.status isEqualTo 201
        stepResult.body isEqualTo "Hello $who!"
    }
}

fun ScenarioBuilder.`get greeted`(vararg expectedGreeted: String) {
    givenHttpCall<List<String>>("${expectedGreeted.joinToString(", ")} were greeted") {

        url = "http://localhost:8080/hello"
        method = "GET"
        headers["Authorization"] = "Basic aGVsbG86d29ybGQ="

    } assertThat { stepResult ->

        stepResult.status isEqualTo 200
        stepResult.body isEqualTo expectedGreeted.toList()
    }
}

fun ScenarioBuilder.`get otp`() =
    givenHttpCall<JsonMap>("generate OTP") {

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

fun ScenarioBuilder.`validate otp`(otp: String) =

    givenHttpCall<NoContent>("validate otp") {

        url = "http://localhost:8080/otp"
        method = "POST"
        headers["Authorization"] = "Basic aGVsbG86d29ybGQ="
        body = otp
        contentType = "text/plain"

    } assertThat { stepResult ->

        stepResult.status isEqualTo 204
    }

fun ScenarioBuilder.`generate otps`() = nestedScenario<List<String>>("generate 3 OTPs") {

    val otp1 = `get otp`() mapResultTo { it.body["otp"] as String }
    val otp2 = `get otp`() mapResultTo { it.body["otp"] as String }
    val otp3 = `get otp`() mapResultTo { it.body["otp"] as String }

    returns { listOf(otp1(), otp2(), otp3()) }
}