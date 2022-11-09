package com.github.lemfi.kest.samplerabbit

import com.github.lemfi.kest.core.cli.`assert that`
import com.github.lemfi.kest.core.cli.eq
import com.github.lemfi.kest.core.model.`by intervals of`
import com.github.lemfi.kest.core.model.ms
import com.github.lemfi.kest.core.model.seconds
import com.github.lemfi.kest.core.model.times
import com.github.lemfi.kest.junit5.runner.`play scenario`
import com.github.lemfi.kest.rabbitmq.cli.`create rabbitmq queue`
import com.github.lemfi.kest.rabbitmq.cli.`given message from rabbitmq queue`
import com.github.lemfi.kest.rabbitmq.cli.`given messages from rabbitmq queue`
import com.github.lemfi.kest.rabbitmq.cli.`given number of messages in rabbitmq queue`
import com.github.lemfi.kest.rabbitmq.cli.`publish rabbitmq message`
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestFactory

class TestReadWrite {

    @BeforeEach
    fun beforeEach() = startRabbitApplication()

    @AfterEach
    fun afterEach() = stopRabbitApplication()

    @TestFactory
    fun `Leia asks for help`() = `play scenario`(name = "Leia asks for help via RabbitMQ") {

        `create rabbitmq queue`("Obi-Wan Kenobi should be born before he can receive a message") {
            `create queue` { "obi-wan_kenobi" }
        }

        `given number of messages in rabbitmq queue`(retry = 20.times `by intervals of` 200.ms) {
            queue = "obi-wan_kenobi"
        } `assert that` {
            eq(0L, it.total)
        }

        `publish rabbitmq message`("declare that R2D2 might deliver a message to Obi-Wan Kenobi") {
            publish { "obi-wan_kenobi" } `to exchange` "" `with routing key` "R2D2"
        }

        `given messages from rabbitmq queue`<String>(
            name = "message from Leia was broadcasted twice",
            retry = 10.times `by intervals of` 1.seconds
        ) {
            queue = "obi-wan_kenobi"
            messageTransformer = { toString(Charsets.UTF_8) }
            nbMessages = 2
        } `assert that` {
            eq("Au secours obi-wan_kenobi, vous êtes notre seul espoir !", it.first().message)
            eq("Au secours obi-wan_kenobi, vous êtes notre seul espoir !", it.last().message)
        }

        `given number of messages in rabbitmq queue` {
            queue = "obi-wan_kenobi"
        } `assert that` {
            eq(0L, it.total)
        }
    }

    @TestFactory
    fun `Leia asks for help - check broadcast once`() =
        `play scenario`(name = "Leia asks for help via RabbitMQ, check broadcast once") {

            `create rabbitmq queue`("Obi-Wan Kenobi should be born before he can receive a message") {
                `create queue` { "obi-wan_kenobi" }
            }

            `publish rabbitmq message`("declare that R2D2 might deliver a message to Obi-Wan Kenobi") {
                publish { "obi-wan_kenobi" } `to exchange` "" `with routing key` "R2D2"
            }

            `given message from rabbitmq queue`<String>(
                name = "message from Leia was broadcasted",
                retry = 10.times `by intervals of` 1.seconds
            ) {
                queue = "obi-wan_kenobi"
                messageTransformer = { toString(Charsets.UTF_8) }
            } `assert that` {
                eq("Au secours obi-wan_kenobi, vous êtes notre seul espoir !", it.message)
            }

            `given number of messages in rabbitmq queue` {
                queue = "obi-wan_kenobi"
            } `assert that` {
                eq(0L, it.total)
            }

        }

}