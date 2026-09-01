package com.lyadev.mygame.lwjgl3;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Objects;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
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
import javax.swing.JToolTip;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
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
import com.lyadev.mygame.debug.DebugSelectionHighlight;
import com.lyadev.mygame.debug.DebugWorldService;
import com.lyadev.mygame.debug.DebugWorldSnapshot;
import com.lyadev.mygame.services.MainService;
import com.lyadev.mygame.base.world.GlobalWorld;

public final class DesktopDebugConsole {
    private static final Color BG = color(0x11111b);
    private static final Color PANEL = color(0x181825);
    private static final Color BORDER = color(0x313244);
    private static final Color SELECTION = color(0x585b70);
    private static final Color TEXT = color(0xcdd6f4);
    private static final Color MUTED = color(0xa6adc8);
    private static final Color LOG_COLOR = color(0xa6e3a1);
    private static final Color ERROR_COLOR = color(0xf38ba8);
    private static final Color DEBUG_COLOR = color(0x89dceb);
    private static final Color CMD_COLOR = color(0xf9e2af);
    private static final Color STATUS_COLOR = color(0xb4befe);

    private static JFrame frame;
    private static JTabbedPane tabbedPane;
    private static JLabel headerWorldLabel;
    private static JComboBox<String> headerWorldCombo;
    private static DefaultComboBoxModel<String> headerWorldComboModel;
    private static boolean updatingWorldCombo = false;
    private static JTextPane logPane;
    private static JTextArea statusArea;
    private static JScrollPane statusScrollPane;
    private static DefaultTableModel keysTableModel;
    private static JTable keysTable;
    private static JScrollPane keysScrollPane;
    private static JScrollPane logScrollPane;
    private static JScrollPane entityDetailsScrollPane;
    private static JScrollPane moduleScrollPane;
    private static JCheckBox autoScrollCheckBox;
    private static Timer statusTimer;
    private static boolean historySynced = false;
    private static String lastStatusText = "";
    private static String lastKeysSignature = "";

    private static DefaultListModel<String> entityListModel;
    private static JList<String> entityList;
    private static JTextArea entityDetailsArea;
    private static DefaultTableModel moduleTableModel;
    private static JTable moduleTable;
    private static List<DebugEntitySnapshot> lastEntitySnapshots = List.of();
    private static String lastEntityListSignature = "";
    private static String lastEntityDetailsText = "";
    private static String lastModuleSignature = "";
    private static String selectedModuleName = "";
    private static String lastSelectedEntityTag = "";

    private static DefaultListModel<String> blockListModel;
    private static JList<String> blockList;
    private static JTextArea blockDetailsArea;
    private static JScrollPane blockDetailsScrollPane;
    private static DefaultTableModel blockModuleTableModel;
    private static JTable blockModuleTable;
    private static JScrollPane blockModuleScrollPane;
    private static List<DebugEntitySnapshot> lastBlockSnapshots = List.of();
    private static String lastBlockListSignature = "";
    private static String lastBlockDetailsText = "";
    private static String lastBlockModuleSignature = "";
    private static String selectedBlockModuleName = "";
    private static String lastSelectedBlockTag = "";
    private static boolean updatingBlockModuleTable = false;
    private static boolean suppressBlockModuleWindowOpen = false;

    private static DefaultListModel<String> worldListModel;
    private static JList<String> worldList;
    private static JTextArea worldDetailsArea;
    private static JScrollPane worldDetailsScrollPane;
    private static List<DebugWorldSnapshot> lastWorldSnapshots = List.of();
    private static String lastWorldListSignature = "";
    private static String lastWorldDetailsText = "";
    private static String lastSelectedWorldId = "";

