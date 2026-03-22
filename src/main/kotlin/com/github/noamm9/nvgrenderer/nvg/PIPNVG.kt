package com.github.noamm9.nvgrenderer.nvg

import com.mojang.blaze3d.opengl.GlConst
import com.mojang.blaze3d.opengl.GlDevice
import com.mojang.blaze3d.opengl.GlStateManager
import com.mojang.blaze3d.opengl.GlTexture
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.navigation.ScreenRectangle
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer
import net.minecraft.client.gui.render.state.pip.PictureInPictureRenderState
import net.minecraft.client.renderer.MultiBufferSource
import org.joml.Matrix3x2f
import org.lwjgl.opengl.GL33C

class PIPNVG(buffer: MultiBufferSource.BufferSource): PictureInPictureRenderer<PIPNVG.NVGRenderState>(buffer) {
    override fun getTranslateY(height: Int, windowScaleFactor: Int): Float = height / 2f
    override fun getRenderStateClass(): Class<NVGRenderState> = NVGRenderState::class.java
    override fun textureIsReadyToBlit(state: NVGRenderState) = false
    override fun getTextureLabel(): String = "nvgrenderer"

    override fun renderToTexture(state: NVGRenderState, poseStack: PoseStack) {
        if (Minecraft.getInstance().window.isIconified) return
        val colorTex = RenderSystem.outputColorTextureOverride ?: return
        val stateAccess = (RenderSystem.getDevice() as? GlDevice)?.directStateAccess() ?: return
        val depthTex = (RenderSystem.outputDepthTextureOverride?.texture() as? GlTexture) ?: return
        val (width, height) = colorTex.let { it.getWidth(0) to it.getHeight(0) }.takeIf { it.first != 0 && it.second != 0 } ?: return
        val fbo = (colorTex.texture() as? GlTexture)?.getFbo(stateAccess, depthTex) ?: return

        GlStateManager._glBindFramebuffer(GlConst.GL_FRAMEBUFFER, fbo)
        GlStateManager._viewport(0, 0, width, height)
        GL33C.glBindSampler(0, 0)

        val dpr = if (state.width > 0 && state.height > 0) width.toFloat() / state.width.toFloat() else NVG.devicePixelRatio()
        NVG.beginFrame(width.toFloat(), height.toFloat(), dpr)
        state.renderContent()
        NVG.endFrame()

        GlStateManager._disableDepthTest()
        GlStateManager._disableCull()
        GlStateManager._enableBlend()
        GlStateManager._blendFuncSeparate(770, 771, 1, 0)
    }

    data class NVGRenderState(
        private val x: Int,
        private val y: Int,
        val width: Int,
        val height: Int,
        private val poseMatrix: Matrix3x2f,
        private val scissor: ScreenRectangle?,
        private val bounds: ScreenRectangle?,
        val renderContent: () -> Unit
    ): PictureInPictureRenderState {
        override fun x0(): Int = x
        override fun y0(): Int = y
        override fun x1(): Int = x + width
        override fun y1(): Int = y + height
        override fun scissorArea(): ScreenRectangle? = scissor
        override fun bounds(): ScreenRectangle? = bounds
        override fun scale(): Float = 1f
    }

    companion object {
        private fun createState(
            context: GuiGraphics,
            x: Int,
            y: Int,
            width: Int,
            height: Int,
            renderContent: () -> Unit
        ): NVGRenderState? {
            if (Minecraft.getInstance().window.isIconified || width <= 0 || height <= 0) return null
            val scissor = context.scissorStack.peek()
            val pose = Matrix3x2f(context.pose())
            val bounds = createBounds(x, y, x + width, y + height, pose, scissor)
            if (bounds == null || bounds.width <= 0 || bounds.height <= 0) return null
            return NVGRenderState(x, y, width, height, pose, scissor, bounds, renderContent)
        }

        @JvmStatic
        fun drawNVG(context: GuiGraphics, x: Int, y: Int, width: Int, height: Int, renderContent: () -> Unit) {
            val state = createState(context, x, y, width, height, renderContent) ?: return
            context.guiRenderState.submitPicturesInPictureState(state)
        }

        private fun createBounds(x0: Int, y0: Int, x1: Int, y1: Int, pose: Matrix3x2f, scissorArea: ScreenRectangle?): ScreenRectangle? {
            val screenRect = ScreenRectangle(x0, y0, x1 - x0, y1 - y0).transformMaxBounds(pose)
            if (screenRect.width <= 0 || screenRect.height <= 0) return null
            return if (scissorArea != null) scissorArea.intersection(screenRect) else screenRect
        }
    }
}
