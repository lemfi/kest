@file:Suppress("FunctionName", "unused")

package com.github.lemfi.kest.json.cli

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.module.kotlin.jacksonMapperBuilder
import com.github.lemfi.kest.core.builder.AssertionsBuilder
import com.github.lemfi.kest.core.cli.eq
import com.github.lemfi.kest.core.model.FilteredAssertionFailedError
import com.github.lemfi.kest.json.model.JsonArray
import com.github.lemfi.kest.json.model.JsonMap
import com.github.lemfi.kest.json.model.jsonProperty
import kotlin.reflect.KClass

fun optionalJsonKey(key: String) = """"'$key'[0-1]""""

sealed class Pattern {
    internal abstract val name: String
    internal abstract val isNullable: Boolean
    abstract val pattern: String
}

data class BasePattern internal constructor(
    override val name: String,
    override val isNullable: Boolean,
) : Pattern() {

    override val pattern = """{{$name${if (isNullable) "?" else ""}}}"""
    override fun toString() = pattern

    /**
     * Define type represented by pattern
     */
    inline fun <reified T : Any> definedBy() = apply {
        @Suppress("DEPRECATION")
        `add json matcher`(this, T::class)
    }

    /**
     * Define type represented by pattern
     */
    infix fun <T : Any> definedBy(cls: KClass<T>) = apply {
        @Suppress("DEPRECATION")
        `add json matcher`(this, cls)
    }

    /**
     * Define json structure represented by pattern
     */
    infix fun definedBy(value: String) = apply {
        `add json matcher`(this, value)
    }

    /**
     * Define list of json structures represented by pattern
     */
    infix fun definedBy(value: List<String>) = apply {
        `add json matcher`(this, value)
    }

    /**
     * Define a function that validates the pattern
     */
    inline infix fun <reified T : Any> definedBy(noinline validator: (T) -> Boolean) = apply {
        @Suppress("DEPRECATION")
        `add json matcher`(this, T::class, validator)
    }
}

data class ArrayPattern internal constructor(
    override val name: String,
    override val isNullable: Boolean,
    val nullableArray: Boolean,
    val arrayOf: Pattern
) : Pattern() {

    override val pattern = """[[{{$name${if (isNullable) "?" else ""}}}]]${if (nullableArray) "?" else ""}"""
    override fun toString(): String = pattern
}

/**
 * Declare a pattern occurence as nullable
 */
val Pattern.nullable: Pattern
    get() = when (this) {
        is BasePattern -> BasePattern(
            name = name,
            isNullable = true,
        )

        is ArrayPattern -> ArrayPattern(
            name = name,
            isNullable = isNullable,
            nullableArray = true,
            arrayOf = arrayOf
        )
    }

/**
 * Not Nullable String Pattern
 */
val stringPattern: Pattern = BasePattern(
    name = "string",
    isNullable = false,
)

/**
 * Not Nullable Boolean Pattern
 */
val booleanPattern = BasePattern(
    name = "boolean",
    isNullable = false,
)

/**
 * Not Nullable Number Pattern
 */
val numberPattern = BasePattern(
    name = "number",
    isNullable = false,
)

/**
 * Define a pattern
 *
 * @param name the key for your matcher
 */
fun pattern(name: String) = BasePattern(
    name = name,
    isNullable = false,
)

/**
 * Describe an array of a pattern type
 *
 * @param pattern the pattern composing the array entries
 */
fun jsonArrayOf(pattern: Pattern) = ArrayPattern(
    name = pattern.name,
    isNullable = pattern.isNullable,
    nullableArray = false,
    arrayOf = pattern,
)


@Deprecated(
    "replace with pattern() / definedBy() functions", replaceWith = ReplaceWith(
        "pattern(key /* /!\\ without surrounding {{ }} */) definedBy value"
    )
)
fun `add json matcher`(key: String, value: KClass<*>) {
    matchers[key] = ClassPatternJsonMatcherKind(value)
}

@Deprecated(
    "replace with pattern() / definedBy() functions", replaceWith = ReplaceWith(
        "pattern(key /* /!\\ without surrounding {{ }} */) definedBy pattern"
    )
)
fun `add json matcher`(key: String, pattern: String) {
    matchers[key] = StringPatternJsonMatcherKind(listOf(pattern))
}


