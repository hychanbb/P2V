package com.example.hoyuichan.p2v;

import org.bytedeco.javacpp.opencv_core;

/**
 * Created by hoyuichan on 4/26/2016.
 */
public class Photo implements Comparable<Photo>{

    private opencv_core.Mat mat;
    private String photoPath;
    private int numberOfFace;
    private double averageAge;
    private double varianceAge;
    private double genderRatio;
    private double meanOfAge;
    private double varianceOfAge;
    private int facePosition;
    private double levelOfSmile;

    public Photo(opencv_core.Mat mat, int facePosition, double varianceOfAge, double meanOfAge,
                 double genderRatio, double varianceAge, double averageAge, int numberOfFace, double levelOfSmile, String photoPath) {
        this.mat = mat;
        this.facePosition = facePosition;
        this.varianceOfAge = varianceOfAge;
        this.meanOfAge = meanOfAge;
        this.genderRatio = genderRatio;
        this.varianceAge = varianceAge;
        this.averageAge = averageAge;
        this.numberOfFace = numberOfFace;
        this.levelOfSmile = levelOfSmile;
        this.photoPath = photoPath;
    }

    public double getLevelOfSmile() {return levelOfSmile;}
    public int getFacePosition() {return facePosition;}
    public double getVarianceOfAge() {return varianceOfAge;}
    public double getMeanOfAge() {return meanOfAge;}
    public double getGenderRatio() {return genderRatio;}
    public double getVarianceAge() {return varianceAge;}
    public double getAverageAge() {return averageAge;}
    public int getNumberOfFace() {return numberOfFace;}
    public String getPhotoPath() {return photoPath;}
    public opencv_core.Mat getMat() {return mat;}

    public void setLevelOfSmile(double levelOfSmile) {this.levelOfSmile = levelOfSmile;}
    public void setFacePosition(int facePosition) {this.facePosition = facePosition;}
    public void setVarianceOfAge(double varianceOfAge) {this.varianceOfAge = varianceOfAge;}
    public void setMeanOfAge(double meanOfAge) {this.meanOfAge = meanOfAge;}
    public void setGenderRatio(double genderRatio) {this.genderRatio = genderRatio;}
    public void setVarianceAge(double varianceAge) {this.varianceAge = varianceAge;}
    public void setAverageAge(double averageAge) {this.averageAge = averageAge;}
    public void setNumberOfFace(int numberOfFace) {this.numberOfFace = numberOfFace;}
    public void setPhotoPath(String photoPath) {this.photoPath = photoPath;}
    public void setMat(opencv_core.Mat mat) {this.mat = mat;}


    public int compareTo(Photo another) {
        double a = this.getLevelOfSmile();
        double b = another.getLevelOfSmile();
        if (a < b) {return 1;}
        if (a > b) {return -1;}
        return 0;
    }


}
