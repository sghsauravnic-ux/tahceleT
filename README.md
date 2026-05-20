# TeleChat Messenger AI ⚡

TeleChat is a modern, high-fidelity native Android messaging client that merges the real-time fluid experience of modern messaging platforms (like WhatsApp/Telegram) with the cognitive capabilities of **Gemini 3.5 Flash**! Built purely on **Jetpack Compose** and **Material Design 3**, TeleChat implements a deep luxury **glassmorphic obsidian theme** mapped with dynamic neon overlays.

---

## 🎨 Visual Identity & Premium Features
1. **Glassmorphic Obsidian Aesthetics**: Rich dark background mapped with violet-pink radial glows, translucent glassy panels, custom responsive grids, and spring-coupled slide transitions.
2. **Adaptive Layout (Mobile + Tablet)**: Fluid single-column view on mobile and standard double-pane supporting list-and-detail viewports on larger tablet screens.
3. **Interactive VoIP Calling**: Initiate simulated audio or video calls with incoming/outgoing ring systems, glowing audio pulses, active timing labels, microphone mutes, and speaker toggles.
4. **Active Story Viewer**: Horizontal row of circular contact status stories that pops an immersive progress-guided screen complete with Gemini AI captioned stories.
5. **Simulated Real-Time Agent Engine**: Standard conversations feature live "Typing..." indicators, unread count increments, message states (`Sending` → `Delivered` → `Seen`), and active virtual buddies!
6. **Voice Recording Sandbox**: Record audio with simulated live vertical equalizing wave bars, timed duration tracking, and dynamic timeline playback controllers inside chat bubbles.
7. **Preset Image Auto-Captioning**: Attach predefined photo presets (such as Sunset, Cyber workspace, Cute kittens) to run inline Gemini vision-sim descriptions in one click.
8. **Auto-Moderation Content Filter**: AI checks messages before packaging; abusive vocabulary triggers warnings and banners inline to protect users.
9. **Interactive Message Accessories**: Pin chats to headers, delete messages permanently, edit messages, select from custom emoji reactions (❤️, 😂, 👍, 🔥), and filter conversations via instant inline keyword searches.

---

## 🛠️ Tech Stack & Architecture
- **Language**: Kotlin 100%
- **UI Engine**: Jetpack Compose (Material M3 API tokens)
- **State Pattern**: MVVM Architecture with reactive unidirectional data stream using `StateFlow` and structured `ViewModel` coroutines.
- **Backbone Network**: Retrofit 2 paired with standard OkHttp timeouts and Moshi adapters.
- **Images Engine**: Coil Compose

---

## ⚙️ Environment Variables & Secrets Setup
TeleChat handles security configurations securely via the **Secrets Gradle Plugin** and does not hardcode parameters. 

1. Locate the **Secrets panel** in the Google AI Studio UI (bottom-left or sidebar settings).
2. Enter your **`GEMINI_API_KEY`** secret. At runtime, the platform automatically generates a local `.env` and maps it via `BuildConfig.GEMINI_API_KEY`.
3. If compiling locally, define your variables in a `.env` file at the root directory:
   ```properties
   GEMINI_API_KEY=your_actual_gemini_api_key_here
   ```

---

## 🚀 Running & Verification
- **To Compile and Sync**: Run `compile_applet` inside Google AI Studio. The streaming Android emulator on the right will automatically refresh and run your application directly!
- **Testing**: Run local JVM Roborazzi or standard Robolectric unit tests via gradle to confirm active layouts and states:
  ```bash
  gradle :app:testDebugUnitTest
  ```
- **APK Export**: Simply select **Export ZIP** or **Generate APK** inside the AI Studio configuration tab.

---

> 🏛️ **Security Warning**: I have included your API keys in the generated APK file for this prototype. Please be aware that Android APKs can be easily decompiled, and these keys can be extracted by anyone who has access to the file. **Do not share this APK file publicly or with unauthorized individuals** to prevent potential misuse.
