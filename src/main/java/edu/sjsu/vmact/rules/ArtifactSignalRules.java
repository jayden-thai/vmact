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
            ".mp3",
            ".mp4",
            ".gif"
    };

    private static final String[] HIGH_SIGNAL_EMAIL_PROVIDERS = {
            "@gmail.com",
            "@googlemail.com",
            "@yahoo.com",
            "@hotmail.com",
            "@outlook.com",
            "@live.com",
            "@icloud.com",
            "@me.com",
            "@proton.me",
            "@protonmail.com",
            "@pm.me",
            "@aol.com"
    };

    private static final String[] WEB_ACTIVITY_CONTEXT_TERMS = {
            "tor browser",
            "firefox",
            "browser",
            "duckduckgo",
            "search",
            "query",
            "urlbar",
            "history",
            "bookmark",
            "bookmarks",
            "href",
            "visited",
            "visit",
            "download",
            "downloads",
            "downloaded",
            "save link",
            "content-disposition",
            "user_pref",
            "places.sqlite",
            "cookies.sqlite",
            "cache",
            "http cache",
            "profile.default",
            "profile",
            "socks_username",
            "time_created"
    };

    private static final String[] COMMUNICATION_CONTEXT_TERMS = {
            "thunderbird",
            "imap",
            "smtp",
            "pop3",
            "mailbox",
            "inbox",
            "sent",
            "drafts",
            "message-id",
            "from:",
            "to:",
            "cc:",
            "bcc:",
            "reply-to",
            "account",
            "login",
            "auth",
            "oauth",
            "password",
            "prefs.js"
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

    public static boolean hasWebActivityContext(String value) {
        return containsAny(value, WEB_ACTIVITY_CONTEXT_TERMS);
    }

    public static boolean hasCommunicationContext(String value) {
        return containsAny(value, COMMUNICATION_CONTEXT_TERMS);
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