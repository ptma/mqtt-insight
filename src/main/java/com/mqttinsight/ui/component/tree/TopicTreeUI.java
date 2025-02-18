package com.mqttinsight.ui.component.tree;

import com.formdev.flatlaf.ui.FlatTreeUI;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.*;

public class TopicTreeUI extends FlatTreeUI {

    @Override
    protected boolean shouldPaintExpandControl(TreePath path, int row,
                                               boolean isExpanded,
                                               boolean hasBeenExpanded,
                                               boolean isLeaf) {
        return true;
    }

    @Override
    protected void paintExpandControl(Graphics g,
                                      Rectangle clipBounds, Insets insets,
                                      Rectangle bounds, TreePath path,
                                      int row, boolean isExpanded,
                                      boolean hasBeenExpanded,
                                      boolean isLeaf) {
        Object value = path.getLastPathComponent();

        // Draw icons if not a leaf and either hasn't been loaded,
        // or the model child count is > 0.
        int middleXOfKnob;
        if (tree.getComponentOrientation().isLeftToRight()) {
            middleXOfKnob = bounds.x - getRightChildIndent() + 1;
        } else {
            middleXOfKnob = bounds.x + bounds.width + getRightChildIndent() - 1;
        }
        int middleYOfKnob = bounds.y + (bounds.height / 2);

        if (isLeaf) {
            Icon leafIcon = TopicTreeCellRenderer.ICON_LEAF;
            if (leafIcon != null)
                drawCentered(tree, g, leafIcon, middleXOfKnob,
                    middleYOfKnob);
        } else if (isExpanded) {
            Icon expandedIcon = getExpandedIcon();
            if (expandedIcon != null)
                drawCentered(tree, g, expandedIcon, middleXOfKnob,
                    middleYOfKnob);
        } else {
            Icon collapsedIcon = getCollapsedIcon();
            if (collapsedIcon != null)
                drawCentered(tree, g, collapsedIcon, middleXOfKnob,
                    middleYOfKnob);
        }
    }
}
