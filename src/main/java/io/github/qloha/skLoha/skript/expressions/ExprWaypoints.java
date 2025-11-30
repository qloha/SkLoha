package io.github.qloha.skLoha.skript.expressions;

import org.bukkit.Location;
import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;

import io.github.qloha.skLoha.skript.cutscene.Cutscene;
import io.github.qloha.skLoha.skript.cutscene.CutsceneManager;

public class ExprWaypoints extends SimpleExpression<Location> {

    static {
        Skript.registerExpression(ExprWaypoints.class, Location.class, ExpressionType.SIMPLE, "waypoints of %string%", "%string%'s waypoints");
    }

    private Expression<String> cutsceneNameExpr;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parser) {
        this.cutsceneNameExpr = (Expression<String>) exprs[0];
        return true;
    }

    @Override
    protected Location[] get(Event e) {
        String name = cutsceneNameExpr.getSingle(e);
        if (name == null) return new Location[0];
        Cutscene c = CutsceneManager.get(name);
        if (c == null) return new Location[0];
        return c.getWaypoints().toArray(new Location[0]);
    }

    @Override
    public boolean isSingle() {
        return false;
    }

    @Override
    public Class<? extends Location> getReturnType() {
        return Location.class;
    }

    @Override
    public String toString(Event e, boolean debug) {
        return "waypoints of " + cutsceneNameExpr.toString(e, debug);
    }
}
