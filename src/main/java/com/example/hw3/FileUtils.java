package com.example.hw3;

import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FileUtils {
    private static final List<String> SUPPORTED_EXTENSIONS = Arrays.asList(".mp3");

    public static File chooseDirectory(Stage stage) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Открыть папку");
        return directoryChooser.showDialog(stage);
    }

    public static List<File> getAudioFiles(File directory) {
        List<File> audioFiles = new ArrayList<>();
        if (directory != null && directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile() && isAudioFile(file)) {
                        audioFiles.add(file);
                    }
                }
            }
        }
        return audioFiles;
    }

    private static boolean isAudioFile(File file) {
        String name = file.getName().toLowerCase();
        for (String extension : SUPPORTED_EXTENSIONS) {
            if (name.endsWith(extension)) {
                return true;
            }
        }
        return false;
    }

    public static String formatTime(Duration duration) {
        if (duration == null) {
            return "00:00";
        }

        int seconds = (int) Math.floor(duration.toSeconds());
        int minutes = seconds / 60;
        int remainingSeconds = seconds % 60;

        return String.format("%02d:%02d", minutes, remainingSeconds);
    }

    public static String getFileName(String path) {
        if (path == null || path.isEmpty()) {
            return "";
        }

        File file = new File(path);
        String fileName = file.getName();

        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0) {
            fileName = fileName.substring(0, dotIndex);
        }

        return fileName;
    }
}