package com.tabtale.plugins.publishingsdk

import groovy.json.JsonSlurper
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.DependencyResolutionListener
import org.gradle.api.artifacts.ResolvableDependencies
import org.gradle.api.credentials.AwsCredentials

/**
 * Created by Rona on 27/03/2017.
 */
class PSDKPlugin implements Plugin<Project> {
    static Project proj
    static String sdkVersion
    static String config
    static String projPath
    static String buildType
    static boolean isApp
    static boolean downloadConfigFile
    static boolean isUnityBuild
    static String gatewayBuildConfigEnv
    static File configFile
    static String store
    static List<String> projs
    static def projsPackageMap
    static def projsServicesMap
    static def configsJSON

    void initConfigsJSON() {
        if (configsJSON != null) {
            return
        }

        proj.exec {
            executable 'sh'
            args '-c', 'pip install --upgrade --user awscli'
        }

        proj.exec {
            executable 'sh'
            environment 'AWS_ACCESS_KEY_ID', PSDKConsts.S3_ACCESS_KEY
            environment 'AWS_SECRET_ACCESS_KEY', PSDKConsts.S3_SECRET_KEY
            args '-c', "~/Library/Python/2.7/bin/aws s3 cp s3://com.tabtale.repo/android/maven/psdk/artifacts/${buildType}/com/tabtale/publishingsdk/configs/${sdkVersion}/configs-${sdkVersion}.json ./PSDK_configs.json"
        }

        configsJSON = new JsonSlurper().parse(proj.file('PSDK_configs.json'))
    }

    def getProjsPackageMap() {
        if (projsPackageMap == null) {
            initConfigsJSON()
            projsPackageMap = configsJSON['projs_package_map']
        }

        return projsPackageMap
    }

    def getProjsServicesMap() {
        if (projsServicesMap == null) {
            initConfigsJSON()
            projsServicesMap = configsJSON['projs_services_map']
        }

        return projsServicesMap
    }

    void apply(Project proj) {
        proj.extensions.create('psdk', PSDKPluginExtension)
        this.proj = proj

        handleConfigFile()
        addRepos()
        handleDeps()
    }

    void handleConfigFile() {
        beforeResolveDeps {
            this.isApp = proj.psdk.isApp

            if (!isApp) {
                return
            }

            this.downloadConfigFile = proj.psdk.downloadConfigFile
            this.gatewayBuildConfigEnv = proj.psdk.gatewayBuildConfigEnv
            this.isUnityBuild = proj.psdk.isUnityBuild
            this.configFile = proj.file("src/main/assets/psdk.json")

            if (isUnityBuild) {
                this.store = proj.psdk.store
                if (store == null || store.isEmpty()) {
                    throw new GradleException("ERROR: psdk.store does not exists")
                }

                this.configFile = proj.file("assets/psdk_${store}.json")
            }

            if (!downloadConfigFile) {
                if (!configFile.exists()) {
                    throw new GradleException("ERROR: The $configFile.absolutePath config file " +
                            "does not exists")
                }
                return
            }

            if (gatewayBuildConfigEnv == null || gatewayBuildConfigEnv.isEmpty()) {
                throw new GradleException("ERROR: 'gatewayBuildConfigEnvURL' property does not exists")
            }

            def configFileText
            def gatewayURL = new URL(gatewayBuildConfigEnv)
            println "Making a request the get the PSDK json from the URL: $gatewayURL"

            try {
                configFileText = gatewayURL.getText()
            } catch (IOException e) {
                throw new GradleException("ERROR: failed to read the PSDK json from the url: ${gatewayURL}, " +
                        "exception: ${e.getMessage()}")
            }

            println "Writing the PSDK json to the file: ${configFile.absolutePath}"
            try {
                configFile.write configFileText
            } catch (IOException e) {
                throw new GradleException("ERROR: Failed to write to the PSDK json file: ${configFile.absolutePath}")
            }
        }
    }

    void addRepos() {
        beforeResolveDeps {
            this.buildType = proj.psdk.buildType
            this.projPath = proj.psdk.projPath
            println "PSDK Build Type: $buildType"

            proj.repositories {
                jcenter()
                mavenCentral()
                maven {
                    name 'debugPSDKArtifacts'
                    url "s3://com.tabtale.repo/android/maven/psdk/artifacts/$buildType"
                    credentials(AwsCredentials) {
                        accessKey PSDKConsts.S3_ACCESS_KEY
                        secretKey PSDKConsts.S3_SECRET_KEY
                    }
                }
                maven {
                    name 'thirdparty'
                    url 's3://com.tabtale.repo/android/maven/thirdparty'
                    credentials(AwsCredentials) {
                        accessKey PSDKConsts.S3_ACCESS_KEY
                        secretKey PSDKConsts.S3_SECRET_KEY
                    }
                }
                flatDir {
                    dirs("$projPath/build_sym_links/Unity/PlaybackEngines/AndroidPlayer/Variations/mono/Release/Classes",
                            "$projPath/build_sym_links/Unity/Unity.app/Contents/PlaybackEngines/AndroidPlayer/Variations/mono/Release/Classes",
                            "$projPath/build_sym_links/Unity/PlaybackEngines/AndroidPlayer/Variations/mono/Release/Classes",
                            "$projPath/build_sym_links/Unity/Unity.app/Contents/PlaybackEngines/AndroidPlayer/release/bin"
                    )
                }
            }
        }
    }

