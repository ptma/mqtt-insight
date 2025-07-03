package com.mqttinsight.scripting;

import com.caoccao.javet.enums.V8ValueReferenceType;
import com.caoccao.javet.exceptions.JavetException;
import com.caoccao.javet.interop.V8Runtime;
import com.caoccao.javet.interop.V8Scope;
import com.caoccao.javet.interop.converters.JavetProxyConverter;
import com.caoccao.javet.values.V8Value;
import com.caoccao.javet.values.reference.V8ValueTypedArray;

public class CustomizeJavetProxyConverter extends JavetProxyConverter {

    public CustomizeJavetProxyConverter() {
        super();
    }

    @Override
    protected <T extends V8Value> T toV8Value(V8Runtime v8Runtime, Object object, final int depth) throws JavetException {
        V8Value v8Value = super.toV8Value(v8Runtime, object, depth);
        if (object instanceof byte[]) {
            try (V8Scope v8Scope = v8Runtime.getV8Scope()) {
                byte[] bytes = (byte[]) object;
                V8ValueTypedArray v8ValueTypedArray = v8Scope.createV8ValueTypedArray(
                    V8ValueReferenceType.Uint8Array, bytes.length);
                v8ValueTypedArray.fromBytes(bytes);
                v8Value = v8ValueTypedArray;
                v8Scope.setEscapable();
            }
        }
        return (T) v8Value;
    }
}
