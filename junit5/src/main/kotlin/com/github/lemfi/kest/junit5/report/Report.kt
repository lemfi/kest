package com.github.lemfi.kest.junit5.report

import java.util.*

data class Report(
    val tests: MutableList<ATestReport> = mutableListOf(),
    var total: Long = 0,
    var nbSuccess: Long = 0,
    var nbFailures: Long = 0,
    var duration: Long = 0
) {
    fun getTest(id: String): ATestReport? = tests.firstOrNull { it.id == id }

    fun children(id: String) = tests.filter { it.parent == id }

    private fun root() = tests.filter { it.parent == null }.let {
        if (it.size == 1) children(it.first().id) else it
    }

    fun build() = """

<!DOCTYPE html>
<html lang="en">
    <head>
        <title>Test execution ${if (nbFailures == 0L) "SUCCESSFUL" else "FAILED"}</title>
        
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

.summary.ok {
    background-color: var(--acid-green);
}
.summary.ko {
    background-color: var(--red);
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
    margin: -2px;
}

ul {
    padding: 0;
    list-style-type: none;
}

dl::before {
    content: "\2B1B";
    float: left;
    padding: 0 4px 0 0;
}

dl.ok::before {
    color: var(--acid-green);
}

dl.ko::before {
    color: var(--red);
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

dt span {
    float: right;
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
                <td rowspan="2" class="summary all">${total} tests</td>
    ${
            if(nbFailures > 0) {
                """<td class="summary ko">${nbFailures} failed</td>"""
            } else ""
        }
                <td class="summary ok">${nbSuccess} passed</td>
            </tr>
    ${
            if (nbFailures > 0) {
                """
                 <tr>
                <td colspan="2">
                    <div class="ratio">
                        <div style="background-color: var(--red); width: ${(nbFailures * 100 / total) + if (nbFailures * 100 % total != 0L) 1 else 0}%"></div>
                        <div style="background-color: var(--acid-green); width: ${nbSuccess * 100 / total}%"></div>
                    </div>
                </td>
            </tr>
            """
            } else ""
        }
        </table>
    """

    private fun buildTests(tests: List<ATestReport>) =
        tests.map {
            when (it) {
                is ContainerTestReport -> it.buildTest()
                is TestReport -> it.buildTest()
            }
        }.joinToString("")

    private fun ContainerTestReport.buildTest(): String =
        UUID.randomUUID().toString().let { htmlId ->
            """
        <ul>
            <li>
                <dl class="${if (this.success) "ok" else "ko"}">
                    <dt><a href="javascript:void(0)" onclick="javascript:showHide('$htmlId')">${this.name} <span>${this.duration.duration()}</span></a></dt>
                    <dd id="$htmlId" class="hidden">
                        ${buildTests(children(id))}
                    </dd>
                </dl>
            </li>
        </ul>
    """.trimIndent()
        }

    private fun TestReport.buildTest() =
        UUID.randomUUID().toString().let { id ->
            """   
<dl class="${if (this.success) "ok" else "ko"}">
    <dt><a href="javascript:void(0)" onclick="javascript:showHide('$id')">${this.name} <span>${this.duration.duration()}</span></a></dt>
    <dd id="$id" class="hidden">
${if (console.isNotBlank()) "<pre class=\"out\">$console</pre>" else ""}
${if (failure.isNotBlank()) "<pre class=\"err\">$failure</pre>" else ""}
    </dd>
</dl>
    """.trimIndent()
        }
}

sealed class ATestReport {
    abstract val id: String
    abstract val name: String
    abstract var success: Boolean
    abstract var duration: Long
    abstract val parent: String?
}

data class ContainerTestReport(
    override val id: String,
    override val name: String,
    override var success: Boolean = false,
    override var duration: Long = 0,
    override val parent: String? = null,
): ATestReport()

data class TestReport(
    override val id: String,
    override val name: String,
    override var success: Boolean = false,
    override var duration: Long = 0,
    var console: String = "",
    var failure: String = "",
    override val parent: String? = null,
): ATestReport()