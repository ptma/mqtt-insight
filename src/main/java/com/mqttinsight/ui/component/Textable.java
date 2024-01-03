package com.mqttinsight.ui.component;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * @author ptma
 */
public interface Textable {

    @JsonIgnore
    String getText();

}
