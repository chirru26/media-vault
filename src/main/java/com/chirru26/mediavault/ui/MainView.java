package com.chirru26.mediavault.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public final class MainView {

    public BorderPane build() {
        var root = new BorderPane();
        root.setStyle("-fx-background-color: #f6f7f9; -fx-font-family: 'Segoe UI';");
        root.setTop(createHeader());
        root.setLeft(createSidebar());
        root.setCenter(createLibrary());
        return root;
    }

    private Node createHeader() {
        var title = new Label("MediaVault");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        var search = new TextField();
        search.setPromptText("Search your media...");
        search.setPrefWidth(420);
        HBox.setHgrow(search, Priority.NEVER);

        var importButton = new Button("+ Import Media");
        importButton.setStyle("-fx-font-weight: bold; -fx-padding: 9 16 9 16;");

        var header = new HBox(24, title, search, importButton);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(18, 24, 18, 24));
        header.setStyle("-fx-background-color: white; -fx-border-color: #e5e7eb; -fx-border-width: 0 0 1 0;");
        HBox.setHgrow(search, Priority.ALWAYS);
        return header;
    }

    private Node createSidebar() {
        var sidebar = new VBox(8);
        sidebar.setPadding(new Insets(24, 16, 24, 16));
        sidebar.setPrefWidth(210);
        sidebar.setStyle("-fx-background-color: white; -fx-border-color: #e5e7eb; -fx-border-width: 0 1 0 0;");

        var heading = new Label("LIBRARY");
        heading.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #6b7280;");

        sidebar.getChildren().addAll(
                heading,
                navButton("All Media", true),
                navButton("Photos", false),
                navButton("Videos", false),
                navButton("Documents", false),
                new Separator(),
                navButton("Albums", false),
                navButton("Favorites", false),
                navButton("Trash", false)
        );
        return sidebar;
    }

    private Button navButton(String text, boolean active) {
        var button = new Button(text);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setAlignment(Pos.CENTER_LEFT);
        button.setStyle(active
                ? "-fx-background-color: #e9eefc; -fx-font-weight: bold; -fx-padding: 10 12;"
                : "-fx-background-color: transparent; -fx-padding: 10 12;");
        return button;
    }

    private Node createLibrary() {
        var title = new Label("Media Library");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

        var subtitle = new Label("Your local media collection");
        subtitle.setStyle("-fx-text-fill: #6b7280;");

        var grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(16);
        for (int row = 0; row < 2; row++) {
            for (int col = 0; col < 4; col++) {
                grid.add(mediaPlaceholder(), col, row);
            }
        }

        var content = new VBox(20, new VBox(4, title, subtitle), grid);
        content.setPadding(new Insets(28));
        return new StackPane(content);
    }

    private Node mediaPlaceholder() {
        var box = new StackPane();
        box.setPrefSize(190, 145);
        box.setStyle("-fx-background-color: #e5e7eb; -fx-background-radius: 10;");
        var label = new Label("No media yet");
        label.setStyle("-fx-text-fill: #6b7280;");
        box.getChildren().add(label);
        return box;
    }
}
