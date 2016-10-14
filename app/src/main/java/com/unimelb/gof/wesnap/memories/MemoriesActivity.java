package com.unimelb.gof.wesnap.memories;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.StorageReference;
import com.unimelb.gof.wesnap.BaseActivity;
import com.unimelb.gof.wesnap.R;
import com.unimelb.gof.wesnap.util.FirebaseUtil;
import com.unimelb.gof.wesnap.util.GlideUtil;
import com.unimelb.gof.wesnap.util.PhotoUploader;

import java.util.ArrayList;
import java.util.List;

/**
 * MemoriesActivity
 * Provides thumbnails of photo uploaded to My Memories;
 * Provides function of "import from local camera roll"
 * Directs to MemoryDetailsActivity when item selected
 *
 * COMP90018 Project, Semester 2, 2016
 * Copyright (C) The University of Melbourne
 */
public class MemoriesActivity extends BaseActivity {
    private static final String TAG = "MemoriesActivity";
    private static int RESULT_LOAD_IMAGE = 1;

    /* UI Variables */
    private RecyclerView mMemoriesRecyclerView;
    private Button mImportButton;
    private MemoriesAdapter mRecyclerAdapter;

    /* Firebase Database / Storage variables */
    private DatabaseReference mMemoriesDatabase;
    private StorageReference mMemoriesStorage;

    public MemoriesActivity() {
    }

    // ========================================================
    /* onCreated() */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");

        // Set up view
        setContentView(R.layout.activity_memories);

        // Firebase Refs
        mMemoriesDatabase = FirebaseUtil.getCurrentMemoriesDatabase();
        mMemoriesStorage = FirebaseUtil.getCurrentMemoriesStorage();
        String idCurrentUser = FirebaseUtil.getCurrentUserId();
        if (mMemoriesDatabase == null || mMemoriesStorage == null || idCurrentUser == null) {
            // null value error out
            Log.e(TAG, "current user uid unexpectedly null; goToLogin()");
            (new BaseActivity()).goToLogin("unexpected null value");
            return;
        }

