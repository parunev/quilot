# Quilot - AI Interview Copilot 🚀

Hey there! 👋 Welcome to Quilot.

This project is something special to me. After a bit of a hiatus from coding, I wanted to dive back in and build something genuinely useful, challenging, and modern. The result is this application.

Quilot listens in on your interview call, transcribes what the interviewer says in real-time, and uses the power of Google's Gemini AI to give you smart, concise answer suggestions right when you need them. It's designed to be the ultimate safety net, helping you stay calm, confident, and prepared for any question that comes your way.

### What's in a Name?

The name **Quilot** is a blend of **Qu**estion and Co**pilot**. It reflects the project's core mission: to be your trusted copilot, ready to help you navigate any interview question.

## The Motivation: Why Build Quilot? 💡

While looking for interview prep tools, I noticed a big problem: the existing solutions are **incredibly expensive**. Many charge $30, $40, or even $50 for just a handful of 30-minute sessions. A single bad interview could cost you a significant amount of money.

I knew there had to be a smarter way.

The "brain" behind Quilot is its efficiency. Instead of blindly sending every spoken word to the AI, I built a custom, rule-based **Smart Question Detector**. This filter intelligently distinguishes between casual conversation and actual questions or commands (like "Explain...").

The result? A massive cost reduction. While other apps burn through your cash, Quilot is surgically precise, achieving costs as low as **\~$0.02 for 20 AI requests** (depending on the model used). It's not just about providing answers, it's about providing them intelligently and affordably.

## 🎥 Live Demo & Gallery

Check out a quick video of Quilot in action during a mock interview!

`[TO BE UPLOADED]`

### Screenshots

<p align="center">
  <img  alt="Screenshot 2025-08-07 at 13 39 18" src="https://github.com/user-attachments/assets/0ff92508-11eb-4f32-993c-3726c3d2d480" width="30%"/>
  <img  alt="Screenshot 2025-08-07 at 13 39 40" src="https://github.com/user-attachments/assets/a5ba968c-6c37-459a-ac90-32b162cd72db" width="30%"/>
  <img  alt="Screenshot 2025-08-07 at 13 39 55" src="https://github.com/user-attachments/assets/3416abc1-6c78-48e9-8ab8-9cb979e5346c" width="30%"/>
</p>

## Core Features

* **Real-Time Transcription**: Utilizes Google Cloud's Speech-to-Text API for live, streaming transcription of the interviewer's voice.
* **Intelligent AI Assistance**: Leverages Google Cloud's Vertex AI (Gemini) to generate high-quality, relevant answers to the interviewer's questions.
* **Smart Question Detection**: The custom filter that makes this app so cost-effective. It analyzes the transcript to only send actual questions and commands to the AI.
* **Interview History & Playback**: Automatically saves a complete record of each interview session—including a user-given title, the full audio recording, and all dialogue—to a local MySQL database. 🎧
* **Dynamic & Configurable UI**:
    * A detailed settings panel allows fine-tuning of all AI and STT parameters.
    * The UI intelligently adapts, enabling or disabling feature options (e.g., Punctuation, Diarization) based on the selected language's official support in the Google Cloud API.
* **Modern User Experience**:
    * Built with Java Swing but looks like a 2025 app thanks to the awesome **FlatLaf** Look and Feel.
    * A clean menu bar keeps the main window focused, and a dynamic, color-coded status bar tells you exactly what's happening at all times.

## 🏛️ Platform & System Requirements

**Heads up! This application was lovingly crafted and tested exclusively on macOS.** While the core Java logic is cross-platform, the audio capture magic is a bit specific.

### macOS (Primary Platform) 

* **macOS Sonoma (14.0)** or newer is recommended.
* **BlackHole Virtual Audio Driver** is **REQUIRED** to capture audio from apps like Zoom, Google Meet, etc. Don't worry, the setup guide below is super detailed!
* **Language Note:** While the application supports multiple languages, all primary development and testing were conducted using **English (United States) (en-US)**. This language currently offers the most comprehensive feature support across both the Speech-to-Text and Vertex AI APIs, ensuring the highest accuracy for features like automatic punctuation and question detection.

### Windows & Linux Users 🐧

* The app has **not been tested on Windows or Linux**.
* It will run, but you'll need to set up your own audio loopback solution. The most common equivalent to BlackHole on Windows is **VB-CABLE Virtual Audio Device**.

## 🏗️ Architectural Design & Principles

I built this project with a strong focus on writing clean, maintainable, and professional-grade code. Here are some of the key principles and patterns I used:

