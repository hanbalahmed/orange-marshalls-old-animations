package com.orangemarshall.animations.config.gui;

import com.orangemarshall.animations.config.ScaledResolution;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

public abstract class ScrollingList {

    private final Minecraft client;
    protected final int listWidth;
    protected final int listHeight;
    protected final int screenWidth;
    protected final int screenHeight;
    protected final int top;
    protected final int bottom;
    protected final int right;
    protected final int left;
    protected final int slotHeight;
    private int scrollUpActionId;
    private int scrollDownActionId;
    protected int mouseX;
    protected int mouseY;
    private float initialMouseClickY;
    private float scrollFactor;
    private float scrollDistance;
    protected int selectedIndex;
    private long lastClickTime;
    private boolean highlightSelected;
    private boolean hasHeader;
    private int headerHeight;
    protected boolean captureMouse;

    /** @deprecated */
    @Deprecated
    public ScrollingList(Minecraft client, int width, int height, int top, int bottom, int left, int entryHeight) {
        this(client, width, height, top, bottom, left, entryHeight, width, height);
    }

    public ScrollingList(Minecraft client, int width, int height, int top, int bottom, int left, int entryHeight, int screenWidth, int screenHeight) {
        this.initialMouseClickY = -2.0F;
        this.selectedIndex = -1;
        this.lastClickTime = 0L;
        this.highlightSelected = true;
        this.captureMouse = true;
        this.client = client;
        this.listWidth = width;
        this.listHeight = height;
        this.top = top;
        this.bottom = bottom;
        this.slotHeight = entryHeight;
        this.left = left;
        this.right = width + this.left;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
    }

    public void func_27258_a(boolean p_27258_1_) {
        this.highlightSelected = p_27258_1_;
    }

    /** @deprecated */
    @Deprecated
    protected void func_27259_a(boolean hasFooter, int footerHeight) {
        this.setHeaderInfo(hasFooter, footerHeight);
    }

    protected void setHeaderInfo(boolean hasHeader, int headerHeight) {
        this.hasHeader = hasHeader;
        this.headerHeight = headerHeight;
        if (!hasHeader) {
            this.headerHeight = 0;
        }

    }

    protected abstract int getSize();

    protected abstract void elementClicked(int i, boolean flag);

    protected abstract boolean isSelected(int i);

    protected int getContentHeight() {
        return this.getSize() * this.slotHeight + this.headerHeight;
    }

    protected abstract void drawBackground();

    protected abstract void drawSlot(int i, int j, int k, int l, Tessellator tessellator);

    /** @deprecated */
    @Deprecated
    protected void func_27260_a(int entryRight, int relativeY, Tessellator tess) {}

    protected void drawHeader(int entryRight, int relativeY, Tessellator tess) {
        this.func_27260_a(entryRight, relativeY, tess);
    }

    /** @deprecated */
    @Deprecated
    protected void func_27255_a(int x, int y) {}

    protected void clickHeader(int x, int y) {
        this.func_27255_a(x, y);
    }

    /** @deprecated */
    @Deprecated
    protected void func_27257_b(int mouseX, int mouseY) {}

    protected void drawScreen(int mouseX, int mouseY) {
        this.func_27257_b(mouseX, mouseY);
    }

    public int func_27256_c(int x, int y) {
        int left = this.left + 1;
        int right = this.left + this.listWidth - 7;
        int relativeY = y - this.top - this.headerHeight + (int) this.scrollDistance - 4;
        int entryIndex = relativeY / this.slotHeight;

        return x >= left && x <= right && entryIndex >= 0 && relativeY >= 0 && entryIndex < this.getSize() ? entryIndex : -1;
    }

    public void registerScrollButtons(List buttons, int upActionID, int downActionID) {
        this.scrollUpActionId = upActionID;
        this.scrollDownActionId = downActionID;
    }

