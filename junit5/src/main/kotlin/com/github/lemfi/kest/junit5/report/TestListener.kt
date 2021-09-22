package com.github.lemfi.kest.junit5.report

import com.github.lemfi.kest.junit5.model.junit5RunnerProperty
import org.junit.platform.engine.TestExecutionResult
import org.junit.platform.engine.reporting.ReportEntry
import org.junit.platform.launcher.TestExecutionListener
import org.junit.platform.launcher.TestIdentifier
import org.junit.platform.launcher.TestPlan
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.OutputStream
import java.io.PrintStream
import java.util.*


internal class TestListener : TestExecutionListener {

    private val consoleSpy = ConsoleSpy()
    private val file = junit5RunnerProperty { report }?.let { File(it) }

    lateinit var report: Report

    override fun testPlanExecutionStarted(testPlan: TestPlan?) {
        if (file != null) {
            file.delete()
            report = Report()
            report.duration = Date().time
        }
    }

    override fun testPlanExecutionFinished(testPlan: TestPlan?) {
        if (file != null) {
            report.duration = Date().time - report.duration
            file.parentFile.mkdirs()
            file.createNewFile()
            file.appendText(report.build())
        }
    }

    override fun dynamicTestRegistered(testIdentifier: TestIdentifier) {
        if (file != null) {
            super.dynamicTestRegistered(testIdentifier)
        }
    }

    override fun executionSkipped(testIdentifier: TestIdentifier?, reason: String?) {
        if (file != null) {
            super.executionSkipped(testIdentifier, reason)
        }
    }

    override fun executionStarted(testIdentifier: TestIdentifier) {
        if (file != null) {
            val test = testIdentifier.toTestReport()
            test.duration = Date().time
            report.tests.add(test)
            if (testIdentifier.isTest) consoleSpy.start()
        }
    }

    override fun executionFinished(testIdentifier: TestIdentifier, testExecutionResult: TestExecutionResult) {
        if (file != null) {
            consoleSpy.stop()

            if (testIdentifier.isTest) {
                report.total += 1
                if (testExecutionResult.status == TestExecutionResult.Status.SUCCESSFUL) report.nbSuccess += 1
                if (testExecutionResult.status == TestExecutionResult.Status.ABORTED) report.nbSkipped += 1
                if (testExecutionResult.status == TestExecutionResult.Status.FAILED) report.nbFailures += 1
            }
            report.getTest(testIdentifier.uniqueId)?.let { test ->
                if (test is TestReport) {
                    test.console = consoleSpy.getText()
                    if (testExecutionResult.status == TestExecutionResult.Status.FAILED)
                        test.failure = testExecutionResult.throwable.orElseGet(null)?.stackTraceToString() ?: ""
                }
                test.duration = Date().time - test.duration
                test.status = when (test) {
                    is TestReport -> when (testExecutionResult.status) {
                        TestExecutionResult.Status.SUCCESSFUL -> TestStatus.SUCCESS
                        TestExecutionResult.Status.FAILED -> TestStatus.FAILED
                        TestExecutionResult.Status.ABORTED, null -> TestStatus.SKIPPED
                    }
                    is ContainerTestReport ->
                        if (report.children(test.id).any { it.status == TestStatus.FAILED }) TestStatus.FAILED
                        else TestStatus.SUCCESS
                }
            }
        }
    }

    override fun reportingEntryPublished(testIdentifier: TestIdentifier?, entry: ReportEntry?) {
        if (file != null) {
            super.reportingEntryPublished(testIdentifier, entry)
        }
    }

    private fun TestIdentifier.toTestReport() =
        if (isTest)
            TestReport(
                id = uniqueId,
                name = displayName,
                parent = parentId.orElseGet { null }
            )
        else ContainerTestReport(
            id = uniqueId,
            name = displayName,
            parent = parentId.orElseGet { null }
        )

}

private class ConsoleSpy {

    private val out = System.out
    private val err = System.err

    private val spy = ByteArrayOutputStream()

    private val printStream: (PrintStream, PrintStream) -> PrintStream = { spied, spy ->
        PrintStream(object : OutputStream() {
            override fun flush() {
                spy.flush()
                spied.flush()
            }

            override fun write(b: Int) {
                spy.write(b)
                spied.write(b)
            }

            override fun write(b: ByteArray) {
                spy.write(b)
                spied.write(b)
            }

            override fun write(b: ByteArray, off: Int, len: Int) {
                spy.write(b, off, len)
                spied.write(b, off, len)
            }
        })
    }

    fun getText() = spy.toString(Charsets.UTF_8)

    fun start() = spy
        .also {
            it.reset()
            System.setOut(printStream(out, PrintStream(it)))
            System.setErr(printStream(err, PrintStream(it)))
        }

    fun stop() {
        System.setOut(out)
        System.setErr(err)
    }
}
