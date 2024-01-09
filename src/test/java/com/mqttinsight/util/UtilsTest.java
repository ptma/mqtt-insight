package com.mqttinsight.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class UtilsTest {

    @Test
    void getSingleValueByJsonPath() {
        String value1 = Utils.getSingleValueByJsonPath("$.key1", "{\"key1\": 1}");
        Assertions.assertEquals(value1, "1");

        String value2 = Utils.getSingleValueByJsonPath("$.key1", "{\"key1\": [0,1,2]}");
        Assertions.assertEquals(value2, "0");
    }
}
