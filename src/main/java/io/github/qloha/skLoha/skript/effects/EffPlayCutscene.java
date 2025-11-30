package io.github.qloha.skLoha.skript.effects;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;

import io.github.qloha.skLoha.skript.cutscene.Cutscene;
import io.github.qloha.skLoha.skript.cutscene.CutsceneManager;

public class EffPlayCutscene extends Effect {

    static {
        try {
            Skript.registerEffect(EffPlayCutscene.class, "play cutscene %string% to %player%", "play cutscene %string%");
            System.out.println("[SkLoHa] EffPlayCutscene static init: registered");
        } catch (Throwable t) {
            System.err.println("[SkLoHa] EffPlayCutscene static init: failed to register: " + t.getMessage());
            t.printStackTrace();
        }
    }

    private Expression<String> cutsceneName;
    private Expression<Player> player;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parser) {
        this.cutsceneName = (Expression<String>) expressions[0];
        if (expressions.length > 1) this.player = (Expression<Player>) expressions[1];
        return true;
    }

    @Override
    public String toString(Event event, boolean debug) {
        return "play cutscene " + cutsceneName.toString(event, debug);
    }

    @Override
    protected void execute(Event event) {
        String name = cutsceneName.getSingle(event);
        if (name == null) return;
        Cutscene cs = CutsceneManager.get(name);
        if (cs == null) return;
        Player p = null;
        if (player != null) p = player.getSingle(event);
        if (p == null) return;
        cs.play(p);
    }
}
