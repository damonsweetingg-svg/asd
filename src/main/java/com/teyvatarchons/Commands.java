package com.teyvatarchons;

import com.mojang.brigadier.arguments.IntegerArgumentType;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.RegisterCommandsEvent;

/**
 * /archons select <1-10>   - choose the spell for the figure item in your hands (used by the spell wheel)
 * /archons unlockall       - (op) unlock every figure for yourself
 */
public final class Commands {
    private Commands() {}

    public static void register(RegisterCommandsEvent e) {
        e.getDispatcher().register(net.minecraft.commands.Commands.literal("archons")
                .then(net.minecraft.commands.Commands.literal("select")
                        .then(net.minecraft.commands.Commands.argument("spell", IntegerArgumentType.integer(1, 10))
                                .executes(ctx -> select(ctx.getSource().getPlayerOrException(), IntegerArgumentType.getInteger(ctx, "spell")))))
                .then(net.minecraft.commands.Commands.literal("unlockall")
                        .requires(s -> s.hasPermission(2))
                        .executes(ctx -> {
                            ServerPlayer p = ctx.getSource().getPlayerOrException();
                            for (Figure f : Figure.values()) PlayerData.unlock(p, f);
                            p.sendSystemMessage(Component.literal("All Archons and Shades unlocked."));
                            return 1;
                        })));
    }

    private static Figure held(ServerPlayer p) {
        for (ItemStack s : new ItemStack[] {p.getMainHandItem(), p.getOffhandItem()}) {
            if (s.getItem() instanceof FigureItem fi) return fi.figure();
        }
        return null;
    }

    private static int select(ServerPlayer p, int n) {
        Figure f = held(p);
        if (f == null) {
            p.sendSystemMessage(Component.literal("Hold a Gnosis or Shade relic first."));
            return 0;
        }
        if (!PlayerData.unlocked(p, f)) {
            p.sendSystemMessage(Component.literal("Right-click the item once to unlock it first."));
            return 0;
        }
        PlayerData.select(p, f, n - 1);
        p.displayClientMessage(Component.literal(n + "/10  " + f.spells()[n - 1].name())
                .withStyle(net.minecraft.network.chat.Style.EMPTY.withColor(f.rgb)), true);
        return 1;
    }
}
