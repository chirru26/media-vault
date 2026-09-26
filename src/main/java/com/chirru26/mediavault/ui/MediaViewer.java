package com.chirru26.mediavault.ui;

import com.chirru26.mediavault.model.MediaItem;
import com.chirru26.mediavault.media.ThumbnailService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.nio.file.Files;
import java.text.DecimalFormat;

public final class MediaViewer {
    private final ThumbnailService thumbnailService = new ThumbnailService();

    public void show(MediaItem item, Stage owner) {
        var stage = new Stage();
        stage.initOwner(owner);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.setTitle(item.fileName() + " — MediaVault");

        var preview = new VBox();
        preview.setAlignment(Pos.CENTER);
        preview.setPadding(new Insets(24));
        preview.setStyle("-fx-background-color: #111827;");

        if (item.mediaType().name().equals("IMAGE") && Files.isRegularFile(item.filePath())) {
            var image = thumbnailService.loadLarge(item.filePath());
            if (image != null) {
                var view = new ImageView(image);
                view.setPreserveRatio(true);
                view.setFitWidth(900);
                view.setFitHeight(650);
                preview.getChildren().add(view);
            }
        }
        if (preview.getChildren().isEmpty()) {
            var type = new Label(item.mediaType().name());
            type.setStyle("-fx-font-size: 32px; -fx-text-fill: white; -fx-font-weight: bold;");
            preview.getChildren().add(type);
        }

        var title = new Label(item.fileName());
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        var details = new Label("Type: " + item.mediaType() + "\nSize: " + formatSize(item.fileSize())
                + "\nModified: " + item.modifiedAt() + "\nLocation: " + item.filePath());
        details.setWrapText(true);
        var close = new Button("Close");
        close.setOnAction(e -> stage.close());

        var info = new VBox(10, title, details, close);
        info.setPadding(new Insets(18));
        var root = new BorderPane(new ScrollPane(preview));
        root.setBottom(info);

        stage.setScene(new Scene(root, 1000, 760));
        stage.show();
    }

    private String formatSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        double value = bytes;
        String[] units = {"KB", "MB", "GB", "TB"};
        int unit = -1;
        do { value /= 1024.0; unit++; } while (value >= 1024 && unit < units.length - 1);
        return new DecimalFormat("0.##").format(value) + " " + units[unit];
    }
}
