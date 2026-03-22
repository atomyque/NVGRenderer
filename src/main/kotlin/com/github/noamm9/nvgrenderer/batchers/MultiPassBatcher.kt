package com.github.noamm9.nvgrenderer.batchers

import com.github.noamm9.nvgrenderer.nvg.NVG
import com.github.noamm9.nvgrenderer.nvg.PIPNVG
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import org.joml.Matrix3x2f
import org.joml.Vector2f
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

object MultiPassBatcher {
    private val callbacks = HashSet<(MultiPassContext) -> Unit>()

    fun addCallback(cb: (MultiPassContext) -> Unit) = callbacks.add(cb)
    fun removeCallback(cb: (MultiPassContext) -> Unit) = callbacks.remove(cb)

    @JvmStatic
    fun endRenderHook(context: GuiGraphics, mc: Minecraft) {
        if (callbacks.isEmpty()) return
        val frame = MultiPassFrame()
        callbacks.forEach { it(frame) }
        frame.submit(context, mc)
    }
}

interface MultiPassContext {
    fun push()
    fun pop()
    fun translate(x: Number, y: Number)
    fun scale(x: Number, y: Number)
    fun pushScissor(x: Number, y: Number, w: Number, h: Number)
    fun popScissor()
    fun nvg(block: () -> Unit)
    fun gui(block: (GuiGraphics) -> Unit)
}

private class MultiPassFrame: MultiPassContext {
    private val commands = ArrayList<PassCommand>(64)

    override fun push() {
        commands.add(PassCommand.Push)
    }

    override fun pop() {
        commands.add(PassCommand.Pop)
    }

    override fun translate(x: Number, y: Number) {
        commands.add(PassCommand.Translate(x.toFloat(), y.toFloat()))
    }

    override fun scale(x: Number, y: Number) {
        commands.add(PassCommand.Scale(x.toFloat(), y.toFloat()))
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
            executeNVG(commands)
        }

        executeGui(commands, context)
    }
}

private sealed class PassCommand {
    data object Push: PassCommand()
    data object Pop: PassCommand()
    data class Translate(val x: Float, val y: Float): PassCommand()
    data class Scale(val x: Float, val y: Float): PassCommand()
    data class PushScissor(val x: Float, val y: Float, val w: Float, val h: Float): PassCommand()
    data object PopScissor: PassCommand()
    data class NvgDraw(val block: () -> Unit): PassCommand()
    data class GuiDraw(val block: (GuiGraphics) -> Unit): PassCommand()
}

private data class Rect(val minX: Float, val minY: Float, val maxX: Float, val maxY: Float) {
    val isEmpty: Boolean = maxX <= minX || maxY <= minY
}

private fun executeNVG(commands: List<PassCommand>) {
    val scissorStack = ArrayDeque<Rect>()
    val scissorSizeStack = ArrayDeque<Int>()
    var transform = Matrix3x2f()
    val transformStack = ArrayDeque<Matrix3x2f>()

    for (cmd in commands) {
        when (cmd) {
            is PassCommand.Push -> {
                NVG.push()
                transformStack.addLast(Matrix3x2f(transform))
                scissorSizeStack.addLast(scissorStack.size)
            }

            is PassCommand.Pop -> {
                NVG.pop()
                if (transformStack.isNotEmpty()) {
                    transform = transformStack.removeLast()
                }
                if (scissorSizeStack.isNotEmpty()) {
                    val targetSize = scissorSizeStack.removeLast()
                    while (scissorStack.size > targetSize) scissorStack.removeLast()
                }
            }

            is PassCommand.Translate -> {
                NVG.translate(cmd.x, cmd.y)
                transform.translate(cmd.x, cmd.y)
            }

            is PassCommand.Scale -> {
                NVG.scale(cmd.x, cmd.y)
                transform.scale(cmd.x, cmd.y)
            }

            is PassCommand.PushScissor -> {
                NVG.pushScissor(cmd.x, cmd.y, cmd.w, cmd.h)
                val rect = transformRect(transform, cmd.x, cmd.y, cmd.w, cmd.h)
                val clipped = intersect(rect, scissorStack.lastOrNull())
                scissorStack.addLast(clipped)
            }

            is PassCommand.PopScissor -> {
                NVG.popScissor()
                if (scissorStack.isNotEmpty()) scissorStack.removeLast()
            }

            is PassCommand.NvgDraw -> cmd.block()
            is PassCommand.GuiDraw -> Unit
        }
    }
}

