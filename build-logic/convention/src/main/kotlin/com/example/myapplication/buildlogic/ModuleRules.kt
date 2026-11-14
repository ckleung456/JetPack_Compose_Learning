package com.example.myapplication.buildlogic

/**
 * Which project may depend on which. Checked for every project dependency as it is declared, so a bad
 * dependency fails at configuration time with a clear message instead of as a task cycle later.
 *
 * - `:app` may depend on anything; nothing may depend on `:app`.
 * - Feature modules (`:feature-*`) may depend only on `:core:*`, never on each other.
 * - Core modules may depend only on core modules in a **lower** layer. Because every edge points strictly
 *   downwards, core modules can never form a cycle.
 */
internal object ModuleRules {

    /** Lower number = more fundamental. Add new core modules here with the layer they belong to. */
    val coreLayers: Map<String, Int> = mapOf(
        ":core:domain" to 0,
        ":core:network" to 1,
        ":core:shared-lib" to 1,
        ":core:presentation" to 2,
        ":core:location" to 2,
    )

    private const val APP = ":app"

    fun isCore(path: String) = path.startsWith(":core:")
    fun isFeature(path: String) = path.startsWith(":feature-") || path.startsWith(":feature:")

    /** Returns why `from -> to` is not allowed, or null if it is. */
    fun violation(from: String, to: String): String? = when {
        from == to -> null // e.g. a module's test source set depending on the module itself
        to == APP -> "nothing may depend on $APP"
        isFeature(from) -> when {
            isCore(to) -> null
            isFeature(to) -> "feature modules must not depend on each other ($to); move shared code to a :core module"
            else -> "feature modules may only depend on :core modules, not $to"
        }
        isCore(from) -> {
            val fromLayer = layerOf(from)
            if (!isCore(to)) {
                "core modules may only depend on other core modules, not $to"
            } else {
                val toLayer = layerOf(to)
                if (toLayer < fromLayer) null
                else "$from (layer $fromLayer) may only depend on core modules in a lower layer, but $to is layer $toLayer"
            }
        }
        else -> null // :app
    }

    private fun layerOf(path: String): Int = coreLayers[path]
        ?: throw IllegalStateException(
            "$path has no layer. Add it to ModuleRules.coreLayers in build-logic with the layer it belongs to."
        )
}
