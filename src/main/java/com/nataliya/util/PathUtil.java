package com.nataliya.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PathUtil {

    public static String normalizePath(String path) {
        if (path == null || path.isBlank()) {
            return "/";
        }
        return path.startsWith("/") ? path.substring(1) : path;
    }

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

    public static boolean isDirectory(String path) {
        return path.endsWith("/");
    }

    public static String extractResourceName(String path, boolean requireTrailingSlash) {
        path = formatPath(path, false, false);
        int lastSlash = path.lastIndexOf('/');
        String resourceName = path.substring(lastSlash + 1);
        return requireTrailingSlash ? resourceName + "/" : resourceName;
    }

    public static String extractParentDirectoryPath(String path) {
        path = formatPath(path, false, false);
        int lastSlash = path.lastIndexOf('/');
        return lastSlash > 0 ? path.substring(0, lastSlash + 1) : "/";
    }

    public static String extractRelativePath(String fullPath, String resourcePath) {
        return fullPath.substring(resourcePath.length());
    }

    public static String extractZipPath(String fullPath, String resourcePath) {
        String relativePath = extractRelativePath(fullPath, resourcePath);
        String rootFolder = extractResourceName(resourcePath, true);
        return rootFolder + relativePath;
    }
}
