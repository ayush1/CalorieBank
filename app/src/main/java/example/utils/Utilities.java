package example.utils;

/**
 * Created by ayushgarg on 17/06/17.
 */

public class Utilities {

    public int convertFootToInch(int foot){
        return foot * 12;
    }

    public float convertInchToMeter(int inch){
        return (float) (inch * 2.54)/100;
    }

    public float calculateBMI(float height, float weight){
        float result = weight/height;
        return result/height;
    }


//    Use Harris Benedict Method to calculate calories.
    /*
    Little or No Exercise, Desk Job:	                1.2 x BMR
    Light Exercise, Sports 1 to 3 Times Per Week:	    1.375 x BMR
    Moderate Exercise, Sports 3 to 5 Times Per Week:	1.55 x BMR
    Heavy Exercise, Sports 6 to 7 Times Per Week:   	1.725 x BMR
    */

    public double calculateCalories(int age, float height, String weight, String gender) {
        double bmr = 0;
        if(gender.equalsIgnoreCase("F")){
            bmr = 655 + (4.35 * convertKgToPound(Float.parseFloat(weight))) + (4.7 * convertMeterToInch(height)) - (4.7 * age);
        }else if(gender.equalsIgnoreCase("M")){
            bmr = 66 + (6.23 * convertKgToPound(Float.parseFloat(weight))) + (12.7 * convertMeterToInch(height)) - (6.8 * age);
        }
        return bmr * 1.375;
    }

    private double convertKgToPound(float weight) {
        return 2.20 * weight;
    }

    private double convertMeterToInch(float height) {
        return height * 39.7;
    }

}
