package com.mqttinsight.ui.component.model;

import com.mqttinsight.ui.component.Textable;
import com.mqttinsight.util.LangUtil;
import lombok.Getter;

/**
 * @author ptma
 */

@Getter
public enum MessageViewMode implements Textable {

    TABLE(LangUtil.getString("TableView")), DIALOGUE(LangUtil.getString("DialogueView"));

    private final String text;

    MessageViewMode(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return name();
    }

    public static MessageViewMode of(String name) {
        for (MessageViewMode c : MessageViewMode.values()) {
            if (c.name().equalsIgnoreCase(name)) {
                return c;
            }
        }
        return MessageViewMode.TABLE;
    }

}
