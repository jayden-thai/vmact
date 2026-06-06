package edu.sjsu.vmact;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import edu.sjsu.vmact.correlate.NoOpCorrelator;
import edu.sjsu.vmact.detect.KeywordDetector;
import edu.sjsu.vmact.extract.AsciiStringExtractor;
import edu.sjsu.vmact.pipeline.ScanConfig;
import edu.sjsu.vmact.pipeline.ScanPipeline;
import edu.sjsu.vmact.report.ConsoleReporter;

public class Main {
    public static void main(String[] args) {
        System.out.println("VMACT - Volatile Memory Artifact Correlation Tool");

        if (args.length == 0) {
            printUsage();
        } else {
            String command = args[0];
            if (command.equals("scan")) {
                handleScanCommand(args);
            } else if (command.equals("help") || command.equals("--help") || command.equals("-h")) {
                printUsage();
            } else {
                System.err.println("Unknown command: " + command);
                printUsage();
                System.exit(1);
            }
        }
    }

    private static void handleScanCommand(String[] args) {
        /*
         * Expected format:
         * scan <input-file> --keywords <keywords-file> --out <output-dir>
         *
         * Example:
         * scan sample.mem --keywords keywords.txt --out out
         */

        if (args.length < 6) {
            System.err.println("Error: Not enough arguments for scan command.");
            printUsage();
            System.exit(1);
        }

        Path inputFile = Path.of(args[1]);
        Path keywordsFile = null;
        Path outputDir = null;

        for (int i = 2; i < args.length; i++) {
            String current = args[i];

            if (current.equals("--keywords")) {
                if (i + 1 >= args.length) {
                    System.err.println("Error: --keywords requires a file path.");
                    System.exit(1);
                }
                keywordsFile = Path.of(args[i + 1]);
                i++;
            } else if (current.equals("--out")) {
                if (i + 1 >= args.length) {
                    System.err.println("Error: --out requires a directory path.");
                    System.exit(1);
                }
                outputDir = Path.of(args[i + 1]);
                i++;
            } else {
                System.err.println("Error: Unknown option: " + current);
                printUsage();
                System.exit(1);
            }
        }

        validateScanInputs(inputFile, keywordsFile, outputDir);

        System.out.println("Scan command recognized");
        System.out.println("Input file: " + inputFile);
        System.out.println("Keywords file: " + keywordsFile);
        System.out.println("Output directory: " + outputDir);

        ScanConfig config = new ScanConfig(inputFile, keywordsFile, outputDir);
        
        ScanPipeline pipeline = new ScanPipeline(config, 
            List.of(new AsciiStringExtractor()), 
            List.of(new KeywordDetector()), 
            new NoOpCorrelator(), 
            List.of(new ConsoleReporter())
        );

        try {
            pipeline.run();
        } catch (Exception e) {
            System.err.println("Scan failed: " + e.getMessage());
            System.exit(1);
        }
    }

    private static void validateScanInputs(Path inputFile, Path keywordsFile, Path outputDir) {
        if (!Files.exists(inputFile)) {
            System.err.println("Error: Input file does not exist: " + inputFile);
            System.exit(1);
        }

        if (!Files.isRegularFile(inputFile)) {
            System.err.println("Error: Input file is not a regular file: " + inputFile);
            System.exit(1);
        }

        if (keywordsFile == null) {
            System.err.println("Error: Missing required option --keywords.");
            System.exit(1);
        }

        if (!Files.exists(keywordsFile)) {
            System.err.println("Error: Keywords file does not exist: " + keywordsFile);
            System.exit(1);
        }

        if (!Files.isRegularFile(keywordsFile)) {
            System.err.println("Error: Keywords path is not a regular file: " + keywordsFile);
            System.exit(1);
        }

        if (outputDir == null) {
            System.err.println("Error: Missing required option --out.");
            System.exit(1);
        }

        try {
            Files.createDirectories(outputDir);
        } catch (Exception e) {
            System.err.println("Error: Could not create output directory: " + outputDir);
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }

    private static void printUsage() {
        System.out.println("\nUsage:");
        System.out.println("    vmact scan <input-file> --keywords <keywords-file> --out <output-directory>");
        System.out.println("\nExample:");
        System.out.println("    vmact scan sample.mem --keywords keywords.txt --out out\n");
    }
}