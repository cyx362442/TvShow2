package com.duowei.tvshow;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;

import com.duowei.tvshow.contact.Consts;

import org.litepal.util.Const;

public class SettingActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener, Preference.OnPreferenceClickListener {
    private EditTextPreference mEtPreference1;
    private EditTextPreference mEtPreference2;
    private EditTextPreference mEtPreference3;
    private ListPreference mListPreference;
    private CheckBoxPreference mCheckPreference;
    private SharedPreferences.Editor mEdit;
    private Intent mIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_setting);
        SharedPreferences preferences = getSharedPreferences("Users", Context.MODE_PRIVATE);
        mEdit = preferences.edit();
        addPreferencesFromResource(R.xml.preferenc);
        initPreferences();
    }
    private void initPreferences() {
        mEtPreference1 = (EditTextPreference)findPreference("edittext_key1");
        mEtPreference2 = (EditTextPreference)findPreference("edittext_key2");
        mEtPreference3 = (EditTextPreference)findPreference("edittext_key3");
        mListPreference = (ListPreference)findPreference(Consts.LIST_KEY);
        mCheckPreference = (CheckBoxPreference)findPreference(Consts.CHECKOUT_KEY);
        findPreference("dect_settings").setOnPreferenceClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Setup the initial values
        SharedPreferences sharedPreferences = getPreferenceScreen().getSharedPreferences();
        mListPreference.setSummary(sharedPreferences.getString(Consts.LIST_KEY, ""));

        String edittext_key1 = sharedPreferences.getString("edittext_key1", "");
        String url= TextUtils.isEmpty(edittext_key1)?"ai.wxdw.top":edittext_key1;
        mEtPreference1.setSummary(url);
        mEtPreference2.setSummary(sharedPreferences.getString("edittext_key2", ""));
        mEtPreference3.setSummary(sharedPreferences.getString("edittext_key3", ""));
        // Set up a listener whenever a key changes
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Unregister the listener whenever a key changes
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals("edittext_key1")) { //服务器地址
//            mEtPreference1.setSummary(sharedPreferences.getString(key, "20"));
//            mEdit.putString("wurl",sharedPreferences.getString(key, "20"));
        }else if(key.equals("edittext_key2")){//微信ID
            String edittext_key1 = sharedPreferences.getString("edittext_key1", "");
            String url= TextUtils.isEmpty(edittext_key1)?"ai.wxdw.top":edittext_key1;
            mEdit.putString("wurl",url);

            mEtPreference2.setSummary(sharedPreferences.getString(key, "20"));
            mEdit.putString("weid",sharedPreferences.getString(key, "20"));
        }else if(key.equals("edittext_key3")){//门店ID
            mEtPreference3.setSummary(sharedPreferences.getString(key, "20"));
            mEdit.putString("storeid",sharedPreferences.getString(key, "20"));
        } else if(key.equals(Consts.LIST_KEY)) {
            mListPreference.setSummary(sharedPreferences.getString(key, ""));
            mEdit.putString("zoneNum",sharedPreferences.getString(key, ""));
        }
        mEdit.commit();
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        SharedPreferences spf = getPreferenceScreen().getSharedPreferences();
        if(spf.getString(Consts.LIST_KEY,"").equals("")||
                spf.getString("edittext_key2","").equals("")||spf.getString("edittext_key3","").equals("")){
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setTitle("提示")
                    .setMessage("设置信息未填完整，是否退出？")
                    .setPositiveButton("退出", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
//                            mIntent = new Intent(SettingActivity.this, WelcomeActivity.class);
//                            startActivity(mIntent);
                            finish();
                        }
                    })
                    .setNegativeButton("取消",null)
                    .create().show();
        }else{
            SharedPreferences preferences = getSharedPreferences("Users", Context.MODE_PRIVATE);
            SharedPreferences.Editor edit = preferences.edit();
            edit.putString("version", "0");
            edit.commit();

            mIntent = new Intent(SettingActivity.this, WelcomeActivity.class);
            startActivity(mIntent);
            finish();
        }
        return false;
    }
}
