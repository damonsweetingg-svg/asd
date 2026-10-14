package com.teyvatarchons;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.minecraft.server.MinecraftServer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.server.ServerLifecycleHooks;

/** Tiny server-side scheduler. Spell animations are Tasks that get ticked once per server tick. */
public final class Tasks {
    private Tasks() {}

    /** Return true when finished. age starts at 1 on the first tick after scheduling. */
    @FunctionalInterface
    public interface Task {
        boolean tick(int age);
    }

    private static final class Entry {
        final Task task;
        int age = 0;
        Entry(Task t) { this.task = t; }
    }

    private static final List<Entry> ACTIVE = new ArrayList<>();
    private static final List<Entry> PENDING = new ArrayList<>();
    private static int lastTick = -1;

    public static void add(Task t) {
        PENDING.add(new Entry(t));
    }

    public static void after(int delay, Runnable r) {
        add(age -> {
            if (age >= delay) {
                r.run();
                return true;
            }
            return false;
        });
    }

    /** Listens on the base ServerTickEvent; de-dupes by server tick count so it works whether or not phases exist. */
    public static void onServerTick(TickEvent.ServerTickEvent e) {
        MinecraftServer s = ServerLifecycleHooks.getCurrentServer();
        if (s == null) return;
        int t = s.getTickCount();
        if (t == lastTick) return;
        lastTick = t;
        ACTIVE.addAll(PENDING);
        PENDING.clear();
        Iterator<Entry> it = ACTIVE.iterator();
        while (it.hasNext()) {
            Entry en = it.next();
            en.age++;
            try {
                if (en.task.tick(en.age)) it.remove();
            } catch (Throwable ex) {
                ex.printStackTrace();
                it.remove();
            }
        }
        ACTIVE.addAll(PENDING);
        PENDING.clear();
    }

    public static void onServerStopping(ServerStoppingEvent e) {
        ACTIVE.clear();
        PENDING.clear();
        lastTick = -1;
    }
}
