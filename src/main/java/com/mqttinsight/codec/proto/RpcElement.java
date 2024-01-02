package com.mqttinsight.codec.proto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

import static com.mqttinsight.codec.proto.DataType.NamedType;

@Getter
@AllArgsConstructor
public class RpcElement implements Element {

    private String name;
    private String documentation = "";
    private NamedType requestType;
    private NamedType responseType;
    private final List<OptionElement> options = new ArrayList<>();

    public static RpcElement of(String name, String documentation) {
        return new RpcElement(name, documentation, null, null);
    }

    public String getDocumentation() {
        return documentation == null ? "" : documentation;
    }

    public void setRequestType(NamedType requestType) {
        this.requestType = requestType;
    }

    public void setResponseType(NamedType responseType) {
        this.responseType = responseType;
    }

    public void addOption(OptionElement option) {
        options.add(Element.checkNotNull(option, "option"));
    }

    public RpcElement validate() {
        Element.checkNotNull(name, "name");
        Element.checkNotNull(requestType, "requestType");
        Element.checkNotNull(responseType, "responseType");

        return this;
    }
}
