package io.kellermann.services.youtube;

import io.kellermann.config.VideoConfiguration;
import io.kellermann.model.gdVerwaltung.WorshipMetaData;
import io.kellermann.services.UtilityComponent;
import io.kellermann.services.video.GdGenerationService;
import io.kellermann.services.video.JaffreeFFmpegService;
import jakarta.annotation.PostConstruct;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.Objdetect;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import java.awt.*;
import java.awt.font.TextLayout;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


@Service
public class ThumbnailService {


    private final VideoConfiguration videoConfiguration;
    private final UtilityComponent utilityComponent;
    private JaffreeFFmpegService jaffreeFFmpegService;
    private GdGenerationService gdGenerationService;
    private CascadeClassifier faceClassifier;
    private CascadeClassifier eyeClassifier;

    public ThumbnailService(VideoConfiguration videoConfiguration, JaffreeFFmpegService jaffreeFFmpegService, GdGenerationService gdGenerationService, UtilityComponent utilityComponent) {
        this.videoConfiguration = videoConfiguration;
        this.jaffreeFFmpegService = jaffreeFFmpegService;
        this.gdGenerationService = gdGenerationService;
        this.utilityComponent = utilityComponent;
    }

    public void generateThumbnails(WorshipMetaData worshipMetaData) throws IOException {
        Path tempWorkspace = videoConfiguration.getTempWorkspace().resolve("thumbnails");
        Path allImages = tempWorkspace.resolve("all");
        Path headSearch = tempWorkspace.resolve("head");
        Path eyeSearch = tempWorkspace.resolve("eye");

        if (Files.notExists(tempWorkspace)) {
            Files.createDirectories(tempWorkspace);
            Files.createDirectories(allImages);
            Files.createDirectories(headSearch);
            Files.createDirectories(eyeSearch);
        }
//        Turn video to images
        jaffreeFFmpegService.generateImageFromVideo(
                videoConfiguration.getGdVideoStartTime(),
                videoConfiguration.getGdVideoEndTime(),
                utilityComponent.getMainRecording(worshipMetaData),
                allImages,
                2);

        List<Path> collect = Files.walk(allImages).filter(Files::isRegularFile).toList();

        List<Path> candidates = new ArrayList<>();

        for (Path path : collect) {
            Candidate candidate = detectFace(path);
            if (candidate.isCandidate) {
                drawThumbnail(path, candidate.centerPoint, worshipMetaData);
            }

        }

    }


    public Candidate detectFace(Path source) {
        Candidate candidate = new Candidate();
        String file = source.toFile().getAbsolutePath();
        Mat imageMat = Imgcodecs.imread(file);

        // Instantiating the CascadeClassifier
        int minFaceSize = Math.round(imageMat.rows() * 0.1f);
        // Detecting the face in the snap
        MatOfRect faceDetections = new MatOfRect();
        faceClassifier.detectMultiScale(imageMat,
                faceDetections,
                1.1,
                10,
                Objdetect.CASCADE_SCALE_IMAGE,
                new Size(minFaceSize, minFaceSize),
                new Size(minFaceSize * 2, minFaceSize * 2));

        int faceCount = faceDetections.toArray().length;
        if (faceCount > 0) {
            Rect faceRect = faceDetections.toArray()[0];
            Mat submat = imageMat.submat(faceRect);

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

                MatOfRect eyeDetections = new MatOfRect();
                eyeClassifier.detectMultiScale(submat,
                        eyeDetections,
                        1.1,
                        10,
                        Objdetect.CASCADE_SCALE_IMAGE,
                        new Size(15, 15),
                        new Size(40, 40));

                for (Rect rect : eyeDetections.toArray()) {
                    Imgproc.rectangle(
                            submat,                                               // where to draw the box
                            new Point(rect.x, rect.y),                            // bottom left
                            new Point(rect.x + rect.width, rect.y + rect.height), // top right
                            new Scalar(0, 0, 255),
                            3                                                     // RGB colour
                    );
                }
                Imgcodecs.imwrite(source.getParent().getParent().resolve("head").resolve(source.getFileName()).toAbsolutePath().toString(), submat);

//                if (eyeDetections.toArray().length > 0) {
                    candidate.setCenterPoint(centerOfRect);
                    Imgcodecs.imwrite(source.getParent().getParent().resolve("eye").resolve(source.getFileName()).toAbsolutePath().toString(), imageMat);
                    return candidate;
//                }
            }
        }
        return candidate;
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

