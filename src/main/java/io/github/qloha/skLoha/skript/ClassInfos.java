package io.github.qloha.skLoha.skript;

import ch.njol.skript.registrations.Classes;
import ch.njol.skript.classes.ClassInfo;

import io.github.qloha.skLoha.skript.cutscene.Cutscene;

/**
 * Registers Skript ClassInfo for Cutscene at runtime.
 */
public class ClassInfos {
    static {
        try {
            Classes.registerClass(new ClassInfo<>(Cutscene.class, "cutscene")
                    .user("cutscenes?")
                    .name("Cutscene")
                    .description("Represents a cutscene created by SkLoha")
                    .examples("create cutscene \"intro\"")
            );
        } catch (Throwable t) {
            // Skript may not be present at compile/load time; ignore failures silently
        }
    }
}
