# CampusOne: Student Management & Campus Automation System (v1.0)

CampusOne is a role-based campus platform designed for modern educational institutions, supporting **Students**, **Teachers**, and **Administrators**.

---

## 🏛 System Architecture

The project consists of three integrated layers:

```
campusone/
├── backend/          # FastAPI 3.12+ Async Backend (Python, SQLAlchemy 2 Async, SQLite/PostgreSQL)
├── admin-web/        # Admin Web Management Console (React 18, TypeScript, Vite, Tailwind-style tokens)
├── composeApp/       # Student & Teacher Cross-Platform Mobile App (Kotlin Multiplatform + Compose)
├── infra/            # Docker Compose orchestration (PostgreSQL, Redis, MinIO, ClamAV)
└── docs/             # Database schemas, technical specifications, permissions matrices
```

---

## 🚀 Quick Start Guide

### 1. Backend Server (FastAPI)
The backend is powered by async SQLAlchemy with optimistic concurrency control, Argon2id auth, and DPDP-compliant data handling.

```bash
cd backend
# 1. Install dependencies
pip install -r requirements.txt

# 2. Seed default data (Department, Courses, Admin, Teacher, Student)
python -m app.db.seed

# 3. Start development server
python -m uvicorn app.main:app --host 0.0.0.0 --port 8000 --reload
```
- **API Health Check:** `http://localhost:8000/health`
- **Interactive Swagger Docs:** `http://localhost:8000/docs`
- **ReDoc:** `http://localhost:8000/redoc`

---

### 2. Administrator Web Console (React + TypeScript)
Built to strictly match the UI Skeleton specification (Pages 11–19):

```bash
cd admin-web
npm install
npm run dev
```
- **Web Console URL:** `http://localhost:5173/`

#### Default Admin Credentials:
- **Staff / Login ID:** `ADMIN001`
- **Password:** `AdminPass123!`
- **MFA Code:** `123456`

---

### 3. Mobile Application (Kotlin Multiplatform / Android)
Built with Compose Multiplatform supporting both Android and iOS targets.

1. Open the root folder `erp system` in **Android Studio**.
2. Select **`composeApp`** in the run configuration dropdown.
3. Select your emulator (e.g. `Pixel 9 Pro`).
4. Click **Run (▶)**.

#### Mobile Features Implemented (UI Skeleton Pages 2–10):
- **Role Switcher:** Switch between **Student** and **Teacher** personas with a single tap.
- **Backend Bridge:** Connects to `http://10.0.2.2:8000` with cleartext local support and real-time connectivity status.
- **Student Flow:**
  - 🏠 **Home:** 82% circular attendance gauge, status marks (`●`, `▲`, `■`, `◆`), and lecture timeline.
  - 📝 **Tasks:** Pending/Submitted/Graded filters, assignment details, and solution submission modal.
  - ✉️ **Leave:** Casual & Medical leave balances, request history, and overlap conflict detection.
- **Teacher Flow:**
  - 🏠 **Home:** Action items (open attendance sessions, pending leave requests), class schedule.
  - 📅 **Attendance Roster:** Segmented `P` (Present), `AB` (Absent), `NM` (Medical) toggles, live counters, and session finalization.
  - ✉️ **Leave Approvals:** Review student medical/casual leave applications with 1-tap Approve/Reject.

---

## 🔒 Status Mark Token Standard (Design Specification)

Every status across Admin Web and Mobile strictly pairs shape symbols with color tokens for accessibility:
- `●` (Circle) **Good / Approved / Present** (`#10B981`)
- `▲` (Triangle) **Attention / Pending / Review** (`#F59E0B`)
- `■` (Square) **Critical / Absent / Rejected** (`#EF4444`)
- `◆` (Diamond) **Informational / Excused / Graded** (`#6366F1`)

---

## 🧪 Automated Testing

Run the backend unit test suite covering auth, attendance calculations, leave workflows, and security:

```bash
cd backend
pytest tests/ -v
```
*(All 16 unit tests passing)*

---

## 🐳 Full Containerized Deployment (Docker)

To launch all infrastructure services (PostgreSQL, Redis, MinIO, Backend, and Admin Web):

```bash
docker compose -f infra/docker/compose.full.yml up --build -d
```
