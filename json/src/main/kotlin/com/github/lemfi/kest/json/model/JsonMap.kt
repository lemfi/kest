package com.github.lemfi.kest.json.model

import com.github.lemfi.kest.core.model.FilteredAssertionFailedError

typealias JsonMap = HashMap<String, Any?>

/**
 * get a value from its path in json
 *
 * For picking nth element of an array use &#91;n&#93; notation
 */
@Suppress("unchecked_cast")
inline fun <reified T> JsonMap.getForPath(vararg keys: String): T {
    return keys.fold(this as Any?) { res, key ->
        if (key.contains("[")) {
            key
                .split("[")
                .map { it.replace("]", "") }
                .let { it.first() to it.subList(1, it.size) }
                .let { (index, path) ->
                    path.fold((res as Map<String, Any?>)[index]) { elem, key ->
                        (elem as List<Any?>)[key.toInt()]
                    }
                }
        } else {
            ((res as Map<String, Any?>)[key])
        }
    }.let {
        if (it is T) it
        else throw FilteredAssertionFailedError("expected ${T::class} for path \"${keys.joinToString(", ") { it }}\", was ${it?.let { it::class } ?: "null"} ($it)")
    }
}

/**
 * get a value from its path in json
 *
 * For picking nth element of an array use &#91;n&#93; notation
 */
@JvmName("getAnyForPath")
@Deprecated(
    message = "Consider using typed function",
    replaceWith = ReplaceWith("this.getForPath<Any?>(*keys)"),
    level = DeprecationLevel.WARNING
)
fun JsonMap.getForPath(vararg keys: String): Any? = getForPath<Any?>(*keys)

typealias JsonArray = ArrayList<JsonMap>
