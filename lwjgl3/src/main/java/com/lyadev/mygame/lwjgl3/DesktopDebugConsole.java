package com.lyadev.mygame.lwjgl3;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.List;
import java.util.Objects;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import com.badlogic.gdx.Gdx;
import com.lyadev.mygame.MyLogger.MyLogger;
import com.lyadev.mygame.debug.DebugBootstrap;
import com.lyadev.mygame.debug.DebugCommandService;
import com.lyadev.mygame.debug.DebugEntityService;
import com.lyadev.mygame.debug.DebugEntitySnapshot;
import com.lyadev.mygame.debug.DebugFeatures;
import com.lyadev.mygame.debug.DebugLogEntry;
import com.lyadev.mygame.debug.DebugModuleSnapshot;
import com.lyadev.mygame.services.MainService;

public final class DesktopDebugConsole {
    private static final Color BG = color(0x11111b);
    private static final Color PANEL = color(0x181825);
    private static final Color BORDER = color(0x313244);
    private static final Color TEXT = color(0xcdd6f4);
    private static final Color MUTED = color(0xa6adc8);
    private static final Color LOG_COLOR = color(0xa6e3a1);
    private static final Color ERROR_COLOR = color(0xf38ba8);
    private static final Color DEBUG_COLOR = color(0x89dceb);
    private static final Color CMD_COLOR = color(0xf9e2af);
    private static final Color STATUS_COLOR = color(0xb4befe);

    private static JFrame frame;
    private static JTabbedPane tabbedPane;
    private static JTextPane logPane;
    private static JTextArea statusArea;
    private static JScrollPane statusScrollPane;
    private static JScrollPane logScrollPane;
    private static JScrollPane entityDetailsScrollPane;
    private static JScrollPane moduleScrollPane;
    private static JCheckBox autoScrollCheckBox;
    private static Timer statusTimer;
    private static boolean historySynced = false;
    private static String lastStatusText = "";

    private static DefaultListModel<String> entityListModel;
    private static JList<String> entityList;
    private static JTextArea entityDetailsArea;
    private static DefaultTableModel moduleTableModel;
    private static JTable moduleTable;
    private static List<DebugEntitySnapshot> lastEntitySnapshots = List.of();
    private static String lastEntityListSignature = "";
    private static String lastEntityDetailsText = "";
    private static String lastModuleSignature = "";
    private static boolean updatingModuleTable = false;

    private DesktopDebugConsole() {
        throw new UnsupportedOperationException();
    }

    public static void register() {
        DebugBootstrap.register(DesktopDebugConsole::open, DesktopDebugConsole::close);
    }

    public static void open() {
        if(!EventQueue.isDispatchThread()){
            SwingUtilities.invokeLater(DesktopDebugConsole::open);
            return;
        }
        if(frame != null){
            frame.setVisible(true);
            frame.toFront();
            syncHistoryFromLogger();
            return;
        }

        applySwingDefaults();

        frame = new JFrame("MyGame Debug Console");
        frame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        frame.setMinimumSize(new Dimension(860, 560));
        frame.getContentPane().setBackground(BG);

        JPanel root = new JPanel(new BorderLayout(0, 8));
        stylePanel(root);
        root.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        root.add(buildHeader(), BorderLayout.NORTH);
        root.add(buildTabbedPanel(), BorderLayout.CENTER);
        root.add(buildCommandPanel(), BorderLayout.SOUTH);
        frame.setContentPane(root);

        DebugFeatures.setExternalLogSink(DesktopDebugConsole::appendLogEntry);
        DebugFeatures.setOverlayVisible(false);

        appendLogEntry(new DebugLogEntry(DebugLogEntry.Level.STATUS, "Console", "Debug console ready. Type help"));
        syncHistoryFromLogger();
        startStatusTimer();

        frame.pack();
        frame.setLocationByPlatform(true);
        frame.setVisible(true);
    }

