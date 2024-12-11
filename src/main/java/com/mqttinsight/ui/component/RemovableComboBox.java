package com.mqttinsight.ui.component;

import com.mqttinsight.util.Icons;

import javax.accessibility.Accessible;
import javax.swing.*;
import javax.swing.plaf.basic.ComboPopup;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

public class RemovableComboBox<E> extends JComboBox<E> {

    private transient CellButtonMouseListener listener;

    private final Consumer<E> onRemoveButtonClicked;

    public RemovableComboBox(Consumer<E> onRemoveButtonClicked) {
        super();
        this.onRemoveButtonClicked = onRemoveButtonClicked;
    }

    @Override
    public void updateUI() {
        if (Objects.nonNull(listener)) {
            getList().ifPresent(list -> {
                list.removeMouseListener(listener);
                list.removeMouseMotionListener(listener);
            });
        }
        super.updateUI();
        setRenderer(new ButtonRenderer<>(this));
        getList().ifPresent(list -> {
            listener = new CellButtonMouseListener();
            list.addMouseListener(listener);
            list.addMouseMotionListener(listener);
        });
    }

    protected Optional<JComponent> getList() {
        JComponent c = null;
        Accessible a = getAccessibleContext().getAccessibleChild(0);
        if (a instanceof ComboPopup) {
            c = ((ComboPopup) a).getList();
        }
        return Optional.ofNullable(c);
    }

    static class CellButtonMouseListener extends MouseAdapter {
        @Override
        public void mouseMoved(MouseEvent e) {
            JList<?> list = (JList<?>) e.getComponent();
            Point pt = e.getPoint();
            int index = list.locationToIndex(pt);
            ListCellRenderer<?> renderer = list.getCellRenderer();
            if (renderer instanceof RemovableComboBox.ButtonRenderer) {
                ((ButtonRenderer<?>) renderer).rolloverIndex = Objects.nonNull(getButton(list, pt, index)) ? index : -1;
            }
            list.repaint();
        }

        @Override
        public void mousePressed(MouseEvent e) {
            e.getComponent().repaint();
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            JList<?> list = (JList<?>) e.getComponent();
            Point pt = e.getPoint();
            int index = list.locationToIndex(pt);
            if (index >= 0) {
                JButton button = getButton(list, pt, index);
                if (Objects.nonNull(button)) {
                    button.doClick();
                }
            }
            ListCellRenderer<?> renderer = list.getCellRenderer();
            if (renderer instanceof RemovableComboBox.ButtonRenderer) {
                ((ButtonRenderer<?>) renderer).rolloverIndex = -1;
            }
            list.repaint();
        }

        @Override
        public void mouseExited(MouseEvent e) {
            JList<?> list = (JList<?>) e.getComponent();
            ListCellRenderer<?> renderer = list.getCellRenderer();
            if (renderer instanceof RemovableComboBox.ButtonRenderer) {
                ((ButtonRenderer<?>) renderer).rolloverIndex = -1;
            }
        }

        private static <E> JButton getButton(JList<E> list, Point pt, int index) {
            E proto = list.getPrototypeCellValue();
            ListCellRenderer<? super E> renderer = list.getCellRenderer();
            Component c = renderer.getListCellRendererComponent(list, proto, index, false, false);
            Rectangle r = list.getCellBounds(index, index);
            c.setBounds(r);

            pt.translate(-r.x, -r.y);
            return Optional.ofNullable(SwingUtilities.getDeepestComponentAt(c, pt.x, pt.y))
                .filter(JButton.class::isInstance)
                .map(JButton.class::cast)
                .orElse(null);
        }
    }

    static class ButtonRenderer<E> implements ListCellRenderer<E> {
        protected int targetIndex;
        protected int rolloverIndex = -1;
        private final JPanel panel = new JPanel(new BorderLayout()) {
            @Override
            public Dimension getPreferredSize() {
                Dimension d = super.getPreferredSize();
                d.width = 0;
                return d;
            }
        };
        private final ListCellRenderer<? super E> renderer = new DefaultListCellRenderer();
        private final JButton deleteButton = new JButton(Icons.REMOVE) {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(16, 16);
            }

            @Override
            public void updateUI() {
                super.updateUI();
                setBorder(BorderFactory.createEmptyBorder());
                setFocusable(false);
                setRolloverEnabled(true);
                setContentAreaFilled(false);
            }
        };

        protected ButtonRenderer(RemovableComboBox<E> comboBox) {
            deleteButton.addActionListener(e -> {
                ComboBoxModel<E> m = comboBox.getModel();
                if (m.getSize() > 0 && m instanceof MutableComboBoxModel) {
                    comboBox.onRemoveButtonClicked.accept(m.getElementAt(targetIndex));
                    comboBox.showPopup();
                }
            });
            panel.setOpaque(true);
            panel.add(deleteButton, BorderLayout.EAST);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends E> list, E value, int index, boolean isSelected, boolean cellHasFocus) {
            Component c = renderer.getListCellRendererComponent(
                list, value, index, isSelected, cellHasFocus);
            if (index >= 0 && c instanceof JComponent) {
                JComponent label = (JComponent) c;
                label.setOpaque(false);
                this.targetIndex = index;
                updateRemoveButton(list, deleteButton, index == rolloverIndex);
                panel.setBackground(getPanelBackground(list, index, isSelected));
                panel.add(label);
                c = panel;
            }
            return c;
        }

        private static Color getPanelBackground(JList<?> list, int index, boolean isSelected) {
            Color bgc;
            if (isSelected) {
                bgc = list.getSelectionBackground();
            } else {
                bgc = list.getBackground();
            }
            return bgc;
        }

        private void updateRemoveButton(JList<?> list, JButton button, boolean isRollover) {
            boolean showButton = list.getModel().getSize() > 0;
            button.setVisible(showButton);
            if (showButton) {
                button.getModel().setRollover(isRollover);
                button.setForeground(isRollover ? Color.WHITE : list.getForeground());
            }
        }
    }
}
