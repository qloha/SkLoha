package io.github.qloha.skLoha.skript.expressions;

import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;

import io.github.qloha.skLoha.skript.cutscene.Cutscene;
import io.github.qloha.skLoha.skript.cutscene.CutsceneManager;

@Description("Gets a cutscene by name")
public class ExprCutscene extends SimpleExpression<Cutscene> {

    static {
        Skript.registerExpression(ExprCutscene.class, Cutscene.class, ExpressionType.SIMPLE, "cutscene %string%");
    }

    private Expression<String> name;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parser) {
        this.name = (Expression<String>) exprs[0];
        return true;
    }

    @Override
    protected Cutscene[] get(Event e) {
        String n = name.getSingle(e);
        if (n == null) return new Cutscene[0];
        Cutscene c = CutsceneManager.get(n);
        if (c == null) return new Cutscene[0];
        return new Cutscene[]{c};
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends Cutscene> getReturnType() {
        return Cutscene.class;
    }

    @Override
    public String toString(Event e, boolean debug) {
        return "cutscene " + name.toString(e, debug);
    }
}
