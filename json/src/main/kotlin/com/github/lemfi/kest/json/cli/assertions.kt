package com.github.lemfi.kest.json.cli

import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.lemfi.kest.core.builder.AssertionsBuilder
import com.github.lemfi.kest.core.cli.eq
import com.github.lemfi.kest.json.model.JsonArray
import com.github.lemfi.kest.json.model.JsonMap
import org.opentest4j.AssertionFailedError
import kotlin.reflect.KClass

private val mapper = jacksonObjectMapper().apply {
    disable(MapperFeature.ALLOW_COERCION_OF_SCALARS)
}

private data class StringPatternJsonMatcher(
        override val matcher: String,
        override val isList: Pair<Boolean, Boolean>,
        override val isNullable: Boolean,
        override val pattern: String,
        val clsDescriptor: List<String>
): JsonMatcher()

private data class ClassPatternJsonMatcher(
        override val matcher: String,
        override val isList: Pair<Boolean, Boolean>,
        override val isNullable: Boolean,
        override val pattern: String,
        val cls: KClass<*>
): JsonMatcher()

sealed class JsonMatcher {

    abstract val matcher: String
    abstract val isList: Pair<Boolean, Boolean>
    abstract val isNullable: Boolean
    abstract val pattern: String

    companion object {

        private val matchers = mutableMapOf<String, Pair<KClass<*>?, List<String>?>>(
                "{{string}}" to Pair(String::class, null),
                "{{number}}" to Pair(Number::class, null),
                "{{boolean}}" to Pair(Boolean::class, null)
        )

        /**
         * Add a matcher
         *
         * @param key the key for your matcher, for example {{my_matcher}}
         * @param value KClass representing of your matcher, jackson annotations can be used on that Class, for polyphormism for example
         */
        fun addMatcher(key: String, value: KClass<*>) {
            matchers.put(key, value to null)
        }

        /**
         * Add a matcher
         *
         * @param key the key for your matcher, for example {{my_matcher}}
         * @param value string description for matcher, for example:
         *          {
         *              "mykey": "{{string}},
         *              "myotherkey": "{{number}}
         *          }
         */
        fun addMatcher(key: String, pattern: String) {
            matchers.put(key, null to listOf(pattern))
        }

        /**
         * Add a matcher
         *
         * @param key the key for your matcher, for example {{my_matcher}}
         * @param patterns a list of possible string description for matcher, to use for polymorphism
         */
        fun addMatcher(key: String, patterns: List<String>) {
            matchers.put(key, null to patterns)
        }

        fun getMatcher(key: String): JsonMatcher? {
            return key.trim().replace(" ", "").let { keyWithoutSpaces ->
                val list = keyWithoutSpaces.startsWith("[[") && keyWithoutSpaces.endsWith("]]") || keyWithoutSpaces.endsWith("]]?")
                val listNullable = list && keyWithoutSpaces.endsWith("?")

                val keyWithoutList = keyWithoutSpaces.removePrefix("[[").removeSuffix("]]")

                val type = keyWithoutList.removePrefix("{{").substringBefore("?").substringBefore("|").substringBefore("}}")
                val nullable = keyWithoutList.substringAfter(type).substringBefore("|").substringBefore("}}").equals("?")
                val pattern = keyWithoutList.substringAfter(type).let { if (nullable) keyWithoutList.substringAfter("?") else it }.substringAfter("|").substringBefore("}}")

                matchers["{{$type}}"]?.toJsonMatcher(type, list to listNullable, nullable, pattern)

            }
        }

        private fun Pair<KClass<*>?, List<String>?>.toJsonMatcher(type: String, list: Pair<Boolean, Boolean>, nullable: Boolean, pattern: String): JsonMatcher {
            return if (first != null) ClassPatternJsonMatcher(type, list, nullable, pattern, first!!) else StringPatternJsonMatcher(type, list, nullable, pattern, second!!)
        }
    }
}

/**
 * Check whether a JsonMap matches a pattern
 *
 * @param expected expected pattern
 * @param observed JsonMap object
 */
fun AssertionsBuilder.jsonMatchesObject(expected: String, observed: JsonMap?) {
    if (JsonMatcher.getMatcher(expected) != null) {
        jsonMatches(JsonMatcher.getMatcher(expected)!!, observed)
    } else {
        if (observed == null && !expected.endsWith("?")) throw AssertionFailedError("expected matching $expected, got null", expected, observed)

        observed?.apply {
            jsonMatchesObject(expected.toJsonMap(), observed)
        }
    }
}

/**
 * Check whether a JsonMap matches one of provided patterns
 * To use for polyphormism
 *
 * @param expected expected patterns
 * @param observed JsonMap object
 */
fun AssertionsBuilder.jsonMatchesObject(expected: List<String>, observed: JsonMap?) {

    try {
        jsonMatchesObject(expected.first(), observed)
    } catch (e: Throwable) {
        if (expected.size > 1) {
            jsonMatchesObject(expected.subList(1, expected.size), observed)
        } else {
            throw e
        }
    }
}

/**
 * Check whether all elements of a JsonArray matches a pattern
 *
 * @param expected expected pattern
 * @param observed JsonArray object
 */
fun AssertionsBuilder.jsonMatchesArray(expected: String, observed: JsonArray?) {

    jsonMatchesArray(listOf(expected), observed)
}

/**
 * Check whether all elements of a JsonArray matches one of provided patterns
 *  To use for polyphormism
 *
 * @param expected expected patterns
 * @param observed JsonArray object
 */
