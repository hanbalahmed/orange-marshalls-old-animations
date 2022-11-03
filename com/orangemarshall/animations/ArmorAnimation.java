package com.orangemarshall.animations;

import com.orangemarshall.animations.config.Config;
import com.orangemarshall.animations.util.CustomLayerBipedArmor;
import com.orangemarshall.animations.util.CustomLayerHeldItem;
import com.orangemarshall.animations.util.FieldWrapper;
import com.orangemarshall.animations.util.ModelMethods;
import java.nio.FloatBuffer;
import java.util.Iterator;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.client.renderer.entity.layers.LayerBipedArmor;
import net.minecraft.client.renderer.entity.layers.LayerHeldItem;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.entity.layers.LayerVillagerArmor;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderPlayerEvent.Pre;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;

public class ArmorAnimation {

    private static final DynamicTexture textureBrightness = new DynamicTexture(16, 16);
    private final Logger logger = LogManager.getLogger();
    private float partialTicks = 0.0F;
    private CustomLayerBipedArmor layerBipedArmor = new CustomLayerBipedArmor((RendererLivingEntity) null);
    private CustomLayerHeldItem layerHeldItem = new CustomLayerHeldItem();
    private Config config = Config.getInstance();
    private static final FieldWrapper layerRenderers = new FieldWrapper(Animations.isObfuscated ? "layerRenderers" : "layerRenderers", RendererLivingEntity.class);

    public ArmorAnimation() {
        ArmorAnimation.textureBrightness.updateDynamicTexture();
    }

    @SubscribeEvent
    public void onRenderPlayer(Pre e) {
        this.partialTicks = e.partialRenderTick;
    }

    @SubscribeEvent
    public void onRenderLiving(net.minecraftforge.client.event.RenderLivingEvent.Pre e) {
        if (e.entity instanceof EntityPlayer) {
            if (!e.entity.isPlayerSleeping()) {
                e.setCanceled(true);
                ModelBase mainModel = e.renderer.getMainModel();
                List layerRenderers = (List) ArmorAnimation.layerRenderers.get(e.renderer);
                EntityLivingBase entitylivingbaseIn = e.entity;

                GlStateManager.pushMatrix();
                GlStateManager.disableCull();
                mainModel.swingProgress = this.getSwingProgress(e.entity, this.partialTicks);
                mainModel.isRiding = e.entity.isRiding();
                mainModel.isChild = e.entity.isChild();

                try {
                    float exception = this.interpolateRotation(e.entity.prevRenderYawOffset, e.entity.renderYawOffset, this.partialTicks);
                    float f3 = this.interpolateRotation(e.entity.prevRotationYawHead, e.entity.rotationYawHead, this.partialTicks);
                    float f4 = f3 - exception;
                    float f5;

                    if (e.entity.isRiding() && e.entity.ridingEntity instanceof EntityLivingBase) {
                        EntityLivingBase f9 = (EntityLivingBase) e.entity.ridingEntity;

                        exception = this.interpolateRotation(f9.prevRenderYawOffset, f9.renderYawOffset, this.partialTicks);
                        f4 = f3 - exception;
                        f5 = MathHelper.wrapAngleTo180_float(f4);
                        if (f5 < -85.0F) {
                            f5 = -85.0F;
                        }

                        if (f5 >= 85.0F) {
                            f5 = 85.0F;
                        }

                        exception = f3 - f5;
                        if (f5 * f5 > 2500.0F) {
                            exception += f5 * 0.2F;
                        }
                    }

                    float f91 = e.entity.prevRotationPitch + (e.entity.rotationPitch - e.entity.prevRotationPitch) * this.partialTicks;

                    this.renderLivingAt(e.entity, e.x, e.y, e.z);
                    f5 = this.handleRotationFloat(e.entity, this.partialTicks);
                    this.rotateCorpse(e.entity, f5, exception, this.partialTicks);
                    GlStateManager.enableRescaleNormal();
                    GlStateManager.scale(-1.0F, -1.0F, 1.0F);
                    this.preRenderCallback(e.entity, this.partialTicks);
                    GlStateManager.translate(0.0F, -1.5078125F, 0.0F);
                    float f7 = e.entity.prevLimbSwingAmount + (e.entity.limbSwingAmount - e.entity.prevLimbSwingAmount) * this.partialTicks;
                    float f8 = e.entity.limbSwing - e.entity.limbSwingAmount * (1.0F - this.partialTicks);

                    if (e.entity.isChild()) {
                        f8 *= 3.0F;
                    }

                    if (f7 > 1.0F) {
                        f7 = 1.0F;
                    }

                    GlStateManager.enableAlpha();
                    mainModel.setLivingAnimations(e.entity, f8, f7, this.partialTicks);
                    mainModel.setRotationAngles(f8, f7, f5, f4, f91, 0.0625F, e.entity);
                    boolean flag = this.setDoRenderBrightness(e.entity, this.partialTicks);

                    this.renderModel(e.entity, f8, f7, f5, f4, f91, 0.0625F, mainModel, e.renderer);
                    if (flag) {
                        this.unsetBrightness();
                    }

                    GlStateManager.depthMask(true);
                    if (!(e.entity instanceof EntityPlayer) || !((EntityPlayer) e.entity).isSpectator()) {
                        Iterator iterator = layerRenderers.iterator();

                        while (iterator.hasNext()) {
                            LayerRenderer layerrenderer = (LayerRenderer) iterator.next();
                            boolean redarmor = this.config.redArmor && (layerrenderer.shouldCombineTextures() || layerrenderer.toString().contains("LayerBipedArmor") || layerrenderer.toString().startsWith("bkx@"));
                            boolean flag_2 = this.setBrightness(entitylivingbaseIn, this.partialTicks, redarmor);

                            if (this.config.thirdPersonBlocking) {
                                if (layerrenderer instanceof LayerBipedArmor && !(layerrenderer instanceof LayerVillagerArmor)) {
                                    this.layerBipedArmor.setRenderer(e.renderer);
                                    this.layerBipedArmor.doRenderLayer(e.entity, f8, f7, this.partialTicks, f5, f4, f91, 0.0625F);
                                } else if (layerrenderer instanceof LayerHeldItem) {
                                    this.layerHeldItem.setRenderer(e.renderer);
                                    this.layerHeldItem.doRenderLayer(e.entity, f8, f7, this.partialTicks, f5, f4, f91, 0.0625F);
                                } else {
                                    layerrenderer.doRenderLayer(e.entity, f8, f7, this.partialTicks, f5, f4, f91, 0.0625F);
                                }
                            } else {
                                layerrenderer.doRenderLayer(e.entity, f8, f7, this.partialTicks, f5, f4, f91, 0.0625F);
                            }

                            if (flag_2) {
                                this.unsetBrightness();
                            }
                        }
                    }

                    GlStateManager.disableRescaleNormal();
                } catch (Exception exception) {
                    this.logger.error("Couldn\'t render e.entity", exception);
                }

                GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
                GlStateManager.enableTexture2D();
                GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
                GlStateManager.enableCull();
                GlStateManager.popMatrix();
                e.renderer.renderName(e.entity, e.x, e.y, e.z);
            }
        }
    }

