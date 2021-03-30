package com.mrshiehx.file.manager.shared.variables;

import com.mrshiehx.file.manager.providers.AuthorInformationProvider;

public class AuthorInformation implements AuthorInformationProvider {
    @Override
    public String getName() {
        return "MrShiehX";
    }

    @Override
    public String getEmailAddress() {
        return "Bntoylort@outlook.com";
    }

    @Override
    public String getMainPageUrl() {
        return "https://mrshieh-x.github.io";
    }

    @Override
    public String getGithubUrl() {
        return "https://www.github.com/MrShieh-X";
    }

    @Override
    public String getOpenSourceRepositoryUrl() {
        return "https://www.github.com/MrShieh-X/msfilemanager";
    }
}
