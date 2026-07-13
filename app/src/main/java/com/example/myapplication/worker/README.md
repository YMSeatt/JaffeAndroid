# ⚙️ Background Tasks & Automated Reporting

This package manages the application's background processing infrastructure using **Jetpack WorkManager**. It is responsible for orchestrating automated classroom reports, ensuring that they are triggered, generated, and transmitted reliably without impacting the foreground user experience.

## 🏛️ Background Architecture

The system utilizes a "Scheduler-Executor" pattern to maintain a clean separation of concerns:

### 1. The Metronome (`EmailSchedulerWorker.kt`)
- **Role**: A lightweight periodic worker (typically running every 15 minutes).
- **Responsibility**: Scans the [EmailRepository] to identify which user-defined schedules are due for processing based on the current system time and day.
- **Action**: It does *not* generate reports itself. Instead, it enqueues a specialized task for the execution engine.

### 2. The Heavy Lifter (`EmailWorker.kt`)
- **Location**: Resides in `com.example.myapplication.util` due to its high dependency on core logical engines (Exporter, EmailUtil).
- **Role**: A transactional worker that performs the "Heavy Lifting."
- **Responsibility**: Handles Excel workbook generation, PII decryption, and SMTP transmission.
- **Isolation**: Runs strictly on `Dispatchers.IO` and ensures deterministic cleanup of temporary files.

## 🛡️ Security & Reliability

### 🔐 PII Protection (Secure Worker Boundary)
Data passed between workers or stored in the `WorkManager` internal database is treated as sensitive.
- All report metadata (recipients, subjects, bodies) is **encrypted** using `SecurityUtil` before being placed in the `Data` input object.
- Workers automatically decrypt this data at the start of their `doWork()` cycle.

### ⛓️ Sequential Execution
By splitting the "Decision" (Scheduler) from the "Execution" (Heavy Lifter), the system ensures that:
1.  The periodic schedule check remains fast and never blocks other background tasks.
2.  Failures in the SMTP server or Excel generation do not "jam" the overall scheduling pulse.
3.  Each report attempt can be retried independently using WorkManager's backoff policies.

---
*Documentation love letter from Scribe 📜*