@Deprecated(
    "replace with pattern() / definedBy() functions", replaceWith = ReplaceWith(
        "pattern(key /* /!\\ without surrounding {{ }} */) definedBy patterns"
    )
)
fun `add json matcher`(key: String, patterns: List<String>) {
    matchers[key] = StringPatternJsonMatcherKind(patterns)
}

@Deprecated(
    "replace with pattern() / definedBy() functions", replaceWith = ReplaceWith(
        "pattern(key /* /!\\ without surrounding {{ }} */) definedBy validator"
    )
)
fun <T : Any> `add json matcher`(key: String, cls: KClass<T>, validator: (T) -> Boolean) {
    matchers[key] = FunctionJsonMatcherKind(cls, validator)
}


@Deprecated(
    "replace with pattern() / definedBy() functions", replaceWith = ReplaceWith(
        "pattern(pattern) definedBy value"
    )
)
fun `add json matcher`(pattern: Pattern, value: KClass<*>) {
    matchers[pattern(pattern.name).pattern] = ClassPatternJsonMatcherKind(value)
}

private fun `add json matcher`(p: Pattern, pattern: String) {
    matchers[pattern(p.name).pattern] = StringPatternJsonMatcherKind(listOf(pattern))
}

private fun `add json matcher`(pattern: Pattern, patterns: List<String>) {
    matchers[pattern(pattern.name).toString()] = StringPatternJsonMatcherKind(patterns)
}

@Deprecated(
    "replace with pattern() / definedBy() functions", replaceWith = ReplaceWith(
        "pattern(pattern) definedBy validator"
    )
)
fun <T : Any> `add json matcher`(pattern: Pattern, cls: KClass<T>, validator: (T) -> Boolean) {
    matchers[pattern(pattern.name).pattern] = FunctionJsonMatcherKind(cls, validator)
}

/**
 * Check whether a JsonMap matches a pattern
 *
 * @param expected expected pattern
 * @param observed JsonMap object
 * @param checkArraysOrder order in arrays should be checked or not (default is true)
 * @param ignoreUnknownProperties unknown properties on observed json should be ignored or not (default is false)
 */
fun AssertionsBuilder.jsonMatches(
    expected: String,
    observed: JsonMap?,
    checkArraysOrder: Boolean = jsonProperty { this.checkArraysOrder },
    ignoreUnknownProperties: Boolean = jsonProperty { this.ignoreUnknownProperties },
) {
    jsonMatches(expected, observed.toNullableJsonString(), checkArraysOrder, ignoreUnknownProperties, mutableListOf())
}

/**
 * Check whether a JsonMap matches one of provided patterns
 * To use for polyphormism
 *
 * @param expectedPatterns expected patterns
 * @param observed JsonMap object
 * @param checkArraysOrder order in arrays should be checked or not (default is true)
 * @param ignoreUnknownProperties unknown properties on observed json should be ignored or not (default is false)
 */
fun AssertionsBuilder.jsonMatches(
    expectedPatterns: List<String>,
    observed: JsonMap?,
    checkArraysOrder: Boolean = jsonProperty { this.checkArraysOrder },
    ignoreUnknownProperties: Boolean = jsonProperty { this.ignoreUnknownProperties },
) {

    jsonMatches(expectedPatterns, observed.toNullableJsonString(), checkArraysOrder, ignoreUnknownProperties, listOf())
}

/**
 * Check whether a JsonMap matches one of provided patterns
 * To use for polyphormism
 *
 * @param expectedPatterns expected patterns
 * @param observed KestArray object
 * @param checkArraysOrder order in arrays should be checked or not (default is true)
 * @param ignoreUnknownProperties unknown properties on observed json should be ignored or not (default is false)
 */
fun AssertionsBuilder.jsonMatches(
    expectedPatterns: List<String>,
    observed: List<*>?,
    checkArraysOrder: Boolean = jsonProperty { this.checkArraysOrder },
    ignoreUnknownProperties: Boolean = jsonProperty { this.ignoreUnknownProperties },
) {

    jsonMatches(expectedPatterns, observed.toNullableJsonString(), checkArraysOrder, ignoreUnknownProperties, listOf())
}


/**
 * Check whether a Json as String matches one of provided patterns
 * To use for polyphormism
 *
 * @param expectedPatterns expected patterns
 * @param observed Json as String
 * @param checkArraysOrder order in arrays should be checked or not (default is true)
 * @param ignoreUnknownProperties unknown properties on observed json should be ignored or not (default is false)
 */
