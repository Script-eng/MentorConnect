# MentorConnect - Student Assistance App

MentorConnect is an Android application designed to help new university students navigate paperwork, locate administrative offices, and connect with mentors for personalized guidance. This app is built as a course project for **Developing Mobile Applications 2 (Android)**.

## 📱 Features

- 🔍 **Mentor Directory**
  - Browse, search, and filter mentors by name, department, or expertise.
  - Mark mentors as favorites for quick access.
  - Display mentor profiles with image and contact details.

- 💬 **In-App Chat System**
  - Real-time messaging using Firebase Firestore or REST API.
  - Offline support using Room Database.
  - Chat interface built with RecyclerView.

- 🗺️ **Navigation Assistance**
  - Integration with Google Maps API.
  - View important university office locations.
  - Check office hours and get step-by-step directions.
  - Save frequently visited locations as favorites.

- 📄 **Document Guide**
  - List of required paperwork for various student services.
  - Step-by-step instructions for document submission.
  - Searchable guide with a RecyclerView interface.

## 🧰 Tech Stack

| Category         | Tools / Technologies                         |
|------------------|----------------------------------------------|
| Language         | Java                                         |
| IDE              | Android Studio                               |
| UI Layout        | ConstraintLayout                             |
| Architecture     | Single Activity with Multiple Fragments      |
| Networking       | Firebase Firestore or Retrofit/Volley        |
| Local Storage    | Room Database                                |
| Media Handling   | Glide                                        |
| Navigation       | Bottom Navigation Bar / Navigation Drawer    |
| Map Integration  | Google Maps API                              |
| Version Control  | GitHub                                       |

## 📂 Project Structure
<pre lang="markdown">
```
MentorConnect/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/mentorconnect/
│   │   │   │   ├── activities/                # MainActivity and utility activities
│   │   │   │   ├── fragments/                 # Home, Chat, MentorList, DocumentGuide, Map, etc.
│   │   │   │   ├── adapters/                  # RecyclerView Adapters
│   │   │   │   ├── models/                    # Data models (Mentor, Message, Document, etc.)
│   │   │   │   ├── network/                   # Retrofit setup or Firestore logic
│   │   │   │   ├── repository/                # Data access layer
│   │   │   │   ├── utils/                     # Helper classes (e.g., Constants, Permissions)
│   │   │   ├── res/
│   │   │   │   ├── layout/                    # XML UI files
│   │   │   │   ├── drawable/                  # Images, icons, etc.
│   │   │   │   ├── values/                    # Strings, colors, styles
│   │   │   └── AndroidManifest.xml
├── build.gradle
├── README.md
└── .github/
    └── workflows/                            # GitHub Actions for CI/CD
        └── android-ci.yml
```
</pre>
## 🚀 Installation Guide

1. **Clone the repository**  
   ```bash
   git clone https://github.com/Script-eng/MentorConnect.git

🧑‍🏫 Instructor
	•	Name: [Bola Kolman]
	•	GitHub Role: Reporter (project supervision)

👨‍💻 Contributors
	•	[Salah Ben Sarar]
	•	[Lesalon Stephan]

📃 License

This project is for educational purposes only as part of the Developing Mobile Applications 2 course. All rights reserved to the authors.

