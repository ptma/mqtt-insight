package com.mqttinsight.config;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.mqttinsight.codec.DynamicCodec;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Conf {
    private List<ConnectionNode> connections;
    private List<DynamicCodec> dynamicCodecs;

    private Map<String, Object> props = new HashMap<>();


    public List<ConnectionNode> getConnections() {
        if (connections == null) {
            connections = new ArrayList<>();
        }
        return connections;
    }

    public void setConnections(List<ConnectionNode> connections) {
        this.connections = connections;
    }

    public List<DynamicCodec> getDynamicCodecs() {
        if (dynamicCodecs == null) {
            dynamicCodecs = new ArrayList<>();
        }
        return dynamicCodecs;
    }

    public void setDynamicCodecs(List<DynamicCodec> dynamicCodecs) {
        this.dynamicCodecs = dynamicCodecs;
    }

    public Object get(String key) {
        return props.get(key);
    }

    public Object getOrDefault(String key, Object defaultValue) {
        return props.getOrDefault(key, defaultValue);
    }


    @JsonAnyGetter
    public Map<String, Object> any() {
        return this.props;
    }

    @JsonAnySetter
    public void set(String name, Object value) {
        this.props.put(name, value);
    }
}
