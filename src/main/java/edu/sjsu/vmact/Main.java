package edu.sjsu.vmact;

public class Main {
    public static void main(String[] args) {
        System.out.println("vmact starting: brrrrrr");

        if (args.length == 0) {
            System.out.println("Usage: java -jar vmact.jar <memory-file>");
        } else {
            System.out.println("Input file: " + args[0]);
        }
    }
}