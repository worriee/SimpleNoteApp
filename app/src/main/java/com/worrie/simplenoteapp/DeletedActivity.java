package com.worrie.simplenoteapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class DeletedActivity extends AppCompatActivity {

    ListView listView;
    ArrayAdapter<Note> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_view);
        setTitle("Recently Deleted");

        listView = findViewById(R.id.list_view);
        
        adapter = new ArrayAdapter<>(this, R.layout.item_note, R.id.note_text_view, MainActivity.deletedNotes);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent = new Intent(getApplicationContext(), NoteEditorActivity.class);
            intent.putExtra("listName", "deleted");
            intent.putExtra("noteIndex", position);
            startActivity(intent);
        });

        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            new AlertDialog.Builder(DeletedActivity.this)
                    .setTitle("Deleted Options")
                    .setItems(new CharSequence[]{"Restore", "Delete Permanently"}, (dialog, which) -> {
                        if (which == 0) {
                            Note note = MainActivity.deletedNotes.get(position);
                            MainActivity.notes.add(note);
                            MainActivity.deletedNotes.remove(position);
                            adapter.notifyDataSetChanged();
                            MainActivity.saveNotes(getApplicationContext());
                            Toast.makeText(this, "Note Restored", Toast.LENGTH_SHORT).show();
                        } else {
                            MainActivity.deletedNotes.remove(position);
                            adapter.notifyDataSetChanged();
                            MainActivity.saveNotes(getApplicationContext());
                            Toast.makeText(this, "Note Deleted Permanently", Toast.LENGTH_SHORT).show();
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
