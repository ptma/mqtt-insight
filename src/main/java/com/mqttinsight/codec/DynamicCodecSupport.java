package com.mqttinsight.codec;

import com.mqttinsight.exception.CodecException;
import com.mqttinsight.exception.SchemaLoadException;

/**
 * Dynamic codec support.
 * <p>A codec that requires users to set corresponding properties before it can be instantiated.</p>
 *
 * @author ptma
 */
public interface DynamicCodecSupport extends CodecSupport {

    /**
     * Instantiate the codec support based on the specified name and schema file.
     */
    DynamicCodecSupport newDynamicInstance(String name, String schemaFile) throws SchemaLoadException, CodecException;

    /**
     * Schema or IDL file required for serialization framework.
     */
    String getSchemaFile();

    /**
     * Schema file extensions allowed in file open dialog. For example: "proto", "json"
     */
    String[] getSchemaFileExtensions();

    /**
     * Is the codec support been instantiated (schema file loaded, capable of encoding or decoding), and instance that have not been instantiated can only be used for codec configuration
     */
    boolean isInstantiated();

    @Override
    default boolean encodable() {
        return false;
    }
}
