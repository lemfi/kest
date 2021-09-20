package com.github.lemfi.kest.samplecadence.sampleapi

import com.github.lemfi.kest.cadence.cli.`create domain`
import com.uber.cadence.activity.ActivityOptions
import com.uber.cadence.client.WorkflowClient
import com.uber.cadence.client.WorkflowClientOptions
import com.uber.cadence.serviceclient.ClientOptions
import com.uber.cadence.serviceclient.WorkflowServiceTChannel
import com.uber.cadence.worker.WorkerFactory
import com.uber.cadence.worker.WorkerFactoryOptions
import com.uber.cadence.workflow.Workflow
import com.uber.cadence.workflow.WorkflowMethod
import java.time.Duration
import java.util.concurrent.TimeUnit

var factory: WorkerFactory? = null

fun startActivitiesAndWorkflows() {

    `create domain`("kest")

    if (factory == null) factory =
        WorkerFactory.newInstance(
            WorkflowClient.newInstance(
                WorkflowServiceTChannel(ClientOptions.defaultInstance()),
                WorkflowClientOptions.newBuilder()
                    .setDomain("kest")
                    .build()
            ),
            WorkerFactoryOptions.newBuilder().build()
        )

    factory!!.newWorker("SAMPLE_CADENCE").let { worker ->
        worker.registerActivitiesImplementations(
            HelloActivity(),
            StartConversationActivity(),
            MayTheForceBeWithYouActivity()
        )
        worker.registerWorkflowImplementationTypes(HelloWorldWorkflow::class.java)
    }
    factory!!.start()

}

fun stopActivitiesAndWorkflows() {
    factory?.awaitTermination(200, TimeUnit.MILLISECONDS)
    factory = null
}


data class MayTheForceBeWithYou(val who: String)
interface IMayTheForceBeWithYouActivity {
    fun mayTheForceBeWithYou(who: MayTheForceBeWithYou): String
}

class MayTheForceBeWithYouActivity : IMayTheForceBeWithYouActivity {

    override fun mayTheForceBeWithYou(who: MayTheForceBeWithYou) =
        "May the Force be with you ${who.who}!"
}

data class Hello(val who: String)
interface IHelloWorldWorkflow {

    @WorkflowMethod(name = "HELLO_WORKFLOW")
    fun hello(hello: Hello): String
}

interface IHelloActivity {
    fun hello(who: Hello): String
}

class HelloActivity : IHelloActivity {

    override fun hello(who: Hello): String = "Hello ${who.who}!"
}

interface IStartConversationActivity {
    fun talk(who: Hello): String
}

class StartConversationActivity : IStartConversationActivity {

    override fun talk(who: Hello) = "How are you doing ${who.who}?"
}

class HelloWorldWorkflow : IHelloWorldWorkflow {

    val helloActivity = Workflow.newActivityStub(
        IHelloActivity::class.java, ActivityOptions.Builder()
            .setScheduleToCloseTimeout(Duration.ofMinutes(1))
            .build()
    )
    val talkActivity = Workflow.newActivityStub(
        IStartConversationActivity::class.java, ActivityOptions.Builder()
            .setScheduleToCloseTimeout(Duration.ofMinutes(1))
            .build()
    )

    override fun hello(hello: Hello): String = """
        ${helloActivity.hello(hello)}
        ${talkActivity.talk(hello)}
    """.trimIndent()
}