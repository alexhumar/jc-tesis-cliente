package com.juegocolaborativo.fragment;

/**
 * Created by Matias on 28/04/14.
 */
import android.os.Bundle;
import android.preference.PreferenceFragment;
import com.juegocolaborativo.R;

public class PrefsFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
    }

}