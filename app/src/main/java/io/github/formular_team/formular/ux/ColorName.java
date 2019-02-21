package io.github.formular_team.formular.ux;

import android.graphics.Color;
import android.util.Log;

import java.util.ArrayList;

public class ColorName {

    private String colorName;
    private int red, green, blue;

    public ColorName(int color)
    {
        this("", Color.red(color), Color.green(color), Color.blue(color));
    }

    public ColorName(String colorName, int red, int green, int blue) {
        this.red = red;
        this.green = green;
        this.blue = blue;

        if(colorName.compareTo("") == 0)
            //this.colorName = determineBasicColor();
            this.colorName = determineColorWithSmallestRgbDistance();
        else
            this.colorName = colorName;
    }

    //Getters for ColorName's member variables
    public int getRed() {
        return this.red;
    }

    public int getGreen() {
        return this.green;
    }

    public int getBlue() {
        return this.blue;
    }

    public String getName() {
        return this.colorName;
    }


    private String determineColorWithSmallestRgbDistance() {

        ColorNames colorNames = new ColorNames();
        ArrayList<ColorName> colorList = colorNames.getColorList();

        int curSmallestRgbDis = computeRgbDistance(colorList.get(0).red, colorList.get(0).green, colorList.get(0).blue);
        String closestColorName = colorList.get(0).getName();

        for(ColorName c : colorList) {
            int computedDistance = computeRgbDistance(c.red, c.green, c.blue);
            Log.d("COLOR_LOG", "CurColor = " + closestColorName + ", rgbDis: " + curSmallestRgbDis + ".    Compared Against: " + c.getName() + ", it's rgb distance = " + computedDistance);
            if (computedDistance < curSmallestRgbDis) {
                curSmallestRgbDis = computedDistance;
                closestColorName = c.getName();
            }

        }

        return closestColorName;
    }

    private int computeRgbDistance(int r, int g, int b) {
        return  Math.abs(this.getRed() - r)    +    Math.abs(this.getGreen() - g)    +    Math.abs(this.getBlue() - b);
    }







    //Everything below was abandoned / deprecated after realizing what the real issue with my other algorithms was.

    private String determineNameUsingMSE() {

        ColorNames colorNames = new ColorNames();
        ArrayList<ColorName> colorList = colorNames.getColorList();

        ColorName closestMatch = null;
        int minMSE = Integer.MAX_VALUE;
        int mse;

        for (ColorName c : colorList) {
            mse = c.computeMSE(red, green, blue);
            if (mse < minMSE) {
                minMSE = mse;
                closestMatch = c;
            }
        }

        if (closestMatch != null) {
            return closestMatch.getName();
        } else {
            return null;
        }
    }

    private int computeMSE(int pixR, int pixG, int pixB) {

        return ((pixR-red)*(pixR-red) + (pixG-green)*(pixG-green) + (pixB-blue)*(pixB-blue)) / 3;
    }


    private String determineBasicColor() {

        if(this.red > this.green + this.blue)
            return "Red";
        if(this.green > this.red + this.blue)
            return "Green";
        if(this.blue > this.red + this.green)
            return "Blue";

        if(this.red > 244 && this.green > 244 && this.blue > 244)
            return "White";

        if(diffBetweenAllIsSmallerThan(4) && sumOfAll() < 100)
            return "Black";

        if(diffBetweenAllIsSmallerThan(4) && sumOfAll() < 650)
            return "Gray";

        if(sumOfRG() > 460 && this.blue < 60)
            return "Sunny";

        if(sumOfRB() > 200 && this.green < 50)
            return "Royal";

        if(sumOfRB() > 300 && this.green < 100)
            return "Purple";

        if(diffBetweenGreenAndBlueSmallerThan(10) && this.red < 20 && sumOfGB() > 200)
            return "Teal";

        return "Unknown";
    }

    private boolean allValuesDifferLessThan5() {
        if(this.red - this.green < 5 && this.green - this.blue < 5 && this.blue - this.red < 5)
            return true;
        return false;
    }

    private boolean diffBetweenAllIsSmallerThan(int diff) {
        return(sumOfAll() % this.red < diff && sumOfAll() % this.green < diff && sumOfAll() % this.blue < diff);
    }

    private boolean diffBetweenRedAndGreenSmallerThan(int diff) {
        return(sumOfRG() % this.red < diff && sumOfRG() % this.green < diff);
    }

    private boolean diffBetweenGreenAndBlueSmallerThan(int diff) {
        return(sumOfGB() % this.green < diff && sumOfGB() % this.blue < diff);
    }

    private int sumOfAll() {
        return this.red + this.green + this.blue;
    }

    private int sumOfRG() {
        return this.red + this.green;
    }

    private int sumOfGB() {
        return this.green + this.blue;
    }

    private int sumOfRB() {
        return this.red + this.blue;
    }

}