### SOLID Principles

* **Single Responsibility Principle (SRP)**: Every class has one job and does it well. Services handle APIs, DAOs handle the database, and UI Builders handle the visuals. Clean and simple.
* **Dependency Inversion Principle (DIP)**: The application relies on interfaces (`IAIService`, `AudioInputService`, etc.), not concrete classes. This makes the code flexible and easy to change—for example, swapping out Google Cloud for another AI provider would be a breeze.

### Design Patterns

* **Builder Pattern**: This pattern is a workhorse here!
    1.  **For the UI**: `UIBuilder` classes make constructing the complex main window manageable and clean.
    2.  **For Settings**: All configuration objects (`AIConfigSettings`, etc.) are **immutable** and created with a builder. This is a modern best practice that prevents bugs by making settings safe and predictable.
* **Data Access Object (DAO) Pattern**: The `InterviewDao` handles all the messy SQL, giving the rest of the app a clean and simple API to talk to the database.
* **Listener/Observer Pattern**: The heart of the app's real-time nature. It's used everywhere from button clicks to handling the asynchronous, streaming responses from the AI and STT services.
* **Singleton Pattern (Conceptual)**: The `DatabaseManager` ensures there's only one connection point to the database, which is efficient and safe.

## 🛠️ Tech Stack

* **Core Application**: Java 21+ with Swing
* **UI Styling**: [FlatLaf](https://www.formdev.com/flatlaf/)
* **AI Services**: Google Cloud Vertex AI (Gemini)
* **Speech-to-Text**: Google Cloud Speech-to-Text API
* **Database**: MySQL
* **Build Tool**: Maven
* **Testing**: JUnit 5

## 🚀 Getting Started

Here’s how to get it running.

### 1. Prerequisites

* **Java JDK 21** or newer.
* **Apache Maven** installed.
* A local **MySQL server** instance running.
* An IDE like **IntelliJ IDEA** or **Eclipse**.

### 2. macOS Audio Setup (BlackHole) - CRITICAL STEP!

To let the app "hear" your interview call, you need to set up a virtual audio device.

1.  **Install BlackHole:**
    * Download the latest **BlackHole 2ch** installer from the [official repository](https://github.com/ExistentialAudio/BlackHole/releases).
    * Run the `.pkg` installer. It's quick and easy.

2.  **Create a Multi-Output Device:**
    * Open the **"Audio MIDI Setup"** app on your Mac (it's in `Applications/Utilities`).
    * Click the **"+"** button in the bottom-left and choose **"Create Multi-Output Device"**.
    * In the new device's panel, check the boxes for both your **main speakers/headphones** and **"BlackHole 2ch"**.
    * **Super Important:** Make sure your main speakers/headphones are at the top of the list and that their "Drift Correction" box is checked.

3.  **Set Your Mac's Sound Output:**
    * Go to **System Settings > Sound**.
    * Under "Output", select the **"Multi-Output Device"** you just made. Now your Mac's sound will go to your headphones AND to BlackHole.

4.  **Set the App's Input:**
    * When you run Quilot, just select **"BlackHole 2ch"** from the "Input Device" dropdown. You're all set!

### 3. Clone the Repository

```bash
git clone [https://github.com/your-username/quilot.git](https://github.com/your-username/quilot.git)
cd quilot
```

### 4. Google Cloud Configuration

1.  **Create a Google Cloud Project:** If you don't have one, create one in the [Google Cloud Console](https://console.cloud.google.com/).
2.  **Enable APIs:** In your project, enable the **Vertex AI API** and the **Cloud Speech-to-Text API**.
3.  **Create a Service Account:**
    * Go to `IAM & Admin` > `Service Accounts`.
    * Create a new service account.
    * Grant it the roles of **Vertex AI User** and **Cloud Speech API User**.
    * Create a **JSON key** for the service account and download it. This is your magic key!

### 5. Application Setup

1.  **Open in IDE:** Open the project in your favorite IDE. It should automatically detect the `pom.xml` and download all the dependencies.
2.  **Run the App:** Find the `Main.java` file in `com.quilot.core` and run it.
3.  **First-Time Setup:**
    * The app will greet you and ask for your Google Cloud credentials. Go to `Settings > Credentials...` and select the JSON key file you downloaded.
    * The first time you hit "Start Input Capture", it will ask to set up the database. Just give it your local MySQL username and password, and it will create everything for you automatically.

---

## License

This project is licensed under the MIT License - see the [LICENSE.md](https://github.com/parunev/quilot/blob/main/LICENSE) file for details.
