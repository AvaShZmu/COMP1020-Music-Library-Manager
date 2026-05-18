# COMP1020-Music-Library-Manager
A desktop application for organizing, managing, and playing your personal music collection. Built with Java and JavaFX for COMP1020 – Object-Oriented Programming & Data Structures.
## 👥 Team (Group 14)

| Name | Student ID | Role |
|------|-----------|------|
| Tran Bach | V202502391 | Team Lead, Storage & Playback |
| Doan Duy Bao Kha | V202502083 | GUI |
| Nguyen Thi Diep Chi | V202502100 | Playlist Model & Algorithms |
| Tran Hoang Minh | V202502416 | Audio Model & Metadata |

## ✨ Features

- Add, remove, and browse audio tracks
- Search by title or artist; sort and filter your library
- Create and manage playlists
- Audio playback with play/pause/skip/volume controls
- Persistent storage — your library saves between sessions

## 🛠️ Tech Stack

- **Java + JavaFX** — UI and audio playback
- **Gson** — JSON-based data persistence
- **jAudioTagger** — Audio metadata extraction
- **Maven** — Build and dependency management

## 🚀 Getting Started

**Prerequisites:** Java 17+, Maven

```bash
git clone https://github.com/AvaShZmu/COMP1020-Music-Library-Manager.git
cd COMP1020-Music-Library-Manager
mvn clean javafx:run
```

## 📁 Project Structure
~~~
COMP1020-Music-Library-Manager/
├── src/
│   └── main/
│       └── java/
│           ├── module-info.java
│           ├── GUI/
│           │   └── controller/
│           ├── module1.audioModel/
│           ├── module2.playlistModel/
│           ├── module3.storage/
│           ├── module4.playback/
│           └── module5.util/
├── .gitignore
├── pom.xml
└── README.md
~~~
## 🏗️ Architecture

The system is split into 5 modules:

| Module | Class(es) | Description |
|--------|-----------|-------------|
| 1 – Audio Model | `AudioItem` | Represents a single track |
| 2 – Playlist | `Playlist` | Ordered collection of track IDs |
| 3 – Storage | `AudioStorage`, `PlaylistStorage`, `FileManager` | In-memory storage + JSON persistence |
| 4 – Playback | `Controller`, `AudioPlayer`, `PlaybackQueue` | Playback engine with facade API |
| 5 – Algorithms | `LibraryLogic` | Search, sort (MergeSort), and filter utilities |

Key data structures: `HashMap<String, AudioItem>` for O(1) lookups, `ArrayList` for the playback queue and playlist ordering.

## 🎓 OOP & DSA Concepts Demonstrated

- **OOP:** Inheritance, Encapsulation, Abstraction (`Comparable`, `Searchable`, `Filterable` interfaces), Facade pattern (`Controller`)
- **Data Structures:** `HashMap`, `ArrayList`
- **Algorithms:** Linear Search, MergeSort
