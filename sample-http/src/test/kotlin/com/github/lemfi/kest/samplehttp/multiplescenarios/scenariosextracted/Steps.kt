package com.github.lemfi.kest.samplehttp.multiplescenarios.scenariosextracted

import com.github.lemfi.kest.core.builder.ScenarioBuilder
import com.github.lemfi.kest.core.cli.`assert that`
import com.github.lemfi.kest.core.cli.eq
import com.github.lemfi.kest.core.cli.step
import com.github.lemfi.kest.core.model.StepPostExecution
import com.github.lemfi.kest.executor.http.cli.`given http call`
import com.github.lemfi.kest.executor.http.model.HttpResponse
import com.github.lemfi.kest.json.model.JsonMap
import com.github.lemfi.kest.samplehttp.multiplescenarios.stepsextracted.`get otp`

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

        eq(201, stepResult.status)
        eq("Hello $who!", stepResult.body)
    }
}

fun ScenarioBuilder.`get greeted`(vararg expectedGreeted: String) {
    `given http call`<List<String>>("check ${expectedGreeted.joinToString(", ")} were greeted") {

        url = "http://localhost:8080/hello"
        method = "GET"
        headers["Authorization"] = "Basic aGVsbG86d29ybGQ="

    } `assert that` { stepResult ->

        eq(200, stepResult.status)
        eq(expectedGreeted.toList(), stepResult.body)
    }
}

fun ScenarioBuilder.`validate otp`(otp: () -> String) =

    `given http call`<JsonMap>("validate an otp") {

        description { "validate otp ${otp()}" }

        url = "http://localhost:8080/otp"
        method = "POST"
        headers["Authorization"] = "Basic aGVsbG86d29ybGQ="
        body = otp()
        contentType = "text/plain"

    } `assert that` { stepResult ->

        eq(204, stepResult.status) { "When validating an OTP http status should be 204" }
    }

fun ScenarioBuilder.generateOtps() = step<List<String>>("generate 3 OTPs") {

    val extractResult: StepPostExecution<HttpResponse<JsonMap>>.() -> String = { this().body["otp"] as String }

    val otp1 = `get otp`()
    val otp2 = `get otp`()
    val otp3 = `get otp`()

    returns {
        listOf(
            otp1.extractResult(),
            otp2.extractResult(),
            otp3.extractResult()
        )
    }
}