package com.mqttinsight.codec;

import com.mqttinsight.exception.SchemaLoadException;

/**
 * @author ptma
 */
public interface DynamicCodecSupport extends CodecSupport {

    DynamicCodecSupport newDynamicInstance(String name, String schemaFile) throws SchemaLoadException;

    String getSchemaFile();

    String[] getSchemaFileExtensions();

    boolean isInstantiated();

    @Override
    default boolean encodable() {
        return false;
    }
}
