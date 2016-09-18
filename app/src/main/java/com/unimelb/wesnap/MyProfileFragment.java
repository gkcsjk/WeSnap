package com.unimelb.wesnap;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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

    private DatabaseReference mPeopleRef;
    private DatabaseReference mCurrentUserRef;
    private ValueEventListener mProfileListener;
    private CircleImageView mProfilePhoto;
    private TextView mProfileEmail;

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

//    /**
//     * Use this factory method to create a new instance of
//     * this fragment using the provided parameters.
//     *
//     * @param param1 Parameter 1.
//     * @param param2 Parameter 2.
//     * @return A new instance of fragment MyProfileFragment.
//     */
//    // TODO: Rename and change types and number of parameters
//    public static MyProfileFragment newInstance(String param1, String param2) {
//        MyProfileFragment fragment = new MyProfileFragment();
//        Bundle args = new Bundle();
//        args.putString(ARG_PARAM1, param1);
//        args.putString(ARG_PARAM2, param2);
//        fragment.setArguments(args);
//        return fragment;
//    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPeopleRef = FirebaseUtil.getPeopleRef();
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

        // [START person_value_event_listener]
        ValueEventListener profileListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Person object and use the values to update the UI
                Person person = dataSnapshot.getValue(Person.class);

                if (person.profilePhoto != null) {
                    GlideUtil.loadProfileIcon(person.profilePhoto, mProfilePhoto);
                } else {
                    mProfilePhoto.setImageResource(R.mipmap.profile);
                }

                mProfileEmail.setText(person.email);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Person failed, log a message
                Log.w(TAG, "loadPerson:onCancelled", databaseError.toException());
//                Toast.makeText(MyProfileFragment.this,
//                        "Failed to load person.",
//                        Toast.LENGTH_SHORT).show();
            }
        };
        mCurrentUserRef.addValueEventListener(profileListener);
        // [END person_value_event_listener]

        // Keep copy of post listener so we can remove it when app stops
        mProfileListener = profileListener;
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
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
