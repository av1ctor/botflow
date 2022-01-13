package com.robotikflow.core.models.misc;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ObjFields 
    implements Map<String, Object>
{
    private final Map<String, Object> fields;

    public ObjFields()
    {
        fields = new HashMap<>();
    }

    public ObjFields(Map<String, Object> from)
    {
        fields = new HashMap<>(from);
    }

    @Override
    public int size() {
        return fields.size();
    }

    @Override
    public boolean isEmpty() {
        return fields.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return fields.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return fields.containsValue(value);
    }

    @Override
    public Object get(Object key) {
        return fields.get(key);
    }

    public String getString(String key) {
        return (String)fields.get(key);
    }

    public boolean getBoolean(String key) {
        return fields.containsKey(key)?
            (Boolean)fields.get(key):
            false;
    }

    public int getInt(String key) {
        return fields.containsKey(key)?
            (Integer)fields.get(key):
            0;
    }

    public float getFloat(String key) {
        return fields.containsKey(key)?
            (Float)fields.get(key):
            0.0f;
    }

    public double getDouble(String key) {
        return fields.containsKey(key)?
            (Double)fields.get(key):
            0.0;
    }

    public ObjFields getObjFields(String key) {
        return (ObjFields)fields.get(key);
    }

    public Map<String, Object> getMap(String key) {
        @SuppressWarnings("unchecked")
        var res = (Map<String, Object>)fields.get(key);
        return res;
    }

    @Override
    public Object put(String key, Object value) {
        return fields.put(key, value);
    }

    @Override
    public Object remove(Object key) {
        return fields.remove(key);
    }

    @Override
    public void putAll(Map<? extends String, ? extends Object> m) {
        fields.putAll(m);
    }

    @Override
    public void clear() {
        fields.clear();
    }

    @Override
    public Set<String> keySet() {
        return fields.keySet();
    }

    @Override
    public Collection<Object> values() {
        return fields.values();
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        return fields.entrySet();
    }

    public Map<String, Object> getFields() {
        return fields;
    }
}
