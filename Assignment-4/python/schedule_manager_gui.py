#!/usr/bin/env python3
"""
Optional GUI for Employee Schedule Manager.
Input: employee names and shift preferences per day (entered by user in GUI).
Output: final weekly schedule in a readable format.
Uses tkinter (standard library).
"""
import tkinter as tk
from tkinter import ttk, messagebox, scrolledtext
import random
from schedule_manager import (
    DAYS,
    SHIFTS,
    Employee,
    build_schedule_structure,
    first_pass_assign,
    backfill_shifts,
)

MIN_EMPLOYEES_PER_SHIFT = 2
MAX_DAYS_PER_EMPLOYEE = 5
FIRST_PASS_MAX_DAYS = 4
MAX_PER_SHIFT = 4


def run_scheduler_from_gui(employees_data: list) -> str:
    """Build schedule from list of {name, preferences: {day: shift}, priority: {day: [shifts]} or None}."""
    employees = []
    for d in employees_data:
        emp = Employee(
            name=d["name"],
            preferences=d.get("preferences", {}),
            priority_ranking=d.get("priority"),
        )
        employees.append(emp)
    schedule = build_schedule_structure()
    use_priority = any(e.priority_ranking for e in employees)
    first_pass_assign(employees, schedule, use_priority=use_priority)
    backfill_shifts(employees, schedule)
    return format_schedule_text(schedule)


def format_schedule_text(schedule) -> str:
    lines = [
        "=" * 70,
        "                    FINAL EMPLOYEE SCHEDULE - WEEK",
        "=" * 70,
    ]
    for day in range(7):
        lines.append(f"\n  {DAYS[day].upper()}")
        lines.append("  " + "-" * 60)
        for shift in SHIFTS:
            names = schedule[day][shift]
            names_str = ", ".join(names) if names else "(none)"
            lines.append(f"    {shift.capitalize():10} : {names_str}")
    lines.append("\n" + "=" * 70)
    return "\n".join(lines)


class AddEmployeeDialog:
    """Dialog to enter employee name and shift preferences for each day."""

    def __init__(self, parent, on_save, edit_data=None):
        self.on_save = on_save
        self.edit_data = edit_data
        self.result = None
        self.win = tk.Toplevel(parent)
        self.win.title("Edit employee" if edit_data else "Add employee")
        self.win.transient(parent)
        self.win.grab_set()
        self.win.geometry("520x380")

        f = ttk.Frame(self.win, padding=10)
        f.pack(fill=tk.BOTH, expand=True)

        ttk.Label(f, text="Employee name:").grid(row=0, column=0, sticky=tk.W, pady=2)
        self.name_var = tk.StringVar(value=(edit_data["name"] if edit_data else ""))
        ttk.Entry(f, textvariable=self.name_var, width=25).grid(row=0, column=1, columnspan=2, sticky=tk.W, pady=2)

        ttk.Label(f, text="Preferred shift for each day (optional: 2nd and 3rd choice for priority):").grid(
            row=1, column=0, columnspan=4, sticky=tk.W, pady=(12, 4)
        )
        # Headers
        ttk.Label(f, text="Day").grid(row=2, column=0, padx=(0, 12), pady=2)
        ttk.Label(f, text="1st choice").grid(row=2, column=1, padx=4, pady=2)
        ttk.Label(f, text="2nd choice").grid(row=2, column=2, padx=4, pady=2)
        ttk.Label(f, text="3rd choice").grid(row=2, column=3, padx=4, pady=2)

        self.day_vars = []
        for day in range(7):
            row_vars = []
            ttk.Label(f, text=DAYS[day]).grid(row=3 + day, column=0, sticky=tk.W, padx=(0, 12), pady=2)
            for choice in range(3):
                var = tk.StringVar(value=SHIFTS[choice] if not edit_data else self._get_choice(edit_data, day, choice))
                row_vars.append(var)
                cb = ttk.Combobox(f, textvariable=var, values=SHIFTS, state="readonly", width=10)
                cb.grid(row=3 + day, column=1 + choice, padx=4, pady=2)
            self.day_vars.append(row_vars)

        btn_f = ttk.Frame(f)
        btn_f.grid(row=11, column=0, columnspan=4, pady=16)
        ttk.Button(btn_f, text="Save", command=self._save).pack(side=tk.LEFT, padx=4)
        ttk.Button(btn_f, text="Cancel", command=self.win.destroy).pack(side=tk.LEFT, padx=4)

        self.win.protocol("WM_DELETE_WINDOW", self.win.destroy)

    def _get_choice(self, data, day, choice):
        if data.get("priority") and day in data["priority"] and choice < len(data["priority"][day]):
            return data["priority"][day][choice]
        if data.get("preferences") and day in data["preferences"] and choice == 0:
            return data["preferences"][day]
        return SHIFTS[choice]

    def _save(self):
        name = self.name_var.get().strip()
        if not name:
            messagebox.showwarning("Missing name", "Please enter employee name.", parent=self.win)
            return
        preferences = {}
        priority = {}
        for day in range(7):
            choices = [self.day_vars[day][c].get().strip().lower() for c in range(3)]
            if not all(c in SHIFTS for c in choices):
                messagebox.showwarning("Invalid shift", "Use only: morning, afternoon, evening.", parent=self.win)
                return
            if len(set(choices)) != 3:
                messagebox.showwarning("Duplicate choice", "1st, 2nd, and 3rd choice must be different per day.", parent=self.win)
                return
            preferences[day] = choices[0]
            priority[day] = choices
        self.result = {"name": name, "preferences": preferences, "priority": priority}
        self.on_save(self.result, self.edit_data)
        self.win.destroy()


