package io.github.qloha.skLoha.skript.effects;

import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;

import io.github.qloha.skLoha.skript.cutscene.CutsceneContext;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class EffWithCutsceneSection extends Effect {

    static {
        Skript.registerEffect(EffWithCutsceneSection.class, "with cutscene %string%:");
    }

    private Expression<String> name;
    private Object parseResultObj;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parser) {
        this.name = (Expression<String>) expressions[0];
        this.parseResultObj = parser;
        return true;
    }

    @Override
    public String toString(Event e, boolean debug) {
        return "with cutscene " + name.toString(e, debug) + ":";
    }

    @Override
    protected void execute(Event e) {
        String n = name.getSingle(e);
        if (n == null) return;
        CutsceneContext.push(n);
        try {
            Object section = resolveSectionNode(parseResultObj);
            if (section != null) invokeExecuteOnSection(section, e);
        } catch (Exception ex) {
            // ignore
        } finally {
            CutsceneContext.pop();
        }
    }

    private Object resolveSectionNode(Object parseResult) {
        if (parseResult == null) return null;
        try {
            for (Method m : parseResult.getClass().getMethods()) {
                if (m.getParameterCount() == 0 && (m.getName().toLowerCase().contains("parse") || m.getName().toLowerCase().contains("mark") || m.getName().toLowerCase().contains("section"))) {
                    Object res = m.invoke(parseResult);
                    if (res != null && res.getClass().getName().toLowerCase().contains("section")) return res;
                }
            }
            for (Field f : parseResult.getClass().getDeclaredFields()) {
                f.setAccessible(true);
                Object val = f.get(parseResult);
                if (val != null && val.getClass().getName().toLowerCase().contains("section")) return val;
            }
        } catch (Throwable t) {
            // ignore
        }
        return null;
    }

    private void invokeExecuteOnSection(Object section, Event e) {
        if (section == null) return;
        try {
            for (Method m : section.getClass().getMethods()) {
                if (m.getName().equals("execute") && m.getParameterCount() == 1) {
                    m.invoke(section, e);
                    return;
                }
            }
        } catch (Throwable t) {
            // ignore
        }
    }
}
