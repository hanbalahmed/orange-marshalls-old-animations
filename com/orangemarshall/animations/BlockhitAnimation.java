package com.orangemarshall.animations;

import com.orangemarshall.animations.config.Config;
import com.orangemarshall.animations.util.FieldWrapper;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.ItemModelMesher;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFishingRod;
import net.minecraft.item.ItemMap;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.world.storage.MapData;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.model.pipeline.LightUtil;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.Project;

public class BlockhitAnimation {

    private static final FieldWrapper prevEquippedProgress = new FieldWrapper(Animations.isObfuscated ? "prevEquippedProgress" : "prevEquippedProgress", ItemRenderer.class);
    private static final FieldWrapper equippedProgress = new FieldWrapper(Animations.isObfuscated ? "equippedProgress" : "equippedProgress", ItemRenderer.class);
    private static final FieldWrapper fovModifierHandPrev = new FieldWrapper(Animations.isObfuscated ? "fovModifierHandPrev" : "fovModifierHandPrev", EntityRenderer.class);
    private static final FieldWrapper fovModifierHand = new FieldWrapper(Animations.isObfuscated ? "fovModifierHand" : "fovModifierHand", EntityRenderer.class);
    private static final FieldWrapper cloudFog = new FieldWrapper(Animations.isObfuscated ? "cloudFog" : "cloudFog", EntityRenderer.class);
    private static final FieldWrapper modelMesher = new FieldWrapper(Animations.isObfuscated ? "itemModelMesher" : "itemModelMesher", RenderItem.class);
    private int farPlaneDistance;
    private ItemStack itemToRender;
    private Minecraft mc = Minecraft.getMinecraft();
    private GameSettings gameSettings;
    private EntityRenderer entityRenderer;
    private ItemRenderer itemRenderer;
    private RenderManager renderManager;
    private ItemModelMesher itemModelMesher;
    private boolean isOF;
    private boolean init;
    private KeyBinding zoom;
    private Config config;
    private static final ResourceLocation RES_ITEM_GLINT = new ResourceLocation("textures/misc/enchanted_item_glint.png");

    public BlockhitAnimation() {
        this.gameSettings = this.mc.gameSettings;
        this.isOF = false;
        this.init = false;
        this.config = Config.getInstance();
    }

    private void init() {
        if (!this.init) {
            this.init = true;
            KeyBinding[] k = this.gameSettings.keyBindings;

            for (int i = 0; i < k.length; ++i) {
                if (k[i].getKeyDescription().equals("Zoom") || k[i].getKeyDescription().equals("of.key.zoom")) {
                    this.isOF = true;
                    this.zoom = k[i];
                    System.out.println("Found Zoom key");
                    break;
                }
            }

            this.entityRenderer = this.mc.entityRenderer;
            this.itemRenderer = this.mc.getItemRenderer();
            this.renderManager = this.mc.getRenderManager();
        }

        this.itemModelMesher = (ItemModelMesher) BlockhitAnimation.modelMesher.get(this.mc.getRenderItem());
        this.farPlaneDistance = this.gameSettings.renderDistanceChunks << 4;
        this.itemToRender = this.mc.thePlayer.getCurrentEquippedItem();
    }

    private boolean isZoomed() {
        return this.isOF && this.zoom.isKeyDown();
    }

    @SubscribeEvent
    public void onRenderFirstHand(RenderHandEvent e) {
        this.init();
        if (!this.isZoomed() && !e.isCanceled() && this.mc.thePlayer.getHeldItem() != null) {
            e.setCanceled(true);
            this.attemptSwing();
            this.renderHand(e.partialTicks, e.renderPass);
            this.renderWorldDirections(e.partialTicks);
        }
    }

    private void renderHand(float partialTicks, int renderPass) {
        GlStateManager.matrixMode(5889);
        GlStateManager.loadIdentity();
        float f1 = 0.07F;

        if (this.gameSettings.anaglyph) {
            GlStateManager.translate((float) (-((renderPass << 1) - 1)) * f1, 0.0F, 0.0F);
        }

        Project.gluPerspective(this.getFOVModifier(partialTicks, false), (float) this.mc.displayWidth / (float) this.mc.displayHeight, 0.05F, (float) (this.farPlaneDistance << 1));
        GlStateManager.matrixMode(5888);
        GlStateManager.loadIdentity();
        if (this.gameSettings.anaglyph) {
            GlStateManager.translate((float) ((renderPass << 1) - 1) * 0.1F, 0.0F, 0.0F);
        }

        GlStateManager.pushMatrix();
        this.hurtCameraEffect(partialTicks);
        if (this.gameSettings.viewBobbing) {
            this.setupViewBobbing(partialTicks);
        }

        boolean flag = this.mc.getRenderViewEntity() instanceof EntityLivingBase && ((EntityLivingBase) this.mc.getRenderViewEntity()).isPlayerSleeping();

        if (this.gameSettings.thirdPersonView == 0 && !flag && !this.gameSettings.hideGUI && !this.mc.playerController.isSpectator()) {
            this.entityRenderer.enableLightmap();
            this.renderItemInFirstPerson(partialTicks);
            this.entityRenderer.disableLightmap();
        }

        GlStateManager.popMatrix();
        if (this.gameSettings.thirdPersonView == 0 && !flag) {
            this.itemRenderer.renderOverlays(partialTicks);
            this.hurtCameraEffect(partialTicks);
        }

        if (this.gameSettings.viewBobbing) {
            this.setupViewBobbing(partialTicks);
        }

    }

