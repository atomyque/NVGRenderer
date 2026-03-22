package com.github.noamm9.nvgrenderer

import com.github.noamm9.nvgrenderer.nvg.PIPNVG
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.rendering.v1.SpecialGuiElementRegistry

class Nvgrenderer: ClientModInitializer {
    override fun onInitializeClient() {
        SpecialGuiElementRegistry.register { PIPNVG(it.vertexConsumers()) }
    }
}