package edu.sjsu.vmact.rules;

import java.util.Locale;

public final class ArtifactSignalRules {
    private static final String[] LOW_SIGNAL_TECHNICAL_URL_TERMS = {
            "w3.org",
            "schemas.xmlsoap.org",
            "xmlenc",
            "xmldsig",
            "xmlns",
            "svg",
            "rdf",
            "ontology",
            "schema",
            "tracker.api.gnome.org",
            "freedesktop.org/standards",
            "www.freedesktop.org"
    };

    private static final String[] LOW_SIGNAL_SOFTWARE_EMAIL_TERMS = {
            "@1000.service",
            "toolkit@mozilla.org",
            "raymondhill.net",
            "automated-testing@tails.net",
            "noreply",
            "no-reply"
    };

    private static final String[] LOW_SIGNAL_SYSTEM_PATH_TERMS = {
            "/usr/",
            "/etc/",
            "/run/",
            "/var/",
            "/dev/",
            "/proc/",
            "/sys/",
            "/media/common/"
    };

    private static final String[] HIGH_SIGNAL_USER_PATH_TERMS = {
            "/home/",
            "/media/amnesia/",
            "/mnt/",
            "downloads",
            "documents",
            "desktop"
    };

    private static final String[] HIGH_SIGNAL_FILE_EXTENSION_TERMS = {
            ".java",
            ".pdf",
            ".doc",
            ".docx",
            ".ods",
            ".odt",
            ".txt",
            ".jpg",
            ".png",
            ".mp3"
    };

    private static final String[] HIGH_SIGNAL_EMAIL_PROVIDERS = {
            "@gmail",
            "@hotmail",
            "@yahoo",
            "@outlook",
            "@icloud",
            "@aol",
            "@proton",
            "@tuta"
    };

    private ArtifactSignalRules() {
    }

    public static boolean isLowSignalTechnicalUrl(String value) {
        return containsAny(value, LOW_SIGNAL_TECHNICAL_URL_TERMS);
    }

    public static boolean isLowSignalSoftwareEmail(String value) {
        return containsAny(value, LOW_SIGNAL_SOFTWARE_EMAIL_TERMS);
    }

    public static boolean isLowSignalSystemPath(String value) {
        return containsAny(value, LOW_SIGNAL_SYSTEM_PATH_TERMS);
    }

    public static boolean isHighSignalUserPath(String value) {
        return containsAny(value, HIGH_SIGNAL_USER_PATH_TERMS);
    }

    public static boolean hasHighSignalFileExtension(String value) {
        return containsAny(value, HIGH_SIGNAL_FILE_EXTENSION_TERMS);
    }

    public static boolean hasHighSignalEmailProvider(String value) {
        return containsAny(value, HIGH_SIGNAL_EMAIL_PROVIDERS);
    }

    private static boolean containsAny(String value, String[] needles) {
        String lowercaseValue = safe(value).toLowerCase(Locale.ROOT);

        for (String needle : needles) {
            if (lowercaseValue.contains(needle)) {
                return true;
            }
        }

        return false;
    }

    private static String safe(String value) {
        if (value == null) {
            return "";
        }

        return value;
    }
}