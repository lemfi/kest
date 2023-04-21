package com.github.lemfi.kest.samplerabbit

import com.github.lemfi.kest.core.cli.assertThat
import com.github.lemfi.kest.core.model.byIntervalsOf
import com.github.lemfi.kest.core.model.ms
import com.github.lemfi.kest.core.model.seconds
import com.github.lemfi.kest.core.model.times
import com.github.lemfi.kest.junit5.runner.playScenario
import com.github.lemfi.kest.rabbitmq.cli.createRabbitmqQueue
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
    fun `Leia asks for help`() = playScenario(name = "Leia asks for help via RabbitMQ") {

        createRabbitmqQueue("Obi-Wan Kenobi should be born before he can receive a message") {
            createQueue { "obi-wan kenobi" }
        }

        `given number of messages in rabbitmq queue`(retry = 20.times byIntervalsOf 200.ms) {
            queue = "obi-wan kenobi"
        } assertThat {
            it.total isEqualTo 0L
        }

        `publish rabbitmq message`("declare that R2D2 might deliver a message to Obi-Wan Kenobi") {
            publish { "obi-wan kenobi" } toExchange "" withRoutingKey "R2D2"
        }

        `given messages from rabbitmq queue`<String>(
            name = "message from Leia was broadcasted twice",
            retry = 10.times byIntervalsOf 1.seconds
        ) {
            queue = "obi-wan kenobi"
            messageTransformer = { toString(Charsets.UTF_8) }
            nbMessages = 2
        } assertThat {
            it.first().message isEqualTo "Au secours obi-wan kenobi, vous êtes notre seul espoir !"
            it.last().message isEqualTo "Au secours obi-wan kenobi, vous êtes notre seul espoir !"
        }

        `given number of messages in rabbitmq queue` {
            queue = "obi-wan kenobi"
        } assertThat {
            it.total isEqualTo 0L
        }
    }

    @TestFactory
    fun `Leia asks for help - check broadcast once`() =
        playScenario(name = "Leia asks for help via RabbitMQ, check broadcast once") {

            createRabbitmqQueue("Obi-Wan Kenobi should be born before he can receive a message") {
                createQueue { "obi-wan kenobi" }
            }

            `publish rabbitmq message`("declare that R2D2 might deliver a message to Obi-Wan Kenobi") {
                publish { "obi-wan kenobi" } toExchange "" withRoutingKey "R2D2"
            }

            `given message from rabbitmq queue`<String>(
                name = "message from Leia was broadcasted",
                retry = 10.times byIntervalsOf 1.seconds
            ) {
                queue = "obi-wan kenobi"
                messageTransformer = { toString(Charsets.UTF_8) }
            } assertThat {
                it.message isEqualTo "Au secours obi-wan kenobi, vous êtes notre seul espoir !"
            }

            `given number of messages in rabbitmq queue` {
                queue = "obi-wan kenobi"
            } assertThat {
                it.total isEqualTo 0L
            }

        }

}