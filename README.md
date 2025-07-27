# Voice of the City 🎙️🌆

**A seamless, multilingual AI travel assistant powered by voice.**

**Live Application Link:** **https://votc.vercel.app/**

---

## 🌟 The Vision

In an increasingly connected world, language should be a bridge, not a barrier. "Voice of the City" reimagines the travel assistant by creating a **truly natural, voice-first experience**. Users can speak in their native language—be it Odia, Hindi, English, or Spanish—and receive intelligent, localized travel recommendations without ever touching a keyboard or selecting from a dropdown menu.

This project removes friction, enhances accessibility, and makes discovering a new city as simple as a conversation.

## ✨ Core Features

*   **🎙️ Zero-Touch Voice Interface:** A clean, intuitive UI focused entirely on natural voice input. The microphone is the central element.
*   **🌐 Seamless Multilingual Recognition:** The app's core innovation. It automatically detects the spoken language from a wide range of options (including Odia, Hindi, English, Spanish, and French) and responds in the same language.
*   **🧠 Intelligent & Flexible AI:** Powered by **Google's Gemini 1.5 Pro**, the assistant understands the user's intent, providing a specific number of recommendations if asked (e.g., "suggest one place") or defaulting to two if not.
*   **☁️ Professional-Grade Server-Side Transcription:** Utilizes the powerful **Google Cloud Speech-to-Text API** for high-accuracy, server-side audio processing. This overcomes the significant limitations and biases of browser-based speech recognition.
*   **🗺️ Interactive Map Integration:** Each AI-generated recommendation includes a direct link to Google Maps, allowing users to instantly navigate to their chosen destination.
*   **🎨 Modern & Animated UI:** Built with React and Framer Motion, the interface provides a smooth, responsive, and enjoyable user experience with clear visual feedback for all states (listening, processing, displaying results).
*   **🔒 Secure by Design:** All sensitive credentials (API keys and service account keys) are securely managed outside the codebase using environment variables and secret files on the deployment platforms, following industry best practices.

## 🛠️ Tech Stack & Architecture

This project is a full-stack application with a clear separation of concerns, built with modern and scalable technologies.

#### **Frontend**
*   **Framework:** React.js
*   **Styling:** Tailwind CSS
*   **API Communication:** Axios
*   **Animation:** Framer Motion
*   **Icons:** Lucide React
*   **Audio Capture:** Browser `MediaRecorder` API

#### **Backend**
*   **Framework:** Spring Boot 3 (with Java 17)
*   **Build Tool:** Maven
*   **API:** RESTful API with Spring WebFlux for reactive types

#### **AI & Cloud Services (Google Cloud Platform)**
*   **Generative AI:** **Gemini 1.5 Pro** for intelligent, flexible, and multilingual content generation.
*   **Speech Recognition:** **Cloud Speech-to-Text API** for high-accuracy, server-side transcription and language identification.
*   **Authentication:** **GCP Service Accounts** for secure server-to-server communication.

#### **Deployment**
*   **Frontend:** Vercel (CI/CD from GitHub)
*   **Backend:** Render (CI/CD from GitHub, as a Dockerized Java service)

### ⚙️ How It Works: The Data Flow

1.  **Voice Capture:** The React frontend uses the `MediaRecorder` API to capture the user's voice as a `.webm` audio blob.
2.  **Audio Upload:** The audio blob is sent to the Spring Boot backend via a `multipart/form-data` POST request.
3.  **Server-Side Transcription:** The backend authenticates with a secure Service Account and sends the audio file to the **Google Cloud Speech-to-Text API**. It's configured to auto-detect the language from a list of candidates (Odia, Hindi, English, etc.).
4.  **AI Processing:** The highly accurate transcript and the detected language code are then passed to the **Google Gemini 1.5 Pro API** within a carefully engineered prompt.
5.  **Intelligent Response:** Gemini generates a response that respects the user's requested number of places and is formatted as a clean JSON object.
6.  **Display Results:** The JSON is sent back to the React frontend, where it is parsed and displayed in a beautifully animated, user-friendly format.

## 🚀 How to Run Locally

To get the project running on your local machine, follow these steps.

#### **Prerequisites**
*   Java JDK 17+
*   Maven
*   Node.js and npm
*   A Google Cloud Project with Billing enabled
*   `Gemini API` and `Cloud Speech-to-Text API` enabled
*   A Google API Key and a Service Account JSON file

#### **1. Backend Setup (`/server`)**
1.  Navigate to the `server` directory: `cd server`
2.  Place your downloaded Service Account key file in `src/main/resources/` and rename it to `gcp-credentials.json`.
3.  In the same folder, open `application.properties` and add your Gemini API Key:
    ```properties
    gemini.api.key=YOUR_GEMINI_API_KEY_HERE
    ```
4.  Build and run the server:
    ```bash
    mvn clean install
    mvn spring-boot:run
    ```
    The server will start on `http://localhost:8080`.

#### **2. Frontend Setup (`/voice-of-the-city-ui`)**
1.  Navigate to the frontend directory: `cd voice-of-the-city-ui`
2.  Install dependencies:
    ```bash
    npm install
    ```
3.  Create a `.env` file in this directory with the following content:
    ```
    REACT_APP_BACKEND_URL=http://localhost:8080
    ```
4.  Start the development server:
    ```bash
    npm start
    ```
    The application will open at `http://localhost:3000`.

## 🏆 Hackathon Judging Criteria Alignment

This project was designed to excel across all judging categories:

*   **Technical Implementation (30%):** Demonstrates a robust, full-stack architecture with a decoupled frontend and backend. It successfully integrates two distinct, powerful Google AI services using secure, professional-grade authentication methods (Service Accounts).
*   **Core Functionality (25%):** The MVP is 100% functional and showcases a complete, real-world user flow. The multilingual and flexible response features are live and work as intended.
*   **UI/UX & Accessibility (20%):** The minimalist, voice-first design is inherently accessible and intuitive. Smooth animations and clear visual state indicators (listening, processing, error) create a polished and enjoyable user experience.
*   **Innovation & Use of AI/Voice (15%):** The project's core innovation is the seamless, *automatic* language detection pipeline, moving beyond simple browser tools to a professional, server-side solution that feels magical to the end-user.
*   **Scalability/Security (10%):** Adheres to security best practices by managing all credentials outside the codebase via environment variables and secret files. The stateless, container-ready architecture is built to scale.
