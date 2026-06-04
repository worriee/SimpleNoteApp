package com.worrie.simplenoteapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class ArchiveActivity extends AppCompatActivity {

    ListView listView;
    ArrayAdapter<Note> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_view);
        setTitle("Archive");

        listView = findViewById(R.id.list_view);
        
        adapter = new ArrayAdapter<>(this, R.layout.item_note, R.id.note_text_view, MainActivity.archivedNotes);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent = new Intent(getApplicationContext(), NoteEditorActivity.class);
            intent.putExtra("listName", "archive");
            intent.putExtra("noteIndex", position);
            startActivity(intent);
        });

        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            new AlertDialog.Builder(ArchiveActivity.this)
                    .setTitle("Archive Options")
                    .setItems(new CharSequence[]{"Unarchive", "Delete"}, (dialog, which) -> {
                        if (which == 0) {
                            Note note = MainActivity.archivedNotes.get(position);
                            MainActivity.notes.add(note);
                            MainActivity.archivedNotes.remove(position);
                            adapter.notifyDataSetChanged();
                            MainActivity.saveNotes(getApplicationContext());
                            Toast.makeText(this, "Note Unarchived", Toast.LENGTH_SHORT).show();
                        } else {
                            Note note = MainActivity.archivedNotes.get(position);
                            MainActivity.deletedNotes.add(note);
                            MainActivity.archivedNotes.remove(position);
                            adapter.notifyDataSetChanged();
                            MainActivity.saveNotes(getApplicationContext());
                            Toast.makeText(this, "Moved to Recently Deleted", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .show();
            return true;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (adapter != null) adapter.notifyDataSetChanged();
    }
}
