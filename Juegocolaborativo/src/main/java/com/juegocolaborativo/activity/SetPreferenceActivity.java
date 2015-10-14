package com.juegocolaborativo.activity;

/**
 * Created by Matias on 28/04/14.
 */
import android.app.Activity;
import android.os.Bundle;

import com.juegocolaborativo.fragment.PrefsFragment;

public class SetPreferenceActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction().replace(android.R.id.content,
                new PrefsFragment()).commit();
    }

}