fun AssertionsBuilder.jsonMatches(
    expectedPatterns: List<String>,
    observed: String?,
    checkArraysOrder: Boolean = jsonProperty { this.checkArraysOrder },
    ignoreUnknownProperties: Boolean = jsonProperty { this.ignoreUnknownProperties },
) {
    jsonMatches(expectedPatterns, observed, checkArraysOrder, ignoreUnknownProperties, listOf())
}

private fun AssertionsBuilder.jsonMatches(
    expectedPatterns: List<String>,
    observed: String?,
    checkArraysOrder: Boolean,
    ignoreUnknownProperties: Boolean,
    path: List<String?>,
) {

    var isPatternValid = false

    val errors = expectedPatterns
        .mapNotNull { expected ->
            if (!isPatternValid) expected to runCatching {
                jsonMatches(expected, observed, checkArraysOrder, ignoreUnknownProperties, path)
            }.exceptionOrNull().apply {
                isPatternValid = this == null
            }
            else null
        }

    if (errors.none { (_, error) -> error == null }) throw FilteredAssertionFailedError(
        """Failed to validate pattern, none of following patterns matched
            |
            |${errors.joinToString("\n\n\n") { (pattern, error) -> "--------\nPATTERN\n--------\n $pattern => ${error?.message}" }}
        """.trimMargin()
    )

}

/**
 * Check whether all elements of a JsonArray matches a pattern
 *
 * @param expected expected pattern
 * @param observed JsonArray object
 * @param checkArraysOrder order in arrays should be checked or not (default is true)
 * @param ignoreUnknownProperties unknown properties on observed json should be ignored or not (default is false)
 */
fun AssertionsBuilder.jsonMatches(
    expected: String,
    observed: Collection<*>?,
    checkArraysOrder: Boolean = jsonProperty { this.checkArraysOrder },
    ignoreUnknownProperties: Boolean = jsonProperty { this.ignoreUnknownProperties },
) {

    jsonMatches(expected, observed, checkArraysOrder, ignoreUnknownProperties, mutableListOf())
}

private fun AssertionsBuilder.jsonMatches(
    expected: String,
    observed: Collection<*>?,
    checkArraysOrder: Boolean,
    ignoreUnknownProperties: Boolean,
    path: List<String?>,
) {

    jsonMatches(expected, observed.toNullableJsonString(), checkArraysOrder, ignoreUnknownProperties, path)
}

/**
 * Check whether a Json as String matches pattern
 *
 * @param expected expected pattern
 * @param observed Json as String
 * @param checkArraysOrder order in arrays should be checked or not (default is true)additional fields
 * @param ignoreUnknownProperties unknown properties on observed json should be ignored or not (default is false)
 */
fun AssertionsBuilder.jsonMatches(
    expected: String,
    observed: String?,
    checkArraysOrder: Boolean = jsonProperty { this.checkArraysOrder },
    ignoreUnknownProperties: Boolean = jsonProperty { this.ignoreUnknownProperties },
) {
    jsonMatches(expected, observed, checkArraysOrder, ignoreUnknownProperties, listOf())
}

private fun AssertionsBuilder.jsonMatches(
    expected: String,
    observed: String?,
    checkArraysOrder: Boolean,
    ignoreUnknownProperties: Boolean,
    path: List<String?>,
) {
    if (isPattern(expected, path)) {
        jsonMatchesPattern(getMatcher(expected, path), observed, checkArraysOrder, ignoreUnknownProperties, path)
    } else if (expected.isObject()) {
        jsonMatchesObject(expected, observed, checkArraysOrder, ignoreUnknownProperties, path)
    } else if (expected.isArray()) {
        jsonMatchesArray(expected, observed, checkArraysOrder, ignoreUnknownProperties, path)
    } else if (expected != observed) {
        throw FilteredAssertionFailedError("expected $expected, got $observed at ${path.path()}", expected, observed)
    }
}

