package com.jetbrains.rider.plugins.meadow.messages

import com.intellij.DynamicBundle
import org.jetbrains.annotations.Nls
import org.jetbrains.annotations.NonNls
import org.jetbrains.annotations.PropertyKey

class MeadowBundle: DynamicBundle(BUNDLE) {
    companion object {
        @NonNls
        private const val BUNDLE = "messages.MeadowBundle"
        private val INSTANCE: MeadowBundle = MeadowBundle()

        @Nls
        fun message(
            @PropertyKey(resourceBundle = BUNDLE) key: String,
            vararg params: Any
        ): String {
            return INSTANCE.getMessage(key, *params)
        }
    }
}