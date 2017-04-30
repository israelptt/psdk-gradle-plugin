package com.tabtale.publishingsdk

/**
 * Created by Rona on 03/04/2017.
 */
class PSDKUtils {
    static String autoCompleteVersion(String version) {
        if (version == '+') {
            return '+'
        }

        if (version == null || version.isEmpty()) {
            return '0.0.0.0.+'
        }

        def finalVersion = version
        def versionArr = version.trim().split("\\s+")
        def buildNUmber = null
        if (versionArr.length == 2) {
            finalVersion = versionArr[0]
            buildNUmber = versionArr[1].replace("b", "")
        }

        finalVersion = completeVersionWithZero(finalVersion, 4)

        if (buildNUmber != null) {
            finalVersion += "." + buildNUmber
        }

        def finalVersionArr = finalVersion.split("\\.")

        if (finalVersionArr.length > 5) {
            return null
        }

        if (finalVersionArr.length == 4) {
            finalVersion += ".+"
        }

        return finalVersion
    }

    static String completeVersionWithZero(String version, int expectedLengthVersionWithZero) {
        List<String> versionList = new ArrayList<>(Arrays.asList(version.replaceAll("\\s+", "").split("[\\.]")))
        if (versionList.size() >= expectedLengthVersionWithZero) {
            return version
        }

        for (int i = versionList.size(); i < expectedLengthVersionWithZero; i++) {
            versionList.add("0")
        }

        return versionList.join('.')
    }
}
