package com.orangemarshall.animations;

import com.orangemarshall.animations.config.Config;
import com.orangemarshall.animations.proxy.CommonProxy;
import java.util.Timer;
import java.util.TimerTask;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientConnectedToServerEvent;

@Mod(
    modid = "animations",
    name = "Orange\'s 1.7 Animations",
    useMetadata = true,
    acceptedMinecraftVersions = "[1.8.9]"
)
public class Animations {

    public static boolean isObfuscated;
    @SidedProxy(
        clientSide = "com.orangemarshall.animations.proxy.ClientProxy",
        serverSide = "com.orangemarshall.animations.proxy.ServerProxy"
    )
    public static CommonProxy proxy;

    @EventHandler
    public void preInit(FMLPreInitializationEvent e) {
        Animations.isObfuscated = this.isObfuscated();
        Config config = new Config(e.getSuggestedConfigurationFile(), "0.1");

        config.load();
        Animations.proxy.preInit(e);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onConnect(ClientConnectedToServerEvent event) {
        (new Timer()).schedule(new TimerTask() {
            public void run() {
                if (Minecraft.getMinecraft().thePlayer != null) {
                    ChatComponentText chatComponent1 = new ChatComponentText(EnumChatFormatting.DARK_GRAY.toString() + "Animations > " + EnumChatFormatting.GRAY + "Make sure to check out /animations if you run into any issues.".replace(" ", " " + EnumChatFormatting.GRAY));

                    Minecraft.getMinecraft().thePlayer.addChatComponentMessage(chatComponent1);
                    MinecraftForge.EVENT_BUS.unregister(Animations.this);
                }

            }
        }, 2000L);
    }

    @EventHandler
    public void onInit(FMLInitializationEvent e) {
        Animations.proxy.init(e);
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent e) {
        Animations.proxy.postInit(e);
    }

    private boolean isObfuscated() {
        try {
            Minecraft.class.getDeclaredField("logger");
            return false;
        } catch (NoSuchFieldException nosuchfieldexception) {
            ;
        } catch (SecurityException securityexception) {
            securityexception.printStackTrace();
        }

        return true;
    }
}
