package com.github.lemfi.kest.sample.sampleapi

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.OutputStream
import java.io.PrintWriter
import java.net.ServerSocket
import java.net.SocketException
import java.net.URLDecoder
import java.util.*

private var server: ServerSocket? = null

private val helloPeople = mutableListOf<String>()
private val otps = mutableListOf<String>()

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
                    while (reader.readLine().also { line = it }.let { it != null && it != "" } ) {
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
                } catch (e: SocketException) {}
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

    if (method == "POST" && path == "/hello") handleSayHello(jacksonObjectMapper().readValue(body!!, Map::class.java)["who"] as String)
    else if (method == "GET" && path == "/hello") handleListHello()
    else if (method == "GET" && path == "/otp") handleOtp()
    else if (method == "POST" && path == "/otp") handleValidateOtp(body!!)

    else if (method == "DELETE" && path.startsWith("/hello")) handleSayGoodbye(URLDecoder.decode(path.substringAfter("who="), Charsets.UTF_8))

    else PrintWriter(this, true).apply {
        println("""
                    HTTP/1.1 405 OK
                    Content-Type: application/json

                    {"message": "method not allowed", "code": 1, "description": "method $method is not allowed for path $path"}"""
                .trimIndent())
    }
}

private fun OutputStream.handleSayHello(who: String) {

    helloPeople.add(who)

    PrintWriter(this, true).apply {
        println("""
                    HTTP/1.1 201 OK
                    Content-Type: text/plain

                    Hello $who!"""
                .trimIndent())
    }
}

private fun OutputStream.handleOtp() {

    val otp = UUID.randomUUID().toString()
    otps.add(otp)

    PrintWriter(this, true).apply {
        println("""
                    HTTP/1.1 201 OK
                    Content-Type: application/json

                    {"otp": "$otp"}"""
                .trimIndent())
    }
}

private fun OutputStream.handleValidateOtp(otp: String) {

    val ok = otps.contains(otp)
    if (ok) {
        PrintWriter(this, true).apply {
            println(
                    """
                            HTTP/1.1 204 OK
                                
                            
                            """.trimIndent())
        }
    } else {
        PrintWriter(this, true).apply {
            println(
                    """
                            HTTP/1.1 400 OK
                            Content-Type: application/json
                            
                            {"message": "method not allowed", "code": 1, "description": "otp $otp is invalid"}
                            """.trimIndent())
        }
    }
}

private fun OutputStream.handleSayGoodbye(who: String) {

    helloPeople.remove(who)

    PrintWriter(this, true).apply {
        println("""
                    HTTP/1.1 201 OK
                    Content-Type: text/plain

                    Goodbye $who!"""
                .trimIndent())
    }
}

private fun OutputStream.handleListHello() {

    PrintWriter(this, true).apply {
        println("""
                    HTTP/1.1 200 OK
                    Content-Type: application/json

                    [${helloPeople.map { """"$it"""" }.joinToString(", ")}]
                """.trimIndent())
    }
}