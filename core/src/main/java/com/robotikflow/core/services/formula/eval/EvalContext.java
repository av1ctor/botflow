package com.robotikflow.core.services.formula.eval;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class EvalContext implements Map<Object, Object>
{
    public final Map<Object, Object> symbols = new HashMap<>();

    public EvalContext()
    {
    }

    public EvalContext(Map<Object, Object> from)
    {
        symbols.putAll(from);
    }

    @Override
    public int size() {
        return symbols.size();
    }

    @Override
    public boolean isEmpty() {
        return symbols.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return symbols.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return symbols.containsValue(value);
    }

    @Override
    public Object get(Object key) {
        return symbols.get(key);
    }

    @Override
    public Object put(Object key, Object value) {
        return symbols.put(key, value);
    }

    @Override
    public Object remove(Object key) {
        return symbols.remove(key);
    }

    @Override
    public void putAll(Map<? extends Object, ? extends Object> m) {
        symbols.putAll(m);
    }

    @Override
    public void clear() {
        symbols.clear();
    }

    @Override
    public Set<Object> keySet() {
        return symbols.keySet();
    }

    @Override
    public Collection<Object> values() {
        return symbols.values();
    }

    @Override
    public Set<Entry<Object, Object>> entrySet() {
        return symbols.entrySet();
    }
}
