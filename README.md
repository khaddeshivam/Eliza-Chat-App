<h1 align="center">📱 Eliza Messenger</h1>
<p align="center">
  A modern, AI-powered real-time chat application built in <b>Java</b> for Android.  
  <br/>
  Combining <b>Firebase Cloud</b> with offline-first architecture, secure authentication, and an elegant UI,  
  Eliza delivers a <b>WhatsApp-like experience</b> enhanced with <b>voice-driven chat</b>.
</p>

---

## ✨ Features

- 🔥 **Real-Time Messaging** – Instant chat powered by Firebase Firestore.  
- 🗣️ **Talk-to-Chat Mode** – Speech-to-text input for hands-free conversations.  
- 📦 **Offline First** – Messages cached in Room DB and auto-synced when online.  
- 🔐 **Authentication** – Firebase Auth with email, password, and session management.  
- 🎨 **Modern UI/UX** – Material Design, Dark/Light mode, animations, and gestures.  
- 📸 **Media Sharing** – Support for images, files, and multimedia messages.  
- 🔔 **Push Notifications** – Stay connected with real-time alerts.

---

## 🏗️ Architecture

Eliza Messenger follows **MVVM** with clean separation of concerns.


Presentation Layer → View (XML, Activities/Fragments)

Business Logic    → ViewModel + LiveData

Data Layer        → Repository + Firebase + Room DB


### Tech Stack
- **Language**: Java  
- **Architecture**: MVVM, Repository Pattern  
- **Backend**: Firebase (Auth, Firestore, Cloud Storage)  
- **Local Storage**: Room Database  
- **UI**: Material Components, RecyclerView, Animations  
- **Extras**: Speech Recognition API, Firebase Cloud Messaging (FCM)

---

## 🚀 Getting Started

### 1. Clone the Repository

git clone https://github.com/khaddeshivam/Eliza-Chat-App.git

### 2. Open in Android Studio

* Sync Gradle dependencies automatically.

### 3. Firebase Setup

* Create a Firebase project.
* Download `google-services.json` and place it inside the `/app` directory.
* Enable **Authentication**, **Firestore**, and **Cloud Storage** in Firebase Console.

### 4. Run the App

* Use a real Android device or emulator.
* Login / Sign-up and start chatting 🎉

---

## 📸 Screenshots (UI Preview)

<p align="center">
  <img src="assets/screenshot1.png" width="250" />
  <img src="assets/screenshot2.png" width="250" />
  <img src="assets/screenshot3.png" width="250" />
</p>

---

## 🌟 Why Eliza?

Eliza Messenger is my capstone Android project demonstrating **end-to-end mobile development in Java**:

* Building a scalable chat app with **real-time cloud sync**.
* Designing **offline-first features** with seamless recovery.
* Integrating **AI-driven voice input** for better accessibility.
* Delivering a **production-quality UI/UX** inspired by WhatsApp & Discord.

It represents my ability to **design, architect, and implement complex apps** — skills directly applicable in real-world product engineering.

---

## 🛠️ Future Improvements

* End-to-End Encryption for chats
* Video/Voice Calls (WebRTC)
* Group Chats & Channels
* Smart AI Assistant for contextual replies

---

## 👨‍💻 Author

**Shivam Khadde**
💼 [LinkedIn](https://linkedin.com/in/khaddeshivam)
💻 [GitHub](https://github.com/khaddeshivam)

---

⭐ If you like this project, feel free to **star** the repo and share your feedback!
