package ThreatDetectionApp;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.plaf.basic.ComboPopup;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import ThreatDetectionApp.AnalysingSystem.AnalysisProgress;
import ThreatDetectionApp.AnalysingSystem.AnalysisResult;
import ThreatDetectionApp.AnalysingSystem.RiskLevel;
import ThreatDetectionApp.AnalysingSystem.VerificationStatus;

/**
 * Threat Detection GUI.
 * ONE table holds everything: the URLs / messages / CSV rows you add (status Pending) and,
 * while the backend analyzes them one after the other (it is sequential, no threads),
 * each row goes Pending -> Running -> Done (or Error) and gets its Risk.
 */
@SuppressWarnings("serial")
public class MainFrame extends JFrame {

    private static final long serialVersionUID = 1L;

    // =====================================================================
    // Design tokens (colors + fonts). Change them here to re-theme the app.
    // =====================================================================

    // ---- brand colors: the same in light and dark mode ----
    static final Color PRIMARY       = new Color(0x16A34A);
    static final Color PRIMARY_HOVER = new Color(0x15803D);

    // ---- theme colors: filled by setTheme(...) (light or dark) ----
    static boolean darkMode;
    static Color BG, CARD, LINE, FIELD_BG, FIELD_LINE, INK, MUTED, PLACEHOLDER;
    static Color TABLE_BG, ROW_ALT, ROW_SEL, HEADER_ROW;
    static Color DANGER, DANGER_HOVER, GHOST_HOVER, DISABLED_BG, SHADOW, SCROLL_THUMB;
    static Color HEAD_A, HEAD_B, HEAD_SUB;
    static Color OK, WARN, INFO, TOTAL_BG, TOTAL_FG;

    static void setTheme(boolean dark) {
        darkMode = dark;
        if (dark) {
            BG = new Color(0x0B1220);        CARD = new Color(0x111A2E);       LINE = new Color(0x1F2A44);
            FIELD_BG = new Color(0x0B1424);  FIELD_LINE = new Color(0x334155);
            INK = new Color(0xE2E8F0);       MUTED = new Color(0x94A3B8);      PLACEHOLDER = new Color(0x64748B);
            TABLE_BG = new Color(0x0F172A);  ROW_ALT = new Color(0x131D33);    ROW_SEL = new Color(0x1E3A8A);
            HEADER_ROW = new Color(0x16213A);
            DANGER = new Color(0xF87171);    DANGER_HOVER = new Color(0x3B1D24); GHOST_HOVER = new Color(0x1B2640);
            DISABLED_BG = new Color(0x1F2A44); SHADOW = new Color(0, 0, 0, 70); SCROLL_THUMB = new Color(0x334155);
            HEAD_A = new Color(0x020617);    HEAD_B = new Color(0x1E3A8A);     HEAD_SUB = new Color(0x93C5FD);
            OK = new Color(0x4ADE80);        WARN = new Color(0xFBBF24);       INFO = new Color(0x60A5FA);
            TOTAL_BG = new Color(0x1E2A5A);  TOTAL_FG = new Color(0xA5B4FC);
        } else {
            BG = new Color(0xF1F5F9);        CARD = Color.WHITE;               LINE = new Color(0xE2E8F0);
            FIELD_BG = Color.WHITE;          FIELD_LINE = new Color(0xCBD5E1);
            INK = new Color(0x0F172A);       MUTED = new Color(0x64748B);      PLACEHOLDER = new Color(0x94A3B8);
            TABLE_BG = Color.WHITE;          ROW_ALT = new Color(0xF8FAFC);    ROW_SEL = new Color(0xDBEAFE);
            HEADER_ROW = new Color(0xF8FAFC);
            DANGER = new Color(0xDC2626);    DANGER_HOVER = new Color(0xFEE2E2); GHOST_HOVER = new Color(0xF1F5F9);
            DISABLED_BG = new Color(0xE2E8F0); SHADOW = new Color(15, 23, 42, 12); SCROLL_THUMB = new Color(0xCBD5E1);
            HEAD_A = new Color(0x0F172A);    HEAD_B = new Color(0x1D4ED8);     HEAD_SUB = new Color(0xBFDBFE);
            OK = new Color(0x15803D);        WARN = new Color(0xB45309);       INFO = new Color(0x2563EB);
            TOTAL_BG = new Color(0xE0E7FF);  TOTAL_FG = new Color(0x3730A3);
        }
    }

    // the last choice (light / dark) is remembered between runs
    private static boolean loadDarkPref() {
        try { return Preferences.userNodeForPackage(MainFrame.class).getBoolean("darkMode", false); }
        catch (Exception e) { return false; }
    }

    private static void saveDarkPref(boolean dark) {
        try { Preferences.userNodeForPackage(MainFrame.class).putBoolean("darkMode", dark); }
        catch (Exception ignored) { }
    }

    static { setTheme(loadDarkPref()); }

    static Font ui(int style, int size) {
        return new Font("Segoe UI", style, size);
    }

    // table columns
    private static final int COL_ID = 0, COL_TYPE = 1, COL_CONTENT = 2, COL_STATUS = 3, COL_PROGRESS = 4,
            COL_RISK = 5, COL_CAUSE = 6, COL_SOURCE = 7, COL_EXP_RISK = 8, COL_EXP_CAT = 9;
    // what the Status column shows
    private static final String PENDING = "PENDING", RUNNING = "RUNNING", DONE = "DONE", ERROR = "ERROR";
    // what the Risk column shows (before the result exists / when the input could not be verified)
    private static final String NO_RISK = "...", NOT_VERIFIED = "NOT_VERIFIED";
    private static final String CSV_HINT = "<html><b>Expected columns:</b><br>id, input_type, content,<br>"
            + "expected_risk_level, expected_category,<br>triggered_rules_description</html>";

    // ---- backend objects (unchanged) ----
    private final AnalysingSystem system = new AnalysingSystem();
    private final AnalysingList manualList = new AnalysingList();
    private int nextManualId = 1;

    // ---- CSV that is currently loaded (null = none) ----
    private File csvFile;
    private final List<CSVfile.CSVRecord> csvRecords = new ArrayList<>();

    // ---- "add inputs" widgets ----
    private final JComboBox<String> typeCombo = new Combo(new String[] { "URL", "Message" });
    private final Field contentField = new Field("https://example.com/login");
    private final JButton addBtn = new RoundedButton("Add", RoundedButton.Kind.FILLED);
    private final JButton browseBtn = new RoundedButton("Load CSV", RoundedButton.Kind.OUTLINE);
    private final JLabel csvInfo = new TLabel("No CSV loaded (optional)", () -> MUTED);

    // ---- the ONE table ----
    private final DefaultTableModel resultModel = new DefaultTableModel(new Object[] {
            "ID", "Type", "Content", "Status", "Progress", "Risk", "Cause", "Source",
            "Expected risk", "Expected category" }, 0) {
        private static final long serialVersionUID = 1L;
        @Override public boolean isCellEditable(int r, int c) { return false; }
        @Override public Class<?> getColumnClass(int c) {
            return (c == COL_ID || c == COL_PROGRESS) ? Integer.class : String.class;
        }
    };
    /** Same order as the table rows (model index). null = row not analyzed yet. */
    private final List<AnalysisResult> rowResults = new ArrayList<>();
    private final JTable resultTable = new EmptyTable(resultModel,
            "Nothing here yet. Add a URL, a message or a CSV file above, then press Analyze all.");
    private final TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(resultModel);
    private final JComboBox<String> filterCombo = new Combo(new String[] {
            "All", "HIGH", "MEDIUM", "LOW", "NOT_VERIFIED", PENDING, RUNNING, DONE, ERROR });
    private final JLabel rowCount = new TLabel("0 rows", () -> MUTED);
    private final JButton removeBtn = new RoundedButton("Remove selected", RoundedButton.Kind.ALERT);
    private final JButton clearAllBtn = new RoundedButton("Clear all", RoundedButton.Kind.OUTLINE);

