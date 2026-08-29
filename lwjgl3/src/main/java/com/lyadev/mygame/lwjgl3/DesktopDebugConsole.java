package com.lyadev.mygame.lwjgl3;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.WindowConstants;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import com.badlogic.gdx.Gdx;
import com.lyadev.mygame.MyLogger.MyLogger;
import com.lyadev.mygame.debug.DebugBootstrap;
import com.lyadev.mygame.debug.DebugCommandService;
import com.lyadev.mygame.debug.DebugFeatures;
import com.lyadev.mygame.debug.DebugLogEntry;
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
    private static JTextPane logPane;
    private static JTextArea statusArea;
    private static JScrollPane logScrollPane;
    private static JCheckBox autoScrollCheckBox;
    private static Timer statusTimer;
    private static boolean historySynced = false;

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

        frame = new JFrame("MyGame Debug Console");
        frame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        frame.setMinimumSize(new Dimension(760, 520));
        frame.getContentPane().setBackground(BG);
        frame.getContentPane().setLayout(new BorderLayout(0, 0));

        JPanel root = new JPanel(new BorderLayout(0, 8));
        root.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        root.setBackground(BG);
        root.add(buildHeader(), BorderLayout.NORTH);
        root.add(buildCenterPanel(), BorderLayout.CENTER);
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

    private static JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("MyGame Debug");
        title.setForeground(TEXT);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 16f));

        JLabel subtitle = new JLabel("Live stats + game logs + commands");
        subtitle.setForeground(MUTED);
        subtitle.setFont(subtitle.getFont().deriveFont(Font.PLAIN, 12f));

        JPanel titles = new JPanel(new GridLayout(2, 1, 0, 2));
        titles.setOpaque(false);
        titles.add(title);
        titles.add(subtitle);
        header.add(titles, BorderLayout.WEST);
        return header;
    }

    private static JPanel buildCenterPanel() {
        statusArea = new JTextArea(9, 40);
        statusArea.setEditable(false);
        statusArea.setLineWrap(true);
        statusArea.setWrapStyleWord(true);
        styleTextArea(statusArea, STATUS_COLOR);

        JScrollPane statusScroll = new JScrollPane(statusArea);
        statusScroll.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(4, 6, 4, 6)));
        statusScroll.setBackground(PANEL);
        statusScroll.getViewport().setBackground(PANEL);
        statusScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        statusScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        logPane = new JTextPane();
        logPane.setEditable(false);
        logPane.setBackground(PANEL);
        logPane.setForeground(TEXT);
        logPane.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        logPane.setMargin(new java.awt.Insets(8, 8, 8, 8));

        logScrollPane = new JScrollPane(logPane);
        logScrollPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(0, 0, 0, 0)));
        logScrollPane.getViewport().setBackground(PANEL);
        logScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        logScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        logScrollPane.getVerticalScrollBar().setUnitIncrement(16);

        JPanel logsHeader = new JPanel(new BorderLayout());
        logsHeader.setOpaque(false);
        logsHeader.setBorder(BorderFactory.createEmptyBorder(8, 0, 4, 0));
        JLabel logsLabel = new JLabel("Logs");
        logsLabel.setForeground(TEXT);
        logsLabel.setFont(logsLabel.getFont().deriveFont(Font.BOLD, 13f));
        logsHeader.add(logsLabel, BorderLayout.WEST);

        JPanel logsToolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        logsToolbar.setOpaque(false);
        autoScrollCheckBox = new JCheckBox("Auto-scroll", true);
        autoScrollCheckBox.setOpaque(false);
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

        JPanel statusPanel = new JPanel(new BorderLayout(0, 4));
        statusPanel.setOpaque(false);
        JLabel statusLabel = new JLabel("Runtime status");
        statusLabel.setForeground(TEXT);
        statusLabel.setFont(statusLabel.getFont().deriveFont(Font.BOLD, 13f));
        statusPanel.add(statusLabel, BorderLayout.NORTH);
        statusPanel.add(statusScroll, BorderLayout.CENTER);

        JPanel logsPanel = new JPanel(new BorderLayout(0, 0));
        logsPanel.setOpaque(false);
        logsPanel.add(logsHeader, BorderLayout.NORTH);
        logsPanel.add(logScrollPane, BorderLayout.CENTER);

        JPanel center = new JPanel(new GridLayout(2, 1, 0, 10));
        center.setOpaque(false);
        center.add(statusPanel);
        center.add(logsPanel);
        return center;
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
        commandPanel.setOpaque(false);
        commandPanel.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));
        commandPanel.add(new JLabel("Command"), BorderLayout.WEST);
        commandPanel.add(commandField, BorderLayout.CENTER);
        commandPanel.add(sendButton, BorderLayout.EAST);
        return commandPanel;
    }

    private static void startStatusTimer() {
        if(statusTimer != null){
            statusTimer.stop();
        }
        statusTimer = new Timer(250, event -> refreshStatusPanel());
        statusTimer.start();
    }

    private static void refreshStatusPanel() {
        if(Gdx.app == null || statusArea == null){
            return;
        }
        Gdx.app.postRunnable(() -> {
            MyLogger logger = MainService.getInstance().getLogger();
            List<String> lines = logger.collectStatusLines();
            SwingUtilities.invokeLater(() -> {
                if(statusArea == null){
                    return;
                }
                StringBuilder builder = new StringBuilder();
                for(int i = 0; i < lines.size(); i++){
                    builder.append(lines.get(i));
                    if(i < lines.size() - 1){
                        builder.append(System.lineSeparator());
                    }
                }
                statusArea.setText(builder.toString());
                statusArea.setCaretPosition(0);
            });
        });
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
                logPane = null;
                statusArea = null;
                logScrollPane = null;
                autoScrollCheckBox = null;
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
            if(logPane == null){
                return;
            }
            try {
                StyledDocument document = logPane.getStyledDocument();
                SimpleAttributeSet attrs = attributesFor(entry.getLevel());
                document.insertString(document.getLength(), entry.formatLine() + System.lineSeparator(), attrs);
                if(autoScrollCheckBox == null || autoScrollCheckBox.isSelected()){
                    logPane.setCaretPosition(document.getLength());
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

    private static void styleTextArea(JTextArea area, Color foreground) {
        area.setBackground(PANEL);
        area.setForeground(foreground);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        area.setCaretColor(foreground);
        area.setBorder(BorderFactory.createEmptyBorder(4, 2, 4, 2));
    }

    private static void styleButton(JButton button) {
        button.setBackground(PANEL);
        button.setForeground(TEXT);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)));
    }

    private static Color color(int rgb) {
        return new Color((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF);
    }
}
