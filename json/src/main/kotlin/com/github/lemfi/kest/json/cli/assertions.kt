package com.github.lemfi.kest.json.cli

import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.lemfi.kest.core.builder.AssertionsBuilder
import com.github.lemfi.kest.core.cli.eq
import com.github.lemfi.kest.core.cli.fail
import com.github.lemfi.kest.json.model.JsonArray
import com.github.lemfi.kest.json.model.JsonMap
import com.github.lemfi.kest.json.model.KestArray
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
) : JsonMatcher()

private data class ClassPatternJsonMatcher(
    override val matcher: String,
    override val isList: Pair<Boolean, Boolean>,
    override val isNullable: Boolean,
    override val pattern: String,
    val cls: KClass<*>
) : JsonMatcher()

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
fun `add json matcher`(key: String, value: KClass<*>) {
    matchers.put(key, value to null)
}

/**
 * Add a matcher
 *
 * @param key the key for your matcher, for example {{my_matcher}}
 * @param pattern string description for matcher, for example:
 *          {
 *              "mykey": "{{string}},
 *              "myotherkey": "{{number}}
 *          }
 */
fun `add json matcher`(key: String, pattern: String) {
    matchers.put(key, null to listOf(pattern))
}

/**
 * Add a matcher
 *
 * @param key the key for your matcher, for example {{my_matcher}}
 * @param patterns a list of possible string description for matcher, to use for polymorphism
 */
fun `add json matcher`(key: String, patterns: List<String>) {
    matchers.put(key, null to patterns)
}

private fun getMatcher(key: String): JsonMatcher? {
    return key.trim().replace(" ", "").let { keyWithoutSpaces ->
        val list =
            keyWithoutSpaces.startsWith("[[") && keyWithoutSpaces.endsWith("]]") || keyWithoutSpaces.endsWith("]]?")
        val listNullable = list && keyWithoutSpaces.endsWith("?")

        val keyWithoutList = keyWithoutSpaces.removePrefix("[[").removeSuffix("]]")

        val type =
            keyWithoutList.removePrefix("{{").substringBefore("?").substringBefore("|").substringBefore("}}")
        val nullable =
            keyWithoutList.substringAfter(type).substringBefore("|").substringBefore("}}").equals("?")
        val pattern =
            keyWithoutList.substringAfter(type).let { if (nullable) keyWithoutList.substringAfter("?") else it }
                .substringAfter("|").substringBefore("}}")

        matchers["{{$type}}"]?.toJsonMatcher(type, list to listNullable, nullable, pattern)

    }
}

private fun Pair<KClass<*>?, List<String>?>.toJsonMatcher(
    type: String,
    list: Pair<Boolean, Boolean>,
    nullable: Boolean,
    pattern: String
): JsonMatcher {
    return if (first != null) ClassPatternJsonMatcher(
        type,
        list,
        nullable,
        pattern,
        first!!
    ) else StringPatternJsonMatcher(type, list, nullable, pattern, second!!)
}

sealed class JsonMatcher {

    abstract val matcher: String
    // is list to is nullable list
    abstract val isList: Pair<Boolean, Boolean>
    abstract val isNullable: Boolean
    abstract val pattern: String
}

/**
 * Check whether a JsonMap matches a pattern
 *
 * @param expected expected pattern
 * @param observed JsonMap object
 */
