package com.orangemarshall.animations.config.gui;

import com.google.common.collect.Sets;
import com.orangemarshall.animations.config.ConfigurationHolder;
import com.orangemarshall.animations.config.FieldContainer;
import com.orangemarshall.animations.config.ScaledResolution;
import com.orangemarshall.animations.config.gui.selector.Selector;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;

public class ConfigList extends GuiScreen {

    private final ArrayList entries;
    private int listWidth;
    private FontRenderer fontRendererObj;
    private SlotConfigList modList;
    private int selected = -1;
    private FieldContainer selectedField;
    private Selector valueSelector;
    private String updateInfo;
    private long lastUpdate;
    private int updateCounter;
    private String name;
    private final Set ignoredCategories = Sets.newHashSet();
    private ConfigurationHolder config;

    public ConfigList(String name, ConfigurationHolder config) {
        this.entries = new ArrayList(config.getFields());
        this.fontRendererObj = Minecraft.getMinecraft().fontRendererObj;
        this.config = config;
        this.name = name;
    }

    public void initGui() {
        this.updateCounter = 0;
        this.lastUpdate = 0L;
        this.entries.removeIf(test<invokedynamic>(this));
        String lastCategory = "";

        for (int res = 0; res < this.entries.size(); ++res) {
            FieldContainer con = (FieldContainer) this.entries.get(res);

            if (!lastCategory.equals(con.category())) {
                lastCategory = con.category();
                if (!this.ignoredCategories.contains(lastCategory)) {
                    if (!lastCategory.isEmpty()) {
                        this.entries.add(res++, new SpacerContainer("", this.config));
                    }

                    this.entries.add(res, new SpacerContainer("§l" + con.category(), this.config));
                }
            }

            this.listWidth = Math.max(this.listWidth, this.getFontRenderer().getStringWidth(con.name()) + 10);
        }

        this.entries.add(new SpacerContainer("", this.config));
        ScaledResolution scaledresolution = new ScaledResolution();

        this.listWidth = Math.min(this.listWidth, scaledresolution.getScaledWidth() / 2 - 20);
        this.modList = new SlotConfigList(this, this.entries, this.listWidth);
        this.addButtons();
    }

    private void addButtons() {
        boolean buttonWidth = true;
        int xBetweenButtons = this.modList.right() + (this.width - this.modList.right()) / 2;
        Buttons.GuiButtonDone buttonDone = new Buttons.GuiButtonDone(6, xBetweenButtons - 75 - 10, this.height - 38, 75) {
            public void mouseReleased(int mouseX, int mouseY) {
                if (this.isMouseOver()) {
                    ConfigList.this.prepareSlotChange();
                    ConfigList.this.mc.displayGuiScreen((GuiScreen) null);
                    if (ConfigList.this.mc.currentScreen == null) {
                        ConfigList.this.mc.setIngameFocus();
                    }

                    ConfigList.this.config.save();
                    ConfigList.this.config.load();
                }

            }
        };

        this.buttonList.add(buttonDone);
        Buttons.GuiButtonCancel buttonCancel = new Buttons.GuiButtonCancel(6, xBetweenButtons + 10, this.height - 38, 75) {
            public void mouseReleased(int mouseX, int mouseY) {
                if (this.isMouseOver()) {
                    ConfigList.this.mc.displayGuiScreen((GuiScreen) null);
                    if (ConfigList.this.mc.currentScreen == null) {
                        ConfigList.this.mc.setIngameFocus();
                    }
                }

            }
        };

        this.buttonList.add(buttonCancel);
        Buttons.GuiButtonReset buttonRestore = new Buttons.GuiButtonReset(6, xBetweenButtons - 37, this.height - 64, 75) {
            public void mouseReleased(int mouseX, int mouseY) {
                if (this.isMouseOver() && ConfigList.this.selectedField != null) {
                    ConfigList.this.valueSelector.setValue(ConfigList.this.selectedField.getValue());
                    ConfigList.this.updateInfo("Reset");
                }

            }
        };

        this.buttonList.add(buttonRestore);
    }

