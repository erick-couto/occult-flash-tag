package br.eti.erickcouto.occultflashtag;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.util.TypedValue;
import android.widget.ListView;
import android.view.LayoutInflater;
import android.view.ViewGroup;


//import android.preference.EditTextPreference;
//import android.preference.Preference;
//import android.preference.PreferenceFragment;

import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.recyclerview.widget.RecyclerView;

public class SettingsFragment extends PreferenceFragmentCompat  {

    @Override
    public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);
    }

}