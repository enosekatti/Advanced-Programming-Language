# Employee Schedule Manager

A small application for managing employee schedules. The company operates 7 days a week with morning, afternoon, and evening shifts. Implemented in **Python** and **Java** to demonstrate control structures (conditionals, loops, branching) in different paradigms.

## Requirements Implemented

1. **Input and Storage**: Employee names and preferred shifts (morning, afternoon, evening) for each day; stored in appropriate data structures.
2. **Scheduling Logic**:
   - No employee works more than one shift per day.
   - Maximum 5 days per week per employee.
   - At least 2 employees per shift per day; backfill with random assignments when needed.
3. **Shift Conflicts**: If an employee's preferred shift is full, assign to another available shift on the same or next day.
4. **Output**: Final weekly schedule in a readable format.
5. **Bonus**: Priority ranking for shift preferences (e.g., morning=1st, evening=2nd, afternoon=3rd); accommodated when possible.

## Project Structure

- `python/` – Python implementation
- `java/` – Java implementation

## Running the Applications

### Python (CLI)
```bash
cd python
python schedule_manager.py
```

### Python (optional GUI)
```bash
cd python
python schedule_manager_gui.py
```
The GUI accepts **user input**: add employees (name + preferred shift for each day; optional 2nd/3rd choice for priority), edit or remove them, then generate the schedule. You can also load sample data to try it quickly.

### Java (CLI)
Requires Java 8 or later.
```bash
cd java
javac ScheduleManager.java
java ScheduleManager
```

### Java (GUI)
Swing GUI that accepts **user input**: add employees (name + 1st/2nd/3rd shift choice per day), remove selected, load sample data, then generate the schedule.
```bash
cd java
javac ScheduleManager.java ScheduleManagerGUI.java
java ScheduleManager gui
# or run the GUI class directly:
java ScheduleManagerGUI
```
