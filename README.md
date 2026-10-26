# Fix Grammar (Android)

Adds a **Fix Grammar** item to Android's text-selection menu (next to Copy/Paste).
The selected text is sent to the OpenAI API with a "fix grammar" prompt and the
corrected text replaces the selection. In read-only places (web pages, etc.) the
result is shown with a Copy button.

## Build

No local tools needed: every push to `main` runs GitHub Actions
(`.github/workflows/build-apk.yml`) and publishes `fix-grammar.apk` to the
**latest** release.

## Setup on the phone

1. Install `fix-grammar.apk` (allow "Install unknown apps" for your browser/file manager).
2. Open **Fix Grammar**, paste your OpenAI API key, Save.
3. In any app, select text → **Fix Grammar** (may be under ⋮ / More).

The API key is stored only in the app's private storage on the phone.
The signing key in `app/fixgrammar.keystore` is for personal sideloading only.
