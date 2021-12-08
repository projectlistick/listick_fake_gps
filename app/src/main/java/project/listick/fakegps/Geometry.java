package project.listick.fakegps;

/*
 * Created by LittleAngry on 03.01.19 (macOS 10.12)
 * */
public class Geometry {

    public static class UnitCast {
        public int speed;
        public int speedDiff;
    }

    // calculate air pressure at altitude
    public static double calculateAirPressure(float altitude) {

        double g = 9.80665; // gravitational acceleration
        double M = 0.0289644; // molar mass
        double R = 8.31432; // universal gas constant
        double T = 288.15; // temperature - 15°C in Kelvin

        double p = 101325 * Math.exp(-g * M * (altitude - 0) / (R * T));

        return p;
    }

    public static double distance(double lat1, double lon1, double lat2, double lon2, int unit) {
        if ((lat1 == lat2) && (lon1 == lon2)) {
            return 0;
        } else {
            double theta = lon1 - lon2;
            double dist = Math.sin(Math.toRadians(lat1)) * Math.sin(Math.toRadians(lat2)) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.cos(Math.toRadians(theta));
            dist = Math.acos(dist);
            dist = Math.toDegrees(dist);
            dist = dist * 60 * 1.1515;
            dist = dist * 1.609344;

            if (unit == Preferences.METERS)
                dist = Distance.kilometersToMeters(dist);
            if (unit == Preferences.MILES)
                dist = Distance.kilometersToMiles(dist);

            return (dist);
        }
    }

    /*
     * This method is convert azimuth to trigonometric angle
     * */
    public static double getAngle(double originLat, double originLng, double destLat, double destLng, double lat, double lng) {

        double azimuth1 = getAzimuth(originLat, originLng, lat, lng);
        double azimuth2 = Math.abs(getAzimuth(lat, lng, destLat, destLng) - azimuth1);

        azimuth1 = azimuth2;
        if (azimuth2 > 180)
            azimuth1 = 360 - azimuth2;

        return 180 - azimuth1;
    }

    public static double getAzimuth(double startLat, double startLong, double destLat, double destLong) {
        double dLat = destLat - startLat;
        double dLong = destLong - startLong;

        return Math.atan2(dLong, dLat) * 180 / Math.PI;
    }

    public enum Unit {
        SPEED
    }

    public static double cast(Unit unit, int standartUnit, int castUnit, double value){
        if (unit == Unit.SPEED && standartUnit == Preferences.KILOMETERS && castUnit == Preferences.METERS){
            return Speed.kilometersToMeters(value);
        }
        return value;
    }

    static double perpendicularDistance(double px, double py, double vx, double vy, double wx, double wy) {
        return Math.sqrt(distanceToSegmentSquared(px, py, vx, vy, wx, wy));
    }

    private static double distanceToSegmentSquared(double px, double py, double vx, double vy, double wx, double wy) {
        final double l2 = distanceBetweenPoints(vx, vy, wx, wy);
        if (l2 == 0)
            return distanceBetweenPoints(px, py, vx, vy);
        final double t = ((px - vx) * (wx - vx) + (py - vy) * (wy - vy)) / l2;
        if (t < 0)
            return distanceBetweenPoints(px, py, vx, vy);
        if (t > 1)
            return distanceBetweenPoints(px, py, wx, wy);
        return distanceBetweenPoints(px, py, (vx + t * (wx - vx)), (vy + t * (wy - vy)));
    }

    private static double distanceBetweenPoints(double vx, double vy, double wx, double wy) {
        return sqr(vx - wx) + sqr(vy - wy);
    }

    private static double sqr(double x) {
        return Math.pow(x, 2);
    }

    public static class Speed {

        public static double milesToKilometers(double value){
            return value * 1.609;
        }

        public static double kilometersToMeters(double value){
            return value / 3.6;
        }

        public static double metersToKilometers(double value){
            return value * 3.6;
        }

        public static double kilometersToMiles(double value) {
            return value / 1.609;
        }
    }

    public static class Distance {

        public static double kilometersToMiles(double value){
            return value / 1.609;
        }

        static double kilometersToMeters(double value){
            return value * 1000;
        }

        public static double metersToKilometers(double value){
            return value / 1000;
        }

        public static double metersToMiles(double meters) {
            return meters * 0.000621371;
        }

    }

}

