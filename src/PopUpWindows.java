/*
 * Created by Dave on 3/15/17.
 * This class is designed to handle all popup windows
 * functions correspond to button name in the application
 */

import javafx.stage.*;
import javafx.scene.*;
import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.geometry.*;

public class PopUpWindows {

    private static boolean answer;
    private static String create;

    public static boolean quit() {
        Stage window = new Stage();

        // user can only interact with alert box window
        window.initModality(Modality.APPLICATION_MODAL);
        window.setTitle("Quit");
        window.setMaxWidth(300);
        window.setMaxHeight(300);

        Label label = new Label();
        label.setText("Are you sure you want to quit?");

        // create two buttons
        Button yesButton = new Button("Yes");
        Button noButton = new Button("No");

        yesButton.setOnAction(e -> {
            answer = true;
            window.close();
        });

        noButton.setOnAction(e -> {
            answer = false;
            window.close();
        });


        VBox layout = new VBox(10);
        layout.getChildren().addAll(label, yesButton, noButton);
        layout.setAlignment(Pos.CENTER);
        //layout.setStyle("-fx-background-color: red");

        Scene scene = new Scene(layout);
        window.setScene(scene);

        window.showAndWait();
        return answer;
    }

    public static String create() {
        Stage window = new Stage();
        String directoryToCreate;

        // user can only interact with alert box window
        window.initModality(Modality.APPLICATION_MODAL);
        window.setTitle("Create Directory");
        TextField textField = new TextField();

        window.setMaxWidth(300);
        window.setMaxHeight(300);

        Label label = new Label();
        Label instructions = new Label("Enter name for directory you'd like to create");
        label.setText("Are you sure you want to create that directory?");

        // create two buttons
        Button yesButton = new Button("Yes");
        Button noButton = new Button("No");

        yesButton.setOnAction(e -> {
            create = textField.getText().trim();
            window.close();
        });

        noButton.setOnAction(e -> {
            create = "1";
            window.close();
        });

        VBox layout = new VBox(10);
        layout.getChildren().addAll(instructions, textField, label, yesButton, noButton);
        layout.setAlignment(Pos.CENTER);
        //layout.setStyle("-fx-background-color: red");

        Scene scene = new Scene(layout);
        window.setScene(scene);

        window.showAndWait();
        return create;
    }
}
