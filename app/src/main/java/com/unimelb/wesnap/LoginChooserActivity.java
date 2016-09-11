package com.unimelb.wesnap;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * List-based Activity to redirect to one of the other Activities:
 *     {@link GoogleLoginActivity}
 *     {@link EmailPasswordLoginActivity}
 */
public class LoginChooserActivity
        extends AppCompatActivity
        implements AdapterView.OnItemClickListener {


    private static final Class[] CLASSES = new Class[]{
            GoogleLoginActivity.class,
            EmailPasswordLoginActivity.class,
    };

    private static final int[] TITLE_IDS = new int[] {
            R.string.title_activity_google_login,
            R.string.title_activity_email_password_login,
    };

    private static final int[] DESCRIPTION_IDS = new int[] {
            R.string.desc_google_sign_in,
            R.string.desc_emailpassword,
    };

    // ======================================================
    /*
    * onCreate()
    * */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseAuth mFirebaseAuth;
        FirebaseUser mFirebaseUser;
        //Initialise Firebase Auth
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

        if (mFirebaseUser != null) {
            // Signed in, launch the Main activity
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
            return;
        }


        setContentView(R.layout.activity_login);

        // Set up ListView and Adapter
        ListView listView = (ListView) findViewById(R.id.activity_login_chooser);

        MyArrayAdapter adapter = new MyArrayAdapter(
                this, R.layout.login_icon, CLASSES);
        adapter.setTitleIds(TITLE_IDS);
        adapter.setDescriptionIds(DESCRIPTION_IDS);

        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);
    }

    // ======================================================
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Class clicked = CLASSES[position];
        startActivity(new Intent(this, clicked));
    }

    // ======================================================
    // TODO: need to update the UI
    public static class MyArrayAdapter extends ArrayAdapter<Class> {

        private Context mContext;
        private Class[] mClasses;

        private int[] mTitleIds;
        private int[] mDescriptionIds;

        public MyArrayAdapter(Context context, int resource, Class[] objects) {
            super(context, resource, objects);

            mContext = context;
            mClasses = objects;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;

            if (convertView == null) {
                LayoutInflater inflater =
                        (LayoutInflater) mContext.getSystemService(LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.login_icon, null);
            }

            ImageView iv = (ImageView) view.findViewById(R.id.login_img);
            if (position == 0)
                iv.setImageResource(R.drawable.google);
            else
                iv.setImageResource(R.drawable.wesnap);
            ((TextView) view.findViewById(R.id.login_txt)).setText(
                    mTitleIds[position]);
//            ((TextView) view.findViewById(android.R.id.text2)).setText(
//                    mDescriptionIds[position]);

            return view;
        }

        public void setTitleIds(int[] titleIds) {
            mTitleIds = titleIds;
        }

        public void setDescriptionIds(int[] descriptionIds) {
            mDescriptionIds = descriptionIds;
        }
    }
}