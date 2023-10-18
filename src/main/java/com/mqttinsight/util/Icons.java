package com.mqttinsight.util;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.formdev.flatlaf.extras.FlatSVGUtils;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * @author ptma
 */
public final class Icons {

    public static final List<Image> WINDOW_ICON = FlatSVGUtils.createWindowIconImages("/svg/logo.svg");

    public static final Icon LOGO = new FlatSVGIcon("svg/logo.svg", 64, 64);

    // Main form
    public static final Icon LOG_VERBOSE = new FlatSVGIcon("svg/icons/logVerbose.svg");

    public static final Icon STATUS_CONNECTING = new FlatSVGIcon("svg/status/status_connecting.svg");
    public static final Icon STATUS_CONNECTING_SMALL = new FlatSVGIcon("svg/status/status_connecting.svg", 12, 12);
    public static final Icon STATUS_CONNECTED = new FlatSVGIcon("svg/status/status_connected.svg");
    public static final Icon STATUS_CONNECTED_SMALL = new FlatSVGIcon("svg/status/status_connected.svg", 12, 12);
    public static final Icon STATUS_DISCONNECTING = new FlatSVGIcon("svg/status/status_disconnecting.svg");
    public static final Icon STATUS_DISCONNECTING_SMALL = new FlatSVGIcon("svg/status/status_disconnecting.svg", 12, 12);
    public static final Icon STATUS_DISCONNECTED = new FlatSVGIcon("svg/status/status_disconnected.svg");
    public static final Icon STATUS_DISCONNECTED_SMALL = new FlatSVGIcon("svg/status/status_disconnected.svg", 12, 12);
    public static final Icon STATUS_FAILED = new FlatSVGIcon("svg/status/status_failed.svg");
    public static final Icon STATUS_FAILED_SMALL = new FlatSVGIcon("svg/status/status_failed.svg", 12, 12);

    public static final Icon TIPS = new FlatSVGIcon("svg/icons/infoOutline.svg", 12, 12);

    public static final Icon ERROR = new FlatSVGIcon("svg/icons/error.svg");
    public static final Icon REFRESH = new FlatSVGIcon("svg/icons/refresh.svg");

    // Connections manager form
    public static final Icon FOLDER = new FlatSVGIcon("svg/icons/folder.svg");
    public static final Icon FOLDER_OPEN = new FlatSVGIcon("svg/icons/folder_open.svg");
    public static final Icon CONNECTION = new FlatSVGIcon("svg/icons/connection.svg");
    public static final Icon ADD = new FlatSVGIcon("svg/icons/add.svg");
    public static final Icon EDIT = new FlatSVGIcon("svg/icons/edit.svg");
    public static final Icon REMOVE = new FlatSVGIcon("svg/icons/remove.svg");
    public static final Icon EXECUTE = new FlatSVGIcon("svg/icons/execute.svg");
    public static final Icon SUSPEND = new FlatSVGIcon("svg/icons/suspend.svg");


    public static final Icon CHECKBOX = new FlatSVGIcon("svg/icons/checkbox.svg");
    public static final Icon CHECKBOX_CHECKED = new FlatSVGIcon("svg/icons/checkbox_checked.svg");

    // Subscription
    public static final Icon SUBSCRIBE = new FlatSVGIcon("svg/icons/subscribe.svg");
    public static final Icon FAVORITE = new FlatSVGIcon("svg/icons/star.svg");
    public static final Icon FAVORITE_FILL = new FlatSVGIcon("svg/icons/star-fill.svg");
    public static final Icon EYE = new FlatSVGIcon("svg/icons/eye.svg");
    public static final Icon EYE_CLOSE = new FlatSVGIcon("svg/icons/eye-close.svg");
    public static final Icon PALETTE = new FlatSVGIcon("svg/icons/palette.svg");
    public static final Icon MORE = new FlatSVGIcon("svg/icons/list.svg");

    // MessageGrid
    public static final Icon ARROW_FIRST = new FlatSVGIcon("svg/icons/arrow_first.svg");
    public static final Icon ARROW_BACK = new FlatSVGIcon("svg/icons/arrow_back.svg");
    public static final Icon ARROW_FORWARD = new FlatSVGIcon("svg/icons/arrow_forward.svg");
    public static final Icon ARROW_LAST = new FlatSVGIcon("svg/icons/arrow_last.svg");
    public static final Icon ARROW_INCOMING = new FlatSVGIcon("svg/icons/arrow_incoming.svg");
    public static final Icon ARROW_OUTGOING = new FlatSVGIcon("svg/icons/arrow_outgoing.svg");
    public static final Icon JAVASCRIPT_GREEN = new FlatSVGIcon("svg/icons/javascript_green.svg");
    public static final Icon JAVASCRIPT_ORANGE = new FlatSVGIcon("svg/icons/javascript_orange.svg");
    public static final Icon SEARCH_MATCHCASE = new FlatSVGIcon("svg/icons/matchCase.svg");
    public static final Icon SEARCH_MATCHCASE_HOVER = new FlatSVGIcon("svg/icons/matchCaseHovered.svg");
    public static final Icon SEARCH_MATCHCASE_SELECTED = new FlatSVGIcon("svg/icons/matchCaseSelected.svg");
    public static final Icon SEARCH_WORDS = new FlatSVGIcon("svg/icons/words.svg");
    public static final Icon SEARCH_WORDS_HOVER = new FlatSVGIcon("svg/icons/wordsHovered.svg");
    public static final Icon SEARCH_WORDS_SELECTED = new FlatSVGIcon("svg/icons/wordsSelected.svg");
    public static final Icon SEARCH_REGEX = new FlatSVGIcon("svg/icons/regex.svg");
    public static final Icon SEARCH_REGEX_HOVER = new FlatSVGIcon("svg/icons/regexHovered.svg");
    public static final Icon SEARCH_REGEX_SELECTED = new FlatSVGIcon("svg/icons/regexSelected.svg");
    public static final Icon NEXT_OCCURENCE = new FlatSVGIcon("svg/icons/nextOccurence.svg");
    public static final Icon PREVIOUS_OCCURENCE = new FlatSVGIcon("svg/icons/previousOccurence.svg");
    public static final Icon FILTER = new FlatSVGIcon("svg/icons/filter.svg");
    public static final Icon DOWN_ARRAW = new FlatSVGIcon("svg/icons/down_arrow.svg");
    public static final Icon TABLE_VIEW = new FlatSVGIcon("svg/icons/table.svg");
    public static final Icon DIALOGUE_VIEW = new FlatSVGIcon("svg/icons/chat.svg");
    public static final Icon SCROLL_DOWN = new FlatSVGIcon("svg/icons/scroll_down.svg");

    public static final Icon PREVIEW = new FlatSVGIcon("svg/icons/preview.svg");
    public static final Icon SEND = new FlatSVGIcon("svg/icons/send.svg");
    public static final Icon SEND_GREEN = new FlatSVGIcon("svg/icons/send_green.svg");
    public static final Icon CANCEL = new FlatSVGIcon("svg/icons/cancel.svg");
    public static final Icon CLEAR = new FlatSVGIcon("svg/icons/clear.svg");
    public static final Icon EXPORT = new FlatSVGIcon("svg/icons/export.svg");

}