    private static final int TAB_RUNTIME = 0;
    private static final int TAB_LOGS = 1;
    private static final int TAB_KEYS = 2;
    private static final int TAB_WORLDS = 3;
    private static final int TAB_ENTITIES = 4;
    private static final int TAB_BLOCKS = 5;
    private static boolean suppressModuleWindowOpen = false;
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
        UIManager.put("ToolTip.background", color(0x2b2d3a));
        UIManager.put("ToolTip.foreground", TEXT);
        UIManager.put("ToolTip.border", BorderFactory.createLineBorder(BORDER));
    }

    private static JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        stylePanel(header);

        JLabel title = new JLabel("MyGame Debug");
        title.setForeground(TEXT);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 16f));

        JLabel subtitle = new JLabel("Active world: —");
        subtitle.setForeground(MUTED);
        subtitle.setFont(subtitle.getFont().deriveFont(Font.PLAIN, 12f));
        headerWorldLabel = subtitle;

        JPanel titles = new JPanel(new GridLayout(2, 1, 0, 2));
        stylePanel(titles);
        titles.add(title);
        titles.add(subtitle);
        header.add(titles, BorderLayout.WEST);

        headerWorldComboModel = new DefaultComboBoxModel<>();
        headerWorldCombo = new JComboBox<>(headerWorldComboModel);
        headerWorldCombo.setBackground(PANEL);
        headerWorldCombo.setForeground(TEXT);
        headerWorldCombo.setPreferredSize(new Dimension(220, 28));

        JButton travelActiveButton = new JButton("Travel active");
        styleButton(travelActiveButton);
        travelActiveButton.setToolTipText("Move active entity into selected world");
        travelActiveButton.addActionListener(event -> headerWorldAction(true));

        JButton switchOnlyButton = new JButton("Switch only");
        styleButton(switchOnlyButton);
        switchOnlyButton.setToolTipText("Activate world without moving any entity");
        switchOnlyButton.addActionListener(event -> headerWorldAction(false));

        JPanel worldSwitch = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        stylePanel(worldSwitch);
        JLabel switchLabel = new JLabel("World");
        switchLabel.setForeground(MUTED);
        worldSwitch.add(switchLabel);
        worldSwitch.add(headerWorldCombo);
        worldSwitch.add(travelActiveButton);
        worldSwitch.add(switchOnlyButton);
        header.add(worldSwitch, BorderLayout.EAST);
        return header;
    }

    private static JTabbedPane buildTabbedPanel() {
        tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        tabbedPane.setBackground(BG);
        tabbedPane.setForeground(TEXT);
        tabbedPane.setOpaque(true);
        tabbedPane.addTab("Runtime", buildRuntimeTab());
        tabbedPane.addTab("Logs", buildLogsTab());
        tabbedPane.addTab("Keys", buildKeysTab());
        tabbedPane.addTab("Worlds", buildWorldsTab());
        tabbedPane.addTab("Entities", buildEntitiesTab());
        tabbedPane.addTab("Blocks", buildBlocksTab());
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
            if(tabbedPane != null && tabbedPane.getSelectedIndex() == TAB_WORLDS){
                refreshWorldsPanel();
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

    private static JPanel buildWorldsTab() {
        worldListModel = new DefaultListModel<>();
        worldList = new JList<>(worldListModel);
        worldList.setBackground(PANEL);
        worldList.setForeground(TEXT);
        worldList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        worldList.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        worldList.setOpaque(true);
        worldList.addListSelectionListener(event -> {
            if(!event.getValueIsAdjusting()){
                refreshWorldDetailsPanel();
            }
        });

        JScrollPane listScroll = new JScrollPane(worldList);
        styleScrollPane(listScroll);
        listScroll.setPreferredSize(new Dimension(280, 0));

        worldDetailsArea = new JTextArea(10, 36);
        worldDetailsArea.setEditable(false);
        worldDetailsArea.setFocusable(false);
        styleTextArea(worldDetailsArea, TEXT);
        worldDetailsScrollPane = new JScrollPane(worldDetailsArea);
        styleScrollPane(worldDetailsScrollPane);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        stylePanel(actions);
        actions.setBorder(BorderFactory.createEmptyBorder(6, 0, 0, 0));

        JButton travelActiveButton = new JButton("Travel active entity");
        styleButton(travelActiveButton);
        travelActiveButton.setToolTipText("Move GlobalWorld.player into this world (spawn cell)");
        travelActiveButton.addActionListener(event -> withSelectedWorldId(worldId ->
                runOnGameThread(() -> logWorldResult(
                        DebugWorldService.travelWithActiveEntity(worldId, null, null)))));

        JButton switchOnlyButton = new JButton("Switch without entity");
        styleButton(switchOnlyButton);
        switchOnlyButton.setToolTipText("Show this world; leave player parked in previous world");
        switchOnlyButton.addActionListener(event -> withSelectedWorldId(worldId ->
                runOnGameThread(() -> logWorldResult(
                        DebugWorldService.switchWithoutEntity(worldId)))));

        actions.add(travelActiveButton);
        actions.add(switchOnlyButton);

        JPanel right = new JPanel(new BorderLayout(0, 8));
        stylePanel(right);
        right.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 0));
        JLabel detailsLabel = new JLabel("World details");
        detailsLabel.setForeground(TEXT);
        detailsLabel.setFont(detailsLabel.getFont().deriveFont(Font.BOLD, 13f));
        right.add(detailsLabel, BorderLayout.NORTH);

        JPanel rightBody = new JPanel(new BorderLayout(0, 8));
        stylePanel(rightBody);
        rightBody.add(worldDetailsScrollPane, BorderLayout.CENTER);
        rightBody.add(actions, BorderLayout.SOUTH);
        right.add(rightBody, BorderLayout.CENTER);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, listScroll, right);
        split.setResizeWeight(0.35);
        DebugConsoleUi.styleSplitPane(split);

        JPanel worlds = new JPanel(new BorderLayout());
        stylePanel(worlds);
        worlds.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));
        worlds.add(split, BorderLayout.CENTER);
        return worlds;
    }

    private static JPanel buildKeysTab() {
        keysTableModel = new DefaultTableModel(
                new Object[]{"Chord", "Action", "Owner", "Trigger", "Description"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        keysTable = new JTable(keysTableModel) {
            @Override
            public String getToolTipText(MouseEvent event) {
                return descriptionTooltipText(this, event);
            }

            @Override
            public Point getToolTipLocation(MouseEvent event) {
                if(getToolTipText(event) == null){
                    return null;
                }
                Point point = event.getPoint();
                return new Point(point.x + 12, point.y + 18);
            }

            @Override
            public JToolTip createToolTip() {
                JToolTip tip = super.createToolTip();
                tip.setBackground(color(0x2b2d3a));
                tip.setForeground(TEXT);
                tip.setOpaque(true);
                tip.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDER),
                        BorderFactory.createEmptyBorder(4, 8, 4, 8)));
                tip.setFont(getFont());
                return tip;
            }
        };
        keysTable.setBackground(PANEL);
        keysTable.setForeground(TEXT);
        keysTable.setSelectionBackground(SELECTION);
        keysTable.setSelectionForeground(TEXT);
        keysTable.setGridColor(BORDER);
        keysTable.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        keysTable.setRowHeight(22);
        keysTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        keysTable.setOpaque(true);
        keysTable.getTableHeader().setBackground(PANEL);
        keysTable.getTableHeader().setForeground(MUTED);
        keysTable.getTableHeader().setFont(keysTable.getTableHeader().getFont().deriveFont(Font.BOLD, 12f));
        keysTable.getColumnModel().getColumn(0).setPreferredWidth(110);
        keysTable.getColumnModel().getColumn(1).setPreferredWidth(180);
        keysTable.getColumnModel().getColumn(2).setPreferredWidth(130);
        keysTable.getColumnModel().getColumn(3).setPreferredWidth(80);
        keysTable.getColumnModel().getColumn(4).setPreferredWidth(320);
        ToolTipManager.sharedInstance().registerComponent(keysTable);
        installKeysTableRenderer(keysTable);

        keysScrollPane = new JScrollPane(keysTable);
        styleScrollPane(keysScrollPane);

        JLabel keysLabel = new JLabel("Declared key bindings (from getKeyBindings / system)");
        keysLabel.setForeground(TEXT);
        keysLabel.setFont(keysLabel.getFont().deriveFont(Font.BOLD, 13f));

        JPanel keys = new JPanel(new BorderLayout(0, 8));
        stylePanel(keys);
        keys.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));
        keys.add(keysLabel, BorderLayout.NORTH);
        keys.add(keysScrollPane, BorderLayout.CENTER);
        return keys;
    }

    private static void installKeysTableRenderer(JTable table) {
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable tableView,
                    Object value,
                    boolean isSelected,
                    boolean hasFocus,
                    int row,
                    int column) {
                Component component = super.getTableCellRendererComponent(
                        tableView, value, isSelected, hasFocus, row, column);
                if(!isSelected){
                    component.setBackground(row % 2 == 0 ? PANEL : color(0x1a1b26));
                    component.setForeground(column == 0 ? STATUS_COLOR : TEXT);
                }
                return component;
            }
        };
        for(int i = 0; i < table.getColumnCount(); i++){
            table.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }
    }

    private static String descriptionTooltipText(JTable table, MouseEvent event) {
        Point point = event.getPoint();
        int viewRow = table.rowAtPoint(point);
        int viewCol = table.columnAtPoint(point);
        if(viewRow < 0 || viewCol < 0){
            return null;
        }
        int modelCol = table.convertColumnIndexToModel(viewCol);
        if(modelCol != 4){
            return null;
        }
        Object value = table.getValueAt(viewRow, viewCol);
        if(value == null){
            return null;
        }
        String text = value.toString().trim();
        if(text.isEmpty()){
            return null;
        }
        Rectangle cellRect = table.getCellRect(viewRow, viewCol, false);
        TableCellRenderer renderer = table.getCellRenderer(viewRow, viewCol);
        Component component = table.prepareRenderer(renderer, viewRow, viewCol);
        int preferredWidth = component.getPreferredSize().width;
        if(preferredWidth <= cellRect.width - 4){
            return null;
        }
        return text;
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
        moduleTable.setSelectionBackground(SELECTION);
        moduleTable.setSelectionForeground(TEXT);
        moduleTable.setGridColor(BORDER);
        moduleTable.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        moduleTable.setOpaque(true);
        installModuleTableRenderers(moduleTable);
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
        moduleTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        moduleTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent event) {
                if(event.getClickCount() < 1 || suppressModuleWindowOpen){
                    return;
                }
                int row = moduleTable.rowAtPoint(event.getPoint());
                int column = moduleTable.columnAtPoint(event.getPoint());
                if(row < 0 || column == 3){
                    return;
                }
                suppressModuleWindowOpen = true;
                moduleTable.setRowSelectionInterval(row, row);
                suppressModuleWindowOpen = false;
                openSelectedModuleWindow();
            }
        });

        moduleScrollPane = new JScrollPane(moduleTable);
        styleScrollPane(moduleScrollPane);

        JPanel entityInfoPanel = new JPanel(new BorderLayout(0, 4));
        stylePanel(entityInfoPanel);
        JLabel entityInfoLabel = new JLabel("Entity info");
        entityInfoLabel.setForeground(TEXT);
        entityInfoLabel.setFont(entityInfoLabel.getFont().deriveFont(Font.BOLD, 12f));
        entityInfoPanel.add(entityInfoLabel, BorderLayout.NORTH);
        entityInfoPanel.add(entityDetailsScrollPane, BorderLayout.CENTER);

        JPanel modulesPanel = new JPanel(new BorderLayout(0, 4));
        stylePanel(modulesPanel);
        JLabel modulesLabel = new JLabel("Modules — click row to open settings window");
        modulesLabel.setForeground(TEXT);
        modulesLabel.setFont(modulesLabel.getFont().deriveFont(Font.BOLD, 12f));
        modulesPanel.add(modulesLabel, BorderLayout.NORTH);
        modulesPanel.add(moduleScrollPane, BorderLayout.CENTER);

        JSplitPane entitySplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, entityInfoPanel, modulesPanel);
        entitySplit.setResizeWeight(0.48);
        DebugConsoleUi.styleSplitPane(entitySplit);

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
        rightBody.add(entitySplit, BorderLayout.CENTER);
        rightBody.add(actions, BorderLayout.SOUTH);
        right.add(rightBody, BorderLayout.CENTER);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, listScroll, right);
        split.setResizeWeight(0.22);
        DebugConsoleUi.styleSplitPane(split);

        JPanel entities = new JPanel(new BorderLayout());
        stylePanel(entities);
        entities.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));
        entities.add(split, BorderLayout.CENTER);
        return entities;
    }

    private static JPanel buildBlocksTab() {
        blockListModel = new DefaultListModel<>();
        blockList = new JList<>(blockListModel);
        blockList.setBackground(PANEL);
        blockList.setForeground(TEXT);
        blockList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        blockList.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        blockList.setOpaque(true);
        blockList.addListSelectionListener(event -> {
            if(!event.getValueIsAdjusting()){
                refreshBlockDetailsPanel();
            }
        });

        JScrollPane listScroll = new JScrollPane(blockList);
        styleScrollPane(listScroll);
        listScroll.setPreferredSize(new Dimension(240, 0));

        blockDetailsArea = new JTextArea(8, 30);
        blockDetailsArea.setEditable(false);
        blockDetailsArea.setFocusable(false);
        styleTextArea(blockDetailsArea, TEXT);
        blockDetailsScrollPane = new JScrollPane(blockDetailsArea);
        styleScrollPane(blockDetailsScrollPane);

        blockModuleTableModel = new DefaultTableModel(new Object[]{"Module", "State", "Requires", "Pause"}, 0) {
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
        blockModuleTable = new JTable(blockModuleTableModel);
        blockModuleTable.setBackground(PANEL);
        blockModuleTable.setForeground(TEXT);
        blockModuleTable.setSelectionBackground(SELECTION);
        blockModuleTable.setSelectionForeground(TEXT);
        blockModuleTable.setGridColor(BORDER);
        blockModuleTable.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        blockModuleTable.setOpaque(true);
        installModuleTableRenderers(blockModuleTable);
        blockModuleTable.getTableHeader().setBackground(PANEL);
        blockModuleTable.getTableHeader().setForeground(MUTED);
        blockModuleTable.getColumnModel().getColumn(0).setPreferredWidth(140);
        blockModuleTable.getColumnModel().getColumn(1).setPreferredWidth(70);
        blockModuleTable.getColumnModel().getColumn(2).setPreferredWidth(160);
        blockModuleTable.getColumnModel().getColumn(3).setPreferredWidth(50);
        blockModuleTable.getModel().addTableModelListener(event -> {
            if(updatingBlockModuleTable || event.getColumn() != 3){
                return;
            }
            int row = event.getFirstRow();
            if(row < 0 || row >= blockModuleTableModel.getRowCount()){
                return;
            }
            int index = blockList == null ? -1 : blockList.getSelectedIndex();
            if(index < 0 || index >= lastBlockSnapshots.size()){
                return;
            }
            String moduleName = String.valueOf(blockModuleTableModel.getValueAt(row, 0));
            Boolean paused = (Boolean) blockModuleTableModel.getValueAt(row, 3);
            DebugEntitySnapshot snapshot = lastBlockSnapshots.get(index);
            runOnGameThread(() -> DebugEntityService.setModuleRuntimePaused(
                    snapshot.tag,
                    moduleName,
                    Boolean.TRUE.equals(paused)));
        });
        blockModuleTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        blockModuleTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent event) {
                if(event.getClickCount() < 1 || suppressBlockModuleWindowOpen){
                    return;
                }
                int row = blockModuleTable.rowAtPoint(event.getPoint());
                int column = blockModuleTable.columnAtPoint(event.getPoint());
                if(row < 0 || column == 3){
                    return;
                }
                suppressBlockModuleWindowOpen = true;
                blockModuleTable.setRowSelectionInterval(row, row);
                suppressBlockModuleWindowOpen = false;
                openSelectedBlockModuleWindow();
            }
        });
        blockModuleScrollPane = new JScrollPane(blockModuleTable);
        styleScrollPane(blockModuleScrollPane);

        JPanel infoPanel = new JPanel(new BorderLayout(0, 4));
        stylePanel(infoPanel);
        JLabel detailsLabel = new JLabel("Block / prop details");
        detailsLabel.setForeground(TEXT);
        detailsLabel.setFont(detailsLabel.getFont().deriveFont(Font.BOLD, 13f));
        infoPanel.add(detailsLabel, BorderLayout.NORTH);
        infoPanel.add(blockDetailsScrollPane, BorderLayout.CENTER);

        JPanel modulesPanel = new JPanel(new BorderLayout(0, 4));
        stylePanel(modulesPanel);
        JLabel modulesLabel = new JLabel("Modules — click row to open settings window");
        modulesLabel.setForeground(TEXT);
        modulesLabel.setFont(modulesLabel.getFont().deriveFont(Font.BOLD, 12f));
        modulesPanel.add(modulesLabel, BorderLayout.NORTH);
        modulesPanel.add(blockModuleScrollPane, BorderLayout.CENTER);

        JSplitPane rightSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, infoPanel, modulesPanel);
        rightSplit.setResizeWeight(0.4);
        DebugConsoleUi.styleSplitPane(rightSplit);

        JPanel right = new JPanel(new BorderLayout(0, 0));
        stylePanel(right);
        right.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 0));
        right.add(rightSplit, BorderLayout.CENTER);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, listScroll, right);
        split.setResizeWeight(0.3);
        DebugConsoleUi.styleSplitPane(split);

        JPanel blocks = new JPanel(new BorderLayout());
        stylePanel(blocks);
        blocks.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));
        blocks.add(split, BorderLayout.CENTER);
        return blocks;
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
            refreshKeysPanel();
            refreshWorldsPanel();
            refreshEntitiesPanel();
            refreshBlocksPanel();
        });
        statusTimer.start();
    }

    private static void refreshKeysPanel() {
        if(Gdx.app == null || keysTableModel == null || keysTable == null){
            return;
        }
        if(tabbedPane != null && tabbedPane.getSelectedIndex() != TAB_KEYS){
            return;
        }
        Gdx.app.postRunnable(() -> {
            List<com.lyadev.mygame.base.entity.ModuleKeyDebugRow> rows =
                    com.lyadev.mygame.base.entity.ModuleInputRegistry.collectDebugRows();
            String signature = keysSignature(rows);
            SwingUtilities.invokeLater(() -> {
                if(keysTableModel == null || keysTable == null){
                    return;
                }
                if(signature.equals(lastKeysSignature)){
                    return;
                }
                lastKeysSignature = signature;
                keysTableModel.setRowCount(0);
                for(com.lyadev.mygame.base.entity.ModuleKeyDebugRow row : rows){
                    keysTableModel.addRow(new Object[]{
                            row.getChord(),
                            row.getActionId(),
                            row.getOwner(),
                            row.getTrigger(),
                            row.getDescription()
                    });
                }
            });
        });
    }

    private static String keysSignature(List<com.lyadev.mygame.base.entity.ModuleKeyDebugRow> rows) {
        StringBuilder builder = new StringBuilder();
        for(com.lyadev.mygame.base.entity.ModuleKeyDebugRow row : rows){
            builder.append(row.getChord()).append('|')
                    .append(row.getActionId()).append('|')
                    .append(row.getOwner()).append('|')
                    .append(row.getTrigger()).append('|')
                    .append(row.getDescription()).append('\n');
        }
        return builder.toString();
    }

    private static void refreshStatusPanel() {
        if(Gdx.app == null){
            return;
        }
        Gdx.app.postRunnable(() -> {
            MyLogger logger = MainService.getInstance().getLogger();
            List<String> lines = logger != null ? logger.collectStatusLines() : List.of();
            String worldLabel = GlobalWorld.activeWorldLabel();
            SwingUtilities.invokeLater(() -> {
                if(headerWorldLabel != null){
                    headerWorldLabel.setText("Active world: " + worldLabel);
                }
                if(frame != null){
                    frame.setTitle("MyGame Debug — " + worldLabel);
                }
                if(statusArea == null || statusScrollPane == null){
                    return;
                }
                if(tabbedPane != null && tabbedPane.getSelectedIndex() != TAB_RUNTIME){
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

    private static void refreshWorldsPanel() {
        if(Gdx.app == null || worldListModel == null || worldList == null){
            return;
        }
        Gdx.app.postRunnable(() -> {
            List<DebugWorldSnapshot> snapshots = DebugWorldService.collectSnapshots();
            String foundActiveId = null;
            for(DebugWorldSnapshot snapshot : snapshots){
                if(snapshot.active){
                    foundActiveId = snapshot.id;
                    break;
                }
            }
            final String activeId = foundActiveId;
            String signature = worldListSignature(snapshots);
            String selectedId = lastSelectedWorldId;
            SwingUtilities.invokeLater(() -> {
                syncHeaderWorldCombo(snapshots, activeId);
                if(worldListModel == null || worldList == null){
                    return;
                }
                lastWorldSnapshots = snapshots;
                if(tabbedPane != null && tabbedPane.getSelectedIndex() != TAB_WORLDS){
                    return;
                }
                if(!signature.equals(lastWorldListSignature)){
                    lastWorldListSignature = signature;
                    int restore = -1;
                    worldListModel.clear();
                    for(int i = 0; i < snapshots.size(); i++){
                        DebugWorldSnapshot snapshot = snapshots.get(i);
                        worldListModel.addElement(snapshot.listLabel());
                        if(selectedId != null && selectedId.equals(snapshot.id)){
                            restore = i;
                        } else if(restore < 0 && snapshot.active){
                            restore = i;
                        }
                    }
                    if(restore >= 0 && restore < worldListModel.size()){
                        worldList.setSelectedIndex(restore);
                    }
                }
                refreshWorldDetailsPanel();
            });
        });
    }

    private static String worldListSignature(List<DebugWorldSnapshot> snapshots) {
        StringBuilder builder = new StringBuilder();
        for(DebugWorldSnapshot snapshot : snapshots){
            builder.append(snapshot.id).append('|')
                    .append(snapshot.active).append('|')
                    .append(snapshot.loaded).append('|')
                    .append(snapshot.entityCount).append('|')
                    .append(snapshot.mapPath).append('\n');
        }
        return builder.toString();
    }

    private static void syncHeaderWorldCombo(List<DebugWorldSnapshot> snapshots, String activeWorldId) {
        if(headerWorldCombo == null || headerWorldComboModel == null){
            return;
        }
        String previous = (String) headerWorldCombo.getSelectedItem();
        updatingWorldCombo = true;
        headerWorldComboModel.removeAllElements();
        for(DebugWorldSnapshot snapshot : snapshots){
            headerWorldComboModel.addElement(snapshot.id);
        }
        String select = previous;
        if(select == null || headerWorldComboModel.getIndexOf(select) < 0){
            select = activeWorldId;
        }
        if(select != null && headerWorldComboModel.getIndexOf(select) >= 0){
            headerWorldCombo.setSelectedItem(select);
        }
        updatingWorldCombo = false;
    }

    private static void refreshWorldDetailsPanel() {
        if(worldDetailsArea == null || worldList == null){
            return;
        }
        int index = worldList.getSelectedIndex();
        if(index < 0 || index >= lastWorldSnapshots.size()){
            if(!lastWorldDetailsText.isEmpty()){
                lastWorldDetailsText = "";
                worldDetailsArea.setText("");
            }
            lastSelectedWorldId = "";
            return;
        }
        DebugWorldSnapshot snapshot = lastWorldSnapshots.get(index);
        lastSelectedWorldId = snapshot.id;
        String text = String.join(System.lineSeparator(),
                "id: " + snapshot.id,
                "name: " + snapshot.displayName,
                "active: " + snapshot.active,
                "loaded: " + snapshot.loaded,
                "entities: " + snapshot.entityCount,
                "map: " + snapshot.mapPath,
                "spawn: [" + snapshot.spawnCol + "," + snapshot.spawnRow + "]");
        if(!text.equals(lastWorldDetailsText)){
            lastWorldDetailsText = text;
            worldDetailsArea.setText(text);
            worldDetailsArea.setCaretPosition(0);
        }
    }

    private static void headerWorldAction(boolean travelWithActiveEntity) {
        if(headerWorldCombo == null){
            return;
        }
        Object selected = headerWorldCombo.getSelectedItem();
        if(!(selected instanceof String) || ((String) selected).isEmpty()){
            appendLogEntry(new DebugLogEntry(DebugLogEntry.Level.ERROR, "Worlds", "Select a world first"));
            return;
        }
        String worldId = (String) selected;
        runOnGameThread(() -> logWorldResult(travelWithActiveEntity
                ? DebugWorldService.travelWithActiveEntity(worldId, null, null)
                : DebugWorldService.switchWithoutEntity(worldId)));
    }

    private static void withSelectedWorldId(java.util.function.Consumer<String> action) {
        int index = worldList == null ? -1 : worldList.getSelectedIndex();
        if(index < 0 || index >= lastWorldSnapshots.size()){
            appendLogEntry(new DebugLogEntry(DebugLogEntry.Level.ERROR, "Worlds", "Select a world first"));
            return;
        }
        action.accept(lastWorldSnapshots.get(index).id);
    }

    private static void logWorldResult(String result) {
        appendLogEntry(new DebugLogEntry(DebugLogEntry.Level.COMMAND, "Worlds", result));
    }

    private static void refreshEntitiesPanel() {
        if(Gdx.app == null || entityListModel == null){
            return;
        }
        if(tabbedPane != null && tabbedPane.getSelectedIndex() != TAB_ENTITIES){
            return;
        }
        Gdx.app.postRunnable(() -> {
            List<DebugEntitySnapshot> snapshots = DebugEntityService.collectSnapshots();
            SwingUtilities.invokeLater(() -> refreshEntitiesPanelUi(snapshots));
        });
    }

    private static void refreshBlocksPanel() {
        if(Gdx.app == null || blockListModel == null){
            return;
        }
        if(tabbedPane != null && tabbedPane.getSelectedIndex() != TAB_BLOCKS){
            return;
        }
        Gdx.app.postRunnable(() -> {
            List<DebugEntitySnapshot> snapshots = DebugEntityService.collectBlockSnapshots();
            SwingUtilities.invokeLater(() -> refreshBlocksPanelUi(snapshots));
        });
    }

    private static void refreshBlocksPanelUi(List<DebugEntitySnapshot> snapshots) {
        if(blockListModel == null){
            return;
        }
        lastBlockSnapshots = snapshots;
        String signature = buildEntityListSignature(snapshots);
        if(!signature.equals(lastBlockListSignature)){
            lastBlockListSignature = signature;
            int keepIndex = blockList.getSelectedIndex();
            blockListModel.clear();
            for(DebugEntitySnapshot snapshot : snapshots){
                blockListModel.addElement(snapshot.listLabel());
            }
            if(!snapshots.isEmpty()){
                if(keepIndex >= 0 && keepIndex < snapshots.size()){
                    blockList.setSelectedIndex(keepIndex);
                } else if(blockList.getSelectedIndex() < 0){
                    blockList.setSelectedIndex(0);
                }
            }
        }
        refreshBlockDetailsPanel();
    }

    private static void refreshBlockDetailsPanel() {
        if(blockDetailsArea == null || blockModuleTableModel == null){
            return;
        }
        int index = blockList == null ? -1 : blockList.getSelectedIndex();
        if(index < 0 || index >= lastBlockSnapshots.size()){
            updateTextAreaPreserveScroll(blockDetailsScrollPane, blockDetailsArea, "No block selected");
            lastBlockDetailsText = "No block selected";
            blockModuleTableModel.setRowCount(0);
            lastBlockModuleSignature = "";
            selectedBlockModuleName = "";
            lastSelectedBlockTag = "";
            runOnGameThread(DebugSelectionHighlight::clear);
            return;
        }
        DebugEntitySnapshot snapshot = lastBlockSnapshots.get(index);
        boolean blockChanged = !snapshot.tag.equals(lastSelectedBlockTag);
        if(blockChanged){
            lastSelectedBlockTag = snapshot.tag;
            selectedBlockModuleName = "";
            suppressBlockModuleWindowOpen = true;
            blockModuleTable.clearSelection();
            suppressBlockModuleWindowOpen = false;
        }

        String details = formatBlockDetails(snapshot);
        if(!details.equals(lastBlockDetailsText)){
            lastBlockDetailsText = details;
            updateTextAreaPreserveScroll(blockDetailsScrollPane, blockDetailsArea, details);
        }

        runOnGameThread(() -> DebugSelectionHighlight.set(
                snapshot.tag,
                snapshot.x,
                snapshot.y,
                snapshot.width,
                snapshot.height));

        String moduleSignature = buildModuleTableSignature(snapshot);
        String keepModule = blockChanged ? "" : selectedBlockModuleName;
        if(!moduleSignature.equals(lastBlockModuleSignature)){
            lastBlockModuleSignature = moduleSignature;
            Point moduleViewPosition = blockModuleScrollPane == null
                    ? new Point(0, 0)
                    : blockModuleScrollPane.getViewport().getViewPosition();
            updatingBlockModuleTable = true;
            try {
                blockModuleTableModel.setRowCount(0);
                for(DebugModuleSnapshot module : snapshot.modules){
                    blockModuleTableModel.addRow(new Object[]{
                            module.name,
                            module.stateLabel(),
                            joinRequired(module.requiredModules),
                            module.runtimePaused
                    });
                }
            } finally {
                updatingBlockModuleTable = false;
            }
            if(blockModuleScrollPane != null){
                restoreScrollPositionLater(blockModuleScrollPane, moduleViewPosition);
            }

            int reselectRow = -1;
            if(keepModule != null && !keepModule.isEmpty()){
                for(int row = 0; row < blockModuleTableModel.getRowCount(); row++){
                    if(keepModule.equals(String.valueOf(blockModuleTableModel.getValueAt(row, 0)))){
                        reselectRow = row;
                        break;
                    }
                }
            }
            if(reselectRow >= 0){
                suppressBlockModuleWindowOpen = true;
                blockModuleTable.setRowSelectionInterval(reselectRow, reselectRow);
                suppressBlockModuleWindowOpen = false;
            }
        }
    }

    private static void openSelectedBlockModuleWindow() {
        if(suppressBlockModuleWindowOpen || blockList == null
                || blockModuleTable == null || blockModuleTableModel == null){
            return;
        }
        int blockIndex = blockList.getSelectedIndex();
        int moduleRow = blockModuleTable.getSelectedRow();
        if(blockIndex < 0 || blockIndex >= lastBlockSnapshots.size() || moduleRow < 0){
            return;
        }
        DebugEntitySnapshot snapshot = lastBlockSnapshots.get(blockIndex);
        String moduleName = String.valueOf(blockModuleTableModel.getValueAt(moduleRow, 0));
        selectedBlockModuleName = moduleName;
        ModuleDebugWindow.open(snapshot.tag, snapshot.displayTag, moduleName);
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

    private static void openSelectedModuleWindow() {
        if(suppressModuleWindowOpen || entityList == null || moduleTable == null || moduleTableModel == null){
            return;
        }
        int entityIndex = entityList.getSelectedIndex();
        int moduleRow = moduleTable.getSelectedRow();
        if(entityIndex < 0 || entityIndex >= lastEntitySnapshots.size() || moduleRow < 0){
            return;
        }
        DebugEntitySnapshot snapshot = lastEntitySnapshots.get(entityIndex);
        String moduleName = String.valueOf(moduleTableModel.getValueAt(moduleRow, 0));
        selectedModuleName = moduleName;
        ModuleDebugWindow.open(snapshot.tag, snapshot.displayTag, moduleName);
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
        boolean entityChanged = !snapshot.tag.equals(lastSelectedEntityTag);
        if(entityChanged){
            lastSelectedEntityTag = snapshot.tag;
            selectedModuleName = "";
            suppressModuleWindowOpen = true;
            moduleTable.clearSelection();
            suppressModuleWindowOpen = false;
        }

        String details = formatEntityDetails(snapshot);
        if(!details.equals(lastEntityDetailsText)){
            lastEntityDetailsText = details;
            updateTextAreaPreserveScroll(entityDetailsScrollPane, entityDetailsArea, details);
        }

        String moduleSignature = buildModuleTableSignature(snapshot);
        String keepModule = entityChanged ? "" : selectedModuleName;
        if(!moduleSignature.equals(lastModuleSignature)){
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

            int reselectRow = -1;
            if(keepModule != null && !keepModule.isEmpty()){
                for(int row = 0; row < moduleTableModel.getRowCount(); row++){
                    if(keepModule.equals(String.valueOf(moduleTableModel.getValueAt(row, 0)))){
                        reselectRow = row;
                        break;
                    }
                }
            }
            if(reselectRow >= 0){
                suppressModuleWindowOpen = true;
                moduleTable.setRowSelectionInterval(reselectRow, reselectRow);
                suppressModuleWindowOpen = false;
            }
        }
    }

    private static void installModuleTableRenderers(JTable table) {
        DefaultTableCellRenderer textRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable tableView,
                    Object value,
                    boolean isSelected,
                    boolean hasFocus,
                    int row,
                    int column) {
                Component component = super.getTableCellRendererComponent(
                        tableView, value, isSelected, hasFocus, row, column);
                applyModuleTableCellColors(component, isSelected);
                return component;
            }
        };
        for(int column = 0; column < table.getColumnCount(); column++){
            if(column != 3){
                table.getColumnModel().getColumn(column).setCellRenderer(textRenderer);
            }
        }

        JCheckBox pauseRenderer = new JCheckBox();
        pauseRenderer.setOpaque(true);
        pauseRenderer.setBackground(PANEL);
        pauseRenderer.setForeground(TEXT);
        table.getColumnModel().getColumn(3).setCellRenderer(new TableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable tableView,
                    Object value,
                    boolean isSelected,
                    boolean hasFocus,
                    int row,
                    int column) {
                pauseRenderer.setSelected(Boolean.TRUE.equals(value));
                pauseRenderer.setHorizontalAlignment(JCheckBox.CENTER);
                applyModuleTableCellColors(pauseRenderer, isSelected);
                return pauseRenderer;
            }
        });
    }

    private static void applyModuleTableCellColors(Component component, boolean isSelected) {
        if(isSelected){
            component.setBackground(SELECTION);
            component.setForeground(TEXT);
        } else {
            component.setBackground(PANEL);
            component.setForeground(TEXT);
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

    private static String formatBlockDetails(DebugEntitySnapshot snapshot) {
        String idLabel = DebugEntitySnapshot.KIND_TILE.equals(snapshot.kind) ? "tileId" : "frame/id";
        return String.join(System.lineSeparator(),
                "Kind: " + snapshot.kind,
                "Tag: " + snapshot.tag,
                "Display: " + snapshot.displayTag,
                "Position: x=" + String.format("%.1f", snapshot.x) + ", y=" + String.format("%.1f", snapshot.y),
                "Size: " + snapshot.width + " x " + snapshot.height,
                idLabel + ": " + snapshot.spriteIndex,
                "Actor visible: " + snapshot.actorVisible,
                "Init: " + snapshot.init);
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
        ModuleDebugWindow.closeAll();
        if(Gdx.app != null){
            Gdx.app.postRunnable(DebugSelectionHighlight::clear);
        } else {
            DebugSelectionHighlight.clear();
        }
        if(statusTimer != null){
            statusTimer.stop();
            statusTimer = null;
        }
        if(frame != null){
            SwingUtilities.invokeLater(() -> {
                frame.dispose();
                frame = null;
                headerWorldLabel = null;
                headerWorldCombo = null;
                headerWorldComboModel = null;
                tabbedPane = null;
                logPane = null;
                statusArea = null;
                statusScrollPane = null;
                keysTableModel = null;
                keysTable = null;
                keysScrollPane = null;
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
                lastKeysSignature = "";
                lastModuleSignature = "";
                selectedModuleName = "";
                lastSelectedEntityTag = "";
                worldListModel = null;
                worldList = null;
                worldDetailsArea = null;
                worldDetailsScrollPane = null;
                lastWorldSnapshots = List.of();
                lastWorldListSignature = "";
                lastWorldDetailsText = "";
                lastSelectedWorldId = "";
                blockListModel = null;
                blockList = null;
                blockDetailsArea = null;
                blockDetailsScrollPane = null;
                blockModuleTableModel = null;
                blockModuleTable = null;
                blockModuleScrollPane = null;
                lastBlockSnapshots = List.of();
                lastBlockListSignature = "";
                lastBlockDetailsText = "";
                lastBlockModuleSignature = "";
                selectedBlockModuleName = "";
                lastSelectedBlockTag = "";
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
