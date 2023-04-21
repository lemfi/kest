package com.github.lemfi.kest.junit5

import com.github.lemfi.kest.core.cli.scenario
import com.github.lemfi.kest.core.cli.step
import com.github.lemfi.kest.core.model.StandaloneStepResult
import com.github.lemfi.kest.junit5.runner.ScenarioStepsIterator
import com.github.lemfi.kest.junit5.runner.`play scenario`
import com.github.lemfi.kest.junit5.runner.playScenario
import com.github.lemfi.kest.junit5.runner.playScenarios
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DynamicContainer
import org.junit.jupiter.api.DynamicNode
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Test

class KestRunnerTest {

    @Test
    fun `play a scenario - wrapped`() {
        lateinit var res1: StandaloneStepResult<String>
        lateinit var res2: StandaloneStepResult<String>

        val scenario = scenario("my scenario") {
            res1 = step(name = "step 1") { "res" }
            res2 = step(name = "step 2") { "${res1()}!" }
        }

        val testNode = playScenario(scenario = scenario, unwrap = false) as DynamicContainer

        Assertions.assertEquals("my scenario", testNode.displayName)

        lateinit var step1: DynamicNode
        lateinit var step2: DynamicNode
        with(testNode.children.iterator()) {
            Assertions.assertTrue(hasNext())
            step1 = next()
            Assertions.assertTrue(hasNext())
            step2 = next()
            Assertions.assertFalse(hasNext())
        }

        Assertions.assertEquals("step 1", step1.displayName)
        Assertions.assertEquals("step 2", step2.displayName)

        Assertions.assertTrue(step1 is DynamicTest)
        (step1 as DynamicTest).executable.execute()

        Assertions.assertTrue(step2 is DynamicTest)
        (step2 as DynamicTest).executable.execute()


        Assertions.assertEquals("res", res1())
        Assertions.assertEquals("res!", res2())
    }

    @Test
    fun `play a scenario - unwrapped`() {
        lateinit var res1: StandaloneStepResult<String>
        lateinit var res2: StandaloneStepResult<String>

        val scenario = scenario("my scenario") {
            res1 = step(name = "step 1") { "res" }
            res2 = step(name = "step 2") { "${res1()}!" }
        }

        val testNode = playScenario(scenario = scenario, unwrap = true) as ScenarioStepsIterator

        lateinit var step1: DynamicNode
        lateinit var step2: DynamicNode
        with(testNode.iterator()) {
            Assertions.assertTrue(hasNext())
            step1 = next()
            Assertions.assertTrue(hasNext())
            step2 = next()
            Assertions.assertFalse(hasNext())
        }

        Assertions.assertEquals("step 1", step1.displayName)
        Assertions.assertEquals("step 2", step2.displayName)

        Assertions.assertTrue(step1 is DynamicTest)
        (step1 as DynamicTest).executable.execute()

        Assertions.assertTrue(step2 is DynamicTest)
        (step2 as DynamicTest).executable.execute()


        Assertions.assertEquals("res", res1())
        Assertions.assertEquals("res!", res2())
    }

    @Test
    fun `play a scenario - builder - wrapped`() {
        lateinit var res1: StandaloneStepResult<String>
        lateinit var res2: StandaloneStepResult<String>

        val testNode = playScenario("my scenario", false) {
            res1 = step(name = "step 1") { "res" }
            res2 = step(name = "step 2") { "${res1()}!" }
        } as DynamicContainer

        Assertions.assertEquals("my scenario", testNode.displayName)

        lateinit var step1: DynamicNode
        lateinit var step2: DynamicNode
        with(testNode.children.iterator()) {
            Assertions.assertTrue(hasNext())
            step1 = next()
            Assertions.assertTrue(hasNext())
            step2 = next()
            Assertions.assertFalse(hasNext())
        }

        Assertions.assertEquals("step 1", step1.displayName)
        Assertions.assertEquals("step 2", step2.displayName)

        Assertions.assertTrue(step1 is DynamicTest)
        (step1 as DynamicTest).executable.execute()

        Assertions.assertTrue(step2 is DynamicTest)
        (step2 as DynamicTest).executable.execute()


        Assertions.assertEquals("res", res1())
        Assertions.assertEquals("res!", res2())
    }

