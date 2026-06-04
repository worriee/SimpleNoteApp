# AI Notes

<p>
    <img src="https://badgen.net/badge/version/1.7/blue" height="30" />
    <img src="https://badgen.net/badge/license/MIT/red" height="30" />
    <img src="https://badgen.net/badge/native/Android/orange" height="30" />
</p>

**Transform YouTube videos into structured study notes instantly.**

AI Notes is a lightweight Android application designed for students, researchers, and lifelong learners. Instead of spending hours re-watching videos and manually typing notes, this app converts them clean notes for you.

> this was previously made from android sutdio but my laptop couldn't handle it anymore therefore I can't generate a signed apk for this.

---

### System Flow

```text
[ User ]
    │
    ▼
[ Android App ] ───────► [ Vercel Serverless Proxy ] ───────► [ RapidAPI ]
                                     │                           │
                                     │                           ▼
                                     │                   (Fetches Captions)
                                     │                           │
                                     ▼                           │
                            [ Google Gemini AI ] ◄───────────────┘
                                     │
                                     ▼
                          (Generates Structured Notes)
                                     │
                                     ▼
                            [ Android App ] ───────► [ Saved as Note ]
```

---

## Key Features

- **Instant Summarization**: Convert any YouTube video into a structured note with one click.
- **Academic Formatting**: Notes are delivered with ALL CAPS headings and clean bullet points for maximum readability.
- **Smart Organization**:
  - **Main Notes**: Your active study list.
  - **Archive**: Keep your workspace clean by archiving old notes.
  - **Recently Deleted**: A safety net to recover notes you deleted by mistake.
- **Adaptive UI**: Full support for Light and Dark modes to protect your eyes during late-night study sessions.
- **Local Persistence**: All your notes are saved locally on your device using encrypted JSON storage.

---

## Tech Stack

- **Frontend**: Java, Android SDK 34, Material Design.
- **Backend**: Node.js (Vercel Serverless Functions).
- **AI Engine**: Google Gemini 3.1 Flash Lite
- **Data Handling**: OkHttp for networking, Gson for serialization.

---

## Getting Started

### For Users

1. [Install](https://github.com/worriee/SimpleNoteApp/releases) the APK on your Android device.
2. Paste a YouTube URL.
3. Watch your study notes appear automatically!

---

**Built for efficiency. Designed for learners.** 🎓

[_worrie_](https://github.com/worriee)
