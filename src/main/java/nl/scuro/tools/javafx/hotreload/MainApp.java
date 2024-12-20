package nl.scuro.tools.javafx.hotreload;

import java.io.File;
import java.net.MalformedURLException;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.Scene;
import javafx.stage.Stage;
import nl.scuro.tools.javafx.hotreload.gui.MainView;

public class MainApp extends Application {

    private DirectoryWatcherService directoryWatcherService;

    @Override
    public void start(Stage primaryStage) throws Exception {

        MainView mainView = new MainView();
        mainView.getSelectedClassPath().addListener((ChangeListener<File>) (observable, oldValue, newValue) -> {
            directoryWatcherService = new DirectoryWatcherService(newValue);
            ObservableList<String> observableArrayList = FXCollections.observableArrayList(directoryWatcherService.getAvailableFiles());
            mainView.setOptionsList(new FilteredList<>(observableArrayList));
            mainView.rightLabelProperty().bind(directoryWatcherService.updateTimestampProperty());
        });
        mainView.onTabCreated(tab -> directoryWatcherService.registerTab(tab));
        mainView.onTabClosed(tab -> directoryWatcherService.deRegisterTab(tab));
        mainView.setPrefSize(600, 600);
        Scene scene = new Scene(mainView);
        primaryStage.setTitle("JavaFx Hot Code Reload");
        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest(eh -> {
            if (directoryWatcherService != null) {
                directoryWatcherService.shutdown();
            }
        });
        primaryStage.show();
    }

    public static void main(String[] args) throws MalformedURLException {
        launch();
    }
}