    @Test
    fun `play a scenario - builder - unwrapped`() {
        lateinit var res1: StandaloneStepResult<String>
        lateinit var res2: StandaloneStepResult<String>

        val testNode = playScenario("my scenario", true) {
            res1 = step(name = "step 1") { "res" }
            res2 = step(name = "step 2") { "${res1()}!" }
        } as ScenarioStepsIterator

        lateinit var step1: DynamicNode
        lateinit var step2: DynamicNode
        with(testNode.iterator()) {
            Assertions.assertTrue(hasNext())
            step1 = next()
            Assertions.assertTrue(hasNext())
            step2 = next()
            Assertions.assertFalse(hasNext())
        }

        Assertions.assertEquals("step 1", step1.displayName)
        Assertions.assertEquals("step 2", step2.displayName)

        Assertions.assertTrue(step1 is DynamicTest)
        (step1 as DynamicTest).executable.execute()

        Assertions.assertTrue(step2 is DynamicTest)
        (step2 as DynamicTest).executable.execute()

        Assertions.assertEquals("res", res1())
        Assertions.assertEquals("res!", res2())
    }

    @Test
    fun `play multiple scenarios`() {
        lateinit var res1: StandaloneStepResult<String>
        lateinit var res2: StandaloneStepResult<String>
        lateinit var res3: StandaloneStepResult<String>
        lateinit var res4: StandaloneStepResult<String>

        val scenario1 = scenario("my scenario 1") {
            res1 = step(name = "step 1") { "res1" }
            res2 = step(name = "step 2") { "${res1()}!" }
        }
        val scenario2 = scenario("my scenario 2") {
            res3 = step(name = "step 3") { "res2" }
            res4 = step(name = "step 4") { "${res3()}!" }
        }

        val testNodes = playScenarios(scenario1, scenario2)

        Assertions.assertEquals(2, testNodes.size)
        val testScenario1 = testNodes.first()
        val testScenario2 = testNodes.last()

        Assertions.assertEquals("my scenario 1", testScenario1.displayName)
        Assertions.assertEquals("my scenario 2", testScenario2.displayName)

        Assertions.assertTrue(testScenario1 is DynamicContainer)
        Assertions.assertTrue(testScenario2 is DynamicContainer)

        lateinit var step1: DynamicNode
        lateinit var step2: DynamicNode
        with((testScenario1 as DynamicContainer).children.iterator()) {
            Assertions.assertTrue(hasNext())
            step1 = next()
            Assertions.assertTrue(hasNext())
            step2 = next()
            Assertions.assertFalse(hasNext())
        }

        Assertions.assertEquals("step 1", step1.displayName)
        Assertions.assertEquals("step 2", step2.displayName)

        Assertions.assertTrue(step1 is DynamicTest)
        (step1 as DynamicTest).executable.execute()

        Assertions.assertTrue(step2 is DynamicTest)
        (step2 as DynamicTest).executable.execute()

        Assertions.assertEquals("res1", res1())
        Assertions.assertEquals("res1!", res2())


        lateinit var step3: DynamicNode
        lateinit var step4: DynamicNode
        with((testScenario2 as DynamicContainer).children.iterator()) {
            Assertions.assertTrue(hasNext())
            step3 = next()
            Assertions.assertTrue(hasNext())
            step4 = next()
            Assertions.assertFalse(hasNext())
        }

        Assertions.assertEquals("step 3", step3.displayName)
        Assertions.assertEquals("step 4", step4.displayName)

        Assertions.assertTrue(step3 is DynamicTest)
        (step3 as DynamicTest).executable.execute()

        Assertions.assertTrue(step4 is DynamicTest)
        (step4 as DynamicTest).executable.execute()

        Assertions.assertEquals("res2", res3())
        Assertions.assertEquals("res2!", res4())
    }

