package com.dch;

import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.imgproc.CLAHE;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.Collections;

public class App {
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    private static JFrame frame;
    private static JFrame secondFrame;
    private static JFrame settingsFrame;
    private static JLabel imageLabel;
    private static JLabel secondImageLabel;
    private static JCheckBox faceDetection;
    private static JCheckBox eyeDetection;


    private static String frontXMLFile = "D:\\lbpcascade_frontalface_improved.xml";
    private static String profileXMLFile = "D:\\lbpcascade_profileface.xml";
    private static String eyeXMLFile = "D:\\haarcascade_eye_tree_eyeglasses.xml";
    private static String rightEyeXMLFile = "D:\\haarcascade_lefteye_2splits.xml";

    public static void main(String[] args) {
        App app = new App();
        app.initGUI();
        //app.runMainLoop();
        CameraWindow c0 = new CameraWindow(1, imageLabel);
        c0.start();
        CameraWindow c1 = new CameraWindow(0, secondImageLabel);
        c1.start();
    }

    private void initGUI() {
        frame = new JFrame("Camera 1");
        secondFrame = new JFrame("Camera 2");
        settingsFrame = new JFrame("Settings");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        secondFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        settingsFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        faceDetection = new JCheckBox("Распознавание лиц", false);
        faceDetection.setVerticalAlignment(JCheckBox.TOP);
        eyeDetection = new JCheckBox("Распознавание глаз", false);
        eyeDetection.setVerticalAlignment(JCheckBox.BOTTOM);
        frame.setSize(400, 400);
        secondFrame.setSize(400, 400);
        settingsFrame.setSize(1700, 550);
        imageLabel = new JLabel();
        secondImageLabel = new JLabel();
        //secondImageLabel.setText("Hello");
        //frame.add(secondImageLabel);
        //frame.add(imageLabel);
        //secondFrame.add(secondImageLabel);
        JPanel jPanel = new JPanel(new VerticalLayout());
        jPanel.add(imageLabel);
        jPanel.add(secondImageLabel);
        jPanel.add(eyeDetection);
        jPanel.add(faceDetection);

        settingsFrame.add(jPanel);

        frame.setVisible(false);
        secondFrame.setVisible(false);
        settingsFrame.setVisible(true);

    }


    static class CameraWindow extends Thread {
        int i;
        JLabel jl;
        int count;
        int work = 1;
        //int halfWork = work / 2;

        public CameraWindow(int i, JLabel jl) {
            this.i = i;
            this.jl = jl;
            if (i == 0) {
                count = work;
            } else {
                count = work / 2;
            }
        }

        @Override
        public void run() {
            super.run();
            runMainLoop(i);
        }

        private void runMainLoop(int i) {
            CascadeClassifier cascadeFrontClassifier = new CascadeClassifier(frontXMLFile);
            CascadeClassifier cascadeProfileClassifier = new CascadeClassifier(profileXMLFile);
            CascadeClassifier cascadeEyeClassifier = new CascadeClassifier(rightEyeXMLFile);
            MatOfRect faceFrontDetection = new MatOfRect();
            MatOfRect faceProfileDetection = new MatOfRect();
            MatOfRect eyeDetection_ = new MatOfRect();

            CLAHE clahe = Imgproc.createCLAHE();
            clahe.setClipLimit(4);

            ImageProcessor imageProcessor = new ImageProcessor();
            Mat webcamMatImage = new Mat();
            Mat webcamMatImageGray = new Mat();
            Image tempImage;
            VideoCapture capture = new VideoCapture();
            capture.set(Videoio.CAP_PROP_FRAME_WIDTH, 240);
            capture.set(Videoio.CAP_PROP_FRAME_HEIGHT, 320);
            capture.open(i);
            if (capture.isOpened()) {
                while (true) {
                    capture.read(webcamMatImage);
                    if (!webcamMatImage.empty()) {
                        //Ресайз изображений
                        Imgproc.resize(webcamMatImage, webcamMatImage, new Size(426, 240));
                        Imgproc.cvtColor(webcamMatImage,webcamMatImageGray, Imgproc.COLOR_BGR2GRAY);
//                        Imgproc.equalizeHist(webcamMatImageGray, webcamMatImageGray);
//                        Imgproc.equalizeHist(webcamMatImageGray, webcamMatImageGray);
                        clahe.apply(webcamMatImageGray, webcamMatImageGray);
                        //System.out.println("Cam "+i+"\nwidth:"+webcamMatImage.width()+"\nheight:"+webcamMatImage.height()+"\n\n\n\n");
                        if (faceDetection.isSelected()) {
                            if (count == 0) {
                                //Определение лица (фронт)
                                cascadeFrontClassifier.detectMultiScale(webcamMatImageGray, faceFrontDetection);
                                for (Rect rect : faceFrontDetection.toArray()) {
                                    Imgproc.rectangle(webcamMatImage, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(0, 0, 255), 1);
                                }
                                count = work;
                            } else {

                                //Профиль лица детектит плохо (и это файл от разработчика)
//                            if (count == halfWork) {
//                                cascadeProfileClassifier.detectMultiScale(webcamMatImage, faceProfileDetection);
//                                for (Rect rect : faceProfileDetection.toArray()) {
//                                    Imgproc.rectangle(webcamMatImage, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(0, 0, 255), 3);
//                                }
//                                //System.out.println("Profile");
//                            }
                            }
                            count--;
                        }

                        if (eyeDetection.isSelected()) {
                            if (count == 0)
                                cascadeEyeClassifier.detectMultiScale(webcamMatImage, eyeDetection_);
                            for (Rect rect : eyeDetection_.toArray()) {
                                Imgproc.rectangle(webcamMatImage, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(0, 155, 255), 1);
                            }
                        }


                        Imgproc.resize(webcamMatImage, webcamMatImage, new Size(640, 480));
                        tempImage = imageProcessor.toBufferedImage(webcamMatImage);
                        ImageIcon imageIcon = new ImageIcon(tempImage, "Captured video");
                        jl.setIcon(imageIcon);
                        if (i == 0) {
                            frame.pack();
                        } else {
                            secondFrame.pack();
                        }
                    } else {
                        break;
                    }
                }
            } else {
                System.out.println("Couldn't open capture");
            }
        }
    }
}