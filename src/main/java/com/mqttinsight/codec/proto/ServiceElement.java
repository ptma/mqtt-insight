package com.mqttinsight.codec.proto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
@AllArgsConstructor(staticName = "of")
public class ServiceElement implements Element {

    private String name;
    private String qualifiedName;
    private String documentation;
    private final List<OptionElement> options = new ArrayList<>();
    private final List<RpcElement> rpcs = new ArrayList<>();

    public String getDocumentation() {
        return documentation == null ? "" : documentation;
    }

    public void addRpc(RpcElement rpc) {
        rpcs.add(Element.checkNotNull(rpc, "rpc"));
    }

    public void addOption(OptionElement option) {
        options.add(Element.checkNotNull(option, "option"));
    }

    public ServiceElement validate() {
        Element.checkNotNull(name, "name");
        Element.checkNotNull(qualifiedName, "qualifiedName");

        return this;
    }
}
