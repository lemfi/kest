package com.github.lemfi.kest.junit5.runner

import com.github.lemfi.kest.core.cli.run
import com.github.lemfi.kest.core.model.Scenario
import com.github.lemfi.kest.core.properties.autoconfigure
import org.junit.jupiter.api.DynamicTest

fun Scenario.toDynamicTest(beforeEach: ()->Unit = {}, afterEach: ()->Unit = {}): DynamicTest {
    return DynamicTest.dynamicTest(name) {
        beforeEach()
        try {
            run()
        } finally {
            afterEach()
        }
    }
}

fun `run scenarios`(vararg scenario: Scenario, beforeEach: ()->Unit = {}, afterEach: ()->Unit = {}): List<DynamicTest> {

    autoconfigure()

    return mutableListOf<DynamicTest>().apply {
        addAll(scenario.map { it.toDynamicTest(beforeEach, afterEach) })
    }
}