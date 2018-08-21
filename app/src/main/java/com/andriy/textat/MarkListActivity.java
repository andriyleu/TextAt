package com.andriy.textat;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import java.util.ArrayList;

public class MarkListActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private ListView markList;
    private Toolbar toolbar;
    ArrayList<Mark> marks;

    // This is the Adapter being used to display the list's data
    SimpleCursorAdapter mAdapter;


    private void bindElements() {
        markList = findViewById(R.id.list);
        toolbar = findViewById(R.id.listToolbar);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mark_list);

        bindElements();

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);


        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Intent intent = getIntent();
        marks = intent.getParcelableArrayListExtra("marks");
        String title = intent.getStringExtra("title");

        getSupportActionBar().setTitle(title);


        CustomListAdapter adapter = new CustomListAdapter(marks, this);
        markList.setAdapter(adapter);
        markList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(MarkListActivity.this, MarkDetailActivity.class);
                intent.putExtra("mark", marks.get(i));
                startActivity(intent);
            }
        });
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        return null;
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {

    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {

    }
}
