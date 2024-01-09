package com.mqttinsight.codec.proto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.mqttinsight.codec.proto.MessageElement.validateFieldTagUniqueness;

@Getter
@AllArgsConstructor(staticName = "of")
public class ExtendElement implements Element {

    private String name;
    private String qualifiedName;
    private String documentation;
    private final List<FieldElement> fields = new ArrayList<>();

    public String getDocumentation() {
        return documentation == null ? "" : documentation;
    }

    public void addField(FieldElement field) {
        fields.add(Element.checkNotNull(field, "field"));
    }

    public ExtendElement validate() {
        Element.checkNotNull(name, "name");
        Element.checkNotNull(qualifiedName, "qualifiedName");
        validateFieldTagUniqueness(qualifiedName, fields, Collections.emptyList());
        return this;
    }

}
