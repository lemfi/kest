package com.github.lemfi.kest.samplemongodb

import com.github.lemfi.kest.core.cli.assertThat
import com.github.lemfi.kest.core.cli.step
import com.github.lemfi.kest.junit5.runner.playScenario
import com.github.lemfi.kest.mongodb.cli.cleanMongoDatabase
import com.github.lemfi.kest.mongodb.cli.givenCountOfMongoDocuments
import com.github.lemfi.kest.mongodb.cli.givenMongoDocuments
import com.github.lemfi.kest.mongodb.cli.insertMongoDocument
import com.github.lemfi.kest.mongodb.cli.updateMongoDocument
import org.junit.jupiter.api.TestFactory
import org.testcontainers.containers.MongoDBContainer

class SampleMongoStepsTest {

    @TestFactory
    fun `prepare my starwars movies database`() = playScenario {

        val connectionString = step("start mongo container") {
            val container = MongoDBContainer("mongo:4.2.10")
            container.start()
            container.connectionString
        }

        insertMongoDocument("insert A New Hope in DB") {
            connection = connectionString()
            collection = "movies"
            document = """
               
               {
                    "name": "Episode IV – A New Hope",
                    "date" : ISODate("1977-05-25T00:00:00.000Z")
               }
                
                
            """
        }

        insertMongoDocument("insert The Empire Strikes Back in DB") {
            connection = connectionString()
            collection = "movies"
            document = """
               
               {
                    "name": "Episode V – The Empire Strikes Back",
                    "date" : ISODate("1980-05-21T00:00:00.000Z")
               }
                
                
            """
        }

        insertMongoDocument("insert Return of the Jedi in DB") {
            connection = connectionString()
            collection = "movies"
            document = """
               
               {
                    "name": "Episode V – Return of the Jedi",
                    "date" : ISODate("1982-05-25T00:00:00.000Z")
               }
                
                
            """
        }

        givenMongoDocuments("there are three movies in my DB") {
            connection = connectionString()
            collection = "movies"
        } assertThat {
            it.size isEqualTo 3
        }

        givenMongoDocuments("oops I forgot The Phantom Menace ^^") {
            connection = connectionString()
            collection = "movies"
            filter = mapOf("name" to "Episode I – The Phantom Menace")
        } assertThat {
            it.size isEqualTo 0
        }

        givenMongoDocuments("oops I made a type on Episode number of Return of the Jedi") {
            connection = connectionString()
            collection = "movies"
            filter = mapOf("name" to "Episode VI – Return of the Jedi")
        } assertThat {
            it.size isEqualTo 0
        }

        updateMongoDocument("fix Return of the Jedi") {
            connection = connectionString()
            collection = "movies"
            filter = mapOf("name" to "Episode V – Return of the Jedi")
            update = mapOf("name" to "Episode VI – Return of the Jedi")
        }

        givenMongoDocuments("and now it is here") {
            connection = connectionString()
            collection = "movies"
            filter = mapOf("name" to "Episode VI – Return of the Jedi")
        } assertThat {
            it.size isEqualTo 1
        }

        givenCountOfMongoDocuments("counting movies works too") {
            connection = connectionString()
            collection = "movies"
            filter = mapOf("name" to "Episode VI – Return of the Jedi")
        } assertThat {
            it isEqualTo 1L
        }

        cleanMongoDatabase("well let's clean DB, after all everything is already on the internet") {
            connection = connectionString()
        }

        givenCountOfMongoDocuments("ok, my DB is empty") {
            connection = connectionString()
            collection = "movies"
        } assertThat {
            it isEqualTo 0L
        }
    }
}