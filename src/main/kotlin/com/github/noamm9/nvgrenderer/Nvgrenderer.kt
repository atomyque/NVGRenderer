package com.github.noamm9.nvgrenderer

import com.github.noamm9.nvgrenderer.batchers.MouseBatcher
import com.github.noamm9.nvgrenderer.batchers.multipass.MultiPassBatcher
import com.github.noamm9.nvgrenderer.helpers.MouseStack
import com.github.noamm9.nvgrenderer.nvg.NVG
import com.github.noamm9.nvgrenderer.nvg.PIPNVG
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.rendering.v1.SpecialGuiElementRegistry
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.network.chat.Component
import java.awt.Color

class Nvgrenderer: ClientModInitializer {
    override fun onInitializeClient() {
        SpecialGuiElementRegistry.register { PIPNVG(it.vertexConsumers()) }

        MultiPassBatcher.addCallback { ctx ->
            ctx.push()
            ctx.translate(200, 200)
            ctx.scale(1.25)

            ctx.nvg {
                NVG.rect(0, 0, 120, 40, Color(0, 0, 0, 140), 6f)
                NVG.text("NVG", 10, 10, 16, Color.WHITE, NVG.font)
            }

            ctx.gui { gfx: GuiGraphics ->
                gfx.drawString(Minecraft.getInstance().font, "GuiGraphics", 0, 50, Color.WHITE.rgb)
            }

            ctx.pop()
        }

        MouseBatcher.addCallbackClick { event ->
            if (event.button != 0) return@addCallbackClick false
            val stack = MouseStack()
            stack.push()
            stack.translate(200, 200)
            stack.scale(1.25)

            if (stack.x.toInt() in 0 .. 120 && stack.y.toInt() in 0 .. 40) {
                Minecraft.getInstance().gui.chat.addMessage(Component.literal("Clicked the nvg box"))
                return@addCallbackClick true
            }

            stack.pop()
            false
        }

        //  */
    }
}