    private float getSwingProgress(EntityLivingBase livingBase, float partialTickTime) {
        return livingBase.getSwingProgress(partialTickTime);
    }

    protected float interpolateRotation(float par1, float par2, float par3) {
        float f3;

        for (f3 = par2 - par1; f3 < -180.0F; f3 += 360.0F) {
            ;
        }

        while (f3 >= 180.0F) {
            f3 -= 360.0F;
        }

        return par1 + par3 * f3;
    }

    protected void renderLivingAt(EntityLivingBase entityLivingBaseIn, double x, double y, double z) {
        GlStateManager.translate((float) x, (float) y, (float) z);
    }

    protected float handleRotationFloat(EntityLivingBase livingBase, float partialTicks) {
        return (float) livingBase.ticksExisted + partialTicks;
    }

    protected void rotateCorpse(EntityLivingBase bat, float p_77043_2_, float p_77043_3_, float partialTicks) {
        GlStateManager.rotate(180.0F - p_77043_3_, 0.0F, 1.0F, 0.0F);
        if (bat.deathTime > 0) {
            float s = ((float) bat.deathTime + partialTicks - 1.0F) / 20.0F * 1.6F;

            s = MathHelper.sqrt_float(s);
            if (s > 1.0F) {
                s = 1.0F;
            }

            GlStateManager.rotate(s * this.getDeathMaxRotation(bat), 0.0F, 0.0F, 1.0F);
        } else {
            String s1 = EnumChatFormatting.getTextWithoutFormattingCodes(bat.getName());

            if (s1 != null && (s1.equals("Dinnerbone") || s1.equals("Grumm")) && !(bat instanceof EntityPlayer) && (!(bat instanceof EntityPlayer) || ((EntityPlayer) bat).isWearing(EnumPlayerModelParts.CAPE))) {
                GlStateManager.translate(0.0F, bat.height + 0.1F, 0.0F);
                GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
            }
        }

    }

