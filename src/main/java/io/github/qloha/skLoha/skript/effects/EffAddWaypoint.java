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

public class EffAddWaypoint extends Effect {

    static {
        Skript.registerEffect(EffAddWaypoint.class, "add %location% to waypoints of cutscene %string%", "add %locations% to waypoints of cutscene %string%");
    }

    private Expression<Location> location;
    private Expression<String> cutsceneName;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parser) {
        this.location = (Expression<Location>) expressions[0];
        this.cutsceneName = (Expression<String>) expressions[1];
        return true;
    }

    @Override
    public String toString(Event event, boolean debug) {
        return "add waypoint to cutscene " + cutsceneName.toString(event, debug);
    }

    @Override
    protected void execute(Event event) {
        String name = cutsceneName.getSingle(event);
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
