package com.github.noamm9.nvgrenderer

import com.github.noamm9.nvgrenderer.demo.NVGDemoScreen
import com.github.noamm9.nvgrenderer.nvg.NVGPIP
import com.mojang.blaze3d.platform.InputConstants
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.fabricmc.fabric.api.client.rendering.v1.SpecialGuiElementRegistry
import net.minecraft.client.KeyMapping
import org.lwjgl.glfw.GLFW

object Nvgrenderer: ClientModInitializer {
    private val openDemoKey = KeyBindingHelper.registerKeyBinding(
        KeyMapping("nvg demo", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_F8, KeyMapping.Category.MISC)
    )

    override fun onInitializeClient() {
        SpecialGuiElementRegistry.register { NVGPIP(it.vertexConsumers()) }

        ClientTickEvents.END_CLIENT_TICK.register { client ->
            while (openDemoKey.consumeClick()) {
                client.setScreen(NVGDemoScreen())
            }
        }
    }
}