package com.worrie.simplenoteapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.splashscreen.SplashScreen;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.worrie.simplenoteapp.gemini.GeminiRequest;
import com.worrie.simplenoteapp.gemini.GeminiResponse;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    RecyclerView notesRecyclerView;
    FloatingActionButton addNoteFab;
    FloatingActionButton pasteLinkFab;
    ImageButton themeToggleButton;
    ImageButton menuButton;

    static ArrayList<Note> notes = new ArrayList<>();
    static ArrayList<Note> archivedNotes = new ArrayList<>();
    static ArrayList<Note> deletedNotes = new ArrayList<>();
    static NoteAdapter noteAdapter;

    private static final String PREF_NAME = "com.worrie.simplenoteapp.notes";
    private static final String NOTES_KEY = "notes_list_v2";
    private static final String ARCHIVE_KEY = "archive_list_v2";
    private static final String DELETED_KEY = "deleted_list_v2";
    private static final String THEME_KEY = "is_dark_mode";

    private boolean isDarkMode;

    private static final String GEMINI_API_KEY = BuildConfig.GEMINI_API_KEY;
    private static final String GEMINI_URL = "https://generativelanguage.googleapis.com/v1/models/gemini-3.1-flash-lite:generateContent?key=" + GEMINI_API_KEY.trim();

    private static final String CAPTION_API_BASE_URL = "https://youtube-captions-transcript-subtitles-video-combiner.p.rapidapi.com/download-all/";
    private static final String CAPTION_API_KEY = BuildConfig.CAPTION_API_KEY;
    private static final String CAPTION_API_HOST = "youtube-captions-transcript-subtitles-video-combiner.p.rapidapi.com";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SplashScreen.installSplashScreen(this);

        SharedPreferences sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        isDarkMode = sharedPreferences.getBoolean(THEME_KEY, false);
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        setContentView(R.layout.activity_main);

        notesRecyclerView = findViewById(R.id.notes_recycler_view);
        addNoteFab = findViewById(R.id.add_note_fab);
        pasteLinkFab = findViewById(R.id.paste_link_fab);
        themeToggleButton = findViewById(R.id.theme_toggle_button);
        menuButton = findViewById(R.id.menu_button);

        loadNotes();

        notesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        noteAdapter = new NoteAdapter(notes, new NoteAdapter.OnNoteClickListener() {
            @Override
            public void onNoteClick(int position) {
                Intent intent = new Intent(getApplicationContext(), NoteEditorActivity.class);
                intent.putExtra("listName", "main");
                intent.putExtra("noteIndex", position);
                startActivity(intent);
            }

            @Override
            public void onNoteLongClick(int position) {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Note Options")
                        .setItems(new CharSequence[]{"Archive", "Delete"}, (dialog, which) -> {
                            if (which == 0) {
                                Note noteToArchive = notes.get(position);
                                archivedNotes.add(noteToArchive);
                                notes.remove(position);
                                noteAdapter.notifyDataSetChanged();
                                saveNotes(getApplicationContext());
                                Toast.makeText(MainActivity.this, "Note Archived", Toast.LENGTH_SHORT).show();
                            } else {
                                Note noteToDelete = notes.get(position);
                                deletedNotes.add(noteToDelete);
                                notes.remove(position);
                                noteAdapter.notifyDataSetChanged();
                                saveNotes(getApplicationContext());
                                Toast.makeText(MainActivity.this, "Moved to Recently Deleted", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .show();
            }
        });
        notesRecyclerView.setAdapter(noteAdapter);

        themeToggleButton.setOnClickListener(v -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            if (isDarkMode) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                editor.putBoolean(THEME_KEY, false);
                isDarkMode = false;
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                editor.putBoolean(THEME_KEY, true);
                isDarkMode = true;
            }
            editor.apply();
        });

        menuButton.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(MainActivity.this, menuButton);
            popup.getMenuInflater().inflate(R.menu.main_menu, popup.getMenu());
            popup.setOnMenuItemClickListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.action_archive) {
                    startActivity(new Intent(MainActivity.this, ArchiveActivity.class));
                    return true;
                } else if (itemId == R.id.action_deleted) {
                    startActivity(new Intent(MainActivity.this, DeletedActivity.class));
                    return true;
                }
                return false;
            });
            popup.show();
        });

        pasteLinkFab.setOnClickListener(v -> showPasteLinkDialog());

        addNoteFab.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), NoteEditorActivity.class);
            intent.putExtra("noteIndex", -1);
            startActivity(intent);
        });

    }

    private void showPasteLinkDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.App_Dialog_Alert);
        builder.setTitle("Paste YouTube Link");
        final EditText input = new EditText(this);
        input.setHint("https://www.youtube.com/watch?v=...");
        builder.setView(input);
        builder.setPositiveButton("Convert", (dialog, which) -> {
            String url = input.getText().toString();
            if (!url.isEmpty()) summarizeVideo(url);
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void summarizeVideo(String url) {
        Toast.makeText(this, "Generating AI Notes...", Toast.LENGTH_SHORT).show();
        callSummarizeProxy(url);
    }

    private void callSummarizeProxy(String url) {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build();

        // REPLACE THIS URL with your actual Vercel deployment URL
        String proxyUrl = "https://simple-note-app-orpin.vercel.app/api/summarize";
        
        Gson gson = new Gson();
        Map<String, String> bodyMap = new HashMap<>();
        bodyMap.put("url", url);
        String jsonBody = gson.toJson(bodyMap);

        RequestBody body = RequestBody.create(jsonBody, MediaType.get("application/json; charset=utf-8"));
        Request request = new Request.Builder().url(proxyUrl).post(body).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> showErrorDialog("Server Error", "Could not connect to proxy server: " + e.getMessage()));
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body() != null ? response.body().string() : "";
                if (response.isSuccessful()) {
                    try {
                        Map<String, String> result = gson.fromJson(responseBody, new TypeToken<Map<String, String>>(){}.getType());
                        String summary = result.get("summary");
                        if (summary != null && !summary.isEmpty()) {
                            runOnUiThread(() -> {
                                notes.add(new Note("Video Summary", summary));
                                noteAdapter.notifyDataSetChanged();
                                saveNotes(getApplicationContext());
                                Toast.makeText(MainActivity.this, "Notes Added!", Toast.LENGTH_SHORT).show();
                            });
                        } else {
                            runOnUiThread(() -> showErrorDialog("AI Error", "No summary returned from server."));
                        }
                    } catch (Exception e) {
                        runOnUiThread(() -> showErrorDialog("Parsing Error", e.getMessage()));
                    }
                } else {
                    runOnUiThread(() -> showErrorDialog("Server Error " + response.code(), responseBody));
                }
            }
        });
    }

    private String extractVideoId(String url) {
        String pattern = "(?<=watch\\?v=|/videos/|embed/|youtu.be/|/v/|/e/|watch\\?v%3D|watch\\?feature=player_embedded&v=|%2Fvideos%2F|embed%2F|youtu.be%2F|/v/)[^#&?\\n]*";
        Matcher matcher = Pattern.compile(pattern).matcher(url);
        return matcher.find() ? matcher.group() : null;
    }

    interface TranscriptCallback {
        void onTranscriptFetched(String transcript);
        void onError(String error);
    }

    private void showErrorDialog(String title, String message) {
        new AlertDialog.Builder(MainActivity.this, R.style.App_Dialog_Alert)
                .setTitle(title).setMessage(message).setPositiveButton("OK", null).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        boolean removed = false;
        for (int i = notes.size() - 1; i >= 0; i--) {
            Note note = notes.get(i);
            if (note.getTitle().trim().isEmpty() && note.getContent().trim().isEmpty()) {
                notes.remove(i);
                removed = true;
            }
        }
        if (removed) {
            saveNotes(getApplicationContext());
            Toast.makeText(this, "Blank Note Deleted", Toast.LENGTH_SHORT).show();
        }
        if (noteAdapter != null) noteAdapter.notifyDataSetChanged();
    }

    public static void saveNotes(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        editor.putString(NOTES_KEY, gson.toJson(notes));
        editor.putString(ARCHIVE_KEY, gson.toJson(archivedNotes));
        editor.putString(DELETED_KEY, gson.toJson(deletedNotes));
        editor.apply();
    }

    private void loadNotes() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<Note>>() {}.getType();
        notes.clear(); archivedNotes.clear(); deletedNotes.clear();
        String jsonNotes = sharedPreferences.getString(NOTES_KEY, null);
        String jsonArchive = sharedPreferences.getString(ARCHIVE_KEY, null);
        String jsonDeleted = sharedPreferences.getString(DELETED_KEY, null);
        if (jsonNotes != null) {
            ArrayList<Note> list = gson.fromJson(jsonNotes, type);
            if (list != null) notes.addAll(list);
        }
        if (jsonArchive != null) {
            ArrayList<Note> list = gson.fromJson(jsonArchive, type);
            if (list != null) archivedNotes.addAll(list);
        }
        if (jsonDeleted != null) {
            ArrayList<Note> list = gson.fromJson(jsonDeleted, type);
            if (list != null) deletedNotes.addAll(list);
        }
        if (notes.isEmpty()) notes.add(new Note("Read here before using!!", "Please double check the notes generated it may have some mispelled words. \n\n-Jul"));
    }
}
