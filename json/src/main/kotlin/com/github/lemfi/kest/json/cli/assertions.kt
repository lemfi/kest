@file:Suppress("FunctionName")

package com.github.lemfi.kest.json.cli

import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.lemfi.kest.core.builder.AssertionsBuilder
import com.github.lemfi.kest.core.cli.eq
import com.github.lemfi.kest.json.model.JsonArray
import com.github.lemfi.kest.json.model.JsonMap
import com.github.lemfi.kest.json.model.KestArray
import com.github.lemfi.kest.json.model.jsonProperty
import org.opentest4j.AssertionFailedError
import kotlin.reflect.KClass

/**
 * Add a matcher
 *
 * @param key the key for your matcher, for example {{my_matcher}}
 * @param value KClass representing of your matcher, jackson annotations can be used on that Class, for polyphormism for example
 */
fun `add json matcher`(key: String, value: KClass<*>) {
    matchers[key] = ClassPatternJsonMatcherKind(value)
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
    matchers[key] = StringPatternJsonMatcherKind(listOf(pattern))
}

/**
 * Add a matcher
 *
 * @param key the key for your matcher, for example {{my_matcher}}
 * @param patterns a list of possible string description for matcher, to use for polymorphism
 */
fun `add json matcher`(key: String, patterns: List<String>) {
    matchers[key] = StringPatternJsonMatcherKind(patterns)
}

/**
 * Add a matcher
 *
 * @param key the key for your matcher, for example {{my_matcher}}
 * @param validator a function that will validate content
 */
fun <T : Any> `add json matcher`(key: String, cls: KClass<T>, validator: (T) -> Boolean) {
    matchers[key] = FunctionJsonMatcherKind(cls, validator)
}

/**
 * Check whether a JsonMap matches a pattern
 *
 * @param expected expected pattern
 * @param observed JsonMap object
 */
fun AssertionsBuilder.jsonMatches(
    expected: String,
    observed: JsonMap?,
    checkArraysOrder: Boolean = jsonProperty { this.checkArraysOrder }
) {
    jsonMatches(expected, observed.toNullableJsonString(), checkArraysOrder)
}

/**
 * Check whether a JsonMap matches one of provided patterns
 * To use for polyphormism
 *
 * @param expectedPatterns expected patterns
 * @param observed JsonMap object
 */
fun AssertionsBuilder.jsonMatches(
    expectedPatterns: List<String>,
    observed: JsonMap?,
    checkArraysOrder: Boolean = jsonProperty { this.checkArraysOrder }
) {

    jsonMatches(expectedPatterns, observed.toNullableJsonString(), checkArraysOrder)
}

/**
 * Check whether a JsonMap matches one of provided patterns
 * To use for polyphormism
 *
 * @param expectedPatterns expected patterns
 * @param observed KestArray object
 */
fun AssertionsBuilder.jsonMatches(
    expectedPatterns: List<String>,
    observed: KestArray<*>?,
    checkArraysOrder: Boolean = jsonProperty { this.checkArraysOrder }
) {

    jsonMatches(expectedPatterns, observed.toNullableJsonString(), checkArraysOrder)
}


/**
 * Check whether a Json as String matches one of provided patterns
 * To use for polyphormism
 *
 * @param expectedPatterns expected patterns
 * @param observed Json as String
 */
fun AssertionsBuilder.jsonMatches(
    expectedPatterns: List<String>,
    observed: String?,
    checkArraysOrder: Boolean = jsonProperty { this.checkArraysOrder }
) {
    expectedPatterns.fold(Throwable() as Throwable?) { acc, expected ->
        if (acc != null)
            runCatching {
                jsonMatches(expected, observed, checkArraysOrder)
            }.exceptionOrNull()
        else acc
    }?.let { throw it }
}

/**
 * Check whether all elements of a JsonArray matches a pattern
 *
 * @param expected expected pattern
 * @param observed JsonArray object
 */
fun AssertionsBuilder.jsonMatches(
    expected: String,
    observed: Collection<*>?,
    checkArraysOrder: Boolean = jsonProperty { this.checkArraysOrder }
) {

    jsonMatches(expected, observed.toNullableJsonString(), checkArraysOrder)
}

/**
 * Check whether a Json as String matches pattern
 *
 * @param expected expected pattern
 * @param observed Json as String
 */
fun AssertionsBuilder.jsonMatches(
    expected: String,
    observed: String?,
    checkArraysOrder: Boolean = jsonProperty { this.checkArraysOrder }
) {
    if (isPattern(expected)) {
        jsonMatchesPattern(getMatcher(expected), observed, checkArraysOrder)
    } else if (expected.isObject()) {
        jsonMatchesObject(expected, observed, checkArraysOrder)
    } else if (expected.isArray()) {
        jsonMatchesArray(expected, observed, checkArraysOrder)
    } else if (expected != observed) {
        throw AssertionFailedError("expected $expected, got $observed", expected, observed)
    }
}

