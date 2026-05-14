Namma Hasiru 🌱
Namma Hasiru is a feature-rich, open-source Android application designed to track and manage tree plantation drives. It provides users with an eco-friendly UI to log their planted saplings, visualize them on a map, get background watering reminders, and use ML to recognize plant types.

🚀 Features
Authentication: Secure entry with Firebase Google Sign-In.
Plantation Dashboard: View key statistics of your plantation activities locally persisted via Room Database.
Interactive Maps: Real-time integration with Google Maps API and FusedLocationProviderClient to accurately drop pins and track the locations of planted saplings.
Machine Learning Integration: Built-in camera feature utilizing CameraX and Google ML Kit Image Labeling for instant plant identification.
Smart Reminders: Automated background tasks scheduled through Android WorkManager to remind users to water their plants and update their status.
Modern UI: Completely built with Jetpack Compose featuring a cohesive, eco-themed visual identity (Dark & Light modes supported).
🛠️ Tech Stack & Architecture
Language: Kotlin
Architecture: MVVM (Model-View-ViewModel)
UI Toolkit: Jetpack Compose
Local Data Persistence: Room Database
Background Tasks: WorkManager
Mapping: Google Maps Compose & Play Services Location
Machine Learning: CameraX + ML Kit Image Labeling
Authentication: Firebase Auth + Google Identity Services
Image Loading: Coil
📦 Building from Source
Prerequisites
Android Studio Iguana (or newer).
Java SDK 17+.
A Firebase Project configured for Android.
Google Maps API Key.
Setup Instructions
Clone the repository:
git clone https://github.com/your-username/Namma-Hasiru.git
Add Firebase Credentials:
Go to the Firebase Console.
Download the google-services.json file for your registered Android app.
Place the google-services.json file in the app/ directory of the project.
Configure Maps API Key:
Go to the Google Cloud Console and generate a Maps SDK for Android API Key.
Open local.properties in your project root and add:
MAPS_API_KEY=YOUR_API_KEY_HERE
Build and Run:
Open the project in Android Studio.
Sync the project with Gradle files.
Select an emulator or physical device and click Run.
