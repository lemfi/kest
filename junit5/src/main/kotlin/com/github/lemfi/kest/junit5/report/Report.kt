package com.github.lemfi.kest.junit5.report

import java.util.UUID

data class Report(
    val total: Long,
    val nbSuccess: Long,
    val nbFailures: Long,
    val nbSkipped: Long,
    val duration: Long,
    val html: String,
    val scenarios: List<ContainerTestReport>,
)

internal fun InternalReport.toReport() = Report(
    total = total,
    nbSuccess = nbSuccess,
    nbFailures = nbFailures,
    nbSkipped = nbSkipped,
    duration = duration,
    html = toHTML(),
    scenarios = root().map { it.toTestReport() as ContainerTestReport }
)

internal data class InternalReport(
    val tests: MutableList<AInternalTestReport> = mutableListOf(),
    var total: Long = 0,
    var nbSuccess: Long = 0,
    var nbFailures: Long = 0,
    var nbSkipped: Long = 0,
    var duration: Long = 0,
) {
    fun getTest(id: String): AInternalTestReport? = tests.firstOrNull { it.id == id }

    fun children(id: String) = tests.filter { it.parent == id }

    fun root() = tests.filter { it.parent == null }.let {
        if (it.size == 1) children(it.first().id) else it
    }

    fun AInternalTestReport.toTestReport(): ATestReport =
        when (this) {
            is InternalContainerTestReport -> toTestReport()
            is InternalTestReport -> toTestReport()
        }

    private fun InternalContainerTestReport.toTestReport(): ATestReport =
        ContainerTestReport(
            name = name,
            status = status,
            duration = duration,
            steps = children(id).map {
                it.toTestReport()
            },
        )

    private fun InternalTestReport.toTestReport(): ATestReport =
        TestReport(
            name = name,
            status = status,
            duration = duration,
            console = console,
            failure = failure,
        )


    private fun TestStatus.toCSS() =
        when (this) {
            TestStatus.SUCCESS -> "ok"
            TestStatus.FAILED -> "ko"
            TestStatus.SKIPPED -> "skipped"
        }

    fun toHTML() = """

<!DOCTYPE html>
<html lang="en">
    <head>
        <title>Test execution ${if (nbFailures == 0L) "SUCCESSFUL" else "FAILED"}</title>
        
        <meta charset="UTF-8">
        
        <script type="text/javascript">
            function showHide(id) {
                var elem = document.getElementById(id);
                if (elem.classList.contains("hidden")) {
                    elem.className = ""
                } else {
                    elem.className = "hidden"
                }
            }
        </script>
      
        <style type="text/css">
:root {
    --royal-blue-dark: #01295fff;
    --teal-blue: #437f97ff;
    --olive-drab-3: #849324ff;
    --acid-green: #BBCF3A;
    --forest-green-crayola: rgb(118, 145, 122);
    --honey-yellow: #ffb30fff;
    --red: #fd151bff;
}

body {
    margin: 20px;
    font-family: sans-serif;
}

h1 {
    background: var(--royal-blue-dark);
    color: var(--honey-yellow);
    padding: 5px 10px;
}

h1 sm {
    float: right;
}

table {
    text-align: center;
    border-spacing: 10px;
    margin: 0 auto;
}
td.summary {
    background-color: var(--teal-blue);
    padding: 20px;
    border-radius: 10px;
    font-weight: bold;
    color: #FFFFFF;
}

.summary.ko {
    background-color: var(--red);
}

.summary.ok {
    background-color: var(--acid-green);
}

.summary.skipped {
    background-color: var(--honey-yellow);
}

.ratio {
    padding-top: 20px;
    float: right;
    width: 100%;
}

.ratio div {
    color: #FFFFFF;
    text-align: center;
    font-weight: bold;
    width: 200px;
    display: inline-block;
    height: 20px;
    margin: -3px;
}

ul {
    padding: 0;
    list-style-type: none;
}

dl.ko span.status {
    color: var(--red);
}

dl.skipped span.status {
    color: var(--honey-yellow);
}

dl.ok span.status {
    color: var(--acid-green);
}

dt {
    padding: 2px 5px;
    margin: 1px 0;
}

.hidden {
    display: none;
}

dt a {
    text-decoration: none;
    color: black;
}

dt span.time {
    float: right;
}

dt span.status {
    border-left: solid 18px;
    padding: 0 4px 0 0;
}

dl {
    background-color: #FFFFFF;

}

pre.out {
    color: var(--royal-blue-dark);
}

pre.err {
    color: var(--red);
}
        </style>
    </head>
    <body>
        <h1>Test Execution ${if (nbFailures == 0L) "SUCCESSFUL" else "FAILED"} <sm>${duration.duration()}</sm></h1>

        ${buildSummary()}
        ${buildTests(root())}
    </body>
</html>
"""

    private fun Long.duration(): String {
        return if (this < 1000) "${this}ms"
        else {
            val seconds = this / 1000
            val ms = this % 1000
            if (seconds < 60) "${seconds}.${ms}s"
            else {
                val minutes = seconds / 60
                val secs = seconds % 60
                "${minutes}m ${secs}.${ms}s"
            }
        }
    }

    private fun buildSummary() =
        """
        <table>
            <tr>
                <td rowspan="3" class="summary all">${total} tests</td>
        ${
            if (nbFailures > 0) {
                """<td class="summary ko">${nbFailures} failed</td>"""
            } else ""
        }
        ${
            if (nbSkipped > 0) {
                """<td class="summary skipped">${nbSkipped} skipped</td>"""
            } else ""
        }
        ${
            if (nbSuccess > 0) {
                """<td class="summary ok">${nbSuccess} passed</td>"""
            } else ""
        }
            </tr>
    ${
            if (nbFailures > 0) {
                """
                 <tr>
                <td colspan="3">
                    <div class="ratio">
                        <div style="background-color: var(--red); width: ${(nbFailures * 100 / total) + if (nbFailures * 100 % total != 0L) 1 else 0}%"></div>
                        <div style="background-color: var(--honey-yellow); width: ${(nbSkipped * 100 / total) + if (nbSkipped * 100 % total != 0L) 1 else 0}%"></div>
                        <div style="background-color: var(--acid-green); width: ${nbSuccess * 100 / total}%"></div>
                    </div>
                </td>
            </tr>
            """
            } else ""
        }
        </table>
    """

    private fun buildTests(tests: List<AInternalTestReport>) =
        tests.joinToString("") {
            when (it) {
                is InternalContainerTestReport -> it.buildTest()
                is InternalTestReport -> it.buildTest()
            }
        }

    private fun InternalContainerTestReport.buildTest(): String =
        UUID.randomUUID().toString().let { htmlId ->
            """
        <ul>
            <li>
                <dl class="${status.toCSS()}">
                    <dt><span class="status"></span><a href="javascript:void(0)" onclick="javascript:showHide('$htmlId')">${this.name} <span class="time">${this.duration.duration()}</span></a></dt>
                    <dd id="$htmlId" class="hidden">
                        ${buildTests(children(id))}
                    </dd>
                </dl>
            </li>
        </ul>
    """.trimIndent()
        }

    private fun InternalTestReport.buildTest() =
        UUID.randomUUID().toString().let { id ->
            """   
<dl class="${status.toCSS()}">
    <dt><span class="status"></span><a href="javascript:void(0)" onclick="javascript:showHide('$id')">${this.name} <span class="time">${this.duration.duration()}</span></a></dt>
    <dd id="$id" class="hidden">
${
                if (console.isNotBlank()) "<pre class=\"out\">${
                    console
                        .replace("<", "&lt;")
                        .replace(">", "&gt;")
                }</pre>" else ""
            }
${
                if (failure.isNotBlank()) "<pre class=\"err\">${
                    failure
                        .replace("<", "&lt;")
                        .replace(">", "&gt;")
                }</pre>" else ""
            }
    </dd>
</dl>
    """.trimIndent()
        }
}

sealed class ATestReport {
    abstract val name: String
    abstract val status: TestStatus
    abstract val duration: Long
}

data class TestReport(
    override val name: String,
    override val status: TestStatus,
    override val duration: Long,
    val console: String,
    val failure: String,
) : ATestReport()

data class ContainerTestReport(
    override val name: String,
    override val status: TestStatus,
    override val duration: Long,
    val steps: List<ATestReport>,
) : ATestReport()

internal sealed class AInternalTestReport {
    abstract val id: String
    abstract val name: String
    abstract var status: TestStatus
    abstract var duration: Long
    abstract val parent: String?
}

enum class TestStatus {
    SUCCESS, FAILED, SKIPPED
}

internal data class InternalContainerTestReport(
    override val id: String,
    override val name: String,
    override var status: TestStatus = TestStatus.FAILED,
    override var duration: Long = 0,
    override val parent: String? = null,
) : AInternalTestReport()

internal data class InternalTestReport(
    override val id: String,
    override val name: String,
    override var status: TestStatus = TestStatus.FAILED,
    override var duration: Long = 0,
    var console: String = "",
    var failure: String = "",
    override val parent: String? = null,
) : AInternalTestReport()