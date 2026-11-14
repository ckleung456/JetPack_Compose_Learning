package com.example.myapplication.buildlogic

import org.gradle.api.Project
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.getByType

internal val Project.libs: VersionCatalog
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs")

internal fun VersionCatalog.lib(alias: String): Provider<MinimalExternalModuleDependency> =
    findLibrary(alias).orElseThrow { IllegalArgumentException("No library '$alias' in gradle/libs.versions.toml") }

internal fun VersionCatalog.pluginId(alias: String): String =
    findPlugin(alias).orElseThrow { IllegalArgumentException("No plugin '$alias' in gradle/libs.versions.toml") }
        .get().pluginId

internal fun Project.applyPlugin(alias: String) = pluginManager.apply(libs.pluginId(alias))

internal fun Project.addDependency(configuration: String, alias: String) =
    dependencies.add(configuration, libs.lib(alias))

internal fun Project.addPlatform(configuration: String, alias: String) =
    dependencies.add(configuration, dependencies.platform(libs.lib(alias)))
