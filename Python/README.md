# 🐍 Seating Chart Desktop (Python)

This directory contains the Python-based desktop implementation of the Seating Chart & Behavior Logger. It provides a visual interface for managing classroom layouts, tracking student behavior, and generating detailed reports.

## 🛠️ Tech Stack

*   **Language**: Python 3
*   **UI Framework**: Tkinter (Standard Library)
*   **Theming**: `sv_ttk` (Sun-Valley Theme) for a modern look.
*   **Data Serialization**: `json`
*   **Encryption**: `cryptography` (Fernet) for secure data storage.
*   **Excel Reporting**: `openpyxl`
*   **Image Processing**: `pillow` (PIL)
*   **Single Instance Locking**: `portalocker`

## 📂 Project Structure

*   `seatingchartmain.py`: The entry point and main application logic. Coordinates the UI, canvas interactions, and data synchronization.
*   `commands.py`: Implements the **Command Pattern**. Every user action (moving students, logging behavior, changing settings) is encapsulated as a `Command` object, enabling a robust multi-step Undo/Redo system.
*   `data_encryption.py`: Handles Fernet encryption and decryption for the application's JSON data files.
*   `data_locker.py`: Manages file-level access and integrity.
*   `dialogs.py`: Contains custom Tkinter dialogs for adding/editing students, furniture, and logging events.
*   `quizhomework.py`: Logic and dialogs specifically for managing quiz and homework templates.
*   `other.py`: Miscellaneous utilities, including `PasswordManager`, `FileLockManager`, and the `HelpDialog`.
*   `undohistorydialog.py`: A visual interface for the Undo/Redo stack.

## 🚀 Setup & Execution

1.  **Install Dependencies**:
    ```bash
    pip install sv_ttk darkdetect openpyxl pillow cryptography portalocker
    ```
2.  **External Dependency: Ghostscript**:
    The "Export Layout as Image" feature generates PostScript files which are rasterized using `pillow`. For this to work, **Ghostscript** must be installed on your system and added to your `PATH`.
    - Windows: [Ghostscript Downloads](https://ghostscript.com/releases/gsdnld.html)
    - Linux: `sudo apt install ghostscript`
3.  **Run the App**:
    ```bash
    python seatingchartmain.py
    ```

## 🔐 Security & Persistence

*   **Encryption**: Data is stored in encrypted JSON files (`classroom_data_v10.json`, etc.) using the Fernet (AES-128) specification.
*   **Passwords**: User passwords are hashed using **SHA3-512** via `hashlib`.
*   **Recovery**: A master recovery hash is provided in `other.py` as a fail-safe.
*   **Locking**: The app uses `portalocker` to ensure only one instance of the application can access the data files at a time.

## 🔄 Logic Parity

This application is designed to maintain logical parity with its [Android counterpart](../README.md). They share the same data structures for students, furniture, behavior logs, and complex conditional formatting rules, facilitating future cross-platform synchronization.

---
*Documentation love letter from Scribe 📜*
