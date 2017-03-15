/*
 * Created by Dave on 3/12/17.
 */

import java.io.*;
import java.net.*;
import java.util.ArrayList;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class ClientToReceiveFiles extends Application {
    private String command; // last command sent to the server
    private File serverFile; // last file sent from the server
    private ObjectOutputStream oos;
    private ObjectInputStream ois;
    private TextArea textArea;
    private Object response;
    private ArrayList<String> textFile;


    @Override
    public void start(Stage primaryStage) throws Exception {

        // GUI
        BorderPane paneForTextField = new BorderPane();
        paneForTextField.setPadding(new Insets(5, 5, 5, 5));
        paneForTextField.setStyle("-fx-border-color: red");
        paneForTextField.setLeft(new Label("Send Command To Server: "));
        Button submit = new Button("Submit");
        paneForTextField.setBottom(submit);

        // add a button to perform actions
        Button read = new Button("Open Text File");
        Button create = new Button("Create Directory");
        Button remove = new Button("Remove");
        Button write = new Button("Write");
        Button seek = new Button("Seek");
        Button quit = new Button("QUIT");
        quit.setStyle("-fx-text-fill: red");

        TextField textField = new TextField();
        textField.setAlignment(Pos.BOTTOM_RIGHT);
        paneForTextField.setCenter(textField);

        //create pane for buttons
        GridPane gridPane = new GridPane();
        // add horizontal spacing between buttons in grid pane
        gridPane.setHgap(8);

        // add all the buttons to grid pane
        gridPane.add(read, 0, 0);
        gridPane.add(create, 1, 0);
        gridPane.add(remove, 2, 0);
        gridPane.add(write, 3, 0);
        gridPane.add(seek, 4, 0);
        gridPane.add(quit, 5, 0);

        BorderPane mainPane = new BorderPane();
        // display contents
        textArea = new TextArea();
        //set the size of the text area
        textArea.setPrefColumnCount(50);
        textArea.setPrefRowCount(30);
        mainPane.setCenter(new ScrollPane(textArea));
        mainPane.setTop(paneForTextField);
        mainPane.setBottom(gridPane);


        // create scene and place it on the stage
        Scene scene = new Scene(mainPane, 700, 700);
        primaryStage.setTitle("Client");
        primaryStage.setScene(scene);
        primaryStage.show();
        // END OF GUI

        Socket socket = new Socket("localhost", 8675);

        //write to socket using ObjectOutputStream
        oos = new ObjectOutputStream(socket.getOutputStream());
        ois = new ObjectInputStream(socket.getInputStream());
        System.out.println("Sending request to Socket Server");

        submit.setOnAction( e -> {
            command = textField.getText();
            if (!command.equals("")) {
                writeToServer();
                response = readFromServer();

                if (response instanceof File[]) {
                    // excepting an array
                    File[] files = (File[]) response;
                    textArea.appendText("Command was " + command + "\n");
                    textArea.appendText("Response from server:\n");
                    // display all the files in the directory to the client
                    for (int i = 0; i < files.length; i++) {
                        textArea.appendText(printFile(files, i));
                    }
                    textField.setText(""); // clear the text field
                }
                else if (isInteger(command) && response instanceof File) {
                    // if command is a number we expect a file in return
                    File requestedFile = (File) response;
                    serverFile = requestedFile; // save the file locally
                    textArea.appendText("Command was " + command + "\n");
                    textArea.appendText("Response from server: ");
                    textArea.appendText(requestedFile.getName() +"\n");
                    textField.setText(""); // clear the text field
                }
                else if (response instanceof String) {
                    textArea.appendText("Response: " + response);
                }
                else {
                    textArea.appendText("Invalid Server Response\n");
                }
            }
            else {
                textArea.appendText("Enter a command");
            }
        });

        read.setOnAction( e -> {
            if (serverFile != null) {
                textFile = PopUpWindows.read(serverFile);
                if (!textFile.isEmpty()) {
                    // write arrayList to the server
                    writeObjectToServer();
                    Object reply = readFromServer();
                    textArea.appendText((String) reply);
                }
            }
            else {
                System.out.println("File does not exist");
            }
        });

        quit.setOnAction(e -> {
            boolean userQuit = PopUpWindows.quit();
            if (userQuit) {
                try {
                    socket.close();
                }
                catch (IOException ioe) {
                    textArea.appendText(ioe.getMessage());
                }
                primaryStage.close();
            }
        });

        create.setOnAction(e -> {
            String directoryToCreate = PopUpWindows.create();
            if (isInteger(directoryToCreate)) {
                // user did not want to create a new directory
                textArea.appendText("Directory creation canceled");
            }

            else {
                // user clicked yes end the string with a dollar sign
                // so we can distinguish this command from others
                String tmp = directoryToCreate + "$";

                command = tmp;
                writeToServer();
                Object reply = readFromServer();
                textArea.appendText((String) reply);
            }
        });

        remove.setOnAction(e -> {
            String toRemove = PopUpWindows.remove();
            if (isInteger(toRemove)) {
                // user did not want to delete directory or file
                textArea.appendText("Directory removal canceled");
            }

            else {
                // user clicked yes end the string with a # sign
                // so we can distinguish this command from others
                String tmp = toRemove + "#";
                command = tmp;
                writeToServer();
                Object reply = readFromServer();
            }
        });
    }

    private String printFile(File[] f, int n) {
        String file = "";
        if (f[n].isFile()) {
            file = ("File(" + n +"): " + f[n].getName() + "\n");
        }
        else if (f[n].isDirectory()) {
            file = ("Directory(" + n + "): " + f[n].getName() + "\n");
        }
        return file;
    }

    private boolean isInteger(String str) {
        try {
            int n = Integer.parseInt(str);
        }
        catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    private void writeToServer() {
        try {
            oos.writeObject(command);
        }
        catch (IOException ioe) {
            textArea.appendText(ioe.getLocalizedMessage());
        }
    }

    private void writeObjectToServer() {
        try {
            oos.writeObject(textFile);
        }
        catch (IOException ioe) {
            //System.out.println(ioe.getMessage());
            textArea.appendText(ioe.getLocalizedMessage());
        }
    }

    private Object readFromServer() {
        try {
            Object response = ois.readObject();
            return response;
        } catch (ClassNotFoundException cnf) {
            textArea.appendText(cnf.getLocalizedMessage());
        } catch (IOException ioe2) {
            textArea.appendText(ioe2.getMessage());
        }
        return "Failed to read from server\n";
    }
}

