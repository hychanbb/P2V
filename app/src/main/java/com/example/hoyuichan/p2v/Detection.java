package com.example.hoyuichan.p2v;

import com.example.hoyuichan.p2v.MultiplePhotoSelection.CustomGallery;

import org.bytedeco.javacpp.opencv_core;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import static org.bytedeco.javacpp.opencv_core.cvAvgSdv;
import static org.bytedeco.javacpp.opencv_imgcodecs.cvLoadImage;
import static org.bytedeco.javacpp.opencv_imgcodecs.imread;
import static org.bytedeco.javacpp.opencv_imgproc.cvLaplace;

/**
 * Created by hoyuichan on 4/26/2016.
 */
public class Detection {

    public boolean resDetection(String path){
        opencv_core.Mat m1 = imread(path);
        int h = m1.arrayHeight();
        int w = m1.arrayWidth();
        if ((h<240) || (w<320)){
            return true;
        }
        return false;
    }

    public boolean blurDetection( String path ){
        opencv_core.IplImage image1 = cvLoadImage(path);
        cvLaplace(image1, image1);
        opencv_core.CvMat image2 = image1.asCvMat();
        opencv_core.CvScalar mean = new opencv_core.CvScalar();
        opencv_core.CvScalar stddev = new opencv_core.CvScalar();
        cvAvgSdv(image2, mean, stddev, null);
        Double m = mean.val(1);
        Double d = stddev.val(1);
        image2.release();
        image1.release();
        double result =  d*d;
        return (result< 300);
    }

    public double[][] getHist (String path){
        opencv_core.Mat m1 = imread(path);
        ByteBuffer pixels = m1.getByteBuffer();
        int height = m1.arrayHeight();
        int width = m1.arrayWidth();
        double [][] hist = new double [3][256];
        int red, green, blue;
        for(int y = 0; y <height; y++) {
            for (int x = 0; x < width; x++) {
                int index = y * width + x;
                red = pixels.get(index)>>16 & 0xFF;
                green =pixels.get(index)>>8 & 0xFF;
                blue =pixels.get(index) & 0xFF;
                hist[0][red]++;
                hist[1][green]++;
                hist[2][blue]++;
            }
        }
        for (int j = 0; j<256 ; j++){
            for(int i =0; i<3 ; i++){
                hist[i][j] = hist[i][j] / (width* height);
            }
        }
        return hist;
    }

    public boolean simDetection (double [][] histA, double[][] histB){
        double sim= (double) 0.0;
        for (int i=0; i<histA.length;i++){
            for(int j=0 ; j<histA[0].length ; j++){
                sim = sim +Math.sqrt(histA[i][j]*histB[i][j]);
            }
        }
        double threshold = (double) 0.97;
        double result = sim/3;
        return (result>threshold);
    }

    public void simCheckingForAnGalleryArray(ArrayList<CustomGallery> selecedGallery){
        for (int i =0 ; i< selecedGallery.size()-1 ; i++){
            for (int j =i+1 ; j< selecedGallery.size() ; j++){
                if (simDetection(getHist(selecedGallery.get(i).sdcardPath), getHist(selecedGallery.get(j).sdcardPath))){
                    if(selecedGallery.get(i).averageSmile > selecedGallery.get(j).averageSmile) {
                        selecedGallery.get(j).isSim = true;
                    }
                    else{
                        selecedGallery.get(i).isSim = true;
                    }
                }
            }
        }
    }
}
