package com.nataliya.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PathUtil {

    public static String getFullDirectoryPath(String rootFolderName, Long userId, String relativeDirectoryPath) {
        if (!relativeDirectoryPath.endsWith("/")) {
            relativeDirectoryPath = relativeDirectoryPath + "/";
        }
        return (String.format(rootFolderName, userId) + relativeDirectoryPath)
                .replaceAll("//", "/");
    }

    public static String getDirectoryName(String relativeDirectoryPath) {
        relativeDirectoryPath = relativeDirectoryPath.replaceAll("/+$", "");
        int lastSlash = relativeDirectoryPath.lastIndexOf('/');
        return relativeDirectoryPath.substring(lastSlash + 1);

    }

    public static String getParentDirectoryPath(String relativeDirectoryPath) {
        relativeDirectoryPath = relativeDirectoryPath
                .replaceAll("^/+", "")
                .replaceAll("/+$", "");
        int lastSlash = relativeDirectoryPath.lastIndexOf('/');
        return relativeDirectoryPath.substring(0, lastSlash + 1);
    }

}
