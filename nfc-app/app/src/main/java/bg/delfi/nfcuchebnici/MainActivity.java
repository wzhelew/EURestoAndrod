package bg.delfi.nfcuchebnici;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.text.InputType;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends Activity implements TextToSpeech.OnInitListener {
    private static final String PREFS = "nfc_books";
    private static final String KEY_SPEAK = "__speak_enabled";
    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;
    private SharedPreferences prefs;
    private TextView titleView, uidView, statusView;
    private Switch speakSwitch;
    private TextToSpeech tts;
    private boolean ttsReady = false;
    private boolean waitingForNewTag = false;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        tts = new TextToSpeech(this, this);
        Intent intent = new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) flags |= PendingIntent.FLAG_MUTABLE;
        pendingIntent = PendingIntent.getActivity(this, 0, intent, flags);
        buildUi();
        checkNfcState();
        handleNfcIntent(getIntent());
    }

    private void buildUi() {
        int pad = dp(18);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(pad, pad, pad, pad);
        root.setGravity(Gravity.CENTER_HORIZONTAL);

        TextView header = new TextView(this);
        header.setText("NFC Учебници"); header.setTextSize(24); header.setTypeface(Typeface.DEFAULT, Typeface.BOLD); header.setGravity(Gravity.CENTER);
        root.addView(header, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        statusView = new TextView(this);
        statusView.setText("Доближи учебника до телефона"); statusView.setTextSize(17); statusView.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT); sp.topMargin = dp(14);
        root.addView(statusView, sp);

        titleView = new TextView(this);
        titleView.setText("—"); titleView.setTextSize(44); titleView.setTypeface(Typeface.DEFAULT, Typeface.BOLD); titleView.setGravity(Gravity.CENTER); titleView.setMinHeight(dp(180));
        LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f); tp.topMargin = dp(12);
        root.addView(titleView, tp);

        uidView = new TextView(this); uidView.setTextSize(13); uidView.setGravity(Gravity.CENTER);
        root.addView(uidView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        Button newTagButton = new Button(this); newTagButton.setText("Нов / преименувай таг");
        newTagButton.setOnClickListener(v -> { waitingForNewTag = true; statusView.setText("Доближи NFC тага"); titleView.setText("ЧАКАМ ТАГ..."); uidView.setText(""); });
        LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT); bp.topMargin = dp(10); root.addView(newTagButton, bp);

        Button listButton = new Button(this); listButton.setText("Учебници"); listButton.setOnClickListener(v -> showSavedBooks());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT); lp.topMargin = dp(8); root.addView(listButton, lp);

        speakSwitch = new Switch(this); speakSwitch.setText("Говори името при сканиране"); speakSwitch.setChecked(prefs.getBoolean(KEY_SPEAK, true));
        speakSwitch.setOnCheckedChangeListener((b, c) -> prefs.edit().putBoolean(KEY_SPEAK, c).apply());
        LinearLayout.LayoutParams swp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT); swp.topMargin = dp(8); root.addView(speakSwitch, swp);
        setContentView(root);
    }

    private void checkNfcState() {
        if (nfcAdapter == null) statusView.setText("Този телефон няма NFC");
        else if (!nfcAdapter.isEnabled()) statusView.setText("NFC е изключено. Включи го от настройките.");
    }

    @Override protected void onResume() { super.onResume(); if (nfcAdapter != null) nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null); }
    @Override protected void onPause() { super.onPause(); if (nfcAdapter != null) nfcAdapter.disableForegroundDispatch(this); }
    @Override protected void onNewIntent(Intent intent) { super.onNewIntent(intent); setIntent(intent); handleNfcIntent(intent); }

    private void handleNfcIntent(Intent intent) {
        if (intent == null) return;
        String action = intent.getAction();
        if (!NfcAdapter.ACTION_TAG_DISCOVERED.equals(action) && !NfcAdapter.ACTION_TECH_DISCOVERED.equals(action) && !NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) return;
        Tag tag;
        if (Build.VERSION.SDK_INT >= 33) tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG, Tag.class);
        else tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (tag == null) return;
        String uid = bytesToHex(tag.getId()); uidView.setText("UID: " + uid);
        String name = prefs.getString(uid, null);
        if (waitingForNewTag || name == null) { waitingForNewTag = false; askForName(uid, name); }
        else showBook(name, uid);
    }

    private void askForName(String uid, String oldName) {
        EditText input = new EditText(this); input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES); input.setSingleLine(true); input.setHint("например Химия");
        if (oldName != null) { input.setText(oldName); input.setSelection(input.getText().length()); }
        LinearLayout box = new LinearLayout(this); box.setPadding(dp(20), 0, dp(20), 0); box.addView(input, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        AlertDialog dialog = new AlertDialog.Builder(this).setTitle(oldName == null ? "Нов NFC таг" : "Преименуване").setMessage("UID: " + uid + "\nВъведи името на учебника:").setView(box).setNegativeButton("Отказ", null).setPositiveButton("Запази", null).create();
        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String name = input.getText().toString().trim(); if (name.isEmpty()) { input.setError("Напиши име"); return; }
            prefs.edit().putString(uid, name).apply(); dialog.dismiss(); showBook(name, uid);
        }));
        dialog.show();
    }

    private void showBook(String name, String uid) {
        statusView.setText("Разпознат учебник"); titleView.setText(name.toUpperCase(new Locale("bg", "BG"))); uidView.setText("UID: " + uid);
        if (speakSwitch.isChecked() && ttsReady) tts.speak(name, TextToSpeech.QUEUE_FLUSH, null, "book_name");
    }

    private void showSavedBooks() {
        List<BookItem> books = getBooks(); if (books.isEmpty()) { Toast.makeText(this, "Още няма записани учебници", Toast.LENGTH_SHORT).show(); return; }
        ScrollView scroll = new ScrollView(this); LinearLayout list = new LinearLayout(this); list.setOrientation(LinearLayout.VERTICAL); list.setPadding(dp(14), dp(8), dp(14), dp(8)); scroll.addView(list);
        AlertDialog dialog = new AlertDialog.Builder(this).setTitle("Учебници").setView(scroll).setNegativeButton("Затвори", null).create();
        for (BookItem book : books) {
            LinearLayout row = new LinearLayout(this); row.setOrientation(LinearLayout.HORIZONTAL); row.setGravity(Gravity.CENTER_VERTICAL); row.setPadding(0, dp(4), 0, dp(4));
            TextView name = new TextView(this); name.setText(book.name + "\n" + book.uid); name.setTextSize(16); row.addView(name, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
            Button edit = new Button(this); edit.setText("Име"); edit.setOnClickListener(v -> { dialog.dismiss(); askForName(book.uid, book.name); }); row.addView(edit);
            Button del = new Button(this); del.setText("X"); del.setOnClickListener(v -> new AlertDialog.Builder(this).setTitle("Изтриване").setMessage("Да изтрия ли \"" + book.name + "\"?").setNegativeButton("Не", null).setPositiveButton("Да", (d, w) -> { prefs.edit().remove(book.uid).apply(); dialog.dismiss(); showSavedBooks(); }).show()); row.addView(del);
            list.addView(row, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        }
        dialog.show();
    }

    private List<BookItem> getBooks() {
        List<BookItem> books = new ArrayList<>();
        for (Map.Entry<String, ?> e : prefs.getAll().entrySet()) if (!KEY_SPEAK.equals(e.getKey()) && e.getValue() instanceof String) books.add(new BookItem(e.getKey(), (String)e.getValue()));
        Collections.sort(books, Comparator.comparing(b -> b.name.toLowerCase(new Locale("bg", "BG")))); return books;
    }

    private String bytesToHex(byte[] bytes) { if (bytes == null || bytes.length == 0) return "UNKNOWN"; StringBuilder sb = new StringBuilder(); for (byte b : bytes) { if (sb.length() > 0) sb.append(':'); sb.append(String.format(Locale.US, "%02X", b & 0xFF)); } return sb.toString(); }
    private int dp(int v) { return Math.round(v * getResources().getDisplayMetrics().density); }
    @Override public void onInit(int status) { if (status == TextToSpeech.SUCCESS) { int r = tts.setLanguage(new Locale("bg", "BG")); ttsReady = r != TextToSpeech.LANG_MISSING_DATA && r != TextToSpeech.LANG_NOT_SUPPORTED; } }
    @Override protected void onDestroy() { if (tts != null) { tts.stop(); tts.shutdown(); } super.onDestroy(); }
    private static class BookItem { final String uid, name; BookItem(String uid, String name) { this.uid = uid; this.name = name; } }
}