fun AssertionsBuilder.jsonMatchesArray(expected: List<String>, observed: JsonArray?) {

    if (observed == null && !expected.first().endsWith("?")) throw AssertionFailedError("expected matching $expected, got null", expected, observed)

    observed?.forEach {
        jsonMatchesObject(expected, it)
    }
}

/**
 * Check whether a Json as String matches pattern
 *
 * @param expected expected pattern
 * @param observed Json as String
 */
fun AssertionsBuilder.jsonMatchesObject(expected: String, observed: String?) {
    return jsonMatchesObject(listOf(expected), observed.toJsonMap())
}

/**
 * Check whether a Json as String matches one of provided patterns
 * To use for polyphormism
 *
 * @param expected expected patterns
 * @param observed Json as String
 */
fun AssertionsBuilder.jsonMatchesObject(expected: List<String>, observed: String?) {
    return jsonMatchesObject(expected, observed.toJsonMap())
}

/**
 * Check whether all elements of a Json Array as String matches a pattern
 *
 * @param expected expected pattern
 * @param observed Json Array as String object
 */
fun AssertionsBuilder.jsonMatchesArray(expected: String, observed: String?) {
    return jsonMatchesArray(listOf(expected), observed.toJsonArray())
}

/**
 * Check whether all elements of a Json Array as String matches one of provided patterns
 *
 * @param expected expected patterns
 * @param observed Json Array as String object
 */
fun AssertionsBuilder.jsonMatchesArray(expected: List<String>, observed: String?) {
    return jsonMatchesArray(expected, observed.toJsonArray())
}

private fun AssertionsBuilder.jsonMatchesObject(expected: JsonMap, observed: JsonMap) {

    if (expected.keys != observed.keys) throw AssertionFailedError("expected ${expected.keys} entries, got ${observed.keys} entries", expected.keys, observed.keys)
    expected.keys.forEach {
        val expectedValue = expected[it]
        when {
            isMap(expectedValue) -> jsonMatchesObject(mapper.writeValueAsString(expected[it]), mapper.writeValueAsString(observed[it]))
            isNumber(expectedValue) || isBoolean(expected[it]) -> eq(expected[it], observed[it])
            isString(expectedValue) -> {
                val observedValue = observed[it]?.let { mapper.writeValueAsString(it) }
                JsonMatcher.getMatcher(expectedValue as String)?.let { matcher ->
                    jsonMatches(matcher, observedValue)

                } ?: eq(expectedValue, observed[it])
            }
        }
    }
}

private fun isString(data: Any?) = data?.let { String::class.java.isAssignableFrom(it.javaClass) } ?: false
private fun isNumber(data: Any?) = data?.let { Number::class.java.isAssignableFrom(it.javaClass) } ?: false
private fun isMap(data: Any?) = data?.let { Map::class.java.isAssignableFrom(it.javaClass) } ?: false
private fun isBoolean(data: Any?) = data?.let { Boolean::class.java.isAssignableFrom(it.javaClass) } ?: false

private fun AssertionsBuilder.jsonMatches(matcher: JsonMatcher, observed: JsonMap?) {
    jsonMatches(matcher, mapper.writeValueAsString(observed))
}

private fun AssertionsBuilder.jsonMatches(matcher: JsonMatcher, observed: String?) {
    when (matcher) {
        is ClassPatternJsonMatcher -> jsonMatches(matcher, observed)
        is StringPatternJsonMatcher -> jsonMatches(matcher, observed)
    }
}


private fun AssertionsBuilder.jsonMatches(matcher: StringPatternJsonMatcher, observed: String?) {
    if (matcher.isList.first) {
        if (!(matcher.isList.second && observed == null)) jsonMatchesArray(matcher.clsDescriptor + if (matcher.isNullable) "?" else "", observed)
    } else {
        jsonMatchesObject(matcher.clsDescriptor, observed)
    }
}


private fun AssertionsBuilder.jsonMatches(matcher: ClassPatternJsonMatcher, observed: String?) {
    try {
        if (matcher.isList.first) {
            if (!(matcher.isList.second && observed == null)) mapper.readValue(observed, List::class.java).let { elements ->
                elements.forEach { element ->
                    mapper.writeValueAsString(element).let { mapper.readValue(it, matcher.cls.java) }
                }
            }

        } else {
            if (!matcher.isNullable && observed == null) {
                throw AssertionFailedError("expected none nullable value ${matcher.pattern}", matcher.pattern, null)
            }
            if(!(matcher.isNullable && observed == null)) {

                mapper.readValue(observed, matcher.cls.java)
                if (matcher.cls == Boolean::class && !matcher.isNullable && mapper.readValue(observed, String::class.java) == null) {
                    throw AssertionFailedError("expected none nullable value ${matcher.pattern}", matcher.pattern, null)
                }
            }
        }
    } catch (e: Throwable) {
        if (e is AssertionFailedError) throw e
        throw AssertionFailedError("expected object of type ${matcher.cls}", matcher.cls, observed)
    }
}

private fun String?.toJsonMap(): JsonMap {
    return try {
        mapper.readValue(this.let { if (it?.endsWith("?") == true) it.substringBeforeLast("?") else it }, JsonMap::class.java)
    } catch (e: Throwable) {
        throw AssertionFailedError("expected json object structure", """{"...": "..."}""", this)
    }
}

private fun String?.toJsonArray(): JsonArray {
    return try {
        mapper.readValue(this, JsonArray::class.java)
    } catch (e: Throwable) {
        throw AssertionFailedError("expected json array structure", """[..., ...]""", this)
    }
}