package com.mqttinsight.mqtt;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.eclipse.paho.mqttv5.common.packet.UserProperty;

import java.io.Serializable;

/**
 * @author ptma
 */
@Getter
@Setter
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
public class Property implements Serializable {

    protected String key;

    protected String value;

    public UserProperty toUserProperty() {
        return new UserProperty(key, value);
    }
}