class ScheduleManagerApp:
    def __init__(self):
        self.root = tk.Tk()
        self.root.title("Employee Schedule Manager")
        self.root.geometry("740x560")
        self.employees_data = []
        self._build_ui()

    def _build_ui(self):
        # Input frame
        input_frame = ttk.LabelFrame(self.root, text="Employees & shift preferences (enter via Add employee)", padding=8)
        input_frame.pack(fill=tk.X, padx=8, pady=4)

        btn_f = ttk.Frame(input_frame)
        btn_f.pack(fill=tk.X)
        ttk.Button(btn_f, text="Add employee", command=self._add_employee).pack(side=tk.LEFT, padx=2)
        ttk.Button(btn_f, text="Edit selected", command=self._edit_employee).pack(side=tk.LEFT, padx=2)
        ttk.Button(btn_f, text="Remove selected", command=self._remove_employee).pack(side=tk.LEFT, padx=2)
        ttk.Button(btn_f, text="Load sample data", command=self._load_sample_data).pack(side=tk.LEFT, padx=2)
        ttk.Button(btn_f, text="Clear all", command=self._clear_employees).pack(side=tk.LEFT, padx=2)
        ttk.Button(btn_f, text="Generate schedule", command=self._generate).pack(side=tk.RIGHT, padx=2)

        self.listbox = tk.Listbox(input_frame, height=5, width=70)
        self.listbox.pack(fill=tk.X, pady=4)
        ttk.Label(input_frame, text="Double-click a name to edit. Add at least 2–3 employees for a valid schedule.").pack(anchor=tk.W)

        # Output frame
        output_frame = ttk.LabelFrame(self.root, text="Final schedule", padding=8)
        output_frame.pack(fill=tk.BOTH, expand=True, padx=8, pady=4)
        self.output_text = scrolledtext.ScrolledText(output_frame, wrap=tk.WORD, font=("Consolas", 10), height=18)
        self.output_text.pack(fill=tk.BOTH, expand=True)
        self.output_text.insert(tk.END, "Add employees (name + preferred shift per day), then click 'Generate schedule'.")
        self.output_text.config(state=tk.DISABLED)

        self.listbox.bind("<Double-Button-1>", lambda e: self._edit_employee())

    def _add_employee(self):
        def save(data, _):
            self.employees_data.append(data)
            self.listbox.insert(tk.END, data["name"])

        AddEmployeeDialog(self.root, save)

    def _edit_employee(self):
        sel = self.listbox.curselection()
        if not sel:
            messagebox.showinfo("Select employee", "Select an employee in the list to edit.")
            return
        idx = sel[0]
        edit_data = self.employees_data[idx]

        def save(data, original):
            self.employees_data[idx] = data
            self.listbox.delete(idx)
            self.listbox.insert(idx, data["name"])

        AddEmployeeDialog(self.root, save, edit_data=edit_data)

    def _remove_employee(self):
        sel = self.listbox.curselection()
        if not sel:
            messagebox.showinfo("Select employee", "Select an employee in the list to remove.")
            return
        idx = sel[0]
        del self.employees_data[idx]
        self.listbox.delete(idx)

    def _load_sample_data(self):
        from schedule_manager import collect_input
        for e in collect_input():
            if not any(d["name"] == e.name for d in self.employees_data):
                self.employees_data.append({
                    "name": e.name,
                    "preferences": e.preferences,
                    "priority": e.priority_ranking,
                })
                self.listbox.insert(tk.END, e.name)
        if self.employees_data:
            messagebox.showinfo("Sample data", "Sample employees added. You can edit or add more, then Generate schedule.")

    def _clear_employees(self):
        self.employees_data.clear()
        self.listbox.delete(0, tk.END)

    def _generate(self):
        if not self.employees_data:
            messagebox.showinfo("Info", "Add at least one employee first (Add employee or Load sample data).")
            return
        if len(self.employees_data) < 2:
            messagebox.showwarning("Few employees", "At least 2 employees are recommended so every shift can have 2 people.")
        random.seed(42)
        try:
            out = run_scheduler_from_gui(self.employees_data)
        except Exception as ex:
            messagebox.showerror("Error", str(ex))
            return
        self.output_text.config(state=tk.NORMAL)
        self.output_text.delete(1.0, tk.END)
        self.output_text.insert(tk.END, out)
        self.output_text.config(state=tk.DISABLED)

    def run(self):
        self.root.mainloop()


if __name__ == "__main__":
    ScheduleManagerApp().run()