    protected float getDeathMaxRotation(EntityLivingBase entityLivingBaseIn) {
        return 90.0F;
    }

    protected void preRenderCallback(EntityLivingBase entitylivingbaseIn, float partialTickTime) {}

    protected boolean setScoreTeamColor(EntityLivingBase entityLivingBaseIn, RendererLivingEntity renderer) {
        int i = 16777215;

        if (entityLivingBaseIn instanceof EntityPlayer) {
            ScorePlayerTeam f1 = (ScorePlayerTeam) entityLivingBaseIn.getTeam();

            if (f1 != null) {
                String f2 = FontRenderer.getFormatFromString(f1.getColorPrefix());

                if (f2.length() >= 2) {
                    i = renderer.getFontRendererFromRenderManager().getColorCode(f2.charAt(1));
                }
            }
        }

        float f11 = (float) (i >> 16 & 255) / 255.0F;
        float f21 = (float) (i >> 8 & 255) / 255.0F;
        float f = (float) (i & 255) / 255.0F;

        GlStateManager.disableLighting();
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
        GlStateManager.color(f11, f21, f, 1.0F);
        GlStateManager.disableTexture2D();
        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.disableTexture2D();
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
        return true;
    }

    protected void renderModel(EntityLivingBase entityIn, float p_77036_2_, float p_77036_3_, float p_77036_4_, float p_77036_5_, float p_77036_6_, float scale, ModelBase mainModel, RendererLivingEntity renderer) {
        boolean flag = !entityIn.isInvisible();
        boolean flag1 = !flag && !entityIn.isInvisibleToPlayer(Minecraft.getMinecraft().thePlayer);

        if (flag || flag1) {
            if (!this.bindEntityTexture(entityIn, renderer)) {
                return;
            }

            if (flag1) {
                GlStateManager.pushMatrix();
                GlStateManager.color(1.0F, 1.0F, 1.0F, 0.15F);
                GlStateManager.depthMask(false);
                GlStateManager.enableBlend();
                GlStateManager.blendFunc(770, 771);
                GlStateManager.alphaFunc(516, 0.003921569F);
            }

            if (this.config.thirdPersonBlocking) {
                if (mainModel instanceof ModelPlayer) {
                    ModelMethods.setRotationAnglesModelPlayer((ModelPlayer) mainModel, p_77036_2_, p_77036_3_, p_77036_4_, p_77036_5_, p_77036_6_, scale, entityIn);
                    ModelMethods.renderModelPlayer((ModelPlayer) mainModel, entityIn, scale);
                } else if (mainModel instanceof ModelBiped) {
                    ModelMethods.setRotationAnglesModelBiped((ModelBiped) mainModel, p_77036_2_, p_77036_3_, p_77036_4_, p_77036_5_, p_77036_6_, scale, entityIn);
                    ModelMethods.renderModelBiped((ModelBiped) mainModel, entityIn, scale);
                } else {
                    mainModel.render(entityIn, p_77036_2_, p_77036_3_, p_77036_4_, p_77036_5_, p_77036_6_, scale);
                }
            } else {
                mainModel.render(entityIn, p_77036_2_, p_77036_3_, p_77036_4_, p_77036_5_, p_77036_6_, scale);
            }

            if (flag1) {
                GlStateManager.disableBlend();
                GlStateManager.alphaFunc(516, 0.1F);
                GlStateManager.popMatrix();
                GlStateManager.depthMask(true);
            }
        }

    }

    protected boolean bindEntityTexture(Entity entity, RendererLivingEntity renderer) {
        ResourceLocation resourcelocation = this.getEntityTexture(entity);

        if (resourcelocation == null) {
            return false;
        } else {
            renderer.bindTexture(resourcelocation);
            return true;
        }
    }

    protected ResourceLocation getEntityTexture(Entity entity) {
        return this.func_180594_a((AbstractClientPlayer) entity);
    }

    protected ResourceLocation func_180594_a(AbstractClientPlayer p_180594_1_) {
        return p_180594_1_.getLocationSkin();
    }

    protected void unsetScoreTeamColor() {
        GlStateManager.enableLighting();
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
        GlStateManager.enableTexture2D();
        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.enableTexture2D();
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
    }

    protected boolean setDoRenderBrightness(EntityLivingBase entityLivingBaseIn, float partialTicks) {
        return this.setBrightness(entityLivingBaseIn, partialTicks, true);
    }

