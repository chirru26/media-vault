package com.chirru26.mediavault.ui;

import com.chirru26.mediavault.data.MediaRepository;
import com.chirru26.mediavault.model.MediaItem;
import com.chirru26.mediavault.model.MediaType;
import com.chirru26.mediavault.service.MediaScanner;
import javafx.collections.FXCollections;
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
import javafx.stage.DirectoryChooser;

import java.nio.file.Path;
import java.util.List;

public final class MainView {
    private final MediaScanner scanner;
    private final MediaRepository repository;
    private final GridPane grid = new GridPane();
    private final Label status = new Label("0 media items");

    public MainView(MediaScanner scanner, MediaRepository repository) {
        this.scanner = scanner;
        this.repository = repository;
    }

    public BorderPane build() {
        var root = new BorderPane();
        root.setStyle("-fx-background-color: #f6f7f9; -fx-font-family: 'Segoe UI';");
        root.setTop(createHeader());
        root.setLeft(createSidebar());
        root.setCenter(createLibrary());
        refresh();
        return root;
    }

    private Node createHeader() {
        var title = new Label("MediaVault");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        var search = new TextField();
        search.setPromptText("Search your media...");
        var importButton = new Button("+ Import Folder");
        importButton.setStyle("-fx-font-weight: bold; -fx-padding: 9 16;");
        importButton.setOnAction(event -> chooseAndScanFolder(importButton));
        var header = new HBox(24, title, search, importButton);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(18, 24, 18, 24));
        header.setStyle("-fx-background-color: white; -fx-border-color: #e5e7eb; -fx-border-width: 0 0 1 0;");
        HBox.setHgrow(search, Priority.ALWAYS);
        return header;
    }

    private void chooseAndScanFolder(Button source) {
        var chooser = new DirectoryChooser();
        chooser.setTitle("Select Media Folder");
        var window = source.getScene().getWindow();
        var folder = chooser.showDialog(window);
        if (folder == null) return;
        source.setDisable(true);
        try {
            var result = scanner.scan(folder.toPath());
            status.setText(result.indexed() + " media items indexed from " + result.discovered() + " files");
            refresh();
        } catch (Exception e) {
            status.setText("Scan failed: " + e.getMessage());
        } finally {
            source.setDisable(false);
        }
    }

    private Node createSidebar() {
        var sidebar = new VBox(8);
        sidebar.setPadding(new Insets(24, 16, 24, 16));
        sidebar.setPrefWidth(210);
        sidebar.setStyle("-fx-background-color: white; -fx-border-color: #e5e7eb; -fx-border-width: 0 1 0 0;");
        var heading = new Label("LIBRARY");
        heading.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #6b7280;");
        sidebar.getChildren().addAll(heading, navButton("All Media", true), navButton("Photos", false), navButton("Videos", false), navButton("Documents", false), new Separator(), navButton("Albums", false), navButton("Favorites", false), navButton("Trash", false));
        return sidebar;
    }

    private Button navButton(String text, boolean active) {
        var button = new Button(text);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setAlignment(Pos.CENTER_LEFT);
        button.setStyle(active ? "-fx-background-color: #e9eefc; -fx-font-weight: bold; -fx-padding: 10 12;" : "-fx-background-color: transparent; -fx-padding: 10 12;");
        return button;
    }

    private Node createLibrary() {
        var title = new Label("Media Library");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");
        var subtitle = new Label("Your local media collection");
        subtitle.setStyle("-fx-text-fill: #6b7280;");
        status.setStyle("-fx-text-fill: #6b7280;");
        grid.setHgap(16);
        grid.setVgap(16);
        var content = new VBox(20, new VBox(4, title, subtitle), status, grid);
        content.setPadding(new Insets(28));
        return new StackPane(content);
    }

    private void refresh() {
        grid.getChildren().clear();
        try {
            List<MediaItem> items = repository.findAll();
            if (items.isEmpty()) {
                grid.add(emptyState(), 0, 0);
                return;
            }
            int index = 0;
            for (var item : items) {
                grid.add(mediaCard(item), index % 4, index / 4);
                index++;
            }
        } catch (Exception e) {
            grid.add(new Label("Unable to load media: " + e.getMessage()), 0, 0);
        }
    }

    private Node mediaCard(MediaItem item) {
        var box = new VBox(8);
        box.setPrefWidth(190);
        var preview = new StackPane();
        preview.setPrefSize(190, 125);
        preview.setStyle("-fx-background-color: #e5e7eb; -fx-background-radius: 10;");
        var icon = new Label(switch (item.mediaType()) {
            case IMAGE -> "🖼";
            case VIDEO -> "▶";
            case AUDIO -> "♫";
            case DOCUMENT -> "▤";
            case OTHER -> "•";
        });
        icon.setStyle("-fx-font-size: 32px;");
        preview.getChildren().add(icon);
        var name = new Label(item.fileName());
        name.setMaxWidth(190);
        name.setEllipsisString("...");
        var path = new Label(item.filePath().toString());
        path.setMaxWidth(190);
        path.setEllipsisString("...");
        path.setStyle("-fx-font-size: 10px; -fx-text-fill: #9ca3af;");
        box.getChildren().addAll(preview, name, path);
        return box;
    }

    private Node emptyState() {
        var box = new VBox(8, new Label("No media indexed yet"), new Label("Click '+ Import Folder' to scan a media folder."));
        box.setAlignment(Pos.CENTER_LEFT);
        return box;
    }
}
