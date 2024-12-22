package cn.hackedmc.urticaria.util.shader.impl;

import cn.hackedmc.urticaria.util.shader.base.RiseShader;
import cn.hackedmc.urticaria.util.shader.base.RiseShaderProgram;
import cn.hackedmc.urticaria.util.shader.base.ShaderRenderType;
import cn.hackedmc.urticaria.util.shader.base.ShaderUniforms;
import cn.hackedmc.urticaria.util.shader.kernel.GaussianKernel;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.shader.Framebuffer;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import util.time.StopWatch;

import java.nio.FloatBuffer;
import java.util.List;

public class GaussianBlurShader extends RiseShader {

    private final RiseShaderProgram blurProgram = new RiseShaderProgram("blur.frag", "vertex.vsh");
    private Framebuffer inputFramebuffer = new Framebuffer(mc.displayWidth, mc.displayHeight, true);
    private Framebuffer outputFramebuffer = new Framebuffer(mc.displayWidth, mc.displayHeight, true);
    private GaussianKernel gaussianKernel = new GaussianKernel(0);

    private static final StopWatch updateTimer = new StopWatch();
    private int radius;
    private float compression;

    public GaussianBlurShader() {
        this(8);
    }

    public GaussianBlurShader(int radius) {
        this.radius = radius;
        this.compression = 4.0f;
    }

    @Override
    public void run(final ShaderRenderType type, final float partialTicks, List<Runnable> runnable) {
        // Prevent rendering
        if (!Display.isVisible()) {
            return;
        }

        this.compression = this.isTryLessRender() ? 4 : 2;

        switch (type) {
            case CAMERA: {
                this.update();
                this.setActive(!runnable.isEmpty());

                if (this.isActive()) {
                    this.inputFramebuffer.bindFramebuffer(true);
                    runnable.forEach(Runnable::run);
                    mc.getFramebuffer().bindFramebuffer(true);
                }
                break;
            }
            case OVERLAY: {
                boolean active = this.isActive() || !runnable.isEmpty();
                this.setActive(active);

                if (active) {
                    this.inputFramebuffer.bindFramebuffer(true);
                    runnable.forEach(Runnable::run);
                    this.outputFramebuffer.bindFramebuffer(true);

                    this.blurProgram.start();
                    final int programId = this.blurProgram.getProgramId();

                    // Only update GaussianKernel if radius has changed
                    if (this.gaussianKernel.getSize() != radius) {
                        this.gaussianKernel = new GaussianKernel(radius);
                        this.gaussianKernel.compute();
                    }

                    final FloatBuffer buffer = BufferUtils.createFloatBuffer(radius);
                    buffer.put(this.gaussianKernel.getKernel()).flip();

                    ShaderUniforms.uniform1f(programId, "u_radius", radius);
                    ShaderUniforms.uniformFB(programId, "u_kernel", buffer);
                    ShaderUniforms.uniform1i(programId, "u_diffuse_sampler", 0);
                    ShaderUniforms.uniform1i(programId, "u_other_sampler", 20);

                    // Update texel size and direction only when necessary
                    if (updateTimer.finished(1000 / 15) || !this.isTryLessRender()) {
                        ShaderUniforms.uniform2f(programId, "u_texel_size", 1.0F / mc.displayWidth, 1.0F / mc.displayHeight);
                        ShaderUniforms.uniform2f(programId, "u_direction", compression, 0.0F);
                    }

                    GlStateManager.enableBlend();
                    GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
                    GlStateManager.alphaFunc(GL11.GL_GREATER, 0.0F);

                    mc.getFramebuffer().bindFramebufferTexture();
                    RiseShaderProgram.drawQuad();
                    mc.getFramebuffer().bindFramebuffer(true);

                    ShaderUniforms.uniform2f(programId, "u_direction", 0.0F, compression);
                    if (updateTimer.finished(1000 / 15) || !this.isTryLessRender()) {
                        outputFramebuffer.bindFramebufferTexture();
                        GL13.glActiveTexture(GL13.GL_TEXTURE20);
                        inputFramebuffer.bindFramebufferTexture();
                        GL13.glActiveTexture(GL13.GL_TEXTURE0);
                        updateTimer.reset();
                    }
                    RiseShaderProgram.drawQuad();
                    GlStateManager.disableBlend();

                    RiseShaderProgram.stop();
                }

                break;
            }
        }
    }


    @Override
    public void update() {
        this.setActive(false);

        if (mc.displayWidth != inputFramebuffer.framebufferWidth || mc.displayHeight != inputFramebuffer.framebufferHeight) {
            inputFramebuffer.deleteFramebuffer();
            inputFramebuffer = new Framebuffer(mc.displayWidth, mc.displayHeight, true);

            outputFramebuffer.deleteFramebuffer();
            outputFramebuffer = new Framebuffer(mc.displayWidth, mc.displayHeight, true);
        } else {
            inputFramebuffer.framebufferClear();
            outputFramebuffer.framebufferClear();
        }
    }
}
