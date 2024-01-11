package com.mqttinsight.codec.proto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

@Getter
@AllArgsConstructor(staticName = "of")
public class MessageElement implements TypeElement {

    private String name;
    private String qualifiedName;
    private String documentation;
    private final List<FieldElement> fields = new ArrayList<>();
    private final List<OneOfElement> oneOfs = new ArrayList<>();
    private final List<TypeElement> nestedElements = new ArrayList<>();
    private final List<ExtensionsElement> extensions = new ArrayList<>();
    private final List<OptionElement> options = new ArrayList<>();

    private final AtomicInteger hitCount = new AtomicInteger(0);

    @Override
    public String getDocumentation() {
        return documentation == null ? "" : documentation;
    }

    @Override
    public List<TypeElement> getNestedElements() {
        return nestedElements;
    }

    public void addField(FieldElement field) {
        fields.add(Element.checkNotNull(field, "field"));
    }

    public void addOneOf(OneOfElement oneOf) {
        oneOfs.add(Element.checkNotNull(oneOf, "oneOf"));
    }

    public void addType(TypeElement type) {
        nestedElements.add(Element.checkNotNull(type, "type"));
    }

    public void addExtensions(ExtensionsElement extensions) {
        this.extensions.add(Element.checkNotNull(extensions, "extensions"));
    }

    public void addOption(OptionElement option) {
        options.add(Element.checkNotNull(option, "option"));
    }

    public int incrementHitCount() {
        return hitCount.incrementAndGet();
    }

    public int getHitCount() {
        return hitCount.get();
    }

    static void validateFieldTagUniqueness(String qualifiedName, List<FieldElement> fields, List<OneOfElement> oneOfs) {
        List<FieldElement> allFields = new ArrayList<>(fields);
        for (OneOfElement oneOf : oneOfs) {
            allFields.addAll(oneOf.getFields());
        }

        Set<Integer> tags = new LinkedHashSet<>();
        for (FieldElement field : allFields) {
            int tag = field.getTag();
            if (!tags.add(tag)) {
                throw new IllegalStateException("Duplicate tag " + tag + " in " + qualifiedName);
            }
        }
    }

    static void validateFieldLabel(String qualifiedName, List<FieldElement> fields) {
        for (FieldElement field : fields) {
            if (field.getLabel() == FieldElement.Label.ONE_OF) {
                throw new IllegalStateException("Field '" + field.getName() + "' in " + qualifiedName + " improperly declares itself a member of a 'oneof' group but is not.");
            }
        }
    }

    public MessageElement validate() {
        Element.checkNotNull(name, "name");
        Element.checkNotNull(qualifiedName, "qualifiedName");

        validateFieldTagUniqueness(qualifiedName, fields, oneOfs);
        validateFieldLabel(qualifiedName, fields);
        EnumElement.validateValueUniquenessInScope(qualifiedName, nestedElements);

        return this;
    }

}
