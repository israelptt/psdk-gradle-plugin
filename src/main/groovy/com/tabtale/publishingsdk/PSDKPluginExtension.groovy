package com.tabtale.publishingsdk

import org.gradle.api.GradleException

/**
 * Created by Rona on 27/03/2017.
 */
class PSDKPluginExtension {
    private String sdkVersion = '+'
    private String config = 'compile'
    private String projPath
    private String buildType = 'release'
    private boolean isApp = true
    private boolean downloadConfigFile = false
    private boolean isUnityBuild = false
    private String gatewayBuildConfigEnv
    private String store
    private List<String> projs

    void initProjs() {
        projs = ["publishingsdkcore",
                 "configurationfetcher",
                 "rewardedads",
                 "monetization",
                 "banners",
                 "startappadsproviders",
                 "millennialadsproviders",
                 "inmobiadsproviders",
                 "applovinadsproviders",
                 "applovincustomadaptor",
                 "startappcustomadaptor",
                 "millennialcustomadaptor",
                 "psdkanalytics",
                 "gameleveldata",
                 "runtimeconfig",
                 "psdkyoutube",
                 "psdkgoogleanalytics",
                 "splash",
                 "psdkappsflyer",
                 "psdkunity",
                 "ttflurryanalytics",
                 "ttanalytics",
                 "deltadnaanalytics",
                 "psdkcrashtool",
                 "ttunity",
                 "chartboostcustomadapter"]
    }

    void setIsUnityBuild(boolean isUnityBuild) {
        this.isUnityBuild = isUnityBuild
    }

    boolean getIsUnityBuild() {
        return isUnityBuild
    }

    void setStore(String store) {
        this.store = store
    }

    String getStore() {
        return store
    }

    void setProjPath(String projPath) {
        this.projPath = projPath
    }

    String getProjPath() {
        return projPath
    }

    void setGatewayBuildConfigEnv(String gatewayBuildConfigEnv) {
        this.gatewayBuildConfigEnv = gatewayBuildConfigEnv
    }

    String getGatewayBuildConfigEnv() {
        return gatewayBuildConfigEnv
    }

    void setDownloadConfigFile(String downloadConfigFile) {
        this.downloadConfigFile = Boolean.parseBoolean(downloadConfigFile)
    }

    boolean getDownloadConfigFile() {
        return downloadConfigFile
    }

    void setIsApp(boolean isApp) {
        this.isApp = isApp
    }

    boolean getIsApp() {
        return isApp
    }

    void setSdkVersion(String sdkVersion) {
        if (!(this.sdkVersion = PSDKUtils.autoCompleteVersion(sdkVersion))) {
            throw new GradleException("Invalid PSDK version, the format should be X.X.X.X or " +
                    "X.X.X.X.X, instead it's: $sdkVersion")
        }
    }

    String getSdkVersion() {
        return sdkVersion
    }

    void setConfig(String config) {
        this.config = config
    }

    String getConfig() {
        return config
    }

    void setBuildType(String buildType) {
        this.buildType = buildType
    }

    String getBuildType() {
        return buildType
    }

    void setProjs(List<String> projs) {
        this.projs = projs
    }

    List<String> getProjs() {
        if (projs == null) {
            initProjs()
        }
        return projs
    }
}