    protected boolean setBrightness(EntityLivingBase entitylivingbaseIn, float partialTicks, boolean combineTextures) {
        float f1 = entitylivingbaseIn.getBrightness(partialTicks);
        int i = this.getColorMultiplier(entitylivingbaseIn, f1, partialTicks);
        boolean flag1 = (i >> 24 & 255) > 0;
        boolean flag2 = entitylivingbaseIn.hurtTime > 0 || entitylivingbaseIn.deathTime > 0;

        if (!flag1 && !flag2) {
            return false;
        } else if (!flag1 && !combineTextures) {
            return false;
        } else {
            FloatBuffer brightnessBuffer = GLAllocation.createDirectFloatBuffer(4);

            GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
            GlStateManager.enableTexture2D();
            GL11.glTexEnvi(8960, 8704, OpenGlHelper.GL_COMBINE);
            GL11.glTexEnvi(8960, OpenGlHelper.GL_COMBINE_RGB, 8448);
            GL11.glTexEnvi(8960, OpenGlHelper.GL_SOURCE0_RGB, OpenGlHelper.defaultTexUnit);
            GL11.glTexEnvi(8960, OpenGlHelper.GL_SOURCE1_RGB, OpenGlHelper.GL_PRIMARY_COLOR);
            GL11.glTexEnvi(8960, OpenGlHelper.GL_OPERAND0_RGB, 768);
            GL11.glTexEnvi(8960, OpenGlHelper.GL_OPERAND1_RGB, 768);
            GL11.glTexEnvi(8960, OpenGlHelper.GL_COMBINE_ALPHA, 7681);
            GL11.glTexEnvi(8960, OpenGlHelper.GL_SOURCE0_ALPHA, OpenGlHelper.defaultTexUnit);
            GL11.glTexEnvi(8960, OpenGlHelper.GL_OPERAND0_ALPHA, 770);
            GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
            GlStateManager.enableTexture2D();
            GL11.glTexEnvi(8960, 8704, OpenGlHelper.GL_COMBINE);
            GL11.glTexEnvi(8960, OpenGlHelper.GL_COMBINE_RGB, OpenGlHelper.GL_INTERPOLATE);
            GL11.glTexEnvi(8960, OpenGlHelper.GL_SOURCE0_RGB, OpenGlHelper.GL_CONSTANT);
            GL11.glTexEnvi(8960, OpenGlHelper.GL_SOURCE1_RGB, OpenGlHelper.GL_PREVIOUS);
            GL11.glTexEnvi(8960, OpenGlHelper.GL_SOURCE2_RGB, OpenGlHelper.GL_CONSTANT);
            GL11.glTexEnvi(8960, OpenGlHelper.GL_OPERAND0_RGB, 768);
            GL11.glTexEnvi(8960, OpenGlHelper.GL_OPERAND1_RGB, 768);
            GL11.glTexEnvi(8960, OpenGlHelper.GL_OPERAND2_RGB, 770);
            GL11.glTexEnvi(8960, OpenGlHelper.GL_COMBINE_ALPHA, 7681);
            GL11.glTexEnvi(8960, OpenGlHelper.GL_SOURCE0_ALPHA, OpenGlHelper.GL_PREVIOUS);
            GL11.glTexEnvi(8960, OpenGlHelper.GL_OPERAND0_ALPHA, 770);
            brightnessBuffer.position(0);
            if (flag2) {
                brightnessBuffer.put(1.0F);
                brightnessBuffer.put(0.0F);
                brightnessBuffer.put(0.0F);
                brightnessBuffer.put(this.config.deepRed ? 0.5F : 0.3F);
            } else {
                float f2 = (float) (i >> 24 & 255) / 255.0F;
                float f3 = (float) (i >> 16 & 255) / 255.0F;
                float f4 = (float) (i >> 8 & 255) / 255.0F;
                float f5 = (float) (i & 255) / 255.0F;

                brightnessBuffer.put(f3);
                brightnessBuffer.put(f4);
                brightnessBuffer.put(f5);
                brightnessBuffer.put(1.0F - f2);
            }

            brightnessBuffer.flip();
            GL11.glTexEnv(8960, 8705, brightnessBuffer);
            GlStateManager.setActiveTexture(OpenGlHelper.GL_TEXTURE2);
            GlStateManager.enableTexture2D();
            GlStateManager.bindTexture(ArmorAnimation.textureBrightness.getGlTextureId());
            GL11.glTexEnvi(8960, 8704, OpenGlHelper.GL_COMBINE);
            GL11.glTexEnvi(8960, OpenGlHelper.GL_COMBINE_RGB, 8448);
            GL11.glTexEnvi(8960, OpenGlHelper.GL_SOURCE0_RGB, OpenGlHelper.GL_PREVIOUS);
            GL11.glTexEnvi(8960, OpenGlHelper.GL_SOURCE1_RGB, OpenGlHelper.lightmapTexUnit);
            GL11.glTexEnvi(8960, OpenGlHelper.GL_OPERAND0_RGB, 768);
            GL11.glTexEnvi(8960, OpenGlHelper.GL_OPERAND1_RGB, 768);
            GL11.glTexEnvi(8960, OpenGlHelper.GL_COMBINE_ALPHA, 7681);
            GL11.glTexEnvi(8960, OpenGlHelper.GL_SOURCE0_ALPHA, OpenGlHelper.GL_PREVIOUS);
            GL11.glTexEnvi(8960, OpenGlHelper.GL_OPERAND0_ALPHA, 770);
            GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
            return true;
        }
    }

