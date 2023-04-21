package com.github.lemfi.kest.sampleredis

import com.github.lemfi.kest.core.cli.assertThat
import com.github.lemfi.kest.junit5.runner.playScenario
import com.github.lemfi.kest.redis.cli.redisDeleteKey
import com.github.lemfi.kest.redis.cli.redisGetKey
import com.github.lemfi.kest.redis.cli.`redis insert data`
import org.junit.jupiter.api.TestFactory

class RedisSample {

    @TestFactory
    fun `redis is a planet in a galaxy far far away`() = playScenario(name = "Starwars characters", unwrap = false) {

        `redis insert data`(name = "declare Luke Skywalker") {
            insert { "Luke Skywalker" } withKey "ls" inNamespace "light"
        }

        `redis insert data`(name = "declare Han Solo") {
            insert { "Han Solo" } withKey "hs" inNamespace "light"
        }

        `redis insert data`(name = "declare Leia Organa (Skywalker) Solo") {
            insert { "Leia Organa (Skywalker) Solo" } withKey "lo" inNamespace "light"
        }

        `redis insert data`(name = "declare Anakin Skywalker") {
            insert { "Anakin Skywalker" } withKey "as" inNamespace "dark"
        }

        `redis insert data`(name = "declare Senator Palpatine") {
            insert { "Senator Palpatine" } withKey "sp" inNamespace "dark"
        }

        redisGetKey(name = "find Luke Skywalker") {
            readKey { "ls" } fromNamespace "light"
        } assertThat {
            it isEqualTo "Luke Skywalker"
        }

        redisGetKey(name = "find Anakin Skywalker") {
            readKey { "as" } fromNamespace "dark"
        } assertThat {
            it isEqualTo "Anakin Skywalker"
        }

        redisDeleteKey(name = "delete vilains") {
            deleteKey { "*" } fromNamespace "dark"
        }

    }
}