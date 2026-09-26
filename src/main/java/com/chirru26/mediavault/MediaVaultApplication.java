package com.chirru26.mediavault;

import com.chirru26.mediavault.data.Database;
import com.chirru26.mediavault.data.MediaRepository;
import com.chirru26.mediavault.service.MediaScanner;
import com.chirru26.mediavault.ui.MainView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public final class MediaVaultApplication extends Application {

    @Override
    public void start(Stage stage) {
        try {
            Database.initialize();
        } catch (Exception e) {
            throw new IllegalStateException("Unable to initialize MediaVault database", e);
        }

        var repository = new MediaRepository();
        var scanner = new MediaScanner(repository);
        var root = new MainView(scanner, repository).build();
        var scene = new Scene(root, 1280, 800);

        stage.setTitle("MediaVault");
        stage.setMinWidth(960);
        stage.setMinHeight(640);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
