package com.teyvatarchons;

public record Spell(String name, Cast cast) {
    @FunctionalInterface
    public interface Cast {
        void run(Ctx c);
    }
}
