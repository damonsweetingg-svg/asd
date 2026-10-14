package com.teyvatarchons;

import com.teyvatarchons.client.ClientInit;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;

@Mod(ArchonsMod.MODID)
public class ArchonsMod {
    public static final String MODID = "archons";

    @SuppressWarnings("removal")
    public ArchonsMod() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModItems.ITEMS.register(modBus);
        ModItems.TABS.register(modBus);

        MinecraftForge.EVENT_BUS.addListener(Tasks::onServerTick);
        MinecraftForge.EVENT_BUS.addListener(Tasks::onServerStopping);
        MinecraftForge.EVENT_BUS.addListener(Commands::register);
        MinecraftForge.EVENT_BUS.addListener(ArchonsMod::onClone);

        if (FMLEnvironment.dist == Dist.CLIENT) {
            ClientInit.init(modBus);
        }
    }

    private static void onClone(PlayerEvent.Clone e) {
        PlayerData.copy(e.getOriginal(), e.getEntity());
    }
}
