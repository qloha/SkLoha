package io.github.qloha.skLoha.skript.cutscene;

import java.util.ArrayDeque;
import java.util.Deque;

public class CutsceneContext {

    private static final ThreadLocal<Deque<String>> CTX = ThreadLocal.withInitial(ArrayDeque::new);

    public static void push(String name) {
        if (name == null) return;
        CTX.get().push(name);
    }

    public static String pop() {
        Deque<String> dq = CTX.get();
        return dq.isEmpty() ? null : dq.pop();
    }

    public static String peek() {
        Deque<String> dq = CTX.get();
        return dq.isEmpty() ? null : dq.peek();
    }

}

