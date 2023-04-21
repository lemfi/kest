package com.github.lemfi.kest.samplecadence

import com.github.lemfi.kest.cadence.cli.`given workflow`
import com.github.lemfi.kest.core.cli.assertThat
import com.github.lemfi.kest.junit5.runner.`play scenario`
import com.github.lemfi.kest.samplecadence.sampleapi.Hello
import com.github.lemfi.kest.samplecadence.sampleapi.IHelloWorldWorkflow
import com.github.lemfi.kest.samplecadence.sampleapi.startActivitiesAndWorkflows
import com.github.lemfi.kest.samplecadence.sampleapi.stopActivitiesAndWorkflows
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestFactory

class TestWorkflow {

    @BeforeEach
    fun beforeEach() = startActivitiesAndWorkflows()

    @AfterEach
    fun afterEach() = stopActivitiesAndWorkflows()

    @TestFactory
    fun `Darth Vader says hello!`() = `play scenario`(name = "Darth Vader says hello!") {

        `given workflow`("Say Hello Workflow") {

            domain = "kest"
            tasklist = "SAMPLE_CADENCE"

            workflow(IHelloWorldWorkflow::hello, Hello("Darth Vader"))

        } assertThat {

            it isEqualTo """
                            Hello Darth Vader!
                            How are you doing Darth Vader?
                        """.trimIndent()
        }
    }

}