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
import android.widget.Toast;

import com.duowei.tvshow.contact.Consts;
import com.duowei.tvshow.event.Update;
import com.duowei.tvshow.fragment.UpdateFragment;
import com.duowei.tvshow.httputils.Post6;
import com.duowei.tvshow.utils.Version;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;


public class SettingActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener,
        Preference.OnPreferenceClickListener {
    private EditTextPreference mEtPreference1;
    private EditTextPreference mEtPreference2;
    private EditTextPreference mEtPreference3;
    private ListPreference mListPreference;
    private CheckBoxPreference mCheckPreference;
    private SharedPreferences.Editor mEdit;
    private Intent mIntent;
    private ListPreference mListKey2;
    private EditTextPreference mEtPreference4;
    private Preference mListKey3;
    private Preference mListKey4;
    private ListPreference mListKey5;
    private ListPreference mListKey6;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
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
        mListKey2 = (ListPreference) findPreference("list_key2");//呼叫显示时长
        mListKey3 = findPreference("list_key3");//等待中字体
        mListKey4 = findPreference("list_key4");//呼叫中字体
        mListKey5 = (ListPreference) findPreference("list_key5");//屏占比
        mListKey6 = (ListPreference) findPreference("list_key6");
        mEtPreference4 = (EditTextPreference) findPreference("edittext_key4");//前台IP
        Preference version = findPreference("version");
        version.setSummary(Version.getVersionName(this));

        findPreference("dect_settings").setOnPreferenceClickListener(this);
        version.setOnPreferenceClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Setup the initial values
        SharedPreferences sharedPreferences = getPreferenceScreen().getSharedPreferences();
        mListPreference.setSummary(sharedPreferences.getString(Consts.LIST_KEY, ""));

        String edittext_key1 = sharedPreferences.getString("edittext_key1", "");
        String url= TextUtils.isEmpty(edittext_key1)?getString(R.string.setting_url):edittext_key1;
        mEdit.putString("wurl",url);
        String edittext_key2 = sharedPreferences.getString("edittext_key2", "");
        String weid=TextUtils.isEmpty(edittext_key2)?getString(R.string.weid):edittext_key2;
        mEdit.putString("weid",weid);
        mEdit.commit();

        mEtPreference1.setSummary(url);
        mEtPreference2.setSummary(weid);
        mEtPreference3.setSummary(sharedPreferences.getString("edittext_key3", ""));
        mCheckPreference.setChecked(sharedPreferences.getBoolean("checkbox_key",true));
        mListKey2.setSummary(sharedPreferences.getString("list_key2","关闭"));
        mListKey3.setSummary(sharedPreferences.getString("list_key3","20"));
        mListKey4.setSummary(sharedPreferences.getString("list_key4","30"));
        mListKey5.setSummary(sharedPreferences.getString("list_key5","1:2"));
        mListKey6.setSummary(sharedPreferences.getString("list_key6",getString(R.string.offLine)));

        mEtPreference4.setSummary(sharedPreferences.getString("edittext_key4",""));
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
            mEtPreference1.setSummary(sharedPreferences.getString(key, "20"));
            mEdit.putString("wurl",sharedPreferences.getString(key, "20"));
        }else if(key.equals("edittext_key2")){//微信ID
            mEtPreference2.setSummary(sharedPreferences.getString(key,"20"));
            mEdit.putString("weid",sharedPreferences.getString(key,"20"));
        }else if(key.equals("edittext_key3")){//门店ID
            mEtPreference3.setSummary(sharedPreferences.getString(key, "20"));
            mEdit.putString("storeid",sharedPreferences.getString(key, "20"));
        } else if(key.equals(Consts.LIST_KEY)) {//电视区号
            mListPreference.setSummary(sharedPreferences.getString(key, ""));
            mEdit.putString("zoneNum",sharedPreferences.getString(key, ""));
        }else if(key.equals(Consts.CHECKOUT_KEY)){//是否开机自启动
            mCheckPreference.setChecked(sharedPreferences.getBoolean(key,true));
            mEdit.putBoolean(Consts.CHECKOUT_KEY,sharedPreferences.getBoolean(key,true));
        }else if(key.equals("list_key2")){//呼叫取菜
            mListKey2.setSummary(sharedPreferences.getString(key,""));
            mEdit.putString("callvalue",sharedPreferences.getString(key,"关闭"));
        }else if(key.equals("list_key3")){//等待中字体大小
            mListKey3.setSummary(sharedPreferences.getString(key,""));
            mEdit.putString("waittext",sharedPreferences.getString(key,"20"));
        }else if(key.equals("list_key4")){//呼叫中字体
            mListKey4.setSummary(sharedPreferences.getString(key,""));
            mEdit.putString("calltext",sharedPreferences.getString(key,"20"));
        }else if(key.equals("list_key5")){//屏占比
            mListKey5.setSummary(sharedPreferences.getString(key,""));
            mEdit.putString("view_weight",sharedPreferences.getString(key,"1:2"));
        }else if(key.equals("list_key6")){
            mListKey6.setSummary(sharedPreferences.getString(key,""));
            mEdit.putString("soundstytle",sharedPreferences.getString(key,getString(R.string.onLine)));
        }else if(key.equals("edittext_key4")){//前台IP
            mEtPreference4.setSummary(sharedPreferences.getString(key,""));
            mEdit.putString("ip",sharedPreferences.getString(key,""));
        }
        mEdit.commit();
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if(preference.getKey().equals("dect_settings")){//确定
            SharedPreferences spf = getPreferenceScreen().getSharedPreferences();
            if(spf.getString(Consts.LIST_KEY,"").equals("")||spf.getString("edittext_key3","").equals("")){
                AlertDialog.Builder dialog = new AlertDialog.Builder(this);
                dialog.setTitle("提示")
                        .setMessage("设置信息未填完整，是否退出？")
                        .setPositiveButton("退出", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
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
        }
        else if(preference.getKey().equals("version")){//版本更新
            Post6.instance().getVersion();
        }
        return false;
    }

    @Subscribe
    public void update(Update event){
        int version = Integer.parseInt(event.versionCode);
        if(version> Version.getVersionCode(this)){
            UpdateFragment updateFragment = UpdateFragment.newInstance(event.url, event.name);
            updateFragment.show(getFragmentManager(),getString(R.string.update));
        }else{
            Toast.makeText(this,"当前己是最新版",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        mIntent = new Intent(this, MainActivity.class);
        startActivity(mIntent);
        finish();
    }
}