    @Test
    fun `play multiple scenarios - beforeEach`() {
        var number = 0
        lateinit var beforeRes1: StandaloneStepResult<Int>
        lateinit var beforeRes2: StandaloneStepResult<Int>
        lateinit var res1: StandaloneStepResult<Int>
        lateinit var res2: StandaloneStepResult<Int>
        lateinit var res3: StandaloneStepResult<Int>
        lateinit var res4: StandaloneStepResult<Int>

        val beforeEach = scenario("before each") {
            beforeRes1 = step(name = "before step 1") { ++number }
            beforeRes2 = step(name = "before step 2") { ++number }
        }
        val scenario1 = scenario("my scenario 1") {
            res1 = step(name = "step 1") { ++number }
            res2 = step(name = "step 2") { ++number }
        }
        val scenario2 = scenario("my scenario 2") {
            res3 = step(name = "step 3") { ++number }
            res4 = step(name = "step 4") { ++number }
        }

        val testNodes = playScenarios(scenario1, scenario2, beforeEach = { beforeEach })

        Assertions.assertEquals(2, testNodes.size)
        val testScenario1 = testNodes.first()
        val testScenario2 = testNodes.last()

        lateinit var beforeStep1: DynamicNode
        lateinit var step1: DynamicNode
        lateinit var step2: DynamicNode
        with((testScenario1 as DynamicContainer).children.iterator()) {
            Assertions.assertTrue(hasNext())
            beforeStep1 = next()
            Assertions.assertTrue(hasNext())
            step1 = next()
            Assertions.assertTrue(hasNext())
            step2 = next()
            Assertions.assertFalse(hasNext())
        }

        Assertions.assertEquals("before each", beforeStep1.displayName)
        Assertions.assertEquals("step 1", step1.displayName)
        Assertions.assertEquals("step 2", step2.displayName)

        Assertions.assertTrue(beforeStep1 is DynamicContainer)

        lateinit var beforeStepStep1: DynamicNode
        lateinit var beforeStepStep2: DynamicNode
        with((beforeStep1 as DynamicContainer).children.iterator()) {
            Assertions.assertTrue(hasNext())
            beforeStepStep1 = next()
            Assertions.assertTrue(hasNext())
            beforeStepStep2 = next()
            Assertions.assertFalse(hasNext())
        }

        Assertions.assertEquals("before step 1", beforeStepStep1.displayName)
        Assertions.assertEquals("before step 2", beforeStepStep2.displayName)

        Assertions.assertTrue(beforeStepStep1 is DynamicTest)
        (beforeStepStep1 as DynamicTest).executable.execute()

        Assertions.assertTrue(beforeStepStep2 is DynamicTest)
        (beforeStepStep2 as DynamicTest).executable.execute()

        Assertions.assertEquals(1, beforeRes1())
        Assertions.assertEquals(2, beforeRes2())

        Assertions.assertTrue(step1 is DynamicTest)
        (step1 as DynamicTest).executable.execute()

        Assertions.assertTrue(step2 is DynamicTest)
        (step2 as DynamicTest).executable.execute()

        Assertions.assertEquals(3, res1())
        Assertions.assertEquals(4, res2())


        lateinit var beforeStep2: DynamicNode
        lateinit var step3: DynamicNode
        lateinit var step4: DynamicNode
        with((testScenario2 as DynamicContainer).children.iterator()) {
            Assertions.assertTrue(hasNext())
            beforeStep2 = next()
            Assertions.assertTrue(hasNext())
            step3 = next()
            Assertions.assertTrue(hasNext())
            step4 = next()
            Assertions.assertFalse(hasNext())
        }

        Assertions.assertEquals("before each", beforeStep2.displayName)
        Assertions.assertEquals("step 3", step3.displayName)
        Assertions.assertEquals("step 4", step4.displayName)

        Assertions.assertTrue(beforeStep2 is DynamicContainer)

        lateinit var beforeStepStep3: DynamicNode
        lateinit var beforeStepStep4: DynamicNode
        with((beforeStep2 as DynamicContainer).children.iterator()) {
            Assertions.assertTrue(hasNext())
            beforeStepStep3 = next()
            Assertions.assertTrue(hasNext())
            beforeStepStep4 = next()
            Assertions.assertFalse(hasNext())
        }

        Assertions.assertEquals("before step 1", beforeStepStep3.displayName)
        Assertions.assertEquals("before step 2", beforeStepStep4.displayName)

        Assertions.assertTrue(beforeStepStep3 is DynamicTest)
        (beforeStepStep3 as DynamicTest).executable.execute()

        Assertions.assertTrue(beforeStepStep4 is DynamicTest)
        (beforeStepStep4 as DynamicTest).executable.execute()

        Assertions.assertEquals(5, beforeRes1())
        Assertions.assertEquals(6, beforeRes2())

        Assertions.assertTrue(step3 is DynamicTest)
        (step3 as DynamicTest).executable.execute()

        Assertions.assertTrue(step4 is DynamicTest)
        (step4 as DynamicTest).executable.execute()

        Assertions.assertEquals(7, res3())
        Assertions.assertEquals(8, res4())
    }

