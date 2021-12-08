import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.image.*;
import javafx.scene.input.PickResult;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

public class ImageManipulatorInator extends Application implements ImageManipulatorInterface {
    private Stage stage = null;
    private Group root  = null;
    private int colorMaxval = 255;
    private int height = 100, width = 100;
    @Override
    /**
     * Load the specified PPM image file.
     * The image file must be in the PPM P3 format
     * @see http://netpbm.sourceforge.net/doc/ppm.html
     *
     * Don't forget to add a load button to the application!
     *
     * @param filename
     * @return WritableImage
     * @throws FileNotFoundException
     */
    public WritableImage loadImage(String filename) throws FileNotFoundException {
        File file = cleanFile(filename);
        Scanner scanner = new Scanner(file);
        if(!scanner.nextLine().equals("P3")){
            //TODO: Handle incorrect format
            System.out.println("Incorrect format!");
        }
        this.width = scanner.nextInt();
        this.height = scanner.nextInt();

        double maxColor = scanner.nextDouble();
        if(maxColor != 255){
            // TODO: Handle incorrect maxColor
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
                // TODO: Handle incorrect format
            }
            double green = scanner.nextDouble() / maxColor;
            if(!scanner.hasNextDouble()){
                // TODO: Handle incorrect format
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
                // clean file already exists
                return out;
            }
        } catch (IOException e) {
            //TODO: handle exception
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

    }

    @Override
    public WritableImage invertImage(WritableImage image) {
        WritableImage invert = new WritableImage(width,height);
        for(int i = 0; i <width-1;i++)
        {
            for(int j = 0; j<height-1;j++){
                Color current = image.getPixelReader().getColor(i,j);
                double b = Math.abs(255.0-(current.getBlue()*255.0));
                double r = Math.abs(255.0-(current.getRed()*255.0));
                double g = Math.abs(255.0-(current.getGreen()*255.0));
                Color inverted = Color.rgb((int)r,(int)g,(int)b);
                invert.getPixelWriter().setColor(i,j,inverted);
            }
        }

        return invert;
    }

    @Override
    public WritableImage grayifyImage(WritableImage image) {
        return null;
    }

    @Override
    public WritableImage pixelateImage(WritableImage image) {
        return null;
    }

    @Override
    public WritableImage flipImage(WritableImage image) {
        WritableImage flipped = new WritableImage(width,height);
        int w2 = width-1;
        int h2 = height-1;
        for(int i = 0; i <width-1;i++)
        {
            for(int j = 0; j<height-1;j++){
                Color current = image.getPixelReader().getColor(i,j);
                flipped.getPixelWriter().setColor(w2,h2,current);
                h2--;
            }
            h2 = height-1;
            w2--;
        }

        return flipped;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.stage = primaryStage;
        String imageName = getParameters().getRaw().get(0);
        Image i = loadImage(imageName);
        ImageView imView = new ImageView();
        imView.setImage(i);
        Image flipped = flipImage(loadImage(imageName));
        imView.setImage(flipped);
        //Image invert = invertImage(loadImage(imageName));
        //imView.setImage(invert);
        root = new Group();
        Scene scene = new Scene(root,width,height);

        root.getChildren().add(imView);
        stage.setTitle("Pen15");
        stage.setScene(scene);
        stage.show();

    }
}
