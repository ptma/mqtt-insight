package com.mqttinsight.codec.proto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(staticName = "of")
@Getter
public class MappingField {

    private String key;

    private String title;

    private int percentageWidth;
}
