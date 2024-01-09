package com.mqttinsight.codec.proto;

import java.util.Collections;
import java.util.List;

public interface TypeElement extends Element {

    String getName();

    String getQualifiedName();

    String getDocumentation();

    List<OptionElement> getOptions();

    default List<TypeElement> getNestedElements() {
        return Collections.emptyList();
    }

}