    // ---- summary chips ----
    private final Chip totalChip  = new Chip("Total",  () -> TOTAL_BG, () -> TOTAL_FG);
    private final Chip highChip   = new Chip("High",   () -> riskBg("HIGH"),   () -> riskFg("HIGH"));
    private final Chip mediumChip = new Chip("Medium", () -> riskBg("MEDIUM"), () -> riskFg("MEDIUM"));
    private final Chip lowChip    = new Chip("Low",    () -> riskBg("LOW"),    () -> riskFg("LOW"));
    private final Chip errorChip  = new Chip("Not verified", () -> riskBg("NA"), () -> riskFg("NA"));

    // ---- bottom bar ----
    private final JButton analyzeButton = new RoundedButton("Analyze all", RoundedButton.Kind.FILLED);
    private final ModernProgress progressBar = new ModernProgress();
    private final JLabel statusLabel = new TLabel("Ready.", () -> MUTED);
    private final JLabel doneLabel = new TLabel("0 / 0 done", () -> INK);
    private final JComboBox<String> speedCombo = new Combo(new String[] { "Step by step", "Instant" });
    private JScrollPane resultScroll;
    private ThemeToggle themeToggle;

    // little animation of the "Running" row (purely visual)
    private javax.swing.Timer runTimer;
    private int runRow = -1, runDelay;
    private long runStart;

    public MainFrame() {
        super("Threat Detection App");

        java.net.URL iconURL = getClass().getResource("/resources/analysis.png");
        if (iconURL != null) {
            setIconImage(new ImageIcon(iconURL).getImage());
        } else {
            setIconImage(new ShieldIcon(48).toImage());
        }
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        getContentPane().setBackground(BG);
        setLayout(new BorderLayout());

        browseBtn.setToolTipText(CSV_HINT);
        csvInfo.setToolTipText(CSV_HINT);

        JPanel body = new JPanel(new BorderLayout(0, 10));
        body.setOpaque(false);
        body.setBorder(BorderFactory.createEmptyBorder(12, 18, 14, 18));
        body.add(buildAddArea(), BorderLayout.NORTH);
        body.add(buildResultsArea(), BorderLayout.CENTER); // the table takes ALL the free space
        body.add(buildBottomBar(), BorderLayout.SOUTH);

        add(buildHeader(), BorderLayout.NORTH);
        add(body, BorderLayout.CENTER);

        Rectangle screen = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        int w = Math.min(1400, screen.width - 20);
        int h = Math.min(860, screen.height - 10);
        setMinimumSize(new Dimension(Math.min(1000, w), Math.min(600, h)));
        setSize(w, h);
        if (screen.height < 900) setExtendedState(MAXIMIZED_BOTH);
        setLocationRelativeTo(null);
    }

    // =====================================================================
    // UI construction
    // =====================================================================

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout(14, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setPaint(new GradientPaint(0, 0, HEAD_A, getWidth(), 0, HEAD_B));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        header.setBorder(BorderFactory.createEmptyBorder(9, 22, 9, 22));

        JLabel title = new JLabel("Threat Detection");
        title.setFont(ui(Font.BOLD, 20));
        title.setForeground(Color.WHITE);
        JLabel subtitle = new TLabel("Analyze URLs and messages for suspicious patterns", () -> HEAD_SUB);
        subtitle.setFont(ui(Font.PLAIN, 13));

        JPanel text = new JPanel(new GridLayout(2, 1, 0, 2));
        text.setOpaque(false);
        text.add(title);
        text.add(subtitle);

        header.add(new JLabel(new ShieldIcon(40)), BorderLayout.WEST);
        header.add(text, BorderLayout.CENTER);

        themeToggle = new ThemeToggle(this::toggleTheme);
        JPanel east = new JPanel(new GridBagLayout());
        east.setOpaque(false);
        east.add(themeToggle);
        header.add(east, BorderLayout.EAST);
        return header;
    }

    /** One slim card: [type][content][Add]   |   [Load CSV] file name. */
    private JPanel buildAddArea() {
        Card card = new Card("Add inputs", "A URL, a message or a CSV dataset. Hover Load CSV to see the expected columns");

        styleCombo(typeCombo, 125);
        addBtn.setToolTipText("Add to the table (or press Enter)");
        JPanel manualRow = new JPanel(new BorderLayout(8, 0));
        manualRow.setOpaque(false);
        manualRow.add(typeCombo, BorderLayout.WEST);
        manualRow.add(contentField, BorderLayout.CENTER);
        manualRow.add(addBtn, BorderLayout.EAST);

        csvInfo.setFont(ui(Font.PLAIN, 13));
        JPanel csvRow = new JPanel(new BorderLayout(12, 0));
        csvRow.setOpaque(false);
        csvRow.add(browseBtn, BorderLayout.WEST);
        csvRow.add(csvInfo, BorderLayout.CENTER);

        JPanel row = new JPanel(new GridBagLayout());
        row.setOpaque(false);
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.gridx = 0;
        gc.weightx = 0.68;
        gc.insets = new Insets(0, 0, 0, 10);
        row.add(manualRow, gc);
        gc.gridx = 1;
        gc.weightx = 0.32;
        gc.insets = new Insets(0, 10, 0, 0);
        row.add(csvRow, gc);
        card.body.add(row, BorderLayout.CENTER);

        addBtn.addActionListener(e -> addManualInput());
        contentField.addActionListener(e -> addManualInput());
        typeCombo.addActionListener(e -> contentField.setPlaceholder(typeCombo.getSelectedIndex() == 0
                ? "https://example.com/login" : "Type or paste a message to analyze..."));
        browseBtn.addActionListener(e -> browseCsv());
        return card;
    }

