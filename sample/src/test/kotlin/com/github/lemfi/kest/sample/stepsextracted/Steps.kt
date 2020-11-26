package com.github.lemfi.kest.sample.stepsextracted

import com.github.lemfi.kest.core.builder.ScenarioBuilder
import com.github.lemfi.kest.core.cli.`assert that`
import com.github.lemfi.kest.core.cli.eq
import com.github.lemfi.kest.executor.http.cli.`given http call`

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