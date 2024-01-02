package com.mqttinsight.codec;

import lombok.Getter;
import lombok.Setter;

/**
 * @author ptma
 */
@Getter
@Setter
public class DynamicCodec {

    private String name;
    private String type;
    private String schemaFile;

    public DynamicCodec(String name, String type, String schemaFile) {
        this.name = name;
        this.type = type;
        this.schemaFile = schemaFile;
    }
}
