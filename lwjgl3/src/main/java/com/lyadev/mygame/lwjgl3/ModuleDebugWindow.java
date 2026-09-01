package com.lyadev.mygame.lwjgl3;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.WindowConstants;

import com.badlogic.gdx.Gdx;
import com.lyadev.mygame.debug.DebugEntityService;
import com.lyadev.mygame.debug.DebugLogEntry;
import com.lyadev.mygame.debug.ModuleDebugAction;
import com.lyadev.mygame.debug.ModuleDebugField;
import com.lyadev.mygame.debug.ModuleDebugFieldType;
import com.lyadev.mygame.debug.ModuleDebugLine;
import com.lyadev.mygame.debug.ModuleDebugScreen;

final class ModuleDebugWindow {
    private static final Map<String, ModuleDebugWindow> openWindows = new HashMap<>();

    private final String entityTag;
    private final String moduleName;
    private final JFrame frame;
    private final JLabel titleLabel;
    private final JTextArea infoArea;
    private final JPanel fieldsPanel;
    private final JPanel actionsPanel;
    private final Timer refreshTimer;
    private String lastSignature = "";
    private boolean updatingFields = false;

    private ModuleDebugWindow(String entityTag, String entityDisplayTag, String moduleName) {
        this.entityTag = entityTag;
        this.moduleName = moduleName;

        frame = new JFrame(buildWindowTitle(entityDisplayTag, moduleName));
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.setMinimumSize(new java.awt.Dimension(420, 360));
        frame.getContentPane().setBackground(DebugConsoleUi.BG);

        titleLabel = new JLabel();
        titleLabel.setForeground(DebugConsoleUi.TEXT);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 13f));

        infoArea = new JTextArea(8, 30);
        infoArea.setEditable(false);
        infoArea.setFocusable(false);
        DebugConsoleUi.styleTextArea(infoArea, DebugConsoleUi.TEXT);

        JScrollPane infoScroll = new JScrollPane(infoArea);
        DebugConsoleUi.styleScrollPane(infoScroll);

        fieldsPanel = new JPanel(new GridBagLayout());
        DebugConsoleUi.stylePanel(fieldsPanel);
        fieldsPanel.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));

        JScrollPane fieldsScroll = new JScrollPane(fieldsPanel);
        DebugConsoleUi.styleScrollPane(fieldsScroll);

        actionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        DebugConsoleUi.stylePanel(actionsPanel);
        actionsPanel.setBorder(BorderFactory.createEmptyBorder(6, 0, 0, 0));

        JPanel root = new JPanel(new BorderLayout(0, 8));
        DebugConsoleUi.stylePanel(root);
        root.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        root.add(titleLabel, BorderLayout.NORTH);
        root.add(infoScroll, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout(0, 8));
        DebugConsoleUi.stylePanel(bottom);
        bottom.add(fieldsScroll, BorderLayout.CENTER);
        bottom.add(actionsPanel, BorderLayout.SOUTH);
        root.add(bottom, BorderLayout.SOUTH);

        frame.setContentPane(root);
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent event) {
                refreshTimer.stop();
                openWindows.remove(windowKey(entityTag, moduleName));
            }
        });

        refreshTimer = new Timer(400, event -> refresh());
        refreshTimer.start();

        frame.pack();
        frame.setLocationByPlatform(true);
        frame.setVisible(true);
        refresh();
    }

    static void open(String entityTag, String entityDisplayTag, String moduleName) {
        if(entityTag == null || moduleName == null){
            return;
        }
        String key = windowKey(entityTag, moduleName);
        ModuleDebugWindow existing = openWindows.get(key);
        if(existing != null){
            existing.focus();
            existing.refresh();
            return;
        }
        ModuleDebugWindow window = new ModuleDebugWindow(entityTag, entityDisplayTag, moduleName);
        openWindows.put(key, window);
    }

    static void closeAll() {
        for(ModuleDebugWindow window : List.copyOf(openWindows.values())){
            window.dispose();
        }
        openWindows.clear();
    }

    private void focus() {
        frame.setVisible(true);
        frame.toFront();
    }

    private void dispose() {
        refreshTimer.stop();
        frame.dispose();
        openWindows.remove(windowKey(entityTag, moduleName));
    }

    private void refresh() {
        if(Gdx.app == null){
            return;
        }
        Gdx.app.postRunnable(() -> {
            ModuleDebugScreen screen = DebugEntityService.collectModuleDebugScreen(entityTag, moduleName);
            SwingUtilities.invokeLater(() -> applyScreen(screen));
        });
    }

    private void applyScreen(ModuleDebugScreen screen) {
        if(screen == null){
            return;
        }
        String signature = screen.signature();
        if(signature.equals(lastSignature)){
            return;
        }
        lastSignature = signature;
        titleLabel.setText(screen.title + " · " + screen.moduleName);
        infoArea.setText(formatLines(screen.lines));
        rebuildFields(screen);
        rebuildActions(screen);
    }

    private static String formatLines(List<ModuleDebugLine> lines) {
        StringBuilder builder = new StringBuilder();
        for(int i = 0; i < lines.size(); i++){
            ModuleDebugLine line = lines.get(i);
            builder.append(line.key).append(": ").append(line.value);
            if(i < lines.size() - 1){
                builder.append(System.lineSeparator());
            }
        }
        return builder.toString();
    }

    private void rebuildFields(ModuleDebugScreen screen) {
        updatingFields = true;
        try {
            fieldsPanel.removeAll();

            GridBagConstraints labelConstraints = new GridBagConstraints();
            labelConstraints.gridx = 0;
            labelConstraints.anchor = GridBagConstraints.WEST;
            labelConstraints.insets = new Insets(3, 0, 3, 8);

            GridBagConstraints fieldConstraints = new GridBagConstraints();
            fieldConstraints.gridx = 1;
            fieldConstraints.weightx = 1.0;
            fieldConstraints.fill = GridBagConstraints.HORIZONTAL;
            fieldConstraints.insets = new Insets(3, 0, 3, 0);

            int row = 0;
            for(ModuleDebugField field : screen.fields){
                labelConstraints.gridy = row;
                fieldConstraints.gridy = row;

                JLabel label = new JLabel(field.label);
                label.setForeground(DebugConsoleUi.MUTED);
                fieldsPanel.add(label, labelConstraints);

                if(field.type == ModuleDebugFieldType.CHECKBOX){
                    JCheckBox checkbox = new JCheckBox();
                    checkbox.setSelected(Boolean.parseBoolean(field.value));
                    DebugConsoleUi.styleCheckBox(checkbox);
                    checkbox.addActionListener(event -> {
                        if(updatingFields){
                            return;
                        }
                        submitField(field.id, Boolean.toString(checkbox.isSelected()));
                    });
                    fieldsPanel.add(checkbox, fieldConstraints);
                } else {
                    JPanel inputRow = new JPanel(new BorderLayout(6, 0));
                    DebugConsoleUi.stylePanel(inputRow);

                    JTextField input = new JTextField(field.value);
                    DebugConsoleUi.styleTextField(input);
                    if(field.type == ModuleDebugFieldType.NUMBER && field.min != null && field.max != null){
                        input.setToolTipText("Range: " + field.min + " … " + field.max);
                    }

                    JButton applyButton = new JButton("Apply");
                    DebugConsoleUi.styleButton(applyButton);
                    Runnable applyValue = () -> submitField(field.id, input.getText().trim());
                    applyButton.addActionListener(event -> applyValue.run());
                    input.addActionListener(event -> applyValue.run());

                    inputRow.add(input, BorderLayout.CENTER);
                    inputRow.add(applyButton, BorderLayout.EAST);
                    fieldsPanel.add(inputRow, fieldConstraints);
                }
                row++;
            }

            if(screen.fields.isEmpty()){
                JLabel placeholder = new JLabel("No editable fields");
                placeholder.setForeground(DebugConsoleUi.MUTED);
                labelConstraints.gridy = 0;
                labelConstraints.gridwidth = 2;
                fieldsPanel.add(placeholder, labelConstraints);
            }
        } finally {
            updatingFields = false;
        }
        fieldsPanel.revalidate();
        fieldsPanel.repaint();
    }

    private void rebuildActions(ModuleDebugScreen screen) {
        actionsPanel.removeAll();
        for(ModuleDebugAction action : screen.actions){
            JButton button = new JButton(action.label);
            DebugConsoleUi.styleButton(button);
            button.addActionListener(event -> submitAction(action.id));
            actionsPanel.add(button);
        }
        actionsPanel.revalidate();
        actionsPanel.repaint();
    }

    private void submitField(String fieldId, String value) {
        runOnGameThread(() -> {
            String result = DebugEntityService.applyModuleDebugField(entityTag, moduleName, fieldId, value);
            SwingUtilities.invokeLater(() -> {
                logResult(result);
                refresh();
            });
        });
    }

    private void submitAction(String actionId) {
        runOnGameThread(() -> {
            String result = DebugEntityService.executeModuleDebugAction(entityTag, moduleName, actionId);
            SwingUtilities.invokeLater(() -> {
                logResult(result);
                refresh();
            });
        });
    }

    private static void runOnGameThread(Runnable action) {
        if(Gdx.app == null){
            return;
        }
        Gdx.app.postRunnable(action);
    }

    private static void logResult(String result) {
        DesktopDebugConsole.appendLogEntry(new DebugLogEntry(DebugLogEntry.Level.COMMAND, "Module", result));
    }

    private static String windowKey(String entityTag, String moduleName) {
        return entityTag + "|" + moduleName;
    }

    private static String buildWindowTitle(String entityDisplayTag, String moduleName) {
        return "Module · " + entityDisplayTag + " · " + moduleName;
    }
}
