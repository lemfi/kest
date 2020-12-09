package com.github.lemfi.kest.sample.stepsextracted

import com.github.lemfi.kest.core.builder.ScenarioBuilder
import com.github.lemfi.kest.core.cli.`assert that`
import com.github.lemfi.kest.core.cli.eq
import com.github.lemfi.kest.executor.http.cli.`given http call`
import com.github.lemfi.kest.executor.http.model.HttpResponse
import com.github.lemfi.kest.json.cli.jsonMatchesObject
import com.github.lemfi.kest.json.model.JsonMap

fun ScenarioBuilder.`say hello`(who: String) {
    `given http call`<String> {

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
    `given http call`<List<String>> {

        url = "http://localhost:8080/hello"
        method = "GET"
        headers["Authorization"] = "Basic aGVsbG86d29ybGQ="

    } `assert that` { stepResult ->

        eq(200, stepResult.status)
        eq(expectedGreeted.toList(), stepResult.body)
    }
}

fun ScenarioBuilder.`get otp`(withResult: HttpResponse<JsonMap>.()->Unit = {}) {
    `given http call`<JsonMap> {

        url = "http://localhost:8080/otp"
        method = "GET"
        headers["Authorization"] = "Basic aGVsbG86d29ybGQ="

        this.withResult(withResult)

    } `assert that` { stepResult ->

        eq(201, stepResult.status)
        jsonMatchesObject("""
                        {
                            "otp": "{{string}}"
                        }
                    """.trimIndent(), stepResult.body)
    }
}

fun ScenarioBuilder.`validate otp`(otp: String, l: HttpResponse<JsonMap>.()->Unit = {}) =

        `given http call`<JsonMap> {

            url = "http://localhost:8080/otp"
            method = "POST"
            headers["Authorization"] = "Basic aGVsbG86d29ybGQ="
            body = otp
            contentType = "text/plain"

            withResult(l)

        } `assert that` { stepResult ->

            eq(204, stepResult.status)
        }

fun ScenarioBuilder.`generate otps`(l: List<String>.()->Unit) {
    lateinit var otp1: String
    `get otp` {
        otp1 = body["otp"] as String
    }
    lateinit var otp2: String
    `get otp` {
        otp2 = body["otp"] as String
    }
    lateinit var otp3: String
    `get otp` {
        otp3 = body["otp"] as String

        listOf(otp1, otp2, otp3).l()
    }
}