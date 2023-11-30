package com.mqttinsight.ui.component;

import com.formdev.flatlaf.ui.FlatRoundBorder;

import java.awt.*;

public class RoundBorder extends FlatRoundBorder {

    private int arc;
    private Color borderColor;
    private Insets margin;

    public RoundBorder(Color borderColor, int arc) {
        this.borderColor = borderColor;
        this.arc = arc;
    }

    @Override
    protected int getArc(Component c) {
        return arc;
    }

    @Override
    protected Paint getBorderColor(Component c) {
        return borderColor;
    }

    public void setBorderColor(Color borderColor) {
        this.borderColor = borderColor;
    }

    public void setMargin(Insets margin) {
        this.margin = margin;
    }

    @Override
    public Insets getBorderInsets(Component c, Insets insets) {
        insets.top = margin != null ? margin.top : 0;
        insets.left = margin != null ? margin.left : 0;
        insets.bottom = margin != null ? margin.bottom : 0;
        insets.right = margin != null ? margin.right : 0;
        return insets;
    }
}
