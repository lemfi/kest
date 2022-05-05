package com.github.lemfi.kest.samplehttp.sampleapi

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.OutputStream
import java.io.PrintWriter
import java.net.ServerSocket
import java.net.SocketException
import java.net.URLDecoder
import java.nio.charset.Charset
import java.util.UUID

private var server: ServerSocket? = null

private val helloPeople = mutableListOf<String>()
private val otps = mutableListOf<String>()
private var deathStarPlan: String? = null

@OptIn(DelicateCoroutinesApi::class)
@Suppress("BlockingMethodInNonBlockingContext")
fun startSampleApi() {
    if (server == null) {

        server = ServerSocket(8080)

        GlobalScope.launch {

            while (server != null) {
                try {
                    val sampleApi = server!!.accept()

                    var contentLength = -1

                    val reader = sampleApi.getInputStream().bufferedReader(Charsets.UTF_8)
                    var line: String?
                    var requestLine: String? = null
                    var body: String? = null
                    while (reader.readLine().also { line = it }.let { it != null && it != "" }) {
                        val currentLine = line!!
                        if (requestLine == null) {
                            requestLine = currentLine
                        }
                        if (currentLine.startsWith("Content-Length")) {
                            val headerValue = currentLine.substringAfter(":").trim { it <= ' ' }
                            contentLength = headerValue.toInt()
                        }
                    }
                    if (contentLength > 0) {
                        body = CharArray(contentLength).let {
                            reader.read(it)
                            it.joinToString("")
                        }
                    }

                    val output = sampleApi.getOutputStream()
                    requestLine?.let {
                        output.handleRequest(it, body)
                    }
                    output.flush()
                    output.close()
                } catch (_: SocketException) {
                }
            }
        }
    }
}

fun stopSampleApi() {
    server?.close()
    helloPeople.clear()
    server = null
}


private fun OutputStream.handleRequest(request: String, body: String?) {

    val (method, path) = request.split(" ").let { it[0] to it[1] }

    if (method == "POST" && path == "/hello") handleSayHello(
        jacksonObjectMapper().readValue(
            body!!,
            Map::class.java
        )["who"] as String
    )
    else if (method == "POST" && path == "/death-star-secret-plans") handleDeathStarSecretPlans(
        body!!
    )
    else if (path == "/death-star-secret-plans") handleGetDeathStarSecretPlans()
    else if (method == "GET" && path == "/hello") handleListHello()
    else if (method == "GET" && path == "/inventory") handleInventory()
    else if (method == "GET" && path == "/hello-redirect") handleRedirectHello()
    else if (method == "GET" && path == "/oh-if-you-retry-it-shall-pass") handleRetry()
    else if (method == "GET" && path == "/otp") handleOtp()
    else if (method == "POST" && path == "/otp") handleValidateOtp(body!!)
    else if (method == "DELETE" && path.startsWith("/hello")) handleSayGoodbye(
        URLDecoder.decode(
            path.substringAfter("who="),
            Charsets.UTF_8
        )
    )
    else PrintWriter(this, true).apply {
        println(
            """
                    HTTP/1.1 405 Method Not Allowed
                    Content-Type: application/json

                    {"message": "method not allowed", "code": 1, "description": "method $method is not allowed for path $path"}"""
                .trimIndent()
        )
    }
}

private fun OutputStream.handleDeathStarSecretPlans(body: String) {

    val sep = body.lines().first()
    val plan =
        body.substringBefore("$sep--").split(sep).map { it.trim() }.filterNot { it.isBlank() }
            .map { it.substringAfter("\n\r") }.first()

    println(plan)
    deathStarPlan = plan
    PrintWriter(this, true).apply {
        println(
            """
                    HTTP/1.1 201 Created
                    Content-Type: text/plain

                    May the Force be with you!"""
                .trimIndent()
        )
    }
}

private fun OutputStream.handleGetDeathStarSecretPlans() {

    PrintWriter(this, true).apply {
        println(
            """
                    |HTTP/1.1 ${if (deathStarPlan != null) "200 OK" else "404 Not Found"}
                    |Content-Type: text/plain
                    |Content-Length: ${deathStarPlan?.toByteArray(Charset.defaultCharset())?.size ?: 24}
                    |
                    |${deathStarPlan ?: "Waiting for Rogue one..."}"""
                .trimMargin()
        )
    }
}

private fun OutputStream.handleSayHello(who: String) {

    helloPeople.add(who)

    PrintWriter(this, true).apply {
        println(
            """
                    HTTP/1.1 201 Created
                    Content-Type: text/plain

                    Hello $who!"""
                .trimIndent()
        )
    }
}

private fun OutputStream.handleOtp() {

    val otp = UUID.randomUUID().toString()
    otps.add(otp)

    PrintWriter(this, true).apply {
        println(
            """
                    HTTP/1.1 201 Created
                    Content-Type: application/json

                    {"otp": "$otp"}"""
                .trimIndent()
        )
    }
}

private fun OutputStream.handleRedirectHello() {
    PrintWriter(this, true).apply {
        println(
            """
                HTTP/1.1 302 Found
                Location: http://localhost:8080/hello

                """.trimIndent()
        )
    }
}

var nbRetryCalls = 0
private fun OutputStream.handleRetry() {

    nbRetryCalls++

    PrintWriter(this, true).apply {
        println(
            """
                HTTP/1.1 200 OK
                Content-Type: text/plain

                You called me $nbRetryCalls times!
                """.trimIndent()
        )
    }
}

private fun OutputStream.handleValidateOtp(otp: String) {

    val ok = otps.contains(otp)
    if (ok) {
        PrintWriter(this, true).apply {
            println(
                """
                            HTTP/1.1 204 No Content
                            
                            """.trimIndent()
            )
        }
    } else {
        PrintWriter(this, true).apply {
            println(
                """
                            HTTP/1.1 400 Bad Request
                            Content-Type: application/json
                            
                            {"message": "method not allowed", "code": 1, "description": "otp $otp is invalid"}
                            """.trimIndent()
            )
        }
    }
}

private fun OutputStream.handleSayGoodbye(who: String) {

    helloPeople.remove(who)

    PrintWriter(this, true).apply {
        println(
            """
                    HTTP/1.1 201 Created
                    Content-Type: text/plain

                    Goodbye $who!"""
                .trimIndent()
        )
    }
}

private fun OutputStream.handleListHello() {

    PrintWriter(this, true).apply {
        println(
            """
                    HTTP/1.1 200 OK
                    Content-Type: application/json

                    [${helloPeople.joinToString(", ") { """"$it"""" }}]
                """.trimIndent()
        )
    }
}

private fun OutputStream.handleInventory() {

    PrintWriter(this, true).apply {
        println(
            """
                    HTTP/1.1 200 OK
                    Content-Type: application/json

                    [
                        {
                            "kind": "weapon",
                            "name": "lightsaber"
                        },
                        {
                            "kind": "vehicle",
                            "name": "landspeeder"
                        }
                    ]
                """.trimIndent()
        )
    }
}