    private void renderWorldDirections(float partialTicks) {
        if (this.gameSettings.showDebugInfo && !this.gameSettings.hideGUI && !this.mc.thePlayer.hasReducedDebug() && !this.gameSettings.reducedDebugInfo) {
            Entity entity = this.mc.getRenderViewEntity();

            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            GL11.glLineWidth(1.0F);
            GlStateManager.disableTexture2D();
            GlStateManager.depthMask(false);
            GlStateManager.pushMatrix();
            GlStateManager.matrixMode(5888);
            GlStateManager.loadIdentity();
            this.orientCamera(partialTicks);
            GlStateManager.translate(0.0F, entity.getEyeHeight(), 0.0F);
            RenderGlobal.drawOutlinedBoundingBox(new AxisAlignedBB(0.0D, 0.0D, 0.0D, 0.005D, 1.0E-4D, 1.0E-4D), 255, 0, 0, 255);
            RenderGlobal.drawOutlinedBoundingBox(new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0E-4D, 1.0E-4D, 0.005D), 0, 0, 255, 255);
            RenderGlobal.drawOutlinedBoundingBox(new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0E-4D, 0.0033D, 1.0E-4D), 0, 255, 0, 255);
            GlStateManager.popMatrix();
            GlStateManager.depthMask(true);
            GlStateManager.enableTexture2D();
            GlStateManager.disableBlend();
        }

    }

    private void orientCamera(float partialTicks) {
        Entity entity = this.mc.getRenderViewEntity();
        float f1 = entity.getEyeHeight();
        double d0 = entity.prevPosX + (entity.posX - entity.prevPosX) * (double) partialTicks;
        double d1 = entity.prevPosY + (entity.posY - entity.prevPosY) * (double) partialTicks + (double) f1;
        double d2 = entity.prevPosZ + (entity.posZ - entity.prevPosZ) * (double) partialTicks;

        if (entity instanceof EntityLivingBase && ((EntityLivingBase) entity).isPlayerSleeping()) {
            f1 = (float) ((double) f1 + 1.0D);
            GlStateManager.translate(0.0F, 0.3F, 0.0F);
            if (!this.gameSettings.debugCamEnable) {
                BlockPos blockpos = new BlockPos(entity);
                IBlockState iblockstate = this.mc.theWorld.getBlockState(blockpos);

                ForgeHooksClient.orientBedCamera(this.mc.theWorld, blockpos, iblockstate, entity);
                GlStateManager.rotate(entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partialTicks + 180.0F, 0.0F, -1.0F, 0.0F);
                GlStateManager.rotate(entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks, -1.0F, 0.0F, 0.0F);
            }
        } else if (this.gameSettings.thirdPersonView > 0) {
            double entityanimal = 4.0D;

            if (this.gameSettings.debugCamEnable) {
                GlStateManager.translate(0.0F, 0.0F, (float) (-entityanimal));
            } else {
                float f2 = entity.rotationYaw;
                float f3 = entity.rotationPitch;

                if (this.gameSettings.thirdPersonView == 2) {
                    f3 += 180.0F;
                }

                double d4 = (double) (-MathHelper.sin(f2 / 180.0F * 3.1415927F) * MathHelper.cos(f3 / 180.0F * 3.1415927F)) * entityanimal;
                double d5 = (double) (MathHelper.cos(f2 / 180.0F * 3.1415927F) * MathHelper.cos(f3 / 180.0F * 3.1415927F)) * entityanimal;
                double d6 = (double) (-MathHelper.sin(f3 / 180.0F * 3.1415927F)) * entityanimal;

                for (int i = 0; i < 8; ++i) {
                    float f4 = (float) ((i & 1) * 2 - 1);
                    float f5 = (float) ((i >> 1 & 1) * 2 - 1);
                    float f6 = (float) ((i >> 2 & 1) * 2 - 1);

                    f4 *= 0.1F;
                    f5 *= 0.1F;
                    f6 *= 0.1F;
                    MovingObjectPosition movingobjectposition = this.mc.theWorld.rayTraceBlocks(new Vec3(d0 + (double) f4, d1 + (double) f5, d2 + (double) f6), new Vec3(d0 - d4 + (double) f4 + (double) f6, d1 - d6 + (double) f5, d2 - d5 + (double) f6));

                    if (movingobjectposition != null) {
                        double d7 = movingobjectposition.hitVec.distanceTo(new Vec3(d0, d1, d2));

                        if (d7 < entityanimal) {
                            entityanimal = d7;
                        }
                    }
                }

                if (this.gameSettings.thirdPersonView == 2) {
                    GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
                }

                GlStateManager.rotate(entity.rotationPitch - f3, 1.0F, 0.0F, 0.0F);
                GlStateManager.rotate(entity.rotationYaw - f2, 0.0F, 1.0F, 0.0F);
                GlStateManager.translate(0.0F, 0.0F, (float) (-entityanimal));
                GlStateManager.rotate(f2 - entity.rotationYaw, 0.0F, 1.0F, 0.0F);
                GlStateManager.rotate(f3 - entity.rotationPitch, 1.0F, 0.0F, 0.0F);
            }
        } else {
            GlStateManager.translate(0.0F, 0.0F, -0.1F);
        }

        if (!this.gameSettings.debugCamEnable) {
            GlStateManager.rotate(entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks, 1.0F, 0.0F, 0.0F);
            if (entity instanceof EntityAnimal) {
                EntityAnimal entityanimal = (EntityAnimal) entity;

                GlStateManager.rotate(entityanimal.prevRotationYawHead + (entityanimal.rotationYawHead - entityanimal.prevRotationYawHead) * partialTicks + 180.0F, 0.0F, 1.0F, 0.0F);
            } else {
                GlStateManager.rotate(entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partialTicks + 180.0F, 0.0F, 1.0F, 0.0F);
            }
        }

        GlStateManager.translate(0.0F, -f1, 0.0F);
        d0 = entity.prevPosX + (entity.posX - entity.prevPosX) * (double) partialTicks;
        d1 = entity.prevPosY + (entity.posY - entity.prevPosY) * (double) partialTicks + (double) f1;
        d2 = entity.prevPosZ + (entity.posZ - entity.prevPosZ) * (double) partialTicks;
        BlockhitAnimation.cloudFog.set(this.entityRenderer, Boolean.valueOf(this.mc.renderGlobal.hasCloudFog(d0, d1, d2, partialTicks)));
    }

    private float getFOVModifier(float partialTicks, boolean useFOVSetting) {
        Entity entity = this.mc.getRenderViewEntity();
        float f1 = 70.0F;
        float block;

        if (useFOVSetting) {
            f1 = this.gameSettings.fovSetting;
            block = ((Float) BlockhitAnimation.fovModifierHand.get(this.entityRenderer)).floatValue();
            float fovModifierHandPrev = ((Float) BlockhitAnimation.fovModifierHandPrev.get(this.entityRenderer)).floatValue();

            f1 *= fovModifierHandPrev + (block - fovModifierHandPrev) * partialTicks;
        }

        if (entity instanceof EntityLivingBase && ((EntityLivingBase) entity).getHealth() <= 0.0F) {
            block = (float) ((EntityLivingBase) entity).deathTime + partialTicks;
            f1 /= (1.0F - 500.0F / (block + 500.0F)) * 2.0F + 1.0F;
        }

        Block block1 = ActiveRenderInfo.getBlockAtEntityViewpoint(this.mc.theWorld, entity, partialTicks);

        if (block1.getMaterial() == Material.water) {
            f1 = f1 * 60.0F / 70.0F;
        }

        return f1;
    }

    private void hurtCameraEffect(float partialTicks) {
        if (this.mc.getRenderViewEntity() instanceof EntityLivingBase) {
            EntityLivingBase entitylivingbase = (EntityLivingBase) this.mc.getRenderViewEntity();
            float f1 = (float) entitylivingbase.hurtTime - partialTicks;
            float f2;

            if (entitylivingbase.getHealth() <= 0.0F) {
                f2 = (float) entitylivingbase.deathTime + partialTicks;
                GlStateManager.rotate(40.0F - 8000.0F / (f2 + 200.0F), 0.0F, 0.0F, 1.0F);
            }

            if (f1 < 0.0F) {
                return;
            }

            f1 /= (float) entitylivingbase.maxHurtTime;
            f1 = MathHelper.sin(f1 * f1 * f1 * f1 * 3.1415927F);
            f2 = entitylivingbase.attackedAtYaw;
            GlStateManager.rotate(-f2, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(-f1 * 14.0F, 0.0F, 0.0F, 1.0F);
            GlStateManager.rotate(f2, 0.0F, 1.0F, 0.0F);
        }

    }

    private void setupViewBobbing(float partialTicks) {
        if (this.mc.getRenderViewEntity() instanceof EntityPlayer) {
            EntityPlayer entityplayer = (EntityPlayer) this.mc.getRenderViewEntity();
            float f1 = entityplayer.distanceWalkedModified - entityplayer.prevDistanceWalkedModified;
            float f2 = -(entityplayer.distanceWalkedModified + f1 * partialTicks);
            float f3 = entityplayer.prevCameraYaw + (entityplayer.cameraYaw - entityplayer.prevCameraYaw) * partialTicks;
            float f4 = entityplayer.prevCameraPitch + (entityplayer.cameraPitch - entityplayer.prevCameraPitch) * partialTicks;

            GlStateManager.translate(MathHelper.sin(f2 * 3.1415927F) * f3 * 0.5F, -Math.abs(MathHelper.cos(f2 * 3.1415927F) * f3), 0.0F);
            GlStateManager.rotate(MathHelper.sin(f2 * 3.1415927F) * f3 * 3.0F, 0.0F, 0.0F, 1.0F);
            GlStateManager.rotate(Math.abs(MathHelper.cos(f2 * 3.1415927F - 0.2F) * f3) * 5.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotate(f4, 1.0F, 0.0F, 0.0F);
        }

    }

    public void renderItemInFirstPerson(float partialTicks) {
        float equippedProgress = ((Float) BlockhitAnimation.equippedProgress.get(this.itemRenderer)).floatValue();
        float prevEquippedProgress = ((Float) BlockhitAnimation.prevEquippedProgress.get(this.itemRenderer)).floatValue();
        float f1 = 1.0F - (prevEquippedProgress + (equippedProgress - prevEquippedProgress) * partialTicks);
        EntityPlayerSP entityplayersp = this.mc.thePlayer;
        float f2 = entityplayersp.getSwingProgress(partialTicks);
        float f3 = entityplayersp.prevRotationPitch + (entityplayersp.rotationPitch - entityplayersp.prevRotationPitch) * partialTicks;
        float f4 = entityplayersp.prevRotationYaw + (entityplayersp.rotationYaw - entityplayersp.prevRotationYaw) * partialTicks;

        this.rotateArroundXAndY(f3, f4);
        this.setLightMapFromPlayer(entityplayersp);
        this.rotateWithPlayerRotations(entityplayersp, partialTicks);
        GlStateManager.enableRescaleNormal();
        GlStateManager.pushMatrix();
        if (this.itemToRender != null) {
            boolean rod = this.itemToRender.getItem() instanceof ItemFishingRod && this.config.oldRod;

            if (this.itemToRender.getItem() instanceof ItemMap) {
                this.renderItemMap(entityplayersp, f3, f1, f2);
            } else if (entityplayersp.getItemInUseCount() > 0) {
                EnumAction enumaction = this.itemToRender.getItemUseAction();

                switch (enumaction) {
                case NONE:
                    this.transformFirstPersonItem(f1, 0.0F, rod);
                    break;

                case EAT:
                case DRINK:
                    f2 = this.config.punching ? f2 : 0.0F;
                    this.performDrinking(entityplayersp, partialTicks);
                    this.transformFirstPersonItem(f1, f2, rod);
                    break;

                case BLOCK:
                    f2 = this.config.blockhit ? f2 : 0.0F;
                    this.transformFirstPersonItem(f1, f2, rod);
                    this.doBlockTransformations();
                    break;

                case BOW:
                    f2 = this.config.punching ? f2 : f2;
                    this.transformFirstPersonItem(f1, f2, rod);
                    this.doBowTransformations(partialTicks, entityplayersp);
                }
            } else {
                this.doItemUsedTransformations(f2);
                this.transformFirstPersonItem(f1, f2, rod);
            }

            this.renderItem(entityplayersp, this.itemToRender, TransformType.FIRST_PERSON);
        } else if (!entityplayersp.isInvisible()) {
            this.renderPlayerArm(entityplayersp, f1, f2);
        }

        GlStateManager.popMatrix();
        GlStateManager.disableRescaleNormal();
        RenderHelper.disableStandardItemLighting();
    }

    public void swingItem(EntityPlayerSP entityplayersp) {
        int swingAnimationEnd = entityplayersp.isPotionActive(Potion.digSpeed) ? 6 - (1 + entityplayersp.getActivePotionEffect(Potion.digSpeed).getAmplifier()) * 1 : (entityplayersp.isPotionActive(Potion.digSlowdown) ? 6 + (1 + entityplayersp.getActivePotionEffect(Potion.digSlowdown).getAmplifier()) * 2 : 6);

        if (!entityplayersp.isSwingInProgress || entityplayersp.swingProgressInt >= swingAnimationEnd / 2 || entityplayersp.swingProgressInt < 0) {
            entityplayersp.swingProgressInt = -1;
            entityplayersp.isSwingInProgress = true;
        }

    }

    private static void enableStencil() {
        GL11.glEnable(2960);
        GL11.glClear(1024);
        GL11.glStencilFunc(519, 1, 255);
        GL11.glDepthRange(-0.5D, 0.5D);
    }

    private static void disableStencil() {
        GL11.glDepthRange(0.0D, 1.0D);
        GL11.glDisable(2960);
    }

    public void renderItem(EntityLivingBase entityIn, ItemStack heldStack, TransformType transform) {
        GL11.glDisable(2929);
        if (heldStack != null) {
            Item item = heldStack.getItem();
            Block block = Block.getBlockFromItem(item);

            GlStateManager.pushMatrix();
            if (this.shouldRenderItemIn3D(heldStack)) {
                GlStateManager.scale(2.0F, 2.0F, 2.0F);
                if (this.isBlockTranslucent(block)) {
                    GlStateManager.depthMask(false);
                }
            }

            this.renderItemModelForEntity(heldStack, entityIn, transform);
            if (this.isBlockTranslucent(block)) {
                GlStateManager.depthMask(true);
            }

            GlStateManager.popMatrix();
        }

        GL11.glEnable(2929);
    }

    public boolean shouldRenderItemIn3D(ItemStack stack) {
        IBakedModel ibakedmodel = Minecraft.getMinecraft().getRenderItem().getItemModelMesher().getItemModel(stack);

        return ibakedmodel == null ? false : ibakedmodel.isGui3d();
    }

    public void renderItemModelForEntity(ItemStack stack, EntityLivingBase entityToRenderFor, TransformType cameraTransformType) {
        if (stack != null && entityToRenderFor != null) {
            IBakedModel ibakedmodel = this.itemModelMesher.getItemModel(stack);

            if (entityToRenderFor instanceof EntityPlayer) {
                EntityPlayer entityplayer = (EntityPlayer) entityToRenderFor;
                Item item = stack.getItem();
                ModelResourceLocation modelresourcelocation = null;

                if (item == Items.fishing_rod && entityplayer.fishEntity != null) {
                    modelresourcelocation = new ModelResourceLocation("fishing_rod_cast", "inventory");
                } else if (item == Items.bow && entityplayer.getItemInUse() != null) {
                    int i = stack.getMaxItemUseDuration() - entityplayer.getItemInUseCount();

                    if (i >= 18) {
                        modelresourcelocation = new ModelResourceLocation("bow_pulling_2", "inventory");
                    } else if (i > 13) {
                        modelresourcelocation = new ModelResourceLocation("bow_pulling_1", "inventory");
                    } else if (i > 0) {
                        modelresourcelocation = new ModelResourceLocation("bow_pulling_0", "inventory");
                    }
                } else {
                    modelresourcelocation = item.getModel(stack, entityplayer, entityplayer.getItemInUseCount());
                }

                if (modelresourcelocation != null) {
                    ibakedmodel = this.itemModelMesher.getModelManager().getModel(modelresourcelocation);
                }
            }

            this.renderItemModelTransform(stack, ibakedmodel, cameraTransformType);
        }

    }

    protected void renderItemModelTransform(ItemStack stack, IBakedModel model, TransformType cameraTransformType) {
        TextureManager textureManager = this.mc.getTextureManager();

        textureManager.bindTexture(TextureMap.locationBlocksTexture);
        textureManager.getTexture(TextureMap.locationBlocksTexture).setBlurMipmap(false, false);
        this.preTransform(stack);
        GlStateManager.enableRescaleNormal();
        GlStateManager.alphaFunc(516, 0.1F);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.pushMatrix();
        model = ForgeHooksClient.handleCameraTransforms(model, cameraTransformType);
        this.renderItem(stack, model);
        GlStateManager.cullFace(1029);
        GlStateManager.popMatrix();
        GlStateManager.disableRescaleNormal();
        GlStateManager.disableBlend();
        textureManager.bindTexture(TextureMap.locationBlocksTexture);
        textureManager.getTexture(TextureMap.locationBlocksTexture).restoreLastBlurMipmap();
    }

    public void renderItem(ItemStack stack, IBakedModel model) {
        GL11.glEnable(2929);
        if (stack != null) {
            GlStateManager.pushMatrix();
            GlStateManager.scale(0.5F, 0.5F, 0.5F);
            if (model.isBuiltInRenderer()) {
                GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
                GlStateManager.translate(-0.5F, -0.5F, -0.5F);
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                GlStateManager.enableRescaleNormal();
                TileEntityItemStackRenderer.instance.renderByItem(stack);
            } else {
                GlStateManager.translate(-0.5F, -0.5F, -0.5F);
                this.renderModel(model, stack);
                if (stack.hasEffect()) {
                    if (this.config.oldEnchantGlint) {
                        this.renderOldEffect(model);
                    } else {
                        this.renderEffect(model);
                    }
                }
            }

            GlStateManager.popMatrix();
        }

        GL11.glDisable(2929);
    }

    private void renderModel(IBakedModel model, ItemStack stack) {
        this.renderModel(model, -1, stack);
    }

    private void renderOldEffect(IBakedModel model) {
        GlStateManager.depthMask(false);
        GlStateManager.depthFunc(516);
        GlStateManager.disableLighting();
        GlStateManager.blendFunc(768, 1);
        this.renderManager.renderEngine.bindTexture(BlockhitAnimation.RES_ITEM_GLINT);
        GlStateManager.matrixMode(5890);
        GL11.glPushMatrix();
        GL11.glEnable(3042);
        GL11.glBlendFunc(768, 1);
        float f = 0.76F;

        GL11.glColor4f(0.5F * f, 0.25F * f, 0.8F * f, 1.0F);
        GL11.glPushMatrix();
        float f1 = 0.125F;

        GL11.glScalef(f1, f1, f1);
        float f2 = (float) (Minecraft.getSystemTime() % 3000L) / 3000.0F * 8.0F;

        GL11.glTranslatef(f2, 0.0F, 0.0F);
        GL11.glRotatef(-50.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.depthFunc(514);
        this.renderModel(model, -8372020);
        GlStateManager.depthFunc(516);
        GL11.glPopMatrix();
        GL11.glPushMatrix();
        GL11.glScalef(f1, f1, f1);
        f2 = (float) (Minecraft.getSystemTime() % 4873L) / 4873.0F * 8.0F;
        GL11.glTranslatef(-f2, 0.0F, 0.0F);
        GL11.glRotatef(10.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.depthFunc(514);
        this.renderModel(model, -8372020);
        GlStateManager.depthFunc(516);
        GL11.glPopMatrix();
        GL11.glPopMatrix();
        GlStateManager.matrixMode(5888);
        GlStateManager.blendFunc(770, 771);
        GlStateManager.enableLighting();
        GlStateManager.depthFunc(515);
        GlStateManager.depthMask(true);
        Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.locationBlocksTexture);
    }

    private void renderEffect(IBakedModel model) {
        GlStateManager.depthMask(false);
        GlStateManager.depthFunc(516);
        GlStateManager.disableLighting();
        GlStateManager.blendFunc(768, 1);
        Minecraft.getMinecraft().getTextureManager().bindTexture(BlockhitAnimation.RES_ITEM_GLINT);
        GlStateManager.matrixMode(5890);
        GlStateManager.pushMatrix();
        GlStateManager.scale(8.0F, 8.0F, 8.0F);
        float f = (float) (Minecraft.getSystemTime() % 3000L) / 3000.0F / 8.0F;

        GlStateManager.translate(f, 0.0F, 0.0F);
        GlStateManager.rotate(-50.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.depthFunc(514);
        this.renderModel(model, -8372020);
        GlStateManager.depthFunc(516);
        GlStateManager.popMatrix();
        GlStateManager.pushMatrix();
        GlStateManager.scale(8.0F, 8.0F, 8.0F);
        float f1 = (float) (Minecraft.getSystemTime() % 4873L) / 4873.0F / 8.0F;

        GlStateManager.translate(-f1, 0.0F, 0.0F);
        GlStateManager.rotate(10.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.depthFunc(514);
        this.renderModel(model, -8372020);
        GlStateManager.depthFunc(516);
        GlStateManager.popMatrix();
        GlStateManager.matrixMode(5888);
        GlStateManager.blendFunc(770, 771);
        GlStateManager.enableLighting();
        GlStateManager.depthFunc(515);
        GlStateManager.depthMask(true);
        Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.locationBlocksTexture);
    }

    private void renderModel(IBakedModel model, int color) {
        this.renderModel(model, color, (ItemStack) null);
    }

    private void renderModel(IBakedModel model, int color, ItemStack stack) {
        enableStencil();
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();

        worldrenderer.begin(7, DefaultVertexFormats.ITEM);
        EnumFacing[] aenumfacing = EnumFacing.values();
        int i = aenumfacing.length;

        for (int j = 0; j < i; ++j) {
            EnumFacing enumfacing = aenumfacing[j];

            this.renderQuads(worldrenderer, model.getFaceQuads(enumfacing), color, stack);
        }

        this.renderQuads(worldrenderer, model.getGeneralQuads(), color, stack);
        tessellator.draw();
        disableStencil();
    }

    private void renderQuads(WorldRenderer renderer, List quads, int color, ItemStack stack) {
        boolean flag = color == -1 && stack != null;
        int i = 0;

        for (int j = quads.size(); i < j; ++i) {
            BakedQuad bakedquad = (BakedQuad) quads.get(i);
            int k = color;

            if (flag && bakedquad.hasTintIndex()) {
                k = stack.getItem().getColorFromItemStack(stack, bakedquad.getTintIndex());
                if (EntityRenderer.anaglyphEnable) {
                    k = TextureUtil.anaglyphColor(k);
                }

                k |= -16777216;
            }

            LightUtil.renderQuadColor(renderer, bakedquad, k);
        }

    }

    private void preTransform(ItemStack stack) {
        IBakedModel ibakedmodel = Minecraft.getMinecraft().getRenderItem().getItemModelMesher().getItemModel(stack);
        Item item = stack.getItem();

        if (item != null) {
            boolean flag = ibakedmodel.isGui3d();

            if (!flag) {
                GlStateManager.scale(2.0F, 2.0F, 2.0F);
            }

            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        }

    }

    private boolean isBlockTranslucent(Block blockIn) {
        return blockIn != null && blockIn.getBlockLayer() == EnumWorldBlockLayer.TRANSLUCENT;
    }

    private void renderItemMap(AbstractClientPlayer clientPlayer, float pitch, float equipmentProgress, float swingProgress) {
        float f3 = -0.4F * MathHelper.sin(MathHelper.sqrt_float(swingProgress) * 3.1415927F);
        float f4 = 0.2F * MathHelper.sin(MathHelper.sqrt_float(swingProgress) * 3.1415927F * 2.0F);
        float f5 = -0.2F * MathHelper.sin(swingProgress * 3.1415927F);

        GlStateManager.translate(f3, f4, f5);
        float f6 = this.getMapAngleFromPitch(pitch);

        GlStateManager.translate(0.0F, 0.04F, -0.72F);
        GlStateManager.translate(0.0F, equipmentProgress * -1.2F, 0.0F);
        GlStateManager.translate(0.0F, f6 * -0.5F, 0.0F);
        GlStateManager.rotate(90.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(f6 * -85.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.rotate(0.0F, 1.0F, 0.0F, 0.0F);
        this.renderPlayerArms(clientPlayer);
        float f7 = MathHelper.sin(swingProgress * swingProgress * 3.1415927F);
        float f8 = MathHelper.sin(MathHelper.sqrt_float(swingProgress) * 3.1415927F);

        GlStateManager.rotate(f7 * -20.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(f8 * -20.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.rotate(f8 * -80.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.scale(0.38F, 0.38F, 0.38F);
        GlStateManager.rotate(90.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.rotate(0.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.translate(-1.0F, -1.0F, 0.0F);
        GlStateManager.scale(0.015625F, 0.015625F, 0.015625F);
        this.mc.getTextureManager().bindTexture(new ResourceLocation("textures/map/map_background.png"));
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();

        GL11.glNormal3f(0.0F, 0.0F, -1.0F);
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
        worldrenderer.pos(-7.0D, 135.0D, 0.0D).tex(0.0D, 1.0D).endVertex();
        worldrenderer.pos(135.0D, 135.0D, 0.0D).tex(1.0D, 1.0D).endVertex();
        worldrenderer.pos(135.0D, -7.0D, 0.0D).tex(1.0D, 0.0D).endVertex();
        worldrenderer.pos(-7.0D, -7.0D, 0.0D).tex(0.0D, 0.0D).endVertex();
        tessellator.draw();
        MapData mapdata = Items.filled_map.getMapData(this.itemToRender, this.mc.theWorld);

        if (mapdata != null) {
            this.mc.entityRenderer.getMapItemRenderer().renderMap(mapdata, false);
        }

    }

    private void renderPlayerArms(AbstractClientPlayer clientPlayer) {
        this.mc.getTextureManager().bindTexture(clientPlayer.getLocationSkin());
        Render render = this.renderManager.getEntityRenderObject(this.mc.thePlayer);
        RenderPlayer renderplayer = (RenderPlayer) render;

        if (!clientPlayer.isInvisible()) {
            this.renderRightArm(renderplayer);
            this.renderLeftArm(renderplayer);
        }

    }

    private float getMapAngleFromPitch(float pitch) {
        float f1 = 1.0F - pitch / 45.0F + 0.1F;

        f1 = MathHelper.clamp_float(f1, 0.0F, 1.0F);
        f1 = -MathHelper.cos(f1 * 3.1415927F) * 0.5F + 0.5F;
        return f1;
    }

    private void renderRightArm(RenderPlayer renderPlayerIn) {
        GlStateManager.pushMatrix();
        GlStateManager.rotate(54.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(64.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(-62.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.translate(0.25F, -0.85F, 0.75F);
        renderPlayerIn.renderRightArm(this.mc.thePlayer);
        GlStateManager.popMatrix();
    }

    private void renderLeftArm(RenderPlayer renderPlayerIn) {
        GlStateManager.pushMatrix();
        GlStateManager.rotate(92.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(45.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(41.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.translate(-0.3F, -1.1F, 0.45F);
        renderPlayerIn.renderLeftArm(this.mc.thePlayer);
        GlStateManager.popMatrix();
    }

    private void renderPlayerArm(AbstractClientPlayer clientPlayer, float equipProgress, float swingProgress) {
        GL11.glDisable(2929);
        float f2 = -0.3F * MathHelper.sin(MathHelper.sqrt_float(swingProgress) * 3.1415927F);
        float f3 = 0.4F * MathHelper.sin(MathHelper.sqrt_float(swingProgress) * 3.1415927F * 2.0F);
        float f4 = -0.4F * MathHelper.sin(swingProgress * 3.1415927F);

        GlStateManager.translate(f2, f3, f4);
        GlStateManager.translate(0.64000005F, -0.6F, -0.71999997F);
        GlStateManager.translate(0.0F, equipProgress * -0.6F, 0.0F);
        GlStateManager.rotate(45.0F, 0.0F, 1.0F, 0.0F);
        float f5 = MathHelper.sin(swingProgress * swingProgress * 3.1415927F);
        float f6 = MathHelper.sin(MathHelper.sqrt_float(swingProgress) * 3.1415927F);

        GlStateManager.rotate(f6 * 70.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(f5 * -20.0F, 0.0F, 0.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(clientPlayer.getLocationSkin());
        GlStateManager.translate(-1.0F, 3.6F, 3.5F);
        GlStateManager.rotate(120.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.rotate(200.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(-135.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.scale(1.0F, 1.0F, 1.0F);
        GlStateManager.translate(5.6F, 0.0F, 0.0F);
        Render render = this.renderManager.getEntityRenderObject(this.mc.thePlayer);
        RenderPlayer renderplayer = (RenderPlayer) render;

        renderplayer.renderRightArm(this.mc.thePlayer);
        GlStateManager.enableCull();
    }

    private void rotateArroundXAndY(float angle, float angleY) {
        GlStateManager.pushMatrix();
        GlStateManager.rotate(angle, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(angleY, 0.0F, 1.0F, 0.0F);
        RenderHelper.enableStandardItemLighting();
        GlStateManager.popMatrix();
    }

    private void setLightMapFromPlayer(AbstractClientPlayer clientPlayer) {
        int i = this.mc.theWorld.getCombinedLight(new BlockPos(clientPlayer.posX, clientPlayer.posY + (double) clientPlayer.getEyeHeight(), clientPlayer.posZ), 0);
        float f = (float) (i & '\uffff');
        float f1 = (float) (i >> 16);

        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, f, f1);
    }

    private void rotateWithPlayerRotations(EntityPlayerSP entityplayerspIn, float partialTicks) {
        float f1 = entityplayerspIn.prevRenderArmPitch + (entityplayerspIn.renderArmPitch - entityplayerspIn.prevRenderArmPitch) * partialTicks;
        float f2 = entityplayerspIn.prevRenderArmYaw + (entityplayerspIn.renderArmYaw - entityplayerspIn.prevRenderArmYaw) * partialTicks;

        GlStateManager.rotate((entityplayerspIn.rotationPitch - f1) * 0.1F, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate((entityplayerspIn.rotationYaw - f2) * 0.1F, 0.0F, 1.0F, 0.0F);
    }

    private void performDrinking(AbstractClientPlayer clientPlayer, float partialTicks) {
        float f1 = (float) clientPlayer.getItemInUseCount() - partialTicks + 1.0F;
        float f2 = f1 / (float) this.itemToRender.getMaxItemUseDuration();
        float f3 = MathHelper.abs(MathHelper.cos(f1 / 4.0F * 3.1415927F) * 0.1F);

        if (f2 >= 0.8F) {
            f3 = 0.0F;
        }

        GlStateManager.translate(0.0F, f3, 0.0F);
        float f4 = 1.0F - (float) Math.pow((double) f2, 27.0D);

        GlStateManager.translate(f4 * 0.6F, f4 * -0.5F, f4 * 0.0F);
        GlStateManager.rotate(f4 * 90.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(f4 * 10.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(f4 * 30.0F, 0.0F, 0.0F, 1.0F);
    }

    private void doBlockTransformations() {
        GlStateManager.translate(-0.5F, 0.2F, 0.0F);
        GlStateManager.rotate(30.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(-80.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(60.0F, 0.0F, 1.0F, 0.0F);
    }

    private void doBowTransformations(float partialTicks, AbstractClientPlayer clientPlayer) {
        GlStateManager.rotate(-18.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.rotate(-12.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(-8.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.translate(-0.9F, 0.2F, 0.0F);
        float f1 = (float) this.itemToRender.getMaxItemUseDuration() - ((float) clientPlayer.getItemInUseCount() - partialTicks + 1.0F);
        float f2 = f1 / 20.0F;

        f2 = (f2 * f2 + f2 * 2.0F) / 3.0F;
        if (f2 > 1.0F) {
            f2 = 1.0F;
        }

        if (f2 > 0.1F) {
            float f3 = MathHelper.sin((f1 - 0.1F) * 1.3F);
            float f4 = f2 - 0.1F;
            float f5 = f3 * f4;

            GlStateManager.translate(f5 * 0.0F, f5 * 0.01F, f5 * 0.0F);
        }

        GlStateManager.translate(f2 * 0.0F, f2 * 0.0F, f2 * 0.1F);
        GlStateManager.scale(1.0F, 1.0F, 1.0F + f2 * 0.2F);
    }

    private void doItemUsedTransformations(float swingProgress) {
        float f1 = -0.4F * MathHelper.sin(MathHelper.sqrt_float(swingProgress) * 3.1415927F);
        float f2 = 0.2F * MathHelper.sin(MathHelper.sqrt_float(swingProgress) * 3.1415927F * 2.0F);
        float f3 = -0.2F * MathHelper.sin(swingProgress * 3.1415927F);

        GlStateManager.translate(f1, f2, f3);
    }

    private void transformFirstPersonItem(float equipProgress, float swingProgress, boolean rod) {
        if (rod) {
            GlStateManager.translate(0.4F, -0.42F, -0.71999997F);
        } else {
            GlStateManager.translate(0.56F, -0.52F, -0.71999997F);
        }

        GlStateManager.translate(0.0F, equipProgress * -0.6F, 0.0F);
        GlStateManager.rotate(rod ? 50.0F : 45.0F, 0.0F, 1.0F, 0.0F);
        float f2 = MathHelper.sin(swingProgress * swingProgress * 3.1415927F);
        float f3 = MathHelper.sin(MathHelper.sqrt_float(swingProgress) * 3.1415927F);

        GlStateManager.rotate(f2 * -20.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(f3 * -20.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.rotate(f3 * -80.0F, 1.0F, 0.0F, 0.0F);
        if (rod) {
            GlStateManager.scale(0.3F, 0.3F, 0.3F);
        } else {
            GlStateManager.scale(0.4F, 0.4F, 0.4F);
        }

    }

    private void attemptSwing() {
        if (this.config.punching && this.mc.thePlayer.getItemInUseCount() > 0) {
            boolean mouseDown = this.mc.gameSettings.keyBindAttack.isKeyDown() && this.mc.gameSettings.keyBindUseItem.isKeyDown();

            if (mouseDown && this.mc.objectMouseOver != null && this.mc.objectMouseOver.typeOfHit == MovingObjectType.BLOCK) {
                this.swingItem(this.mc.thePlayer);
            }
        }

    }
}
