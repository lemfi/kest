package com.github.lemfi.kest.samplecadence

import com.github.lemfi.kest.cadence.cli.`given activity call`
import com.github.lemfi.kest.core.cli.`assert that`
import com.github.lemfi.kest.core.cli.eq
import com.github.lemfi.kest.junit5.runner.`play scenario`
import com.github.lemfi.kest.samplecadence.sampleapi.IMayTheForceBeWithYouActivity
import com.github.lemfi.kest.samplecadence.sampleapi.MayTheForceBeWithYou
import com.github.lemfi.kest.samplecadence.sampleapi.startActivitiesAndWorkflows
import com.github.lemfi.kest.samplecadence.sampleapi.stopActivitiesAndWorkflows
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestFactory

class TestActivity {

    @BeforeEach
    fun beforeEach() = startActivitiesAndWorkflows()

    @AfterEach
    fun afterEach() = stopActivitiesAndWorkflows()

    @TestFactory
    fun `Darth Vader needs the Force`() = `play scenario` {

        name { "Darth Vader needs the Force" }

        `given activity call`<String> {
            domain = "kest"
            tasklist = "SAMPLE_CADENCE"

            activity(IMayTheForceBeWithYouActivity::mayTheForceBeWithYou, MayTheForceBeWithYou("Darth Vader"))
        } `assert that` {
            eq("May the Force be with you Darth Vader!", it)
        }

    }

}