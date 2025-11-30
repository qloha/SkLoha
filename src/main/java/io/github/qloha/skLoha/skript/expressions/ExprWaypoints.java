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

public class ExprWaypoints extends SimpleExpression<Location> {

    static {
        Skript.registerExpression(ExprWaypoints.class, Location.class, ExpressionType.SIMPLE, "waypoints of %cutscene%", "%cutscene%'s waypoints");
    }

    private Expression<Cutscene> cutsceneExpr;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parser) {
        this.cutsceneExpr = (Expression<Cutscene>) exprs[0];
        return true;
    }

    @Override
    protected Location[] get(Event e) {
        Cutscene c = cutsceneExpr.getSingle(e);
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
        return "waypoints of " + cutsceneExpr.toString(e, debug);
    }
}
