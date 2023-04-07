package com.github.lemfi.kest.samplehttp.multiplescenarios.stepsextracted

import com.github.lemfi.kest.core.builder.ScenarioBuilder
import com.github.lemfi.kest.core.cli.`assert that`
import com.github.lemfi.kest.core.cli.nestedScenario
import com.github.lemfi.kest.http.cli.`given http call`
import com.github.lemfi.kest.json.cli.jsonMatches
import com.github.lemfi.kest.json.model.JsonMap

fun ScenarioBuilder.`say hello`(who: String) {
    `given http call`<String>("$who says hello") {

        url = "http://localhost:8080/hello"
        method = "POST"
        headers["Authorization"] = "Basic aGVsbG86d29ybGQ="
        body = """
            {
                "who": "$who"
            }
            """
    } `assert that` { stepResult ->

        stepResult.status isEqualTo 201
        stepResult.body isEqualTo "Hello $who!"
    }
}

fun ScenarioBuilder.`get greeted`(vararg expectedGreeted: String) {
    `given http call`<List<String>>("${expectedGreeted.joinToString(", ")} were greeted") {

        url = "http://localhost:8080/hello"
        method = "GET"
        headers["Authorization"] = "Basic aGVsbG86d29ybGQ="

    } `assert that` { stepResult ->

        stepResult.status isEqualTo 200
        stepResult.body isEqualTo expectedGreeted.toList()
    }
}

fun ScenarioBuilder.`get otp`() =
    `given http call`<JsonMap>("generate OTP") {

        url = "http://localhost:8080/otp"
        method = "GET"
        headers["Authorization"] = "Basic aGVsbG86d29ybGQ="

    } `assert that` { stepResult ->

        stepResult.status isEqualTo 201
        jsonMatches(
            """
                        {
                            "otp": "{{string}}"
                        }
                    """.trimIndent(), stepResult.body
        )
    }

fun ScenarioBuilder.`validate otp`(otp: String) =

    `given http call`<JsonMap>("validate otp") {

        url = "http://localhost:8080/otp"
        method = "POST"
        headers["Authorization"] = "Basic aGVsbG86d29ybGQ="
        body = otp
        contentType = "text/plain"

    } `assert that` { stepResult ->

        stepResult.status isEqualTo 204
    }

fun ScenarioBuilder.`generate otps`() = nestedScenario<List<String>>("generate 3 OTPs") {

    val otp1 = `get otp`().`map result to` { it.body["otp"] as String }
    val otp2 = `get otp`().`map result to` { it.body["otp"] as String }
    val otp3 = `get otp`().`map result to` { it.body["otp"] as String }

    returns { listOf(otp1(), otp2(), otp3()) }
}