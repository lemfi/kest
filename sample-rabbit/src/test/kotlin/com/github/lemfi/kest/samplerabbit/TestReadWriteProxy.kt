package com.github.lemfi.kest.samplerabbit

import com.github.lemfi.kest.core.cli.`assert that`
import com.github.lemfi.kest.core.cli.eq
import com.github.lemfi.kest.core.model.`delayed by`
import com.github.lemfi.kest.core.model.seconds
import com.github.lemfi.kest.core.model.times
import com.github.lemfi.kest.core.properties.kestconfig
import com.github.lemfi.kest.executor.rabbitmq.cli.`create rabbitmq queue`
import com.github.lemfi.kest.executor.rabbitmq.cli.`given message from rabbitmq queue`
import com.github.lemfi.kest.executor.rabbitmq.cli.`publish rabbitmq message`
import com.github.lemfi.kest.executor.rabbitmq.listener.startRabbitMQProxy
import com.github.lemfi.kest.executor.rabbitmq.listener.stopRabbitMQProxy
import com.github.lemfi.kest.junit5.runner.`play scenario`
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestFactory

class TestReadWriteProxy {

    init {
        kestconfig.clear()
        System.setProperty("kest-conf", "/proxy.yml")
    }

    @BeforeEach
    fun beforeEach() {
        startRabbitMQProxy("amqp://guest:guest@localhost:5672", "%2F", 5673)
        startRabbitApplication("5673")
    }

    @AfterEach
    fun afterEach() {
        stopRabbitApplication()
        stopRabbitMQProxy()
    }

    @TestFactory
    fun `Leia asks for help`() = `play scenario` {

        name { "Hello World is written to my queue" }

        `create rabbitmq queue`("Obi-Wan Kenobi should be born before he can receive a message") {
            `create queue` { "obi-wan_kenobi" }
        }

        `publish rabbitmq message`("declare that R2D2 might deliver a message to Obi-Wan Kenobi") {
            publish { "obi-wan_kenobi" } `to exchange` "" `with routing key` "R2D2"
        }

        `given message from rabbitmq queue`<String>(name = "read message from Leia") {
            queue = "obi-wan_kenobi"
            messageTransformer = { toString(Charsets.UTF_8) }
        } `assert that` {
            eq("Au secours obi-wan_kenobi, vous êtes notre seul espoir !", it)
        }

    }

}