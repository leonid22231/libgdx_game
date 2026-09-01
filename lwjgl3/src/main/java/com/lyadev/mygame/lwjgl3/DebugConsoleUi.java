package com.lyadev.mygame.lwjgl3;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;

final class DebugConsoleUi {
    static final Color BG = color(0x11111b);
    static final Color PANEL = color(0x181825);
    static final Color BORDER = color(0x313244);
    static final Color TEXT = color(0xcdd6f4);
    static final Color MUTED = color(0xa6adc8);

    private DebugConsoleUi() {
        throw new UnsupportedOperationException();
    }

    static void stylePanel(JPanel panel) {
        panel.setOpaque(true);
        panel.setBackground(BG);
    }

    static void styleSplitPane(JSplitPane split) {
        split.setDividerSize(5);
        split.setBorder(BorderFactory.createEmptyBorder());
        split.setBackground(BG);
        split.setOpaque(true);
        split.setContinuousLayout(true);
        split.setUI(new BasicSplitPaneUI() {
            @Override
            public BasicSplitPaneDivider createDefaultDivider() {
                return new BasicSplitPaneDivider(this) {
                    @Override
                    public void paint(Graphics graphics) {
                        graphics.setColor(BORDER);
                        graphics.fillRect(0, 0, getWidth(), getHeight());
                    }
                };
            }
        });
    }

    static void styleScrollPane(JScrollPane scrollPane) {
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(4, 6, 4, 6)));
        scrollPane.setBackground(PANEL);
        scrollPane.getViewport().setBackground(PANEL);
        scrollPane.getViewport().setOpaque(true);
        scrollPane.setOpaque(true);
        styleScrollBar(scrollPane.getVerticalScrollBar());
        styleScrollBar(scrollPane.getHorizontalScrollBar());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
    }

    static void styleTextArea(JTextArea area, Color foreground) {
        area.setBackground(PANEL);
        area.setForeground(foreground);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        area.setCaretColor(foreground);
        area.setBorder(BorderFactory.createEmptyBorder(4, 2, 4, 2));
        area.setOpaque(true);
    }

    static void styleTextField(JTextField field) {
        field.setBackground(PANEL);
        field.setForeground(TEXT);
        field.setCaretColor(TEXT);
        field.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(4, 6, 4, 6)));
        field.setOpaque(true);
    }

    static void styleCheckBox(JCheckBox checkbox) {
        checkbox.setOpaque(true);
        checkbox.setBackground(PANEL);
        checkbox.setForeground(TEXT);
    }

    static void styleButton(JButton button) {
        button.setBackground(PANEL);
        button.setForeground(TEXT);
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)));
    }

    private static void styleScrollBar(JScrollBar scrollBar) {
        scrollBar.setBackground(PANEL);
        scrollBar.setForeground(BORDER);
        scrollBar.setOpaque(true);
        scrollBar.setUI(new BasicScrollBarUI() {
            @Override
            protected JButton createDecreaseButton(int orientation) {
                return zeroSizeButton();
            }

            @Override
            protected JButton createIncreaseButton(int orientation) {
                return zeroSizeButton();
            }

            private JButton zeroSizeButton() {
                JButton button = new JButton();
                Dimension zero = new Dimension(0, 0);
                button.setPreferredSize(zero);
                button.setMinimumSize(zero);
                button.setMaximumSize(zero);
                return button;
            }

            @Override
            protected void paintTrack(Graphics graphics, JComponent component, Rectangle trackBounds) {
                graphics.setColor(PANEL);
                graphics.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);
            }

            @Override
            protected void paintThumb(Graphics graphics, JComponent component, Rectangle thumbBounds) {
                if(thumbBounds.isEmpty() || !scrollbar.isEnabled()){
                    return;
                }
                graphics.setColor(BORDER);
                graphics.fillRoundRect(
                        thumbBounds.x + 2,
                        thumbBounds.y + 2,
                        Math.max(0, thumbBounds.width - 4),
                        Math.max(0, thumbBounds.height - 4),
                        6,
                        6);
            }
        });
    }

    private static Color color(int rgb) {
        return new Color((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF);
    }
}