    /** The big table: inputs and results live here. */
    private JPanel buildResultsArea() {
        Card panel = new Card("Inputs & results", "Double-click a row for details. Press Delete to remove a row");

        JPanel chips = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        chips.setOpaque(false);
        chips.add(totalChip);
        chips.add(highChip);
        chips.add(mediumChip);
        chips.add(lowChip);
        chips.add(errorChip);
        panel.setHeaderRight(chips);

        JLabel filterLabel = new TLabel("Filter:", () -> MUTED);
        filterLabel.setFont(ui(Font.PLAIN, 13));
        styleCombo(filterCombo, 160);
        JPanel leftTools = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        leftTools.setOpaque(false);
        leftTools.add(filterLabel);
        leftTools.add(filterCombo);

        rowCount.setFont(ui(Font.PLAIN, 12));
        JPanel rightTools = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightTools.setOpaque(false);
        rightTools.add(rowCount);
        rightTools.add(removeBtn);
        rightTools.add(clearAllBtn);

        JPanel tools = new JPanel(new BorderLayout());
        tools.setOpaque(false);
        tools.add(leftTools, BorderLayout.WEST);
        tools.add(rightTools, BorderLayout.EAST);
        panel.body.add(tools, BorderLayout.NORTH);

        styleTable(resultTable);
        resultTable.setRowSorter(sorter);
        resultTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        int[] widths = { 45, 100, 270, 100, 150, 120, 250, 80, 110, 130 };
        for (int i = 0; i < widths.length; i++) {
            resultTable.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
        resultTable.getColumnModel().getColumn(COL_STATUS).setCellRenderer(new StatusRenderer());
        resultTable.getColumnModel().getColumn(COL_PROGRESS).setCellRenderer(new ProgressRenderer());
        resultTable.getColumnModel().getColumn(COL_RISK).setCellRenderer(new RiskRenderer());
        resultScroll = tableScroll(resultTable);
        panel.body.add(resultScroll, BorderLayout.CENTER);

        removeBtn.setToolTipText("Remove the selected row (Delete key)");
        filterCombo.addActionListener(e -> applyFilter());
        removeBtn.addActionListener(e -> removeSelectedRow());
        clearAllBtn.addActionListener(e -> clearAll());
        resultTable.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) showDetails();
            }
        });
        resultTable.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke("DELETE"), "removeRow");
        resultTable.getActionMap().put("removeRow", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                if (removeBtn.isEnabled()) removeSelectedRow();
            }
        });

        refreshSummary();
        return panel;
    }

    /** [Analyze all]  [=== overall progress ===]  "6 / 10 done"  Speed: [..] */
    private JPanel buildBottomBar() {
        Round bar = new Round(() -> CARD, () -> LINE, 18);
        bar.setLayout(new BorderLayout(16, 0));
        bar.setBorder(BorderFactory.createEmptyBorder(9, 14, 12, 16));

        analyzeButton.setFont(ui(Font.BOLD, 15));
        analyzeButton.setPreferredSize(new Dimension(150, 40));
        analyzeButton.setToolTipText("Analyze every row of the table, one after the other");
        analyzeButton.addActionListener(e -> startAnalysis());

        statusLabel.setFont(ui(Font.PLAIN, 12));
        JPanel middle = new JPanel(new GridBagLayout());
        middle.setOpaque(false);
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0;
        gc.weightx = 1;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.gridy = 0;
        middle.add(progressBar, gc);
        gc.gridy = 1;
        gc.insets = new Insets(2, 4, 0, 0);
        middle.add(statusLabel, gc);

        doneLabel.setFont(ui(Font.BOLD, 13));
        JLabel speedLabel = new TLabel("Speed:", () -> MUTED);
        speedLabel.setFont(ui(Font.PLAIN, 13));
        styleCombo(speedCombo, 140);
        speedCombo.setToolTipText("<html>The backend is instant (a few milliseconds).<br>"
                + "\"Step by step\" only adds a short pause between inputs so you can watch them one by one.</html>");
        JPanel east = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        east.setOpaque(false);
        east.add(doneLabel);
        east.add(speedLabel);
        east.add(speedCombo);

        bar.add(analyzeButton, BorderLayout.WEST);
        bar.add(middle, BorderLayout.CENTER);
        bar.add(east, BorderLayout.EAST);
        return bar;
    }

    // ---- small styling helpers ----

    private static void styleTable(JTable t) {
        t.setRowHeight(32);
        t.setShowGrid(false);
        t.setIntercellSpacing(new Dimension(0, 0));
        t.setFont(ui(Font.PLAIN, 13));
        t.setFillsViewportHeight(true);
        t.setDefaultRenderer(Object.class, new CellRenderer());
        t.setDefaultRenderer(Integer.class, new CellRenderer());
        JTableHeader th = t.getTableHeader();
        th.setReorderingAllowed(false);
        th.setDefaultRenderer(new HeaderRenderer());
        th.setPreferredSize(new Dimension(th.getPreferredSize().width, 32));
        applyTableColors(t);
    }

    /** Colors that depend on the theme (called again when the theme changes). */
    private static void applyTableColors(JTable t) {
        t.setForeground(INK);
        t.setBackground(TABLE_BG);
        t.setSelectionBackground(ROW_SEL);
        t.setSelectionForeground(INK);
        t.getTableHeader().setBackground(HEADER_ROW);
    }

    private static JScrollPane tableScroll(JTable t) {
        JScrollPane sp = new JScrollPane(t);
        sp.getVerticalScrollBar().setUI(new ScrollUI());
        sp.getHorizontalScrollBar().setUI(new ScrollUI());
        sp.getVerticalScrollBar().setPreferredSize(new Dimension(12, 0));
        sp.getHorizontalScrollBar().setPreferredSize(new Dimension(0, 12));
        sp.getVerticalScrollBar().setUnitIncrement(16);
        sp.setCorner(JScrollPane.UPPER_RIGHT_CORNER, new CornerFill());
        applyScrollColors(sp);
        return sp;
    }

    private static void applyScrollColors(JScrollPane sp) {
        sp.setBorder(BorderFactory.createLineBorder(LINE));
        sp.getViewport().setBackground(TABLE_BG);
        sp.setBackground(TABLE_BG);
    }

    private static void styleCombo(JComboBox<String> c, int width) {
        c.setUI(new ComboUI());
        c.setFont(ui(Font.PLAIN, 13));
        c.setBorder(BorderFactory.createLineBorder(FIELD_LINE));
        c.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        c.setPreferredSize(new Dimension(width, 36));
        c.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, false);
                setBorder(BorderFactory.createEmptyBorder(7, 10, 7, 10));
                setFont(ui(Font.PLAIN, 13));
                setForeground(INK);
                setBackground(isSelected ? ROW_SEL : FIELD_BG);
                return this;
            }
        });
    }

    // =====================================================================
    // Row helpers (EDT only)
    // =====================================================================

    private static boolean isError(AnalysisResult r) {
        return r.getStatus() == VerificationStatus.NOT_VERIFIED || r.getRisk() == RiskLevel.NOT_AVAILABLE;
    }

    /** Row state shown in the Status column: the backend could not verify it -> ERROR, otherwise DONE. */
    private static String statusOf(AnalysisResult r) {
        return isError(r) ? ERROR : DONE;
    }

    private static String riskOf(AnalysisResult r) {
        return isError(r) ? NOT_VERIFIED : r.getRisk().name();
    }

    private static String nullToDash(String s) {
        return (s == null || s.isEmpty()) ? "-" : s;
    }

    /** Appends a not-yet-analyzed row (no summary refresh, used for bulk loading). */
    private void addRawRow(Integer id, String type, String source, String content, String expRisk, String expCat) {
        resultModel.addRow(new Object[] { id, type, content, PENDING, 0, NO_RISK,
                "Waiting for analysis", source, expRisk, expCat });
        rowResults.add(null);
    }

    /**
     * Adds a manual input. The backend analyzes the manual inputs first and the CSV lines after,
     * so the new row goes right after the last manual row (the table order = the analysis order).
     */
    private void addPendingRow(String type, String source, String content) {
        filterCombo.setSelectedIndex(0); // make sure the new row is visible
        int pos = 0;
        while (pos < resultModel.getRowCount()
                && "MANUAL".equals(String.valueOf(resultModel.getValueAt(pos, COL_SOURCE)))) {
            pos++;
        }
        resultModel.insertRow(pos, new Object[] { null, type, content, PENDING, 0, NO_RISK,
                "Waiting for analysis", source, "-", "-" });
        rowResults.add(pos, null);
        renumberRows();
        refreshSummary();
        final int newRow = pos;
        SwingUtilities.invokeLater(() -> {
            int view = resultTable.convertRowIndexToView(newRow);
            if (view >= 0) resultTable.scrollRectToVisible(resultTable.getCellRect(view, 0, true));
        });
    }

    /** The ID column is a unique sequence 1, 2, 3... in table order (manual inputs, then CSV lines). */
    private void renumberRows() {
        for (int i = 0; i < resultModel.getRowCount(); i++) {
            resultModel.setValueAt(i + 1, i, COL_ID);
        }
    }

    private void resetRow(int row) {
        resultModel.setValueAt(PENDING, row, COL_STATUS);
        resultModel.setValueAt(0, row, COL_PROGRESS);
        resultModel.setValueAt(NO_RISK, row, COL_RISK);
        resultModel.setValueAt("Waiting for analysis", row, COL_CAUSE);
    }

    private void applyResult(int row, AnalysisResult r) {
        resultModel.setValueAt(statusOf(r), row, COL_STATUS);
        resultModel.setValueAt(r.getProgress(), row, COL_PROGRESS);
        resultModel.setValueAt(riskOf(r), row, COL_RISK);
        resultModel.setValueAt(r.getCause(), row, COL_CAUSE);
    }

    private void markRunning(int row, int delay) {
        resultModel.setValueAt(RUNNING, row, COL_STATUS);
        resultModel.setValueAt(0, row, COL_PROGRESS);
        resultModel.setValueAt("Analyzing...", row, COL_CAUSE);
        startRunAnimation(row, delay);
    }

    /** The backend only tells us "input k is finished", so the bar of the Running row is just animated. */
    private void startRunAnimation(int row, int delay) {
        stopRunAnimation();
        if (delay <= 0) return;
        runRow = row;
        runDelay = delay;
        runStart = System.currentTimeMillis();
        runTimer = new javax.swing.Timer(40, e -> {
            long elapsed = System.currentTimeMillis() - runStart;
            int v = (int) Math.min(92, 100 * elapsed / runDelay);
            if (runRow >= 0 && runRow < resultModel.getRowCount()) {
                resultModel.setValueAt(v, runRow, COL_PROGRESS);
            }
        });
        runTimer.start();
    }

    private void stopRunAnimation() {
        if (runTimer != null) runTimer.stop();
        runTimer = null;
        runRow = -1;
    }

    /** Puts every row back in a consistent state (used if the analysis fails). */
    private void restoreRows() {
        for (int i = 0; i < resultModel.getRowCount(); i++) {
            AnalysisResult r = rowResults.get(i);
            if (r == null) resetRow(i); else applyResult(i, r);
        }
    }

    // =====================================================================
    // Adding / removing rows (EDT only)
    // =====================================================================

    private void addManualInput() {
        String text = contentField.getText().trim();
        if (text.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Content is empty.", "Add input", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int id = nextManualId++;
        boolean isUrl = typeCombo.getSelectedIndex() == 0;
        if (isUrl) {
            manualList.addInput(new URL(id, text, detectUrlFormat(text)));
        } else {
            manualList.addInput(new Message(id, text));
        }
        addPendingRow(isUrl ? "URL" : "MESSAGE", "MANUAL", text);
        contentField.setText("");
        contentField.requestFocusInWindow();
    }

    /** Same rule the backend uses in CSVfile.determineURLFormat. */
    private static String detectUrlFormat(String url) {
        String u = url.toLowerCase();
        if (u.startsWith("https://")) return "HTTPS";
        if (u.startsWith("http://")) return "HTTP";
        return "UNKNOWN";
    }

    private void browseCsv() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("CSV files", "csv"));
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File f = chooser.getSelectedFile();
        if (!f.getName().toLowerCase().endsWith(".csv")) {
            JOptionPane.showMessageDialog(this, "Please select a valid .csv file.", "Invalid file",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        loadCsv(f);
    }

    /**
     * Reads the CSV right away (with a separate CSVfile object, the backend will read it again when
     * you press Analyze) so that every line of the file appears in the table as a Pending row.
     */
    private void loadCsv(File f) {
        CSVfile preview = new CSVfile(f.getName(), f.getAbsolutePath());
        List<CSVfile.CSVRecord> records;
        try {
            records = preview.readDataset();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Cannot read the CSV file:\n" + ex.getMessage(), "CSV error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (records.isEmpty()) {
            List<String> errs = new ArrayList<>(preview.getErrors());
            if (errs.isEmpty()) errs.add("No valid rows found in this file.");
            showCsvErrors(errs);
            return;
        }
        removeRowsBySource("CSV"); // only one CSV file at a time
        csvFile = f;
        csvRecords.clear();
        csvRecords.addAll(records);
        filterCombo.setSelectedIndex(0);
        for (CSVfile.CSVRecord rec : records) {
            input in = rec.getInput();
            addRawRow(null, (in instanceof URL) ? "URL" : "MESSAGE", "CSV", in.getContent(),
                    nullToDash(rec.getExpectedRiskLevel()), nullToDash(rec.getExpectedCategory()));
        }
        csvInfo.setText(f.getName() + "  -  " + records.size() + " rows");
        renumberRows();
        refreshSummary();
        if (!preview.getErrors().isEmpty()) {
            showCsvErrors(new ArrayList<>(preview.getErrors())); // lines the backend could not parse
        }
    }

    private void removeRowsBySource(String source) {
        for (int i = resultModel.getRowCount() - 1; i >= 0; i--) {
            if (source.equals(String.valueOf(resultModel.getValueAt(i, COL_SOURCE)))) {
                resultModel.removeRow(i);
                rowResults.remove(i);
            }
        }
        renumberRows();
        refreshSummary();
    }

    private void forgetCsv() {
        csvFile = null;
        csvRecords.clear();
        csvInfo.setText("No CSV loaded (optional)");
    }

    private void removeSelectedRow() {
        int viewRow = resultTable.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this, "Select a row first.", "Remove", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int m = resultTable.convertRowIndexToModel(viewRow);
        String source = String.valueOf(resultModel.getValueAt(m, COL_SOURCE));
        if (source.equals("CSV")) {
            // CSV lines can only be analyzed together (the backend reads the whole file)
            int ok = JOptionPane.showConfirmDialog(this,
                    "CSV rows come from the file and are analyzed together.\nRemove the whole CSV dataset?",
                    "Remove CSV", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (ok == JOptionPane.YES_OPTION) {
                removeRowsBySource("CSV");
                forgetCsv();
            }
            return;
        }
        if (source.equals("MANUAL")) {
            // the k-th MANUAL row of the table is the k-th input of manualList
            int k = m; // manual rows are at the top of the table, so the row index is the position
            List<input> inputs = manualList.getInputs();
            if (k < inputs.size()) manualList.removeInput(inputs.get(k).getID()); // so it is not analyzed again
        }
        resultModel.removeRow(m);
        rowResults.remove(m);
        renumberRows();
        refreshSummary();
    }

    private void clearAll() {
        if (resultModel.getRowCount() == 0 && csvFile == null) return;
        int ok = JOptionPane.showConfirmDialog(this, "Remove everything from the table?", "Clear all",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (ok != JOptionPane.YES_OPTION) return;
        for (input in : new ArrayList<>(manualList.getInputs())) {
            manualList.removeInput(in.getID());
        }
        forgetCsv();
        nextManualId = 1; // numbering starts again from 1
        resultModel.setRowCount(0);
        rowResults.clear();
        progressBar.setValue(0);
        statusLabel.setText("Ready.");
        refreshSummary();
    }

    /** Recomputes the chips, the "N rows" label and the "x / y done" label from the table content. */
    private void refreshSummary() {
        int total = resultModel.getRowCount();
        int high = 0, medium = 0, low = 0, notVerified = 0, pending = 0, finished = 0;
        for (int i = 0; i < total; i++) {
            String status = String.valueOf(resultModel.getValueAt(i, COL_STATUS));
            if (status.equals(PENDING)) pending++;
            else if (!status.equals(RUNNING)) finished++;
            switch (String.valueOf(resultModel.getValueAt(i, COL_RISK))) {
                case "HIGH": high++; break;
                case "MEDIUM": medium++; break;
                case "LOW": low++; break;
                case NOT_VERIFIED: notVerified++; break;
                default: break;
            }
        }
        totalChip.setValue(total);
        highChip.setValue(high);
        mediumChip.setValue(medium);
        lowChip.setValue(low);
        errorChip.setValue(notVerified);
        rowCount.setText(total + (total == 1 ? " row" : " rows") + (pending > 0 ? "  (" + pending + " pending)" : ""));
        doneLabel.setText(finished + " / " + total + " done");
    }

    /** One filter for both columns: HIGH/MEDIUM/LOW/NOT_VERIFIED (Risk) and PENDING/RUNNING/DONE/ERROR (Status). */
    private void applyFilter() {
        final String sel = (String) filterCombo.getSelectedItem();
        if (sel == null || sel.equals("All")) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(new RowFilter<DefaultTableModel, Integer>() {
                @Override
                public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> e) {
                    return sel.equals(e.getStringValue(COL_RISK)) || sel.equals(e.getStringValue(COL_STATUS));
                }
            });
        }
    }

    // =====================================================================
    // Light / dark mode
    // =====================================================================

    private void toggleTheme() {
        applyTheme(!darkMode);
    }

    private void applyTheme(boolean dark) {
        setTheme(dark);
        saveDarkPref(dark);
        applyDialogColors();
        getContentPane().setBackground(BG);
        applyTableColors(resultTable);
        applyScrollColors(resultScroll);
        styleCombo(typeCombo, 125);   // re-create the combo popups with the new colors
        styleCombo(filterCombo, 160);
        styleCombo(speedCombo, 140);
        themeToggle.refresh();
        repaint();
    }

    /** Dialogs (JOptionPane) and tooltips follow the theme too. */
    static void applyDialogColors() {
        UIManager.put("Panel.background", BG);
        UIManager.put("OptionPane.background", BG);
        UIManager.put("OptionPane.messageForeground", INK);
        UIManager.put("OptionPane.foreground", INK);
        UIManager.put("ToolTip.background", CARD);
        UIManager.put("ToolTip.foreground", INK);
        UIManager.put("ToolTip.border", BorderFactory.createLineBorder(FIELD_LINE));
    }

    private void setBusy(boolean busy) {
        boolean on = !busy;
        analyzeButton.setEnabled(on);
        addBtn.setEnabled(on);
        browseBtn.setEnabled(on);
        removeBtn.setEnabled(on);
        clearAllBtn.setEnabled(on);
        contentField.setEnabled(on);
        typeCombo.setEnabled(on);
        speedCombo.setEnabled(on);
        if (on) contentField.requestFocusInWindow();
    }

    // =====================================================================
    // Analysis (worker thread -> EDT)
    //
    // The backend (AnalysingSystem) is SEQUENTIAL: one single for-loop analyzes the inputs one after
    // the other (manual inputs first, then the CSV lines) and calls our ProgressListener after EACH
    // input is finished. We use those calls to move the table rows Pending -> Running -> Done/Error.
    // =====================================================================

    private void startAnalysis() {
        boolean hasCsv = csvFile != null;
        boolean hasManual = !manualList.isEmpty();

        if (!hasCsv && !hasManual) {
            JOptionPane.showMessageDialog(this, "Add at least one URL, message or CSV file first.",
                    "Nothing to analyze", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        CSVfile csv = null;
        if (hasCsv) {
            csv = new CSVfile(csvFile.getName(), csvFile.getAbsolutePath());
            if (!csv.exists()) {
                JOptionPane.showMessageDialog(this, "CSV file not found:\n" + csvFile.getAbsolutePath(),
                        "CSV error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        // the order used by the backend: manual inputs first, then the CSV lines
        List<input> order = new ArrayList<>(manualList.getInputs());
        for (CSVfile.CSVRecord rec : csvRecords) order.add(rec.getInput());
        List<Integer> rowOrder = new ArrayList<>(); // table row of each input, in that same order
        for (String src : new String[] { "MANUAL", "CSV" }) {
            for (int i = 0; i < resultModel.getRowCount(); i++) {
                if (src.equals(String.valueOf(resultModel.getValueAt(i, COL_SOURCE)))) rowOrder.add(i);
            }
        }
        boolean live = !order.isEmpty() && order.size() == rowOrder.size();

        // "Step by step" = short pause between inputs (only visual). Never longer than ~12 s in total.
        int delay = speedCombo.getSelectedIndex() == 0 ? Math.max(30, Math.min(450, 12000 / Math.max(1, order.size()))) : 0;

        setBusy(true);
        for (int i = 0; i < resultModel.getRowCount(); i++) resetRow(i);
        refreshSummary();
        progressBar.setValue(0);
        statusLabel.setText("Analyzing... 0 / " + order.size());
        if (live) markRunning(rowOrder.get(0), delay);

        new AnalysisWorker(hasManual, csv, order, rowOrder, live, delay).execute();
    }

    /** Result of one background run: the results plus any CSV parsing errors. */
    private static final class Outcome {
        final List<AnalysisResult> results;
        final List<String> csvErrors;
        Outcome(List<AnalysisResult> results, List<String> csvErrors) {
            this.results = results;
            this.csvErrors = csvErrors;
        }
    }

    /** One "input k is finished" event sent from the worker thread to the EDT. */
    private static final class Step {
        final int index;              // 0-based position of the finished input
        final AnalysisResult result;  // its result (null if unknown)
        final AnalysisProgress progress;
        Step(int index, AnalysisResult result, AnalysisProgress progress) {
            this.index = index;
            this.result = result;
            this.progress = progress;
        }
    }

    private final class AnalysisWorker extends SwingWorker<Outcome, Step> {
        private final boolean useManual;
        private final CSVfile csv;            // null if no CSV
        private final List<input> order;
        private final List<Integer> rowOrder;
        private final boolean live;
        private final int delay;

        AnalysisWorker(boolean useManual, CSVfile csv, List<input> order, List<Integer> rowOrder,
                boolean live, int delay) {
            this.useManual = useManual;
            this.csv = csv;
            this.order = order;
            this.rowOrder = rowOrder;
            this.live = live;
            this.delay = delay;
        }

        private void pause(int ms) {
            try {
                Thread.sleep(ms);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }

        /** Same backend code, applied to one single input, to show its risk as soon as it is finished. */
        private AnalysisResult analyzeOne(input in) {
            AnalysingList one = new AnalysingList();
            one.addInput(in);
            List<AnalysisResult> r = system.analyzeManualList(one);
            return r.isEmpty() ? null : r.get(0);
        }

        // Runs on a WORKER thread: no Swing calls here!
        @Override
        protected Outcome doInBackground() throws Exception {
            // called by the backend after EACH input is finished
            AnalysingSystem.ProgressListener listener = p -> {
                int k = p.getCompleted() - 1;
                AnalysisResult one = (live && k >= 0 && k < order.size()) ? analyzeOne(order.get(k)) : null;
                publish(new Step(k, one, p));
                if (delay > 0 && p.getCompleted() < p.getTotal()) pause(delay);
            };

            if (delay > 0) pause(delay); // the first row stays "Running" for a moment

            List<AnalysisResult> results;
            if (useManual && csv != null) {
                results = system.analyzeEverything(manualList, csv, listener);
            } else if (csv != null) {
                results = system.analyzeCSV(csv, listener);
            } else {
                results = system.analyzeManualList(manualList, listener);
            }
            List<String> errors = (csv != null) ? new ArrayList<>(system.getCSVParsingErrors(csv))
                                                : new ArrayList<String>();
            return new Outcome(results, errors);
        }

        // Runs on the EDT
        @Override
        protected void process(List<Step> steps) {
            for (Step st : steps) {
                if (live && st.index >= 0 && st.index < rowOrder.size()) {
                    stopRunAnimation();
                    if (st.result != null) applyResult(rowOrder.get(st.index), st.result);
                    int next = st.index + 1;
                    if (next < rowOrder.size()) markRunning(rowOrder.get(next), delay);
                }
                AnalysisProgress p = st.progress;
                progressBar.setValue(p.getPercentage());
                statusLabel.setText("Analyzing... " + p.getCompleted() + " / " + p.getTotal()
                        + "   (ID " + p.getCurrentID() + ", " + p.getSource() + ")");
            }
            refreshSummary();
        }

        // Runs on the EDT
        @Override
        protected void done() {
            stopRunAnimation();
            setBusy(false);
            try {
                Outcome out = get();
                showResults(out.results); // the official results from the backend replace the live ones
                progressBar.setValue(100);
                String msg = "Done. " + out.results.size() + " input(s) analyzed.";
                if (!out.csvErrors.isEmpty()) msg += "  " + out.csvErrors.size() + " CSV line(s) skipped.";
                statusLabel.setText(msg);
            } catch (ExecutionException ex) {
                Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                restoreRows();
                refreshSummary();
                statusLabel.setText("Analysis failed.");
                JOptionPane.showMessageDialog(MainFrame.this, cause.getClass().getSimpleName() + ": "
                        + cause.getMessage(), "Analysis error", JOptionPane.ERROR_MESSAGE);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
    }

    // =====================================================================
    // Results display (EDT only)
    // =====================================================================

    /** Replaces the table content with the results (they contain every input that was analyzed). */
    private void showResults(List<AnalysisResult> results) {
        resultModel.setRowCount(0);
        rowResults.clear();
        int n = 1;
        for (AnalysisResult r : results) {
            resultModel.addRow(new Object[] {
                    n++, r.getType(), r.getContent(), statusOf(r), r.getProgress(), riskOf(r),
                    r.getCause(), String.valueOf(r.getSource()),
                    nullToDash(r.getExpectedRiskLevel()), nullToDash(r.getExpectedCategory()) });
            rowResults.add(r);
        }
        refreshSummary();
        applyFilter();
    }

    private void showDetails() {
        int viewRow = resultTable.getSelectedRow();
        if (viewRow < 0) return;
        int modelRow = resultTable.convertRowIndexToModel(viewRow);
        AnalysisResult r = rowResults.get(modelRow);
        if (r == null) {
            JOptionPane.showMessageDialog(this, "This row has not been analyzed yet.\nPress Analyze all first.",
                    "Result details", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String text = "#: " + resultModel.getValueAt(modelRow, COL_ID)
                + "\nOriginal ID (backend / CSV file): " + r.getID()
                + "\nType: " + r.getType()
                + "\nSource: " + r.getSource()
                + "\nStatus: " + statusOf(r) + "  (backend: " + r.getStatus() + ")"
                + "\nRisk: " + r.getRisk()
                + "\nProgress: " + r.getProgress() + "%"
                + "\n\nContent:\n" + r.getContent()
                + "\n\nCause:\n" + r.getCause()
                + "\n\nExpected risk: " + nullToDash(r.getExpectedRiskLevel())
                + "\nExpected category: " + nullToDash(r.getExpectedCategory())
                + "\nTriggered rules (dataset): " + nullToDash(r.getTriggeredRulesDescription());
        JTextArea area = new JTextArea(text, 16, 55);
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setFont(ui(Font.PLAIN, 13));
        area.setBackground(FIELD_BG);
        area.setForeground(INK);
        area.setMargin(new Insets(10, 12, 10, 12));
        JOptionPane.showMessageDialog(this, new JScrollPane(area), "Result details", JOptionPane.PLAIN_MESSAGE);
    }

    private void showCsvErrors(List<String> errors) {
        StringBuilder sb = new StringBuilder();
        for (String e : errors) sb.append(e).append('\n');
        JTextArea area = new JTextArea(sb.toString(), 12, 55);
        area.setEditable(false);
        area.setFont(ui(Font.PLAIN, 13));
        area.setBackground(FIELD_BG);
        area.setForeground(INK);
        JOptionPane.showMessageDialog(this, new JScrollPane(area), "CSV parsing errors (" + errors.size() + ")",
                JOptionPane.WARNING_MESSAGE);
    }

    // =====================================================================
    // Risk colors (one place for the table pills AND the summary chips)
    // =====================================================================

    static Color riskBg(String risk) {
        if (risk.equals(RiskLevel.HIGH.name())) return darkMode ? new Color(0x451A1F) : new Color(0xFEE2E2);
        if (risk.equals(RiskLevel.MEDIUM.name())) return darkMode ? new Color(0x422C0A) : new Color(0xFEF3C7);
        if (risk.equals(RiskLevel.LOW.name())) return darkMode ? new Color(0x0F3A25) : new Color(0xDCFCE7);
        return darkMode ? new Color(0x1E293B) : new Color(0xE2E8F0);
    }

    static Color riskFg(String risk) {
        if (risk.equals(RiskLevel.HIGH.name())) return darkMode ? new Color(0xFCA5A5) : new Color(0xB91C1C);
        if (risk.equals(RiskLevel.MEDIUM.name())) return darkMode ? new Color(0xFCD34D) : new Color(0xB45309);
        if (risk.equals(RiskLevel.LOW.name())) return darkMode ? new Color(0x86EFAC) : new Color(0x15803D);
        return darkMode ? new Color(0x94A3B8) : new Color(0x475569);
    }

    static void smooth(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
    }

    /** Panel with rounded corners; colors are read at paint time so the theme can change. */
    static class Round extends JPanel {
        private final Supplier<Color> fill, line;
        private final int arc;

        Round(Supplier<Color> fill, Supplier<Color> line, int arc) {
            this.fill = fill;
            this.line = line;
            this.arc = arc;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            smooth(g2);
            int w = getWidth(), h = getHeight();
            g2.setColor(SHADOW); // soft shadow
            g2.fillRoundRect(0, 2, w - 1, h - 2, arc, arc);
            g2.setColor(fill.get());
            g2.fillRoundRect(0, 0, w - 1, h - 3, arc, arc);
            if (line != null) {
                g2.setColor(line.get());
                g2.drawRoundRect(0, 0, w - 1, h - 3, arc, arc);
            }
            g2.dispose();
        }
    }

    /** White card with a title, a subtitle and a body area. */
    static class Card extends Round {
        final JPanel body = new JPanel(new BorderLayout(0, 8));
        private final JPanel header = new JPanel(new BorderLayout());

        Card(String title, String subtitle) {
            super(() -> CARD, () -> LINE, 18);
            setLayout(new BorderLayout(0, 10));
            setBorder(BorderFactory.createEmptyBorder(12, 16, 16, 16));
            body.setOpaque(false);
            header.setOpaque(false);

            JLabel t = new TLabel(title, () -> INK);
            t.setFont(ui(Font.BOLD, 15));
            JLabel s = new TLabel(subtitle, () -> MUTED);
            s.setFont(ui(Font.PLAIN, 12));
            JPanel texts = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
            texts.setOpaque(false);
            texts.add(t);
            texts.add(s);
            header.add(texts, BorderLayout.WEST);

            add(header, BorderLayout.NORTH);
            add(body, BorderLayout.CENTER);
        }

        void setHeaderRight(Component c) {
            header.add(c, BorderLayout.EAST);
        }
    }

    /** Small colored badge: "12 High". */
    static class Chip extends Round {
        private final TLabel num;

        Chip(String label, Supplier<Color> bg, Supplier<Color> fg) {
            super(bg, null, 16);
            setLayout(new FlowLayout(FlowLayout.CENTER, 6, 4));
            num = new TLabel("0", fg);
            num.setFont(ui(Font.BOLD, 14));
            TLabel lab = new TLabel(label, fg);
            lab.setFont(ui(Font.PLAIN, 12));
            add(num);
            add(lab);
        }

        void setValue(int v) {
            num.setText(String.valueOf(v));
        }
    }

    /** Rounded button with hover / pressed states. FILLED = green, OUTLINE = white with border, ALERT = red text. */
    static class RoundedButton extends JButton {
        enum Kind { FILLED, OUTLINE, ALERT }

        private final Kind kind;
        private boolean over, down;

        RoundedButton(String text, Kind kind) {
            super(text);
            this.kind = kind;
            setFont(ui(Font.BOLD, 13));
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(BorderFactory.createEmptyBorder(7, 16, 7, 16));
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { over = true; repaint(); }
                @Override public void mouseExited(MouseEvent e) { over = false; down = false; repaint(); }
                @Override public void mousePressed(MouseEvent e) { down = true; repaint(); }
                @Override public void mouseReleased(MouseEvent e) { down = false; repaint(); }
            });
        }

        private Color bg() { return kind == Kind.FILLED ? PRIMARY : CARD; }
        private Color hover() {
            return kind == Kind.FILLED ? PRIMARY_HOVER : kind == Kind.ALERT ? DANGER_HOVER : GHOST_HOVER;
        }
        private Color line() { return kind == Kind.FILLED ? null : FIELD_LINE; }

        @Override
        public Color getForeground() {
            return kind == Kind.FILLED ? Color.WHITE : kind == Kind.ALERT ? DANGER : INK;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            smooth(g2);
            Color c = !isEnabled() ? DISABLED_BG : down ? hover().darker() : over ? hover() : bg();
            g2.setColor(c);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
            if (line() != null) {
                g2.setColor(isEnabled() ? line() : DISABLED_BG);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /** Text field with rounded border, focus highlight and placeholder text. */
    static class Field extends JTextField {
        private String placeholder;

        Field(String placeholder) {
            this.placeholder = placeholder;
            setOpaque(false);
            setFont(ui(Font.PLAIN, 13));
            setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
            addFocusListener(new FocusAdapter() {
                @Override public void focusGained(FocusEvent e) { repaint(); }
                @Override public void focusLost(FocusEvent e) { repaint(); }
            });
        }

        @Override public Color getForeground() { return isEnabled() ? INK : MUTED; }
        @Override public Color getCaretColor() { return PRIMARY; }
        @Override public Color getDisabledTextColor() { return MUTED; }

        void setPlaceholder(String p) {
            placeholder = p;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            smooth(g2);
            boolean focus = hasFocus();
            g2.setColor(FIELD_BG);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
            g2.setColor(focus ? PRIMARY : FIELD_LINE);
            g2.setStroke(new BasicStroke(focus ? 2f : 1f));
            int i = focus ? 1 : 0;
            g2.drawRoundRect(i, i, getWidth() - 1 - 2 * i, getHeight() - 1 - 2 * i, 12, 12);
            if (getText().isEmpty() && placeholder != null) {
                g2.setColor(PLACEHOLDER);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(placeholder, getInsets().left, y);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /** Progress bar with rounded track and a blue gradient fill. */
    static class ModernProgress extends JProgressBar {
        ModernProgress() {
            super(0, 100);
            setStringPainted(true);
            setBorderPainted(false);
            setOpaque(false);
            setFont(ui(Font.BOLD, 11));
            setPreferredSize(new Dimension(100, 24));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            smooth(g2);
            int w = getWidth(), h = getHeight();
            g2.setColor(LINE);
            g2.fillRoundRect(0, 0, w, h, h, h);
            int fw = (int) Math.round(w * getPercentComplete());
            if (fw > 0) {
                g2.setClip(new RoundRectangle2D.Float(0, 0, w, h, h, h));
                g2.setPaint(new GradientPaint(0, 0, PRIMARY, w, 0, new Color(0x38BDF8)));
                g2.fillRect(0, 0, fw, h);
                g2.setClip(null);
            }
            String s = getValue() + "%";
            g2.setFont(getFont());
            FontMetrics fm = g2.getFontMetrics();
            g2.setColor(fw > w / 2 ? Color.WHITE : INK);
            g2.drawString(s, (w - fm.stringWidth(s)) / 2, (h - fm.getHeight()) / 2 + fm.getAscent());
            g2.dispose();
        }
    }

    /** Table that shows a friendly message when it has no rows. */
    static class EmptyTable extends JTable {
        private final String emptyText;

        EmptyTable(TableModel model, String emptyText) {
            super(model);
            this.emptyText = emptyText;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (getRowCount() == 0) {
                Graphics2D g2 = (Graphics2D) g.create();
                smooth(g2);
                g2.setFont(ui(Font.PLAIN, 13));
                g2.setColor(PLACEHOLDER);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(emptyText, (getWidth() - fm.stringWidth(emptyText)) / 2,
                        Math.max(fm.getHeight(), getHeight() / 2));
                g2.dispose();
            }
        }
    }

    /** Combo box with a flat look and a custom chevron arrow. */
    static class ComboUI extends BasicComboBoxUI {
        @Override
        protected JButton createArrowButton() {
            JButton b = new JButton() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    smooth(g2);
                    g2.setColor(MUTED);
                    g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    int cx = getWidth() / 2, cy = getHeight() / 2;
                    g2.drawLine(cx - 4, cy - 2, cx, cy + 2);
                    g2.drawLine(cx, cy + 2, cx + 4, cy - 2);
                    g2.dispose();
                }
            };
            b.setBorder(BorderFactory.createEmptyBorder());
            b.setContentAreaFilled(false);
            b.setFocusPainted(false);
            b.setOpaque(false);
            return b;
        }

        @Override
        public void paintCurrentValue(Graphics g, Rectangle bounds, boolean hasFocus) {
            super.paintCurrentValue(g, bounds, false); // no Windows-blue highlight when the combo has focus
        }

        @Override
        public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus) {
            g.setColor(FIELD_BG);
            g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
        }

        @Override
        protected ComboPopup createPopup() {
            BasicComboPopup popup = new BasicComboPopup(comboBox);
            popup.setBorder(BorderFactory.createLineBorder(FIELD_LINE));
            return popup;
        }
    }

    /** Label whose color is read when painting, so it follows the theme. */
    static class TLabel extends JLabel {
        private final Supplier<Color> fg;

        TLabel(String text, Supplier<Color> fg) {
            super(text);
            this.fg = fg;
        }

        @Override
        public Color getForeground() {
            return fg == null ? super.getForeground() : fg.get();
        }
    }

    /** Combo box that follows the theme. */
    static class Combo extends JComboBox<String> {
        Combo(String[] items) {
            super(items);
        }

        @Override public Color getBackground() { return FIELD_BG; }
        @Override public Color getForeground() { return INK; }
    }

    /** Sun / moon switch shown in the header. */
    static class ThemeToggle extends JComponent {
        private final Runnable onClick;

        ThemeToggle(Runnable onClick) {
            this.onClick = onClick;
            setPreferredSize(new Dimension(64, 30));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            refresh();
            addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) { ThemeToggle.this.onClick.run(); }
            });
        }

        void refresh() {
            setToolTipText(darkMode ? "Switch to light mode" : "Switch to dark mode");
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            smooth(g2);
            int w = getWidth(), h = getHeight();
            g2.setColor(new Color(255, 255, 255, darkMode ? 38 : 56));
            g2.fillRoundRect(0, 0, w - 1, h - 1, h, h);
            g2.setColor(new Color(255, 255, 255, 110));
            g2.drawRoundRect(0, 0, w - 1, h - 1, h, h);

            int d = h - 6;
            int x = darkMode ? w - d - 3 : 3;
            g2.setColor(darkMode ? new Color(0xE2E8F0) : Color.WHITE);
            g2.fillOval(x, 3, d, d);
            double cx = x + d / 2.0, cy = 3 + d / 2.0;
            if (darkMode) { // moon
                double r = d * 0.30;
                Area moon = new Area(new Ellipse2D.Double(cx - r, cy - r, 2 * r, 2 * r));
                moon.subtract(new Area(new Ellipse2D.Double(cx - r + r * 0.75, cy - r - r * 0.2, 2 * r * 0.85, 2 * r * 0.85)));
                g2.setColor(new Color(0x334155));
                g2.fill(moon);
            } else {        // sun
                g2.setColor(new Color(0xF59E0B));
                double r = d * 0.20;
                g2.fill(new Ellipse2D.Double(cx - r, cy - r, 2 * r, 2 * r));
                g2.setStroke(new BasicStroke(1.7f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                for (int i = 0; i < 8; i++) {
                    double a = i * Math.PI / 4, r1 = d * 0.32, r2 = d * 0.44;
                    g2.draw(new java.awt.geom.Line2D.Double(cx + Math.cos(a) * r1, cy + Math.sin(a) * r1,
                            cx + Math.cos(a) * r2, cy + Math.sin(a) * r2));
                }
            }
            g2.dispose();
        }
    }

    /** Thin rounded scrollbar (no arrows) that follows the theme. */
    static class ScrollUI extends BasicScrollBarUI {
        @Override protected void configureScrollBarColors() { }

        @Override protected JButton createDecreaseButton(int orientation) { return zero(); }
        @Override protected JButton createIncreaseButton(int orientation) { return zero(); }

        private static JButton zero() {
            JButton b = new JButton();
            Dimension d = new Dimension(0, 0);
            b.setPreferredSize(d);
            b.setMinimumSize(d);
            b.setMaximumSize(d);
            return b;
        }

        @Override
        protected void paintTrack(Graphics g, JComponent c, Rectangle r) {
            g.setColor(TABLE_BG);
            g.fillRect(r.x, r.y, r.width, r.height);
        }

        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
            if (r.width <= 0 || r.height <= 0) return;
            Graphics2D g2 = (Graphics2D) g.create();
            smooth(g2);
            g2.setColor(isDragging ? MUTED : SCROLL_THUMB);
            g2.fillRoundRect(r.x + 2, r.y + 2, r.width - 4, r.height - 4, 8, 8);
            g2.dispose();
        }
    }

    /** Fills the little square above the vertical scrollbar (next to the table header). */
    static class CornerFill extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            g.setColor(HEADER_ROW);
            g.fillRect(0, 0, getWidth(), getHeight());
        }
    }

    /** Shield logo drawn with Java2D (no image file needed). */
    static class ShieldIcon implements Icon {
        private final int s;

        ShieldIcon(int size) {
            this.s = size;
        }

        @Override public int getIconWidth() { return s; }
        @Override public int getIconHeight() { return s; }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            smooth(g2);
            g2.translate(x, y);
            Path2D p = new Path2D.Float();
            p.moveTo(.50f * s, .05f * s);
            p.lineTo(.88f * s, .20f * s);
            p.lineTo(.88f * s, .50f * s);
            p.curveTo(.88f * s, .75f * s, .68f * s, .90f * s, .50f * s, .97f * s);
            p.curveTo(.32f * s, .90f * s, .12f * s, .75f * s, .12f * s, .50f * s);
            p.lineTo(.12f * s, .20f * s);
            p.closePath();
            g2.setPaint(new GradientPaint(0, 0, Color.WHITE, 0, s, new Color(0xBFDBFE)));
            g2.fill(p);
            g2.setColor(PRIMARY);
            g2.setStroke(new BasicStroke(s * 0.09f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            Path2D check = new Path2D.Float();
            check.moveTo(.32f * s, .50f * s);
            check.lineTo(.45f * s, .64f * s);
            check.lineTo(.69f * s, .36f * s);
            g2.draw(check);
            g2.dispose();
        }

        /** Window / taskbar icon used when /resources/analysis.png is missing. */
        Image toImage() {
            BufferedImage img = new BufferedImage(s, s, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = img.createGraphics();
            smooth(g);
            g.setColor(new Color(0x1D4ED8));
            g.fillRoundRect(0, 0, s, s, s / 4, s / 4);
            paintIcon(null, g, 0, 0);
            g.dispose();
            return img;
        }
    }

    // =====================================================================
    // Table renderers
    // =====================================================================

    static class HeaderRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int row, int col) {
            super.getTableCellRendererComponent(t, v, false, false, row, col);
            setText(v == null ? "" : v.toString().toUpperCase());
            setBackground(HEADER_ROW);
            setForeground(MUTED);
            setFont(ui(Font.BOLD, 11));
            setHorizontalAlignment(col == 0 ? SwingConstants.CENTER : SwingConstants.LEFT);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, LINE),
                    BorderFactory.createEmptyBorder(0, 10, 0, 10)));
            return this;
        }
    }

    /** Zebra rows, padding and tooltips for every normal cell. */
    static class CellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int row, int col) {
            super.getTableCellRendererComponent(t, v, sel, false, row, col);
            setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
            setHorizontalAlignment(col == 0 ? SwingConstants.CENTER : SwingConstants.LEFT);
            setForeground(INK);
            setBackground(sel ? ROW_SEL : (row % 2 == 0 ? TABLE_BG : ROW_ALT));
            setToolTipText(v == null ? null : String.valueOf(v));
            return this;
        }
    }

    /** Status column: Pending (gray) / Running (blue) / Done (green) / Error (red), with a colored dot. */
    static class StatusRenderer extends CellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int row, int col) {
            super.getTableCellRendererComponent(t, v, sel, foc, row, col);
            String s = String.valueOf(v);
            setText("\u25CF  " + s.charAt(0) + s.substring(1).toLowerCase());
            setForeground(s.equals("DONE") ? OK
                    : s.equals("RUNNING") ? INFO
                    : s.equals("ERROR") ? DANGER : MUTED);
            setFont(ui(Font.BOLD, 12));
            return this;
        }
    }

    /** Progress column: a small bar per row (0..100). */
    static class ProgressRenderer extends JComponent implements TableCellRenderer {
        private int value;
        private Color rowBg = Color.WHITE;

        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int row, int col) {
            value = (v instanceof Integer) ? (Integer) v : 0;
            rowBg = sel ? ROW_SEL : (row % 2 == 0 ? TABLE_BG : ROW_ALT);
            return this;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            smooth(g2);
            g2.setColor(rowBg);
            g2.fillRect(0, 0, getWidth(), getHeight());
            int pad = 12, h = 8, w = getWidth() - 2 * pad, y = (getHeight() - h) / 2;
            if (w > 0) {
                g2.setColor(LINE);
                g2.fillRoundRect(pad, y, w, h, h, h);
                int fw = w * Math.max(0, Math.min(100, value)) / 100;
                if (fw > 0) {
                    g2.setColor(INFO);
                    g2.fillRoundRect(pad, y, Math.max(fw, h), h, h, h);
                }
            }
            g2.dispose();
        }
    }

    static String riskLabel(String risk) {
        switch (risk) {
            case "HIGH": return "High";
            case "MEDIUM": return "Medium";
            case "LOW": return "Low";
            case NOT_VERIFIED: return "Not Verified";
            default: return "...";
        }
    }

    /** Risk column: colored rounded pill. Low = green, Medium = yellow, High = red, Not Verified = gray. */
    static class RiskRenderer extends DefaultTableCellRenderer {
        private Color rowBg = Color.WHITE;
        private Color pillBg = Color.WHITE;

        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int row, int col) {
            super.getTableCellRendererComponent(t, v, sel, false, row, col);
            String risk = String.valueOf(v);
            rowBg = sel ? ROW_SEL : (row % 2 == 0 ? TABLE_BG : ROW_ALT);
            pillBg = riskBg(risk);
            setText(riskLabel(risk));
            setForeground(riskFg(risk));
            setFont(ui(Font.BOLD, 11));
            setHorizontalAlignment(SwingConstants.CENTER);
            setBorder(BorderFactory.createEmptyBorder());
            setOpaque(false);
            return this;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            smooth(g2);
            g2.setColor(rowBg);
            g2.fillRect(0, 0, getWidth(), getHeight());
            FontMetrics fm = g2.getFontMetrics(getFont());
            int pw = Math.min(getWidth() - 8, fm.stringWidth(getText()) + 28), ph = 22;
            g2.setColor(pillBg);
            g2.fillRoundRect((getWidth() - pw) / 2, (getHeight() - ph) / 2, pw, ph, ph, ph);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    public static void main(String[] args) {
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) { }
        applyDialogColors();
        UIManager.put("OptionPane.messageFont", ui(Font.PLAIN, 13));
        UIManager.put("OptionPane.buttonFont", ui(Font.PLAIN, 13));
        UIManager.put("ToolTip.font", ui(Font.PLAIN, 12));
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
}
