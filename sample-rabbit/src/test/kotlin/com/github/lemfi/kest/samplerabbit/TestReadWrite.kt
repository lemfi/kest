package com.github.lemfi.kest.samplerabbit

import com.github.lemfi.kest.core.cli.assertThat
import com.github.lemfi.kest.core.model.byIntervalsOf
import com.github.lemfi.kest.core.model.ms
import com.github.lemfi.kest.core.model.seconds
import com.github.lemfi.kest.core.model.times
import com.github.lemfi.kest.junit5.runner.playScenario
import com.github.lemfi.kest.rabbitmq.cli.createRabbitmqQueue
import com.github.lemfi.kest.rabbitmq.cli.givenMessageFromRabbitmqQueue
import com.github.lemfi.kest.rabbitmq.cli.givenMessagesFromRabbitmqQueue
import com.github.lemfi.kest.rabbitmq.cli.givenNumberOfMessagesInRabbitmqQueue
import com.github.lemfi.kest.rabbitmq.cli.publishRabbitmqMessage
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestFactory
import org.testcontainers.containers.RabbitMQContainer

class TestReadWrite {

    companion object {

        private lateinit var amqpConnectionString: String
        private lateinit var apiConnectionString: String

        @JvmStatic
        @BeforeAll
        fun beforeAll() {
            RabbitMQContainer("rabbitmq:3.7.25-management-alpine")
                .apply {
                    start()
                    amqpConnectionString = "amqp://guest:guest@localhost:${getMappedPort(5672)}"
                    apiConnectionString = "http://localhost:${getMappedPort(15672)}"
                }
        }
    }

    @BeforeEach
    fun beforeEach() = startRabbitApplication(amqpConnectionString)

    @AfterEach
    fun afterEach() = stopRabbitApplication()

    @TestFactory
    fun `Leia asks for help`() = playScenario(name = "Leia asks for help via RabbitMQ") {

        createRabbitmqQueue("Obi-Wan Kenobi should be born before he can receive a message") {
            connection = amqpConnectionString
            createQueue { "obi-wan kenobi" }
        }

        givenNumberOfMessagesInRabbitmqQueue(retry = 20.times byIntervalsOf 200.ms) {
            connection = apiConnectionString
            queue = "obi-wan kenobi"
        } assertThat {
            it.total isEqualTo 0L
        }

        publishRabbitmqMessage("declare that R2D2 might deliver a message to Obi-Wan Kenobi") {
            connection = amqpConnectionString
            publish { "obi-wan kenobi" } toExchange "" withRoutingKey "R2D2"
        }

        givenMessagesFromRabbitmqQueue<String>(
            name = "message from Leia was broadcasted twice",
            retry = 10.times byIntervalsOf 1.seconds
        ) {
            connection = amqpConnectionString
            queue = "obi-wan kenobi"
            messageTransformer = { toString(Charsets.UTF_8) }
            nbMessages = 2
        } assertThat {
            it.first().message isEqualTo "Au secours obi-wan kenobi, vous êtes notre seul espoir !"
            it.last().message isEqualTo "Au secours obi-wan kenobi, vous êtes notre seul espoir !"
        }

        givenNumberOfMessagesInRabbitmqQueue {
            connection = apiConnectionString
            queue = "obi-wan kenobi"
        } assertThat {
            it.total isEqualTo 0L
        }
    }

    @TestFactory
    fun `Leia asks for help - check broadcast once`() =
        playScenario(name = "Leia asks for help via RabbitMQ, check broadcast once") {

            createRabbitmqQueue("Obi-Wan Kenobi should be born before he can receive a message") {
                connection = amqpConnectionString
                createQueue { "obi-wan kenobi" }
            }

            publishRabbitmqMessage("declare that R2D2 might deliver a message to Obi-Wan Kenobi") {
                connection = amqpConnectionString
                publish { "obi-wan kenobi" } toExchange "" withRoutingKey "R2D2"
            }

            givenMessageFromRabbitmqQueue<String>(
                name = "message from Leia was broadcasted",
                retry = 10.times byIntervalsOf 1.seconds
            ) {
                connection = amqpConnectionString
                queue = "obi-wan kenobi"
                messageTransformer = { toString(Charsets.UTF_8) }
            } assertThat {
                it.message isEqualTo "Au secours obi-wan kenobi, vous êtes notre seul espoir !"
            }

            givenNumberOfMessagesInRabbitmqQueue {
                connection = apiConnectionString
                queue = "obi-wan kenobi"
            } assertThat {
                it.total isEqualTo 0L
            }

        }

}