private fun AssertionsBuilder.jsonMatchesObject(expected: String, observed: String?, checkArraysOrder: Boolean) {

    if (expected.endsWith("?") && observed == null) return

    val exp = expected.toJsonMap()
    val obs = observed.toJsonMap()

    if (exp.keys != obs.keys) throw AssertionFailedError(
        "expected ${exp.keys} entries, got ${obs.keys} entries",
        exp.keys,
        obs.keys
    )
    exp.keys.forEach { key ->
        val expectedValue = exp[key]
        val observedValue = obs[key]

        if (isPattern(expectedValue))
            jsonMatches(expectedValue as String, observedValue.toNullableJsonString(), checkArraysOrder)
        else if (isObject(expectedValue) || isArray(expectedValue)) {
            jsonMatches(
                expectedValue.toJsonString(),
                observedValue.toNullableJsonString(),
                checkArraysOrder
            )
        } else {
            expectedValue eq observedValue
        }
    }
}

private fun AssertionsBuilder.jsonMatchesArray(expected: String, observed: String?, checkArraysOrder: Boolean) {

    val expectedArray = expected.toJsonArray().toMutableList()
    val observedArray = observed.toJsonArray()

    eq(
        expectedArray.size,
        observedArray.size
    ) { "missing entries for $observedArray, expected ${expectedArray.size} entries, got ${observedArray.size} entries" }

    if (checkArraysOrder) {
        observedArray.foldIndexed(null as Throwable?) { index, acc, observedValue ->
            acc ?: runCatching {
                jsonMatches(expectedArray[index].toJsonString(), observedValue.toNullableJsonString(), true)
            }.exceptionOrNull()
        }
    } else {
        observedArray.fold(null as Throwable?) { errorFound, observedValue ->
            errorFound ?: expectedArray
                .removeIf { expectedValue ->
                    runCatching {
                        jsonMatches(expectedValue.toJsonString(), observedValue.toNullableJsonString(), false)
                    }.exceptionOrNull() == null
                }.let { removed ->
                    if (removed) null else IllegalArgumentException("$observedValue not present in array")
                }
        }
    }
        ?.also {
            throw AssertionFailedError(it.message, it)
        }

}

private fun isString(data: Any?) = data?.let { String::class.java.isAssignableFrom(it.javaClass) } ?: false
private fun isObject(data: Any?) = data?.let { it is Map<*, *> } ?: false
private fun isArray(data: Any?) = data?.let { it is List<*> } ?: false
private fun isPattern(data: Any?) = data?.let {
    isString(data) && (data as String).let {
        val observed = it.trimIndent().trim()

        observed.startsWith("[[") && (observed.endsWith("]]") || observed.endsWith("]]?"))
                || observed.startsWith("{{") && (observed.endsWith("}}") || observed.endsWith("}}?"))
    }
} ?: false

private fun AssertionsBuilder.jsonMatchesPattern(
    matcher: JsonMatcher,
    observed: String?,
    checkArraysOrder: Boolean
) {

    val (isList, isNullableList) = matcher.isList
    if (isList) {
        if (observed == null) {
            if (!isNullableList)
                throw AssertionFailedError(
                    "expected none nullable value ${matcher.pattern}",
                    matcher.pattern,
                    null
                )
        } else {
            val observedArray = observed.toJsonArray().map { it.toNullableJsonString() }
            observedArray.forEach { element ->
                with(matcher) { matchElement(element, checkArraysOrder) }
            }
        }
    } else {
        with(matcher) { matchElement(observed, checkArraysOrder) }
    }
}

private fun String?.toJsonMap(): JsonMap {
    return try {
        mapper.readValue(
            this.let { if (it?.endsWith("?") == true) it.substringBeforeLast("?") else it },
            JsonMap::class.java
        )
    } catch (e: Throwable) {
        throw AssertionFailedError("expected json object structure", """{"...": "..."}, got $this""", this)
    }
}

private fun String?.toJsonArray(): KestArray<*> =
    try {
        mapper.readValue(this, JsonArray::class.java)
    } catch (e: Throwable) {
        try {
            mapper.readValue(this, KestArray::class.java)
        } catch (e: Throwable) {
            throw AssertionFailedError("expected json array structure", """[..., ...], got $this""", this)
        }
    }

private fun String?.isArray() = this?.trimIndent()?.trim()?.startsWith("[") ?: true
private fun String?.isObject() = this?.trimIndent()?.trim()?.startsWith("{") ?: true


private val mapper = jacksonObjectMapper().apply {
    disable(MapperFeature.ALLOW_COERCION_OF_SCALARS)
}

private sealed class JsonMatcherKind
private data class StringPatternJsonMatcherKind(val patterns: List<String>) : JsonMatcherKind()
private data class ClassPatternJsonMatcherKind(val cls: KClass<*>) : JsonMatcherKind()
private data class FunctionJsonMatcherKind<T : Any>(val cls: KClass<T>, val validator: (T) -> Boolean) :
    JsonMatcherKind()

