package com.octavian.galactic.ui;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;

import java.io.IOException;
import java.util.Objects;

public final class ViewLoader {

    private ViewLoader() {}

    public static <T> LoadedView<T> load(String resourceName) {
        String path = resourceName.startsWith("/")
                ? resourceName
                : "/com/octavian/galactic/ui/" + resourceName;
        FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(
                ViewLoader.class.getResource(path),
                "FXML not found: " + path
        ));
        try {
            Node root = loader.load();
            return new LoadedView<>(root, loader.getController());
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load FXML: " + path, e);
        }
    }

    public record LoadedView<T>(Node root, T controller) {}
}
