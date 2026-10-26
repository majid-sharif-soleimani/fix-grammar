package se.majid.fixgrammar;

import android.app.Activity;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

/** Settings screen. The actual work happens from the text-selection menu. */
public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Settings settings = new Settings(this);
        int pad = (int) (16 * getResources().getDisplayMetrics().density);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(pad, pad, pad, pad);

        TextView intro = new TextView(this);
        intro.setText("Select text in any app and choose \"Fix Grammar\" from the menu "
                + "(it may be under ⋮ / More). The corrected text replaces your selection.");
        intro.setTextSize(15);
        intro.setPadding(0, 0, 0, pad);
        root.addView(intro);

        root.addView(label("OpenAI API key"));
        EditText apiKey = new EditText(this);
        apiKey.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        apiKey.setHint("sk-...");
        apiKey.setText(settings.apiKey());
        root.addView(apiKey);

        root.addView(label("Model"));
        EditText model = new EditText(this);
        model.setSingleLine(true);
        model.setHint(Settings.DEFAULT_MODEL);
        model.setText(settings.model());
        root.addView(model);

        root.addView(label("Prompt"));
        EditText prompt = new EditText(this);
        prompt.setMinLines(4);
        prompt.setText(settings.prompt());
        root.addView(prompt);

        Button save = new Button(this);
        save.setText("Save");
        save.setOnClickListener(v -> {
            settings.save(apiKey.getText().toString(), model.getText().toString(),
                    prompt.getText().toString());
            Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
        });
        root.addView(save);

        Button reset = new Button(this);
        reset.setText("Reset prompt to default");
        reset.setOnClickListener(v -> prompt.setText(Settings.DEFAULT_PROMPT));
        root.addView(reset);

        ScrollView scroll = new ScrollView(this);
        scroll.addView(root);
        setContentView(scroll);
    }

    private TextView label(String text) {
        TextView t = new TextView(this);
        t.setText(text);
        t.setTextSize(13);
        t.setPadding(0, (int) (12 * getResources().getDisplayMetrics().density), 0, 0);
        return t;
    }
}
