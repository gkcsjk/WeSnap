package com.unimelb.wesnap;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import com.unimelb.wesnap.models.Person;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MyProfileFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MyProfileFragment#getInstance} factory method to
 * create an instance of this fragment.
 */
public class MyProfileFragment extends Fragment {

    private static final String TAG = "MyProfileFragment";

    private DatabaseReference mCurrentUserRef;
    private ValueEventListener mProfileListener;
    private CircleImageView mProfilePhoto;
    private TextView mProfileEmail;

    // buttons
    private Button mBottonMemories;
    private View.OnClickListener listenerMemories;
    private Button mBottonSettings;
    private View.OnClickListener listenerSettings;

    private Button mBottonExitApp;
    private View.OnClickListener listenerExitApp;
    private Button mBottonLogout;
    private View.OnClickListener listenerLogout;

    private OnFragmentInteractionListener mListener;

    /* Fragment singleton??? */
    private static MyProfileFragment mMyProfileFragment = null;

    public MyProfileFragment() {
        // Required empty public constructor
    }

    /* Returns a singleton instance of this fragment */
    public static MyProfileFragment getInstance() {
        if (mMyProfileFragment == null) {
            mMyProfileFragment = new MyProfileFragment();
        }
        return mMyProfileFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mCurrentUserRef = FirebaseUtil.getCurrentUserRef();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_my_profile, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        mProfilePhoto = (CircleImageView) view.findViewById(R.id.profile_user_photo);
        mProfileEmail = (TextView) view.findViewById(R.id.profile_email);
        mBottonMemories = (Button) view.findViewById(R.id.button_memories);
        mBottonSettings = (Button) view.findViewById(R.id.button_settings);
        mBottonExitApp = (Button) view.findViewById(R.id.button_exit_app);
        mBottonLogout = (Button) view.findViewById(R.id.button_logout);

        // [START person_value_event_listener]
        ValueEventListener profileListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Person object and use the values to update the UI
                Person person = dataSnapshot.getValue(Person.class);

                if (person.getProfilePhoto() != null) {
                    GlideUtil.loadProfileIcon(person.getProfilePhoto(), mProfilePhoto);
                } else {
                    mProfilePhoto.setImageResource(R.mipmap.profile);
                }

                mProfileEmail.setText(person.getEmail());

                // TODO Person username & display name
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Person failed, log a message
                Log.w(TAG, "loadPerson:onCancelled", databaseError.toException());
                Toast.makeText(getActivity(), "Failed to load person.", Toast.LENGTH_SHORT).show();
            }
        };
        mCurrentUserRef.addValueEventListener(profileListener);
        // [END person_value_event_listener]

        // Keep copy of post listener so we can remove it when app stops
        mProfileListener = profileListener;


        // [START click_listener for the buttons]
        listenerMemories = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO start Memories activity???
            }
        };
        mBottonMemories.setOnClickListener(listenerMemories);

        listenerSettings = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Go to Settings
                Intent intent = new Intent(getActivity(), SettingsActivity.class);
                startActivity(intent);
            }
        };
        mBottonSettings.setOnClickListener(listenerSettings);

        listenerExitApp = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO possible?
            }
        };
        mBottonExitApp.setOnClickListener(listenerExitApp);

        listenerLogout = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO possible?
            }
        };
        mBottonLogout.setOnClickListener(listenerLogout);
        // [END click_listener for the buttons]
    }

    @Override
    public void onStop() {
        super.onStop();

        // Remove person value event listener
        if (mProfileListener != null) {
            mCurrentUserRef.removeEventListener(mProfileListener);
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    public interface OnFragmentInteractionListener {
        // TODO ???
        void onFragmentInteraction(Uri uri);
    }
}
