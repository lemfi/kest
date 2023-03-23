package com.github.lemfi.kest.junit5.report

import com.github.lemfi.kest.junit5.model.junit5RunnerProperty
import org.junit.platform.engine.TestExecutionResult
import org.junit.platform.engine.reporting.ReportEntry
import org.junit.platform.launcher.TestExecutionListener
import org.junit.platform.launcher.TestIdentifier
import org.junit.platform.launcher.TestPlan
import java.io.File
import java.util.Date

interface IKestExecutionListener {

    fun onTestPlanStarted(report: Report)

    fun onTestPlanEnded(report: Report)

    fun onStepEnded(step: ATestReport)

    fun onStepStarted(step: ATestReport)


}

@Suppress("unused")
fun kestTestListener(listener: KestTestListenerBuilder.() -> Unit) =
    KestTestListenerBuilder().apply(listener).build()

@Suppress("unused")
class KestTestListenerBuilder {

    private var testPlanStarted: (Report) -> Unit = {}
    private var testPlanEnded: (Report) -> Unit = {}
    private var stepStarted: (ATestReport) -> Unit = {}
    private var stepEnded: (ATestReport) -> Unit = {}

    fun onTestPlanStarted(testPlanStarted: (Report) -> Unit) {
        this.testPlanStarted = testPlanStarted
    }

    fun onTestPlanEnded(testPlanEnded: (Report) -> Unit) {
        this.testPlanEnded = testPlanEnded
    }

    fun onStepEnded(stepEnded: (ATestReport) -> Unit) {
        this.stepEnded = stepEnded
    }

    fun onStepStarted(stepStarted: (ATestReport) -> Unit) {
        this.stepStarted = stepStarted
    }

    internal fun build() = KestTestListener(object : IKestExecutionListener {
        override fun onTestPlanStarted(report: Report) {
            testPlanStarted(report)
        }

        override fun onTestPlanEnded(report: Report) {
            testPlanEnded(report)
        }

        override fun onStepEnded(step: ATestReport) {
            stepEnded(step)
        }

        override fun onStepStarted(step: ATestReport) {
            stepStarted(step)
        }
    })
}


@Suppress("unused")
class KestTestListener(listener: IKestExecutionListener) : TestExecutionListener,
    IKestExecutionListener by listener {

    private val listener: TestExecutionListener by lazy {
        object : AbstractTestListener(this) {}
    }

    override fun testPlanExecutionStarted(testPlan: TestPlan) {
        listener.testPlanExecutionStarted(testPlan)
    }

    override fun testPlanExecutionFinished(testPlan: TestPlan) {
        listener.testPlanExecutionFinished(testPlan)
    }

    override fun dynamicTestRegistered(testIdentifier: TestIdentifier) {
        listener.dynamicTestRegistered(testIdentifier)
    }

    override fun executionSkipped(testIdentifier: TestIdentifier, reason: String) {
        listener.executionSkipped(testIdentifier, reason)
    }

    override fun executionStarted(testIdentifier: TestIdentifier) {
        listener.executionStarted(testIdentifier)
    }

    override fun executionFinished(testIdentifier: TestIdentifier, testExecutionResult: TestExecutionResult) {
        listener.executionFinished(testIdentifier, testExecutionResult)
    }

    override fun reportingEntryPublished(testIdentifier: TestIdentifier, entry: ReportEntry) {
        listener.reportingEntryPublished(testIdentifier, entry)
    }
}

