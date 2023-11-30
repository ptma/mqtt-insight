package com.mqttinsight.ui.chart.series;

import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.renderer.CellContext;
import org.jdesktop.swingx.renderer.ComponentProvider;
import org.jdesktop.swingx.renderer.JRendererLabel;

import javax.swing.*;

/**
 * @author ptma
 */
public class SeriesTableRendererProvider extends ComponentProvider<JLabel> {

    @Override
    protected JLabel createRendererComponent() {
        JRendererLabel label = new JRendererLabel();
        label.setOpaque(true);
        label.setHorizontalAlignment(JLabel.LEFT);
        return label;
    }

    @Override
    protected void configureState(CellContext context) {
        rendererComponent.setHorizontalAlignment(getHorizontalAlignment());
    }

    @Override
    protected void format(CellContext context) {
        JXTable table = (JXTable) context.getComponent();
        Object alignment = table.getColumnExt(context.getColumn()).getClientProperty("Alignment");
        rendererComponent.setText(context.getValue().toString());
        if (alignment != null) {
            rendererComponent.setHorizontalAlignment((Integer) alignment);
        }
    }

}
