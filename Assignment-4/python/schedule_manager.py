#!/usr/bin/env python3
"""
Employee Schedule Manager - Python Implementation
Manages employee schedules with morning, afternoon, and evening shifts across 7 days.
Uses conditionals, loops, and branching to implement scheduling logic.
"""

import random
from typing import Dict, List, Optional, Tuple
from dataclasses import dataclass, field

# Constants
DAYS = ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"]
SHIFTS = ["morning", "afternoon", "evening"]
MIN_EMPLOYEES_PER_SHIFT = 2
MAX_DAYS_PER_EMPLOYEE = 5
FIRST_PASS_MAX_DAYS = 4  # Leave capacity for backfill to meet min 2 per shift
MAX_PER_SHIFT = 4  # Cap per shift to allow conflict detection ("full")


@dataclass
class Employee:
    """Stores employee name and shift preferences per day."""
    name: str
    # preferences[day_index] = preferred shift name, or list for priority (bonus)
    preferences: Dict[int, str] = field(default_factory=dict)
    # Optional: priority_ranking[day_index] = [shift1, shift2, shift3] (1st, 2nd, 3rd choice)
    priority_ranking: Optional[Dict[int, List[str]]] = None

    def get_preferred_shift(self, day: int) -> str:
        """Return preferred shift for day (single preference)."""
        return self.preferences.get(day, random.choice(SHIFTS))

    def get_priority_list(self, day: int) -> List[str]:
        """Return ordered list of shift preferences for day (bonus)."""
        if self.priority_ranking and day in self.priority_ranking:
            return self.priority_ranking[day]
        default = [self.get_preferred_shift(day)]
        for s in SHIFTS:
            if s not in default:
                default.append(s)
        return default


def build_schedule_structure() -> Dict[int, Dict[str, List[str]]]:
    """Create empty schedule: schedule[day_index][shift] = list of employee names."""
    schedule = {}
    for day in range(7):
        schedule[day] = {shift: [] for shift in SHIFTS}
    return schedule


def count_employee_days(assignments: List[Tuple[int, str]], employee: str) -> int:
    """Count how many days an employee is assigned."""
    return sum(1 for d, _ in assignments if d is not None)


def get_employee_assignments(schedule: Dict[int, Dict[str, List[str]]]) -> Dict[str, List[Tuple[int, str]]]:
    """From schedule, build map: employee_name -> [(day_index, shift), ...]."""
    result = {}
    for day in range(7):
        for shift in SHIFTS:
            for name in schedule[day][shift]:
                if name not in result:
                    result[name] = []
                result[name].append((day, shift))
    return result


def is_shift_full(schedule: Dict[int, Dict[str, List[str]]], day: int, shift: str) -> bool:
    """Return True if this shift on this day has reached capacity (for conflict)."""
    return len(schedule[day][shift]) >= MAX_PER_SHIFT


def days_worked(employee_assignments: Dict[str, List[Tuple[int, str]]], name: str) -> int:
    """Return number of days the employee is assigned."""
    return len(employee_assignments.get(name, []))


def first_pass_assign(
    employees: List[Employee],
    schedule: Dict[int, Dict[str, List[str]]],
    use_priority: bool,
) -> None:
    """
    Assign employees to shifts by preference (or priority order).
    No more than one shift per day, max 5 days per employee.
    Resolve conflicts: if preferred shift full, assign to another shift same day or next day.
    """
    employee_assignments: Dict[str, List[Tuple[int, str]]] = {}

    day_order = list(range(7))
    for emp in employees:
        days_assigned = 0
        random.shuffle(day_order)  # Spread assignments across week (including weekend)
        for day in day_order:
            if days_assigned >= FIRST_PASS_MAX_DAYS:
                break
            if day in [a[0] for a in employee_assignments.get(emp.name, [])]:
                continue  # Already assigned this day

            if use_priority and emp.priority_ranking and day in emp.priority_ranking:
                shift_order = emp.priority_ranking[day]
            else:
                preferred = emp.get_preferred_shift(day)
                shift_order = [preferred] + [s for s in SHIFTS if s != preferred]

            assigned = False
            for shift in shift_order:
                if is_shift_full(schedule, day, shift):
                    continue  # Conflict: shift full, try next preference
                schedule[day][shift].append(emp.name)
                employee_assignments.setdefault(emp.name, []).append((day, shift))
                days_assigned += 1
                assigned = True
                break

            if not assigned:
                # Try next day for any available shift (conflict resolution)
                for next_day in range(day + 1, min(day + 2, 7)):
                    if days_assigned >= FIRST_PASS_MAX_DAYS:
                        break
                    if next_day in [a[0] for a in employee_assignments.get(emp.name, [])]:
                        continue
                    for shift in SHIFTS:
                        if not is_shift_full(schedule, next_day, shift):
                            schedule[next_day][shift].append(emp.name)
                            employee_assignments.setdefault(emp.name, []).append((next_day, shift))
                            days_assigned += 1
                            assigned = True
                            break
                    if assigned:
                        break


