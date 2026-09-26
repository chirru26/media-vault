package com.chirru26.mediavault.ui;

import com.chirru26.mediavault.data.MediaRepository;
import com.chirru26.mediavault.model.MediaItem;
import com.chirru26.mediavault.service.MediaFilter;
import com.chirru26.mediavault.service.MediaScanner;
import com.chirru26.mediavault.media.ThumbnailService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.nio.file.Files;
import java.util.List;

public final class MainView {
    private final MediaScanner scanner;
    private final MediaRepository repository;
    private final MediaFilter filter = new MediaFilter();
    private final ThumbnailService thumbnailService = new ThumbnailService();
    private final GridPane grid = new GridPane();
    private final Label status = new Label("0 media items");
    private final Label selectedName = new Label("No media selected");
    private final Label selectedDetails = new Label("Select a media item to view its details.");
    private final TextField searchField = new TextField();
    private final ComboBox<String> typeFilter = new ComboBox<>();
    private List<MediaItem> allItems = List.of();
    private MediaItem selectedItem;

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
        root.setRight(createDetailsPanel());
        refresh();
        return root;
    }

    private Node createHeader() {
        var title = new Label("MediaVault");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        searchField.setPromptText("Search by filename or location...");
        searchField.textProperty().addListener((obs, oldValue, newValue) -> applyFilters());
        typeFilter.getItems().addAll("All", "IMAGE", "VIDEO", "AUDIO", "DOCUMENT", "OTHER");
        typeFilter.setValue("All");
        typeFilter.setOnAction(event -> applyFilters());
        var importButton = new Button("+ Import Folder");
        importButton.setStyle("-fx-font-weight: bold; -fx-padding: 9 16;");
        importButton.setOnAction(event -> chooseAndScanFolder(importButton));
        var header = new HBox(14, title, searchField, typeFilter, importButton);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(18, 24, 18, 24));
        header.setStyle("-fx-background-color: white; -fx-border-color: #e5e7eb; -fx-border-width: 0 0 1 0;");
        HBox.setHgrow(searchField, Priority.ALWAYS);
        return header;
    }

    private void chooseAndScanFolder(Button source) {
        var chooser = new DirectoryChooser();
        chooser.setTitle("Select Media Folder");
        var folder = chooser.showDialog(source.getScene().getWindow());
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
        var subtitle = new Label("Search, filter and browse your local media collection");
        subtitle.setStyle("-fx-text-fill: #6b7280;");
        status.setStyle("-fx-text-fill: #6b7280;");
        grid.setHgap(16);
        grid.setVgap(16);
        var content = new VBox(20, new VBox(4, title, subtitle), status, grid);
        content.setPadding(new Insets(28));
        return new StackPane(content);
    }

    private Node createDetailsPanel() {
        var panel = new VBox(14);
        panel.setPrefWidth(290);
        panel.setPadding(new Insets(24));
        panel.setStyle("-fx-background-color: white; -fx-border-color: #e5e7eb; -fx-border-width: 0 0 0 1;");
        var heading = new Label("Details");
        heading.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        selectedName.setStyle("-fx-font-size: 15px; -fx-font-weight: bold;");
        selectedName.setWrapText(true);
        selectedDetails.setWrapText(true);
        selectedDetails.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 12px;");
        var open = new Button("Open Viewer");
        open.setMaxWidth(Double.MAX_VALUE);
        open.setOnAction(event -> {
            if (selectedItem != null && grid.getScene() != null) {
                new MediaViewer().show(selectedItem, (Stage) grid.getScene().getWindow());
            }
        });
        open.disableProperty().bind(javafx.beans.binding.Bindings.createBooleanBinding(() -> selectedItem == null));
        panel.getChildren().addAll(heading, new Separator(), selectedName, selectedDetails, open);
        return panel;
    }

    private void refresh() {
        try {
            allItems = repository.findAll();
            applyFilters();
        } catch (Exception e) {
            grid.getChildren().clear();
            grid.add(new Label("Unable to load media: " + e.getMessage()), 0, 0);
        }
    }

    private void applyFilters() {
        var filtered = filter.apply(allItems, searchField.getText(), typeFilter.getValue());
        render(filtered);
    }

    private void render(List<MediaItem> items) {
        grid.getChildren().clear();
        selectedItem = null;
        selectedName.setText("No media selected");
        selectedDetails.setText("Select a media item to view its details.");
        status.setText(items.size() + " of " + allItems.size() + " media items");
        if (items.isEmpty()) {
            grid.add(new Label("No media matches your search or filter."), 0, 0);
            return;
        }
        int index = 0;
        for (var item : items) {
            grid.add(mediaCard(item), index % 4, index / 4);
            index++;
        }
    }

    private Node mediaCard(MediaItem item) {
        var box = new VBox(8);
        box.setPrefWidth(190);
        var preview = new StackPane();
        preview.setPrefSize(190, 125);
        preview.setStyle("-fx-background-color: #e5e7eb; -fx-background-radius: 10;");
        if (item.mediaType().name().equals("IMAGE") && Files.isRegularFile(item.filePath())) {
            var image = thumbnailService.load(item.filePath());
            if (image != null) {
                var imageView = new ImageView(image);
                imageView.setFitWidth(190);
                imageView.setFitHeight(125);
                imageView.setPreserveRatio(true);
                preview.getChildren().add(imageView);
            }
        }
        if (preview.getChildren().isEmpty()) {
            var icon = new Label(switch (item.mediaType()) {
                case IMAGE -> "🖼"; case VIDEO -> "▶"; case AUDIO -> "♫"; case DOCUMENT -> "▤"; case OTHER -> "•";
            });
            icon.setStyle("-fx-font-size: 32px;");
            preview.getChildren().add(icon);
        }
        var name = new Label(item.fileName());
        name.setMaxWidth(190);
        name.setEllipsisString("...");
        var path = new Label(item.filePath().toString());
        path.setMaxWidth(190);
        path.setEllipsisString("...");
        path.setStyle("-fx-font-size: 10px; -fx-text-fill: #9ca3af;");
        box.getChildren().addAll(preview, name, path);
        box.setOnMouseClicked(event -> {
            select(item);
            if (event.getClickCount() == 2) new MediaViewer().show(item, (Stage) box.getScene().getWindow());
        });
        box.setStyle("-fx-cursor: hand;");
        return box;
    }

    private void select(MediaItem item) {
        selectedItem = item;
        selectedName.setText(item.fileName());
        selectedDetails.setText("Type: " + item.mediaType() + "\nSize: " + formatSize(item.fileSize()) + "\nModified: " + item.modifiedAt() + "\n\nLocation:\n" + item.filePath());
    }

    private String formatSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        double value = bytes;
        String[] units = {"KB", "MB", "GB", "TB"};
        int unit = -1;
        do { value /= 1024.0; unit++; } while (value >= 1024 && unit < units.length - 1);
        return String.format("%.2f %s", value, units[unit]);
    }
}
