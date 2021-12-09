/**
CS1131
Image Manipulator Assignment
Ben Papp, Caleb Brinkman, Chance Masar, Ethan Jenkins
12/9/2021
*/

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.InputMismatchException;
import java.util.Scanner;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class ImageManipulator extends Application implements ImageManipulatorInterface{
    public static void main(String[] args) {
        launch(args);
    }

    private Text error;

    @Override
    public void start(Stage primaryStage){
        BorderPane root = new BorderPane();

        Label label = new Label();
        ImageView view = new ImageView();
        view.fitWidthProperty().bind(root.widthProperty().subtract(75));
        view.fitHeightProperty().bind(root.heightProperty().subtract(75));
        label.setGraphic(view);

        error = new Text();
        error.setFill(Color.RED);
        root.setTop(error);

        HBox hbox = new HBox(10);
        hbox.setPadding(new Insets(10));
        hbox.setAlignment(Pos.CENTER);
        root.setBottom(hbox);
        
        Button load = new Button("Load Image");
        load.setOnAction(event -> {
            FileChooser fileChooser = configureFileChooser();
            File file = fileChooser.showOpenDialog(primaryStage);
            if (file != null){
                try {
                    view.setImage(loadImage(file.getAbsolutePath()));
                    error.setText("");
                } catch (FileNotFoundException e) {
                    error.setText("File not found!");
                } catch (InputMismatchException e){
                    error.setText("Incorrect file format!");
                }
            }

        });

        Button save = new Button("Save Image");
        save.setOnAction(event -> {
            FileChooser fileChooser = configureFileChooser();
            if(view.getImage() == null){
                error.setText("No image loaded!");
            } else{
                File file = fileChooser.showSaveDialog(primaryStage);
                if(file != null){
                    try {
                        saveImage(file.getAbsolutePath(), (WritableImage)view.getImage());
                        error.setText("");
                    } catch (FileNotFoundException e) {
                        error.setText("File not found!");
                    }
                }
            }
        });

        Button invert = new Button("Invert Image");
        invert.setOnAction(event -> {
            if(view.getImage() == null){
                error.setText("No image loaded!");
            } else{
                view.setImage(invertImage((WritableImage) view.getImage()));
                error.setText("");
            }

        });

        Button grayify = new Button("Grayify Image");
        grayify.setOnAction(event -> {
            if(view.getImage() == null){
                error.setText("No image loaded!");
            } else{
                view.setImage(grayifyImage((WritableImage) view.getImage()));
                error.setText("");
            }
        });

        Button pixelate = new Button("Pixelate Image");
        pixelate.setOnAction(event -> {
            if(view.getImage() == null){
                error.setText("No image loaded!");
            } else{
                view.setImage(pixelateImage((WritableImage) view.getImage()));
                error.setText("");
            }

        });

        Button flip = new Button("Flip Image");
        flip.setOnAction(event -> {
            if(view.getImage() == null){
                error.setText("No image loaded!");
            } else{
                view.setImage(flipImage((WritableImage) view.getImage()));
                error.setText("");
            }

        });
        hbox.getChildren().addAll(load, save, invert, grayify, pixelate, flip);

        root.setCenter(label);

        Scene scene = new Scene(root, 700, 500);
        primaryStage.setTitle("Image Manipulator Inator");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private FileChooser configureFileChooser(){
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("PPM", "*.ppm")
        );
        fileChooser.setInitialDirectory(
            new File(System.getProperty("user.home"))
        );
        return fileChooser;
    }

    @Override
    public WritableImage loadImage(String filename) throws FileNotFoundException, InputMismatchException {
        File file = cleanFile(filename);
        Scanner scanner = new Scanner(file);
        if(!scanner.nextLine().equals("P3")){
            scanner.close();
            throw new InputMismatchException();
        }
        int width = scanner.nextInt();
        int height = scanner.nextInt();
        double maxColor = scanner.nextInt();

        if(maxColor != 255){
            scanner.close();
            throw new InputMismatchException();
        }

        WritableImage out = new WritableImage(width, height);
        PixelWriter writer = out.getPixelWriter();
        for(int x = 0, y = 0; scanner.hasNextDouble(); x++){
            if(x == width){
                y++;
                x = 0;
            }
            double red = scanner.nextDouble() / maxColor;
            if(!scanner.hasNextDouble()){
                error.setText("Incorrect file format!");
                scanner.close();
                return null;
            }
            double green = scanner.nextDouble() / maxColor;
            if(!scanner.hasNextDouble()){
                error.setText("Incorrect file format!");
                scanner.close();
                return null;
            }
            double blue = scanner.nextDouble() / maxColor;
            Color color = new Color(red, green, blue, 1.0);
            writer.setColor(x, y, color);
        }

        scanner.close();
        return out;

    }

    /**
     * Copies the contents of the given File, but without comments
     * 
     * @param filename String corresponding to the filename of a .ppm File
     * @return new .ppm File without comments
     * @throws FileNotFoundException
     */
    private File cleanFile(String filename) throws FileNotFoundException{
        // Assumes files are .ppm files
        File in = new File(filename);
        String path = in.getAbsolutePath();
        File out = new File(path.substring(0, path.length() - 4) + "_clean.ppm");
        try {
            if(!out.createNewFile()){
                // Clean file already exists
                return out;
            }
        } catch (IOException e) {
            error.setText("File failed to load!");
        }
        Scanner scanner = new Scanner(in);
        PrintWriter writer = new PrintWriter(out);
        while(scanner.hasNextLine()){
            String line = scanner.nextLine();
            if(line.startsWith("#")){
                continue;
            }
            writer.println(line);
        }
        scanner.close();
        writer.close();
        return out;
    }

    @Override
    public void saveImage(String filename, WritableImage image) throws FileNotFoundException {
        File file = new File(filename);
        PrintWriter writer = new PrintWriter(file);
        PixelReader reader = image.getPixelReader();
        writer.println("P3");
        writer.println("# " + file.getName());
        writer.printf("%d %d\n", (int) image.getWidth(), (int) image.getHeight());
        writer.println(255);
        for(int y = 0; y < image.getHeight(); y++){
            for(int x = 0; x < image.getWidth(); x++){
                Color color = reader.getColor(x, y);
                int red = (int) (color.getRed() * 255);
                int green = (int) (color.getGreen() * 255);
                int blue = (int) (color.getBlue() * 255);
                writer.printf("%d %d %d\n", red, green, blue);
            }
        }
        writer.close();
    }

    @Override
    public WritableImage invertImage(WritableImage image) {
        WritableImage out = new WritableImage((int)image.getWidth(), (int)image.getHeight());
        PixelWriter writer = out.getPixelWriter();
        PixelReader reader = image.getPixelReader();
        for(int y = 0; y < image.getHeight(); y++){
            for(int x = 0; x < image.getWidth(); x++){
                writer.setColor(x, y, reader.getColor(x, y).invert());
            }
        }
        return out;
    }

    @Override
    public WritableImage grayifyImage(WritableImage image) {
        WritableImage out = new WritableImage((int)image.getWidth(), (int)image.getHeight());
        PixelWriter writer = out.getPixelWriter();
        PixelReader reader = image.getPixelReader();
        for(int y = 0; y < image.getHeight(); y++){
            for(int x = 0; x < image.getWidth(); x++){
                // Color color = reader.getColor(x, y).grayscale()
                // Cannot use the above because a specific rgb formula is given
                Color color = reader.getColor(x, y);
                double brightness = Math.floor((0.2989 * color.getRed()*255 + 0.5870 * color.getGreen()*255 + 0.1140 * color.getBlue()*255)) / 255;
                Color grayifiedColor = new Color(brightness, brightness, brightness, 1.0);
                writer.setColor(x, y, grayifiedColor);
            }
        }
        return out;
    }

    @Override
    public WritableImage pixelateImage(WritableImage image) {
        WritableImage out = new WritableImage((int)image.getWidth(), (int)image.getHeight());
        PixelWriter writer = out.getPixelWriter();
        PixelReader reader = image.getPixelReader();
        boolean horizontalEdge = false;
        boolean verticalEdge = false;
        for(int x = 2, y = 2; y < (int) image.getHeight() ; x += 5){
            if (x >= (int) image.getWidth()) {
                x = (int) image.getWidth()-1;
                horizontalEdge = true;
            }
            if (y >= (int) image.getHeight()) {
                y = (int) image.getHeight()-1;
                verticalEdge = true;
            }
            for(int a = x-2, b = y-2; b <= ((verticalEdge) ? y : (y+2 >= (int) image.getHeight()) ? (int) image.getHeight()-1: y+2 ); a++) {
                if (!horizontalEdge && !((x+2 >= (int) image.getWidth()))) {
                    writer.setColor(a, b, reader.getColor(x, y));
                }
                if(a >= ((horizontalEdge) ? x : (x+2 >= (int) image.getWidth()) ? (int) image.getWidth()-1: x+2 )) {
                    a = x-3;
                    b++;
                }
            }
            if (horizontalEdge || (x+2 >= (int) image.getWidth()) ) {
                x = -3;
                y+=5;
                horizontalEdge = false;
            }
        }
        return out;
    }

    @Override
    public WritableImage flipImage(WritableImage image) {
        int width = (int)image.getWidth();
        int height = (int)image.getHeight();
        WritableImage out = new WritableImage(width, height);
        PixelReader reader = image.getPixelReader();
        PixelWriter writer = out.getPixelWriter();
        int w2 = width - 1;
        int h2 = height - 1;
        for(int i = 0; i < width - 1; i++)
        {
            for(int j = 0; j < height - 1; j++){
                Color current = reader.getColor(i,j);
                writer.setColor(w2,h2,current);
                h2--;
            }
            h2 = height - 1;
            w2--;
        }
        return out;
    }
}
