package com.github.lemfi.kest.sample.scenariosextracted

import com.github.lemfi.kest.core.cli.`assert that`
import com.github.lemfi.kest.core.cli.eq
import com.github.lemfi.kest.core.cli.scenario
import com.github.lemfi.kest.executor.http.cli.`given http call`

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
