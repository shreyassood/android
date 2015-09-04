package hacktx.hacktx2015.fragments;

import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;

import hacktx.hacktx2015.BuildConfig;
import hacktx.hacktx2015.R;
import hacktx.hacktx2015.activities.DebugActivity;
import hacktx.hacktx2015.services.BeaconService;

public class DebugFragment extends PreferenceFragment {

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences_debug);

        final PreferenceScreen buildConfigCheckIn = (PreferenceScreen) findPreference(getString(R.string.debug_build_config_check_in_key));
        buildConfigCheckIn.setSummary(BuildConfig.IN_APP_CHECK_IN ? R.string.debug_build_config_enabled : R.string.debug_build_config_disabled);

        final PreferenceScreen buildConfigFeedback = (PreferenceScreen) findPreference(getString(R.string.debug_build_config_feedback_key));
        buildConfigFeedback.setSummary(BuildConfig.IN_APP_FEEDBACK ? R.string.debug_build_config_enabled : R.string.debug_build_config_disabled);

        final PreferenceScreen bleStatus = (PreferenceScreen) findPreference(getString(R.string.debug_beacon_status_key));
        bleStatus.setSummary(doesDeviceSupportBle() ? R.string.debug_ble_support_true : R.string.debug_ble_support_false);

        final CheckBoxPreference hacktxUtilsOverride = (CheckBoxPreference) findPreference(getString(R.string.debug_hacktx_utils_override_key));
        setHackTXUtilOverridesEnabled(hacktxUtilsOverride.isChecked());
        hacktxUtilsOverride.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                setHackTXUtilOverridesEnabled((boolean) newValue);
                return true;
            }
        });
    }

    private void setHackTXUtilOverridesEnabled(boolean enabled) {
        CheckBoxPreference hacktxUtilsStarted = (CheckBoxPreference) findPreference(getString(R.string.debug_hacktx_utils_started_key));
        CheckBoxPreference hacktxUtilsEnded = (CheckBoxPreference) findPreference(getString(R.string.debug_hacktx_utils_ended_key));

        hacktxUtilsStarted.setEnabled(enabled);
        hacktxUtilsEnded.setEnabled(enabled);
    }

    private boolean doesDeviceSupportBle() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2 &&
                BluetoothAdapter.getDefaultAdapter() != null &&
                getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
    }
}
