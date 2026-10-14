package com.teyvatarchons.client;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.platform.InputConstants;
import com.teyvatarchons.Figure;
import com.teyvatarchons.FigureItem;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;

public final class ClientInit {
    private ClientInit() {}

    public static final KeyMapping WHEEL = new KeyMapping("key.archons.wheel", InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_V, "key.categories.archons");

    public static void init(IEventBus modBus) {
        modBus.addListener(ClientInit::registerKeys);
        MinecraftForge.EVENT_BUS.addListener(ClientInit::onKey);
    }

    private static void registerKeys(RegisterKeyMappingsEvent e) {
        e.register(WHEEL);
    }

    private static void onKey(InputEvent.Key e) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.screen != null) return;
        while (WHEEL.consumeClick()) {
            for (ItemStack s : new ItemStack[] {mc.player.getMainHandItem(), mc.player.getOffhandItem()}) {
                if (s.getItem() instanceof FigureItem fi) {
                    Figure f = fi.figure();
                    mc.setScreen(new WheelScreen(f));
                    return;
                }
            }
        }
    }
}