    @Test
    fun `play multiple scenarios - beforeAll`() {
        var number = 0
        lateinit var beforeRes1: StandaloneStepResult<Int>
        lateinit var beforeRes2: StandaloneStepResult<Int>
        lateinit var res1: StandaloneStepResult<Int>
        lateinit var res2: StandaloneStepResult<Int>
        lateinit var res3: StandaloneStepResult<Int>
        lateinit var res4: StandaloneStepResult<Int>

        val beforeAll = scenario("before all") {
            beforeRes1 = step(name = "before step 1") { ++number }
            beforeRes2 = step(name = "before step 2") { ++number }
        }
        val scenario1 = scenario("my scenario 1") {
            res1 = step(name = "step 1") { ++number }
            res2 = step(name = "step 2") { ++number }
        }
        val scenario2 = scenario("my scenario 2") {
            res3 = step(name = "step 3") { ++number }
            res4 = step(name = "step 4") { ++number }
        }

        val testNodes = playScenarios(scenario1, scenario2, beforeAll = { beforeAll })

        Assertions.assertEquals(3, testNodes.size)
        val beforeAllSscenario = testNodes.first()
        val testScenario1 = testNodes[1]
        val testScenario2 = testNodes.last()

        lateinit var step1: DynamicNode
        lateinit var step2: DynamicNode
        with((testScenario1 as DynamicContainer).children.iterator()) {
            Assertions.assertTrue(hasNext())
            step1 = next()
            Assertions.assertTrue(hasNext())
            step2 = next()
            Assertions.assertFalse(hasNext())
        }

        Assertions.assertTrue(beforeAllSscenario is DynamicContainer)
        Assertions.assertEquals("before all", beforeAllSscenario.displayName)
        Assertions.assertEquals("step 1", step1.displayName)
        Assertions.assertEquals("step 2", step2.displayName)

        lateinit var beforeStepStep1: DynamicNode
        lateinit var beforeStepStep2: DynamicNode
        with((beforeAllSscenario as DynamicContainer).children.iterator()) {
            Assertions.assertTrue(hasNext())
            beforeStepStep1 = next()
            Assertions.assertTrue(hasNext())
            beforeStepStep2 = next()
            Assertions.assertFalse(hasNext())
        }

        Assertions.assertEquals("before step 1", beforeStepStep1.displayName)
        Assertions.assertEquals("before step 2", beforeStepStep2.displayName)

        Assertions.assertTrue(beforeStepStep1 is DynamicTest)
        (beforeStepStep1 as DynamicTest).executable.execute()

        Assertions.assertTrue(beforeStepStep2 is DynamicTest)
        (beforeStepStep2 as DynamicTest).executable.execute()

        Assertions.assertEquals(1, beforeRes1())
        Assertions.assertEquals(2, beforeRes2())

        Assertions.assertTrue(step1 is DynamicTest)
        (step1 as DynamicTest).executable.execute()

        Assertions.assertTrue(step2 is DynamicTest)
        (step2 as DynamicTest).executable.execute()

        Assertions.assertEquals(3, res1())
        Assertions.assertEquals(4, res2())

        lateinit var step3: DynamicNode
        lateinit var step4: DynamicNode
        with((testScenario2 as DynamicContainer).children.iterator()) {
            Assertions.assertTrue(hasNext())
            step3 = next()
            Assertions.assertTrue(hasNext())
            step4 = next()
            Assertions.assertFalse(hasNext())
        }

        Assertions.assertEquals("step 3", step3.displayName)
        Assertions.assertEquals("step 4", step4.displayName)

        Assertions.assertTrue(step3 is DynamicTest)
        (step3 as DynamicTest).executable.execute()

        Assertions.assertTrue(step4 is DynamicTest)
        (step4 as DynamicTest).executable.execute()

        Assertions.assertEquals(5, res3())
        Assertions.assertEquals(6, res4())
    }

