package com.orangemarshall.animations;

import com.orangemarshall.animations.config.Config;
import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.GuiIngameForge;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.client.event.RenderGameOverlayEvent.Pre;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class HealthAnimation extends Gui {

    private Random rand = new Random();
    private long healthUpdateCounter;
    private int playerHealth;
    private int lastPlayerHealth;
    private float lastSystemTime;
    private Config config = Config.getInstance();

    @SubscribeEvent
    public void onRender(Pre event) {
        if (event.type == ElementType.HEALTH) {
            event.setCanceled(true);
            this.renderHealth(event.resolution.getScaledWidth(), event.resolution.getScaledHeight());
        }

    }

    public void renderHealth(int width, int height) {
        this.bind(HealthAnimation.icons);
        GlStateManager.enableBlend();
        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayer player = (EntityPlayer) mc.getRenderViewEntity();
        int health = MathHelper.ceiling_float_int(player.getHealth());
        int updateCounter = mc.ingameGUI.getUpdateCounter();
        boolean highlight = this.healthUpdateCounter > (long) updateCounter && (this.healthUpdateCounter - (long) updateCounter) / 3L % 2L == 1L;

        if (health < this.playerHealth && player.hurtResistantTime > 0) {
            this.lastSystemTime = (float) Minecraft.getSystemTime();
            this.healthUpdateCounter = (long) (updateCounter + 20);
        } else if (health > this.playerHealth && player.hurtResistantTime > 0) {
            this.lastSystemTime = (float) Minecraft.getSystemTime();
            this.healthUpdateCounter = (long) (updateCounter + 10);
        }

        if ((float) Minecraft.getSystemTime() - this.lastSystemTime > 1000.0F) {
            this.playerHealth = health;
            this.lastPlayerHealth = health;
            this.lastSystemTime = (float) Minecraft.getSystemTime();
        }

        this.playerHealth = health;
        int healthLast = this.lastPlayerHealth;
        IAttributeInstance attrMaxHealth = player.getEntityAttribute(SharedMonsterAttributes.maxHealth);
        float healthMax = (float) attrMaxHealth.getAttributeValue();
        float absorb = player.getAbsorptionAmount();
        int healthRows = MathHelper.ceiling_float_int((healthMax + absorb) / 2.0F / 10.0F);
        int rowHeight = Math.max(10 - (healthRows - 2), 3);

        this.rand.setSeed((long) (updateCounter * 312871));
        int left = width / 2 - 91;
        int top = height - GuiIngameForge.left_height;

        GuiIngameForge.left_height += healthRows * rowHeight;
        if (rowHeight != 10) {
            GuiIngameForge.left_height += 10 - rowHeight;
        }

        int regen = -1;

        if (player.isPotionActive(Potion.regeneration)) {
            regen = updateCounter % 25;
        }

        int TOP = 9 * (mc.theWorld.getWorldInfo().isHardcoreModeEnabled() ? 5 : 0);
        int BACKGROUND = highlight ? 25 : 16;
        int MARGIN = 16;

        if (player.isPotionActive(Potion.poison)) {
            MARGIN += 36;
        } else if (player.isPotionActive(Potion.wither)) {
            MARGIN += 72;
        }

        float absorbRemaining = absorb;

        for (int i = MathHelper.ceiling_float_int((healthMax + absorb) / 2.0F) - 1; i >= 0; --i) {
            int row = MathHelper.ceiling_float_int((float) (i + 1) / 10.0F) - 1;
            int x = left + i % 10 * 8;
            int y = top - row * rowHeight;

            if (health <= 4) {
                y += this.rand.nextInt(2);
            }

            if (i == regen) {
                y -= 2;
            }

            this.drawTexturedModalRect(x, y, BACKGROUND, TOP, 9, 9);
            if (highlight && !this.config.flashingHearts) {
                if (i * 2 + 1 < healthLast) {
                    this.drawTexturedModalRect(x, y, MARGIN + 54, TOP, 9, 9);
                } else if (i * 2 + 1 == healthLast) {
                    this.drawTexturedModalRect(x, y, MARGIN + 63, TOP, 9, 9);
                }
            }

            if (absorbRemaining > 0.0F) {
                if (absorbRemaining == absorb && absorb % 2.0F == 1.0F) {
                    this.drawTexturedModalRect(x, y, MARGIN + 153, TOP, 9, 9);
                } else {
                    this.drawTexturedModalRect(x, y, MARGIN + 144, TOP, 9, 9);
                }

                absorbRemaining -= 2.0F;
            } else if (i * 2 + 1 < health) {
                this.drawTexturedModalRect(x, y, MARGIN + 36, TOP, 9, 9);
            } else if (i * 2 + 1 == health) {
                this.drawTexturedModalRect(x, y, MARGIN + 45, TOP, 9, 9);
            }
        }

        GlStateManager.disableBlend();
    }

    private void bind(ResourceLocation res) {
        Minecraft.getMinecraft().getTextureManager().bindTexture(res);
    }
}
