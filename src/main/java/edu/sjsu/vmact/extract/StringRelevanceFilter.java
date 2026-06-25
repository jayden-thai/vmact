package edu.sjsu.vmact.extract;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import edu.sjsu.vmact.pipeline.ScanConfig;

public class StringRelevanceFilter {
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\\b"
    );

    private static final Pattern URL_PATTERN = Pattern.compile(
            "\\bhttps?://[^\\s\"'<>]+"
    );

    private static final Pattern WINDOWS_FILE_PATH_PATTERN = Pattern.compile(
            "\\b[A-Za-z]:\\\\Users\\\\[^\\s\"'<>|]+"
    );

    private static final Pattern LINUX_USER_FILE_PATH_PATTERN = Pattern.compile(
            "/(?:home|media|mnt)/[^\\s\"'<>]+"
    );

    private static final Pattern FILE_URI_PATTERN = Pattern.compile(
            "file:///[A-Za-z0-9._~:/%+\\-=]+"
    );

    private static final Pattern DEVICE_ID_PATTERN = Pattern.compile(
            "(?:USBSTOR[\\\\#][^\\s\"'<>]+|DISK&VEN_[^\\s\"'<>]+|idVendor=[0-9A-Fa-f]{4})",
            Pattern.CASE_INSENSITIVE
    );

    private final List<String> lowercaseKeywords;

    public StringRelevanceFilter(List<String> keywords) {
        this.lowercaseKeywords = new ArrayList<>();

        for (String keyword : keywords) {
            String trimmed = keyword.trim();

            if (!trimmed.isEmpty()) {
                lowercaseKeywords.add(trimmed.toLowerCase(Locale.ROOT));
            }
        }
    }

    public static StringRelevanceFilter fromConfig(ScanConfig config) throws IOException {
        List<String> keywords = new ArrayList<>();

        for (String line : Files.readAllLines(config.getKeywordsFile())) {
            String trimmed = line.trim();

            if (!trimmed.isEmpty() && !trimmed.startsWith("#")) {
                keywords.add(trimmed);
            }
        }

        return new StringRelevanceFilter(keywords);
    }

    public boolean isRelevant(CharSequence value) {
        if (value == null || value.length() == 0) {
            return false;
        }

        if (EMAIL_PATTERN.matcher(value).find()) {
            return true;
        }

        if (URL_PATTERN.matcher(value).find()) {
            return true;
        }

        if (WINDOWS_FILE_PATH_PATTERN.matcher(value).find()) {
            return true;
        }

        if (LINUX_USER_FILE_PATH_PATTERN.matcher(value).find()) {
            return true;
        }

        if (FILE_URI_PATTERN.matcher(value).find()) {
            return true;
        }

        if (DEVICE_ID_PATTERN.matcher(value).find()) {
            return true;
        }

        return containsKeyword(value);
    }

    private boolean containsKeyword(CharSequence value) {
        String lowercaseValue = value.toString().toLowerCase(Locale.ROOT);

        for (String keyword : lowercaseKeywords) {
            if (lowercaseValue.contains(keyword)) {
                return true;
            }
        }

        return false;
    }
}