    protected int getColorMultiplier(EntityLivingBase entitylivingbaseIn, float lightBrightness, float partialTickTime) {
        return 0;
    }

    protected void unsetBrightness() {
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
        GlStateManager.enableTexture2D();
        GL11.glTexEnvi(8960, 8704, OpenGlHelper.GL_COMBINE);
        GL11.glTexEnvi(8960, OpenGlHelper.GL_COMBINE_RGB, 8448);
        GL11.glTexEnvi(8960, OpenGlHelper.GL_SOURCE0_RGB, OpenGlHelper.defaultTexUnit);
        GL11.glTexEnvi(8960, OpenGlHelper.GL_SOURCE1_RGB, OpenGlHelper.GL_PRIMARY_COLOR);
        GL11.glTexEnvi(8960, OpenGlHelper.GL_OPERAND0_RGB, 768);
        GL11.glTexEnvi(8960, OpenGlHelper.GL_OPERAND1_RGB, 768);
        GL11.glTexEnvi(8960, OpenGlHelper.GL_COMBINE_ALPHA, 8448);
        GL11.glTexEnvi(8960, OpenGlHelper.GL_SOURCE0_ALPHA, OpenGlHelper.defaultTexUnit);
        GL11.glTexEnvi(8960, OpenGlHelper.GL_SOURCE1_ALPHA, OpenGlHelper.GL_PRIMARY_COLOR);
        GL11.glTexEnvi(8960, OpenGlHelper.GL_OPERAND0_ALPHA, 770);
        GL11.glTexEnvi(8960, OpenGlHelper.GL_OPERAND1_ALPHA, 770);
        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GL11.glTexEnvi(8960, 8704, OpenGlHelper.GL_COMBINE);
        GL11.glTexEnvi(8960, OpenGlHelper.GL_COMBINE_RGB, 8448);
        GL11.glTexEnvi(8960, OpenGlHelper.GL_OPERAND0_RGB, 768);
        GL11.glTexEnvi(8960, OpenGlHelper.GL_OPERAND1_RGB, 768);
        GL11.glTexEnvi(8960, OpenGlHelper.GL_SOURCE0_RGB, 5890);
        GL11.glTexEnvi(8960, OpenGlHelper.GL_SOURCE1_RGB, OpenGlHelper.GL_PREVIOUS);
        GL11.glTexEnvi(8960, OpenGlHelper.GL_COMBINE_ALPHA, 8448);
        GL11.glTexEnvi(8960, OpenGlHelper.GL_OPERAND0_ALPHA, 770);
        GL11.glTexEnvi(8960, OpenGlHelper.GL_SOURCE0_ALPHA, 5890);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.setActiveTexture(OpenGlHelper.GL_TEXTURE2);
        GlStateManager.disableTexture2D();
        GlStateManager.bindTexture(0);
        GL11.glTexEnvi(8960, 8704, OpenGlHelper.GL_COMBINE);
        GL11.glTexEnvi(8960, OpenGlHelper.GL_COMBINE_RGB, 8448);
        GL11.glTexEnvi(8960, OpenGlHelper.GL_OPERAND0_RGB, 768);
        GL11.glTexEnvi(8960, OpenGlHelper.GL_OPERAND1_RGB, 768);
        GL11.glTexEnvi(8960, OpenGlHelper.GL_SOURCE0_RGB, 5890);
        GL11.glTexEnvi(8960, OpenGlHelper.GL_SOURCE1_RGB, OpenGlHelper.GL_PREVIOUS);
        GL11.glTexEnvi(8960, OpenGlHelper.GL_COMBINE_ALPHA, 8448);
        GL11.glTexEnvi(8960, OpenGlHelper.GL_OPERAND0_ALPHA, 770);
        GL11.glTexEnvi(8960, OpenGlHelper.GL_SOURCE0_ALPHA, 5890);
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
    }
}
