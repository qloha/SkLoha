package io.github.qloha.skLoha.skript.effects;

import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;

import io.github.qloha.skLoha.skript.cutscene.CutsceneManager;

public class EffCreateCutscene extends Effect {

    static {
        Skript.registerEffect(EffCreateCutscene.class, "create cutscene %string%");
    }

    private Expression<String> name;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parser) {
        this.name = (Expression<String>) expressions[0];
        return true;
    }

    @Override
    public String toString(Event event, boolean debug) {
        return "create cutscene " + name.toString(event, debug);
    }

    @Override
    protected void execute(Event event) {
        String n = name.getSingle(event);
        if (n == null) return;
        CutsceneManager.create(n);
    }
}
