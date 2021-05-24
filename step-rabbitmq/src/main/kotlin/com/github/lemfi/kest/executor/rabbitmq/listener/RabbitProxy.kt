package com.github.lemfi.kest.executor.rabbitmq.listener

import com.github.lemfi.kest.executor.rabbitmq.model.RabbitMQSnifferProp
import com.rabbitmq.client.AMQP
import com.rabbitmq.client.BasicProperties
import com.rabbitmq.client.Channel
import com.rabbitmq.client.ConnectionFactory
import com.rabbitmq.client.impl.AMQCommand
import com.rabbitmq.client.impl.AMQImpl
import com.rabbitmq.client.impl.Frame
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.net.ServerSocket
import java.net.Socket

private val replies: MutableMap<Pair<String, Int>, String?> = mutableMapOf()

private var server: ServerSocket? = null
private var rabbitmqSocket: Socket? = null

private lateinit var channel: Channel

fun main(args: List<String>) {
    startRabbitMQProxy(
        args[0], args[1], args[2], args[3].toInt(), args[4], RabbitMQSnifferProp(
            true, false, args[5].toInt()
        )
    )
}

@Suppress("BlockingMethodInNonBlockingContext")
fun startRabbitMQProxy(
    rabbitUser: String,
    rabbitPassword: String,
    rabbitHost: String,
    rabbitPort: Int,
    rabbitVhost: String,
    properties: RabbitMQSnifferProp
) {

    if (server == null) {

        GlobalScope.launch {
            server = ServerSocket(properties.port)

            channel = ConnectionFactory().apply {
                setUri("amqp://$rabbitUser:$rabbitPassword@$rabbitHost:$rabbitPort/$rabbitVhost")
            }
                .newConnection("sniffer connection")
                .createChannel()

            rabbitmqSocket = Socket(rabbitHost, rabbitPort)

            while (true) {
                val proxyServer = server!!.accept()
                proxify(proxyServer, rabbitmqSocket!!)
            }
        }
    }
}

@Suppress("BlockingMethodInNonBlockingContext")
private fun CoroutineScope.proxify(proxyServer: Socket, rabbitmqServer: Socket) {

    try {

        // read AMQP Protocol version on connection establishment
        ByteArray(8)
            .also { proxyServer.getInputStream().read(it) }
            .also { rabbitmqServer.getOutputStream().write(it) }

        // handle deliveries
        launch {
            frameReader(rabbitmqServer.getInputStream(), proxyServer.getOutputStream()) { channelNumber ->
                method.visit(object : RabbitCommandVisitor() {
                    override fun visit(x: AMQImpl.Basic.Deliver?): Any {
                        (contentHeader as BasicProperties?)?.replyTo?.let {
                            replies.put(x!!.deliveryTag.toString() to channelNumber, it)
                        }
                        return Unit
                    }
                })
            }
        }

        // handle acks
        run {
            frameReader(proxyServer.getInputStream(), rabbitmqServer.getOutputStream()) { channelNumber ->
                method.visit(object : RabbitCommandVisitor() {
                    override fun visit(x: AMQImpl.Basic.Ack?): Any {
                        replies.getOrDefault(x!!.deliveryTag.toString() to channelNumber, null)?.let {
                            channel.basicPublish("", it, AMQP.BasicProperties(), "ack".toByteArray(Charsets.UTF_8))
                        }

                        return Unit
                    }
                })
            }
        }

    } finally {
        proxyServer.close()
        rabbitmqServer.close()
    }
}

private fun frameReader(
    inputStream: InputStream,
    outputStream: OutputStream,
    l: AMQCommand.(channelNumber: Int) -> Unit
) {

    val commands: MutableMap<Int, AMQCommand> = mutableMapOf()

    fun Frame.onCommandReady(l: AMQCommand.() -> Unit) {
        val command = commands[channel] ?: AMQCommand().also { commands.put(channel, it) }
        if (command.handleFrame(this)) {
            command.l()
            commands.remove(channel)
        }
    }

    while (true) {
        val frame: Frame = Frame.readFrom(DataInputStream(inputStream))
        frame.writeTo(DataOutputStream(outputStream))
        frame.onCommandReady { l(frame.channel) }
    }

}

private abstract class RabbitCommandVisitor : AMQImpl.MethodVisitor {
    override fun visit(x: AMQImpl.Connection.Start?): Any {
        return Unit
    }

    override fun visit(x: AMQImpl.Connection.StartOk?): Any {
        return Unit
    }

    override fun visit(x: AMQImpl.Connection.Secure?): Any {
        return Unit
    }

    override fun visit(x: AMQImpl.Connection.SecureOk?): Any {
        return Unit
    }

    override fun visit(x: AMQImpl.Connection.Tune?): Any {
        return Unit
    }

    override fun visit(x: AMQImpl.Connection.TuneOk?): Any {
        return Unit
    }

    override fun visit(x: AMQImpl.Connection.Open?): Any {
        return Unit
    }

    override fun visit(x: AMQImpl.Connection.OpenOk?): Any {
        return Unit
    }

    override fun visit(x: AMQImpl.Connection.Close?): Any {
        return Unit
    }

    override fun visit(x: AMQImpl.Connection.CloseOk?): Any {
        return Unit
    }

    override fun visit(x: AMQImpl.Connection.Blocked?): Any {
        return Unit
    }