    private static void applySwingDefaults() {
        JFrame.setDefaultLookAndFeelDecorated(true);
        UIManager.put("TabbedPane.contentOpaque", Boolean.TRUE);
        UIManager.put("TabbedPane.opaque", Boolean.TRUE);
        UIManager.put("TabbedPane.background", BG);
        UIManager.put("TabbedPane.contentAreaColor", BG);
        UIManager.put("Panel.opaque", Boolean.TRUE);
    }

    private static JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        stylePanel(header);

        JLabel title = new JLabel("MyGame Debug");
        title.setForeground(TEXT);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 16f));

        JLabel subtitle = new JLabel("Runtime · Logs · Entities · Command line below");
        subtitle.setForeground(MUTED);
        subtitle.setFont(subtitle.getFont().deriveFont(Font.PLAIN, 12f));

        JPanel titles = new JPanel(new GridLayout(2, 1, 0, 2));
        stylePanel(titles);
        titles.add(title);
        titles.add(subtitle);
        header.add(titles, BorderLayout.WEST);
        return header;
    }

    private static JTabbedPane buildTabbedPanel() {
        tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        tabbedPane.setBackground(BG);
        tabbedPane.setForeground(TEXT);
        tabbedPane.setOpaque(true);
        tabbedPane.addTab("Runtime", buildRuntimeTab());
        tabbedPane.addTab("Logs", buildLogsTab());
        tabbedPane.addTab("Entities", buildEntitiesTab());
        tabbedPane.addChangeListener(event -> {
            java.awt.Component selected = tabbedPane.getSelectedComponent();
            if(selected instanceof JComponent){
                JComponent panel = (JComponent) selected;
                panel.setOpaque(true);
                panel.revalidate();
                panel.repaint();
            }
            tabbedPane.revalidate();
            tabbedPane.repaint();
            if(frame != null){
                frame.repaint();
            }
        });
        return tabbedPane;
    }

    private static JPanel buildRuntimeTab() {
        statusArea = new JTextArea(12, 40);
        statusArea.setEditable(false);
        statusArea.setFocusable(false);
        statusArea.setLineWrap(true);
        statusArea.setWrapStyleWord(true);
        styleTextArea(statusArea, STATUS_COLOR);

        statusScrollPane = new JScrollPane(statusArea);
        styleScrollPane(statusScrollPane);

        JLabel statusLabel = new JLabel("Runtime status (pinned panel, scroll preserved on refresh)");
        statusLabel.setForeground(TEXT);
        statusLabel.setFont(statusLabel.getFont().deriveFont(Font.BOLD, 13f));

        JPanel runtime = new JPanel(new BorderLayout(0, 8));
        stylePanel(runtime);
        runtime.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));
        runtime.add(statusLabel, BorderLayout.NORTH);
        runtime.add(statusScrollPane, BorderLayout.CENTER);
        return runtime;
    }

    private static JPanel buildLogsTab() {
        logPane = new JTextPane();
        logPane.setEditable(false);
        logPane.setFocusable(false);
        logPane.setBackground(PANEL);
        logPane.setForeground(TEXT);
        logPane.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        logPane.setMargin(new java.awt.Insets(8, 8, 8, 8));
        logPane.setOpaque(true);

        logScrollPane = new JScrollPane(logPane);
        styleScrollPane(logScrollPane);
        logScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        JPanel logsHeader = new JPanel(new BorderLayout());
        stylePanel(logsHeader);
        logsHeader.setBorder(BorderFactory.createEmptyBorder(4, 0, 8, 0));

        JLabel logsLabel = new JLabel("Game logs");
        logsLabel.setForeground(TEXT);
        logsLabel.setFont(logsLabel.getFont().deriveFont(Font.BOLD, 13f));
        logsHeader.add(logsLabel, BorderLayout.WEST);

        JPanel logsToolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        stylePanel(logsToolbar);
        autoScrollCheckBox = new JCheckBox("Auto-scroll", true);
        autoScrollCheckBox.setOpaque(true);
        autoScrollCheckBox.setBackground(BG);
        autoScrollCheckBox.setForeground(MUTED);
        JButton clearButton = new JButton("Clear logs");
        styleButton(clearButton);
        clearButton.addActionListener(event -> {
            if(Gdx.app != null){
                Gdx.app.postRunnable(() -> MainService.getInstance().getLogger().clearLogs());
            }
            clearLogPane();
        });
        logsToolbar.add(autoScrollCheckBox);
        logsToolbar.add(clearButton);
        logsHeader.add(logsToolbar, BorderLayout.EAST);

        JPanel logs = new JPanel(new BorderLayout(0, 0));
        stylePanel(logs);
        logs.add(logsHeader, BorderLayout.NORTH);
        logs.add(logScrollPane, BorderLayout.CENTER);
        return logs;
    }

    private static JPanel buildEntitiesTab() {
        entityListModel = new DefaultListModel<>();
        entityList = new JList<>(entityListModel);
        entityList.setBackground(PANEL);
        entityList.setForeground(TEXT);
        entityList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        entityList.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        entityList.setOpaque(true);
        entityList.addListSelectionListener(event -> {
            if(!event.getValueIsAdjusting()){
                refreshEntityDetailsPanel();
            }
        });

        JScrollPane listScroll = new JScrollPane(entityList);
        styleScrollPane(listScroll);
        listScroll.setPreferredSize(new Dimension(180, 0));

        entityDetailsArea = new JTextArea(6, 30);
        entityDetailsArea.setEditable(false);
        entityDetailsArea.setFocusable(false);
        styleTextArea(entityDetailsArea, TEXT);

        entityDetailsScrollPane = new JScrollPane(entityDetailsArea);
        styleScrollPane(entityDetailsScrollPane);
        entityDetailsScrollPane.setPreferredSize(new Dimension(0, 120));

        moduleTableModel = new DefaultTableModel(new Object[]{"Module", "State", "Requires", "Pause"}, 0) {
            @Override
            public Class<?> getColumnClass(int column) {
                return column == 3 ? Boolean.class : String.class;
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                if(column != 3){
                    return false;
                }
                Object state = getValueAt(row, 1);
                return "active".equals(state) || "paused".equals(state);
            }
        };
        moduleTable = new JTable(moduleTableModel);
        moduleTable.setBackground(PANEL);
        moduleTable.setForeground(TEXT);
        moduleTable.setSelectionBackground(BORDER);
        moduleTable.setGridColor(BORDER);
        moduleTable.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        moduleTable.setOpaque(true);
        moduleTable.getTableHeader().setBackground(PANEL);
        moduleTable.getTableHeader().setForeground(MUTED);
        moduleTable.getColumnModel().getColumn(0).setPreferredWidth(140);
        moduleTable.getColumnModel().getColumn(1).setPreferredWidth(70);
        moduleTable.getColumnModel().getColumn(2).setPreferredWidth(160);
        moduleTable.getColumnModel().getColumn(3).setPreferredWidth(50);
        moduleTable.getModel().addTableModelListener(event -> {
            if(updatingModuleTable || event.getColumn() != 3){
                return;
            }
            int row = event.getFirstRow();
            if(row < 0 || row >= moduleTableModel.getRowCount()){
                return;
            }
            int index = entityList == null ? -1 : entityList.getSelectedIndex();
            if(index < 0 || index >= lastEntitySnapshots.size()){
                return;
            }
            String moduleName = String.valueOf(moduleTableModel.getValueAt(row, 0));
            Boolean paused = (Boolean) moduleTableModel.getValueAt(row, 3);
            DebugEntitySnapshot snapshot = lastEntitySnapshots.get(index);
            runOnGameThread(() -> DebugEntityService.setModuleRuntimePaused(
                    snapshot.tag,
                    moduleName,
                    Boolean.TRUE.equals(paused)));
        });

        moduleScrollPane = new JScrollPane(moduleTable);
        styleScrollPane(moduleScrollPane);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        stylePanel(actions);
        actions.setBorder(BorderFactory.createEmptyBorder(6, 0, 0, 0));

        JButton activeButton = new JButton("Set active");
        JButton teleportButton = new JButton("Random pos");
        styleButton(activeButton);
        styleButton(teleportButton);

        activeButton.addActionListener(event -> withSelectedEntityTag(tag ->
                runOnGameThread(() -> logCommandResult(DebugEntityService.setActivePlayer(tag)))));
        teleportButton.addActionListener(event -> withSelectedEntityTag(tag ->
                runOnGameThread(() -> logCommandResult(DebugEntityService.teleportRandom(tag)))));

        actions.add(activeButton);
        actions.add(teleportButton);

        JPanel right = new JPanel(new BorderLayout(0, 8));
        stylePanel(right);
        right.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 0));

        JLabel detailsLabel = new JLabel("Entity details");
        detailsLabel.setForeground(TEXT);
        detailsLabel.setFont(detailsLabel.getFont().deriveFont(Font.BOLD, 13f));
        right.add(detailsLabel, BorderLayout.NORTH);

        JPanel rightBody = new JPanel(new BorderLayout(0, 8));
        stylePanel(rightBody);
        rightBody.add(entityDetailsScrollPane, BorderLayout.NORTH);
        rightBody.add(moduleScrollPane, BorderLayout.CENTER);
        rightBody.add(actions, BorderLayout.SOUTH);
        right.add(rightBody, BorderLayout.CENTER);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, listScroll, right);
        split.setResizeWeight(0.22);
        split.setBorder(BorderFactory.createEmptyBorder());
        split.setBackground(BG);
        split.setOpaque(true);
        split.setContinuousLayout(true);

        JPanel entities = new JPanel(new BorderLayout());
        stylePanel(entities);
        entities.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));
        entities.add(split, BorderLayout.CENTER);
        return entities;
    }

    private static JPanel buildCommandPanel() {
        JTextField commandField = new JTextField();
        commandField.setBackground(PANEL);
        commandField.setForeground(TEXT);
        commandField.setCaretColor(TEXT);
        commandField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)));
        commandField.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));

        JButton sendButton = new JButton("Run");
        styleButton(sendButton);

        Runnable submitCommand = () -> {
            String command = commandField.getText().trim();
            if(command.isEmpty()){
                return;
            }
            commandField.setText("");
            appendLogEntry(new DebugLogEntry(DebugLogEntry.Level.COMMAND, "CMD", "> " + command));
            if(Gdx.app == null){
                appendLogEntry(new DebugLogEntry(DebugLogEntry.Level.ERROR, "Console", "Game is not ready yet"));
                return;
            }
            Gdx.app.postRunnable(() -> {
                String result = DebugCommandService.execute(command);
                appendLogEntry(new DebugLogEntry(DebugLogEntry.Level.COMMAND, "Result", result));
                if("clear".equalsIgnoreCase(command)){
                    clearLogPane();
                }
            });
        };

        commandField.addActionListener(event -> submitCommand.run());
        sendButton.addActionListener(event -> submitCommand.run());

        JPanel commandPanel = new JPanel(new BorderLayout(8, 0));
        stylePanel(commandPanel);
        commandPanel.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));
        JLabel commandLabel = new JLabel("Command");
        commandLabel.setForeground(MUTED);
        commandPanel.add(commandLabel, BorderLayout.WEST);
        commandPanel.add(commandField, BorderLayout.CENTER);
        commandPanel.add(sendButton, BorderLayout.EAST);
        return commandPanel;
    }

    private static void startStatusTimer() {
        if(statusTimer != null){
            statusTimer.stop();
        }
        statusTimer = new Timer(400, event -> {
            refreshStatusPanel();
            refreshEntitiesPanel();
        });
        statusTimer.start();
    }

    private static void refreshStatusPanel() {
        if(Gdx.app == null || statusArea == null || statusScrollPane == null){
            return;
        }
        if(tabbedPane != null && tabbedPane.getSelectedIndex() != 0){
            return;
        }
        Gdx.app.postRunnable(() -> {
            MyLogger logger = MainService.getInstance().getLogger();
            List<String> lines = logger.collectStatusLines();
            SwingUtilities.invokeLater(() -> {
                if(statusArea == null || statusScrollPane == null){
                    return;
                }
                String next = joinLines(lines);
                if(next.equals(lastStatusText)){
                    return;
                }
                lastStatusText = next;
                updateTextAreaPreserveScroll(statusScrollPane, statusArea, next);
            });
        });
    }

    private static void refreshEntitiesPanel() {
        if(Gdx.app == null || entityListModel == null){
            return;
        }
        if(tabbedPane != null && tabbedPane.getSelectedIndex() != 2){
            return;
        }
        Gdx.app.postRunnable(() -> {
            List<DebugEntitySnapshot> snapshots = DebugEntityService.collectSnapshots();
            SwingUtilities.invokeLater(() -> refreshEntitiesPanelUi(snapshots));
        });
    }

    private static void refreshEntitiesPanelUi(List<DebugEntitySnapshot> snapshots) {
        if(entityListModel == null){
            return;
        }
        lastEntitySnapshots = snapshots;
        String signature = buildEntityListSignature(snapshots);
        if(!signature.equals(lastEntityListSignature)){
            lastEntityListSignature = signature;
            int keepIndex = entityList.getSelectedIndex();
            entityListModel.clear();
            for(DebugEntitySnapshot snapshot : snapshots){
                entityListModel.addElement(snapshot.listLabel());
            }
            if(!snapshots.isEmpty()){
                if(keepIndex >= 0 && keepIndex < snapshots.size()){
                    entityList.setSelectedIndex(keepIndex);
                } else if(entityList.getSelectedIndex() < 0){
                    entityList.setSelectedIndex(0);
                }
            }
        }
        refreshEntityDetailsPanel();
    }

    private static void refreshEntityDetailsPanel() {
        if(entityDetailsArea == null || moduleTableModel == null){
            return;
        }
        int index = entityList == null ? -1 : entityList.getSelectedIndex();
        if(index < 0 || index >= lastEntitySnapshots.size()){
            updateTextAreaPreserveScroll(entityDetailsScrollPane, entityDetailsArea, "No entity selected");
            moduleTableModel.setRowCount(0);
            lastEntityDetailsText = "No entity selected";
            return;
        }
        DebugEntitySnapshot snapshot = lastEntitySnapshots.get(index);
        String details = formatEntityDetails(snapshot);
        if(!details.equals(lastEntityDetailsText)){
            lastEntityDetailsText = details;
            updateTextAreaPreserveScroll(entityDetailsScrollPane, entityDetailsArea, details);
        }

        String moduleSignature = buildModuleTableSignature(snapshot);
        if(moduleSignature.equals(lastModuleSignature)){
            return;
        }
        lastModuleSignature = moduleSignature;

        Point moduleViewPosition = moduleScrollPane == null
                ? new Point(0, 0)
                : moduleScrollPane.getViewport().getViewPosition();
        updatingModuleTable = true;
        try {
            moduleTableModel.setRowCount(0);
            for(DebugModuleSnapshot module : snapshot.modules){
                moduleTableModel.addRow(new Object[]{
                        module.name,
                        module.stateLabel(),
                        joinRequired(module.requiredModules),
                        module.runtimePaused
                });
            }
        } finally {
            updatingModuleTable = false;
        }
        if(moduleScrollPane != null){
            restoreScrollPositionLater(moduleScrollPane, moduleViewPosition);
        }
    }

    private static String buildEntityListSignature(List<DebugEntitySnapshot> snapshots) {
        StringBuilder builder = new StringBuilder();
        for(DebugEntitySnapshot snapshot : snapshots){
            builder.append(snapshot.listLabel()).append('|');
        }
        return builder.toString();
    }

    private static String buildModuleTableSignature(DebugEntitySnapshot snapshot) {
        StringBuilder builder = new StringBuilder();
        for(DebugModuleSnapshot module : snapshot.modules){
            builder.append(module.name)
                    .append(':')
                    .append(module.stateLabel())
                    .append(':')
                    .append(module.runtimePaused)
                    .append('|');
        }
        return builder.toString();
    }

    private static String joinLines(List<String> lines) {
        StringBuilder builder = new StringBuilder();
        for(int i = 0; i < lines.size(); i++){
            builder.append(lines.get(i));
            if(i < lines.size() - 1){
                builder.append(System.lineSeparator());
            }
        }
        return builder.toString();
    }

    private static void updateTextAreaPreserveScroll(JScrollPane scrollPane, JTextArea area, String text) {
        if(Objects.equals(text, area.getText())){
            return;
        }
        Point viewPosition = scrollPane.getViewport().getViewPosition();
        area.setText(text);
        restoreScrollPositionLater(scrollPane, viewPosition);
    }

    private static void restoreScrollPositionLater(JScrollPane scrollPane, Point viewPosition) {
        Runnable restore = () -> {
            if(scrollPane.getViewport().getView() == null){
                return;
            }
            JScrollBar bar = scrollPane.getVerticalScrollBar();
            int max = Math.max(0, bar.getMaximum() - bar.getVisibleAmount());
            int y = Math.min(viewPosition.y, max);
            scrollPane.getViewport().setViewPosition(new Point(0, y));
        };
        SwingUtilities.invokeLater(restore);
    }

    private static void restoreScrollBarLater(JScrollPane scrollPane, int scrollValue) {
        Runnable restore = () -> {
            JScrollBar bar = scrollPane.getVerticalScrollBar();
            int max = Math.max(0, bar.getMaximum() - bar.getVisibleAmount());
            bar.setValue(Math.min(scrollValue, max));
        };
        SwingUtilities.invokeLater(restore);
    }

    private static String formatEntityDetails(DebugEntitySnapshot snapshot) {
        return String.join(System.lineSeparator(),
                "Tag: " + snapshot.tag,
                "Display: " + snapshot.displayTag,
                "Position: x=" + String.format("%.1f", snapshot.x) + ", y=" + String.format("%.1f", snapshot.y),
                "Size: " + snapshot.width + " x " + snapshot.height,
                "Status: active=" + snapshot.active
                        + ", focused=" + snapshot.focused
                        + ", show=" + snapshot.show
                        + ", init=" + snapshot.init,
                "Actor visible: " + snapshot.actorVisible,
                "Vision visible count: " + snapshot.visibleEntities,
                "Sprite index: " + snapshot.spriteIndex,
                "Current player: " + snapshot.currentPlayer);
    }

    private static String joinRequired(List<String> requiredModules) {
        if(requiredModules == null || requiredModules.isEmpty()){
            return "—";
        }
        return String.join(", ", requiredModules);
    }

    private static void withSelectedEntityTag(java.util.function.Consumer<String> action) {
        int index = entityList == null ? -1 : entityList.getSelectedIndex();
        if(index < 0 || index >= lastEntitySnapshots.size()){
            appendLogEntry(new DebugLogEntry(DebugLogEntry.Level.ERROR, "Entities", "Select an entity first"));
            return;
        }
        action.accept(lastEntitySnapshots.get(index).tag);
    }

    private static void runOnGameThread(Runnable action) {
        if(Gdx.app == null){
            appendLogEntry(new DebugLogEntry(DebugLogEntry.Level.ERROR, "Console", "Game is not ready yet"));
            return;
        }
        Gdx.app.postRunnable(action);
    }

    private static void logCommandResult(String result) {
        appendLogEntry(new DebugLogEntry(DebugLogEntry.Level.COMMAND, "Entities", result));
    }

    private static void syncHistoryFromLogger() {
        if(Gdx.app == null || historySynced){
            return;
        }
        Gdx.app.postRunnable(() -> {
            MyLogger logger = MainService.getInstance().getLogger();
            for(DebugLogEntry entry : logger.getLogEntriesSnapshot()){
                appendLogEntry(entry);
            }
            historySynced = true;
        });
    }

    public static void close() {
        if(statusTimer != null){
            statusTimer.stop();
            statusTimer = null;
        }
        if(frame != null){
            SwingUtilities.invokeLater(() -> {
                frame.dispose();
                frame = null;
                tabbedPane = null;
                logPane = null;
                statusArea = null;
                statusScrollPane = null;
                logScrollPane = null;
                entityDetailsScrollPane = null;
                moduleScrollPane = null;
                autoScrollCheckBox = null;
                entityListModel = null;
                entityList = null;
                entityDetailsArea = null;
                moduleTableModel = null;
                moduleTable = null;
                lastEntitySnapshots = List.of();
                lastEntityListSignature = "";
                lastEntityDetailsText = "";
                lastStatusText = "";
                lastModuleSignature = "";
            });
        }
        historySynced = false;
        DebugFeatures.setExternalLogSink(null);
    }

    public static void appendLogEntry(DebugLogEntry entry) {
        if(entry == null){
            return;
        }
        Runnable appendTask = () -> {
            if(logPane == null || logScrollPane == null){
                return;
            }
            try {
                JScrollBar bar = logScrollPane.getVerticalScrollBar();
                int scrollValue = bar.getValue();
                boolean stickToBottom = autoScrollCheckBox != null && autoScrollCheckBox.isSelected();

                StyledDocument document = logPane.getStyledDocument();
                SimpleAttributeSet attrs = attributesFor(entry.getLevel());
                document.insertString(document.getLength(), entry.formatLine() + System.lineSeparator(), attrs);

                if(stickToBottom){
                    logPane.setCaretPosition(document.getLength());
                } else {
                    restoreScrollBarLater(logScrollPane, scrollValue);
                }
            } catch(BadLocationException ignored) {
            }
        };
        if(EventQueue.isDispatchThread()){
            appendTask.run();
        } else {
            SwingUtilities.invokeLater(appendTask);
        }
    }

    private static void clearLogPane() {
        Runnable clearTask = () -> {
            if(logPane != null){
                logPane.setText("");
            }
        };
        if(EventQueue.isDispatchThread()){
            clearTask.run();
        } else {
            SwingUtilities.invokeLater(clearTask);
        }
    }

    private static SimpleAttributeSet attributesFor(DebugLogEntry.Level level) {
        SimpleAttributeSet attrs = new SimpleAttributeSet();
        StyleConstants.setFontFamily(attrs, Font.MONOSPACED);
        StyleConstants.setFontSize(attrs, 12);
        StyleConstants.setForeground(attrs, colorFor(level));
        return attrs;
    }

    private static Color colorFor(DebugLogEntry.Level level) {
        switch(level){
            case ERROR:
                return ERROR_COLOR;
            case DEBUG:
                return DEBUG_COLOR;
            case COMMAND:
                return CMD_COLOR;
            case STATUS:
                return STATUS_COLOR;
            default:
                return LOG_COLOR;
        }
    }

    private static void stylePanel(JPanel panel) {
        panel.setOpaque(true);
        panel.setBackground(BG);
    }

    private static void styleScrollPane(JScrollPane scrollPane) {
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
            protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
                g.setColor(PANEL);
                g.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);
            }

            @Override
            protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
                if(thumbBounds.isEmpty() || !scrollbar.isEnabled()){
                    return;
                }
                g.setColor(BORDER);
                g.fillRoundRect(
                        thumbBounds.x + 2,
                        thumbBounds.y + 2,
                        Math.max(0, thumbBounds.width - 4),
                        Math.max(0, thumbBounds.height - 4),
                        6,
                        6);
            }
        });
    }

    private static void styleTextArea(JTextArea area, Color foreground) {
        area.setBackground(PANEL);
        area.setForeground(foreground);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        area.setCaretColor(foreground);
        area.setBorder(BorderFactory.createEmptyBorder(4, 2, 4, 2));
        area.setOpaque(true);
    }

    private static void styleButton(JButton button) {
        button.setBackground(PANEL);
        button.setForeground(TEXT);
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)));
    }

    private static Color color(int rgb) {
        return new Color((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF);
    }
}
