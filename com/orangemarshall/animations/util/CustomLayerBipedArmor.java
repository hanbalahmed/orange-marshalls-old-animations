package com.orangemarshall.animations.util;

import com.orangemarshall.animations.Animations;
import com.orangemarshall.animations.config.Config;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.client.renderer.entity.layers.LayerArmorBase;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class CustomLayerBipedArmor extends LayerArmorBase {

    private RendererLivingEntity rendererIn;
    private float colorR = 1.0F;
    private float colorG = 1.0F;
    private float colorB = 1.0F;
    private float alpha = 1.0F;
    private static final FieldWrapper renderer = new FieldWrapper(Animations.isObfuscated ? "renderer" : "renderer", LayerArmorBase.class);

    public CustomLayerBipedArmor(RendererLivingEntity rendererIn) {
        super(rendererIn);
        this.rendererIn = rendererIn;
    }

    public void setRenderer(RendererLivingEntity rendererIn) {
        CustomLayerBipedArmor.renderer.setFinal(this, rendererIn);
        this.rendererIn = rendererIn;
    }

    protected void initArmor() {
        this.modelLeggings = new ModelBiped(0.5F);
        this.modelArmor = new ModelBiped(1.0F);
    }

    protected void setModelPartVisible(ModelBiped model, int armorSlot) {
        this.setModelVisible(model);
        switch (armorSlot) {
        case 1:
            model.bipedRightLeg.showModel = true;
            model.bipedLeftLeg.showModel = true;
            break;

        case 2:
            model.bipedBody.showModel = true;
            model.bipedRightLeg.showModel = true;
            model.bipedLeftLeg.showModel = true;
            break;

        case 3:
            model.bipedBody.showModel = true;
            model.bipedRightArm.showModel = true;
            model.bipedLeftArm.showModel = true;
            break;

        case 4:
            model.bipedHead.showModel = true;
            model.bipedHeadwear.showModel = true;
        }

    }

    protected void setModelVisible(ModelBiped model) {
        model.setInvisible(false);
    }

    protected ModelBiped getArmorModelHook(EntityLivingBase entity, ItemStack itemStack, int slot, ModelBiped model) {
        return ForgeHooksClient.getArmorModel(entity, itemStack, slot, model);
    }

    public void doRenderLayer(EntityLivingBase entitylivingbaseIn, float p_177141_2_, float p_177141_3_, float partialTicks, float p_177141_5_, float p_177141_6_, float p_177141_7_, float scale) {
        this.renderLayer(entitylivingbaseIn, p_177141_2_, p_177141_3_, partialTicks, p_177141_5_, p_177141_6_, p_177141_7_, scale, 4);
        this.renderLayer(entitylivingbaseIn, p_177141_2_, p_177141_3_, partialTicks, p_177141_5_, p_177141_6_, p_177141_7_, scale, 3);
        this.renderLayer(entitylivingbaseIn, p_177141_2_, p_177141_3_, partialTicks, p_177141_5_, p_177141_6_, p_177141_7_, scale, 2);
        this.renderLayer(entitylivingbaseIn, p_177141_2_, p_177141_3_, partialTicks, p_177141_5_, p_177141_6_, p_177141_7_, scale, 1);
    }

    private void renderLayer(EntityLivingBase entitylivingbaseIn, float p_177182_2_, float p_177182_3_, float partialTicks, float p_177182_5_, float p_177182_6_, float p_177182_7_, float scale, int armorSlot) {
        ItemStack itemstack = this.getCurrentArmor(entitylivingbaseIn, armorSlot);

        if (itemstack != null && itemstack.getItem() instanceof ItemArmor) {
            ItemArmor itemarmor = (ItemArmor) itemstack.getItem();
            ModelBiped model = (ModelBiped) this.getArmorModel(armorSlot);

            model.setModelAttributes(this.rendererIn.getMainModel());
            model.setLivingAnimations(entitylivingbaseIn, p_177182_2_, p_177182_3_, partialTicks);
            model = this.getArmorModelHook(entitylivingbaseIn, itemstack, armorSlot, model);
            this.setModelPartVisible(model, armorSlot);
            boolean flag = this.isSlotForLeggings(armorSlot);

            this.rendererIn.bindTexture(this.getArmorResource(entitylivingbaseIn, itemstack, flag ? 2 : 1, (String) null));
            int i = itemarmor.getColor(itemstack);

            if (i != -1) {
                float f = (float) (i >> 16 & 255) / 255.0F;
                float f1 = (float) (i >> 8 & 255) / 255.0F;
                float f2 = (float) (i & 255) / 255.0F;

                GlStateManager.color(this.colorR * f, this.colorG * f1, this.colorB * f2, this.alpha);
                ModelMethods.setRotationAnglesModelBiped(model, p_177182_2_, p_177182_3_, p_177182_5_, p_177182_6_, p_177182_7_, scale, entitylivingbaseIn);
                ModelMethods.renderModelBiped(model, entitylivingbaseIn, scale);
                this.rendererIn.bindTexture(this.getArmorResource(entitylivingbaseIn, itemstack, flag ? 2 : 1, "overlay"));
            }

            GlStateManager.color(this.colorR, this.colorG, this.colorB, this.alpha);
            if (Config.getInstance().thirdPersonBlocking) {
                ModelMethods.setRotationAnglesModelBiped(model, p_177182_2_, p_177182_3_, p_177182_5_, p_177182_6_, p_177182_7_, scale, entitylivingbaseIn);
                ModelMethods.renderModelBiped(model, entitylivingbaseIn, scale);
            } else {
                model.render(entitylivingbaseIn, p_177182_2_, p_177182_3_, p_177182_5_, p_177182_6_, p_177182_7_, scale);
            }

            if (itemstack.hasEffect()) {
                this.renderGlint(entitylivingbaseIn, model, p_177182_2_, p_177182_3_, partialTicks, p_177182_5_, p_177182_6_, p_177182_7_, scale);
            }
        }

    }

    private boolean isSlotForLeggings(int armorSlot) {
        return armorSlot == 2;
    }

    private void renderGlint(EntityLivingBase entitylivingbaseIn, ModelBiped modelbaseIn, float p_177183_3_, float p_177183_4_, float partialTicks, float p_177183_6_, float p_177183_7_, float p_177183_8_, float scale) {
        float f = (float) entitylivingbaseIn.ticksExisted + partialTicks;

        this.rendererIn.bindTexture(CustomLayerBipedArmor.ENCHANTED_ITEM_GLINT_RES);
        GlStateManager.enableBlend();
        GlStateManager.depthFunc(514);
        GlStateManager.depthMask(false);
        float f1 = 0.5F;

        GlStateManager.color(f1, f1, f1, 1.0F);

        for (int i = 0; i < 2; ++i) {
            GlStateManager.disableLighting();
            GlStateManager.blendFunc(768, 1);
            float f2 = 0.76F;

            GlStateManager.color(0.5F * f2, 0.25F * f2, 0.8F * f2, 1.0F);
            GlStateManager.matrixMode(5890);
            GlStateManager.loadIdentity();
            float f3 = 0.33333334F;

            GlStateManager.scale(f3, f3, f3);
            GlStateManager.rotate(30.0F - (float) i * 60.0F, 0.0F, 0.0F, 1.0F);
            GlStateManager.translate(0.0F, f * (0.001F + (float) i * 0.003F) * 20.0F, 0.0F);
            GlStateManager.matrixMode(5888);
            ModelMethods.renderModelBiped(modelbaseIn, entitylivingbaseIn, scale);
        }

        GlStateManager.matrixMode(5890);
        GlStateManager.loadIdentity();
        GlStateManager.matrixMode(5888);
        GlStateManager.enableLighting();
        GlStateManager.depthMask(true);
        GlStateManager.depthFunc(515);
        GlStateManager.disableBlend();
    }
}
