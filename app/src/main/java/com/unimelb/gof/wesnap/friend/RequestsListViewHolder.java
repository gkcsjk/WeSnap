package com.unimelb.gof.wesnap.friend;

import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.unimelb.gof.wesnap.R;

/**
 * Created by qideng on 28/9/16.
 */
public class RequestsListViewHolder extends RecyclerView.ViewHolder {
    public ImageView avatarView;
    public TextView nameView;
    public TextView emailView;
    public ImageButton doButton;

    public RequestsListViewHolder(View v) {
        super(v);

        avatarView = (ImageView) itemView.findViewById(R.id.avatar_request);
        nameView = (TextView) itemView.findViewById(R.id.text_name_request);
        emailView = (TextView) itemView.findViewById(R.id.text_email_request);
        doButton = (ImageButton) itemView.findViewById(R.id.button_request);
    }

    /* Update the button UI after friend request sent or if already isFriend */
    public void changeToDoneButton() {
        doButton.setImageResource(R.drawable.ic_action_done);
        doButton.setColorFilter(R.color.colorAccent);
        doButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Snackbar.make(v, "Friend request sent, or you are friends already!",
                        Snackbar.LENGTH_LONG).show();
            }
        });
    }
}

