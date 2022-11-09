package com.github.lemfi.kest.rabbitmq.executor

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.lemfi.kest.core.logger.LoggerFactory
import com.github.lemfi.kest.core.model.Execution
import com.github.lemfi.kest.json.model.JsonMap
import com.github.lemfi.kest.rabbitmq.model.RabbitMQMessageCount
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URLEncoder
import java.util.Base64


internal data class RabbitMQCountMessagesExecution(
    val connection: String,
    val user: String,
    val password: String,
    val vhost: String,
    val queue: String,
) : Execution<RabbitMQMessageCount>() {

    private val encodedVhost: String = URLEncoder.encode(vhost, Charsets.UTF_8)
    private val encodedQueue: String = URLEncoder.encode(queue, Charsets.UTF_8)
    private val bass64Credentials: String = Base64.getEncoder().encodeToString("$user:$password".toByteArray())

    override fun execute(): RabbitMQMessageCount {

        return OkHttpClient.Builder()
            .build().newCall(
                Request.Builder()
                    .url("$connection/api/queues/$encodedVhost/$encodedQueue")
                    .method("GET", null)
                    .header("Authorization", "Basic $bass64Credentials")
                    .build()
            )
            .execute()
            .let {
                jacksonObjectMapper().readValue(it.body?.byteStream(), object : TypeReference<JsonMap>() {})
                    .let { queueDetails ->
                        RabbitMQMessageCount(
                            ready = runCatching {
                                queueDetails["messages_ready"].toString().toLong()
                            }.getOrDefault(-1L),
                            unacked = runCatching {
                                queueDetails["messages_unacknowledged"].toString().toLong()
                            }.getOrDefault(-1L),
                            total = runCatching {
                                queueDetails["messages"].toString().toLong()
                            }.getOrDefault(-1L),
                        )
                    }
            }.also { count ->

                if (count.ready == -1L || count.unacked == -1L || count.total == -1L)
                    LoggerFactory.getLogger("RABBITMQ-Kest").info(
                        """
                    |Count messages in queue not available yet
                    |
                    |vhost: $vhost
                    |queue: $queue
                    |
                    |ready messages         : unavailable
                    |unacknowledged messages: unavailable
                    |total                  : unavailable
                    |
                    |""".trimMargin()
                    )
                else
                    LoggerFactory.getLogger("RABBITMQ-Kest").info(
                        """
                    |Count messages in queue:
                    |
                    |vhost: $vhost
                    |queue: $queue
                    |
                    |ready messages         : ${count.ready}
                    |unacknowledged messages: ${count.unacked}
                    |total                  : ${count.total}
                    |
                    |""".trimMargin()
                    )
            }
    }

}
