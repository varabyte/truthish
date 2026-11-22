package com.varabyte.truthish.failure

import kotlin.reflect.KClass

internal actual fun KClass<*>.toQualifiedString(): String = toString()
