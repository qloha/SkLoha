package io.github.qloha.skLoha.skript.cutscene;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CutsceneManager {

    private static final Map<String, Cutscene> CUTSCENES = new ConcurrentHashMap<>();

    public static Cutscene create(String name) {
        if (name == null) return null;
        Cutscene c = new Cutscene(name);
        CUTSCENES.put(name.toLowerCase(), c);
        return c;
    }

    public static Cutscene get(String name) {
        if (name == null) return null;
        return CUTSCENES.get(name.toLowerCase());
    }

    public static boolean exists(String name) {
        return get(name) != null;
    }

    public static void remove(String name) {
        if (name == null) return;
        CUTSCENES.remove(name.toLowerCase());
    }

    public static Map<String, Cutscene> all() {
        return CUTSCENES;
    }
}

