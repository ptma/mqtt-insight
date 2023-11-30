package com.mqttinsight.mqtt;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author ptma
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReconnectionSettings implements Serializable, Cloneable {

    protected boolean enable;

    protected Integer reconnectInterval;

    protected Boolean autoResubscribe;


    @Override
    public ReconnectionSettings clone() throws CloneNotSupportedException {
        return (ReconnectionSettings) super.clone();
    }
}