        // Add Toolbar to main screen
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar_memories);
        setSupportActionBar(mToolbar);

        // Add button
        mImportButton = (Button) findViewById(R.id.button_import_local);
        mImportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // connect to local photo storage
                Intent intent = new Intent(Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, RESULT_LOAD_IMAGE);
            }
        });

        // Add recycler
        mMemoriesRecyclerView = (RecyclerView) findViewById(R.id.recycler_memories);
        mMemoriesRecyclerView.setTag(TAG);

        // UI: GridLayoutManager
        int tilePadding = getResources().getDimensionPixelSize(R.dimen.size_edges_small);
        mMemoriesRecyclerView.setPadding(tilePadding, tilePadding, tilePadding, tilePadding);
        mMemoriesRecyclerView.setLayoutManager(new GridLayoutManager(MemoriesActivity.this, 2));

        // UI: RecyclerAdapter
        mRecyclerAdapter = new MemoriesAdapter(MemoriesActivity.this, mMemoriesDatabase);
        mMemoriesRecyclerView.setAdapter(mRecyclerAdapter);
    }

    // ========================================================
    /* Remove listener */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // Exit the app after confirming via dialog;
            // only show the Dialog when the activity is not finished
            Log.d(TAG, "onKeyDown");
            if (!isFinishing()) {
                mRecyclerAdapter.cleanupListener();
                finish();
            }
            return true;
        }
        return false;
    }

    // ======================================================
    /* MemoryViewHolder */
    private static class MemoryViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        MemoryViewHolder(View v) {
            super(v);
            imageView = (ImageView) itemView.findViewById(R.id.image_memory);
        }
    }

    // ======================================================
    /* MemoriesAdapter */
    private class MemoriesAdapter extends RecyclerView.Adapter<MemoryViewHolder> {
        private Context mContext;
        private DatabaseReference mDatabaseReference;
        private ChildEventListener mChildEventListener;

        private List<String> mMemoryFilenames = new ArrayList<>();
        private List<Uri> mMemoryUris = new ArrayList<>();

        public MemoriesAdapter(final Context context, DatabaseReference ref) {
            mContext = context;
            mDatabaseReference = ref;

            // Create child event listener
            // [START child_event_listener_recycler]
            ChildEventListener childEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                    Log.d(TAG, "getMemoryFilename:onChildAdded:" + dataSnapshot.getKey());
                    // get MemoryUrl string
                    final String newMemoryFilename = dataSnapshot.getKey();
                    // get MemoryUrl from current user storage
                    mMemoriesStorage.child(newMemoryFilename).getDownloadUrl()
                            .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    // Got the download URL for 'memories/myUid/newMemoryFilename'
                                    Log.d(TAG, "getUri:onSuccess:" + uri);
                                    mMemoryFilenames.add(newMemoryFilename);
                                    mMemoryUris.add(uri);
                                    notifyItemInserted(mMemoryUris.size() - 1);
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception exception) {
                                    // Handle any errors
                                    Log.w(TAG, "getUri:onFailure", exception);
                                    // TODO getUri:onFailure
                                }
                            });
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                    Log.d(TAG, "getMemoryFilename:onChildChanged:" + dataSnapshot.getKey());
                    Toast.makeText(mContext, "Changed:" + dataSnapshot.getKey(),
                            Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    Log.d(TAG, "getMemoryFilename:onChildRemoved:" + dataSnapshot.getKey());
                    // get filename and index
                    final String removedMemoryFilename = dataSnapshot.getKey();
                    int index = mMemoryFilenames.indexOf(removedMemoryFilename);
                    if (index > -1) {
                        // Remove data from the list
                        mMemoryUris.remove(index);
                        mMemoryFilenames.remove(index);
                        // Update the RecyclerView
                        notifyItemRemoved(index);
                    } else {
                        Log.w(TAG, "getMemoryFilename:onChildRemoved:unknown_child:" + removedMemoryFilename);
                    }
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
                    Log.d(TAG, "getMemoryFilename:onChildMoved:" + dataSnapshot.getKey());
                    // This method is triggered when a child location's priority changes.
                    Toast.makeText(mContext, "Moved:" + dataSnapshot.getKey(),
                            Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.w(TAG, "getMemoryFilename:onCancelled", databaseError.toException());
                    Toast.makeText(mContext, "Failed to load memory filenames.",
                            Toast.LENGTH_SHORT).show();
                }
            };
            ref.addChildEventListener(childEventListener);
            // [END child_event_listener_recycler]

            // Store reference to listener so it can be removed on app stop
            mChildEventListener = childEventListener;
        }

        @Override
        public MemoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            View view = inflater.inflate(R.layout.item_memory, parent, false);
            return new MemoryViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final MemoryViewHolder viewHolder, final int position) {
            Log.d(TAG, "populateViewHolder:" + position);

            final Uri uri = mMemoryUris.get(position);
            final String filename = mMemoryFilenames.get(position);

            // Load the item view
            String imageUrl = uri.toString();
            if (imageUrl != null && imageUrl.length() != 0) {
                GlideUtil.loadImage(imageUrl, viewHolder.imageView);
            }

            // on click: directs to image actions (delete/share/lock/create_story)
            viewHolder.itemView.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent showMemoryIntent = new Intent(
                                    MemoriesActivity.this, MemoryDetailsActivity.class);
                            showMemoryIntent.putExtra(
                                    MemoryDetailsActivity.EXTRA_PHOTO_FILENAME,
                                    filename);
                            showMemoryIntent.putExtra(
                                    MemoryDetailsActivity.EXTRA_PHOTO_URI,
                                    uri.toString());
                            startActivity(showMemoryIntent);
                        }
                    }
            );
        }

        @Override
        public int getItemCount() {
            return mMemoryUris.size();
        }

        public void cleanupListener() {
            if (mChildEventListener != null) {
                mDatabaseReference.removeEventListener(mChildEventListener);
            }
        }
    }

    // ======================================================
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            Uri selectedImageUri = data.getData();
            PhotoUploader.uploadToMemories(selectedImageUri, MemoriesActivity.this);
        }
    }

    // ======================================================
}
