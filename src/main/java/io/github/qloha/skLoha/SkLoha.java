package io.github.qloha.skLoha;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public final class SkLoha extends JavaPlugin {

    private static SkLoha instance;
    private Object addon;
    private int registrationAttempts = 0;
    private static final int MAX_ATTEMPTS = 20;
    private BukkitTask registrationTask = null;

    @Override
    public void onEnable() {
        instance = this;
        tryRegisterOrDefer();
    }

    private void tryRegisterOrDefer() {
        try {
            Class<?> skriptClass = Class.forName("ch.njol.skript.Skript");
            // If Skript provides isAcceptRegistrations(), check it first
            boolean canRegister = true;
            try {
                Method accept = skriptClass.getMethod("isAcceptRegistrations");
                Object ok = accept.invoke(null);
                if (ok instanceof Boolean && !((Boolean) ok)) {
                    canRegister = false;
                }
            } catch (NoSuchMethodException ignored) {
                // method not present; assume OK and attempt registration
            }

            if (!canRegister) {
                getLogger().warning("Skript is not accepting registrations right now; will retry when Skript becomes available.");
                // Listen for Skript enable and also schedule repeating attempts
                Bukkit.getPluginManager().registerEvents(new Listener() {
                    @EventHandler
                    public void onPluginEnable(PluginEnableEvent event) {
                        Plugin p = event.getPlugin();
                        if (p != null && "Skript".equalsIgnoreCase(p.getName())) {
                            getLogger().info("Detected Skript plugin enable; attempting registration...");
                            tryRegisterNow();
                        }
                    }
                }, this);
                // Also schedule periodic attempts in case Skript is already enabled shortly
                attemptRegistrationPeriodically();
                return;
            }
            // Otherwise attempt now
            tryRegisterNow();
        } catch (ClassNotFoundException e) {
            getLogger().info("Skript not found, Skript addon will not be registered.");
        } catch (InvocationTargetException ite) {
            Throwable cause = ite.getCause();
            if (cause != null && cause.getClass().getName().equals("ch.njol.skript.SkriptAPIException")) {
                getLogger().warning("Skript refused addon registration: " + cause.getMessage());
                attemptRegistrationPeriodically();
                return;
            }
            ite.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void attemptRegistrationPeriodically() {
        // Try every 1 second up to MAX_ATTEMPTS
        if (registrationTask != null) return; // already scheduled
        registrationTask = Bukkit.getScheduler().runTaskTimer(this, () -> {
            if (addon != null) {
                // cancel the task if registration succeeded elsewhere
                if (registrationTask != null) registrationTask.cancel();
                registrationTask = null;
                return;
            }
            if (registrationAttempts++ > MAX_ATTEMPTS) {
                getLogger().warning("Exceeded max registration attempts; giving up for now.");
                if (registrationTask != null) registrationTask.cancel();
                registrationTask = null;
                return;
            }
            getLogger().info("Registration attempt " + registrationAttempts + " of " + MAX_ATTEMPTS);
            try {
                tryRegisterNow();
            } catch (Exception e) {
                // swallow; will retry until max attempts
            }
        }, 20L, 20L);
    }

    private void tryRegisterNow() {
        try {
            Class<?> skriptClass = Class.forName("ch.njol.skript.Skript");
            // check accept registrations if available
            try {
                Method accept = skriptClass.getMethod("isAcceptRegistrations");
                Object ok = accept.invoke(null);
                if (ok instanceof Boolean && !((Boolean) ok)) {
                    getLogger().warning("Skript is not accepting registrations at this time");
                    return;
                }
            } catch (NoSuchMethodException ignored) {
                // ignore
            }

            Method registerMethod = null;
            for (Method m : skriptClass.getDeclaredMethods()) {
                if (!m.getName().equals("registerAddon")) continue;
                if (!Modifier.isStatic(m.getModifiers())) continue;
                if (m.getParameterCount() != 1) continue;
                registerMethod = m;
            }

            if (registerMethod != null) {
                registerMethod.setAccessible(true);
                try {
                    addon = registerMethod.invoke(null, this);
                } catch (IllegalArgumentException iae) {
                    Class<?> param = registerMethod.getParameterTypes()[0];
                    Object arg = null;
                    if (param == Class.class) arg = this.getClass();
                    else if (param == String.class) arg = this.getName();
                    else if (param.isAssignableFrom(this.getClass())) arg = this;
                    if (arg != null) {
                        addon = registerMethod.invoke(null, arg);
                    } else {
                        getLogger().warning("Found Skript.registerAddon, but couldn't call it with available arguments.");
                    }
                } catch (InvocationTargetException ite) {
                    Throwable cause = ite.getCause();
                    if (cause != null && cause.getClass().getName().equals("ch.njol.skript.SkriptAPIException")) {
                        getLogger().warning("Skript refused addon registration: " + cause.getMessage());
                        return;
                    }
                    throw ite;
                }
            } else {
                getLogger().info("No compatible registerAddon method found on Skript; skipping addon registration.");
            }

            if (addon != null) {
                // ClassInfo registration for 'cutscene' is handled by the runtime ClassInfos placeholder or via SkriptAddon.loadClasses.
                // Avoid direct compile-time references to Skript Parser types here to keep compilation independent of Skript jar.
                // Attempt to call loadClasses if available
                Method loadSingle = null;
                for (Method m : addon.getClass().getMethods()) {
                    if (!m.getName().equals("loadClasses")) continue;
                    if (m.getParameterCount() == 1 && m.getParameterTypes()[0] == String.class) {
                        loadSingle = m;
                        break;
                    }
                    if (m.getParameterCount() == 2 && m.getParameterTypes()[0] == String.class && m.getParameterTypes()[1] == String.class) {
                        loadSingle = m;
                        break;
                    }
                }
                if (loadSingle != null) {
                    try {
                        if (loadSingle.getParameterCount() == 1) loadSingle.invoke(addon, "io.github.qloha.skLoha.skript");
                        else loadSingle.invoke(addon, "io.github.qloha.skLoha", "skript");
                    } catch (Exception e) {
                        getLogger().warning("Failed to invoke addon.loadClasses(...) method: " + e.getMessage());
                    }
                }
                // As an extra step, explicitly load our Skript classes to ensure their static initializers run
                String[] classesToLoad = new String[] {
                        "io.github.qloha.skLoha.skript.ClassInfos",
                        "io.github.qloha.skLoha.skript.effects.EffAddWaypoint",
                        "io.github.qloha.skLoha.skript.effects.EffCreateCutscene",
                        "io.github.qloha.skLoha.skript.effects.EffCreateCutsceneSection",
                        "io.github.qloha.skLoha.skript.effects.EffWithCutsceneSection",
                        "io.github.qloha.skLoha.skript.effects.EffPlayCutscene",
                        "io.github.qloha.skLoha.skript.effects.EffSetMovement",
                        "io.github.qloha.skLoha.skript.effects.EffSetMovementInterval",
                        "io.github.qloha.skLoha.skript.expressions.ExprCutscene",
                        "io.github.qloha.skLoha.skript.expressions.ExprWaypoints"
                };
                for (String cls : classesToLoad) {
                    try {
                        Class.forName(cls, true, this.getClass().getClassLoader());
                        getLogger().info("Loaded Skript class: " + cls);
                    } catch (ClassNotFoundException cnf) {
                        getLogger().warning("Could not find Skript class to load: " + cls + " (" + cnf.getMessage() + ")");
                    } catch (Throwable t) {
                        getLogger().warning("Failed loading Skript class " + cls + ": " + t.getMessage());
                    }
                }
                getLogger().info("Skript addon registered successfully.");
                // cancel periodic task if running
                if (registrationTask != null) {
                    registrationTask.cancel();
                    registrationTask = null;
                }
            }
        } catch (ClassNotFoundException e) {
            // Skript not present yet
        } catch (InvocationTargetException ite) {
            Throwable cause = ite.getCause();
            if (cause != null && cause.getClass().getName().equals("ch.njol.skript.SkriptAPIException")) {
                getLogger().warning("Skript refused addon registration: " + cause.getMessage());
                return;
            }
            ite.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        if (registrationTask != null) {
            registrationTask.cancel();
            registrationTask = null;
        }
    }

    public static SkLoha getInstance() {
        return instance;
    }

    public Object getAddon() {
        return addon;
    }
}
