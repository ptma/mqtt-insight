package com.mqttinsight.codec.proto;

import com.google.protobuf.DescriptorProtos.DescriptorProto;
import com.google.protobuf.DescriptorProtos.FieldDescriptorProto;
import com.google.protobuf.DescriptorProtos.OneofDescriptorProto;

import java.util.HashMap;
import java.util.Map;

public class MessageDefinition {
    private static Map<String, FieldDescriptorProto.Type> fieldTypeMap;
    private static Map<String, FieldDescriptorProto.Label> fieldLabelMap;

    static {
        fieldTypeMap = new HashMap<>();
        fieldTypeMap.put("double", FieldDescriptorProto.Type.TYPE_DOUBLE);
        fieldTypeMap.put("float", FieldDescriptorProto.Type.TYPE_FLOAT);
        fieldTypeMap.put("int32", FieldDescriptorProto.Type.TYPE_INT32);
        fieldTypeMap.put("int64", FieldDescriptorProto.Type.TYPE_INT64);
        fieldTypeMap.put("uint32", FieldDescriptorProto.Type.TYPE_UINT32);
        fieldTypeMap.put("uint64", FieldDescriptorProto.Type.TYPE_UINT64);
        fieldTypeMap.put("sint32", FieldDescriptorProto.Type.TYPE_SINT32);
        fieldTypeMap.put("sint64", FieldDescriptorProto.Type.TYPE_SINT64);
        fieldTypeMap.put("fixed32", FieldDescriptorProto.Type.TYPE_FIXED32);
        fieldTypeMap.put("fixed64", FieldDescriptorProto.Type.TYPE_FIXED64);
        fieldTypeMap.put("sfixed32", FieldDescriptorProto.Type.TYPE_SFIXED32);
        fieldTypeMap.put("sfixed64", FieldDescriptorProto.Type.TYPE_SFIXED64);
        fieldTypeMap.put("bool", FieldDescriptorProto.Type.TYPE_BOOL);
        fieldTypeMap.put("string", FieldDescriptorProto.Type.TYPE_STRING);
        fieldTypeMap.put("bytes", FieldDescriptorProto.Type.TYPE_BYTES);
        fieldTypeMap.put("enum", FieldDescriptorProto.Type.TYPE_ENUM);
        fieldTypeMap.put("message", FieldDescriptorProto.Type.TYPE_MESSAGE);
        fieldTypeMap.put("group", FieldDescriptorProto.Type.TYPE_GROUP);

        fieldLabelMap = new HashMap<>();
        fieldLabelMap.put("optional", FieldDescriptorProto.Label.LABEL_OPTIONAL);
        fieldLabelMap.put("required", FieldDescriptorProto.Label.LABEL_REQUIRED);
        fieldLabelMap.put("repeated", FieldDescriptorProto.Label.LABEL_REPEATED);
    }

    private final DescriptorProto messageType;

    private MessageDefinition(DescriptorProto messageType) {
        this.messageType = messageType;
    }

    public DescriptorProto getMessageType() {
        return messageType;
    }

    @Override
    public String toString() {
        return messageType.toString();
    }

    public static Builder newBuilder(String msgTypeName) {
        return new Builder(msgTypeName);
    }

    public static class Builder {

        private final DescriptorProto.Builder messageTypeBuilder;
        private int mOneofIndex = 0;

        private Builder(String messageName) {
            messageTypeBuilder = DescriptorProto.newBuilder();
            messageTypeBuilder.setName(messageName);
        }

        public Builder addField(String label, String type, String name, int num) {
            return addField(label, type, name, num, null);
        }

        public Builder addField(String label, String type, String name, int num, String defaultVal) {
            FieldDescriptorProto.Label protoLabel = fieldLabelMap.get(label);
            if (protoLabel == null) {
                throw new IllegalArgumentException("Illegal label: " + label);
            }
            addField(protoLabel, type, name, num, defaultVal, null);
            return this;
        }

        public OneofBuilder addOneof(String oneofName) {
            messageTypeBuilder.addOneofDecl(OneofDescriptorProto.newBuilder().setName(oneofName).build());
            return new OneofBuilder(this, mOneofIndex++);
        }

        public Builder addMessageDefinition(MessageDefinition msgDef) {
            messageTypeBuilder.addNestedType(msgDef.getMessageType());
            return this;
        }

        public Builder addEnumDefinition(EnumDefinition enumDef) {
            messageTypeBuilder.addEnumType(enumDef.getEnumType());
            return this;
        }

        private void addField(FieldDescriptorProto.Label label, String type, String name, int num, String defaultVal, OneofBuilder oneofBuilder) {
            FieldDescriptorProto.Builder fieldBuilder = FieldDescriptorProto.newBuilder();
            fieldBuilder.setLabel(label);
            FieldDescriptorProto.Type fieldType = fieldTypeMap.get(type);
            if (fieldType != null) {
                fieldBuilder.setType(fieldType);
            } else {
                fieldBuilder.setTypeName(type);
            }
            fieldBuilder.setName(name).setNumber(num);
            if (defaultVal != null) {
                fieldBuilder.setDefaultValue(defaultVal);
            }
            if (oneofBuilder != null) {
                fieldBuilder.setOneofIndex(oneofBuilder.getIdx());
            }
            messageTypeBuilder.addField(fieldBuilder.build());
        }

        public MessageDefinition build() {
            return new MessageDefinition(messageTypeBuilder.build());
        }

    }

    public static class OneofBuilder {

        private final Builder mMsgBuilder;
        private final int mIdx;

        private OneofBuilder(Builder msgBuilder, int oneofIdx) {
            mMsgBuilder = msgBuilder;
            mIdx = oneofIdx;
        }

        public OneofBuilder addField(String type, String name, int num) {
            return addField(type, name, num, null);
        }

        public OneofBuilder addField(String type, String name, int num, String defaultVal) {
            mMsgBuilder.addField(FieldDescriptorProto.Label.LABEL_OPTIONAL, type, name, num, defaultVal, this);
            return this;
        }

        public Builder msgDefBuilder() {
            return mMsgBuilder;
        }

        public int getIdx() {
            return mIdx;
        }

    }
}
