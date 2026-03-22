package com.github.noamm9.nvgrenderer

import com.github.noamm9.nvgrenderer.batchers.MultiPassBatcher
import com.github.noamm9.nvgrenderer.nvg.NVG
import com.github.noamm9.nvgrenderer.nvg.PIPNVG
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.rendering.v1.SpecialGuiElementRegistry
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import java.awt.Color

class Nvgrenderer: ClientModInitializer {
    override fun onInitializeClient() {
        SpecialGuiElementRegistry.register { PIPNVG(it.vertexConsumers()) }

        MultiPassBatcher.addCallback { ctx ->
            ctx.push()
            ctx.translate(100, 100)
            ctx.scale(1.25f, 1.25f)

            ctx.nvg {
                NVG.rect(0, 0, 120, 40, Color(0, 0, 0, 140), 6f)
                NVG.text("NVG", 10, 10, 16, Color.WHITE, NVG.font)
            }

            ctx.gui { gfx: GuiGraphics ->
                gfx.drawString(Minecraft.getInstance().font, "GuiGraphics", 0, 50, Color.WHITE.rgb)
            }

            ctx.pop()
        }
    }
}
