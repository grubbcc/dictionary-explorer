import javafx.application.Application;
import javafx.stage.Stage;


public class App extends Application {
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Hello World!");
        primaryStage.show();
    }

    public static void main(String[] args) {
        // Simply delegates to the JavaFX Application class
        javafx.application.Application.launch(App.class, args);
    }
}