    @Test
    fun `play multiple scenarios - afterEach`() {
        var number = 0
        lateinit var afterRes1: StandaloneStepResult<Int>
        lateinit var afterRes2: StandaloneStepResult<Int>
        lateinit var res1: StandaloneStepResult<Int>
        lateinit var res2: StandaloneStepResult<Int>
        lateinit var res3: StandaloneStepResult<Int>
        lateinit var res4: StandaloneStepResult<Int>

        val afterEach = scenario("after each") {
            afterRes1 = step(name = "after step 1") { ++number }
            afterRes2 = step(name = "after step 2") { ++number }
        }
        val scenario1 = scenario("my scenario 1") {
            res1 = step(name = "step 1") { ++number }
            res2 = step(name = "step 2") { ++number }
        }
        val scenario2 = scenario("my scenario 2") {
            res3 = step(name = "step 3") { ++number }
            res4 = step(name = "step 4") { ++number }
        }

        val testNodes = playScenarios(scenario1, scenario2, afterEach = { afterEach })

        Assertions.assertEquals(2, testNodes.size)
        val testScenario1 = testNodes.first()
        val testScenario2 = testNodes.last()

        lateinit var afterStep1: DynamicNode
        lateinit var step1: DynamicNode
        lateinit var step2: DynamicNode
        with((testScenario1 as DynamicContainer).children.iterator()) {
            Assertions.assertTrue(hasNext())
            step1 = next()
            Assertions.assertTrue(hasNext())
            step2 = next()
            Assertions.assertTrue(hasNext())
            afterStep1 = next()
            Assertions.assertFalse(hasNext())
        }

        Assertions.assertEquals("after each", afterStep1.displayName)
        Assertions.assertEquals("step 1", step1.displayName)
        Assertions.assertEquals("step 2", step2.displayName)

        Assertions.assertTrue(afterStep1 is DynamicContainer)

        lateinit var afterStepStep1: DynamicNode
        lateinit var afterStepStep2: DynamicNode
        with((afterStep1 as DynamicContainer).children.iterator()) {
            Assertions.assertTrue(hasNext())
            afterStepStep1 = next()
            Assertions.assertTrue(hasNext())
            afterStepStep2 = next()
            Assertions.assertFalse(hasNext())
        }

        Assertions.assertEquals("after step 1", afterStepStep1.displayName)
        Assertions.assertEquals("after step 2", afterStepStep2.displayName)

        Assertions.assertTrue(step1 is DynamicTest)
        (step1 as DynamicTest).executable.execute()

        Assertions.assertTrue(step2 is DynamicTest)
        (step2 as DynamicTest).executable.execute()

        Assertions.assertEquals(1, res1())
        Assertions.assertEquals(2, res2())

        Assertions.assertTrue(afterStepStep1 is DynamicTest)
        (afterStepStep1 as DynamicTest).executable.execute()

        Assertions.assertTrue(afterStepStep2 is DynamicTest)
        (afterStepStep2 as DynamicTest).executable.execute()

        Assertions.assertEquals(3, afterRes1())
        Assertions.assertEquals(4, afterRes2())


        lateinit var afterStep2: DynamicNode
        lateinit var step3: DynamicNode
        lateinit var step4: DynamicNode
        with((testScenario2 as DynamicContainer).children.iterator()) {
            Assertions.assertTrue(hasNext())
            step3 = next()
            Assertions.assertTrue(hasNext())
            step4 = next()
            Assertions.assertTrue(hasNext())
            afterStep2 = next()
            Assertions.assertFalse(hasNext())
        }

        Assertions.assertEquals("after each", afterStep2.displayName)
        Assertions.assertEquals("step 3", step3.displayName)
        Assertions.assertEquals("step 4", step4.displayName)

        Assertions.assertTrue(afterStep2 is DynamicContainer)

        lateinit var afterStepStep3: DynamicNode
        lateinit var afterStepStep4: DynamicNode
        with((afterStep2 as DynamicContainer).children.iterator()) {
            Assertions.assertTrue(hasNext())
            afterStepStep3 = next()
            Assertions.assertTrue(hasNext())
            afterStepStep4 = next()
            Assertions.assertFalse(hasNext())
        }

        Assertions.assertEquals("after step 1", afterStepStep3.displayName)
        Assertions.assertEquals("after step 2", afterStepStep4.displayName)

        Assertions.assertTrue(step3 is DynamicTest)
        (step3 as DynamicTest).executable.execute()

        Assertions.assertTrue(step4 is DynamicTest)
        (step4 as DynamicTest).executable.execute()

        Assertions.assertEquals(5, res3())
        Assertions.assertEquals(6, res4())

        Assertions.assertTrue(afterStepStep3 is DynamicTest)
        (afterStepStep3 as DynamicTest).executable.execute()

        Assertions.assertTrue(afterStepStep4 is DynamicTest)
        (afterStepStep4 as DynamicTest).executable.execute()

        Assertions.assertEquals(7, afterRes1())
        Assertions.assertEquals(8, afterRes2())
    }

