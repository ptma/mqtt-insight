package com.mqttinsight.codec.proto;

import com.google.protobuf.DescriptorProtos.EnumDescriptorProto;
import com.google.protobuf.DescriptorProtos.EnumValueDescriptorProto;

public class EnumDefinition {

    private EnumDescriptorProto mEnumType;

    private EnumDefinition(EnumDescriptorProto enumType) {
        mEnumType = enumType;
    }

    public EnumDescriptorProto getEnumType() {
        return mEnumType;
    }

    @Override
    public String toString() {
        return mEnumType.toString();
    }

    public static Builder newBuilder(String enumName) {
        return new Builder(enumName);
    }

    public static class Builder {

        private EnumDescriptorProto.Builder mEnumTypeBuilder;

        private Builder(String enumName) {
            mEnumTypeBuilder = EnumDescriptorProto.newBuilder();
            mEnumTypeBuilder.setName(enumName);
        }

        public Builder addValue(String name, int num) {
            EnumValueDescriptorProto.Builder enumValBuilder = EnumValueDescriptorProto.newBuilder();
            enumValBuilder.setName(name).setNumber(num);
            mEnumTypeBuilder.addValue(enumValBuilder.build());
            return this;
        }

        public EnumDefinition build() {
            return new EnumDefinition(mEnumTypeBuilder.build());
        }

    }
}
