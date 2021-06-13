package com.github.lemfi.kest.junit5.model

import com.github.lemfi.kest.core.properties.property

data class Junit5RunnerProperties(
    val junit5: Junit5Prop
)

data class Junit5Prop(
    val report: String? = null,
)

fun <R> junit5RunnerProperty(l: Junit5Prop.() -> R): R {
    val shortcut: Junit5RunnerProperties.() -> R = { junit5.l() }
    return try {
        property(shortcut)
    } catch (e: Throwable) {
        Junit5Prop().l()
    }
}