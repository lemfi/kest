package com.github.lemfi.kest.samplecadence

import com.github.lemfi.kest.cadence.cli.`given activity call`
import com.github.lemfi.kest.core.cli.assertThat
import com.github.lemfi.kest.junit5.runner.playScenario
import com.github.lemfi.kest.samplecadence.sampleapi.IMayTheForceBeWithYouActivity
import com.github.lemfi.kest.samplecadence.sampleapi.IRecruitPadawansActivity
import com.github.lemfi.kest.samplecadence.sampleapi.MayTheForceBeWithYou
import com.github.lemfi.kest.samplecadence.sampleapi.Padawan
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
    fun `Darth Vader needs the Force`() = playScenario(name = "Darth Vader needs the Force") {

        `given activity call` {
            domain = "kest"
            tasklist = "SAMPLE_CADENCE"

            activity(IMayTheForceBeWithYouActivity::mayTheForceBeWithYou, MayTheForceBeWithYou("Darth Vader"))
        } assertThat {
            it isEqualTo "May the Force be with you Darth Vader!"
        }

    }

    @TestFactory
    fun `Darth Vader recruits a padawan`() = playScenario(name = "Darth Vader recruits a padawan") {

        `given activity call` {
            domain = "kest"
            tasklist = "SAMPLE_CADENCE"

            activity(IRecruitPadawansActivity::recruitOne, "Darth Vader")
        } assertThat {
            it isEqualTo Padawan("Ahsoka Tano")
        }

    }

    @TestFactory
    fun `Darth Vader recruits all padawans`() = playScenario(name = "Darth Vader recruits a padawan") {

        `given activity call`<List<Padawan>> {
            domain = "kest"
            tasklist = "SAMPLE_CADENCE"

            activity(IRecruitPadawansActivity::recruitThemAll, "Darth Vader")
        } assertThat {
            it isEqualTo listOf(Padawan("Ahsoka Tano"))
        }

    }

}