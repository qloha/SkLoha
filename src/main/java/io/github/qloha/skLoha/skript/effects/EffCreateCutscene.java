package io.github.qloha.skLoha.skript.effects;

import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;

import io.github.qloha.skLoha.skript.cutscene.CutsceneManager;
import io.github.qloha.skLoha.skript.cutscene.CutsceneContext;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class EffCreateCutscene extends Effect {

    static {
        // Register only the simple non-section form here. The section form is handled in EffCreateCutsceneSection.
        Skript.registerEffect(EffCreateCutscene.class, "create cutscene %string%");
    }

    private Expression<String> name;
    private Object parseResultObj; // still store parser though unused for non-section

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parser) {
        this.name = (Expression<String>) expressions[0];
        this.parseResultObj = parser; // not used for simple form
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
