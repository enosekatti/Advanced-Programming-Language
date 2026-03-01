import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

/**
 * GUI for Employee Schedule Manager (Java Swing).
 * Input: employee names and shift preferences per day (entered by user).
 * Output: final weekly schedule in a readable format.
 */
public class ScheduleManagerGUI extends JFrame {

    private static final String[] DAYS = {
        "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"
    };
    private static final String[] SHIFTS = { "morning", "afternoon", "evening" };

    private final DefaultTableModel tableModel;
    private final JTable employeeTable;
    private final JTextArea outputArea;
    private final JLabel statusLabel;
    private final java.util.List<ScheduleManager.Employee> employees = new ArrayList<>();

    public ScheduleManagerGUI() {
        setTitle("Employee Schedule Manager");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(820, 620);
        setLocationRelativeTo(null);

        JPanel main = new JPanel(new BorderLayout(10, 10));
        main.setBorder(new EmptyBorder(12, 12, 12, 12));

        // Top: buttons
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Add employees and set their shift preferences, then generate the schedule."));
        JButton addBtn = new JButton("Add employee");
        addBtn.addActionListener(e -> openAddEmployeeDialog());
        top.add(addBtn);
        JButton removeBtn = new JButton("Remove selected");
        removeBtn.addActionListener(e -> removeSelected());
        top.add(removeBtn);
        JButton sampleBtn = new JButton("Load sample data");
        sampleBtn.addActionListener(e -> loadSampleData());
        top.add(sampleBtn);
        JButton clearBtn = new JButton("Clear all");
        clearBtn.addActionListener(e -> clearAll());
        top.add(clearBtn);
        JButton generateBtn = new JButton("Generate schedule");
        generateBtn.addActionListener(e -> generateSchedule());
        top.add(generateBtn);
        statusLabel = new JLabel(" ");
        top.add(statusLabel);
        main.add(top, BorderLayout.NORTH);

        // Center: employee list (name + summary) and output
        JPanel center = new JPanel(new BorderLayout(5, 5));
        String[] cols = { "Name", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun" };
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        employeeTable = new JTable(tableModel);
        employeeTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        employeeTable.getTableHeader().setReorderingAllowed(false);
        employeeTable.setRowHeight(22);
        center.add(new JScrollPane(employeeTable), BorderLayout.CENTER);
        JPanel centerSouth = new JPanel(new BorderLayout());
        centerSouth.setBorder(BorderFactory.createTitledBorder("Final schedule"));
        outputArea = new JTextArea(16, 72);
        outputArea.setEditable(false);
        outputArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        outputArea.setLineWrap(false);
        outputArea.setText("Add employees (name + preferred shift per day), then click 'Generate schedule'.");
        centerSouth.add(new JScrollPane(outputArea), BorderLayout.CENTER);
        center.add(centerSouth, BorderLayout.SOUTH);
        main.add(center, BorderLayout.CENTER);

        setContentPane(main);
    }

    private void openAddEmployeeDialog() {
        AddEmployeeDialog dlg = new AddEmployeeDialog(this);
        dlg.setVisible(true);
        if (dlg.getResult() != null) {
            addEmployeeToTable(dlg.getResult());
        }
    }

    private void addEmployeeToTable(ScheduleManager.Employee emp) {
        employees.add(emp);
        String[] prefs = new String[7];
        for (int d = 0; d < 7; d++) {
            String p = emp.preferences != null && emp.preferences.containsKey(d)
                ? emp.preferences.get(d) : "-";
            prefs[d] = p.substring(0, 1).toUpperCase();
        }
        tableModel.addRow(new Object[] {
            emp.name, prefs[0], prefs[1], prefs[2], prefs[3], prefs[4], prefs[5], prefs[6]
        });
    }

    private void removeSelected() {
        int row = getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select an employee in the table to remove.");
            return;
        }
        employees.remove(row);
        tableModel.removeRow(row);
    }

    private void loadSampleData() {
        java.util.List<ScheduleManager.Employee> sample = ScheduleManager.collectInput();
        for (ScheduleManager.Employee e : sample) {
            if (!employees.stream().anyMatch(emp -> emp.name.equals(e.name))) {
                addEmployeeToTable(e);
            }
        }
        JOptionPane.showMessageDialog(this, "Sample employees added. You can add more or generate the schedule.");
    }

