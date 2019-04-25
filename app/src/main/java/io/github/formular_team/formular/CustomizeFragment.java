package io.github.formular_team.formular;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;

public class CustomizeFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.addPreferencesFromResource(R.xml.preferences);

        final SharedPreferences prefs = this.getPreferenceScreen().getSharedPreferences();
        this.onSharedPreferenceChanged(prefs, "prefName");
    }

    @Override
    public void onResume() {
        super.onResume();
        this.getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        this.getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, final String key) {
        final Preference pref = this.findPreference(key);
        switch(key) {
        case "prefName":
            final EditTextPreference thisPref = (EditTextPreference) pref;
            thisPref.setSummary(thisPref.getText());
        }
    }
}