private fun AssertionsBuilder.jsonMatchesObject(
    expected: String,
    observed: String?,
    checkArraysOrder: Boolean,
    ignoreUnknownProperties: Boolean,
    path: List<String?>
) {

    if (expected.endsWith("?") && observed == null) return

    val exp = expected.toJsonMap(path)
    val obs = observed.toJsonMap(path)

    val unwrappedKeys = exp.keys.unwrappedOptionalKeys()
    val optionalKeys = exp.keys.filterOptionalKeys()
    val mandatoryKeys = unwrappedKeys.minus(optionalKeys)

    if (unwrappedKeys != obs.keys && mandatoryKeys != obs.keys.minus(optionalKeys)) {

        val displayExpectedKeys =
            (
                    mandatoryKeys +
                            optionalKeys.filter { obs.containsKey(it) } +
                            optionalKeys
                                .filter { !obs.containsKey(it) }
                                .map { "optional($it)" }
                    )
                .sortedWith { k1, k2 ->
                    if (k1.startsWith("optional(")) 1 else k1.compareTo(k2)
                }

        if (!ignoreUnknownProperties || !obs.keys.containsAll(mandatoryKeys))
            throw FilteredAssertionFailedError(
                "expected $displayExpectedKeys entries, got ${obs.keys.sorted()} entries at ${path.path()}",
                displayExpectedKeys,
                obs.keys.sorted(),
            )
    }
    exp.keys.forEach { key ->

        val unwrappedKey = key.unwrappedOptionalKey()

        if (obs.containsKey(unwrappedKey)) {

            val expectedValue = exp[key]
            val observedValue = obs[unwrappedKey]

            if (isPattern(expectedValue, path) || isObject(expectedValue) || isArray(expectedValue))
                jsonMatches(
                    expectedValue.toJsonStringOrPattern(path),
                    observedValue.toNullableJsonString(),
                    checkArraysOrder,
                    ignoreUnknownProperties,
                    path.copyAdd(key)
                )
            else {
                (observedValue?.equals(expectedValue) ?: (expectedValue == null)).let { success ->
                    if (!success) throw FilteredAssertionFailedError(
                        "Expected $expectedValue, got $observedValue at ${path.copyAdd(key).path()}",
                        expectedValue,
                        observedValue
                    )
                }
            }
        }
    }
}

private fun AssertionsBuilder.jsonMatchesArray(
    expected: String,
    observed: String?,
    checkArraysOrder: Boolean,
    ignoreUnknownProperties: Boolean,
    path: List<String?>
) {

    val expectedArray = expected.toJsonArray(path)
    val observedArray = observed.toJsonArray(path).toMutableList()

    eq(
        expectedArray.size,
        observedArray.size
    ) { "missing entries for $observedArray, expected ${expectedArray.size} entries, got ${observedArray.size} entries at ${path.path()}" }

    if (checkArraysOrder) {
        expectedArray.foldIndexed(null as Throwable?) { index, acc, expectedValue ->
            acc ?: runCatching {
                jsonMatches(
                    expectedValue.toJsonStringOrPattern(path),
                    observedArray[index].toNullableJsonString(),
                    true,
                    ignoreUnknownProperties,
                    path.copyAddIndex(index)
                )
            }.exceptionOrNull()
        }
    } else {
        expectedArray.fold(null as Throwable?) { errorFound, expectedValue ->
            errorFound ?: observedArray
                .firstOrNull { observedValue ->
                    runCatching {
                        jsonMatches(
                            expectedValue.toJsonStringOrPattern(path),
                            observedValue.toNullableJsonString(),
                            false,
                            ignoreUnknownProperties,
                            path
                        )
                    }.exceptionOrNull() == null
                }.let {
                    if (it == null)
                        IllegalArgumentException("$expectedValue not found in array at ${path.path()}")
                    else run {
                        observedArray.remove(it)
                        null
                    }
                }
        }
    }
        ?.also {
            throw FilteredAssertionFailedError(it.message, it)
        }

}

