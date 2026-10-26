package se.majid.fixgrammar;

import android.content.Context;
import android.content.SharedPreferences;

/** Stores the user's API key, model and prompt in private app storage. */
final class Settings {
    static final String DEFAULT_MODEL = "gpt-4o-mini";
    static final String DEFAULT_PROMPT =
            "Fix the grammar, spelling and punctuation of the text the user sends. "
            + "Keep the original language, meaning, tone, line breaks and formatting. "
            + "Do not add explanations, quotes or comments. Reply with the corrected text only.";

    private static final String FILE = "settings";
    private static final String KEY_API = "api_key";
    private static final String KEY_MODEL = "model";
    private static final String KEY_PROMPT = "prompt";

    private final SharedPreferences prefs;

    Settings(Context context) {
        prefs = context.getSharedPreferences(FILE, Context.MODE_PRIVATE);
    }

    String apiKey() { return prefs.getString(KEY_API, "").trim(); }

    String model() {
        String m = prefs.getString(KEY_MODEL, "").trim();
        return m.isEmpty() ? DEFAULT_MODEL : m;
    }

    String prompt() {
        String p = prefs.getString(KEY_PROMPT, "").trim();
        return p.isEmpty() ? DEFAULT_PROMPT : p;
    }

    void save(String apiKey, String model, String prompt) {
        prefs.edit()
                .putString(KEY_API, apiKey.trim())
                .putString(KEY_MODEL, model.trim())
                .putString(KEY_PROMPT, prompt.trim())
                .apply();
    }
}
