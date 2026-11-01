import pytest
from app.services.attendance_service import AttendanceService


def test_section_5_1_example():
    """
    Validates example from documentation Section 5.1:
    - 22 present, 3 absent, 5 not marked
    - Counted = 22 + 3 = 25 (the 5 NM are excluded)
    - Percentage = 22 / 25 * 100 = 88%
    """
    present = 22
    absent = 3
    # 5 NM are ignored by definition

    percentage, is_low, display = AttendanceService.calculate_attendance_percentage(
        present_count=present,
        absent_count=absent,
        threshold=75.0
    )

    assert percentage == 88.0
    assert is_low is False
    assert display == "88.0%"


def test_no_finalized_records():
    """
    Validates rule: No finalized records -> "No attendance data"
    """
    percentage, is_low, display = AttendanceService.calculate_attendance_percentage(
        present_count=0,
        absent_count=0,
        threshold=75.0
    )

    assert percentage is None
    assert is_low is False
    assert display == "No attendance data"


def test_low_attendance_threshold_alert():
    """
    Validates rule: Attendance below threshold (75%) triggers low alert.
    """
    # 14 present, 6 absent -> 14 / 20 = 70% (< 75%)
    percentage, is_low, display = AttendanceService.calculate_attendance_percentage(
        present_count=14,
        absent_count=6,
        threshold=75.0
    )

    assert percentage == 70.0
    assert is_low is True
    assert display == "70.0%"
