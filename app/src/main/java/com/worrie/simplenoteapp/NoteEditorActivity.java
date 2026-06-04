package com.worrie.simplenoteapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class NoteEditorActivity extends AppCompatActivity {

    EditText titleEditText;
    EditText contentEditText;
    FloatingActionButton saveNoteFab;
    Note currentNote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_editor);

        titleEditText = findViewById(R.id.titleEditText);
        contentEditText = findViewById(R.id.contentEditText);
        saveNoteFab = findViewById(R.id.save_note_fab);

        Intent intent = getIntent();
        String listName = intent.getStringExtra("listName");
        int noteIndex = intent.getIntExtra("noteIndex", -1);

        if (noteIndex != -1 && listName != null) {
            switch (listName) {
                case "archive":
                    currentNote = MainActivity.archivedNotes.get(noteIndex);
                    break;
                case "deleted":
                    currentNote = MainActivity.deletedNotes.get(noteIndex);
                    break;
                case "main":
                default:
                    currentNote = MainActivity.notes.get(noteIndex);
                    break;
            }
            titleEditText.setText(currentNote.getTitle());
            contentEditText.setText(currentNote.getContent());
        } else {
            currentNote = new Note("", "");
            MainActivity.notes.add(currentNote);
        }

        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentNote.setTitle(titleEditText.getText().toString());
                currentNote.setContent(contentEditText.getText().toString());
                MainActivity.saveNotes(getApplicationContext());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        };

        titleEditText.addTextChangedListener(textWatcher);
        contentEditText.addTextChangedListener(textWatcher);
        saveNoteFab.setOnClickListener(v -> finish());
    }
}
