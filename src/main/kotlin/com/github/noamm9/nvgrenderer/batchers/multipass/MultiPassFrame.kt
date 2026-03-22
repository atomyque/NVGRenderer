package com.github.noamm9.nvgrenderer.batchers.multipass

import com.github.noamm9.nvgrenderer.helpers.MouseStack
import com.github.noamm9.nvgrenderer.nvg.PIPNVG
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics

class MultiPassFrame: MultiPassContext {
    private val commands = ArrayList<PassCommand>(64)
    override val mouse = MouseStack()

    override fun push() {
        commands.add(PassCommand.Push)
        mouse.push()
    }

    override fun pop() {
        commands.add(PassCommand.Pop)
        mouse.pop()
        mouse.updateFromMinecraft()
    }

    override fun translate(x: Number, y: Number) {
        commands.add(PassCommand.Translate(x.toFloat(), y.toFloat()))
        mouse.translate(x, y)
    }

    override fun scale(x: Number, y: Number) {
        commands.add(PassCommand.Scale(x.toFloat(), y.toFloat()))
        mouse.scale(x, y)
    }

    override fun scale(n: Number) {
        commands.add(PassCommand.Scale(n.toFloat(), n.toFloat()))
        mouse.scale(n)
    }

    override fun pushScissor(x: Number, y: Number, w: Number, h: Number) {
        commands.add(PassCommand.PushScissor(x.toFloat(), y.toFloat(), w.toFloat(), h.toFloat()))
    }

    override fun popScissor() {
        commands.add(PassCommand.PopScissor)
    }

    override fun nvg(block: () -> Unit) {
        commands.add(PassCommand.NvgDraw(block))
    }

    override fun gui(block: (GuiGraphics) -> Unit) {
        commands.add(PassCommand.GuiDraw(block))
    }

    fun submit(context: GuiGraphics, mc: Minecraft) {
        if (commands.isEmpty()) return
        PIPNVG.drawNVG(context, 0, 0, mc.window.screenWidth, mc.window.screenHeight) {
            MultiPassHandler.executeNVG(commands)
        }

        MultiPassHandler.executeGui(commands, context)
    }
}
