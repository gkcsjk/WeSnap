package com.unimelb.gof.wesnap;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.unimelb.gof.wesnap.models.User;

/**
 * Created by qideng on 20/09/2016.
 */

public class FriendsFragment extends Fragment {
    private static final String TAG = "FriendsFragment";
    private static FriendsFragment mFriendsFragment = null;

    /* UI Variables */
    private RecyclerView mFriendsRecyclerView;
    private FirebaseRecyclerAdapter<User, FriendsFragment.FriendsListViewHolder> mFirebaseAdapter;
    private LinearLayoutManager mLinearLayoutManager;
    private FloatingActionButton mFabAddFriend;

    /* Firebase Database variables */  // TODO DatabaseReference
    private DatabaseReference refCurrentUser;
    private DatabaseReference refCurrentFriends;

    // ========================================================

    public FriendsFragment() {
    }

    /* Returns a singleton instance of this fragment */
    public static FriendsFragment getInstance() {
        if (mFriendsFragment == null) {
            mFriendsFragment = new FriendsFragment();
        }
        return mFriendsFragment;
    }

    // ========================================================
    /**
     * */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_friends, container, false);
        mFriendsRecyclerView = (RecyclerView)
                rootView.findViewById(R.id.recycler_friends);
        mFriendsRecyclerView.setTag(TAG);

        // Add Floating Action Button to Main screen
        mFabAddFriend = (FloatingActionButton) rootView.findViewById(R.id.fab_add_friend);
        mFabAddFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Snackbar.make(v, "Want to add some friend? TBD", Snackbar.LENGTH_LONG).show();
            }
        });

        return rootView;
    }

    // ========================================================
    /**
     * */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Database Refs
        refCurrentUser = FirebaseUtil.getCurrentUserRef();
        refCurrentFriends = FirebaseUtil.getCurrentFriendsRef();

        // UI: LinearLayoutManager
        mLinearLayoutManager = new LinearLayoutManager(getActivity());
        mLinearLayoutManager.setReverseLayout(false);
        mLinearLayoutManager.setStackFromEnd(false);
        // TODO sorting friends

        // UI: RecyclerAdapter
        // [START create the recycler adapter for chat]
        mFirebaseAdapter = new FirebaseRecyclerAdapter<User, FriendsListViewHolder>(
                User.class, R.layout.item_friend,
                FriendsListViewHolder.class, refCurrentFriends) {
            @Override
            protected void populateViewHolder(
                    FriendsListViewHolder viewHolder, User friend, int position) {
                // load the name of the receiver
                viewHolder.nameView.setText(friend.getDisplayedName());
                // load the avatar unless non-existing
                String avatarUrl = friend.getAvatarUrl();
                if (avatarUrl != null && avatarUrl.length() != 0) {
                    GlideUtil.loadProfileIcon(avatarUrl, viewHolder.avatarView);
                } else {
                    viewHolder.avatarView.setImageResource(R.drawable.ic_default_avatar);
                }
            }
        };

        mFirebaseAdapter.registerAdapterDataObserver(
                new RecyclerView.AdapterDataObserver() {
                    @Override
                    public void onItemRangeInserted(int positionStart, int itemCount) {
                        super.onItemRangeInserted(positionStart, itemCount);

                        int friendsCount = mFirebaseAdapter.getItemCount();
                        int lastVisiblePosition = mLinearLayoutManager
                                .findLastCompletelyVisibleItemPosition();
                    }
                });
        // [END create the recycler adapter for chat]

        // UI: link them to RecyclerView
        mFriendsRecyclerView.setLayoutManager(mLinearLayoutManager);
        mFriendsRecyclerView.setAdapter(mFirebaseAdapter);
    }

    // ======================================================

    /**
     * FriendsListViewHolder
     * */
    public static class FriendsListViewHolder extends RecyclerView.ViewHolder {
        public ImageView avatarView;
        public TextView nameView;

        public FriendsListViewHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.item_friend, parent, false));

            avatarView = (ImageView) itemView.findViewById(R.id.friend_avatar);
            nameView = (TextView) itemView.findViewById(R.id.friend_name);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Snackbar.make(v, "Friend's detail view: TBD", Snackbar.LENGTH_LONG).show();
//                    Context context = v.getContext();
//                    Intent intent = new Intent(context, MessageActivity.class);
//                    intent.putExtra(
//                            MessageActivity.EXTRA_POSITION,
//                            getAdapterPosition()); // TODO
//                    context.startActivity(intent);
                }
            });
        }
    }
}
