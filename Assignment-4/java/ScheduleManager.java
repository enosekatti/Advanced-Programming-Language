import java.util.*;

/**
 * Employee Schedule Manager - Java Implementation
 * Manages employee schedules with morning, afternoon, and evening shifts across 7 days.
 * Uses conditionals, loops, and branching to implement scheduling logic.
 */
public class ScheduleManager {

    private static final String[] DAYS = {
        "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"
    };
    private static final String[] SHIFTS = { "morning", "afternoon", "evening" };
    private static final int MIN_EMPLOYEES_PER_SHIFT = 2;
    private static final int MAX_DAYS_PER_EMPLOYEE = 5;
    private static final int FIRST_PASS_MAX_DAYS = 4;
    private static final int MAX_PER_SHIFT = 4;

    private static final Random RANDOM = new Random(42);

    /** schedule[day][shift] = list of employee names */
    private List<List<List<String>>> schedule;
    /** employee name -> list of (day, shift) assignments */
    private Map<String, List<int[]>> employeeAssignments;

    static class Employee {
        String name;
        Map<Integer, String> preferences;
        Map<Integer, List<String>> priorityRanking;

        Employee(String name, Map<Integer, String> preferences, Map<Integer, List<String>> priorityRanking) {
            this.name = name;
            this.preferences = preferences;
            this.priorityRanking = priorityRanking;
        }

        String getPreferredShift(int day) {
            if (preferences != null && preferences.containsKey(day)) {
                return preferences.get(day);
            }
            return SHIFTS[RANDOM.nextInt(SHIFTS.length)];
        }

        List<String> getPriorityList(int day) {
            if (priorityRanking != null && priorityRanking.containsKey(day)) {
                return priorityRanking.get(day);
            }
            List<String> list = new ArrayList<>();
            list.add(getPreferredShift(day));
            for (String s : SHIFTS) {
                if (!list.contains(s)) list.add(s);
            }
            return list;
        }
    }

    public ScheduleManager() {
        schedule = new ArrayList<>(7);
        for (int day = 0; day < 7; day++) {
            List<List<String>> dayShifts = new ArrayList<>(3);
            for (int s = 0; s < 3; s++) {
                dayShifts.add(new ArrayList<>());
            }
            schedule.add(dayShifts);
        }
        employeeAssignments = new HashMap<>();
    }

    private int shiftIndex(String shift) {
        for (int i = 0; i < SHIFTS.length; i++) {
            if (SHIFTS[i].equals(shift)) return i;
        }
        return 0;
    }

    private void buildEmployeeAssignmentsFromSchedule() {
        employeeAssignments.clear();
        for (int day = 0; day < 7; day++) {
            for (int si = 0; si < SHIFTS.length; si++) {
                for (String name : schedule.get(day).get(si)) {
                    employeeAssignments.computeIfAbsent(name, k -> new ArrayList<>())
                        .add(new int[] { day, si });
                }
            }
        }
    }

    private boolean isShiftFull(int day, String shift) {
        int si = shiftIndex(shift);
        return schedule.get(day).get(si).size() >= MAX_PER_SHIFT;
    }

    private int daysWorked(String name) {
        return employeeAssignments.getOrDefault(name, Collections.emptyList()).size();
    }

    private boolean isAssignedOnDay(String name, int day) {
        for (int[] a : employeeAssignments.getOrDefault(name, Collections.emptyList())) {
            if (a[0] == day) return true;
        }
        return false;
    }

