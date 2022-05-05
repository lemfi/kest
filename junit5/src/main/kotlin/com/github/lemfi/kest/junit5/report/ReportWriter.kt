package com.github.lemfi.kest.junit5.report

import com.github.lemfi.kest.junit5.model.junit5RunnerProperty
import java.io.File

internal interface ReportWriter {

    fun init() {}

    fun writeReport(report: InternalReport)
}

internal class FileReportWriter : ReportWriter {

    private val file = junit5RunnerProperty { report }?.let { File(it) }

    override fun init() {
        file?.delete()
    }

    override fun writeReport(report: InternalReport) {
        file?.apply {
            parentFile.mkdirs()
            createNewFile()
            println("write : " + report.duration)
            appendText(report.toHTML())
        }
    }
}