    override fun visit(x: AMQImpl.Connection.Unblocked?): Any {
        return Unit
    }

    override fun visit(x: AMQImpl.Connection.UpdateSecret?): Any {
        return Unit
    }

    override fun visit(x: AMQImpl.Connection.UpdateSecretOk?): Any {
        return Unit
    }

    override fun visit(x: AMQImpl.Channel.Open?): Any {
        return Unit
    }

    override fun visit(x: AMQImpl.Channel.OpenOk?): Any {
        return Unit
    }

    override fun visit(x: AMQImpl.Channel.Flow?): Any {
        return Unit
    }

    override fun visit(x: AMQImpl.Channel.FlowOk?): Any {
        return Unit
    }

    override fun visit(x: AMQImpl.Channel.Close?): Any {
        return Unit
    }

    override fun visit(x: AMQImpl.Channel.CloseOk?): Any {
        return Unit
    }

    override fun visit(x: AMQImpl.Access.Request?): Any {
        return Unit
    }

    override fun visit(x: AMQImpl.Access.RequestOk?): Any {
        return Unit
    }

    override fun visit(x: AMQImpl.Exchange.Declare?): Any {
        return Unit
    }

    override fun visit(x: AMQImpl.Exchange.DeclareOk?): Any {
        return Unit
    }

    override fun visit(x: AMQImpl.Exchange.Delete?): Any {
        return Unit
    }

    override fun visit(x: AMQImpl.Exchange.DeleteOk?): Any {
        return Unit
    }

    override fun visit(x: AMQImpl.Exchange.Bind?): Any {
        return Unit
    }

    override fun visit(x: AMQImpl.Exchange.BindOk?): Any {
        return Unit
    }

    override fun visit(x: AMQImpl.Exchange.Unbind?): Any {
        return Unit
    }

    override fun visit(x: AMQImpl.Exchange.UnbindOk?): Any {
        return Unit
    }

    override fun visit(x: AMQImpl.Queue.Declare?): Any {
        return Unit
    }

    override fun visit(x: AMQImpl.Queue.DeclareOk?): Any {
        return Unit
    }

    override fun visit(x: AMQImpl.Queue.Bind?): Any {
        return Unit
    }

    override fun visit(x: AMQImpl.Queue.BindOk?): Any {
        return Unit
    }

    override fun visit(x: AMQImpl.Queue.Purge?): Any {
        return Unit
    }

    override fun visit(x: AMQImpl.Queue.PurgeOk?): Any {
        return Unit
    }

    override fun visit(x: AMQImpl.Queue.Delete?): Any {
        return Unit
    }

    override fun visit(x: AMQImpl.Queue.DeleteOk?): Any {
        return Unit
    }

    override fun visit(x: AMQImpl.Queue.Unbind?): Any {
        return Unit
    }

    override fun visit(x: AMQImpl.Queue.UnbindOk?): Any {
        return Unit
    }

    override fun visit(x: AMQImpl.Basic.Qos?): Any {
        return Unit
    }

    override fun visit(x: AMQImpl.Basic.QosOk?): Any {
        return Unit
    }

    override fun visit(x: AMQImpl.Basic.Consume?): Any {
        return Unit
    }

    override fun visit(x: AMQImpl.Basic.ConsumeOk?): Any {
        return Unit
    }

    override fun visit(x: AMQImpl.Basic.Cancel?): Any {
        return Unit
    }

    override fun visit(x: AMQImpl.Basic.CancelOk?): Any {
        return Unit
    }

    override fun visit(x: AMQImpl.Basic.Publish?): Any {
        return Unit
    }

    override fun visit(x: AMQImpl.Basic.Return?): Any {
        return Unit
    }

    override fun visit(x: AMQImpl.Basic.Get?): Any {
        return Unit
    }

    override fun visit(x: AMQImpl.Basic.GetOk?): Any {
        return Unit
    }

    override fun visit(x: AMQImpl.Basic.GetEmpty?): Any {
        return Unit
    }

    override fun visit(x: AMQImpl.Basic.Reject?): Any {
        return Unit
    }

    override fun visit(x: AMQImpl.Basic.RecoverAsync?): Any {
        return Unit
    }

    override fun visit(x: AMQImpl.Basic.Recover?): Any {
        return Unit
    }

    override fun visit(x: AMQImpl.Basic.RecoverOk?): Any {
        return Unit
    }

    override fun visit(x: AMQImpl.Basic.Nack?): Any {
        return Unit
    }

    override fun visit(x: AMQImpl.Tx.Select?): Any {
        return Unit
    }

    override fun visit(x: AMQImpl.Tx.SelectOk?): Any {
        return Unit
    }

    override fun visit(x: AMQImpl.Tx.Commit?): Any {
        return Unit
    }

    override fun visit(x: AMQImpl.Tx.CommitOk?): Any {
        return Unit
    }

    override fun visit(x: AMQImpl.Tx.Rollback?): Any {
        return Unit
    }

    override fun visit(x: AMQImpl.Tx.RollbackOk?): Any {
        return Unit
    }

    override fun visit(x: AMQImpl.Confirm.Select?): Any {
        return Unit
    }

    override fun visit(x: AMQImpl.Confirm.SelectOk?): Any {
        return Unit
    }

    override fun visit(x: AMQImpl.Basic.Deliver?): Any {
        return Unit
    }

    override fun visit(x: AMQImpl.Basic.Ack?): Any {
        return Unit
    }
}