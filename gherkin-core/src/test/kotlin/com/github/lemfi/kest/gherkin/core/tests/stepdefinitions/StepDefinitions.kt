@file:Suppress("unused")

package com.github.lemfi.kest.gherkin.core.tests.stepdefinitions

import com.github.lemfi.kest.core.builder.ExecutionBuilder
import com.github.lemfi.kest.core.builder.ScenarioBuilder
import com.github.lemfi.kest.core.cli.assertThat
import com.github.lemfi.kest.core.cli.step
import com.github.lemfi.kest.core.model.Execution
import com.github.lemfi.kest.core.model.StepResult
import com.github.lemfi.kest.gherkin.core.And
import com.github.lemfi.kest.gherkin.core.Given
import com.github.lemfi.kest.gherkin.core.Then
import com.github.lemfi.kest.gherkin.core.When


class StepDefinitions {

    @Given("a step that does something where we do not need the result")
    fun ScenarioBuilder.step1() = step { /* do things, or not, whatever... */ }

    @And("another step that does something where we do not need the result")
    fun ScenarioBuilder.step2() = step { /* do things, or not, whatever... */ }

    @When("something happens, but I do not care what")
    fun ScenarioBuilder.step3() = step { /* do things, or not, whatever... */ }

    @Then("I can check another thing, whatever, that scenario is boring!")
    fun ScenarioBuilder.step4() = step { "a boring scenario" } assertThat {
        it isEqualTo "a boring scenario"
    }
}

object OtherStepDefinitions {

    @Given("a static string")
    fun ScenarioBuilder.aStaticString() = step {
        "a static string"
    }

    @And("another static string")
    fun ScenarioBuilder.anotherStaticString(stepResult: StepResult<String>) = step {
        stepResult() to "another static string"
    }

    @When("they are concatenated")
    fun ScenarioBuilder.theyAreConcatenated(stepResult: StepResult<Pair<String, String>>) = step {
        stepResult().run { first + second }
    }

    @Then("the result is")
    fun ScenarioBuilder.theResultIs(stepResult: StepResult<String>) = step { stepResult() } assertThat {
        it isEqualTo "a static stringanother static string"
    }

}

class YetOtherStepDefinitions {

    @Given("string (.*)")
    fun ScenarioBuilder.string(s: String) = step { s }

    @When("it is reversed")
    fun ScenarioBuilder.reverseString(previousStep: StepResult<String>) = step { previousStep().reversed() }

    @Then("it becomes (.*)")
    fun ScenarioBuilder.checkString(previousStep: StepResult<String>, expectedString: String) =
        step { previousStep() } assertThat {
            it isEqualTo expectedString
        }

    @Then("it becomes")
    fun ScenarioBuilder.checkString2(previousStep: StepResult<String>, expectedString: String) =
        step { previousStep() } assertThat {
            it isEqualTo expectedString
        }

    @Given("number (.*)")
    fun ScenarioBuilder.number(number: Int) = step { number }

    @When("it is divided by (.*)")
    fun ScenarioBuilder.divide(previousStep: StepResult<Int>, number: Long) = step { previousStep() / number }

    @And("multiplied by sum of (.*) and (.*)")
    fun ScenarioBuilder.multiplyBySumOf(previousStep: StepResult<Long>, s1: Double?, s2: Float?) =
        step { previousStep() * ((s1 ?: 0.0) + (s2 ?: 0f)) }

    @Then("the result is (.*)")
    fun ScenarioBuilder.operationResult(previousStep: StepResult<Double>, expectedResult: Double) =
        step { previousStep() } assertThat {
            it isEqualTo expectedResult
        }

    @Given("boolean (.*) and boolean (.*)")
    fun ScenarioBuilder.booleans(b1: Boolean, b2: Boolean) = step { b1 to b2 }

    @When("a logical AND is performed")
    fun ScenarioBuilder.and(previousStep: StepResult<Pair<Boolean, Boolean>>) =
        step { previousStep().let { it.first && it.second } }

    @When("a logical OR is performed")
    fun ScenarioBuilder.or(previousStep: StepResult<Pair<Boolean, Boolean>>) =
        step { previousStep().let { it.first || it.second } }

    @Then("the boolean result is (.*)")
    fun ScenarioBuilder.booleanResult(previousStep: StepResult<Boolean>, expectedResult: Boolean) =
        step { previousStep() } assertThat {
            it isEqualTo expectedResult
        }
}

@Given("numbers (.*) and (.*)")
fun prepareSum(
    sumExecutionBuilder: SumExecutionBuilder? = null,
    n1: Long,
    n2: Long
) =
    (sumExecutionBuilder ?: SumExecutionBuilder()).apply {
        values.add(n1)
        values.add(n2)
    }

@Given("nums (.*) and (.*)")
fun MultiplyExecutionBuilder?.prepareMult(
    n1: Long,
    n2: Long
) =
    (this ?: MultiplyExecutionBuilder()).apply {
        values.add(n1)
        values.add(n2)
    }

@When("they are added")
fun ScenarioBuilder.doSum(sumExecutionBuilder: SumExecutionBuilder) =
    createStep { sumExecutionBuilder }


@When("they are multiplied")
fun ScenarioBuilder.doSum(multiplyExecutionBuilder: MultiplyExecutionBuilder) =
    createStep { multiplyExecutionBuilder }

class SumExecutionBuilder : ExecutionBuilder<Long> {
    val values: MutableList<Long> = mutableListOf()

    override fun toExecution(): Execution<Long> {
        return object : Execution<Long>() {
            override fun execute() = values.sum()
        }
    }
}

class MultiplyExecutionBuilder : ExecutionBuilder<Long> {
    val values: MutableList<Long> = mutableListOf()

    override fun toExecution(): Execution<Long> {
        return object : Execution<Long>() {
            override fun execute() = values.reduce { res, n -> res * n  }
        }
    }
}

object WrongDefinitions {

    @Given("a definition with an incompatible receiver")
    fun String.wrongDefinition() = this

    @Given("a definition")
    fun ScenarioBuilder.aDefinition() = step { "a definition" }

    @When("calling a definition with wrong parameter")
    fun ScenarioBuilder.wrongParameter(param: String) = step { param }

    @When("a definition with wrong number of parameter (.*) and (.*)")
    fun ScenarioBuilder.wrongNumberOfParameter(param: String) = step { param }
}