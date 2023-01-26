package uk.ac.ed.inf;

public class LongLat {
    public double longitude;
    public double latitude;

    /**
     * Constructs the longlat storing longitude and latitude
     * @param longi longitude for given coordinate
     * @param lat latitude for given coordinate
     */
    public LongLat(double longi, double lat){
        longitude = longi;
        latitude = lat;
    }

    /**
     * Returns true if the longlat is within the confinement area and false if it is not
     * @return boolean representing whether or not its in the confinement area
     */
    public boolean isConfined(){
        /*Returns true if the given longitude and latitude fall within the confinement area*/
        if(latitude > 55.942617 && latitude < 55.946233 && longitude > -3.192473 && longitude < -3.184319){
            return true;
        }
        /*Returns false otherwise*/
        return false;
    }

    /**
     * calculates the distance from the longlat object to another longlat object and returns it
     * @param x the LongLat object we want to know the distance to
     * @return The double value representing the distance
     */
    public double distanceTo(LongLat x){
        /* calculates the Pythagorean distance between the two points following the formula
        The square root is taken of the sum of (x1-x2)^2 and (y1-y2)^2 */
        double dist = Math.sqrt((Math.pow((longitude-x.longitude), 2)) + (Math.pow((latitude-x.latitude), 2)));
        /* The double value this gives back is then returned */
        return dist;
    }

    /**
     * The closeTo method checks whether the lonlat object is considered close to another longlat object
     * it returns true if it is and false otherwise
     * @param x The longlat of the point we want to see if the referenced longlat is close to
     * @return Returns boolean, true if close to false if not
     */
    public boolean closeTo(LongLat x){
        /*if the distance between the two points is less than what was defined as 'close to' (0.00015)
        we return true */
        if(distanceTo(x) < 0.00015){
            return true;
        }
        /*otherwise the points are too far apart to be called 'close' and so we return false*/
        return false;
    }

    /**
     * The angle method calculates the angle (rounded to the nearest 10) from the current longlat object to another.
     * @param x the longlat object we want to calculate the angle to
     * @return the angle fom the referenced longlat to x
     */
    public int angle(LongLat x){
        //longitude = lng1, latitude = lat1, x.longitude = lng2, x.latitude = lat2
        double T = 6.2831853071795865;
        double R = 57.2957795130823209;

        double theta = Math.atan2(x.longitude - longitude, x.latitude-latitude);

        if (theta < 0.0) {
            theta += T;
        }

        theta = R*theta;

        if (x.latitude == latitude) {
            if (x.longitude>longitude) {
                theta = 0;
            } else {
                theta = 180;
            }
        } else if (x.longitude == longitude) {
            if (x.latitude>latitude) {
                theta = 90;
            } else {
                theta = 270;
            }
        } else if (x.latitude>latitude && x.longitude>longitude) {
            theta = 90 - theta;
        } else if (x.latitude<latitude && x.longitude<longitude) {
            theta = 180+(270 - theta);
        } else if (x.latitude>latitude && x.longitude<longitude) {
            theta = 90+(360-theta);
        } else if (x.latitude<latitude && x.longitude>longitude) {
            theta = 270+(180 - theta);
        }
        return (int) Math.round(theta/10.0) * 10;
    }

   // public boolean inNoGo(LongLat x){
     //   return true;
    //}

    /**
     * The nextPositon method calculates the longlat of the next position, given an angle it makes a move of 0.00015
     * degrees in that direction and returns the new longlat
     * @param angle The angle of the direction we want to move in
     * @return The new longlat we have moved to
     */
    public LongLat nextPosition(int angle){
        /* The first four if and else if statements calculate the next position if
           the angle that the drone is going to head in is straight north, south, east or west.
           In these cases the new position is simply the 0.00015 degree movement in that direction.
            East: */
        if (angle == 0 || angle == 360) {
            LongLat x = new LongLat((longitude + 0.00015), latitude);
            return x;
        } /* North: */
        else if (angle == 90) {
            LongLat x = new LongLat(longitude, (latitude+0.00015));
            return x;
        } /* West: */
        else if (angle == 180) {
            LongLat x = new LongLat((longitude - 0.00015), latitude);
            return x;
        } /* South: */
        else if (angle == 270) {
            LongLat x = new LongLat(longitude, (latitude-0.00015));
            return x;
        } /* if the angle isn't one of the above then we have to calculate how much to change the
        latitude and longitude by*/
        else if (angle>0 && angle<360){
            /* This is done by pythagoras theorem, with the hypotenuse as 0.00015 we
             calculate the opposite side of the right angled triangle to tell us how much to change
             the latitude by and  the adjacent side to tell us how much to change the longitude*/
            double latchange = 0.00015*(Math.sin(Math.toRadians(angle)));
            double lngchange = 0.00015*(Math.cos(Math.toRadians(angle)));
            /* We then create a new LongLat object reflecting the change in lat and long and then return it*/
            LongLat x = new LongLat((longitude + (lngchange)),(latitude+ (latchange)));
            return x;
        } /* If the angle we receive is -999 then we know that the drone is hovering and so
             we don't need to change longitude or latitude so we create and return a LongLat
             object with the same longitude and latitude*/
        else if (angle==-999){
            LongLat x = new LongLat(longitude, latitude);
            return x;
        }
        /* We return a LongLat object with the same long and lat if the other if statements haven't been met*/
    LongLat x = new LongLat(longitude, latitude);
        return x;
    }


}