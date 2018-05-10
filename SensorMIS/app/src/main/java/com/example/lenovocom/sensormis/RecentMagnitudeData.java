package com.example.lenovocom.sensormis;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RecentMagnitudeData {
    private int windowSize;
    List <Double> magnitudeValues;

    public RecentMagnitudeData(int windowSize){
        magnitudeValues = new ArrayList<>();
        initiateWindow(windowSize);

    }

    public void initiateWindow(int windowSize){
        this.windowSize = windowSize;//(int) Math.pow(2, windowSize);
        if(this.windowSize < magnitudeValues.size()){
            int endloop = magnitudeValues.size() - this.windowSize;
            for(int i=0 ; i < endloop ; i++){
                magnitudeValues.remove(0);
            }
        }
        else {
            Random rand = new Random();
            int endloop = this.windowSize - magnitudeValues.size();
            for (int i = 0; i < endloop; i++) {
                magnitudeValues.add(rand.nextDouble());
            }

        }
    }

    public void addToQueue(double newItem){
        if(magnitudeValues.size() >= windowSize)
            magnitudeValues.remove(0);
        magnitudeValues.add(newItem);
    }

    public double[] getRecentWindow(){
        double[] magnitudeArray = new double[windowSize];
        for(int i = 0; i < windowSize ; i++)
            magnitudeArray[i] = magnitudeValues.get(i);
        return magnitudeArray;
    }




}