private data class StringPatternJsonMatcher(
    override val matcher: String,
    override val isList: Pair<Boolean, Boolean>,
    override val isNullable: Boolean,
    override val pattern: String,
    val clsDescriptor: List<String>
) : JsonMatcher() {

    override fun AssertionsBuilder.matchElement(observed: String?, checkArraysOrder: Boolean) {
        jsonMatches(clsDescriptor.map { if (isNullable) "$it?" else it }, observed, checkArraysOrder)
    }
}

private data class ClassPatternJsonMatcher(
    override val matcher: String,
    override val isList: Pair<Boolean, Boolean>,
    override val isNullable: Boolean,
    override val pattern: String,
    val cls: KClass<*>
) : JsonMatcher() {

    override fun AssertionsBuilder.matchElement(observed: String?, checkArraysOrder: Boolean) {
        if (observed == null) {
            if (!isNullable) throw AssertionFailedError(
                "expected none nullable value $pattern",
                pattern,
                null
            )
        } else {
            if (cls == String::class && !observed.isJsonString()) throw AssertionFailedError(
                "expected $cls, got $observed",
                pattern,
                observed
            ) else {
                try {
                    mapper.readValue(observed, cls.java)
                } catch (e: Throwable) {
                    throw AssertionFailedError("expected object of type $cls, got $observed", cls, observed)
                }
            }

        }
    }
}

private data class FunctionJsonMatcher<T : Any>(
    override val matcher: String,
    override val isList: Pair<Boolean, Boolean>,
    override val isNullable: Boolean,
    override val pattern: String,
    val cls: KClass<T>,
    val validator: (T) -> Boolean,
) : JsonMatcher() {

    override fun AssertionsBuilder.matchElement(observed: String?, checkArraysOrder: Boolean) {
        if (observed == null) {
            if (!isNullable) throw AssertionFailedError(
                "expected none nullable value $pattern",
                pattern,
                null
            )
        } else if (!validator(
                mapper.readValue(
                    observed,
                    cls.java
                )
            )
        ) throw AssertionFailedError(
            "$observed does not validate pattern $pattern",
            pattern,
            observed
        )
    }
}

private val matchers = mutableMapOf<String, JsonMatcherKind>(
    "{{string}}" to ClassPatternJsonMatcherKind(String::class),
    "{{number}}" to ClassPatternJsonMatcherKind(Number::class),
    "{{boolean}}" to ClassPatternJsonMatcherKind(Boolean::class),
)

private fun getMatcher(key: String): JsonMatcher {
    return key.trim().replace(" ", "").let { keyWithoutSpaces ->
        val list =
            keyWithoutSpaces.startsWith("[[") && keyWithoutSpaces.endsWith("]]") || keyWithoutSpaces.endsWith("]]?")
        val listNullable = list && keyWithoutSpaces.endsWith("?")

        val keyWithoutList = keyWithoutSpaces.removePrefix("[[").removeSuffix("?").removeSuffix("]]")

        if (list && isPattern(keyWithoutList) || !list) {


            val type =
                keyWithoutList.removePrefix("{{").substringBefore("?").substringBefore("}}")
            val nullable =
                keyWithoutList.substringAfter(type).substringBefore("}}") == "?"
            val pattern = "{{$type}}"

            matchers["{{$type}}"]?.toJsonMatcher(type, list to listNullable, nullable, pattern)
                ?: throw AssertionFailedError("unknown matcher $key", "valid matcher", key)
        } else {
            StringPatternJsonMatcherKind(listOf(keyWithoutList)).toJsonMatcher(
                keyWithoutList,
                list to listNullable,
                keyWithoutList.trimIndent().trim().endsWith("?"),
                keyWithoutList
            )
        }

    }
}

@Suppress("unchecked_cast")
private fun JsonMatcherKind.toJsonMatcher(
    type: String,
    list: Pair<Boolean, Boolean>,
    nullable: Boolean,
    pattern: String
): JsonMatcher =

    when (this) {
        is ClassPatternJsonMatcherKind -> ClassPatternJsonMatcher(type, list, nullable, pattern, cls)
        is StringPatternJsonMatcherKind -> StringPatternJsonMatcher(type, list, nullable, pattern, patterns)
        is FunctionJsonMatcherKind<*> -> FunctionJsonMatcher(
            type,
            list,
            nullable,
            pattern,
            cls,
            validator as ((Any) -> Boolean)
        )
    }

sealed class JsonMatcher {

    abstract val matcher: String

    // is list to is nullable list
    abstract val isList: Pair<Boolean, Boolean>
    abstract val isNullable: Boolean
    abstract val pattern: String

    abstract fun AssertionsBuilder.matchElement(observed: String?, checkArraysOrder: Boolean)
}

fun Any?.toJsonString(): String = mapper.writeValueAsString(this)
fun Any?.toNullableJsonString() = this?.let { mapper.writeValueAsString(this) }

private fun String?.isJsonString() = this?.startsWith("\"") ?: false