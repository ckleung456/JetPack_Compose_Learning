package com.example.core.navigation.model

import androidx.navigation3.runtime.NavKey
import androidx.savedstate.serialization.SavedStateConfiguration
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.SerializersModuleBuilder
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.serializer
import kotlin.reflect.KClass

@OptIn(InternalSerializationApi::class)
fun SerializersModuleBuilder.addPolymorphicScreens(
    screens: List<KClass<out NavKey>>
) {
    screens.forEach { screenClass ->
        // Add polymorphic mapping for each screen class
        polymorphic(NavKey::class) {
            @Suppress("UNCHECKED_CAST")
            val typeSafeClass = screenClass as KClass<NavKey>
            subclass(typeSafeClass, typeSafeClass.serializer())
        }
    }
}

fun getSerializersConfig(
    screens: List<KClass<out NavKey>> = emptyList(),
): SavedStateConfiguration {
    return SavedStateConfiguration {
        serializersModule = SerializersModule {
            addPolymorphicScreens(screens = screens)
        }
    }
}