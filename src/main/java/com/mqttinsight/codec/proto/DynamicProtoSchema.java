package com.mqttinsight.codec.proto;

import com.google.protobuf.ByteString;
import com.google.protobuf.Descriptors;
import com.google.protobuf.Descriptors.DescriptorValidationException;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.InvalidProtocolBufferException;
import com.mqttinsight.exception.SchemaLoadException;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author ptma
 */
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

    public Map<String, Object> parse(byte[] binary) throws ProtoParseException {
        DynamicMessage dynamicMessage = null;
        MessageElement messageElement = null;
        for (MessageElement type : proto.getMessages()) {
            try {
                dynamicMessage = tryBuildMessage(type.getName(), binary);
                messageElement = type;
                if (dynamicMessage.getUnknownFields().asMap().isEmpty()) {
                    messageElement.incrementHitCount();
                    break;
                }
            } catch (InvalidProtocolBufferException ignore) {
            }
        }
        if (dynamicMessage == null) {
            throw new ProtoParseException("Cannot find proper schema for this protocol buffer message, maybe provide worng schema file");
        }

        Map<String, Object> objectMap = new HashMap<>();
        dynamicMessage.getAllFields().forEach((k, v) -> {
            if (v instanceof ByteString) {
                objectMap.put(k.getName(), ((ByteString) v).toByteArray());
            } else {
                objectMap.put(k.getName(), v);
            }
        });
        if (dynamicMessage.getAllFields().size() < messageElement.getFields().size()) {
            Set<String> fieldSet = dynamicMessage.getAllFields()
                .keySet()
                .stream()
                .map(Descriptors.FieldDescriptor::getName)
                .collect(Collectors.toSet());

            messageElement.getFields().stream()
                .filter(field -> !fieldSet.contains(field.getName()) && field.hasDefault())
                .forEach(field -> {
                    objectMap.put(field.getName(), field.getDefault().convertValue());
                });
        }
        return objectMap;
    }

    private DynamicMessage tryBuildMessage(String testName, byte[] binary) throws InvalidProtocolBufferException {
        DynamicMessage.Builder testBuilder = dynamicSchema.newMessageBuilder(testName);
        testBuilder.mergeFrom(binary);
        return testBuilder.build();
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