fun AssertionsBuilder.jsonMatches(expected: String, observed: JsonMap?) {
    if (getMatcher(expected) != null) {
        jsonMatches(getMatcher(expected)!!, observed)
    } else {
        if (observed == null && !expected.endsWith("?")) fail(
            "expected matching $expected, got null",
            expected,
            observed
        )

        observed?.apply {
            jsonMatches(expected.toJsonMap(fail()), observed)
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
fun AssertionsBuilder.jsonMatches(expected: List<String>, observed: JsonMap?) {

    expected.toMutableList().let { tries ->
        try {
            jsonMatches(tries.removeFirst(), observed)
        } catch (e: Throwable) {
            if (tries.size > 0) {
                jsonMatches(tries, observed)
            } else {
                throw e
            }
        }
    }
}

/**
 * Check whether all elements of a JsonArray matches a pattern
 *
 * @param expected expected pattern
 * @param observed JsonArray object
 */
fun AssertionsBuilder.jsonMatches(expected: String, observed: Collection<*>?) {

    jsonMatches(listOf(expected), observed)
}

/**
 * Check whether all elements of a JsonArray matches one of provided patterns
 *  To use for polyphormism
 *
 * @param expected expected patterns
 * @param observed JsonArray object
 */
private fun AssertionsBuilder.jsonMatches(expected: List<String>, observed: Collection<*>?) {

    if (observed == null && !expected.any { it.endsWith("?") })
        fail("expected matching $expected, got null", expected, observed)

    if (observed is JsonArray)
        observed.forEach {
            jsonMatches(expected, it)
        }
    else if (!(observed != null && observed.containsAll(expected) && expected.size == observed.size)) fail(
        "expected $expected, got $observed",
        expected,
        observed
    )
}

/**
 * Check whether all elements of a JsonArray matches one of provided patterns
 *
 * @param expected expected patterns
 * @param observed JsonArray object
 */
fun AssertionsBuilder.jsonMatches(expected: KestArray<*>, observed: KestArray<*>?) {

    if (observed == null) fail("expected matching $expected, got null", expected, observed)

    if (observed is JsonArray && expected is JsonArray)
        observed.forEachIndexed { index, it ->
            jsonMatches(expected[index], it)
        }
    else if (!(observed != null && observed.containsAll(expected) && expected.size == observed.size)) fail(
        "expected $expected, got $observed",
        expected,
        observed
    )
}

/**
 * Check whether a Json as String matches pattern
 *
 * @param expected expected pattern
 * @param observed Json as String
 */
fun AssertionsBuilder.jsonMatches(expected: String, observed: String?) {
    if (expected.isObject()) {
        jsonMatches(expected, observed.toJsonMap(fail()))
    } else {
        if (isPattern(expected)) {

            val start = expected.indexOf("[[")
            val end = expected.lastIndexOf("]]")

            jsonMatches(listOf(expected
                .removeRange(end, expected.length)
                .removeRange(0, start + 2)
            ), observed.toJsonArray(fail()))
        }
    }
}

/**
 * Check whether a Json as String matches one of provided patterns
 * To use for polyphormism
 *
 * @param expected expected patterns
 * @param observed Json as String
 */
fun AssertionsBuilder.jsonMatches(expected: List<String>, observed: String?) {
    return jsonMatches(expected, observed.toJsonMap(fail()))
}

private fun AssertionsBuilder.jsonMatches(expected: JsonMap, observed: JsonMap) {

    if (expected.keys != observed.keys) fail(
        "expected ${expected.keys} entries, got ${observed.keys} entries",
        expected.keys,
        observed.keys
    )
    expected.keys.forEach {
        val expectedValue = expected[it]
        when {
            isMap(expectedValue) -> jsonMatches(
                mapper.writeValueAsString(expected[it]),
                mapper.writeValueAsString(observed[it])
            )
            isArray(expectedValue) ->
                jsonMatches(
                    mapper.writeValueAsString(expectedValue).toJsonArray(fail()),
                    mapper.writeValueAsString(observed[it]).toJsonArray(fail())
                )
            isNumber(expectedValue) || isBoolean(expected[it]) -> eq(expected[it], observed[it])
            isPattern(expectedValue) -> {
                val matcher = getMatcher(expectedValue as String)
                jsonMatches(matcher!!, observed[it])
            }
            isString(expectedValue) -> {

                if (observed[it]?.let { String::class.java.isAssignableFrom(it.javaClass) } == false) {
                    fail("expected ${String::class.java} got ${observed[it]?.javaClass}", String::class.java, observed[it]?.javaClass)
                } else {

                    val observedValue = observed[it]?.let { mapper.writeValueAsString(it) }
                    getMatcher(expectedValue as String)?.let { matcher ->
                        jsonMatches(matcher, observedValue)

                    } ?: eq(expectedValue, observed[it])
                }
            }
        }
    }
}

private fun isString(data: Any?) = data?.let { String::class.java.isAssignableFrom(it.javaClass) } ?: false
private fun isNumber(data: Any?) = data?.let { Number::class.java.isAssignableFrom(it.javaClass) } ?: false
private fun isMap(data: Any?) = data?.let { Map::class.java.isAssignableFrom(it.javaClass) } ?: false
private fun isArray(data: Any?) = data?.let { List::class.java.isAssignableFrom(it.javaClass) } ?: false
private fun isBoolean(data: Any?) = data?.let { Boolean::class.java.isAssignableFrom(it.javaClass) } ?: false
private fun isPattern(data: Any?) = data?.let {
    isString(data) && (data as String).let { it.startsWith("[[") && it.endsWith("]]") || it.startsWith("{{") && it.endsWith("}}") }
} ?: false

private fun AssertionsBuilder.jsonMatches(matcher: JsonMatcher, observed: Any?) {
    when (matcher) {
        is ClassPatternJsonMatcher -> jsonMatches(matcher, observed)
        is StringPatternJsonMatcher -> jsonMatches(matcher, observed)
    }
}


private fun AssertionsBuilder.jsonMatches(matcher: StringPatternJsonMatcher, observed: Any?) {
    val observedString = mapper.writeValueAsString(observed)
    val (isList, isNullableList) = matcher.isList
    if (isList) {
        if (!(isNullableList && observed == null))
            jsonMatches(
                matcher.clsDescriptor.map { if (matcher.isNullable) "$it?" else it } ,
                observedString.toJsonArray(fail())
            )
    } else {
        jsonMatches(matcher.clsDescriptor, observedString)
    }
}

private fun AssertionsBuilder.jsonMatches(matcher: ClassPatternJsonMatcher, observed: Any?) {
    val observedString = mapper.writeValueAsString(observed)
    try {
        val (isList, isNullableList) = matcher.isList
        if (isList) {
            if (!(isNullableList && observed == null)) mapper.readValue(observedString, List::class.java)
                .let { elements ->
                    elements.forEach { element ->
                        if (matcher.cls == String::class && element?.let { matcher.cls.java.isAssignableFrom(it.javaClass) } == false) {
                            fail("expected ${matcher.cls} got ${element.javaClass}", matcher.cls, element)
                        } else {
                            mapper.writeValueAsString(element).let { mapper.readValue(it, matcher.cls.java) }
                        }
                    }
                }

        } else {
            if (!matcher.isNullable && observed == null) {
                fail("expected none nullable value ${matcher.pattern}", matcher.pattern, null)
            }
            if (!(matcher.isNullable && observed == null)) {

                if (matcher.cls == String::class && observed?.let { matcher.cls.java.isAssignableFrom(it.javaClass) } == false) {
                    fail("expected ${matcher.cls} got ${observed.javaClass}", matcher.cls, observed)
                } else {
                    mapper.readValue(observedString, matcher.cls.java)
                }
                if (matcher.cls == Boolean::class && !matcher.isNullable && mapper.readValue(
                        observedString,
                        String::class.java
                    ) == null
                ) {
                    fail("expected none nullable value ${matcher.pattern}", matcher.pattern, null)
                }
            }
        }
    } catch (e: Throwable) {
        if (e is AssertionFailedError) throw e
        fail("expected object of type ${matcher.cls}", matcher.cls, observed)
    }
}

private fun String?.toJsonMap(fail: (String, Any?, Any?)->Unit): JsonMap {
    return try {
        mapper.readValue(
            this.let { if (it?.endsWith("?") == true) it.substringBeforeLast("?") else it },
            JsonMap::class.java
        )
    } catch (e: Throwable) {
        fail("expected json object structure", """{"...": "..."}""", this)
        throw e
    }
}

private fun String?.toJsonArray(fail: (String, Any?, Any?)->Unit): KestArray<*> {
    return try {
        mapper.readValue(this, JsonArray::class.java)
    } catch (e: Throwable) {
        try {
            mapper.readValue(this, KestArray::class.java)
        } catch (e: Throwable) {
            fail("expected json array structure", """[..., ...]""", this)
            throw e
        }
    }
}

private fun AssertionsBuilder.fail() = { s: String, a: Any?, b: Any? ->
    fail(s, a, b)
}

private fun String?.isObject() = this?.trimIndent()?.trim()?.startsWith("{") ?: true
