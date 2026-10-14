package com.teyvatarchons;

import java.util.List;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * One class for all eleven items (seven Gnoses + four Shade relics).
 *  - First right-click: unlocks the figure's ten spells.
 *  - Right-click: casts the selected spell (no mana, no cooldown).
 *  - Shift + right-click: cycle to the next spell.   (Or press V for the spell wheel.)
 */
public class FigureItem extends Item {
    private final Figure figure;

    public FigureItem(Figure figure, Properties props) {
        super(props);
        this.figure = figure;
    }

    public Figure figure() {
        return figure;
    }

    private static net.minecraft.network.chat.Style colour(int rgb) {
        return net.minecraft.network.chat.Style.EMPTY.withColor(rgb);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide() || !(player instanceof ServerPlayer sp)) {
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }
        ServerLevel lv = sp.serverLevel();

        if (!PlayerData.unlocked(sp, figure)) {
            PlayerData.unlock(sp, figure);
            Vec3 pos = sp.position();
            Fx.sound(lv, pos, "ui.toast.challenge_complete", 1.2f, 1.0f);
            Fx.sound(lv, pos, "block.beacon.activate", 1.5f, 1.2f);
            for (int i = 0; i < 4; i++) Fx.helix(lv, FigureSpells.styleOf(figure).a, pos, 1.2 - i * 0.2, 3.5, 3, 60, i * 1.57);
            Fx.burst(lv, FigureSpells.styleOf(figure).dust, pos.add(0, 1, 0), 80, 1.5, 0.2);
            sp.sendSystemMessage(Component.literal("You have unlocked the power of " + figure.display + " (" + figure.title + ")!")
                    .withStyle(colour(figure.rgb)));
            sp.sendSystemMessage(Component.literal("Right-click to cast. Shift+right-click or [V] to change spell.")
                    .withStyle(net.minecraft.network.chat.Style.EMPTY.withColor(0xAAAAAA)));
            return InteractionResultHolder.sidedSuccess(stack, false);
        }

        Spell[] spells = figure.spells();
        int sel = PlayerData.selected(sp, figure);
        if (sp.isShiftKeyDown()) {
            sel = (sel + 1) % spells.length;
            PlayerData.select(sp, figure, sel);
            sp.displayClientMessage(Component.literal((sel + 1) + "/10  " + spells[sel].name()).withStyle(colour(figure.rgb)), true);
            return InteractionResultHolder.sidedSuccess(stack, false);
        }

        sp.displayClientMessage(Component.literal(spells[sel].name()).withStyle(colour(figure.rgb)), true);
        spells[sel].cast().run(new Ctx(sp));
        return InteractionResultHolder.sidedSuccess(stack, false);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (level.isClientSide() || !(entity instanceof ServerPlayer sp)) return;
        if (!(selected || sp.getOffhandItem() == stack)) return;
        if (PlayerData.unlocked(sp, figure)) {
            Auras.tick(sp, figure);
        } else if (level.getGameTime() % 10 == 0) {
            Fx.burst(sp.serverLevel(), FigureSpells.styleOf(figure).dust, sp.position().add(0, 1.4, 0), 3, 0.5, 0.0);
        }
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext ctx, List<Component> tip, TooltipFlag flag) {
        tip.add(Component.literal(figure.title).withStyle(colour(figure.rgb)));
        tip.add(Component.literal("Right-click to unlock, then to cast.").withStyle(net.minecraft.network.chat.Style.EMPTY.withColor(0xAAAAAA)));
        tip.add(Component.literal("Shift+right-click / [V] wheel: choose spell").withStyle(net.minecraft.network.chat.Style.EMPTY.withColor(0xAAAAAA)));
        Spell[] sp = figure.spells();
        for (int i = 0; i < sp.length; i++) {
            tip.add(Component.literal((i + 1) + ". " + sp[i].name()).withStyle(net.minecraft.network.chat.Style.EMPTY.withColor(0x777777)));
        }
    }
}