def backfill_shifts(
    employees: List[Employee],
    schedule: Dict[int, Dict[str, List[str]]],
) -> None:
    """
    Ensure at least MIN_EMPLOYEES_PER_SHIFT per shift per day.
    Randomly assign additional employees who have not worked 5 days yet.
    """
    names = [e.name for e in employees]
    added = True
    while added:
        added = False
        employee_assignments = get_employee_assignments(schedule)
        for day in range(7):
            for shift in SHIFTS:
                current = len(schedule[day][shift])
                needed = max(0, MIN_EMPLOYEES_PER_SHIFT - current)
                if needed == 0:
                    continue

                # Candidates: not on this shift this day, and have worked < 5 days
                candidates = [
                    n for n in names
                    if n not in schedule[day][shift]
                    and days_worked(employee_assignments, n) < MAX_DAYS_PER_EMPLOYEE
                    and not any(a[0] == day for a in employee_assignments.get(n, []))
                ]
                random.shuffle(candidates)
                for i in range(min(needed, len(candidates))):
                    name = candidates[i]
                    schedule[day][shift].append(name)
                    employee_assignments.setdefault(name, []).append((day, shift))
                    added = True
                    break


def collect_input() -> List[Employee]:
    """
    Collect employee names and shift preferences (and optional priority ranking).
    Uses sample data for demo; can be replaced with interactive input.
    """
    employees = []

    # Sample data with preferences per day and optional priority (bonus)
    sample = [
        {
            "name": "Alice",
            "preferences": {0: "morning", 1: "morning", 2: "afternoon", 3: "evening", 4: "morning", 5: "afternoon", 6: "evening"},
            "priority": {0: ["morning", "afternoon", "evening"], 1: ["morning", "evening", "afternoon"], 2: ["afternoon", "morning", "evening"], 3: ["evening", "afternoon", "morning"], 4: ["morning", "afternoon", "evening"], 5: ["afternoon", "morning", "evening"], 6: ["evening", "morning", "afternoon"]},
        },
        {
            "name": "Bob",
            "preferences": {0: "afternoon", 1: "evening", 2: "morning", 3: "morning", 4: "afternoon", 5: "evening", 6: "morning"},
            "priority": {d: ["afternoon", "evening", "morning"] if d % 3 == 0 else ["evening", "morning", "afternoon"] if d % 3 == 1 else ["morning", "afternoon", "evening"] for d in range(7)},
        },
        {
            "name": "Carol",
            "preferences": {0: "evening", 1: "afternoon", 2: "evening", 3: "afternoon", 4: "evening", 5: "morning", 6: "afternoon"},
            "priority": {d: ["evening", "afternoon", "morning"] for d in range(7)},
        },
        {
            "name": "David",
            "preferences": {0: "morning", 1: "afternoon", 2: "morning", 3: "evening", 4: "afternoon", 5: "morning", 6: "evening"},
            "priority": None,
        },
        {
            "name": "Eve",
            "preferences": {0: "afternoon", 1: "morning", 2: "evening", 3: "morning", 4: "evening", 5: "afternoon", 6: "morning"},
            "priority": {d: ["afternoon", "morning", "evening"] for d in range(7)},
        },
        {
            "name": "Frank",
            "preferences": {0: "morning", 1: "evening", 2: "afternoon", 3: "morning", 4: "morning", 5: "evening", 6: "afternoon"},
            "priority": None,
        },
        {
            "name": "Grace",
            "preferences": {0: "evening", 1: "afternoon", 2: "morning", 3: "evening", 4: "afternoon", 5: "morning", 6: "evening"},
            "priority": {d: ["evening", "afternoon", "morning"] for d in range(7)},
        },
        {
            "name": "Henry",
            "preferences": {0: "afternoon", 1: "morning", 2: "evening", 3: "afternoon", 4: "evening", 5: "afternoon", 6: "morning"},
            "priority": None,
        },
        {
            "name": "Ivy",
            "preferences": {0: "morning", 1: "evening", 2: "morning", 3: "afternoon", 4: "afternoon", 5: "morning", 6: "evening"},
            "priority": {d: ["morning", "evening", "afternoon"] for d in range(7)},
        },
        {
            "name": "Jack",
            "preferences": {0: "evening", 1: "morning", 2: "afternoon", 3: "evening", 4: "morning", 5: "afternoon", 6: "morning"},
            "priority": None,
        },
        {
            "name": "Kate",
            "preferences": {0: "afternoon", 1: "evening", 2: "morning", 3: "morning", 4: "evening", 5: "evening", 6: "afternoon"},
            "priority": {d: ["afternoon", "evening", "morning"] for d in range(7)},
        },
    ]

    for s in sample:
        emp = Employee(
            name=s["name"],
            preferences=s["preferences"],
            priority_ranking=s.get("priority"),
        )
        employees.append(emp)

    return employees


def print_schedule(schedule: Dict[int, Dict[str, List[str]]]) -> None:
    """Output the final schedule in a readable format."""
    print("\n" + "=" * 70)
    print("                    FINAL EMPLOYEE SCHEDULE - WEEK")
    print("=" * 70)
    for day in range(7):
        print(f"\n  {DAYS[day].upper()}")
        print("  " + "-" * 60)
        for shift in SHIFTS:
            names = schedule[day][shift]
            names_str = ", ".join(names) if names else "(none)"
            print(f"    {shift.capitalize():10} : {names_str}")
    print("\n" + "=" * 70)


def main() -> None:
    """Run the schedule manager: input, schedule, resolve conflicts, backfill, output."""
    random.seed(42)
    employees = collect_input()
    schedule = build_schedule_structure()

    # Scheduling: first pass with priority preferences (bonus)
    use_priority = any(e.priority_ranking for e in employees)
    first_pass_assign(employees, schedule, use_priority=use_priority)

    # Ensure minimum 2 per shift
    backfill_shifts(employees, schedule)

    print_schedule(schedule)


if __name__ == "__main__":
    main()