    protected void keyTyped(char c, int i) throws IOException {
        super.keyTyped(c, i);
        if (this.valueSelector != null) {
            this.valueSelector.keyTyped(c, i);
        }

    }

    public void mouseClicked(int i, int j, int k) throws IOException {
        super.mouseClicked(i, j, k);
        if (this.valueSelector != null) {
            this.valueSelector.mouseClicked(i, j, k);
        }

    }

    public FontRenderer getFontRenderer() {
        return this.fontRendererObj;
    }

    public void selectModIndex(int index) {
        if (index != this.selected) {
            if (index >= 0 && index <= this.entries.size() && this.entries.get(index) instanceof SpacerContainer) {
                this.selectedField = null;
                this.selected = -1;
                this.valueSelector = null;
            } else {
                this.prepareSlotChange();
                this.selected = index;
                this.selectedField = index >= 0 && index <= this.entries.size() ? (FieldContainer) this.entries.get(this.selected) : null;
                int middle = this.width / 2;
                byte horizontalSpacing = 20;
                int textfieldLeft = middle + horizontalSpacing;
                int textfieldTop = this.height / 2 - 30;

                this.valueSelector = Selector.getFromField(this.selectedField, textfieldLeft, textfieldTop, middle - horizontalSpacing * 2);
            }
        }
    }

    private void prepareSlotChange() {
        if (this.selectedField != null) {
            String prev = this.selectedField.getValue();

            this.selectedField.setValue(this.valueSelector.getValue());
            if (!prev.equals(this.selectedField.getValue())) {
                this.updateInfo("Saved");
            }
        }

    }

    public boolean fieldIndexSelected(int index) {
        return index == this.selected;
    }

    public void updateInfo(String text) {
        this.lastUpdate = System.currentTimeMillis();
        this.updateInfo = text;
        this.updateCounter = 0;
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.modList.drawScreen(mouseX, mouseY, partialTicks);
        if (this.valueSelector != null) {
            this.valueSelector.draw();
        }

        ScaledResolution res = new ScaledResolution();
        int middle = res.getScaledWidth() / 2;

        this.drawCenteredString(this.fontRendererObj, this.name, middle, 12, 16777215);
        if (this.selectedField != null) {
            int maxlinewidth = middle - 20;
            String[] info = this.lineBreaksAfterWidth(this.selectedField.name(), maxlinewidth);
            int height = 50;
            String[] astring = info;
            int i = info.length;

            for (int j = 0; j < i; ++j) {
                String str = astring[j];

                this.drawString(this.fontRendererObj, str, middle + 10, height, 16777215);
                height += this.fontRendererObj.FONT_HEIGHT;
            }
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
        if (System.currentTimeMillis() - this.lastUpdate <= 1000L) {
            this.fontRendererObj.drawString(this.updateInfo, res.getScaledWidth() - this.fontRendererObj.getStringWidth(this.updateInfo) - 1, res.getScaledHeight() - this.fontRendererObj.FONT_HEIGHT - 1, 16777215 | 255 - this.updateCounter << 24);
            this.updateCounter += 3;
            this.updateCounter = Math.max(this.updateCounter, 150);
        }

    }

    private String[] lineBreaksAfterWidth(String text, int maxlinewidth) {
        StringBuilder output = new StringBuilder();

        while (this.fontRendererObj.getStringWidth(text) > maxlinewidth) {
            int index;

            for (index = 1; this.fontRendererObj.getStringWidth(text.substring(0, index)) < maxlinewidth; ++index) {
                ;
            }

            --index;
            String subText = text.substring(0, index);

            subText = subText.trim();
            if (subText.contains(" ")) {
                if (subText.endsWith(" ")) {
                    subText = subText.substring(0, subText.length() - 1);
                }

                int spaceIndex = subText.lastIndexOf(" ");

                subText = subText.substring(0, spaceIndex);
                text = text.substring(spaceIndex);
                output.append(subText + "\n");
            } else {
                text = text.substring(index);
                output.append(subText + "\n");
            }
        }

        output.append(text + "\n");
        return output.toString().split("\n");
    }

    private boolean lambda$initGui$0(FieldContainer con) {
        return this.ignoredCategories.contains(con.category());
    }
}
