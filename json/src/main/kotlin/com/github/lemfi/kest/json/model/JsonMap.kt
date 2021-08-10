package com.github.lemfi.kest.json.model

class JsonMap : HashMap<String, Any>() {

    /**
     * get a value from its path in json
     *
     * For picking nth element of an array use &#91;n&#93; notation
     */
    @Suppress("unchecked_cast")
    fun getForPath(vararg keys: String): Any? {
        return keys.fold(this as Any?) { res, key ->
            if (key.contains("[")) {
                key.split("[").map { it.replace("]", "") }.let { it.first() to it.subList(1, it.size) }.let {
                    it.second.fold((res as Map<String, Any?>)[it.first]) { elem, key ->
                        (elem as List<Any?>)[key.toInt()]
                    }
                }
            } else {
                (res as Map<String, Any?>)[key]
            }
        }
    }
}

class JsonArray: KestArray<JsonMap>()
open class KestArray<T> : ArrayList<T>()
