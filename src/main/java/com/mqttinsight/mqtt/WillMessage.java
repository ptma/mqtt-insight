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
public class WillMessage implements Serializable, Cloneable {

    protected boolean enable;

    protected String topic;

    protected String payload;

    protected int qos;

    protected boolean retained;

    protected String payloadFormat;


    @Override
    public WillMessage clone() throws CloneNotSupportedException {
        return (WillMessage) super.clone();
    }
}
