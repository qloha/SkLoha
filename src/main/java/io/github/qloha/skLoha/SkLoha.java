package io.github.qloha.skLoha;

import org.bukkit.plugin.java.JavaPlugin;

public final class SkLoha extends JavaPlugin {

    private static SkLoha instance;
    private Object addon;

    @Override
    public void onEnable() {
        instance = this;
        // Try to register Skript addon via reflection so compilation doesn't require Skript on classpath here
        try {
            Class<?> skriptClass = Class.forName("ch.njol.skript.Skript");
            java.lang.reflect.Method registerMethod = skriptClass.getMethod("registerAddon", org.bukkit.plugin.Plugin.class);
            addon = registerMethod.invoke(null, this);
            if (addon != null) {
                // try to call loadClasses(String) or loadClasses(String, String)
                try {
                    java.lang.reflect.Method loadSingle = addon.getClass().getMethod("loadClasses", String.class);
                    loadSingle.invoke(addon, "io.github.qloha.skLoha.skript");
                } catch (NoSuchMethodException e) {
                    try {
                        java.lang.reflect.Method loadTwo = addon.getClass().getMethod("loadClasses", String.class, String.class);
                        loadTwo.invoke(addon, "io.github.qloha.skLoha", "skript");
                    } catch (NoSuchMethodException ex) {
                        // method not found; ignore — classes may be loaded elsewhere
                    }
                }
            }
        } catch (ClassNotFoundException e) {
            // Skript not present on server — that's fine, addon features will be unavailable
            getLogger().info("Skript not found, Skript addon will not be registered.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public static SkLoha getInstance() {
        return instance;
    }

    public Object getAddon() {
        return addon;
    }
}
