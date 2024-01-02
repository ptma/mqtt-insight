package com.mqttinsight.codec.proto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Getter
@AllArgsConstructor(staticName = "of")
public class EnumElement implements TypeElement {

    private String name;
    private String qualifiedName;
    private String documentation;
    private final List<EnumConstantElement> constants = new ArrayList<>();
    private final List<OptionElement> options = new ArrayList<>();

    @Override
    public String getDocumentation() {
        return documentation == null ? "" : documentation;
    }

    public void addConstant(EnumConstantElement constant) {
        constants.add(Element.checkNotNull(constant, "constant"));
    }

    public void addOption(OptionElement option) {
        options.add(Element.checkNotNull(option, "option"));
    }

    private void validateTagUniqueness(String qualifiedName, List<EnumConstantElement> constants) {
        Element.checkNotNull(qualifiedName, "qualifiedName");

        Set<Integer> tags = new LinkedHashSet<>();
        for (EnumConstantElement constant : constants) {
            int tag = constant.getTag();
            if (!tags.add(tag)) {
                throw new IllegalStateException("Duplicate tag " + tag + " in " + qualifiedName);
            }
        }
    }

    private boolean parseAllowAlias(List<OptionElement> options) {
        OptionElement option = OptionElement.findByName(options, "allow_alias");
        return option != null && "true".equals(option.getValue());
    }

    public static void validateValueUniquenessInScope(String qualifiedName, List<TypeElement> nestedElements) {
        Set<String> names = new LinkedHashSet<>();
        for (TypeElement nestedElement : nestedElements) {
            if (nestedElement instanceof EnumElement) {
                EnumElement enumElement = (EnumElement) nestedElement;
                for (EnumConstantElement constant : enumElement.getConstants()) {
                    String name = constant.getName();
                    if (!names.add(name)) {
                        throw new IllegalStateException(
                            "Duplicate enum constant " + name + " in scope " + qualifiedName);
                    }
                }
            }
        }
    }

    public EnumElement validate() {
        Element.checkNotNull(name, "name");
        Element.checkNotNull(qualifiedName, "qualifiedName");

        if (!parseAllowAlias(options)) {
            validateTagUniqueness(qualifiedName, constants);
        }
        return this;
    }

}