    private void firstPassAssign(List<Employee> employees, boolean usePriority) {
        employeeAssignments.clear();

        List<Integer> dayOrder = new ArrayList<>();
        for (int d = 0; d < 7; d++) dayOrder.add(d);

        for (Employee emp : employees) {
            int daysAssigned = 0;
            Collections.shuffle(dayOrder, RANDOM);

            for (int day : dayOrder) {
                if (daysAssigned >= FIRST_PASS_MAX_DAYS) break;
                if (isAssignedOnDay(emp.name, day)) continue;

                List<String> shiftOrder;
                if (usePriority && emp.priorityRanking != null && emp.priorityRanking.containsKey(day)) {
                    shiftOrder = emp.getPriorityList(day);
                } else {
                    String preferred = emp.getPreferredShift(day);
                    shiftOrder = new ArrayList<>();
                    shiftOrder.add(preferred);
                    for (String s : SHIFTS) {
                        if (!s.equals(preferred)) shiftOrder.add(s);
                    }
                }

                boolean assigned = false;
                for (String shift : shiftOrder) {
                    if (isShiftFull(day, shift)) continue;
                    int si = shiftIndex(shift);
                    schedule.get(day).get(si).add(emp.name);
                    employeeAssignments.computeIfAbsent(emp.name, k -> new ArrayList<>())
                        .add(new int[] { day, si });
                    daysAssigned++;
                    assigned = true;
                    break;
                }

                if (!assigned) {
                    int nextDay = day + 1;
                    if (nextDay < 7 && daysAssigned < FIRST_PASS_MAX_DAYS && !isAssignedOnDay(emp.name, nextDay)) {
                        for (String shift : SHIFTS) {
                            if (!isShiftFull(nextDay, shift)) {
                                int si = shiftIndex(shift);
                                schedule.get(nextDay).get(si).add(emp.name);
                                employeeAssignments.computeIfAbsent(emp.name, k -> new ArrayList<>())
                                    .add(new int[] { nextDay, si });
                                daysAssigned++;
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    private void backfillShifts(List<Employee> employees) {
        List<String> names = new ArrayList<>();
        for (Employee e : employees) names.add(e.name);

        boolean added = true;
        while (added) {
            added = false;
            buildEmployeeAssignmentsFromSchedule();

            for (int day = 0; day < 7; day++) {
                for (int si = 0; si < SHIFTS.length; si++) {
                    int current = schedule.get(day).get(si).size();
                    int needed = Math.max(0, MIN_EMPLOYEES_PER_SHIFT - current);
                    if (needed == 0) continue;

                    List<String> candidates = new ArrayList<>();
                    for (String n : names) {
                        if (schedule.get(day).get(si).contains(n)) continue;
                        if (daysWorked(n) >= MAX_DAYS_PER_EMPLOYEE) continue;
                        if (isAssignedOnDay(n, day)) continue;
                        candidates.add(n);
                    }
                    Collections.shuffle(candidates, RANDOM);
                    for (int i = 0; i < Math.min(needed, candidates.size()); i++) {
                        String name = candidates.get(i);
                        schedule.get(day).get(si).add(name);
                        employeeAssignments.computeIfAbsent(name, k -> new ArrayList<>())
                            .add(new int[] { day, si });
                        added = true;
                        break;
                    }
                }
            }
        }
    }

    /** Returns sample employee data; also used by GUI to load sample data. */
    public static List<Employee> collectInput() {
        List<Employee> employees = new ArrayList<>();

        Map<Integer, String> prefsAlice = new HashMap<>();
        prefsAlice.put(0, "morning"); prefsAlice.put(1, "morning"); prefsAlice.put(2, "afternoon");
        prefsAlice.put(3, "evening"); prefsAlice.put(4, "morning"); prefsAlice.put(5, "afternoon"); prefsAlice.put(6, "evening");
        Map<Integer, List<String>> prioAlice = new HashMap<>();
        prioAlice.put(0, Arrays.asList("morning", "afternoon", "evening"));
        prioAlice.put(1, Arrays.asList("morning", "evening", "afternoon"));
        prioAlice.put(2, Arrays.asList("afternoon", "morning", "evening"));
        prioAlice.put(3, Arrays.asList("evening", "afternoon", "morning"));
        prioAlice.put(4, Arrays.asList("morning", "afternoon", "evening"));
        prioAlice.put(5, Arrays.asList("afternoon", "morning", "evening"));
        prioAlice.put(6, Arrays.asList("evening", "morning", "afternoon"));
        employees.add(new Employee("Alice", prefsAlice, prioAlice));

        Map<Integer, String> prefsBob = new HashMap<>();
        prefsBob.put(0, "afternoon"); prefsBob.put(1, "evening"); prefsBob.put(2, "morning");
        prefsBob.put(3, "morning"); prefsBob.put(4, "afternoon"); prefsBob.put(5, "evening"); prefsBob.put(6, "morning");
        Map<Integer, List<String>> prioBob = new HashMap<>();
        for (int d = 0; d < 7; d++) {
            if (d % 3 == 0) prioBob.put(d, Arrays.asList("afternoon", "evening", "morning"));
            else if (d % 3 == 1) prioBob.put(d, Arrays.asList("evening", "morning", "afternoon"));
            else prioBob.put(d, Arrays.asList("morning", "afternoon", "evening"));
        }
        employees.add(new Employee("Bob", prefsBob, prioBob));

        Map<Integer, String> prefsCarol = new HashMap<>();
        prefsCarol.put(0, "evening"); prefsCarol.put(1, "afternoon"); prefsCarol.put(2, "evening");
        prefsCarol.put(3, "afternoon"); prefsCarol.put(4, "evening"); prefsCarol.put(5, "morning"); prefsCarol.put(6, "afternoon");
        Map<Integer, List<String>> prioCarol = new HashMap<>();
        for (int d = 0; d < 7; d++) prioCarol.put(d, Arrays.asList("evening", "afternoon", "morning"));
        employees.add(new Employee("Carol", prefsCarol, prioCarol));

        Map<Integer, String> prefsDavid = new HashMap<>();
        prefsDavid.put(0, "morning"); prefsDavid.put(1, "afternoon"); prefsDavid.put(2, "morning");
        prefsDavid.put(3, "evening"); prefsDavid.put(4, "afternoon"); prefsDavid.put(5, "morning"); prefsDavid.put(6, "evening");
        employees.add(new Employee("David", prefsDavid, null));

        Map<Integer, String> prefsEve = new HashMap<>();
        prefsEve.put(0, "afternoon"); prefsEve.put(1, "morning"); prefsEve.put(2, "evening");
        prefsEve.put(3, "morning"); prefsEve.put(4, "evening"); prefsEve.put(5, "afternoon"); prefsEve.put(6, "morning");
        Map<Integer, List<String>> prioEve = new HashMap<>();
        for (int d = 0; d < 7; d++) prioEve.put(d, Arrays.asList("afternoon", "morning", "evening"));
        employees.add(new Employee("Eve", prefsEve, prioEve));

        Map<Integer, String> prefsFrank = new HashMap<>();
        prefsFrank.put(0, "morning"); prefsFrank.put(1, "evening"); prefsFrank.put(2, "afternoon");
        prefsFrank.put(3, "morning"); prefsFrank.put(4, "morning"); prefsFrank.put(5, "evening"); prefsFrank.put(6, "afternoon");
        employees.add(new Employee("Frank", prefsFrank, null));

        Map<Integer, String> prefsGrace = new HashMap<>();
        prefsGrace.put(0, "evening"); prefsGrace.put(1, "afternoon"); prefsGrace.put(2, "morning");
        prefsGrace.put(3, "evening"); prefsGrace.put(4, "afternoon"); prefsGrace.put(5, "morning"); prefsGrace.put(6, "evening");
        Map<Integer, List<String>> prioGrace = new HashMap<>();
        for (int d = 0; d < 7; d++) prioGrace.put(d, Arrays.asList("evening", "afternoon", "morning"));
        employees.add(new Employee("Grace", prefsGrace, prioGrace));

        Map<Integer, String> prefsHenry = new HashMap<>();
        prefsHenry.put(0, "afternoon"); prefsHenry.put(1, "morning"); prefsHenry.put(2, "evening");
        prefsHenry.put(3, "afternoon"); prefsHenry.put(4, "evening"); prefsHenry.put(5, "afternoon"); prefsHenry.put(6, "morning");
        employees.add(new Employee("Henry", prefsHenry, null));

        Map<Integer, String> prefsIvy = new HashMap<>();
        prefsIvy.put(0, "morning"); prefsIvy.put(1, "evening"); prefsIvy.put(2, "morning");
        prefsIvy.put(3, "afternoon"); prefsIvy.put(4, "afternoon"); prefsIvy.put(5, "morning"); prefsIvy.put(6, "evening");
        Map<Integer, List<String>> prioIvy = new HashMap<>();
        for (int d = 0; d < 7; d++) prioIvy.put(d, Arrays.asList("morning", "evening", "afternoon"));
        employees.add(new Employee("Ivy", prefsIvy, prioIvy));

        Map<Integer, String> prefsJack = new HashMap<>();
        prefsJack.put(0, "evening"); prefsJack.put(1, "morning"); prefsJack.put(2, "afternoon");
        prefsJack.put(3, "evening"); prefsJack.put(4, "morning"); prefsJack.put(5, "afternoon"); prefsJack.put(6, "morning");
        employees.add(new Employee("Jack", prefsJack, null));

        Map<Integer, String> prefsKate = new HashMap<>();
        prefsKate.put(0, "afternoon"); prefsKate.put(1, "evening"); prefsKate.put(2, "morning");
        prefsKate.put(3, "morning"); prefsKate.put(4, "evening"); prefsKate.put(5, "evening"); prefsKate.put(6, "afternoon");
        Map<Integer, List<String>> prioKate = new HashMap<>();
        for (int d = 0; d < 7; d++) prioKate.put(d, Arrays.asList("afternoon", "evening", "morning"));
        employees.add(new Employee("Kate", prefsKate, prioKate));

        return employees;
    }

    private static String repeat(char c, int n) {
        StringBuilder sb = new StringBuilder(n);
        for (int i = 0; i < n; i++) sb.append(c);
        return sb.toString();
    }

    private void printSchedule() {
        System.out.println(formatScheduleToString());
    }

    /** Returns the schedule as a formatted string (for GUI or other use). */
    public String formatScheduleToString() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n").append(repeat('=', 70)).append("\n");
        sb.append("                    FINAL EMPLOYEE SCHEDULE - WEEK\n");
        sb.append(repeat('=', 70)).append("\n");
        for (int day = 0; day < 7; day++) {
            sb.append("\n  ").append(DAYS[day].toUpperCase()).append("\n");
            sb.append("  ").append(repeat('-', 60)).append("\n");
            for (int si = 0; si < SHIFTS.length; si++) {
                List<String> names = schedule.get(day).get(si);
                String namesStr = names.isEmpty() ? "(none)" : String.join(", ", names);
                String shiftName = SHIFTS[si].substring(0, 1).toUpperCase() + SHIFTS[si].substring(1);
                sb.append(String.format("    %-10s : %s%n", shiftName, namesStr));
            }
        }
        sb.append("\n").append(repeat('=', 70)).append("\n");
        return sb.toString();
    }

    public void run() {
        List<Employee> employees = collectInput();
        boolean usePriority = false;
        for (Employee e : employees) {
            if (e.priorityRanking != null && !e.priorityRanking.isEmpty()) {
                usePriority = true;
                break;
            }
        }
        firstPassAssign(employees, usePriority);
        backfillShifts(employees);
        printSchedule();
    }

    /** Runs scheduling with sample data and returns the schedule as a string (for GUI). */
    public static String runAndGetScheduleOutput() {
        return runAndGetScheduleOutput(collectInput());
    }

    /** Runs scheduling with the given employees and returns the schedule as a string (for GUI). */
    public static String runAndGetScheduleOutput(List<Employee> employees) {
        if (employees == null || employees.isEmpty()) return "Add at least one employee.";
        ScheduleManager manager = new ScheduleManager();
        boolean usePriority = false;
        for (Employee e : employees) {
            if (e.priorityRanking != null && !e.priorityRanking.isEmpty()) {
                usePriority = true;
                break;
            }
        }
        manager.firstPassAssign(employees, usePriority);
        manager.backfillShifts(employees);
        return manager.formatScheduleToString();
    }

    public static void main(String[] args) {
        if (args.length > 0 && "gui".equalsIgnoreCase(args[0])) {
            ScheduleManagerGUI.launch();
            return;
        }
        ScheduleManager manager = new ScheduleManager();
        manager.run();
    }
}
