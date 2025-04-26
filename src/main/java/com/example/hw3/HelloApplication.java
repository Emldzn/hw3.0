package com.example.hw3;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;

public class HelloApplication extends Application {
    private MainController controller;
    private ListView<String> trackListView;
    private Label currentTrackLabel;
    private Label timeLabel;
    private Slider progressSlider;
    private Button playButton;
    private Button pauseButton;
    private Button stopButton;
    private Button prevButton;
    private Button nextButton;
    private CheckBox repeatBox;
    private CheckBox shuffleBox;

    @Override
    public void start(Stage stage) {
        controller = new MainController();


        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));


        Button openFolderButton = new Button("Открыть папку");
        openFolderButton.setOnAction(e -> controller.openDirectory(stage, trackListView));

        HBox topBox = new HBox(10, openFolderButton);
        topBox.setPadding(new Insets(0, 0, 10, 0));
        root.setTop(topBox);


        trackListView = new ListView<>();
        ObservableList<String> trackList = FXCollections.observableArrayList();
        trackListView.setItems(trackList);
        trackListView.setPrefHeight(200);
        trackListView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        int selectedIndex = trackListView.getSelectionModel().getSelectedIndex();
                        controller.playSelected(selectedIndex);
                    }
                });

        ContextMenu contextMenu = new ContextMenu();
        MenuItem deleteItem = new MenuItem("Удалить");
        deleteItem.setOnAction(e -> {
            int selectedIndex = trackListView.getSelectionModel().getSelectedIndex();
            if (selectedIndex >= 0) {
                controller.removeTrack(selectedIndex);
            }
        });
        contextMenu.getItems().add(deleteItem);
        trackListView.setContextMenu(contextMenu);

        root.setCenter(trackListView);


        VBox bottomBox = new VBox(10);
        bottomBox.setPadding(new Insets(10, 0, 0, 0));

        currentTrackLabel = new Label("Текущий трек:");


        progressSlider = new Slider(0, 100, 0);
        progressSlider.setPrefWidth(Double.MAX_VALUE);
        progressSlider.setOnMousePressed(e -> controller.beginSeek());
        progressSlider.setOnMouseReleased(e -> controller.endSeek(progressSlider.getValue()));

        timeLabel = new Label("00:00/00:00");


        prevButton = new Button("⏮ Prev");
        prevButton.setOnAction(e -> controller.previous());

        playButton = new Button("▶ Play");
        playButton.setOnAction(e -> controller.play());

        pauseButton = new Button("⏸ Pause");
        pauseButton.setOnAction(e -> controller.pause());

        stopButton = new Button("⏹ Stop");
        stopButton.setOnAction(e -> controller.stop());

        nextButton = new Button("⏭ Next");
        nextButton.setOnAction(e -> controller.next());

        HBox buttonBox = new HBox(10, prevButton, playButton, pauseButton, stopButton, nextButton);
        buttonBox.setAlignment(Pos.CENTER);


        repeatBox = new CheckBox("Повтор");
        repeatBox.setOnAction(e -> controller.setRepeat(repeatBox.isSelected()));

        shuffleBox = new CheckBox("Случайно");
        shuffleBox.setOnAction(e -> controller.setShuffle(shuffleBox.isSelected()));

        HBox optionsBox = new HBox(20, repeatBox, shuffleBox);
        optionsBox.setAlignment(Pos.CENTER);

        bottomBox.getChildren().addAll(currentTrackLabel, progressSlider, timeLabel, buttonBox, optionsBox);
        root.setBottom(bottomBox);


        Scene scene = new Scene(root, 500, 400);


        scene.setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case SPACE -> controller.togglePlayPause();
                case RIGHT -> controller.seekForward();
                case LEFT -> controller.seekBackward();
                case UP -> controller.increaseVolume();
                case DOWN -> controller.decreaseVolume();
            }
        });


        controller.setUICallbacks(
                trackList,
                currentTrackLabel,
                timeLabel,
                progressSlider,
                trackListView
        );

        stage.setTitle("AudioPlayer");
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void stop() {
        controller.cleanUp();
    }

    public static void main(String[] args) {
        launch();
    }
}