package com.example.myapplication.buildlogic

/**
 * Which project may depend on which. Checked for every project dependency as it is declared, so a bad
 * dependency fails at configuration time with a clear message instead of as a task cycle later.
 *
 * - `:app` may depend on anything; nothing may depend on `:app`.
 * - Feature implementation modules (`:feature-<name>`) may depend on `:core:*` and on any feature API
 *   module (`:feature-<name>-api`, their own or another feature's), but never on another feature's
 *   implementation.
 * - Feature API modules hold the contract one feature offers others (models, interfaces, route keys).
 *   They may depend only on `:core:*`: no feature implementations and no other API modules.
 * - Core modules may depend only on core modules in a **lower** layer.
 *
 * Why this can't form a cycle: core edges always point to a lower layer; API modules point only to core;
 * implementations point only to core and APIs; and only `:app` points to an implementation.
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
    private val FLAT_API = Regex("""^:feature-[^:]+-api$""")      // :feature-business-api
    private val NESTED_API = Regex("""^:feature:[^:]+:api$""")     // :feature:business:api

    fun isCore(path: String) = path.startsWith(":core:")
    fun isFeature(path: String) = path.startsWith(":feature-") || path.startsWith(":feature:")
    fun isFeatureApi(path: String) = FLAT_API.matches(path) || NESTED_API.matches(path)
    fun isFeatureImpl(path: String) = isFeature(path) && !isFeatureApi(path)

    /** Returns why `from -> to` is not allowed, or null if it is. */
    fun violation(from: String, to: String): String? = when {
        from == to -> null // e.g. a module's test source set depending on the module itself
        to == APP -> "nothing may depend on $APP"
        isFeatureApi(from) -> when {
            isCore(to) -> null
            isFeatureApi(to) -> "feature API modules must not depend on other API modules ($to); " +
                "move the shared types to a :core module"
            isFeature(to) -> "feature API modules must not depend on feature implementations ($to); " +
                "an API is the contract, the implementation depends on it, not the other way round"
            else -> "feature API modules may only depend on :core modules, not $to"
        }
        isFeatureImpl(from) -> when {
            isCore(to) -> null
            isFeatureApi(to) -> null
            isFeatureImpl(to) -> "feature modules must not depend on another feature's implementation ($to); " +
                "depend on its API module (${apiModuleFor(to)}) or move shared code to a :core module"
            else -> "feature modules may only depend on :core and feature API modules, not $to"
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

    /** The API module name a feature implementation would expose, used in error messages. */
    private fun apiModuleFor(impl: String): String =
        if (impl.startsWith(":feature:")) impl.split(":").take(3).joinToString(":") + ":api"
        else "$impl-api"

    private fun layerOf(path: String): Int = coreLayers[path]
        ?: throw IllegalStateException(
            "$path has no layer. Add it to ModuleRules.coreLayers in build-logic with the layer it belongs to."
        )
}
