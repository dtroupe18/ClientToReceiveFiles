/*
 * Created by Dave on 3/12/17.
 */


/**
 * Created by Dave on 3/9/17.
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
    private ArrayList<String> txtFile; // text from file

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
        Button read = new Button("Read Text File");
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
        TextArea textArea = new TextArea();
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
        ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
        System.out.println("Sending request to Socket Server");

        submit.setOnAction( e -> {

            try {
                command = textField.getText();
                oos.writeObject(command);

                Object response = ois.readObject();

                if (response instanceof File[]) {
                    // excepting an array
                    File [] files = (File[]) response;
                    textArea.appendText("Command was " + command + "\n");
                    textArea.appendText("Response from server:\n");
                    // display all the files in the directory to the client
                    for (int i = 0; i < files.length; i++) {
                        textArea.appendText(printFile(files, i));
                    }
                }

                else if (isInteger(command) && response instanceof File) {
                    // if command is a number we expect a file in return
                    File requestedFile = (File) response;
                    serverFile = requestedFile; // save the file locally
                    textArea.appendText("Command was " + command + "\n");
                    textArea.appendText("Response from server:\n");
                    textArea.appendText(requestedFile.getName());
                }

                else {
                    textArea.appendText("Invalid Server Response\n");
                }
            }
            catch (IOException ex1) {
                System.err.print(ex1);
            }
            catch (ClassNotFoundException ex2) {
                ex2.printStackTrace();
            }
        });

        read.setOnAction( e -> {
            // read text file received from server
            String line;

            if (serverFile != null) {
                try {
                    // FileReader to read text files in default encoding
                    FileReader fileReader = new FileReader(serverFile);

                    // wrap FileReader in BufferedReader
                    BufferedReader bufferedReader = new BufferedReader(fileReader);

                    while ((line = bufferedReader.readLine()) != null) {
                        textArea.appendText("\n");
                        textArea.appendText(line);
                    }

                    bufferedReader.close();
                }
                catch (IOException ex) {
                    System.out.println("Unable to open file " + serverFile.getName());
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
                textArea.appendText("Directory creation avoided");
            }

            else {
                // user clicked yes end the string with a dollar sign
                // so we can distinguish this command from others
                String tmp = directoryToCreate + "$";

                command = tmp;

                try {
                    oos.writeObject(command);
                }
                catch (IOException ioe) {
                    textArea.appendText(ioe.getLocalizedMessage());
                }

                try {
                    Object response = ois.readObject();
                    textArea.appendText("Server Response: " + response);
                }
                catch (ClassNotFoundException cnf) {
                    textArea.appendText(cnf.getLocalizedMessage());
                }
                catch (IOException ioe2) {
                    textArea.appendText(ioe2.getLocalizedMessage());
                }
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
}

