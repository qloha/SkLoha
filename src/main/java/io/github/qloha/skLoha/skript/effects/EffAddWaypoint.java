package io.github.qloha.skLoha.skript.effects;

import org.bukkit.Location;
import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;

import io.github.qloha.skLoha.skript.cutscene.Cutscene;
import io.github.qloha.skLoha.skript.cutscene.CutsceneManager;
import io.github.qloha.skLoha.skript.cutscene.CutsceneContext;

public class EffAddWaypoint extends Effect {

    static {
        try {
            Skript.registerEffect(EffAddWaypoint.class, "add %location% to waypoints of cutscene %string%", "add %locations% to waypoints of cutscene %string%", "add %location% to waypoints", "add %locations% to waypoints");
            System.out.println("[SkLoha] EffAddWaypoint static init: registered");
        } catch (Throwable t) {
            System.out.println("[SkLoha] EffAddWaypoint static init: failed to register: " + t.getMessage());
        }
    }

    private Expression<Location> location;
    private Expression<String> cutsceneName;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parser) {
        this.location = (Expression<Location>) expressions[0];
        // second expression may be null (optional)
        if (expressions.length > 1) this.cutsceneName = (Expression<String>) expressions[1];
        return true;
    }

    @Override
    public String toString(Event event, boolean debug) {
        return "add waypoint to cutscene " + (cutsceneName != null ? cutsceneName.toString(event, debug) : "(implicit)");
    }

    @Override
    protected void execute(Event event) {
        String name = (cutsceneName != null) ? cutsceneName.getSingle(event) : CutsceneContext.peek();
        if (name == null) return;
        Cutscene cs = CutsceneManager.get(name);
        if (cs == null) return;
        Location[] locs = location.getAll(event);
        if (locs != null && locs.length > 0) {
            for (Location l : locs) {
                if (l != null) cs.addWaypoint(l);
            }
        }
    }
}
