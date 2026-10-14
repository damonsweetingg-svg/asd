package com.teyvatarchons;

import java.util.EnumMap;
import java.util.Map;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModItems {
    private ModItems() {}

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, ArchonsMod.MODID);
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ArchonsMod.MODID);

    public static final Map<Figure, RegistryObject<Item>> BY_FIGURE = new EnumMap<>(Figure.class);

    static {
        for (Figure f : Figure.values()) {
            BY_FIGURE.put(f, ITEMS.register(f.itemId,
                    () -> new FigureItem(f, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC).fireResistant())));
        }
    }

    public static final RegistryObject<CreativeModeTab> TAB = TABS.register("archons", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.archons"))
            .icon(() -> new ItemStack(BY_FIGURE.get(Figure.VENTI).get()))
            .displayItems((params, out) -> {
                for (Figure f : Figure.values()) out.accept(BY_FIGURE.get(f).get());
            })
            .build());
}
