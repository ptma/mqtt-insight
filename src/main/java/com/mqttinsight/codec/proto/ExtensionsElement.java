package com.mqttinsight.codec.proto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ExtensionsElement implements Element {

    private int start;
    private int end;
    private String documentation;

    public static ExtensionsElement of(int start, int end, String documentation) {
        Element.checkArgument(Element.isValidTag(start), "Invalid start value: %s", start);
        Element.checkArgument(Element.isValidTag(end), "Invalid end value: %s", end);

        return new ExtensionsElement(start, end, documentation);
    }

    public String getDocumentation() {
        return documentation == null ? "" : documentation;
    }

}
