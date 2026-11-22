package com.varabyte.truthish.failure

import kotlin.reflect.KClass

// `KClass.toString()` appends "(Kotlin reflection is not available)" when kotlin-reflect isn't on the classpath, and
// prints Java names (e.g. `java.lang.Integer` for `Int`). `qualifiedName` works without kotlin-reflect and maps Java
// types back to their Kotlin names. It's null for local and anonymous classes, so fall back to the Java name for those.
internal actual fun KClass<*>.toQualifiedString(): String = "class ${qualifiedName ?: java.name}"