    void handleDeps() {
        beforeResolveDeps {
            this.sdkVersion = proj.psdk.sdkVersion
            this.config = proj.psdk.config
            this.isApp = proj.psdk.isApp
            this.projs = proj.psdk.projs

            handleDepsApp()

            def binProjs = []
            def debugProjs = []

            projs.each {
                if (proj.findProject(":${it}")) {
                    debugProjs.add(it)
                    return
                }
                binProjs.add("$PSDKConsts.GROUP_ID:$it:$sdkVersion")
            }

            println "PSDK Version: $sdkVersion"
            println "PSDK Config: $config"
            println "PSDK Is App: $isApp"
            println "PSDK Binary Projects: $binProjs"
            println "PSDK Debug Projects: $debugProjs"

            addDeps(debugProjs, config, true)
            addDeps(binProjs, config)
        }
    }

    void handleDepsApp() {
        if (!isApp) {
            return
        }

        def projsPackageMapTemp = getProjsPackageMap()
        def projsServicesMapTemp = getProjsServicesMap()

        def configJSONText
        try {
            configJSONText = configFile.text
        } catch (IOException e) {
            throw new GradleException("ERROR: failed to read the PSDK json from the path: " +
                    "${psdkJsonFile.absolutePath}, exception: ${e.getMessage()}")
        }

        def jsonSlurper = new JsonSlurper()
        def configJSON = jsonSlurper.parseText(configJSONText)

        projs = []
        def excludedProjs = []

        if (!configJSON.containsKey('packages')) {
            println "WARNING: The 'packages' key does not exists in psdk.json so all the projects" +
                    " will include: ${projsPackageMapTemp.keySet()}"
            projs.addAll(projsPackageMapTemp.keySet())
        } else {
            for (psdkPackage in projsPackageMapTemp) {
                if (psdkPackage.value == 'include') {
                    projs.add(psdkPackage.key)
                    continue
                }

                if (!configJSON.packages.containsKey(psdkPackage.value)) {
                    println "WARNING: The package: $psdkPackage.key include in the app but this " +
                            "package key does not exists in psdk.json"
                    projs.add(psdkPackage.key)
                    continue
                }

                if (configJSON.packages."$psdkPackage.value") {
                    projs.add(psdkPackage.key)
                    continue
                }

                excludedProjs.add(psdkPackage.key)
            }
        }

        for (projServices in projsServicesMapTemp) {
            if (projServices.value == 'exclude') {
                excludedProjs.add(projServices.key)
                continue
            }

            if (projServices.value == 'include') {
                projs.add(projServices.key)
                continue
            }

            def include = false
            for (psdkService in projServices.value) {
                if (!configJSON.containsKey(psdkService)) {
                    println "WARNING: The service: $psdkService include in the app but this " +
                            "service key does not exists in psdk.json"
                    projs.add(projServices.key)
                    include = true
                    continue
                }

                if (!configJSON."$psdkService".containsKey('included')) {
                    println "WARNING: The service: $psdkService include in the app but " +
                            "the 'included' key does not exists for this service"
                    projs.add(projServices.key)
                    include = true
                    continue
                }

                if (configJSON."$psdkService".included) {
                    projs.add(projServices.key)
                    include = true
                    break
                }
            }

            if (!include) {
                excludedProjs.add(projServices.key)
            }
        }

        println "PSDK Excluded Projects: " + excludedProjs
    }

    void beforeResolveDeps(Closure closure) {
        proj.getGradle().addListener(new DependencyResolutionListener() {
            @Override
            void beforeResolve(ResolvableDependencies resolvableDependencies) {
                closure()
                proj.getGradle().removeListener(this)
            }

            @Override
            void afterResolve(ResolvableDependencies resolvableDependencies) {}
        })
    }

    void addDeps(List deps, String config, isDebugProjs = false) {
        def compileDeps = proj.getConfigurations().getByName(config).getDependencies()
        deps.each {
            if (isDebugProjs) {
                compileDeps.add(proj.getDependencies().project(path: ":$it"))
                return
            }
            compileDeps.add(proj.getDependencies().create(it))
        }
    }
}