private fun executeGui(commands: List<PassCommand>, gui: GuiGraphics) {
    val pose = gui.pose()
    var transform = Matrix3x2f()
    val transformStack = ArrayDeque<Matrix3x2f>()
    val scissorStack = ArrayDeque<Rect>()
    val scissorSizeStack = ArrayDeque<Int>()
    var guiClippedOut = false
    var guiScissorDepth = 0

    fun applyGuiScissor(rect: Rect?) {
        guiClippedOut = rect?.isEmpty == true
        if (rect == null) {
            if (guiScissorDepth > 0) {
                gui.disableScissor()
                guiScissorDepth --
            }
            return
        }
        val x0 = floor(rect.minX).toInt()
        val y0 = floor(rect.minY).toInt()
        val x1 = ceil(rect.maxX).toInt()
        val y1 = ceil(rect.maxY).toInt()
        if (guiScissorDepth > 0) {
            gui.disableScissor()
            guiScissorDepth --
        }
        gui.enableScissor(x0, y0, x1, y1)
        guiScissorDepth ++
    }

    for (cmd in commands) {
        when (cmd) {
            is PassCommand.Push -> {
                pose.pushMatrix()
                transformStack.addLast(Matrix3x2f(transform))
                scissorSizeStack.addLast(scissorStack.size)
            }

            is PassCommand.Pop -> {
                if (transformStack.isNotEmpty()) {
                    transform = transformStack.removeLast()
                }
                pose.popMatrix()
                if (scissorSizeStack.isNotEmpty()) {
                    val targetSize = scissorSizeStack.removeLast()
                    while (scissorStack.size > targetSize) scissorStack.removeLast()
                }
                applyGuiScissor(scissorStack.lastOrNull())
            }

            is PassCommand.Translate -> {
                pose.translate(cmd.x, cmd.y)
                transform.translate(cmd.x, cmd.y)
            }

            is PassCommand.Scale -> {
                pose.scale(cmd.x, cmd.y)
                transform.scale(cmd.x, cmd.y)
            }

            is PassCommand.PushScissor -> {
                val rect = transformRect(transform, cmd.x, cmd.y, cmd.w, cmd.h)
                val clipped = intersect(rect, scissorStack.lastOrNull())
                scissorStack.addLast(clipped)
                applyGuiScissor(clipped)
            }

            is PassCommand.PopScissor -> {
                if (scissorStack.isNotEmpty()) scissorStack.removeLast()
                applyGuiScissor(scissorStack.lastOrNull())
            }

            is PassCommand.NvgDraw -> Unit
            is PassCommand.GuiDraw -> if (! guiClippedOut) cmd.block(gui)
        }
    }

    while (guiScissorDepth > 0) {
        gui.disableScissor()
        guiScissorDepth --
    }

}

private fun transformRect(m: Matrix3x2f, x: Float, y: Float, w: Float, h: Float): Rect {
    val p1 = Vector2f(x, y)
    val p2 = Vector2f(x + w, y)
    val p3 = Vector2f(x, y + h)
    val p4 = Vector2f(x + w, y + h)
    m.transformPosition(p1)
    m.transformPosition(p2)
    m.transformPosition(p3)
    m.transformPosition(p4)

    val minX = min(min(p1.x, p2.x), min(p3.x, p4.x))
    val minY = min(min(p1.y, p2.y), min(p3.y, p4.y))
    val maxX = max(max(p1.x, p2.x), max(p3.x, p4.x))
    val maxY = max(max(p1.y, p2.y), max(p3.y, p4.y))
    return Rect(minX, minY, maxX, maxY)
}

private fun intersect(rect: Rect, other: Rect?): Rect {
    if (other == null) return rect
    val minX = max(rect.minX, other.minX)
    val minY = max(rect.minY, other.minY)
    val maxX = min(rect.maxX, other.maxX)
    val maxY = min(rect.maxY, other.maxY)
    return Rect(minX, minY, maxX, maxY)
}
