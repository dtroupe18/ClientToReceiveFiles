/*
 * Created by Dave on 3/15/17.
 * This class is designed to handle all popup windows
 * functions correspond to button names in the application
 */


import javafx.stage.*;
import javafx.scene.*;
import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.geometry.*;
import java.io.*;
import java.util.ArrayList;
import java.nio.file.Files;
import java.util.Arrays;


public class PopUpWindows {

    private static boolean answer;
    private static String create;
    private static String remove;
    private static ArrayList<String> newText;
    private static Stage window;
    private static  byte[] bytes;


    public static boolean quit() {
        window = new Stage();

        // user can only interact with alert box window
        window.initModality(Modality.APPLICATION_MODAL);
        window.setTitle("Quit");
        window.setMaxWidth(450);
        window.setMaxHeight(450);

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
        window = new Stage();

        // user can only interact with alert box window
        window.initModality(Modality.APPLICATION_MODAL);
        window.setTitle("Create Directory");
        TextField textField = new TextField();

        window.setMaxWidth(400);
        window.setMaxHeight(400);

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

    public static String remove() {
        window = new Stage();


        // user can only interact with alert box window
        window.initModality(Modality.APPLICATION_MODAL);
        window.setTitle("Remove Directory or File");
        TextField textField = new TextField();

        window.setMaxWidth(400);
        window.setMaxHeight(400);

        Label label = new Label();
        Label instructions = new Label("Enter name for directory or file you'd like to remove");
        label.setText("Are you sure you want to remove that file or directory?");

        // create two buttons
        Button yesButton = new Button("Yes");
        Button noButton = new Button("No");

        yesButton.setOnAction(e -> {
            remove = textField.getText().trim();
            window.close();
        });

        noButton.setOnAction(e -> {
            remove = "1";
            window.close();
        });

        VBox layout = new VBox(10);
        layout.getChildren().addAll(instructions, textField, label, yesButton, noButton);
        layout.setAlignment(Pos.CENTER);
        //layout.setStyle("-fx-background-color: red");

        Scene scene = new Scene(layout);
        window.setScene(scene);

        window.showAndWait();
        return remove;
    }


    public static ArrayList<String> read(File file) {
        window = new Stage();
        window.setTitle(file.getName());

        BorderPane mainPane = new BorderPane();
        // display contents
        TextArea textArea = new TextArea();
        //set the size of the text area
        textArea.setPrefColumnCount(50);
        textArea.setPrefRowCount(30);
        mainPane.setCenter(new ScrollPane(textArea));
        // create two buttons
        Button write = new Button("Write");

        //create pane for buttons
        GridPane gridPane = new GridPane();
        // add horizontal spacing between buttons in grid pane
        gridPane.setHgap(8);

        // add all the buttons to grid pane
        gridPane.add(write, 1, 0);

        window.setMaxWidth(600);
        window.setMaxHeight(500);

        // read file and place in textArea
        String line;
        try {
            // FileReader to read text files in default encoding
            FileReader fileReader = new FileReader(file);

            // wrap FileReader in BufferedReader
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            while ((line = bufferedReader.readLine()) != null) {
                textArea.appendText("\n");
                textArea.appendText(line);
            }

            bufferedReader.close();
        }
        catch (IOException ex) {
            System.out.println("Unable to open file " + file.getName());
        }


        write.setOnAction(e -> {
            newText = new ArrayList<>();
            String name = file.getName();
            newText.add(name);
            for (String textLine: textArea.getText().split("\\n")) {
                newText.add(textLine);
                window.close();
            }
        });

        mainPane.setBottom(gridPane);
        Scene scene = new Scene(mainPane);
        window.setScene(scene);
        window.showAndWait();
        
        return newText;
    }

    // this method will fail with large files
    public static void readBytes(File file) {
        window = new Stage();
        window.setTitle(file.getName());

        BorderPane mainPane = new BorderPane();
        // display contents
        TextArea textArea = new TextArea();
        //set the size of the text area
        textArea.setPrefColumnCount(50);
        textArea.setPrefRowCount(30);
        mainPane.setCenter(new ScrollPane(textArea));
        // create two buttons
        Button write = new Button("Write");

        //create pane for buttons
        GridPane gridPane = new GridPane();
        // add horizontal spacing between buttons in grid pane
        gridPane.setHgap(8);

        // add all the buttons to grid pane
        gridPane.add(write, 1, 0);

        window.setMaxWidth(600);
        window.setMaxHeight(500);

        // convert file into bytes
        try {
            bytes = Files.readAllBytes(file.toPath());
            //textArea.appendText(Arrays.toString(bytes));
            for (int i = 0; i < bytes.length; i++) {
                if (i % 5 == 0) {
                    textArea.appendText("\n");
                }
                textArea.appendText(Integer.toBinaryString((bytes[i] & 0xFF) + 0x100).substring(1) + " ");
            }
        }
        catch (IOException ex) {
            System.out.println(ex.getLocalizedMessage());
        }

        write.setOnAction(e -> {
            // send bytes back???
        });

        mainPane.setBottom(gridPane);
        Scene scene = new Scene(mainPane);
        window.setScene(scene);
        window.showAndWait();
    }


}
