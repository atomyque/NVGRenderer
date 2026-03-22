package com.github.noamm9.nvgrenderer.helpers

import net.minecraft.client.Minecraft
import org.joml.Matrix3x2f
import org.joml.Vector2f

/**
 * Simple 2D transform stack for mouse coordinate mapping (translate/scale only).
 */
class MouseStack {
    private var current = Matrix3x2f()
    private val stack = ArrayDeque<Matrix3x2f>()

    var x: Double = 0.0
        private set
    var y: Double = 0.0
        private set

    init {
        updateFromMinecraft()
    }

    fun push() {
        stack.addLast(Matrix3x2f(current))
    }

    fun pop() {
        if (stack.isNotEmpty()) {
            current = stack.removeLast()
        }
    }

    fun translate(x: Number, y: Number) {
        current.translate(x.toFloat(), y.toFloat())
        updateFromMinecraft()
    }

    fun scale(x: Number, y: Number) {
        current.scale(x.toFloat(), y.toFloat())
        updateFromMinecraft()
    }

    fun scale(n: Number) {
        current.scale(n.toFloat(), n.toFloat())
        updateFromMinecraft()
    }

    fun updateFromScreen(screenX: Double, screenY: Double) {
        val (lx, ly) = toLocal(screenX, screenY)
        x = lx
        y = ly
    }

    fun updateFromMinecraft() {
        val mc = Minecraft.getInstance()
        val mouse = mc.mouseHandler
        val window = mc.window
        updateFromScreen(mouse.getScaledXPos(window), mouse.getScaledYPos(window))
    }

    fun toLocal(x: Double, y: Double): Pair<Double, Double> {
        val det = current.determinant()
        if (det == 0f) return x to y
        val inv = Matrix3x2f(current).invert()
        val v = Vector2f(x.toFloat(), y.toFloat())
        inv.transformPosition(v)
        return v.x.toDouble() to v.y.toDouble()
    }
}