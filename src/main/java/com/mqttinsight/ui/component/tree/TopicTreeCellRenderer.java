package com.mqttinsight.ui.component.tree;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.mqttinsight.util.LangUtil;
import net.miginfocom.swing.MigLayout;
import org.jdesktop.swingx.JXLabel;

import javax.swing.*;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;

public class TopicTreeCellRenderer extends DefaultTreeCellRenderer {

    public static final Color TEXT_VISIBLE_COLOR = UIManager.getColor("Label.foreground");
    public static final Color TEXT_INVISIBLE_COLOR = UIManager.getColor("Label.disabledForeground");
    public static final Icon ICON_COLLAPSED = UIManager.getIcon("Tree.collapsedIcon");
    public static final Icon ICON_EXPANDED = UIManager.getIcon("Tree.expandedIcon");
    public static final Icon ICON_LEAF = new FlatSVGIcon("svg/icons/dot.svg", ICON_EXPANDED.getIconWidth(), ICON_EXPANDED.getIconHeight());

    private final JPanel nodePanel;

    private final JXLabel nameLabel;
    private final JXLabel descriptionLabel;

    public TopicTreeCellRenderer() {
        super();
        nodePanel = new JPanel();
        nodePanel.setLayout(new MigLayout(
            "insets 4 4 4 4",
            "[]5[grow]",
            "[]"
        ));
        nodePanel.setOpaque(false);

        nameLabel = new JXLabel("");
        nameLabel.setOpaque(false);
        nameLabel.putClientProperty("FlatLaf.styleClass", "h4");
        nameLabel.setToolTipText("");
        nodePanel.add(nameLabel, "cell 0 0");

        descriptionLabel = new JXLabel();
        descriptionLabel.setOpaque(false);
        descriptionLabel.setForeground(TEXT_INVISIBLE_COLOR);
        nodePanel.add(descriptionLabel, "cell 1 0,wmin 10px,growx");
    }

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        if (value instanceof SegmentTreeNode node) {
            nameLabel.setText(node.getName());
            Color fg;
            if (node.isSegmentVisible()) {
                if (selected && hasFocus) {
                    fg = getTextSelectionColor();

                } else {
                    fg = getTextNonSelectionColor();
                }
            } else {
                fg = TEXT_INVISIBLE_COLOR;
            }
            nameLabel.setForeground(fg);
            String topicsDescription, messagesDescription;
            int topicsCount = node.getTopicCount();
            if (topicsCount > 1) {
                topicsDescription = LangUtil.getString("SegmentTopicsDescription");
            } else {
                topicsDescription = LangUtil.getString("SegmentTopicDescription");
            }
            int messagesCount = node.getTotalMessageCount();
            if (messagesCount > 1) {
                messagesDescription = LangUtil.getString("SegmentMessagesDescription");
            } else {
                messagesDescription = LangUtil.getString("SegmentMessageDescription");
            }
            String descriptionTemplate = String.format("(%s, %s)", topicsDescription, messagesDescription);
            descriptionLabel.setText(String.format(descriptionTemplate, topicsCount, messagesCount));
        }
        return nodePanel;
    }

}
