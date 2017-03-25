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
    private Socket socket;
    private Boolean connected;



    @Override
    public void start(Stage primaryStage) throws Exception {

        // GUI
        BorderPane paneForTextField = new BorderPane();
        paneForTextField.setPadding(new Insets(5, 5, 5, 5));
        paneForTextField.setStyle("-fx-border-color: red");
        paneForTextField.setLeft(new Label("Send Command To Server: "));
        Button submit = new Button("Submit");
        Button reconnect = new Button("Reconnect");
        paneForTextField.setBottom(reconnect);
        paneForTextField.setRight(submit);

        // add a button to perform actions
        Button read = new Button("Open Text File");
        Button create = new Button("Create Directory");
        Button remove = new Button("Remove");
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
        gridPane.add(quit, 10, 0);

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

        connected = connectToServer();

        submit.setOnAction( e -> {
            if (connected) {
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
                        textArea.appendText("Response: " + response + "\n");
                    }
                    else {
                        textArea.appendText("Invalid Server Response\n");
                    }
                }
                else {
                    textArea.appendText("Enter a command:\n");
                }
            }
            // not connected
            else  {
                textArea.appendText("Please connect to the server:\n");
            }
        });

        reconnect.setOnAction(e -> {
            connected = connectToServer();
        });

        read.setOnAction( e -> {
            if (connected) {
                if (serverFile != null) {
                    textFile = PopUpWindows.read(serverFile);
                    if (textFile.isEmpty()) {
                        textArea.appendText("Text file closed\n");
                    }
                    else {
                        // write arrayList to the server
                        writeObjectToServer();
                        Object reply = readFromServer();
                        textArea.appendText((String) reply);
                    }
                }
                else {
                    System.out.println("File does not exist\n");
                }
            }
            // not connected
            else {
                textArea.appendText("Please connect to the server:\n");
            }
        });

        quit.setOnAction(e -> {
            boolean userQuit = PopUpWindows.quit();
            if (userQuit && connected) {
                try {
                    command = "Quit";
                    writeToServer();
                    socket.close();
                }
                catch (IOException ioe) {
                    textArea.appendText(ioe.getMessage());
                }
                primaryStage.close();
            }
            else {
                primaryStage.close();
            }
        });

        create.setOnAction(e -> {
            if (connected) {
                String directoryToCreate = PopUpWindows.create();
                if (isInteger(directoryToCreate)) {
                    // user did not want to create a new directory
                    textArea.appendText("Directory creation cancelled\n");
                }

                else {
                    // user clicked yes end the string with a dollar sign
                    // so we can distinguish this command from others
                    String tmp = directoryToCreate + "$";

                    command = tmp;
                    writeToServer();
                    Object reply = readFromServer();
                    textArea.appendText((String) reply + "\n");
                }
            }
            // not connected
            else {
                textArea.appendText("Please connect to the server:\n");
            }
        });

        remove.setOnAction(e -> {
            if (connected) {
                String toRemove = PopUpWindows.remove();
                if (isInteger(toRemove)) {
                    // user did not want to delete directory or file
                    textArea.appendText("Directory removal cancelled\n");
                }

                else {
                    // user clicked yes end the string with a # sign
                    // so we can distinguish this command from others
                    String tmp = toRemove + "#";
                    command = tmp;
                    writeToServer();
                    Object reply = readFromServer();
                    textArea.appendText((String) reply + "\n");
                }
            }
            // not connected
            else {
                textArea.appendText("Please connect to the server:\n");
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
            textArea.appendText(ioe.getLocalizedMessage() + "\n");
        }
    }

    private void writeObjectToServer() {
        try {
            oos.writeObject(textFile);
        }
        catch (IOException ioe) {
            textArea.appendText(ioe.getLocalizedMessage() + "\n");
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

    private boolean connectToServer() {
        try {
            socket = new Socket("localhost", 8675);
            //write to socket using ObjectOutputStream
            oos = new ObjectOutputStream(socket.getOutputStream());
            ois = new ObjectInputStream(socket.getInputStream());
            textArea.appendText("Connected to Server\n");
            return true;
        }
        catch (IOException ex) {
            textArea.appendText("Failed to connect to server\n");
            return false;
        }
    }
}

