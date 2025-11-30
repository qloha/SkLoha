package io.github.qloha.skLoha.skript.effects;

import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;

import io.github.qloha.skLoha.skript.cutscene.Cutscene;
import io.github.qloha.skLoha.skript.cutscene.CutsceneManager;
import io.github.qloha.skLoha.skript.cutscene.CutsceneContext;

public class EffSetMovementInterval extends Effect {

    static {
        try {
            Skript.registerEffect(EffSetMovementInterval.class, "set movement interval of cutscene %string% to %number% (seconds|secs|s)", "set movement interval of %number% (seconds|secs|s)");
            System.out.println("[SkLoha] EffSetMovementInterval static init: registered");
        } catch (Throwable t) {
            System.err.println("[SkLoha] EffSetMovementInterval static init: failed to register: " + t.getMessage());
            t.printStackTrace();
        }
    }

    private Expression<String> cutsceneName;
    private Expression<Number> seconds;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parser) {
        if (expressions.length == 2) {
            this.cutsceneName = (Expression<String>) expressions[0];
            this.seconds = (Expression<Number>) expressions[1];
        } else {
            this.seconds = (Expression<Number>) expressions[0];
        }
        return true;
    }

    @Override
    public String toString(Event event, boolean debug) {
        return "set movement interval of cutscene " + (cutsceneName != null ? cutsceneName.toString(event, debug) : "(implicit)") + " to " + seconds.toString(event, debug);
    }

    @Override
    protected void execute(Event event) {
        String name = (cutsceneName != null) ? cutsceneName.getSingle(event) : CutsceneContext.peek();
        if (name == null) return;
        Cutscene cs = CutsceneManager.get(name);
        if (cs == null) return;
        Number n = seconds.getSingle(event);
        if (n == null) return;
        cs.setIntervalSeconds(n.doubleValue());
    }
}
