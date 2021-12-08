package project.listick.fakegps;

import java.util.concurrent.ThreadLocalRandom;

public class Randomizer {

    public Randomizer() {

    }

    public float getElevation(float elevation, float diff){
        float min = elevation - diff;
        float max = elevation + diff;
        max -= min;

        return (int) (Math.random() * ++max) + min;
    }

    public int getRandomSpeed(int speed, int diff){
        float min = speed - diff;
        float max = speed + diff;
        max -= min;

        return (int) ((Math.random() * ++max) + min);
    }

    public float getAccuracy(float accuracy){
        double elevationDiff = ThreadLocalRandom.current().nextDouble(-2, 2);
        return (float) (accuracy + elevationDiff);
    }

    public float getBearing(float bearing, float diff) {
        return (float) (Math.random() * diff) + bearing;
    }
    public float getStaticSpeed(float speed, float diff) {
        return (float) (Math.random() * diff) + speed;
    }

    public int getArrayRunSpeed(int speed, int updatesDelay){
        if (updatesDelay >= 1000)
            return (speed * 1000) / (3600 * (updatesDelay / 1000));
        else {
            float delay = 3600 / ((float) (updatesDelay) / (float) 1000);
            float speedInMeters = ((float) speed * 1000);
            int calculatedSpeed = (int) (speedInMeters / delay);
            if (calculatedSpeed == 0)
                calculatedSpeed = 1;
            return calculatedSpeed;
        }

    }

}
