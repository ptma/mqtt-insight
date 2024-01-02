package com.mqttinsight.codec.proto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
@AllArgsConstructor(staticName = "of")
public class EnumConstantElement implements Element {

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

    public EnumConstantElement validate() {
        Element.checkNotNull(name, "name");
        Element.checkNotNull(tag, "tag");
        return this;
    }

}
