package com.unimelb.gof.wesnap;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;

import com.unimelb.gof.wesnap.models.Chat;

/**
 * Provides UI for Chat List view.
 */
public class ChatFragment extends Fragment {
    private static final String TAG = "ChatFragment";
    private static ChatFragment mChatFragment = null;

    /* Firebase Database variables */
    private DatabaseReference mCurrentUserRef; // TODO
    private DatabaseReference mCurrentChatsRef;

    /* UI variables */
    private RecyclerView mChatRecyclerView;
    private FirebaseRecyclerAdapter<Chat, ChatListViewHolder> mFirebaseAdapter;
    private LinearLayoutManager mLinearLayoutManager;

// ======================================================

    public ChatFragment() {
    }

    public static ChatFragment getInstance() {
        if (mChatFragment == null) {
            mChatFragment = new ChatFragment();
        }
        return mChatFragment;
    }

    // ======================================================

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mChatRecyclerView = (RecyclerView) inflater.inflate(
                R.layout.recycler_view, container, false);
        mChatRecyclerView.setTag(TAG);

//        mBottonNewChat = (FloatingActionButton) rootView.findViewById(R.id.button_new_chat);

        return mChatRecyclerView;
    }

    // ======================================================

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Database Refs
        mCurrentUserRef = FirebaseUtil.getCurrentUserRef();
        mCurrentChatsRef = FirebaseUtil.getCurrentChatsRef();

        // UI: LinearLayoutManager
        mLinearLayoutManager = new LinearLayoutManager(getActivity());
        mLinearLayoutManager.setReverseLayout(false);
        mLinearLayoutManager.setStackFromEnd(false);

        // UI: RecyclerAdapter
        // [START create the recycler adapter for chat]
        mFirebaseAdapter = new FirebaseRecyclerAdapter<Chat, ChatListViewHolder>(
                Chat.class, R.layout.item_chat,
                ChatListViewHolder.class, mCurrentChatsRef) {
            @Override
            protected void populateViewHolder(
                    ChatListViewHolder viewHolder, Chat chat, int position) {
                // load the name of the receiver
                viewHolder.nameView.setText(chat.getReceiverName());
                // load the avatar unless non-existing
                if (chat.getReceiverPhotoUrl() != null
                        && chat.getReceiverPhotoUrl().length() != 0) {
                    GlideUtil.loadProfileIcon(
                            chat.getReceiverPhotoUrl(), viewHolder.avatarView);

                } else {
                    viewHolder.avatarView.setImageResource(
                            R.drawable.ic_person);
                }
            }
        };

        mFirebaseAdapter.registerAdapterDataObserver(
                new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);

                int chatCount = mFirebaseAdapter.getItemCount();
                int lastVisiblePosition = mLinearLayoutManager
                        .findLastCompletelyVisibleItemPosition();

                // If the recycler view is initially being loaded or
                // the user is at the bottom of the list,
                // scroll to the bottom of the list to show the newly added message.
                if (lastVisiblePosition == -1 ||
                        (positionStart >= (chatCount - 1) &&
                                lastVisiblePosition == (positionStart - 1))) {
                    mChatRecyclerView.scrollToPosition(positionStart);
                }
            }
        });
        // [END create the recycler adapter for chat]

        // UI: link them to RecyclerView
        mChatRecyclerView.setLayoutManager(mLinearLayoutManager);
        mChatRecyclerView.setAdapter(mFirebaseAdapter);
    }

    // ======================================================

    /**
     * ChatViewHolder
     * */
    public static class ChatListViewHolder extends RecyclerView.ViewHolder {
        public TextView nameView;
        public ImageView avatarView;

        public ChatListViewHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.item_chat, parent, false));

            nameView = (TextView) itemView.findViewById(R.id.chat_name);
            avatarView = (ImageView) itemView.findViewById(R.id.chat_avatar);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Context context = v.getContext();
                    Intent intent = new Intent(context, MessageActivity.class);
                    intent.putExtra(
                            MessageActivity.EXTRA_POSITION,
                            getAdapterPosition()); // TODO
                    context.startActivity(intent);
                }
            });
        }
    }

    // ======================================================

}
