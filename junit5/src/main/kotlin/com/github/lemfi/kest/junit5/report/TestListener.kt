package com.github.lemfi.kest.junit5.report

import org.junit.platform.engine.TestExecutionResult
import org.junit.platform.engine.reporting.ReportEntry
import org.junit.platform.launcher.TestExecutionListener
import org.junit.platform.launcher.TestIdentifier
import org.junit.platform.launcher.TestPlan
import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.io.PrintStream
import java.util.Date
import kotlin.concurrent.getOrSet

@Suppress("unused")
class KestTestListener(report: (Report) -> Unit) : TestExecutionListener {

    private val listener: TestExecutionListener = object : AbstractTestListener(object : ReportWriter {
        override fun writeReport(report: InternalReport) {
            report(report.toReport())
        }
    }) {}

    override fun testPlanExecutionStarted(testPlan: TestPlan?) {
        listener.testPlanExecutionStarted(testPlan)
    }

    override fun testPlanExecutionFinished(testPlan: TestPlan?) {
        listener.testPlanExecutionFinished(testPlan)
    }

    override fun dynamicTestRegistered(testIdentifier: TestIdentifier?) {
        listener.dynamicTestRegistered(testIdentifier)
    }

    override fun executionSkipped(testIdentifier: TestIdentifier?, reason: String?) {
        listener.executionSkipped(testIdentifier, reason)
    }

    override fun executionStarted(testIdentifier: TestIdentifier?) {
        listener.executionStarted(testIdentifier)
    }

    override fun executionFinished(testIdentifier: TestIdentifier?, testExecutionResult: TestExecutionResult?) {
        listener.executionFinished(testIdentifier, testExecutionResult)
    }

    override fun reportingEntryPublished(testIdentifier: TestIdentifier?, entry: ReportEntry?) {
        listener.reportingEntryPublished(testIdentifier, entry)
    }
}

internal abstract class AbstractTestListener(private val reportWriter: ReportWriter) : TestExecutionListener {

    private lateinit var report: InternalReport

    override fun testPlanExecutionStarted(testPlan: TestPlan?) {
        reportWriter.init()
        report = InternalReport()
        report.duration = Date().time
    }

    override fun testPlanExecutionFinished(testPlan: TestPlan?) {
        report.duration = Date().time - report.duration
        reportWriter.writeReport(report)
    }

    override fun executionStarted(testIdentifier: TestIdentifier) {
        val test = testIdentifier.toTestReport()
        test.duration = Date().time
        report.tests.add(test)
        if (testIdentifier.isTest) consoleSpy.start()
    }

    override fun executionFinished(testIdentifier: TestIdentifier, testExecutionResult: TestExecutionResult) {

        if (testIdentifier.isTest) {
            report.total += 1
            if (testExecutionResult.status == TestExecutionResult.Status.SUCCESSFUL) report.nbSuccess += 1
            if (testExecutionResult.status == TestExecutionResult.Status.ABORTED) report.nbSkipped += 1
            if (testExecutionResult.status == TestExecutionResult.Status.FAILED) report.nbFailures += 1
        }
        report.getTest(testIdentifier.uniqueId)?.let { test ->
            if (test is InternalTestReport) {
                test.console = consoleSpy.getText()
                if (testExecutionResult.status == TestExecutionResult.Status.FAILED)
                    test.failure = testExecutionResult.throwable.orElseGet(null)?.stackTraceToString() ?: ""
            }
            test.duration = Date().time - test.duration
            test.status = when (test) {
                is InternalTestReport -> when (testExecutionResult.status) {
                    TestExecutionResult.Status.SUCCESSFUL -> TestStatus.SUCCESS
                    TestExecutionResult.Status.FAILED -> TestStatus.FAILED
                    TestExecutionResult.Status.ABORTED, null -> TestStatus.SKIPPED
                }
                is InternalContainerTestReport ->
                    if (report.children(test.id).any { it.status == TestStatus.FAILED }) TestStatus.FAILED
                    else TestStatus.SUCCESS
            }
        }
    }

    private fun TestIdentifier.toTestReport() =
        if (isTest)
            InternalTestReport(
                id = uniqueId,
                name = displayName,
                parent = parentId.orElseGet { null }
            )
        else InternalContainerTestReport(
            id = uniqueId,
            name = displayName,
            parent = parentId.orElseGet { null }
        )
}

internal class TestListener : AbstractTestListener(FileReportWriter())

private val spy: ThreadLocal<ByteArrayOutputStream> = ThreadLocal<ByteArrayOutputStream>()

private val consoleSpy = ConsoleSpy()

private class ConsoleSpy {

    private val printStream: (PrintStream) -> PrintStream = { spied ->
        PrintStream(object : OutputStream() {
            override fun flush() {
                spy.getOrDefault().flush()
                spied.flush()
            }

            override fun write(b: Int) {
                spy.getOrDefault().write(b)
                spied.write(b)
            }

            override fun write(b: ByteArray) {
                spy.getOrDefault().write(b)
                spied.write(b)
            }

            override fun write(b: ByteArray, off: Int, len: Int) {
                spy.getOrDefault().write(b, off, len)
                spied.write(b, off, len)
            }
        })
    }

    init {
        System.setOut(printStream(System.out))
        System.setErr(printStream(System.err))
    }

    fun getText(): String = spy.get().toString(Charsets.UTF_8)

    fun start() = spy.set(ByteArrayOutputStream())

    private fun ThreadLocal<ByteArrayOutputStream>.getOrDefault() = getOrSet { ByteArrayOutputStream() }
}
