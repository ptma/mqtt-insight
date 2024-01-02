package com.mqttinsight.codec.proto;

import cn.hutool.json.JSONObject;
import com.google.protobuf.Descriptors;
import com.google.protobuf.Descriptors.DescriptorValidationException;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.InvalidProtocolBufferException;
import com.mqttinsight.exception.SchemaLoadException;
import lombok.extern.slf4j.Slf4j;

import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class DynamicProtoSchema {

    private final DynamicSchema dynamicSchema;

    private final Proto proto;

    public DynamicProtoSchema(String protoFilePath) throws SchemaLoadException {
        try {
            proto = ProtoParser.parse(protoFilePath);
            dynamicSchema = getDynamicSchema(proto);
        } catch (Exception e) {
            throw new SchemaLoadException(e.getMessage(), e);
        }
    }

    public String getFilename() {
        return proto.getFilename();
    }

    public JSONObject parse(byte[] binary) throws ProtoParseException {
        DynamicMessage dynamicMessage = null;
        MessageElement messageElement = null;
        for (TypeElement type : proto.getTypes()) {
            if (type instanceof MessageElement) {
                DynamicMessage tempMessage = tryBuildMessage(type.getName(), binary);
                if (tempMessage != null) {
                    dynamicMessage = tempMessage;
                    messageElement = (MessageElement) type;
                    if (dynamicMessage.getUnknownFields().asMap().isEmpty()) {
                        break;
                    }
                }
            }
        }
        if (dynamicMessage == null) {
            throw new ProtoParseException("Cannot find proper schema for this protocol buffer message, maybe provide worng schema file");
        }

        JSONObject json = new JSONObject();
        dynamicMessage.getAllFields().forEach((k, v) -> {
            json.set(k.getName(), v);
        });
        if (dynamicMessage.getAllFields().size() != messageElement.getFields().size()) {
            Set<String> fieldSet = dynamicMessage.getAllFields()
                .keySet()
                .stream()
                .map(Descriptors.FieldDescriptor::getName)
                .collect(Collectors.toSet());

            messageElement.getFields().stream()
                .filter(field -> !fieldSet.contains(field.getName()) && field.hasDefault())
                .forEach(field -> {
                    json.set(field.getName(), field.getDefault().convertValue());
                });
        }
        return json;
    }

    private DynamicMessage tryBuildMessage(String testName, byte[] binary) {
        DynamicMessage properBuilder = null;
        try {
            DynamicMessage.Builder testBuilder = dynamicSchema.newMessageBuilder(testName);
            testBuilder.mergeFrom(binary);
            properBuilder = testBuilder.build();
        } catch (InvalidProtocolBufferException e) {
            log.warn(e.getMessage());
        }
        return properBuilder;
    }

    private DynamicSchema getDynamicSchema(Proto proto) throws DescriptorValidationException {
        DynamicSchema.Builder schemaBuilder = DynamicSchema.newBuilder();
        schemaBuilder.setName(proto.getFilename());
        schemaBuilder.setPackage(proto.getPackageName());
        if (!proto.getTypes().isEmpty()) {
            for (TypeElement element : proto.getTypes()) {
                if (element instanceof MessageElement) {
                    schemaBuilder.addMessageDefinition(getMessageDefinition((MessageElement) element));
                } else if (element instanceof EnumElement) {
                    schemaBuilder.addEnumDefinition(getEnumDefinition((EnumElement) element));
                }
            }
        }
        return schemaBuilder.build();
    }

    private MessageDefinition getMessageDefinition(MessageElement message) {
        MessageDefinition.Builder builder = MessageDefinition.newBuilder(message.getName());
        for (FieldElement field : message.getFields()) {
            if (!field.getOptions().isEmpty()) {
                Object defaultValue = OptionElement.findByName(field.getOptions(), "default").getValue();
                String defaultString = getDefault(defaultValue);
                builder.addField(
                    field.getLabel().toString().toLowerCase(Locale.US),
                    field.getType().toString(),
                    field.getName(),
                    field.getTag(),
                    defaultString
                );
            } else {
                builder.addField(
                    field.getLabel().toString().toLowerCase(Locale.US),
                    field.getType().toString(),
                    field.getName(),
                    field.getTag()
                );
            }
        }

        for (TypeElement typeElement : message.getNestedElements()) {
            if (typeElement instanceof MessageElement) {
                builder.addMessageDefinition(getMessageDefinition((MessageElement) typeElement));
            } else if (typeElement instanceof EnumElement) {
                builder.addEnumDefinition(getEnumDefinition((EnumElement) typeElement));
            }
        }
        return builder.build();
    }

    private String getDefault(Object defaultValue) {
        String defaultString;

        if (defaultValue instanceof EnumConstantElement) {
            defaultString = ((EnumConstantElement) defaultValue).getName();
        } else {
            defaultString = (String) defaultValue;
        }
        return defaultString;
    }

    private EnumDefinition getEnumDefinition(EnumElement enumType) {
        EnumDefinition.Builder enumBuilder = EnumDefinition.newBuilder(enumType.getName());
        for (EnumConstantElement element : enumType.getConstants()) {
            enumBuilder.addValue(element.getName(), element.getTag());
        }
        return enumBuilder.build();
    }

}
