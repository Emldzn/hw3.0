package com.example.hw3;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Slider;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class MainController {
    private ObservableList<String> trackList;
    private Label currentTrackLabel;
    private Label timeLabel;
    private Slider progressSlider;
    private ListView<String> trackListView;

    private MediaPlayer mediaPlayer;
    private List<File> audioFiles = new ArrayList<>();
    private int currentTrackIndex = -1;
    private boolean isPlaying = false;
    private boolean isSeeking = false;
    private boolean repeat = false;
    private boolean shuffle = false;
    private Random random = new Random();

    public void setUICallbacks(
            ObservableList<String> trackList,
            Label currentTrackLabel,
            Label timeLabel,
            Slider progressSlider,
            ListView<String> trackListView
    ) {
        this.trackList = trackList;
        this.currentTrackLabel = currentTrackLabel;
        this.timeLabel = timeLabel;
        this.progressSlider = progressSlider;
        this.trackListView = trackListView;
    }

    public void openDirectory(Stage stage, ListView<String> trackListView) {
        File directory = FileUtils.chooseDirectory(stage);
        if (directory != null) {
            audioFiles = FileUtils.getAudioFiles(directory);
            updateTrackList();
            if (!audioFiles.isEmpty()) {
                currentTrackIndex = 0;
                loadAndPrepareTrack(currentTrackIndex);
            }
        }
    }

    private void updateTrackList() {
        trackList.clear();
        for (File file : audioFiles) {
            trackList.add(FileUtils.getFileName(file.getPath()));
        }
    }

    public void play() {
        if (mediaPlayer == null && !audioFiles.isEmpty()) {
            loadAndPrepareTrack(currentTrackIndex);
        }

        if (mediaPlayer != null) {
            mediaPlayer.play();
            isPlaying = true;
            highlightCurrentTrack();
        }
    }

    public void pause() {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
            isPlaying = false;
        }
    }

    public void stop() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            isPlaying = false;
        }
    }

    public void previous() {
        if (audioFiles.isEmpty()) return;

        if (shuffle) {
            currentTrackIndex = random.nextInt(audioFiles.size());
        } else {
            currentTrackIndex = (currentTrackIndex - 1 + audioFiles.size()) % audioFiles.size();
        }

        loadAndPrepareTrack(currentTrackIndex);
        play();
    }

    public void next() {
        if (audioFiles.isEmpty()) return;

        if (shuffle) {
            currentTrackIndex = random.nextInt(audioFiles.size());
        } else {
            currentTrackIndex = (currentTrackIndex + 1) % audioFiles.size();
        }

        loadAndPrepareTrack(currentTrackIndex);
        play();
    }

    public void playSelected(int index) {
        if (index >= 0 && index < audioFiles.size()) {
            currentTrackIndex = index;
            loadAndPrepareTrack(currentTrackIndex);
            play();
        }
    }

    public void removeTrack(int index) {
        if (index < 0 || index >= audioFiles.size()) return;

        boolean wasCurrentTrack = (index == currentTrackIndex);
        boolean wasPlaying = isPlaying;

        if (wasCurrentTrack && mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
            mediaPlayer = null;
        }

        audioFiles.remove(index);
        trackList.remove(index);


        if (wasCurrentTrack) {
            if (audioFiles.isEmpty()) {
                currentTrackIndex = -1;
                currentTrackLabel.setText("Текущий трек:");
                timeLabel.setText("00:00/00:00");
                progressSlider.setValue(0);
            } else {
                currentTrackIndex = Math.min(index, audioFiles.size() - 1);
                if (wasPlaying) {
                    loadAndPrepareTrack(currentTrackIndex);
                    play();
                }
            }
        } else if (index < currentTrackIndex) {
            currentTrackIndex--;
        }
    }

    private void loadAndPrepareTrack(int index) {
        if (index < 0 || index >= audioFiles.size()) return;

        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
        }

        File audioFile = audioFiles.get(index);
        Media media = new Media(audioFile.toURI().toString());
        mediaPlayer = new MediaPlayer(media);


        currentTrackLabel.setText("Текущий трек: " + FileUtils.getFileName(audioFile.getPath()));


        mediaPlayer.currentTimeProperty().addListener((observable, oldValue, newValue) -> {
            if (!isSeeking) {
                updateProgress(newValue, mediaPlayer.getTotalDuration());
            }
        });


        mediaPlayer.setOnEndOfMedia(() -> {
            if (repeat) {
                mediaPlayer.seek(Duration.ZERO);
                mediaPlayer.play();
            } else {
                next();
            }
        });

        mediaPlayer.setOnReady(() -> {
            Duration total = mediaPlayer.getTotalDuration();
            updateProgress(Duration.ZERO, total);
        });

        highlightCurrentTrack();
    }

    private void updateProgress(Duration current, Duration total) {
        if (total.greaterThan(Duration.ZERO)) {
            double progress = current.toMillis() / total.toMillis() * 100;
            Platform.runLater(() -> {
                progressSlider.setValue(progress);
                timeLabel.setText(FileUtils.formatTime(current) + "/" + FileUtils.formatTime(total));
            });
        }
    }

    public void beginSeek() {
        isSeeking = true;
    }

    public void endSeek(double value) {
        if (mediaPlayer != null) {
            Duration total = mediaPlayer.getTotalDuration();
            double seekTime = total.toMillis() * (value / 100.0);
            mediaPlayer.seek(Duration.millis(seekTime));
        }
        isSeeking = false;
    }

    public void togglePlayPause() {
        if (mediaPlayer == null) {
            play();
            return;
        }

        if (isPlaying) {
            pause();
        } else {
            play();
        }
    }

    public void seekForward() {
        if (mediaPlayer != null) {
            Duration current = mediaPlayer.getCurrentTime();
            mediaPlayer.seek(current.add(Duration.seconds(5)));
        }
    }

    public void seekBackward() {
        if (mediaPlayer != null) {
            Duration current = mediaPlayer.getCurrentTime();
            mediaPlayer.seek(current.subtract(Duration.seconds(5)));
        }
    }

    public void increaseVolume() {
        if (mediaPlayer != null) {
            double volume = mediaPlayer.getVolume();
            mediaPlayer.setVolume(Math.min(1.0, volume + 0.1));
        }
    }

    public void decreaseVolume() {
        if (mediaPlayer != null) {
            double volume = mediaPlayer.getVolume();
            mediaPlayer.setVolume(Math.max(0.0, volume - 0.1));
        }
    }

    public void setRepeat(boolean repeat) {
        this.repeat = repeat;
    }

    public void setShuffle(boolean shuffle) {
        this.shuffle = shuffle;
    }

    private void highlightCurrentTrack() {
        Platform.runLater(() -> {
            trackListView.getSelectionModel().select(currentTrackIndex);
            trackListView.scrollTo(currentTrackIndex);
        });
    }

    public void cleanUp() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
        }
    }
}