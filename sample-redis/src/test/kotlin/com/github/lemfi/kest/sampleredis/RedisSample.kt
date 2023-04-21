package com.github.lemfi.kest.sampleredis

import com.github.lemfi.kest.core.cli.assertThat
import com.github.lemfi.kest.junit5.runner.playScenario
import com.github.lemfi.kest.redis.cli.`redis delete key`
import com.github.lemfi.kest.redis.cli.`redis get key`
import com.github.lemfi.kest.redis.cli.`redis insert data`
import org.junit.jupiter.api.TestFactory

class RedisSample {

    @TestFactory
    fun `redis is a planet in a galaxy far far away`() = playScenario(name = "Starwars characters", unwrap = false) {

        `redis insert data`(name = "declare Luke Skywalker") {
            insert { "Luke Skywalker" } `with key` "ls" `in namespace` "light"
        }

        `redis insert data`(name = "declare Han Solo") {
            insert { "Han Solo" } `with key` "hs" `in namespace` "light"
        }

        `redis insert data`(name = "declare Leia Organa (Skywalker) Solo") {
            insert { "Leia Organa (Skywalker) Solo" } `with key` "lo" `in namespace` "light"
        }

        `redis insert data`(name = "declare Anakin Skywalker") {
            insert { "Anakin Skywalker" } `with key` "as" `in namespace` "dark"
        }

        `redis insert data`(name = "declare Senator Palpatine") {
            insert { "Senator Palpatine" } `with key` "sp" `in namespace` "dark"
        }

        `redis get key`(name = "find Luke Skywalker") {
            `read key` { "ls" } `from namespace` "light"
        } assertThat {
            it isEqualTo "Luke Skywalker"
        }

        `redis get key`(name = "find Anakin Skywalker") {
            `read key` { "as" } `from namespace` "dark"
        } assertThat {
            it isEqualTo "Anakin Skywalker"
        }

        `redis delete key`(name = "delete vilains") {
            `delete key` { "*" } `from namespace` "dark"
        }

    }
}