internal abstract class AbstractTestListener(
    private val kestListener: KestTestListener? = null
) : TestExecutionListener {

    protected lateinit var report: InternalReport

    private val logs by lazy {
        Class
            .forName("com.github.lemfi.kest.core.logger.KestLogs")
            .getMethod("getLog")
    }

    override fun testPlanExecutionStarted(testPlan: TestPlan) {
        report = InternalReport()
        report.duration = Date().time
        report.tests.addAll(
            testPlan
                .roots
                .flatMap { testPlan.getChildren(it) }
                .map {
                    it.toTestReport()
                }
        )

        kestListener?.onTestPlanStarted(report.toReport().copy(html = "", duration = 0))
    }

    override fun testPlanExecutionFinished(testPlan: TestPlan) {
        report.duration = Date().time - report.duration

        kestListener?.onTestPlanEnded(report.toReport())
    }

    override fun executionStarted(testIdentifier: TestIdentifier) {
        (report
            .getTest(testIdentifier.uniqueId) ?: testIdentifier
            .toTestReport()
            .apply { report.tests.add(this) })
            .let { test ->

                test.duration = Date().time
                test.console = logs.invoke(null) as String

                kestListener?.also {
                    when (test) {
                        is InternalTestReport -> it.onStepStarted(
                            TestReport(
                                id = test.id,
                                name = test.name,
                                status = TestStatus.ONGOING,
                                duration = 0,
                                level = test.level,
                                log = test.console,
                                failure = test.failure,
                            )
                        )

                        is InternalContainerTestReport -> it.onStepStarted(
                            ContainerTestReport(
                                id = test.id,
                                name = test.name,
                                status = TestStatus.ONGOING,
                                level = test.level,
                                duration = 0,
                                log = test.console,
                                steps = emptyList()
                            )
                        )
                    }
                }
            }
    }

    override fun executionSkipped(testIdentifier: TestIdentifier, reason: String) {
        if (testIdentifier.isTest) {
            report.total += 1
            report.nbSkipped += 1
        }

        report.getTest(testIdentifier.uniqueId)?.let { test ->
            if (test is InternalTestReport) {

                test.console = logs.invoke(null) as String
                test.failure = reason
            }
            test.duration = Date().time - test.duration
            test.status = TestStatus.SKIPPED

            kestListener?.also {
                when (test) {
                    is InternalTestReport -> it.onStepEnded(
                        TestReport(
                            id = test.id,
                            name = test.name,
                            status = test.status,
                            duration = test.duration,
                            level = test.level,
                            log = test.console,
                            failure = test.failure,
                        )
                    )

                    is InternalContainerTestReport -> it.onStepEnded(
                        ContainerTestReport(
                            id = test.id,
                            name = test.name,
                            status = test.status,
                            duration = test.duration,
                            log = test.console,
                            level = test.level,
                            steps = emptyList()
                        )
                    )
                }

            }

        }
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

                test.console = logs.invoke(null) as String
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
                    when {
                        report.children(test.id).any { it.status == TestStatus.FAILED } -> TestStatus.FAILED
                        report.children(testIdentifier.uniqueId)
                            .any { it.status == TestStatus.SKIPPED } -> TestStatus.SKIPPED
                        testExecutionResult.status == TestExecutionResult.Status.FAILED -> TestStatus.FAILED
                        else -> TestStatus.SUCCESS
                    }
            }


            kestListener?.also {
                when (test) {
                    is InternalTestReport -> it.onStepEnded(
                        TestReport(
                            id = test.id,
                            name = test.name,
                            status = test.status,
                            duration = test.duration,
                            level = test.level,
                            log = test.console,
                            failure = test.failure,
                        )
                    )

                    is InternalContainerTestReport -> it.onStepEnded(
                        ContainerTestReport(
                            id = test.id,
                            name = test.name,
                            status = test.status,
                            duration = test.duration,
                            level = test.level,
                            log = test.console,
                            steps = emptyList()
                        )
                    )
                }

            }

        }
    }

    private fun TestIdentifier.toTestReport() =
        if (isTest)
            InternalTestReport(
                id = uniqueId,
                name = displayName,
                level = uniqueIdObject.segments.size,
                parent = parentId.orElseGet { null }
            )
        else InternalContainerTestReport(
            id = uniqueId,
            name = displayName,
            level = uniqueIdObject.segments.size,
            parent = parentId.orElseGet { null }
        )
}

internal class TestListener : AbstractTestListener() {

    private val file = junit5RunnerProperty { report }?.let { File(it) }

    override fun testPlanExecutionStarted(testPlan: TestPlan) {
        super.testPlanExecutionStarted(testPlan)
        file?.delete()
    }

    override fun testPlanExecutionFinished(testPlan: TestPlan) {
        super.testPlanExecutionFinished(testPlan)
        file?.apply {
            parentFile.mkdirs()
            createNewFile()
            appendText(report.toHTML())
        }
    }
}
