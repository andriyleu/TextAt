package com.andriy.textat;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import java.util.ArrayList;

public class MarkListActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor>  {

    ListView markList;
    ArrayList<Mark> marks;

    // This is the Adapter being used to display the list's data
    SimpleCursorAdapter mAdapter;


    private void bindElements() {
        markList = findViewById(R.id.list);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mark_list);

        bindElements();

        Intent intent = getIntent();
        marks = intent.getParcelableArrayListExtra("marks");

        CustomListAdapter adapter = new CustomListAdapter(marks, this);
        markList.setAdapter(adapter);
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
