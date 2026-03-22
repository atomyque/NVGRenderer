package com.github.noamm9.nvgrenderer.batchers

import com.github.noamm9.nvgrenderer.helpers.MouseStack
import com.github.noamm9.nvgrenderer.nvg.PIPNVG
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics

object NVGBatcher {
    private val callbacks = HashSet<Runnable>()
    private val mouseCallbacks = HashSet<(MouseStack) -> Unit>()

    fun addCallback(cb: Runnable) = callbacks.add(cb)
    fun removeCallback(cb: Runnable) = callbacks.remove(cb)
    fun addMouseCallback(cb: (MouseStack) -> Unit) = mouseCallbacks.add(cb)
    fun removeMouseCallback(cb: (MouseStack) -> Unit) = mouseCallbacks.remove(cb)

    @JvmStatic
    fun endRenderHook(context: GuiGraphics, mc: Minecraft) {
        PIPNVG.drawNVG(context, 0, 0, mc.window.screenWidth, mc.window.screenHeight) {
            callbacks.forEach { it.run() }
            if (mouseCallbacks.isNotEmpty()) {
                val mouse = MouseStack()
                mouseCallbacks.forEach { it.invoke(mouse) }
            }
        }
    }
}