private fun isString(data: Any?) = data?.let { String::class.java.isAssignableFrom(it.javaClass) } ?: false
private fun isObject(data: Any?) = data?.let { it is Map<*, *> } ?: false
private fun isArray(data: Any?) = data?.let { it is List<*> } ?: false
private fun isPattern(data: Any?, path: List<String?>): Boolean = data?.let {
    isString(data) && (data as String).let {
        val observed = it.trimIndent().trim()

        val isPattern = observed.startsWith("[[") && (observed.endsWith("]]") || observed.endsWith("]]?"))
                || observed.startsWith("{{") && (observed.endsWith("}}") || observed.endsWith("}}?"))

        if (isPattern) {
            if (observed.lastIndexOf("[[") != observed.indexOf("[["))
                error("wrong pattern $observed")
            if (observed.startsWith("[[")) {
                val arrayOf = observed.removePrefix("[[").removeSuffix("?").removeSuffix("]]")
                if (!isPattern(arrayOf, path)
                    && runCatching { arrayOf.toJsonMap(path) }.isFailure
                    && runCatching { arrayOf.toJsonArray(path) }.isFailure
                )
                    error("wrong pattern $observed")
            }
        }
        isPattern
    }
} ?: false

private fun AssertionsBuilder.jsonMatchesPattern(
    matcher: JsonMatcher,
    observed: String?,
    checkArraysOrder: Boolean,
    ignoreUnknownProperties: Boolean,
    path: List<String?>,
) {

    val (isList, isNullableList) = matcher.isList
    if (isList) {
        if (observed == null) {
            if (!isNullableList)
                throw FilteredAssertionFailedError(
                    "expected none nullable value ${matcher.pattern} at ${path.path()}",
                    matcher.pattern,
                    null
                )
        } else {
            val observedArray = observed.toJsonArray(path).map { it.toNullableJsonString() }
            observedArray.forEachIndexed { index, element ->
                with(matcher) {
                    matchElement(
                        element,
                        checkArraysOrder,
                        ignoreUnknownProperties,
                        path.copyAddIndex(index)
                    )
                }
            }
        }
    } else {
        with(matcher) { matchElement(observed, checkArraysOrder, ignoreUnknownProperties, path) }
    }
}

private fun String?.toJsonMap(path: List<String?>): JsonMap {
    return try {
        mapper.readValue(
            this.let { if (it?.trim()?.endsWith("?") == true) it.substringBeforeLast("?") else it?.trim() },
            object : TypeReference<JsonMap>() {}
        )
    } catch (e: Throwable) {
        throw FilteredAssertionFailedError(
            "expected json object structure at ${path.path()}",
            """{"...": "..."}, got $this""",
            this
        )
    }
}

private fun String?.toJsonArray(path: List<String?>): List<*> =
    try {
        mapper.readValue(this, object : TypeReference<JsonArray>() {})
    } catch (e: Throwable) {
        try {
            mapper.readValue(this, List::class.java)
        } catch (e: Throwable) {
            throw FilteredAssertionFailedError(
                "expected json array structure at ${path.path()}",
                """[..., ...], got $this""",
                this
            )
        }
    }

private fun String?.isArray() = this?.trimIndent()?.trim()?.startsWith("[") ?: true
private fun String?.isObject() = this?.trimIndent()?.trim()?.startsWith("{") ?: true


private val mapper = jacksonMapperBuilder().apply {
    disable(MapperFeature.ALLOW_COERCION_OF_SCALARS)
    enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS)
}.build()

private val mapperIgnoringAddtionalProperties = jacksonMapperBuilder().apply {
    disable(MapperFeature.ALLOW_COERCION_OF_SCALARS)
    enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS)
    disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
}.build()

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

    override fun AssertionsBuilder.matchElement(
        observed: String?,
        checkArraysOrder: Boolean,
        ignoreUnknownProperties: Boolean,
        path: List<String?>
    ) {

        if (clsDescriptor.size == 1) {
            jsonMatches(
                clsDescriptor.map { if (isNullable) "$it?" else it }.first(),
                observed,
                checkArraysOrder,
                ignoreUnknownProperties,
                path
            )
        } else {
            jsonMatches(
                clsDescriptor.map { if (isNullable) "$it?" else it },
                observed,
                checkArraysOrder,
                ignoreUnknownProperties,
                path
            )
        }
    }
}

