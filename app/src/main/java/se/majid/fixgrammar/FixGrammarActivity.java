package se.majid.fixgrammar;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Receives the selected text via ACTION_PROCESS_TEXT, sends it to OpenAI and
 * returns the corrected text so the app replaces the selection.
 * If the source text is read-only (e.g. a web page), the result is shown and can be copied.
 */
public class FixGrammarActivity extends Activity {

    private AlertDialog progress;
    private volatile boolean cancelled;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent in = getIntent();
        if (ACTION_FIX_CLIPBOARD.equals(in.getAction())) {
            // Android only lets an app read the clipboard while it has focus,
            // so wait for onWindowFocusChanged before reading it.
            clipboardMode = true;
            return;
        }

        final boolean shared = Intent.ACTION_SEND.equals(in.getAction());
        CharSequence selected = shared
                ? in.getCharSequenceExtra(Intent.EXTRA_TEXT)
                : in.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT);
        // Shared text can't be replaced in place, so treat it as read-only (show + copy).
        final boolean readOnly = shared
                || in.getBooleanExtra(Intent.EXTRA_PROCESS_TEXT_READONLY, false);
        start(selected, readOnly);
    }

    static final String ACTION_FIX_CLIPBOARD = "se.majid.fixgrammar.FIX_CLIPBOARD";
    private boolean clipboardMode;
    private boolean clipboardRead;

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (!clipboardMode || clipboardRead || !hasFocus) return;
        clipboardRead = true;
        CharSequence text = null;
        ClipboardManager cm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        ClipData clip = cm.getPrimaryClip();
        if (clip != null && clip.getItemCount() > 0) {
            text = clip.getItemAt(0).coerceToText(this);
        }
        if (text == null || text.toString().trim().isEmpty()) {
            Toast.makeText(this, "Clipboard is empty. Copy some text first.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        start(text, true);
    }

    private void start(CharSequence selected, final boolean readOnly) {
        if (selected == null || selected.toString().trim().isEmpty()) {
            finish();
            return;
        }

        final Settings settings = new Settings(this);
        if (settings.apiKey().isEmpty()) {
            Toast.makeText(this, "Open the Fix Grammar app and enter your OpenAI API key first.",
                    Toast.LENGTH_LONG).show();
            startActivity(new Intent(this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            finish();
            return;
        }

        showProgress();

        final String text = selected.toString();
        final Handler main = new Handler(Looper.getMainLooper());
        new Thread(() -> {
            try {
                String fixed = OpenAiClient.fix(settings.apiKey(), settings.model(), settings.prompt(), text);
                main.post(() -> onResult(fixed, readOnly));
            } catch (Exception e) {
                main.post(() -> onError(e.getMessage()));
            }
        }).start();
    }

    private void showProgress() {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.HORIZONTAL);
        int pad = (int) (24 * getResources().getDisplayMetrics().density);
        box.setPadding(pad, pad, pad, pad);
        box.addView(new ProgressBar(this));
        TextView label = new TextView(this);
        label.setText("  Fixing grammar…");
        label.setTextSize(16);
        box.addView(label);

        progress = new AlertDialog.Builder(this)
                .setView(box)
                .setOnCancelListener(d -> {
                    cancelled = true;
                    finish();
                })
                .show();
    }

    private void onResult(String fixed, boolean readOnly) {
        if (cancelled || isFinishing()) return;
        dismissProgress();

        if (!readOnly) {
            Intent result = new Intent();
            result.putExtra(Intent.EXTRA_PROCESS_TEXT, fixed);
            setResult(RESULT_OK, result);
            finish();
            return;
        }

        // Copy right away so the user can paste it back immediately.
        ClipboardManager clip = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        clip.setPrimaryClip(ClipData.newPlainText("Fixed text", fixed));

        new AlertDialog.Builder(this)
                .setTitle("Corrected text (copied)")
                .setMessage(fixed)
                .setPositiveButton("Copy", (d, w) -> {
                    ClipboardManager cm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                    cm.setPrimaryClip(ClipData.newPlainText("Fixed text", fixed));
                    Toast.makeText(this, "Copied", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .setNegativeButton("Close", (d, w) -> finish())
                .setOnCancelListener(d -> finish())
                .show();
    }

    private void onError(String message) {
        if (cancelled || isFinishing()) return;
        dismissProgress();
        Toast.makeText(this, "Fix Grammar failed: " + message, Toast.LENGTH_LONG).show();
        setResult(RESULT_CANCELED);
        finish();
    }

    private void dismissProgress() {
        if (progress != null && progress.isShowing()) progress.dismiss();
    }

    @Override
    protected void onDestroy() {
        dismissProgress();
        super.onDestroy();
    }
}
