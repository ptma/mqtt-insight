package com.mqttinsight.codec.proto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
@AllArgsConstructor(staticName = "of")
public class FieldElement implements Element {


    private Label label;
    private DataType type;
    private String name;
    private Integer tag;
    private String documentation;
    private final List<OptionElement> options = new ArrayList<>();

    public String getDocumentation() {
        return documentation == null ? "" : documentation;
    }

    public void addOption(OptionElement option) {
        options.add(Element.checkNotNull(option, "option"));
    }

    public final boolean isDeprecated() {
        OptionElement deprecatedOption = OptionElement.findByName(getOptions(), "deprecated");
        return deprecatedOption != null && "true".equals(deprecatedOption.getValue());
    }

    public final boolean isPacked() {
        OptionElement packedOption = OptionElement.findByName(getOptions(), "packed");
        return packedOption != null && "true".equals(packedOption.getValue());
    }

    public final OptionElement getDefault() {
        return OptionElement.findByName(getOptions(), "default");
    }

    public final boolean hasDefault() {
        return getDefault() != null;
    }

    public FieldElement validate() {
        Element.checkNotNull(label, "label");
        Element.checkNotNull(type, "type");
        Element.checkNotNull(name, "name");
        Element.checkNotNull(tag, "tag");
        Element.checkArgument(Element.isValidTag(tag), "Illegal tag value: %s", tag);
        return this;
    }

    public enum Label {
        OPTIONAL, REQUIRED, REPEATED, ONE_OF
    }
}