private data class ClassPatternJsonMatcher(
    override val matcher: String,
    override val isList: Pair<Boolean, Boolean>,
    override val isNullable: Boolean,
    override val pattern: String,
    val cls: KClass<*>
) : JsonMatcher() {

    override fun AssertionsBuilder.matchElement(
        observed: String?,
        checkArraysOrder: Boolean,
        ignoreUnknownProperties: Boolean,
        path: List<String?>
    ) {
        if (observed == null) {
            if (!isNullable) throw FilteredAssertionFailedError(
                "expected none nullable value $pattern at ${path.path()}",
                pattern,
                null
            )
        } else {
            if (cls == String::class && !observed.isJsonString()) throw FilteredAssertionFailedError(
                "expected $cls, got $observed at ${path.path()}",
                pattern,
                observed
            ) else {
                try {
                    (if (ignoreUnknownProperties) mapperIgnoringAddtionalProperties else mapper)
                        .readValue(observed, cls.java)
                } catch (e: Throwable) {
                    throw FilteredAssertionFailedError(
                        "expected object of type $cls, got $observed at ${path.path()}",
                        cls,
                        observed
                    )
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

    override fun AssertionsBuilder.matchElement(
        observed: String?,
        checkArraysOrder: Boolean,
        ignoreUnknownProperties: Boolean,
        path: List<String?>
    ) {
        if (observed == null) {
            if (!isNullable) throw FilteredAssertionFailedError(
                "expected none nullable value $pattern at ${path.path()}",
                pattern,
                null
            )
        } else if (!validator(mapper.readValue(observed, cls.java))) {
            throw FilteredAssertionFailedError(
                "$observed does not validate pattern $pattern at ${path.path()}",
                pattern,
                observed
            )
        }
    }
}

private val matchers = mutableMapOf<String, JsonMatcherKind>(
    "{{string}}" to ClassPatternJsonMatcherKind(String::class),
    "{{number}}" to ClassPatternJsonMatcherKind(Number::class),
    "{{boolean}}" to ClassPatternJsonMatcherKind(Boolean::class),
)

private fun getMatcher(key: String, path: List<String?>): JsonMatcher {
    return key.trim().replace(" ", "").let { keyWithoutSpaces ->
        val list =
            keyWithoutSpaces.startsWith("[[") && keyWithoutSpaces.endsWith("]]") || keyWithoutSpaces.endsWith("]]?")
        val listNullable = list && keyWithoutSpaces.endsWith("?")

        val keyWithoutList = keyWithoutSpaces.removePrefix("[[").removeSuffix("?").removeSuffix("]]")

        if (list && isPattern(keyWithoutList, path) || !list) {

            val type =
                keyWithoutList.removePrefix("{{").substringBefore("?").substringBefore("}}")
            val nullable =
                keyWithoutList.substringAfter(type).substringBefore("}}") == "?"
            val pattern = "{{$type}}"

            matchers["{{$type}}"]?.toJsonMatcher(type, list to listNullable, nullable, pattern)
                ?: throw FilteredAssertionFailedError("unknown matcher $key at ${path.path()}", "valid matcher", key)
        } else {
            @Suppress("KotlinConstantConditions")
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

    abstract fun AssertionsBuilder.matchElement(
        observed: String?,
        checkArraysOrder: Boolean,
        ignoreUnknownProperties: Boolean,
        path: List<String?>
    )
}

fun Any?.toJsonString(): String = mapper.writeValueAsString(this)
fun Any?.toNullableJsonString() = this?.let { mapper.writeValueAsString(this) }

private fun String?.isJsonString() = this?.startsWith("\"") ?: false

private fun Any?.toJsonStringOrPattern(path: List<String?>): String =
    if (isPattern(this, path)) toString() else toJsonString()

private fun List<String?>.copyAdd(s: String?): List<String?> = toMutableList().apply { add(s) }
private fun List<String?>.copyAddIndex(i: Int): List<String?> =
    take(maxOf(size - 1, 0))
        .toMutableList()
        .apply { add("${this@copyAddIndex.lastOrNull() ?: ""}[$i]") }

private fun List<String?>.path() =
    if (isEmpty()) "ROOT" else joinToString(" > ") { """"${it?.unwrappedOptionalKey()}"""" }

private val isOptionalKey: String.() -> Boolean = {
    startsWith("'") && endsWith("'[0-1]")
}

private val unwrappedOptionalKey: String.() -> String = {
    if (isOptionalKey()) removePrefix("'").removeSuffix("'[0-1]") else this
}

private val unwrappedOptionalKeys: Set<String>.() -> Set<String> = {
    map { it.unwrappedOptionalKey() }.toSet()
}

private val filterOptionalKeys: Set<String>.() -> Set<String> = {
    filter { it.isOptionalKey() }
        .toSet()
        .unwrappedOptionalKeys()
}

