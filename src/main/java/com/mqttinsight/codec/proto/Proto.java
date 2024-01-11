package com.mqttinsight.codec.proto;

import cn.hutool.core.io.FileUtil;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Getter
public class Proto {

    public static final int MIN_TAG_VALUE = 1;
    public static final int MAX_TAG_VALUE = (1 << 29) - 1; // 536,870,911
    public static final int RESERVED_TAG_VALUE_START = 19000;
    public static final int RESERVED_TAG_VALUE_END = 19999;

    private String filename;
    @Setter
    private String packageName;
    @Setter
    private Syntax syntax;

    private final List<String> dependencies = new ArrayList<>();
    private final List<String> publicDependencies = new ArrayList<>();
    private final List<TypeElement> types = new ArrayList<>();
    private final List<ServiceElement> services = new ArrayList<>();
    private final List<ExtendElement> extendDeclarations = new ArrayList<>();
    private final List<OptionElement> options = new ArrayList<>();

    public Proto(String filename) {
        this.filename = filename;
    }

    public String getFilename() {
        return FileUtil.getName(filename);
    }

    public void addDependency(String dependency) {
        dependencies.add(Element.checkNotNull(dependency, "dependency"));
    }

    public void addPublicDependency(String dependency) {
        publicDependencies.add(Element.checkNotNull(dependency, "dependency"));
    }

    public void addType(TypeElement type) {
        types.add(Element.checkNotNull(type, "type"));
    }

    public Optional<TypeElement> findType(String name) {
        return types.stream().filter(element -> element.getName().equals(name)).findFirst();
    }

    public void addService(ServiceElement service) {
        services.add(Element.checkNotNull(service, "service"));
    }

    public void addExtendDeclaration(ExtendElement extend) {
        extendDeclarations.add(Element.checkNotNull(extend, "extend"));
    }

    public void addOption(OptionElement option) {
        options.add(Element.checkNotNull(option, "option"));
    }

    public List<MessageElement> getMessages() {
        return types.stream()
            .filter(e -> e instanceof MessageElement)
            .map(e -> (MessageElement) e)
            .sorted(Comparator.comparingInt(MessageElement::getHitCount).reversed())
            .toList();
    }
}
