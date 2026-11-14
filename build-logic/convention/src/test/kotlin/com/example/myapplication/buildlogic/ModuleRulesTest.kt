package com.example.myapplication.buildlogic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class ModuleRulesTest {

    private fun allowed(from: String, to: String) = assertNull("$from -> $to", ModuleRules.violation(from, to))
    private fun forbidden(from: String, to: String) = assertNotNull("$from -> $to", ModuleRules.violation(from, to))

    @Test
    fun `app may depend on anything`() {
        allowed(":app", ":feature-business")
        allowed(":app", ":core:location")
    }

    @Test
    fun `nothing may depend on app`() {
        forbidden(":feature-business", ":app")
        forbidden(":core:domain", ":app")
    }

    @Test
    fun `features may depend on core only`() {
        ModuleRules.coreLayers.keys.forEach { allowed(":feature-business", it) }
        forbidden(":feature-business", ":feature-country")
        forbidden(":feature:a:presentation", ":feature:b:domain")
    }

    @Test
    fun `core may not depend on features`() {
        forbidden(":core:presentation", ":feature-business")
    }

    @Test
    fun `core may only depend on a lower layer`() {
        allowed(":core:presentation", ":core:shared-lib")
        allowed(":core:location", ":core:domain")
        forbidden(":core:domain", ":core:network")
        forbidden(":core:shared-lib", ":core:network") // same layer
        forbidden(":core:shared-lib", ":core:presentation") // upwards
    }

    @Test
    fun `core layers cannot form a cycle`() {
        // For every pair, at most one direction is allowed.
        val core = ModuleRules.coreLayers.keys
        for (a in core) for (b in core) if (a != b) {
            val both = ModuleRules.violation(a, b) == null && ModuleRules.violation(b, a) == null
            assertEquals("$a <-> $b both allowed", false, both)
        }
    }

    @Test(expected = IllegalStateException::class)
    fun `unknown core module must be given a layer`() {
        ModuleRules.violation(":core:new-thing", ":core:domain")
    }

    @Test
    fun `self dependency is allowed`() {
        allowed(":core:domain", ":core:domain")
    }
}
