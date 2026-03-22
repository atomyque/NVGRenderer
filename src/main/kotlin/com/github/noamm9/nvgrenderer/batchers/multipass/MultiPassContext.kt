package com.github.noamm9.nvgrenderer.batchers.multipass

import com.github.noamm9.nvgrenderer.helpers.MouseStack
import net.minecraft.client.gui.GuiGraphics

interface MultiPassContext {
    val mouse: MouseStack
    fun push()
    fun pop()
    fun translate(x: Number, y: Number)
    fun scale(x: Number, y: Number)
    fun scale(n: Number)
    fun pushScissor(x: Number, y: Number, w: Number, h: Number)
    fun popScissor()
    fun nvg(block: () -> Unit)
    fun gui(block: (GuiGraphics) -> Unit)
}
