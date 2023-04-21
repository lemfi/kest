package com.github.lemfi.kest.samplehttp.multiplescenarios.scenariosextracted

import com.github.lemfi.kest.core.builder.ScenarioBuilder
import com.github.lemfi.kest.core.cli.assertThat
import com.github.lemfi.kest.core.cli.nestedScenario
import com.github.lemfi.kest.core.model.StandaloneStepResult
import com.github.lemfi.kest.http.cli.givenHttpCall
import com.github.lemfi.kest.http.model.HttpResponse
import com.github.lemfi.kest.json.model.JsonMap
import com.github.lemfi.kest.samplehttp.multiplescenarios.stepsextracted.`get otp`

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
    givenHttpCall<List<String>>("check ${expectedGreeted.joinToString(", ")} were greeted") {

        url = "http://localhost:8080/hello"
        method = "GET"
        headers["Authorization"] = "Basic aGVsbG86d29ybGQ="

    } assertThat { stepResult ->

        stepResult.status isEqualTo 200
        stepResult.body isEqualTo expectedGreeted.toList()
    }
}

fun ScenarioBuilder.`validate otp`(otp: () -> String) =

    givenHttpCall<JsonMap>("validate an otp") {

        url = "http://localhost:8080/otp"
        method = "POST"
        headers["Authorization"] = "Basic aGVsbG86d29ybGQ="
        body = otp()
        contentType = "text/plain"

    } assertThat { stepResult ->

        stepResult.status isEqualTo 204 { "When validating an OTP http status should be 204" }
    }

fun ScenarioBuilder.generateOtps() = nestedScenario<List<String>>("generate 3 OTPs") {

    val extractResult: StandaloneStepResult<HttpResponse<JsonMap>>.() -> String = { this().body["otp"] as String }

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