    @Test
    fun `play multiple scenarios - afterAll`() {
        var number = 0
        lateinit var afterRes1: StandaloneStepResult<Int>
        lateinit var afterRes2: StandaloneStepResult<Int>
        lateinit var res1: StandaloneStepResult<Int>
        lateinit var res2: StandaloneStepResult<Int>
        lateinit var res3: StandaloneStepResult<Int>
        lateinit var res4: StandaloneStepResult<Int>

        val afterAll = scenario("after all") {
            afterRes1 = step(name = "after step 1") { ++number }
            afterRes2 = step(name = "after step 2") { ++number }
        }
        val scenario1 = scenario("my scenario 1") {
            res1 = step(name = "step 1") { ++number }
            res2 = step(name = "step 2") { ++number }
        }
        val scenario2 = scenario("my scenario 2") {
            res3 = step(name = "step 3") { ++number }
            res4 = step(name = "step 4") { ++number }
        }

        val testNodes = playScenarios(scenario1, scenario2, afterAll = { afterAll })

        Assertions.assertEquals(3, testNodes.size)
        val testScenario1 = testNodes.first()
        val testScenario2 = testNodes[1]
        val afterAllScenario = testNodes.last()

        lateinit var step1: DynamicNode
        lateinit var step2: DynamicNode
        with((testScenario1 as DynamicContainer).children.iterator()) {
            Assertions.assertTrue(hasNext())
            step1 = next()
            Assertions.assertTrue(hasNext())
            step2 = next()
            Assertions.assertFalse(hasNext())
        }

        Assertions.assertEquals("step 1", step1.displayName)
        Assertions.assertEquals("step 2", step2.displayName)

        Assertions.assertTrue(step1 is DynamicTest)
        (step1 as DynamicTest).executable.execute()

        Assertions.assertTrue(step2 is DynamicTest)
        (step2 as DynamicTest).executable.execute()

        Assertions.assertEquals(1, res1())
        Assertions.assertEquals(2, res2())

        lateinit var step3: DynamicNode
        lateinit var step4: DynamicNode
        with((testScenario2 as DynamicContainer).children.iterator()) {
            Assertions.assertTrue(hasNext())
            step3 = next()
            Assertions.assertTrue(hasNext())
            step4 = next()
            Assertions.assertFalse(hasNext())
        }

        Assertions.assertEquals("step 3", step3.displayName)
        Assertions.assertEquals("step 4", step4.displayName)

        Assertions.assertTrue(afterAllScenario is DynamicContainer)
        Assertions.assertEquals("after all", afterAllScenario.displayName)

        lateinit var afterStepStep1: DynamicNode
        lateinit var afterStepStep2: DynamicNode
        with((afterAllScenario as DynamicContainer).children.iterator()) {
            Assertions.assertTrue(hasNext())
            afterStepStep1 = next()
            Assertions.assertTrue(hasNext())
            afterStepStep2 = next()
            Assertions.assertFalse(hasNext())
        }

        Assertions.assertEquals("after step 1", afterStepStep1.displayName)
        Assertions.assertEquals("after step 2", afterStepStep2.displayName)

        Assertions.assertTrue(step3 is DynamicTest)
        (step3 as DynamicTest).executable.execute()

        Assertions.assertTrue(step4 is DynamicTest)
        (step4 as DynamicTest).executable.execute()

        Assertions.assertEquals(3, res3())
        Assertions.assertEquals(4, res4())

        Assertions.assertTrue(afterStepStep1 is DynamicTest)
        (afterStepStep1 as DynamicTest).executable.execute()

        Assertions.assertTrue(afterStepStep2 is DynamicTest)
        (afterStepStep2 as DynamicTest).executable.execute()

        Assertions.assertEquals(5, afterRes1())
        Assertions.assertEquals(6, afterRes2())
    }
}