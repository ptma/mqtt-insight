package com.mqttinsight.codec;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author ptma
 */
@Getter
@Setter
public class DynamicCodec {

    private String name;
    private String type;
    private String schemaFile;
    private List<Map<String, String>> mappings = new ArrayList<>();

    public DynamicCodec() {
    }

    public DynamicCodec(String name, String type, String schemaFile) {
        this.name = name;
        this.type = type;
        this.schemaFile = schemaFile;
    }
}
