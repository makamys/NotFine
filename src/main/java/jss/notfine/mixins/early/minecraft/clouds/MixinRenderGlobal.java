package jss.notfine.mixins.early.minecraft.clouds;

import jss.notfine.core.NotFineSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.IRenderHandler;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(RenderGlobal.class)
public class MixinRenderGlobal {

    /**
     * @author jss2a98aj
     * @reason Adjust how cloud render mode is selected.
     */
    @Overwrite
    public void renderClouds(float partialTicks) {
        IRenderHandler renderer;
        if((renderer = theWorld.provider.getCloudRenderer()) != null) {
            renderer.render(partialTicks, theWorld, mc);
            return;
        }
        if(mc.theWorld.provider.isSurfaceWorld()) {
            int cloudMode = (int)NotFineSettings.Settings.MODE_CLOUDS.getValue();
            if(cloudMode == 0 || cloudMode == -1 && mc.gameSettings.fancyGraphics) {
                renderCloudsFancy(partialTicks);
            } else {
                renderCloudsFast(partialTicks);
            }
        }
    }

    /**
     * @author jss2a98aj
     * @reason Adjust fancy cloud render.
     */
    @Overwrite
    public void renderCloudsFancy(float partialTicks) {
        Tessellator tessellator = Tessellator.instance;
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glEnable(GL11.GL_BLEND);
        OpenGlHelper.glBlendFunc(770, 771, 1, 0);
        renderEngine.bindTexture(locationCloudsPng);

        Vec3 color = theWorld.getCloudColour(partialTicks);
        float red = (float)color.xCoord;
        float green = (float)color.yCoord;
        float blue = (float)color.zCoord;
        if(mc.gameSettings.anaglyph) {
            float altRed = (red * 30.0F + green * 59.0F + blue * 11.0F) / 100.0F;
            float altGreen = (red * 30.0F + green * 70.0F) / 100.0F;
            float altBlue = (red * 30.0F + blue * 70.0F) / 100.0F;
            red = altRed;
            green = altGreen;
            blue = altBlue;
        }
        double cloudTick = ((float)cloudTickCounter + partialTicks);

        float cloudInteriorWidth = 12.0F * NotFineSettings.cloudScale;
        float cloudInteriorHeight = 4.0F * NotFineSettings.cloudScale;
        float cameraOffsetY = (float)(mc.renderViewEntity.lastTickPosY + (mc.renderViewEntity.posY - mc.renderViewEntity.lastTickPosY) * (double)partialTicks);
        double cameraOffsetX = (mc.renderViewEntity.prevPosX + (mc.renderViewEntity.posX - mc.renderViewEntity.prevPosX) * (double)partialTicks + cloudTick * 0.03D) / (double)cloudInteriorWidth;
        double cameraOffsetZ = (mc.renderViewEntity.prevPosZ + (mc.renderViewEntity.posZ - mc.renderViewEntity.prevPosZ) * (double)partialTicks) / (double)cloudInteriorWidth + 0.33D;
        cameraOffsetX -= MathHelper.floor_double(cameraOffsetX / 2048.0D) * 2048;
        cameraOffsetZ -= MathHelper.floor_double(cameraOffsetZ / 2048.0D) * 2048;

        float cameraRelativeY = theWorld.provider.getCloudHeight() - cameraOffsetY + 0.33F;
        float cameraRelativeX = (float)(cameraOffsetX - (double)MathHelper.floor_double(cameraOffsetX));
        float cameraRelativeZ = (float)(cameraOffsetZ - (double)MathHelper.floor_double(cameraOffsetZ));

        float scrollSpeed = 0.00390625F;
        float cloudScrollingX = (float)MathHelper.floor_double(cameraOffsetX) * scrollSpeed;
        float cloudScrollingZ = (float)MathHelper.floor_double(cameraOffsetZ) * scrollSpeed;

        float cloudWidth = 8f;
        int renderRadius = (int)(NotFineSettings.Settings.RENDER_DISTANCE_CLOUDS.getValue() / NotFineSettings.cloudScale);
        float edgeOverlap = 0.0001f;//0.001F;
        GL11.glScalef(cloudInteriorWidth, 1.0F, cloudInteriorWidth);

        for (int loop = 0; loop < 2; ++loop) {
            if (loop == 0) {
                GL11.glColorMask(false, false, false, false);
            } else if (mc.gameSettings.anaglyph) {
                if (EntityRenderer.anaglyphField == 0) {
                    GL11.glColorMask(false, true, true, true);
                } else {
                    GL11.glColorMask(true, false, false, true);
                }
            } else {
                GL11.glColorMask(true, true, true, true);
            }

            for(int chunkX = -renderRadius + 1; chunkX <= renderRadius; ++chunkX) {
                for(int chunkY = -renderRadius + 1; chunkY <= renderRadius; ++chunkY) {
                    tessellator.startDrawingQuads();
                    float chunkOffsetX = (chunkX * cloudWidth);
                    float chunkOffsetY = (chunkY * cloudWidth);
                    float startX = chunkOffsetX - cameraRelativeX;
                    float startZ = chunkOffsetY - cameraRelativeZ;

                    //Cloud top
                    if(cameraRelativeY > -cloudInteriorHeight - 1.0F) {
                        tessellator.setColorRGBA_F(red * 0.7F, green * 0.7F, blue * 0.7F, 0.8F);
                        tessellator.setNormal(0.0F, -1.0F, 0.0F);
                        tessellator.addVertexWithUV(startX, cameraRelativeY, (startZ + cloudWidth), (chunkOffsetX * scrollSpeed + cloudScrollingX), ((chunkOffsetY + cloudWidth) * scrollSpeed + cloudScrollingZ));
                        tessellator.addVertexWithUV((startX + cloudWidth), cameraRelativeY, (startZ + cloudWidth), ((chunkOffsetX + cloudWidth) * scrollSpeed + cloudScrollingX), ((chunkOffsetY + cloudWidth) * scrollSpeed + cloudScrollingZ));
                        tessellator.addVertexWithUV((startX + cloudWidth), cameraRelativeY, startZ, ((chunkOffsetX + cloudWidth) * scrollSpeed + cloudScrollingX), (chunkOffsetY * scrollSpeed + cloudScrollingZ));
                        tessellator.addVertexWithUV(startX, cameraRelativeY, startZ, (chunkOffsetX * scrollSpeed + cloudScrollingX), (chunkOffsetY * scrollSpeed + cloudScrollingZ));
                    }
                    //Cloud bottom
                    if(cameraRelativeY <= cloudInteriorHeight + 1.0F) {
                        tessellator.setColorRGBA_F(red, green, blue, 0.8F);
                        tessellator.setNormal(0.0F, 1.0F, 0.0F);
                        tessellator.addVertexWithUV(startX, (cameraRelativeY + cloudInteriorHeight - edgeOverlap), (startZ + cloudWidth), ((chunkOffsetX) * scrollSpeed + cloudScrollingX), ((chunkOffsetY + cloudWidth) * scrollSpeed + cloudScrollingZ));
                        tessellator.addVertexWithUV((startX + cloudWidth), (cameraRelativeY + cloudInteriorHeight - edgeOverlap), (startZ + cloudWidth), ((chunkOffsetX + cloudWidth) * scrollSpeed + cloudScrollingX), ((chunkOffsetY + cloudWidth) * scrollSpeed + cloudScrollingZ));
                        tessellator.addVertexWithUV((startX + cloudWidth), (cameraRelativeY + cloudInteriorHeight - edgeOverlap), startZ, ((chunkOffsetX + cloudWidth) * scrollSpeed + cloudScrollingX), (chunkOffsetY * scrollSpeed + cloudScrollingZ));
                        tessellator.addVertexWithUV(startX, (cameraRelativeY + cloudInteriorHeight - edgeOverlap), startZ, (chunkOffsetX * scrollSpeed + cloudScrollingX), (chunkOffsetY * scrollSpeed + cloudScrollingZ));
                    }

                    tessellator.setColorRGBA_F(red * 0.9F, green * 0.9F, blue * 0.9F, 0.8F);
                    float chunk;

                    if(chunkX > -1) {
                        tessellator.setNormal(-1.0F, 0.0F, 0.0F);
                        for(chunk = 0f; chunk < cloudWidth; ++chunk) {
                            double x = startX + chunk;
                            tessellator.addVertexWithUV(x, cameraRelativeY, (startZ + cloudWidth), ((chunkOffsetX + chunk + 0.5F) * scrollSpeed + cloudScrollingX), ((chunkOffsetY + cloudWidth) * scrollSpeed + cloudScrollingZ));
                            tessellator.addVertexWithUV(x, (cameraRelativeY + cloudInteriorHeight), (startZ + cloudWidth), ((chunkOffsetX + chunk + 0.5F) * scrollSpeed + cloudScrollingX), ((chunkOffsetY + cloudWidth) * scrollSpeed + cloudScrollingZ));
                            tessellator.addVertexWithUV(x, (cameraRelativeY + cloudInteriorHeight), startZ, ((chunkOffsetX + chunk + 0.5F) * scrollSpeed + cloudScrollingX), ((chunkOffsetY) * scrollSpeed + cloudScrollingZ));
                            tessellator.addVertexWithUV(x, cameraRelativeY, startZ, ((chunkOffsetX + chunk + 0.5F) * scrollSpeed + cloudScrollingX), ((chunkOffsetY) * scrollSpeed + cloudScrollingZ));
                        }
                    }

                    if(chunkX <= 1) {
                        tessellator.setNormal(1.0F, 0.0F, 0.0F);
                        for(chunk = 0f; chunk < cloudWidth; ++chunk) {
                            double x = startX + chunk + 1.0F - edgeOverlap;
                            tessellator.addVertexWithUV(x, cameraRelativeY, (startZ + cloudWidth), ((chunkOffsetX + chunk + 0.5F) * scrollSpeed + cloudScrollingX), ((chunkOffsetY + cloudWidth) * scrollSpeed + cloudScrollingZ));
                            tessellator.addVertexWithUV(x, (cameraRelativeY + cloudInteriorHeight), (startZ + cloudWidth), ((chunkOffsetX + chunk + 0.5F) * scrollSpeed + cloudScrollingX), ((chunkOffsetY + cloudWidth) * scrollSpeed + cloudScrollingZ));
                            tessellator.addVertexWithUV(x, (cameraRelativeY + cloudInteriorHeight), startZ, ((chunkOffsetX + chunk + 0.5F) * scrollSpeed + cloudScrollingX), ((chunkOffsetY) * scrollSpeed + cloudScrollingZ));
                            tessellator.addVertexWithUV(x, cameraRelativeY, startZ, ((chunkOffsetX + chunk + 0.5F) * scrollSpeed + cloudScrollingX), ((chunkOffsetY) * scrollSpeed + cloudScrollingZ));
                        }
                    }

                    tessellator.setColorRGBA_F(red * 0.8F, green * 0.8F, blue * 0.8F, 0.8F);

                    if(chunkY > -1) {
                        tessellator.setNormal(0.0F, 0.0F, -1.0F);
                        for(chunk = 0f; chunk < cloudWidth; ++chunk) {
                            tessellator.addVertexWithUV(startX, (cameraRelativeY + cloudInteriorHeight), (startZ +chunk), ((chunkOffsetX) * scrollSpeed + cloudScrollingX), ((chunkOffsetY + chunk + 0.5F) * scrollSpeed + cloudScrollingZ));
                            tessellator.addVertexWithUV((startX + cloudWidth), (cameraRelativeY + cloudInteriorHeight), (startZ + chunk), ((chunkOffsetX + cloudWidth) * scrollSpeed + cloudScrollingX), ((chunkOffsetY + chunk + 0.5F) * scrollSpeed + cloudScrollingZ));
                            tessellator.addVertexWithUV((startX + cloudWidth), (cameraRelativeY), (startZ + chunk), ((chunkOffsetX + cloudWidth) * scrollSpeed + cloudScrollingX), ((chunkOffsetY + chunk + 0.5F) * scrollSpeed + cloudScrollingZ));
                            tessellator.addVertexWithUV(startX, (cameraRelativeY), (startZ + chunk), ((chunkOffsetX) * scrollSpeed + cloudScrollingX), ((chunkOffsetY + chunk + 0.5F) * scrollSpeed + cloudScrollingZ));
                        }
                    }

                    if(chunkY <= 1) {
                        tessellator.setNormal(0.0F, 0.0F, 1.0F);
                        for(chunk = 0f; chunk < cloudWidth; ++chunk) {
                            tessellator.addVertexWithUV(startX, (cameraRelativeY + cloudInteriorHeight), (startZ + chunk + 1.0F - edgeOverlap), (chunkOffsetX * scrollSpeed + cloudScrollingX), ((chunkOffsetY + chunk + 0.5F) * scrollSpeed + cloudScrollingZ));
                            tessellator.addVertexWithUV((startX + cloudWidth), (cameraRelativeY + cloudInteriorHeight), (startZ + chunk + 1.0F - edgeOverlap), ((chunkOffsetX + cloudWidth) * scrollSpeed + cloudScrollingX), ((chunkOffsetY + chunk + 0.5F) * scrollSpeed + cloudScrollingZ));
                            tessellator.addVertexWithUV((startX + cloudWidth), cameraRelativeY, (startZ + chunk + 1.0F - edgeOverlap), ((chunkOffsetX + cloudWidth) * scrollSpeed + cloudScrollingX), ((chunkOffsetY + chunk + 0.5F) * scrollSpeed + cloudScrollingZ));
                            tessellator.addVertexWithUV(startX, (cameraRelativeY), (startZ + chunk + 1.0F - edgeOverlap), (chunkOffsetX * scrollSpeed + cloudScrollingX), ((chunkOffsetY + chunk + 0.5F) * scrollSpeed + cloudScrollingZ));
                        }
                    }
                    tessellator.draw();
                }
            }
        }

        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_CULL_FACE);
    }

    public void renderCloudsFast(float partialTicks) {
        Tessellator tessellator = Tessellator.instance;
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glEnable(GL11.GL_BLEND);
        OpenGlHelper.glBlendFunc(770, 771, 1, 0);
        renderEngine.bindTexture(locationCloudsPng);

        Vec3 color = theWorld.getCloudColour(partialTicks);
        float red = (float)color.xCoord;
        float green = (float)color.yCoord;
        float blue = (float)color.zCoord;
        if (mc.gameSettings.anaglyph) {
            float altRed = (red * 30.0F + green * 59.0F + blue * 11.0F) / 100.0F;
            float altGreen = (red * 30.0F + green * 70.0F) / 100.0F;
            float altBlue = (red * 30.0F + blue * 70.0F) / 100.0F;
            red = altRed;
            green = altGreen;
            blue = altBlue;
        }
        double cloudTick = ((float)cloudTickCounter + partialTicks);

        float cameraOffsetY = (float)(mc.renderViewEntity.lastTickPosY + (mc.renderViewEntity.posY - mc.renderViewEntity.lastTickPosY) * (double)partialTicks);
        double cameraOffsetX = mc.renderViewEntity.prevPosX + (mc.renderViewEntity.posX - mc.renderViewEntity.prevPosX) * (double)partialTicks + cloudTick * 0.029999999329447746D;
        double cameraOffsetZ = mc.renderViewEntity.prevPosZ + (mc.renderViewEntity.posZ - mc.renderViewEntity.prevPosZ) * (double)partialTicks;
        cameraOffsetX -= MathHelper.floor_double(cameraOffsetX / 2048.0D) * 2048;
        cameraOffsetZ -= MathHelper.floor_double(cameraOffsetZ / 2048.0D) * 2048;

        float renderRadius = 32 * NotFineSettings.Settings.RENDER_DISTANCE_CLOUDS.getValue();
        double uvScale = 0.0005D / NotFineSettings.cloudScale;

        float uvShiftX = (float)(cameraOffsetX * uvScale);
        float uvShiftZ = (float)(cameraOffsetZ * uvScale);

        double cameraRelativeY = theWorld.provider.getCloudHeight() - cameraOffsetY + 0.33F;
        double neg = -renderRadius;
        double pos = renderRadius;

        double startXUv = neg * uvScale + uvShiftX;
        double startZUv = neg * uvScale + uvShiftZ;
        double movedXUv = pos * uvScale + uvShiftX;
        double movedZUv = pos * uvScale + uvShiftZ;

        tessellator.startDrawingQuads();
        tessellator.setColorRGBA_F(red, green, blue, 0.8F);
        tessellator.addVertexWithUV(neg, cameraRelativeY, pos, startXUv, movedZUv);
        tessellator.addVertexWithUV(pos, cameraRelativeY, pos, movedXUv, movedZUv);
        tessellator.addVertexWithUV(pos, cameraRelativeY, neg, movedXUv, startZUv);
        tessellator.addVertexWithUV(neg, cameraRelativeY, neg, startXUv, startZUv);
        tessellator.draw();

        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_CULL_FACE);
    }

    @Shadow @Final
    private static ResourceLocation locationCloudsPng;
    @Shadow @Final
    private TextureManager renderEngine;

    @Shadow private WorldClient theWorld;
    @Shadow private Minecraft mc;
    @Shadow private int cloudTickCounter;

}
