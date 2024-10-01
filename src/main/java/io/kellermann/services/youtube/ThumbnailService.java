package io.kellermann.services.youtube;

import io.kellermann.model.gdVerwaltung.WorshipMetaData;
import jakarta.annotation.PostConstruct;
import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.Objdetect;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.font.TextLayout;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class ThumbnailService {


    public void detectFace(Path path, WorshipMetaData worshipMetaData) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        String file = path.toFile().getAbsolutePath();
        Mat imageMat = Imgcodecs.imread(file);

        // Instantiating the CascadeClassifier
        String xmlFile = "C:\\Users\\Arieh\\IdeaProjects\\AutoGD\\src\\main\\resources\\lbpcascade_frontalface.xml";
        int minFaceSize = Math.round(imageMat.rows() * 0.1f);

        CascadeClassifier classifier = new CascadeClassifier(xmlFile);

        // Detecting the face in the snap
        MatOfRect faceDetections = new MatOfRect();
        classifier.detectMultiScale(imageMat,
                faceDetections,
                1.1,
                10,
                Objdetect.CASCADE_SCALE_IMAGE,
                new Size(minFaceSize, minFaceSize),
                new Size(minFaceSize * 2, minFaceSize * 2));

        int faceCount = faceDetections.toArray().length;
        if (faceCount > 0) {
            Rect faceRect = faceDetections.toArray()[0];
            Imgproc.rectangle(
                    imageMat,                                               // where to draw the box
                    new Point(faceRect.x, faceRect.y),                            // bottom left
                    new Point(faceRect.x + faceRect.width, faceRect.y + faceRect.height), // top right
                    new Scalar(0, 0, 255),
                    3                                                     // RGB colour
            );
            double percentWidth = 0.50;
            double percentHeightCutoff = 0.40;

            Rect searchRect = new Rect(new Point((imageMat.width() * percentWidth) / 2, imageMat.height() * percentHeightCutoff),
                    new Point(imageMat.width() - ((imageMat.width() * percentWidth) / 2), faceRect.height / 2.0));

            Imgproc.rectangle(
                    imageMat,                                               // where to draw the box
                    searchRect, // top right
                    new Scalar(0, 255, 0),
                    3                                                     // RGB colour
            );
            Point centerOfRect = getCenterOfRect(faceRect);
            Imgproc.rectangle(
                    imageMat,                                               // where to draw the box
                    centerOfRect,                            // bottom left
                    centerOfRect, // top right
                    new Scalar(255, 0, 255),
                    8                                                     // RGB colour
            );
            if (isCandidate(faceRect, searchRect)) {
                try {
                    thumbnailDemo(path, centerOfRect, worshipMetaData);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                Imgcodecs.imwrite(path.getParent().getParent().resolve("detected").resolve(path.getFileName()).toAbsolutePath().toString(), imageMat);
            }
        }

    }

    public boolean isCandidate(Rect faceRect, Rect searchRect) {
        int centerX = (int) ((faceRect.x + faceRect.x + faceRect.width) / 2.0);
        int centerY = (int) ((faceRect.y + faceRect.y + faceRect.height) / 2.0);

        boolean isInside = searchRect.x < centerX && centerX < (searchRect.x + searchRect.width)
                && searchRect.y < centerY && centerY < (searchRect.y + searchRect.height);
        return isInside;
    }

    public Point getCenterOfRect(Rect faceRect) {
        int centerX = (int) ((faceRect.x + faceRect.x + faceRect.width) / 2.0);
        int centerY = (int) ((faceRect.y + faceRect.y + faceRect.height) / 2.0);
        return new Point(centerX, centerY);
    }

    public void thumbnailDemo(Path imagePath, Point centerOfFace, WorshipMetaData worshipMetaData) throws IOException {
        BufferedImage bufferedImage = new BufferedImage(1920, 1080, BufferedImage.TYPE_INT_RGB);
        BufferedImage screenShot = ImageIO.read(imagePath.toFile());
        int fiftyPercentageWidth = bufferedImage.getWidth() / 2;

        BufferedImage croppedScreenshot = screenShot.getSubimage(
                (int) (centerOfFace.x - (fiftyPercentageWidth / 2.0)),
                0,
                fiftyPercentageWidth,
                1080);
        Graphics2D graphics = (Graphics2D) bufferedImage.getGraphics();
        graphics.setPaint(new Color(255, 255, 255));
        graphics.fillRect(0, 0, bufferedImage.getWidth(), bufferedImage.getHeight());
        graphics.drawImage(croppedScreenshot, fiftyPercentageWidth, 0, null);


        try {
            drawSeriesTitle(bufferedImage, worshipMetaData.getSeries().getTitleLanguage(worshipMetaData.getServiceLanguage()));
            drawTitle(bufferedImage, worshipMetaData.getServiceTitle(worshipMetaData.getServiceLanguage()));
            drawTitle(bufferedImage, "Wie wir in unseren\nBeziehungen Frieden Stiften");
            drawPastor(bufferedImage, worshipMetaData.getPerson().getFirstName() + " " + worshipMetaData.getPerson().getLastName() );
            insertImage(bufferedImage, ImageIO.read(Paths.get("C:\\Users\\Arieh\\Desktop\\silbern_small.png").toFile()), 0.86);

            ImageIO.write(bufferedImage, "jpg", imagePath.getParent().getParent().resolve("cropped").resolve(imagePath.getFileName()).toFile());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    public void drawSeriesTitle(BufferedImage bf, String seriesText) {
        var font = new Font("Source Sans 3 Regular", Font.PLAIN, 100);
        drawText(bf, seriesText, font, .24);
    }

    public void drawPastor(BufferedImage bf, String title) {
        var font = new Font("Source Sans 3 Regular", Font.PLAIN, 92);
        drawText(bf, title, font, .805);
    }

    public void drawTitle(BufferedImage bf, String title) {
        var font = new Font("Source Sans 3 ExtraLight", Font.PLAIN, 68);
        drawText(bf, title, font, .52);
    }




    public BufferedImage drawText(BufferedImage bufferedImage, String text, Font font, double heightPosition) {

        Graphics2D g = bufferedImage.createGraphics();
        g.setFont(font);
        g.setPaint(Color.BLACK);
        String upperCase = text.toUpperCase();

        var textLayout = new TextLayout(upperCase, g.getFont(), g.getFontRenderContext());
        double textWidth = textLayout.getBounds().getWidth();

        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        g.drawString(upperCase, bufferedImage.getWidth() / 4 - (int) textWidth / 2,
                (int) (bufferedImage.getHeight() * heightPosition));


        return bufferedImage;
    }


    public BufferedImage insertImage(BufferedImage main, BufferedImage insert, double height) {
        Graphics2D graphics = (Graphics2D) main.getGraphics();
        int quarterPoint = (int) (main.getWidth() / 4.0 - insert.getWidth() / 2.0);
        graphics.drawImage(insert, quarterPoint, (int) (main.getHeight() * height), null);
        return main;
    }


    @PostConstruct
    public void loadFonts() {
        GraphicsEnvironment ge = null;
        try {
            ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, this.getClass().getResourceAsStream("/fonts/SourceSans3-ExtraLight.ttf")));
            ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, this.getClass().getResourceAsStream("/fonts/SourceSans3-Regular.ttf")));
        } catch (FontFormatException e) {
        } catch (IOException e) {
        }
    }
}
