# Nocturne - Music Library Manager
Users with large personal audio collections frequently struggle with locating tracks, organizing playlists, and controlling playback in one interface. Nocturne addresses this by providing a single desktop application where users can import audio files,
browse, navigate and search their library, manage playlists, and control playback - all within a unified JavaFX interface!.

## Table of Contents

* [Installation](#installation)
* [Developer Guide](#developer-guide)
* [Features](#features)
* [Tech Stack](#tech-stack)
* [Project Structure](#project-structure)
* [Architecture](#architecture)
* [OOP & Data Structures](#oop-and-data-structures)
* [Team Members](#team-members)
* [Acknowledgements](#acknowledgements)

## Installation

>[!NOTE]
>This application works with local audio files. If you don't already have a local music collection of (preferably well-tagged) mp3 files, you can refer to
>our [sample list](https://drive.google.com/drive/folders/10TS9JoTon5uKNKznU8Vyobcdj2MITmDN?usp=sharing) to try it out for yourself. There are many unofficial
>sites out there supporting well tagged mp3 files ([Squid.wtf](https://qobuz.squid.wtf)), although we do not officially endorse the usage of such websites. Instead,
>you also have the option of buying and supporting the creators directly from trusted online vendors!

- In order to install and run the application, please make sure that you have installed [Java SE 17+](https://www.oracle.com/java/technologies/) in your system.

- If you are viewing this from the github repository, head towards the Github release page and install the latest version, based on your operating system (Windows or macOS).

- For people viewing from a zip file (e.g., instructors), note that two .jar files have been placed in the demonstration folder. As such, if you have Java installed, you can run either one depending on your operating system.

> [!IMPORTANT]
> Upon launching, some operating systems may prompt you for security permission. Please click **"Allow"** or **"Open Anyway"** to proceed. Rest assured, this application is completely safe and is not malware.

## Developer Guide

For developers, in order to view and run the project, please do the following:

* Clone the repository to your machine:
```bash
  git clone https://github.com/AvaShZmu/Nocturne.git
```

* Open the project in your IDE (preferably IntelliJ IDEA)

* Run the project:
```bash
  mvn javafx:run
```

## Features

- Add, remove, and browse audio tracks
- Search by title or artist; sort and filter your library
- Create and manage playlists
- Audio playback with play/pause/skip/volume controls
- Persistent storage — your library saves between sessions

> [!NOTE]
> **Supported Formats:** Nocturne currently supports playback and metadata extraction for `.mp3` and `.wav` files. Formats such as `flac` are to be added in the future.

## Tech Stack

- **Java SE** — Core language and standard library
- **JavaFX** — GUI framework and Mediaplayer for audio playback
- **Gson** — JSON-based data persistence
- **jAudioTagger** — Extraction of ID3 metadata from audio files
- **Maven** — Build and dependency management
- **IntelliJ IDEA** — Primary IDE for development

## Project Structure

~~~
COMP1020_Music_Library_Manager/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   ├── GUI/                     # UI Layer (JavaFX)
│   │   │   │   ├── controller/          # FXML view controllers (library, playback, etc.)
│   │   │   │   ├── Launcher.java        # Entry point
│   │   │   │   └── Main.java            # Main JavaFX Application class
│   │   │   ├── interfaces/              # Core application interfaces
│   │   │   ├── module1.audioModel/      # Data structures for audio tracks
│   │   │   ├── module2.playlistModel/   # Playlist management and logic
│   │   │   ├── module3.storage/         # Data persistence and file I/O
│   │   │   ├── module4.playback/        # Audio engine and controls
│   │   │   ├── module5.util/            # General helper and utility classes
│   │   │   └── module-info.java         # Java module declarations
│   │   └── resources/                   # Static assets (FXML files, CSS, images)
│   └── test/                            # Unit tests
├── pom.xml                              # Maven configuration and dependencies
└── README.md                            # Project overview and run instructions
~~~

## Architecture

The system is split into 5 modules:

| Module | Class(es) | Description |
|--------|-----------|-------------|
| 1 – Audio Model | `AudioItem` | Represents a single track |
| 2 – Playlist | `Playlist` | Ordered collection of track IDs |
| 3 – Storage | `AudioStorage`, `PlaylistStorage`, `FileManager` | In-memory storage + JSON persistence |
| 4 – Playback | `Controller`, `AudioPlayer`, `PlaybackQueue` | Playback engine with facade API |
| 5 – Algorithms | `LibraryLogic` | Search, sort (MergeSort), and filter utilities |

> [!NOTE]
> **Data Persistence:** User library and playlist data is serialized via Gson and saved locally. Upon closing the application, look for the generated `library_data.json` and `playlists.json` files in the root execution directory.

Key data structures: `HashMap<String, AudioItem>` for O(1) lookups, `ArrayList` for the playback queue and playlist ordering.

## OOP and Data Structures

The following Object-Oriented Programming and DSA concepts were used in this project.
- **OOP:** Inheritance, Encapsulation, Abstraction (`Comparable`, `Searchable`, `Filterable` interfaces), Facade pattern (`Controller`)
- **Data Structures:** `HashMap`, `ArrayList`
- **Algorithms:** Linear Search, MergeSort

## Team Members

| Name | Student ID | Email |
|------|-----------|------|
| Tran Bach | V202502391 | 25bach.t@vinuni.edu.vn |
| Doan Duy Bao Kha | V202502083 | 25kha.ddb@vinuni.edu.vn |
| Nguyen Thi Diep Chi | V202502100 | 25chi.ntd@vinuni.edu.vn |
| Tran Hoang Minh | V202502416 | 25minh.th@vinuni.edu.vn |

## Acknowledgements

We acknowledge the following the following were used as inspiration for our project:

- [Spotify](https://www.spotify.com) (for UI inspiration)
- [wavelink](https://github.com/PythonistaGuild/Wavelink) (inspiration for the playback engine architecture)
- [lavaplayer](https://github.com/sedmelluq/lavaplayer) (inspiration for the playback engine architecturer)
- [Gemini](https://gemini.google.com) (utilized for architectural advice and debugging assistance)

---

*This project was developed as part of COMP1020 - Object-Oriented Programming and Data Structures, Spring 2026, VinUniversity.*
