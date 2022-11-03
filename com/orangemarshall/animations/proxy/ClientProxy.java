package com.orangemarshall.animations.proxy;

import com.orangemarshall.animations.ArmorAnimation;
import com.orangemarshall.animations.BlockhitAnimation;
import com.orangemarshall.animations.HealthAnimation;
import com.orangemarshall.animations.config.CommandAnimationsConfig;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class ClientProxy extends CommonProxy {

    public void preInit(FMLPreInitializationEvent e) {
        super.preInit(e);
        MinecraftForge.EVENT_BUS.register(new ArmorAnimation());
        MinecraftForge.EVENT_BUS.register(new BlockhitAnimation());
        MinecraftForge.EVENT_BUS.register(new HealthAnimation());
        System.out.println("Hooked Old Animations");
    }

    public void init(FMLInitializationEvent e) {
        super.init(e);
        ClientCommandHandler.instance.registerCommand(new CommandAnimationsConfig());
    }

    public void postInit(FMLPostInitializationEvent e) {
        super.postInit(e);
    }
}
