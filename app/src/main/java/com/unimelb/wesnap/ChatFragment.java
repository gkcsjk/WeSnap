package com.unimelb.wesnap;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.unimelb.wesnap.models.Chat;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatFragment extends Fragment {
    private static final String TAG = "ChatFragment";

    private DatabaseReference mCurrentUserRef;
    private DatabaseReference mCurrentChatsRef;

    private RecyclerView mChatRecyclerView;
    private FirebaseRecyclerAdapter<Chat, ChatViewHolder> mFirebaseAdapter;
    private LinearLayoutManager mLinearLayoutManager;
    private OnChatSelectedListener mChatSelectedListener;

    private FloatingActionButton mBottonNewChat;
    private View.OnClickListener listenerNewChat;

    // ======================================================

    /* Fragment singleton */
    private static ChatFragment mChatFragment = null;

    public ChatFragment() {

    }

    /* Returns a singleton instance of this fragment */
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
        View rootView = inflater.inflate(R.layout.fragment_chat, container, false);
        rootView.setTag(TAG);

        mChatRecyclerView = (RecyclerView) rootView.findViewById(R.id.chatRecyclerView);
        mBottonNewChat = (FloatingActionButton) rootView.findViewById(R.id.button_new_chat);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // [START settings_click_listener]
        listenerNewChat = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Go to Settings
                Intent intent = new Intent(getActivity(), NewChatActivity.class);
                startActivity(intent);
            }
        };
        mBottonNewChat.setOnClickListener(listenerNewChat);
        // [END settings_click_listener]

        // UI
        mLinearLayoutManager = new LinearLayoutManager(getActivity());
        mLinearLayoutManager.setReverseLayout(false);
        mLinearLayoutManager.setStackFromEnd(false);

        // Database Refs
        mCurrentUserRef = FirebaseUtil.getCurrentUserRef();
        mCurrentChatsRef = FirebaseUtil.getCurrentChatsRef();

        // [START setting up adapter for chat]
        mFirebaseAdapter = new FirebaseRecyclerAdapter<Chat, ChatViewHolder>(
                Chat.class,
                R.layout.item_chat,
                ChatViewHolder.class,
                mCurrentChatsRef) {

            @Override
            protected void populateViewHolder(
                    ChatViewHolder viewHolder, Chat activeChat, int position) {

                viewHolder.receiverNameView.setText(activeChat.getReceiverName());

                if (activeChat.getReceiverPhotoUrl() != null) {
                    GlideUtil.loadProfileIcon(
                            activeChat.getReceiverPhotoUrl(), viewHolder.receiverPhotoView);
                } else {
                    viewHolder.receiverPhotoView.setImageResource(
                            R.drawable.ic_account_circle_black_36dp);
                }
            }
        };

        mFirebaseAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);

//                int chatCount = mFirebaseAdapter.getItemCount();
//                int lastVisiblePosition = mLinearLayoutManager.findLastCompletelyVisibleItemPosition();
//                // If the recycler view is initially being loaded or
//                // the user is at the bottom of the list,
//                // scroll to the bottom of the list to show the newly added message.
//                if (lastVisiblePosition == -1 ||
//                        (positionStart >= (chatCount - 1) &&
//                                lastVisiblePosition == (positionStart - 1))) {
//                    mChatRecyclerView.scrollToPosition(positionStart);
//                }
            }
        });
        // [END setting up adapter for chat]

        // more UI
        mChatRecyclerView.setLayoutManager(mLinearLayoutManager);
        mChatRecyclerView.setAdapter(mFirebaseAdapter);
    }

    // ======================================================

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    public interface OnChatSelectedListener {
        // TODO: OnChatSelectedListener in "MainActivity"
        void onChatSelected(String chatKey);
    }

    /** Attach the mChatSelectedListener */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnChatSelectedListener) {
            mChatSelectedListener = (OnChatSelectedListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnPostSelectedListener");
        }
    }

    /** Detach the mChatSelectedListener */
    @Override
    public void onDetach() {
        super.onDetach();
        mChatSelectedListener = null;
    }

    // ======================================================
    /** class: ChatViewHolder
     * */
    public static class ChatViewHolder extends RecyclerView.ViewHolder {
        public TextView receiverNameView;
        public CircleImageView receiverPhotoView;

        public ChatViewHolder(View v) {
            super(v);
            receiverNameView = (TextView) itemView.findViewById(R.id.chat_receiver_name);
            receiverPhotoView = (CircleImageView) itemView.findViewById(R.id.chat_receiver_photo);
        }
    }

    // ======================================================

}