package com.tabtale.publishingsdk

/**
 * Created by Rona on 03/04/2017.
 */
class PSDKConsts {
    public final static String GROUP_ID = 'com.tabtale.publishingsdk'
    public final static String S3_ACCESS_KEY = 'AKIAIQNSIAQF2YN23M6Q'
    public final static String S3_SECRET_KEY = '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'
    public final static def PROJS_PACKAGE_MAP = [
            'chartboostcustomadapter': 'include',
            'ttflurryanalytics'      : 'flurry',
            'ttanalytics'            : 'ttanalytics',
            'deltadnaanalytics'      : 'deltadna',
            'startappadsproviders'   : 'startapp',
            "startappcustomadaptor"  : 'startapp',
            "inmobiadsproviders"     : 'inmobi',
            "applovinadsproviders"   : 'applovin',
            "applovincustomadaptor"  : 'applovin',
            "millennialcustomadaptor": 'millennial',
            "millennialadsproviders" : 'millennial'
    ]
    public final static def PROJS_SERVICES_MAP = [
            "publishingsdkcore"   : 'include',
            "psdkunity"           : 'include',
            "ttunity"             : 'include',
            "psdkgoogleanalytics" : 'exclude',
            "configurationfetcher": ['configurationFetcher'],
            "monetization"        : ['appShelf', 'promotionPage', 'chartboost', 'interstitial',
                                     'locationMgr'],
            "gameleveldata"       : ['gameLevelData'],
            "runtimeconfig"       : ['gameLevelData'],
            "banners"             : ['banners'],
            "splash"              : ['splash'],
            "rewardedads"         : ['rewardedAds'],
            "psdkappsflyer"       : ['appsFlyer'],
            "psdkanalytics"       : ['analytics'],
            "psdkcrashtool"       : ['crashMonitoringTool']
    ]
}