    private void clearAll() {
        int n = employees.size();
        employees.clear();
        for (int i = n - 1; i >= 0; i--) tableModel.removeRow(i);
    }

    private int getSelectedRow() {
        int r = employeeTable.getSelectedRow();
        return r >= 0 && r < employees.size() ? r : -1;
    }

    private void generateSchedule() {
        if (employees.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Add at least one employee first.");
            return;
        }
        statusLabel.setText("Generating...");
        outputArea.setText("Generating schedule...");
        repaint();

        SwingWorker<String, Void> worker = new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() {
                return ScheduleManager.runAndGetScheduleOutput(employees);
            }
            @Override
            protected void done() {
                try {
                    String result = get();
                    outputArea.setText(result);
                    statusLabel.setText("Done.");
                } catch (Exception ex) {
                    outputArea.setText("Error: " + ex.getMessage());
                    statusLabel.setText("Error.");
                }
            }
        };
        worker.execute();
    }

    public static void launch() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}
        EventQueue.invokeLater(() -> {
            ScheduleManagerGUI gui = new ScheduleManagerGUI();
            gui.setVisible(true);
        });
    }

    public static void main(String[] args) {
        launch();
    }

    // ---- Add Employee Dialog ----
    private static class AddEmployeeDialog extends JDialog {
        private final JTextField nameField;
        private final JComboBox<String>[][] dayCombos; // [day][0=1st, 1=2nd, 2=3rd]
        private ScheduleManager.Employee result;

        AddEmployeeDialog(Frame parent) {
            super(parent, "Add employee", true);
            setSize(520, 420);
            setLocationRelativeTo(parent);
            JPanel p = new JPanel(new BorderLayout(10, 10));
            p.setBorder(new EmptyBorder(12, 12, 12, 12));

            JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
            top.add(new JLabel("Name:"));
            nameField = new JTextField(20);
            top.add(nameField);
            p.add(top, BorderLayout.NORTH);

            JPanel grid = new JPanel(new GridLayout(8, 4, 4, 4));
            grid.add(new JLabel("Day"));
            grid.add(new JLabel("1st"));
            grid.add(new JLabel("2nd"));
            grid.add(new JLabel("3rd"));
            dayCombos = new JComboBox[7][3];
            for (int day = 0; day < 7; day++) {
                grid.add(new JLabel(DAYS[day]));
                for (int c = 0; c < 3; c++) {
                    JComboBox<String> cb = new JComboBox<>(SHIFTS);
                    cb.setSelectedIndex(c);
                    dayCombos[day][c] = cb;
                    grid.add(cb);
                }
            }
            p.add(grid, BorderLayout.CENTER);

            JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JButton ok = new JButton("Save");
            ok.addActionListener(e -> onSave());
            JButton cancel = new JButton("Cancel");
            cancel.addActionListener(e -> dispose());
            buttons.add(ok);
            buttons.add(cancel);
            p.add(buttons, BorderLayout.SOUTH);

            setContentPane(p);
        }

        private void onSave() {
            String name = nameField.getText().trim();
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter employee name.");
                return;
            }
            Map<Integer, String> preferences = new HashMap<>();
            Map<Integer, java.util.List<String>> priority = new HashMap<>();
            for (int day = 0; day < 7; day++) {
                String first = (String) dayCombos[day][0].getSelectedItem();
                String second = (String) dayCombos[day][1].getSelectedItem();
                String third = (String) dayCombos[day][2].getSelectedItem();
                if (first.equals(second) || first.equals(third) || second.equals(third)) {
                    JOptionPane.showMessageDialog(this, "1st, 2nd, and 3rd choice must be different for each day.");
                    return;
                }
                preferences.put(day, first);
                java.util.List<String> list = new ArrayList<>();
                list.add(first);
                list.add(second);
                list.add(third);
                priority.put(day, list);
            }
            result = new ScheduleManager.Employee(name, preferences, priority);
            dispose();
        }

        ScheduleManager.Employee getResult() {
            return result;
        }
    }
}
