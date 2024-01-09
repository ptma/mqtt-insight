package com.mqttinsight.codec.proto;

import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.DescriptorProtos.FileDescriptorSet;
import com.google.protobuf.Descriptors.*;
import com.google.protobuf.DynamicMessage;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class DynamicSchema {

    private final FileDescriptorSet mFileDescSet;
    private final Map<String, Descriptor> mMsgDescriptorMapFull = new HashMap<>();
    private final Map<String, Descriptor> mMsgDescriptorMapShort = new HashMap<>();
    private final Map<String, EnumDescriptor> mEnumDescriptorMapFull = new HashMap<>();
    private final Map<String, EnumDescriptor> mEnumDescriptorMapShort = new HashMap<>();

    public static Builder newBuilder() {
        return new Builder();
    }

    public static DynamicSchema parseFrom(InputStream schemaDescIn) throws DescriptorValidationException, IOException {
        try (schemaDescIn) {
            return new DynamicSchema(FileDescriptorSet.parseFrom(schemaDescIn));
        }
    }

    public DynamicMessage.Builder newMessageBuilder(String msgTypeName) {
        Descriptor msgType = getMessageDescriptor(msgTypeName);
        if (msgType == null) {
            return null;
        }
        return DynamicMessage.newBuilder(msgType);
    }

    public Descriptor getMessageDescriptor(String msgTypeName) {
        Descriptor msgType = mMsgDescriptorMapShort.get(msgTypeName);
        if (msgType == null) {
            msgType = mMsgDescriptorMapFull.get(msgTypeName);
        }
        return msgType;
    }

    public EnumValueDescriptor getEnumValue(String enumTypeName, String enumName) {
        EnumDescriptor enumType = getEnumDescriptor(enumTypeName);
        if (enumType == null) {
            return null;
        }
        return enumType.findValueByName(enumName);
    }

    public EnumValueDescriptor getEnumValue(String enumTypeName, int enumNumber) {
        EnumDescriptor enumType = getEnumDescriptor(enumTypeName);
        if (enumType == null) {
            return null;
        }
        return enumType.findValueByNumber(enumNumber);
    }

    public EnumDescriptor getEnumDescriptor(String enumTypeName) {
        EnumDescriptor enumType = mEnumDescriptorMapShort.get(enumTypeName);
        if (enumType == null) {
            enumType = mEnumDescriptorMapFull.get(enumTypeName);
        }
        return enumType;
    }

    public Set<String> getMessageTypes() {
        return new TreeSet<>(mMsgDescriptorMapFull.keySet());
    }

    public Set<String> getEnumTypes() {
        return new TreeSet<>(mEnumDescriptorMapFull.keySet());
    }

    private DynamicSchema(FileDescriptorSet fileDescSet) throws DescriptorValidationException {
        mFileDescSet = fileDescSet;
        Map<String, FileDescriptor> fileDescMap = init(fileDescSet);

        Set<String> msgDupes = new HashSet<>();
        Set<String> enumDupes = new HashSet<>();
        for (FileDescriptor fileDesc : fileDescMap.values()) {
            for (Descriptor msgType : fileDesc.getMessageTypes()) {
                addMessageType(msgType, null, msgDupes, enumDupes);
            }
            for (EnumDescriptor enumType : fileDesc.getEnumTypes()) {
                addEnumType(enumType, null, enumDupes);
            }
        }

        for (String msgName : msgDupes) {
            mMsgDescriptorMapShort.remove(msgName);
        }
        for (String enumName : enumDupes) {
            mEnumDescriptorMapShort.remove(enumName);
        }
    }

    private Map<String, FileDescriptor> init(FileDescriptorSet fileDescSet) throws DescriptorValidationException {
        Set<String> allFdProtoNames = new HashSet<String>();
        for (FileDescriptorProto fdProto : fileDescSet.getFileList()) {
            if (allFdProtoNames.contains(fdProto.getName())) {
                throw new IllegalArgumentException("duplicate name: " + fdProto.getName());
            }
            allFdProtoNames.add(fdProto.getName());
        }

        Map<String, FileDescriptor> resolvedFileDescMap = new HashMap<String, FileDescriptor>();
        while (resolvedFileDescMap.size() < fileDescSet.getFileCount()) {
            for (FileDescriptorProto fdProto : fileDescSet.getFileList()) {
                if (resolvedFileDescMap.containsKey(fdProto.getName())) {
                    continue;
                }

                List<String> dependencyList = fdProto.getDependencyList();
                List<FileDescriptor> resolvedFdList = new ArrayList<FileDescriptor>();
                for (String depName : dependencyList) {
                    if (!allFdProtoNames.contains(depName)) {
                        throw new IllegalArgumentException("cannot resolve import " + depName + " in " + fdProto.getName());
                    }
                    FileDescriptor fd = resolvedFileDescMap.get(depName);
                    if (fd != null) {
                        resolvedFdList.add(fd);
                    }
                }

                if (resolvedFdList.size() == dependencyList.size()) { // dependencies resolved
                    FileDescriptor[] fds = new FileDescriptor[resolvedFdList.size()];
                    FileDescriptor fd = FileDescriptor.buildFrom(fdProto, resolvedFdList.toArray(fds));
                    resolvedFileDescMap.put(fdProto.getName(), fd);
                }
            }
        }

        return resolvedFileDescMap;
    }

    private void addMessageType(Descriptor msgType, String scope, Set<String> msgDupes, Set<String> enumDupes) {
        String msgTypeNameFull = msgType.getFullName();
        String msgTypeNameShort = (scope == null ? msgType.getName() : scope + "." + msgType.getName());

        if (mMsgDescriptorMapFull.containsKey(msgTypeNameFull)) {
            throw new IllegalArgumentException("duplicate name: " + msgTypeNameFull);
        }
        if (mMsgDescriptorMapShort.containsKey(msgTypeNameShort)) {
            msgDupes.add(msgTypeNameShort);
        }

        mMsgDescriptorMapFull.put(msgTypeNameFull, msgType);
        mMsgDescriptorMapShort.put(msgTypeNameShort, msgType);

        for (Descriptor nestedType : msgType.getNestedTypes()) {
            addMessageType(nestedType, msgTypeNameShort, msgDupes, enumDupes);
        }
        for (EnumDescriptor enumType : msgType.getEnumTypes()) {
            addEnumType(enumType, msgTypeNameShort, enumDupes);
        }
    }

    private void addEnumType(EnumDescriptor enumType, String scope, Set<String> enumDupes) {
        String enumTypeNameFull = enumType.getFullName();
        String enumTypeNameShort = (scope == null ? enumType.getName() : scope + "." + enumType.getName());

        if (mEnumDescriptorMapFull.containsKey(enumTypeNameFull)) {
            throw new IllegalArgumentException("duplicate name: " + enumTypeNameFull);
        }
        if (mEnumDescriptorMapShort.containsKey(enumTypeNameShort)) {
            enumDupes.add(enumTypeNameShort);
        }

        mEnumDescriptorMapFull.put(enumTypeNameFull, enumType);
        mEnumDescriptorMapShort.put(enumTypeNameShort, enumType);
    }

    public static class Builder {
        private FileDescriptorProto.Builder mFileDescProtoBuilder;
        private FileDescriptorSet.Builder mFileDescSetBuilder;

        public DynamicSchema build() throws DescriptorValidationException {
            FileDescriptorSet.Builder fileDescSetBuilder = FileDescriptorSet.newBuilder();
            fileDescSetBuilder.addFile(mFileDescProtoBuilder.build());
            fileDescSetBuilder.mergeFrom(mFileDescSetBuilder.build());
            return new DynamicSchema(fileDescSetBuilder.build());
        }

        public Builder setName(String name) {
            mFileDescProtoBuilder.setName(name);
            return this;
        }

        public Builder setPackage(String name) {
            mFileDescProtoBuilder.setPackage(name);
            return this;
        }

        public Builder addMessageDefinition(MessageDefinition msgDef) {
            mFileDescProtoBuilder.addMessageType(msgDef.getMessageType());
            return this;
        }

        public Builder addEnumDefinition(EnumDefinition enumDef) {
            mFileDescProtoBuilder.addEnumType(enumDef.getEnumType());
            return this;
        }

        public Builder addDependency(String dependency) {
            mFileDescProtoBuilder.addDependency(dependency);
            return this;
        }

        public Builder addPublicDependency(String dependency) {
            for (int i = 0; i < mFileDescProtoBuilder.getDependencyCount(); i++) {
                if (mFileDescProtoBuilder.getDependency(i).equals(dependency)) {
                    mFileDescProtoBuilder.addPublicDependency(i);
                    return this;
                }
            }
            mFileDescProtoBuilder.addDependency(dependency);
            mFileDescProtoBuilder.addPublicDependency(mFileDescProtoBuilder.getDependencyCount() - 1);
            return this;
        }

        public Builder addSchema(DynamicSchema schema) {
            mFileDescSetBuilder.mergeFrom(schema.mFileDescSet);
            return this;
        }

        private Builder() {
            mFileDescProtoBuilder = FileDescriptorProto.newBuilder();
            mFileDescSetBuilder = FileDescriptorSet.newBuilder();
        }

    }
}
