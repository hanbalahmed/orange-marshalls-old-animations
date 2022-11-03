package com.orangemarshall.animations.util;

import com.orangemarshall.animations.Animations;
import com.orangemarshall.animations.config.Config;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;

public class ModelMethods {

    private static final FieldWrapper bipedCape = new FieldWrapper(Animations.isObfuscated ? "bipedCape" : "bipedCape", ModelPlayer.class);

    public static void setRotationAnglesModelBiped(ModelBiped model, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn) {
        model.bipedHead.rotateAngleY = netHeadYaw / 57.295776F;
        model.bipedHead.rotateAngleX = headPitch / 57.295776F;
        model.bipedRightArm.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F + 3.1415927F) * 2.0F * limbSwingAmount * 0.5F;
        model.bipedLeftArm.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F) * 2.0F * limbSwingAmount * 0.5F;
        model.bipedRightArm.rotateAngleZ = 0.0F;
        model.bipedLeftArm.rotateAngleZ = 0.0F;
        model.bipedRightLeg.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
        model.bipedLeftLeg.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F + 3.1415927F) * 1.4F * limbSwingAmount;
        model.bipedRightLeg.rotateAngleY = 0.0F;
        model.bipedLeftLeg.rotateAngleY = 0.0F;
        if (model.isRiding) {
            model.bipedRightArm.rotateAngleX += -0.62831855F;
            model.bipedLeftArm.rotateAngleX += -0.62831855F;
            model.bipedRightLeg.rotateAngleX = -1.2566371F;
            model.bipedLeftLeg.rotateAngleX = -1.2566371F;
            model.bipedRightLeg.rotateAngleY = 0.31415927F;
            model.bipedLeftLeg.rotateAngleY = -0.31415927F;
        }

        if (model.heldItemLeft != 0) {
            model.bipedLeftArm.rotateAngleX = model.bipedLeftArm.rotateAngleX * 0.5F - 0.31415927F * (float) model.heldItemLeft;
        }

        model.bipedRightArm.rotateAngleY = 0.0F;
        model.bipedRightArm.rotateAngleZ = 0.0F;
        switch (model.heldItemRight) {
        case 0:
        case 2:
        default:
            break;

        case 1:
            model.bipedRightArm.rotateAngleX = model.bipedRightArm.rotateAngleX * 0.5F - 0.31415927F * (float) model.heldItemRight;
            break;

        case 3:
            model.bipedRightArm.rotateAngleX = model.bipedRightArm.rotateAngleX * 0.5F - 0.31415927F * (float) model.heldItemRight;
            if (Config.getInstance().thirdPersonBlocking) {
                model.bipedRightArm.rotateAngleY = 0.0F;
            } else {
                model.bipedRightArm.rotateAngleY = -0.5235988F;
            }
        }

        model.bipedLeftArm.rotateAngleY = 0.0F;
        float f3;
        float f4;

        if (model.swingProgress > -9990.0F) {
            f3 = model.swingProgress;
            model.bipedBody.rotateAngleY = MathHelper.sin(MathHelper.sqrt_float(f3) * 3.1415927F * 2.0F) * 0.2F;
            model.bipedRightArm.rotationPointZ = MathHelper.sin(model.bipedBody.rotateAngleY) * 5.0F;
            model.bipedRightArm.rotationPointX = -MathHelper.cos(model.bipedBody.rotateAngleY) * 5.0F;
            model.bipedLeftArm.rotationPointZ = -MathHelper.sin(model.bipedBody.rotateAngleY) * 5.0F;
            model.bipedLeftArm.rotationPointX = MathHelper.cos(model.bipedBody.rotateAngleY) * 5.0F;
            model.bipedRightArm.rotateAngleY += model.bipedBody.rotateAngleY;
            model.bipedLeftArm.rotateAngleY += model.bipedBody.rotateAngleY;
            model.bipedLeftArm.rotateAngleX += model.bipedBody.rotateAngleY;
            f3 = 1.0F - model.swingProgress;
            f3 *= f3;
            f3 *= f3;
            f3 = 1.0F - f3;
            f4 = MathHelper.sin(f3 * 3.1415927F);
            float f2 = MathHelper.sin(model.swingProgress * 3.1415927F) * -(model.bipedHead.rotateAngleX - 0.7F) * 0.75F;

            model.bipedRightArm.rotateAngleX = (float) ((double) model.bipedRightArm.rotateAngleX - ((double) f4 * 1.2D + (double) f2));
            model.bipedRightArm.rotateAngleY += model.bipedBody.rotateAngleY * 2.0F;
            model.bipedRightArm.rotateAngleZ += MathHelper.sin(model.swingProgress * 3.1415927F) * -0.4F;
        }

        if (model.isSneak) {
            model.bipedBody.rotateAngleX = 0.5F;
            model.bipedRightArm.rotateAngleX += 0.4F;
            model.bipedLeftArm.rotateAngleX += 0.4F;
            model.bipedRightLeg.rotationPointZ = 4.0F;
            model.bipedLeftLeg.rotationPointZ = 4.0F;
            model.bipedRightLeg.rotationPointY = 9.0F;
            model.bipedLeftLeg.rotationPointY = 9.0F;
            model.bipedHead.rotationPointY = 1.0F;
        } else {
            model.bipedBody.rotateAngleX = 0.0F;
            model.bipedRightLeg.rotationPointZ = 0.1F;
            model.bipedLeftLeg.rotationPointZ = 0.1F;
            model.bipedRightLeg.rotationPointY = 12.0F;
            model.bipedLeftLeg.rotationPointY = 12.0F;
            model.bipedHead.rotationPointY = 0.0F;
        }

        model.bipedRightArm.rotateAngleZ += MathHelper.cos(ageInTicks * 0.09F) * 0.05F + 0.05F;
        model.bipedLeftArm.rotateAngleZ -= MathHelper.cos(ageInTicks * 0.09F) * 0.05F + 0.05F;
        model.bipedRightArm.rotateAngleX += MathHelper.sin(ageInTicks * 0.067F) * 0.05F;
        model.bipedLeftArm.rotateAngleX -= MathHelper.sin(ageInTicks * 0.067F) * 0.05F;
        if (model.aimedBow) {
            f3 = 0.0F;
            f4 = 0.0F;
            model.bipedRightArm.rotateAngleZ = 0.0F;
            model.bipedLeftArm.rotateAngleZ = 0.0F;
            model.bipedRightArm.rotateAngleY = -(0.1F - f3 * 0.6F) + model.bipedHead.rotateAngleY;
            model.bipedLeftArm.rotateAngleY = 0.1F - f3 * 0.6F + model.bipedHead.rotateAngleY + 0.4F;
            model.bipedRightArm.rotateAngleX = -1.5707964F + model.bipedHead.rotateAngleX;
            model.bipedLeftArm.rotateAngleX = -1.5707964F + model.bipedHead.rotateAngleX;
            model.bipedRightArm.rotateAngleX -= f3 * 1.2F - f4 * 0.4F;
            model.bipedLeftArm.rotateAngleX -= f3 * 1.2F - f4 * 0.4F;
            model.bipedRightArm.rotateAngleZ += MathHelper.cos(ageInTicks * 0.09F) * 0.05F + 0.05F;
            model.bipedLeftArm.rotateAngleZ -= MathHelper.cos(ageInTicks * 0.09F) * 0.05F + 0.05F;
            model.bipedRightArm.rotateAngleX += MathHelper.sin(ageInTicks * 0.067F) * 0.05F;
            model.bipedLeftArm.rotateAngleX -= MathHelper.sin(ageInTicks * 0.067F) * 0.05F;
        }

        ModelBase.copyModelAngles(model.bipedHead, model.bipedHeadwear);
    }

    public static void setRotationAnglesModelPlayer(ModelPlayer model, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn) {
        setRotationAnglesModelBiped(model, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entityIn);
        ModelBase.copyModelAngles(model.bipedLeftLeg, model.bipedLeftLegwear);
        ModelBase.copyModelAngles(model.bipedRightLeg, model.bipedRightLegwear);
        ModelBase.copyModelAngles(model.bipedLeftArm, model.bipedLeftArmwear);
        ModelBase.copyModelAngles(model.bipedRightArm, model.bipedRightArmwear);
        ModelBase.copyModelAngles(model.bipedBody, model.bipedBodyWear);
        if (entityIn.isSneaking()) {
            ((ModelRenderer) ModelMethods.bipedCape.get(model)).rotationPointY = 2.0F;
        } else {
            ((ModelRenderer) ModelMethods.bipedCape.get(model)).rotationPointY = 0.0F;
        }

    }

    public static void renderModelPlayer(ModelPlayer model, Entity entityIn, float scale) {
        renderModelBiped(model, entityIn, scale);
        GlStateManager.pushMatrix();
        if (model.isChild) {
            float f = 2.0F;

            GlStateManager.scale(1.0F / f, 1.0F / f, 1.0F / f);
            GlStateManager.translate(0.0F, 24.0F * scale, 0.0F);
            model.bipedLeftLegwear.render(scale);
            model.bipedRightLegwear.render(scale);
            model.bipedLeftArmwear.render(scale);
            model.bipedRightArmwear.render(scale);
            model.bipedBodyWear.render(scale);
        } else {
            if (entityIn.isSneaking()) {
                GlStateManager.translate(0.0F, 0.2F, 0.0F);
            }

            model.bipedLeftLegwear.render(scale);
            model.bipedRightLegwear.render(scale);
            model.bipedLeftArmwear.render(scale);
            model.bipedRightArmwear.render(scale);
            model.bipedBodyWear.render(scale);
        }

        GlStateManager.popMatrix();
    }

    public static void renderModelBiped(ModelBiped model, Entity entityIn, float scale) {
        GlStateManager.pushMatrix();
        if (model.isChild) {
            float f = 2.0F;

            GlStateManager.scale(1.5F / f, 1.5F / f, 1.5F / f);
            GlStateManager.translate(0.0F, 16.0F * scale, 0.0F);
            model.bipedHead.render(scale);
            GlStateManager.popMatrix();
            GlStateManager.pushMatrix();
            GlStateManager.scale(1.0F / f, 1.0F / f, 1.0F / f);
            GlStateManager.translate(0.0F, 24.0F * scale, 0.0F);
            model.bipedBody.render(scale);
            model.bipedRightArm.render(scale);
            model.bipedLeftArm.render(scale);
            model.bipedRightLeg.render(scale);
            model.bipedLeftLeg.render(scale);
            model.bipedHeadwear.render(scale);
        } else {
            if (entityIn.isSneaking()) {
                GlStateManager.translate(0.0F, 0.2F, 0.0F);
            }

            model.bipedHead.render(scale);
            model.bipedBody.render(scale);
            model.bipedRightArm.render(scale);
            model.bipedLeftArm.render(scale);
            model.bipedRightLeg.render(scale);
            model.bipedLeftLeg.render(scale);
            model.bipedHeadwear.render(scale);
        }

        GlStateManager.popMatrix();
    }
}
