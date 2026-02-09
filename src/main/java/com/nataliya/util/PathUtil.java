package com.nataliya.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PathUtil {

    public static String formatPath(String path, boolean requireLeadingSlash, boolean requireTrailingSlash) {
        if (path == null || path.isBlank()) {
            return "/";
        }

        String formatted = path;

        // strip all leading & trailing slashes
        formatted = formatted.replaceAll("^/+", "")
                .replaceAll("/+$", "");

        if (requireLeadingSlash) {
            formatted = "/" + formatted;
        }
        if (requireTrailingSlash) {
            formatted = formatted + "/";
        }

        return formatted.isEmpty() ? "/" : formatted;
    }

    public static String getFullDirectoryPath(String rootFolderName, Long userId, String relativeDirectoryPath) {
        relativeDirectoryPath = formatPath(relativeDirectoryPath, false, true);
        return (String.format(rootFolderName, userId) + relativeDirectoryPath)
                .replaceAll("//", "/");
    }

    public static String getFullResourcePath(String rootFolderName, Long userId, String relativeResourcePath) {
        return (String.format(rootFolderName, userId) + relativeResourcePath)
                .replaceAll("//", "/");
    }

    public static String getResourceName(String path, boolean requireTrailingSlash) {
        path = formatPath(path, false, false);
        int lastSlash = path.lastIndexOf('/');
        String resourceName = path.substring(lastSlash + 1);
        return requireTrailingSlash ? resourceName + "/" : resourceName;

    }

    public static String getParentDirectoryPath(String path) {
        path = formatPath(path, false, false);
        int lastSlash = path.lastIndexOf('/');
        return path.substring(0, lastSlash + 1);
    }

    public static String buildFullObjectName(String rootFolderName, Long userId, String relativeDirectoryPath) {
        return getFullDirectoryPath(rootFolderName, userId, relativeDirectoryPath) + UUID.randomUUID();
    }

}
