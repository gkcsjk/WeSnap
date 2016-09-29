package com.unimelb.gof.wesnap.friend;

import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.unimelb.gof.wesnap.R;
import com.unimelb.gof.wesnap.models.User;
import com.unimelb.gof.wesnap.util.FirebaseUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by qideng on 28/9/16.
 */
public class FriendRequest {
    private static final String TAG = "FriendRequest";

    public static boolean sendFriendRequest(final String toUserId) {
        Log.w(TAG, "sendFriendRequest");

        final String fromUserId = FirebaseUtil.getCurrentUserId();
        if (fromUserId != null) {
            FirebaseUtil.getCurrentUserRef()
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Log.w(TAG, "getCurrentUser:onDataChange");

                            // fetch current user info
                            User currentUser = dataSnapshot.getValue(User.class);
                            Map<String, Object> requestValues = currentUser.toFriendRequest();

                            // add request to destination user
                            FirebaseUtil.getRequestsRef()
                                    .child(toUserId).child(fromUserId).setValue(requestValues);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.w(TAG, "getUsername:onCancelled", databaseError.toException());
                        }
                    });
            return true;
        } else {
            Log.w(TAG, "getCurrentUserId: unexpected null id");
            // TODO null user id?
            return false;
        }
    }

    public static boolean replyFriendRequest(final String fromUserId, final RequestsListViewHolder viewHolder) {
        Log.w(TAG, "replyFriendRequest");
        viewHolder.changeToDoneButton();
        // TODO: replyFriendRequest

        return false;
    }

    public static void deleteMyFriend(final String friendId) {
        Log.w(TAG, "deleteMyFriend:id=" + friendId);

        final Map<String, Object> friendIds = new HashMap<String, Object>();
        FirebaseUtil.getCurrentFriendsRef()
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.w(TAG, "deleteMyFriend:getMyFriends:onDataChange");
                if (dataSnapshot.exists()) {
                    List<String> myFriends = (List<String>) dataSnapshot.getValue();
                    if (myFriends.contains(friendId)) {
                        myFriends.remove(friendId);
                        friendIds.put("friends", myFriends);
                        FirebaseUtil.getCurrentUserRef().updateChildren(friendIds);
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "deleteMyFriend:getMyFriends:onCancelled", databaseError.toException());
            }
        });
    }
}
