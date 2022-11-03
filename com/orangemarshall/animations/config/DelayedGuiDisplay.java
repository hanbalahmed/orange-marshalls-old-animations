package com.orangemarshall.animations.config;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;

public class DelayedGuiDisplay {

    private int delayTicks;
    private Minecraft mcClient;
    private GuiScreen screen;

    public DelayedGuiDisplay(int delayTicks, GuiScreen screen) {
        this.delayTicks = delayTicks;
        this.mcClient = Minecraft.getMinecraft();
        this.screen = screen;
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onTick(ClientTickEvent event) {
        if (!event.phase.equals(Phase.START)) {
            if (--this.delayTicks <= 0) {
                this.mcClient.displayGuiScreen(this.screen);
                MinecraftForge.EVENT_BUS.unregister(this);
            }

        }
    }
}
