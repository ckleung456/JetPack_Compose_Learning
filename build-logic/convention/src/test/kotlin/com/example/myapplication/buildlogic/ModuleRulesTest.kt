package com.example.myapplication.buildlogic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ModuleRulesTest {

    private fun allowed(from: String, to: String) = assertNull("$from -> $to", ModuleRules.violation(from, to))
    private fun forbidden(from: String, to: String) = assertNotNull("$from -> $to", ModuleRules.violation(from, to))

    @Test
    fun `app may depend on anything`() {
        allowed(":app", ":feature-business")
        allowed(":app", ":feature-business-api")
        allowed(":app", ":core:location")
    }

    @Test
    fun `nothing may depend on app`() {
        forbidden(":feature-business", ":app")
        forbidden(":feature-business-api", ":app")
        forbidden(":core:domain", ":app")
    }

    // --- module kinds

    @Test
    fun `api modules are recognised in flat and nested form`() {
        assertTrue(ModuleRules.isFeatureApi(":feature-business-api"))
        assertTrue(ModuleRules.isFeatureApi(":feature:business:api"))
        assertFalse(ModuleRules.isFeatureApi(":feature-business"))
        assertFalse(ModuleRules.isFeatureApi(":feature-api-tools")) // "api" not at the end
        assertTrue(ModuleRules.isFeatureImpl(":feature-business"))
        assertFalse(ModuleRules.isFeatureImpl(":feature-business-api"))
    }

    // --- feature implementations

    @Test
    fun `features may depend on core`() {
        ModuleRules.coreLayers.keys.forEach { allowed(":feature-business", it) }
    }

    @Test
    fun `features may depend on their own and other features' api modules`() {
        allowed(":feature-business", ":feature-business-api")
        allowed(":feature-business", ":feature-country-api")
        allowed(":feature:business:presentation", ":feature:country:api")
    }

    @Test
    fun `features must not depend on another feature's implementation`() {
        forbidden(":feature-business", ":feature-country")
        forbidden(":feature:a:presentation", ":feature:b:domain")
    }

    @Test
    fun `impl to impl message points to the api module`() {
        val flat = ModuleRules.violation(":feature-business", ":feature-country")!!
        assertTrue(flat, flat.contains(":feature-country-api"))
        val nested = ModuleRules.violation(":feature:business:presentation", ":feature:country:data")!!
        assertTrue(nested, nested.contains(":feature:country:api"))
    }

    // --- feature api modules

    @Test
    fun `api modules may depend on core`() {
        ModuleRules.coreLayers.keys.forEach { allowed(":feature-country-api", it) }
    }

    @Test
    fun `api modules must not depend on feature implementations`() {
        forbidden(":feature-country-api", ":feature-country") // its own implementation
        forbidden(":feature-country-api", ":feature-business")
    }

    @Test
    fun `api modules must not depend on other api modules`() {
        forbidden(":feature-country-api", ":feature-business-api")
        forbidden(":feature:country:api", ":feature:business:api")
    }

    // --- core

    @Test
    fun `core may not depend on features or apis`() {
        forbidden(":core:presentation", ":feature-business")
        forbidden(":core:presentation", ":feature-business-api")
    }

    @Test
    fun `core may only depend on a lower layer`() {
        allowed(":core:presentation", ":core:shared-lib")
        allowed(":core:location", ":core:domain")
        forbidden(":core:domain", ":core:network")
        forbidden(":core:shared-lib", ":core:network") // same layer
        forbidden(":core:shared-lib", ":core:presentation") // upwards
    }

    @Test(expected = IllegalStateException::class)
    fun `unknown core module must be given a layer`() {
        ModuleRules.violation(":core:new-thing", ":core:domain")
    }

    @Test
    fun `self dependency is allowed`() {
        allowed(":core:domain", ":core:domain")
    }

    // --- no cycles

    @Test
    fun `no two modules may depend on each other`() {
        val modules = listOf(":app") + ModuleRules.coreLayers.keys + listOf(
            ":feature-business", ":feature-country", ":feature-doordash",
            ":feature-business-api", ":feature-country-api",
            ":feature:orders:presentation", ":feature:orders:api", ":feature:cart:api",
        )
        for (a in modules) for (b in modules) if (a != b) {
            val both = ModuleRules.violation(a, b) == null && ModuleRules.violation(b, a) == null
            assertEquals("$a <-> $b both allowed", false, both)
        }
    }

    @Test
    fun `allowed edges never form a cycle`() {
        // Depth-first search over every allowed edge between a representative set of modules.
        val modules = ModuleRules.coreLayers.keys + listOf(
            ":feature-business", ":feature-country", ":feature-business-api", ":feature-country-api",
            ":feature:orders:presentation", ":feature:orders:api",
        )
        val edges = modules.associateWith { from -> modules.filter { to -> to != from && ModuleRules.violation(from, to) == null } }
        val done = mutableSetOf<String>()
        fun visit(node: String, path: List<String>) {
            if (node in path) error("cycle: ${(path + node).joinToString(" -> ")}")
            if (!done.add(node)) return
            edges.getValue(node).forEach { visit(it, path + node) }
        }
        modules.forEach { done.clear(); visit(it, emptyList()) }
    }
}
