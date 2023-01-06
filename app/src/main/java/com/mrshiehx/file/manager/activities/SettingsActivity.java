package com.mrshiehx.file.manager.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.view.MenuItem;

import com.mrshiehx.file.manager.R;
import com.mrshiehx.file.manager.activities.base.BasePreferenceActivity;
import com.mrshiehx.file.manager.shared.variables.AuthorInformation;
import com.mrshiehx.file.manager.utils.ApplicationUtils;
import com.mrshiehx.file.manager.utils.ResourceUtils;
import com.mrshiehx.file.manager.utils.Utils;

public class SettingsActivity extends BasePreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    protected Context context = SettingsActivity.this;
    ListPreference language;
    ListPreference startupDir;
    EditTextPreference home;
    CheckBoxPreference getRoot;
    Preference about_application;
    Preference author;
    Preference about_email;
    Preference about_github;
    Preference about_open_source;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AuthorInformation authorInformationProvider = new AuthorInformation();
        addPreferencesFromResource(R.xml.preferences_settings);
        setTitle(ResourceUtils.getTextByLocale(R.string.activity_settings_name));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        language = (ListPreference) findPreference("language");
        startupDir = (ListPreference) findPreference("startupDir");
        home = (EditTextPreference) findPreference("home");
        getRoot = (CheckBoxPreference) findPreference("getRoot");
        about_application = findPreference("about_application");
        author = findPreference("author");
        about_email = findPreference("about_email");
        about_github = findPreference("about_github");
        about_open_source = findPreference("about_open_source");
        /*getRoot.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                try {
                    SystemUtils.getRootPermission();
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(context, getText(R.string.message_failed_to_get_root), Toast.LENGTH_SHORT).show();
                }
                return true;
            }
        });*/
        about_application.setTitle(String.format(getString(R.string.preference_settings_about_application_title), ApplicationUtils.getVersionName(), ApplicationUtils.getVersionCode()));
        author.setSummary(authorInformationProvider.getName());
        about_email.setSummary(authorInformationProvider.getEmailAddress());
        about_github.setSummary(authorInformationProvider.getGithubUrl());
        about_open_source.setSummary(authorInformationProvider.getOpenSourceRepositoryUrl());
        author.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Utils.goToWebsite(context, authorInformationProvider.getMainPageUrl());
                return true;
            }
        });
        about_email.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Utils.sendMail(context, authorInformationProvider.getEmailAddress(), "", "");
                return true;
            }
        });
        about_github.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Utils.goToWebsite(context, authorInformationProvider.getGithubUrl());
                return true;
            }
        });
        about_open_source.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Utils.goToWebsite(context, authorInformationProvider.getOpenSourceRepositoryUrl());
                return true;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) finish();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPause() {
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if ("language".equals(key)) dynamicModifyLanguageSummary();
        if ("startupDir".equals(key)) dynamicModifyStartupDirSummary();
        ;
        if ("home".equals(key)) dynamicModifyHomeSummary();
    }

    @Override
    protected void onResume() {
        super.onResume();
        dynamicModifyLanguageSummary();
        dynamicModifyStartupDirSummary();
        dynamicModifyHomeSummary();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    private void dynamicModifyLanguageSummary() {
        language.setSummary(language.getEntry());
    }

    private void dynamicModifyHomeSummary() {
        home.setSummary(home.getText());
    }

    private void dynamicModifyStartupDirSummary() {
        startupDir.setSummary(startupDir.getEntry());
        home.setEnabled(!"last".equals(startupDir.getValue()));
    }
}