    public void drawThumbnail(Path imagePath, Point centerOfFace, WorshipMetaData worshipMetaData) throws IOException {
        BufferedImage screenShot = ImageIO.read(imagePath.toFile());
        BufferedImage bufferedImage = new BufferedImage(screenShot.getWidth(), screenShot.getHeight(), BufferedImage.TYPE_INT_RGB);
        int fiftyPercentageWidth = bufferedImage.getWidth() / 2;

        BufferedImage croppedScreenshot = screenShot.getSubimage(
                (int) (centerOfFace.x - (fiftyPercentageWidth / 2.0)),
                0,
                fiftyPercentageWidth,
                screenShot.getHeight());
        Graphics2D graphics = (Graphics2D) bufferedImage.getGraphics();
        graphics.setPaint(new Color(255, 255, 255));
        graphics.fillRect(0, 0, bufferedImage.getWidth(), bufferedImage.getHeight());
        graphics.drawImage(croppedScreenshot, fiftyPercentageWidth, 0, null);


        try {
            if (Objects.nonNull(worshipMetaData.getSeries().getTitleLanguage(worshipMetaData.getServiceLanguage()))) {
                drawSeriesTitle(bufferedImage, worshipMetaData.getSeries().getTitleLanguage(worshipMetaData.getServiceLanguage()));
                drawTitle(bufferedImage, worshipMetaData.getServiceTitle(worshipMetaData.getServiceLanguage()));
            } else {
                drawTitleOpenTopic(bufferedImage, worshipMetaData.getServiceTitle(worshipMetaData.getServiceLanguage()));
            }

            drawPastor(bufferedImage, worshipMetaData.getPerson().getFirstName() + " " + worshipMetaData.getPerson().getLastName());

            insertImage(bufferedImage, ImageIO.read(videoConfiguration.getResources().resolve("silbern_small.png").toFile()), 0.86);


            ImageWriter jpgWriter = ImageIO.getImageWritersByFormatName("jpg").next();
            ImageWriteParam jpgWriteParam = jpgWriter.getDefaultWriteParam();
            jpgWriteParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            jpgWriteParam.setCompressionQuality(1f);

            Path tumbnailsOutPath = videoConfiguration.getTempWorkspace().resolve("thumbnails").resolve("candidates");
            if (Files.notExists(tumbnailsOutPath)) {
                Files.createDirectory(tumbnailsOutPath);
            }


            jpgWriter.setOutput(ImageIO.createImageOutputStream(tumbnailsOutPath.resolve(imagePath.getFileName()).toFile()));
            IIOImage outputImage = new IIOImage(bufferedImage, null, null);
            jpgWriter.write(null, outputImage, jpgWriteParam);
            jpgWriter.dispose();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void drawSeriesTitle(BufferedImage bf, String seriesText) {
        var font = new Font("Source Sans 3 Regular", Font.PLAIN, 100);
        drawText(bf, seriesText, font, .205, .205);
    }

    public void drawPastor(BufferedImage bf, String title) {
        var font = new Font("Source Sans 3 Regular", Font.PLAIN, 92);
        drawText(bf, title, font, .775, .775);
    }

    public void drawTitle(BufferedImage bf, String title) {
        var font = new Font("Source Sans 3 ExtraLight", Font.PLAIN, 68);
        drawText(bf, title, font, .215, .775);
    }

    public void drawTitleOpenTopic(BufferedImage bf, String title) {
        var font = new Font("Source Sans 3 ExtraLight", Font.PLAIN, 68);
        drawText(bf, title, font, .205, .5);
    }


    public void drawText(BufferedImage bufferedImage, String text, Font font, double startPosition, double endPosition) {
        Graphics2D g = bufferedImage.createGraphics();
        g.setFont(font);
        g.setPaint(Color.BLACK);
        String upperCase = text.toUpperCase();

        var textLayout = new TextLayout(upperCase, g.getFont(), g.getFontRenderContext());

        double textWidth = textLayout.getBounds().getWidth();
        double textHeight = textLayout.getBounds().getHeight();
        double heightPosition = (startPosition + endPosition) / 2.0;


        if (textLayout.getBounds().getWidth() > (bufferedImage.getWidth() / 2.0 * .95)) {
            textHeight = textHeight * 1.3;
            List<String> titleLines = getTitleLines(bufferedImage, font, List.of(text));
            double textBlockHeight = (int) textHeight * (titleLines.size());
            int yBlockAnchor = (int) ((bufferedImage.getHeight() * heightPosition) - textBlockHeight / 2.0 - textHeight / 2.0);

            for (String titleLine : titleLines) {
                yBlockAnchor = (int) (yBlockAnchor + textHeight);
                double yPercentage = yBlockAnchor / (double) bufferedImage.getHeight();
                drawText(bufferedImage, titleLine, font, yPercentage, yPercentage);
            }
            return;
        }

        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        int xAnchor = (int) (bufferedImage.getWidth() / 4.0 - textWidth / 2.0);
        int yAnchor = (int) ((bufferedImage.getHeight() * heightPosition) + textHeight / 2.0);

        g.drawString(text.toUpperCase(), xAnchor, yAnchor);
    }


    public BufferedImage insertImage(BufferedImage main, BufferedImage insert, double height) {
        Graphics2D graphics = (Graphics2D) main.getGraphics();
        int quarterPoint = (int) (main.getWidth() / 4.0 - insert.getWidth() / 2.0);
        graphics.drawImage(insert, quarterPoint, (int) (main.getHeight() * height), null);
        return main;
    }


    public List<String> getTitleLines(BufferedImage bf, Font font, List<String> longTitles) {
        List<String> titleLines = new ArrayList<>();
        int lines = longTitles.size() + 1;
        String fullTitle = listToString(longTitles);
        int startInd = 0;
        int whiteSpaceIndex = getClosestWhitespaceForFirstPart(fullTitle, lines);
        for (int i = 0; i < lines - 1; i++) {
            titleLines.add(fullTitle.substring(startInd, whiteSpaceIndex));
            startInd = whiteSpaceIndex;
            if (lines - (i + 1) > 1) {
                whiteSpaceIndex = getClosestWhitespaceForFirstPart(fullTitle.substring(startInd), lines - (i + 1)) + startInd;
            }
        }
        titleLines.add(fullTitle.substring(startInd));
        Graphics2D graphics = bf.createGraphics();
        graphics.setFont(font);
        for (String titleLine : titleLines) {
            TextLayout textLayout = new TextLayout(titleLine.toUpperCase(), graphics.getFont(), graphics.getFontRenderContext());
            if (textLayout.getBounds().getWidth() > (bf.getWidth() / 2.0 * .95)) {
                return getTitleLines(bf, font, titleLines);
            }
        }

        return titleLines.stream().map(s -> s.replaceAll("(^[\\s–—―‒-]+)|([\\s–—―‒-]+$)", "")).toList();
    }

    public String listToString(List<String> list) {
        return list.stream()
                .map(n -> String.valueOf(n))
                .collect(Collectors.joining(" ", "", ""));
    }

    public int getClosestWhitespaceForFirstPart(String text, int parts) {

        int length = text.length();
        int initialIndex = (int) ((double) length / (double) parts);
        int searchIndexUpper = (int) ((double) length / (double) parts);
        int searchIndexLower = (int) ((double) length / (double) parts);
        char[] charArray = text.toCharArray();
        while (charArray[searchIndexUpper] != ' ') {
            searchIndexUpper++;
        }
        while (charArray[searchIndexLower] != ' ') {
            searchIndexLower--;
        }


        if (initialIndex - searchIndexLower > searchIndexUpper - initialIndex) {
            return searchIndexUpper;
        } else {
            return searchIndexLower;
        }
    }

    @PostConstruct
    public void prepareThumbnailEngine() {
        Path libPath = Paths.get("lib");
        GraphicsEnvironment ge = null;
        try {
            extractResourceDir("lib", libPath);
            System.load(libPath.resolve("opencv_java490.dll").toAbsolutePath().toFile().toString());
            String faceXML = libPath.resolve("lbpcascade_frontalface.xml").toFile().toString();
            String eyeXML = libPath.resolve("haarcascade_eye_tree_eyeglasses.xml").toFile().toString();
            faceClassifier = new CascadeClassifier(faceXML);
            eyeClassifier = new CascadeClassifier(eyeXML);
            ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, this.getClass().getResourceAsStream("/fonts/SourceSans3-ExtraLight.ttf")));
            ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, this.getClass().getResourceAsStream("/fonts/SourceSans3-Regular.ttf")));


        } catch (Exception e) {
        }
    }

    public void extractResourceDir(String resourceDir, Path target) throws IOException {
        ClassLoader cl = this.getClass().getClassLoader();
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(cl);
        Resource[] resources = resolver.getResources("classpath:/" + resourceDir + "/**");
        if (Files.notExists(target)) {
            Files.createDirectories(target);
        }
        for (Resource resource : resources) {
            byte[] resourceFile = FileCopyUtils.copyToByteArray(resource.getInputStream());
            if (Files.notExists(target.resolve(resource.getFilename()))) {
                Files.write(target.resolve(resource.getFilename()), resourceFile);
            }
        }
    }

    class Candidate {
        private Point centerPoint;
        private boolean isCandidate = false;

        public void setCenterPoint(Point centerPoint) {
            isCandidate = true;
            this.centerPoint = centerPoint;
        }

        public Point getCenterPoint() {
            return centerPoint;
        }

        public boolean isCandidate() {
            return isCandidate;
        }
    }
}