    private void applyScrollLimits() {
        int listHeight = this.getContentHeight() - (this.bottom - this.top - 4);

        if (listHeight < 0) {
            listHeight /= 2;
        }

        if (this.scrollDistance < 0.0F) {
            this.scrollDistance = 0.0F;
        }

        if (this.scrollDistance > (float) listHeight) {
            this.scrollDistance = (float) listHeight;
        }

    }

    public void actionPerformed(GuiButton button) {
        if (button.enabled) {
            if (button.id == this.scrollUpActionId) {
                this.scrollDistance -= (float) (this.slotHeight * 2 / 3);
                this.initialMouseClickY = -2.0F;
                this.applyScrollLimits();
            } else if (button.id == this.scrollDownActionId) {
                this.scrollDistance += (float) (this.slotHeight * 2 / 3);
                this.initialMouseClickY = -2.0F;
                this.applyScrollLimits();
            }
        }

    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        this.drawBackground();
        boolean isHovering = mouseX >= this.left && mouseX <= this.left + this.listWidth && mouseY >= this.top && mouseY <= this.bottom;
        int listLength = this.getSize();
        byte scrollBarWidth = 6;
        int scrollBarRight = this.left + this.listWidth;
        int scrollBarLeft = scrollBarRight - scrollBarWidth;
        int entryLeft = this.left;
        int entryRight = scrollBarLeft - 1;
        int viewHeight = this.bottom - this.top;
        byte border = 4;
        int tess;

        if (Mouse.isButtonDown(0)) {
            if (this.initialMouseClickY == -1.0F) {
                if (isHovering) {
                    tess = mouseY - this.top - this.headerHeight + (int) this.scrollDistance - border;
                    int worldr = tess / this.slotHeight;

                    if (mouseX >= entryLeft && mouseX <= entryRight && worldr >= 0 && tess >= 0 && worldr < listLength) {
                        this.elementClicked(worldr, worldr == this.selectedIndex && System.currentTimeMillis() - this.lastClickTime < 250L);
                        this.selectedIndex = worldr;
                        this.lastClickTime = System.currentTimeMillis();
                    } else if (mouseX >= entryLeft && mouseX <= entryRight && tess < 0) {
                        this.clickHeader(mouseX - entryLeft, mouseY - this.top + (int) this.scrollDistance - border);
                    }

                    if (mouseX >= scrollBarLeft && mouseX <= scrollBarRight) {
                        this.scrollFactor = -1.0F;
                        int res = this.getContentHeight() - viewHeight - border;

                        if (res < 1) {
                            res = 1;
                        }

                        int scaleW = (int) ((float) (viewHeight * viewHeight) / (float) this.getContentHeight());

                        if (scaleW < 32) {
                            scaleW = 32;
                        }

                        if (scaleW > viewHeight - border * 2) {
                            scaleW = viewHeight - border * 2;
                        }

                        this.scrollFactor /= (float) (viewHeight - scaleW) / (float) res;
                    } else {
                        this.scrollFactor = 1.0F;
                    }

                    this.initialMouseClickY = (float) mouseY;
                } else {
                    this.initialMouseClickY = -2.0F;
                }
            } else if (this.initialMouseClickY >= 0.0F) {
                this.scrollDistance -= ((float) mouseY - this.initialMouseClickY) * this.scrollFactor;
                this.initialMouseClickY = (float) mouseY;
            }
        } else {
            while (isHovering && Mouse.next()) {
                tess = Mouse.getEventDWheel();
                if (tess != 0) {
                    if (tess > 0) {
                        tess = -1;
                    } else if (tess < 0) {
                        tess = 1;
                    }

                    this.scrollDistance += (float) (tess * this.slotHeight * 2);
                }
            }

            this.initialMouseClickY = -1.0F;
        }

        this.applyScrollLimits();
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        ScaledResolution scaledresolution = new ScaledResolution();
        double d0 = (double) this.client.displayWidth / scaledresolution.getScaledWidth_double();
        double scaleH = (double) this.client.displayHeight / scaledresolution.getScaledHeight_double();

        GL11.glEnable(3089);
        GL11.glScissor((int) ((double) this.left * d0), (int) ((double) this.client.displayHeight - (double) this.bottom * scaleH), (int) ((double) this.listWidth * d0), (int) ((double) viewHeight * scaleH));
        if (this.client.theWorld != null) {
            this.drawGradientRect(this.left, this.top, this.right, this.bottom, -1072689136, -804253680);
        } else {
            GlStateManager.disableLighting();
            GlStateManager.disableFog();
            this.client.renderEngine.bindTexture(Gui.optionsBackground);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            float baseY = 32.0F;

            worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
            worldrenderer.pos((double) this.left, (double) this.bottom, 0.0D).tex((double) ((float) this.left / baseY), (double) ((float) (this.bottom + (int) this.scrollDistance) / baseY)).color(32, 32, 32, 255).endVertex();
            worldrenderer.pos((double) this.right, (double) this.bottom, 0.0D).tex((double) ((float) this.right / baseY), (double) ((float) (this.bottom + (int) this.scrollDistance) / baseY)).color(32, 32, 32, 255).endVertex();
            worldrenderer.pos((double) this.right, (double) this.top, 0.0D).tex((double) ((float) this.right / baseY), (double) ((float) (this.top + (int) this.scrollDistance) / baseY)).color(32, 32, 32, 255).endVertex();
            worldrenderer.pos((double) this.left, (double) this.top, 0.0D).tex((double) ((float) this.left / baseY), (double) ((float) (this.top + (int) this.scrollDistance) / baseY)).color(32, 32, 32, 255).endVertex();
            tessellator.draw();
        }

        int i = this.top + border - (int) this.scrollDistance;

        if (this.hasHeader) {
            this.drawHeader(entryRight, i, tessellator);
        }

        int extraHeight;
        int height;
        int barTop;

        for (extraHeight = 0; extraHeight < listLength; ++extraHeight) {
            height = i + extraHeight * this.slotHeight + this.headerHeight;
            barTop = this.slotHeight - border;
            if (height <= this.bottom && height + barTop >= this.top) {
                if (this.highlightSelected && this.isSelected(extraHeight)) {
                    int min = this.left;

                    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                    GlStateManager.disableTexture2D();
                    worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
                    worldrenderer.pos((double) min, (double) (height + barTop + 2), 0.0D).tex(0.0D, 1.0D).color(128, 128, 128, 255).endVertex();
                    worldrenderer.pos((double) entryRight, (double) (height + barTop + 2), 0.0D).tex(1.0D, 1.0D).color(128, 128, 128, 255).endVertex();
                    worldrenderer.pos((double) entryRight, (double) (height - 2), 0.0D).tex(1.0D, 0.0D).color(128, 128, 128, 255).endVertex();
                    worldrenderer.pos((double) min, (double) (height - 2), 0.0D).tex(0.0D, 0.0D).color(128, 128, 128, 255).endVertex();
                    worldrenderer.pos((double) (min + 1), (double) (height + barTop + 1), 0.0D).tex(0.0D, 1.0D).color(0, 0, 0, 255).endVertex();
                    worldrenderer.pos((double) (entryRight - 1), (double) (height + barTop + 1), 0.0D).tex(1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
                    worldrenderer.pos((double) (entryRight - 1), (double) (height - 1), 0.0D).tex(1.0D, 0.0D).color(0, 0, 0, 255).endVertex();
                    worldrenderer.pos((double) (min + 1), (double) (height - 1), 0.0D).tex(0.0D, 0.0D).color(0, 0, 0, 255).endVertex();
                    tessellator.draw();
                    GlStateManager.enableTexture2D();
                }

                this.drawSlot(extraHeight, entryRight, height, barTop, tessellator);
            }
        }

        GlStateManager.disableDepth();
        extraHeight = this.getContentHeight() - viewHeight - border;
        if (extraHeight > 0) {
            height = viewHeight * viewHeight / this.getContentHeight();
            if (height < 32) {
                height = 32;
            }

            if (height > viewHeight - border * 2) {
                height = viewHeight - border * 2;
            }

            barTop = (int) this.scrollDistance * (viewHeight - height) / extraHeight + this.top;
            if (barTop < this.top) {
                barTop = this.top;
            }

            GlStateManager.disableTexture2D();
            worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
            worldrenderer.pos((double) scrollBarLeft, (double) this.bottom, 0.0D).tex(0.0D, 1.0D).color(0, 0, 0, 255).endVertex();
            worldrenderer.pos((double) scrollBarRight, (double) this.bottom, 0.0D).tex(1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
            worldrenderer.pos((double) scrollBarRight, (double) this.top, 0.0D).tex(1.0D, 0.0D).color(0, 0, 0, 255).endVertex();
            worldrenderer.pos((double) scrollBarLeft, (double) this.top, 0.0D).tex(0.0D, 0.0D).color(0, 0, 0, 255).endVertex();
            tessellator.draw();
            worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
            worldrenderer.pos((double) scrollBarLeft, (double) (barTop + height), 0.0D).tex(0.0D, 1.0D).color(128, 128, 128, 255).endVertex();
            worldrenderer.pos((double) scrollBarRight, (double) (barTop + height), 0.0D).tex(1.0D, 1.0D).color(128, 128, 128, 255).endVertex();
            worldrenderer.pos((double) scrollBarRight, (double) barTop, 0.0D).tex(1.0D, 0.0D).color(128, 128, 128, 255).endVertex();
            worldrenderer.pos((double) scrollBarLeft, (double) barTop, 0.0D).tex(0.0D, 0.0D).color(128, 128, 128, 255).endVertex();
            tessellator.draw();
            worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
            worldrenderer.pos((double) scrollBarLeft, (double) (barTop + height - 1), 0.0D).tex(0.0D, 1.0D).color(192, 192, 192, 255).endVertex();
            worldrenderer.pos((double) (scrollBarRight - 1), (double) (barTop + height - 1), 0.0D).tex(1.0D, 1.0D).color(192, 192, 192, 255).endVertex();
            worldrenderer.pos((double) (scrollBarRight - 1), (double) barTop, 0.0D).tex(1.0D, 0.0D).color(192, 192, 192, 255).endVertex();
            worldrenderer.pos((double) scrollBarLeft, (double) barTop, 0.0D).tex(0.0D, 0.0D).color(192, 192, 192, 255).endVertex();
            tessellator.draw();
        }

        this.drawScreen(mouseX, mouseY);
        GlStateManager.enableTexture2D();
        GlStateManager.shadeModel(7424);
        GlStateManager.enableAlpha();
        GlStateManager.disableBlend();
        GL11.glDisable(3089);
    }

    protected void drawGradientRect(int left, int top, int right, int bottom, int color1, int color2) {
        float a1 = (float) (color1 >> 24 & 255) / 255.0F;
        float r1 = (float) (color1 >> 16 & 255) / 255.0F;
        float g1 = (float) (color1 >> 8 & 255) / 255.0F;
        float b1 = (float) (color1 & 255) / 255.0F;
        float a2 = (float) (color2 >> 24 & 255) / 255.0F;
        float r2 = (float) (color2 >> 16 & 255) / 255.0F;
        float g2 = (float) (color2 >> 8 & 255) / 255.0F;
        float b2 = (float) (color2 & 255) / 255.0F;

        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.shadeModel(7425);
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();

        worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        worldrenderer.pos((double) right, (double) top, 0.0D).color(r1, g1, b1, a1).endVertex();
        worldrenderer.pos((double) left, (double) top, 0.0D).color(r1, g1, b1, a1).endVertex();
        worldrenderer.pos((double) left, (double) bottom, 0.0D).color(r2, g2, b2, a2).endVertex();
        worldrenderer.pos((double) right, (double) bottom, 0.0D).color(r2, g2, b2, a2).endVertex();
        tessellator.draw();
        GlStateManager.shadeModel(7424);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
    }
}
