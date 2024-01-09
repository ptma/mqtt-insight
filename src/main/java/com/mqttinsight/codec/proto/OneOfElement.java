package com.mqttinsight.codec.proto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
@AllArgsConstructor(staticName = "of")
public class OneOfElement implements Element {

    private String name;
    private String documentation;
    private final List<FieldElement> fields = new ArrayList<>();

    public String getDocumentation() {
        return documentation == null ? "" : documentation;
    }

    public void addField(FieldElement field) {
        fields.add(Element.checkNotNull(field, "field"));
    }

    public OneOfElement validate() {
        Element.checkNotNull(name, "name");
        return this;
    }
}
