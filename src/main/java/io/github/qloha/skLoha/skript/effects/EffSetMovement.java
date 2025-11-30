package io.github.qloha.skLoha.skript.effects;

import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;

import io.github.qloha.skLoha.skript.cutscene.Cutscene;
import io.github.qloha.skLoha.skript.cutscene.CutsceneManager;
import io.github.qloha.skLoha.skript.cutscene.Movement;
import io.github.qloha.skLoha.skript.cutscene.CutsceneContext;

public class EffSetMovement extends Effect {

    static {
        Skript.registerEffect(EffSetMovement.class, "set movement of cutscene %string% to %string%", "set movement of %string% to %string%", "set movement of cutscene to %string%");
    }

    private Expression<String> cutsceneName;
    private Expression<String> movement;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parser) {
        // allow optional cutscene name
        if (expressions.length == 2) {
            this.cutsceneName = (Expression<String>) expressions[0];
            this.movement = (Expression<String>) expressions[1];
        } else {
            this.movement = (Expression<String>) expressions[0];
        }
        return true;
    }

    @Override
    public String toString(Event event, boolean debug) {
        return "set movement of cutscene " + (cutsceneName != null ? cutsceneName.toString(event, debug) : "(implicit)") + " to " + movement.toString(event, debug);
    }

    @Override
    protected void execute(Event event) {
        String name = (cutsceneName != null) ? cutsceneName.getSingle(event) : CutsceneContext.peek();
        if (name == null) return;
        Cutscene cs = CutsceneManager.get(name);
        if (cs == null) return;
        String m = movement.getSingle(event);
        if (m == null) return;
        if (m.equalsIgnoreCase("teleport") || m.equalsIgnoreCase("tp")) cs.setMovement(Movement.TELEPORT);
        else if (m.equalsIgnoreCase("glide") || m.equalsIgnoreCase("walk")) cs.setMovement(Movement.GLIDE);
    }
}
