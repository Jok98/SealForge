package com.sealforge.app;

import com.sealforge.ui.controller.AppController;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApplication extends Application {

    private AppController controller;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        AppContext appContext = ApplicationBootstrap.bootstrap();
        controller = new AppController(appContext);

        Scene scene = new Scene(controller.createView(), 1_440, 920);
        stage.setScene(scene);
        stage.setTitle(appContext.appConfig().applicationName());
        stage.setMinWidth(1_200);
        stage.setMinHeight(800);
        stage.setOnCloseRequest(event -> {
            if (!controller.confirmApplicationClose()) {
                event.consume();
                return;
            }
            controller.prepareForShutdown();
        });
        stage.show();
    }

    @Override
    public void stop() {
        if (controller != null) {
            controller.prepareForShutdown();
        }
    }
}
