package uk.ac.ed.inf;
import com.mapbox.geojson.*;
import java.awt.geom.Line2D;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;

public class Movement {
    private final List<Polygon> noGos = new ArrayList<>();
    private final List<LongLat> landmarks = new ArrayList<>();
    private final String port;
    public List<Point> points = new ArrayList<>();



    /**
     * The constructor for the Movement class, this receives the port used to connect to the webserver
     *
     * @param p The port which the webserver is running on which will be used to connect to the webserver.
     */
    public Movement(String p){
        port = p;
    }


    /**
     *  The moveTo method takes in a start location and a desired end location and moves to that location step by step.
     *  If it is possible to move straight to the location (ie there are no no fly zones in the way) then the algorithm
     *  recursively calculates the angle from the current location to the desired location and takes a step towards it,
     *  doing so until it is regarded as close to the location. If however it is not possible to move directly to the
     *  desired location then the algorithm will check if its possible to move to one of the landmarks, once it finds a
     *  landmark which it is possible to move to AND it is possible to move to the next desired location from that
     *  landmark, then it recursively calls moveTo to the landmark and then to the next location. It returns the
     *  longlat of the location it ends up in.
     *
     * @param current The starting longlat location of the drone
     * @param next The desired next longlat location of the drone
     * @param conn The connection to the database server created in App.java main
     * @param orderNo The string orderNo for the order currently being completed ('Appleton' if were going to appleton)
     * @return The longlat location which we end up at.
     */
    public LongLat moveTo(LongLat current, LongLat next, Connection conn, String orderNo){
        int angle;
        try {
            PreparedStatement psFlightpath = conn.prepareStatement("insert into flightpath values (?, ?, ?, ?, ?, ?)");

                if (possible(current, next)){
                    while (!current.closeTo(next)){
                        angle = current.angle(next);
                        psFlightpath.setString(1, orderNo);
                        psFlightpath.setDouble(2, current.longitude);
                        psFlightpath.setDouble(3, current.latitude);
                        psFlightpath.setInt(4, angle);
                        points.add(Point.fromLngLat(current.longitude, current.latitude));
                        current = current.nextPosition(angle);
                        psFlightpath.setDouble(5, current.longitude);
                        psFlightpath.setDouble(6, current.latitude);
                        psFlightpath.execute();
                        App.updateMoves();
                    }
                } else {
                    boolean done;
                    done = false;
                    for (LongLat y : landmarks){
                        if (possible(current, y) & possible(y, next) & (!done)){
                            current = moveTo(current, y, conn, orderNo);
                            current = moveTo(current, next, conn, orderNo);
                            done = true;
                        }
                    }


                }
                //the hover once we have moved to the location:
            angle = -999;
            psFlightpath.setString(1, orderNo);
            psFlightpath.setDouble(2, current.longitude);
            psFlightpath.setDouble(3, current.latitude);
            psFlightpath.setInt(4, angle);
            psFlightpath.setDouble(5, current.longitude);
            psFlightpath.setDouble(6, current.latitude);
            psFlightpath.execute();
            App.updateMoves();
        } catch (Exception e) {
            throw new RuntimeException("Uncaught", e);
        }
        return current;
    }


    /**
     * The possible method checks if it is possible to move straight from one location to another, if there are no-go
     * zones in the way then the method will return false, but if there are no obstacles in the way it will return True.
     * It does this by performing the line-line intersection problem; constructing each edge of each no go zone and
     * checking to see if a straight line from current to next intersects with them.
     *
     * @param current The starting longlat location
     * @param next The desired end location
     * @return     A boolean with value true if its possible to go from current to next and false if it's not.
     */
    public boolean possible(LongLat current, LongLat next){
        Line2D line = new Line2D.Double(current.longitude, current.latitude, next.longitude, next.latitude);
        for (Polygon poly: noGos){
            for (int x = 0;x < poly.coordinates().get(0).size()-1; x++){
                Line2D edge = new Line2D.Double(poly.coordinates().get(0).get(x).coordinates().get(0),
                        poly.coordinates().get(0).get(x).coordinates().get(1),
                        poly.coordinates().get(0).get(x+1).coordinates().get(0),
                        poly.coordinates().get(0).get(x+1).coordinates().get(1));
                if (line.intersectsLine(edge)) {
                    return false;
                }
            }
        }
        return true;
    }


    /**
     * This method retrieves the polygons representing the no-fly zones from the webserver. It connects to the webserver
     * retrieves the json string and convert this into a list of features, before adding each as a polygon to a list.
     *
     * @return It returns the list of polygons representing each no-fly zone
     */
    public List<Polygon> getFeatures(){
        String httpString3 = String.format("http://localhost:%s/buildings/no-fly-zones.geojson", port);
        HttpResponse<String> response;
        HttpRequest request;
        try {
            request = HttpRequest.newBuilder().uri(URI.create(httpString3)).build();
            response = Client.client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            throw new RuntimeException("Uncaught", e);
        }
        String noGoJson = response.body();

        FeatureCollection fc = FeatureCollection.fromJson(noGoJson);
        List<Feature> features;
        features = fc.features();
        if (features != null) {
            for (Feature x : features){
                noGos.add((Polygon)x.geometry());
            }
        }
        return noGos;
    }

    /**
     * The howManyMoves method calculates how many moves would be required in order to travel from one location to the
     * next. It does so by incrementally moving closer to the desired end location while keeping track of how many moves
     * it has taken
     *
     * @param from  The longlat location we are starting from
     * @param to The longlat location we want to end up at
     * @return The count of how many moves it would take to get from 'from' to 'to'
     */
    public int howManyMoves(LongLat from, LongLat to){
        LongLat x = from;
        int moves = 0;
        while(!x.closeTo(to)){
            int angle = x.angle(to);
            x = x.nextPosition(angle);
            moves = moves + 1;
        }
        moves = moves+1;
        return moves;
    }

    /**
     * The moves to appleton method returns the number of moves that would be required to move from a given location to
     * appleton. If it's possible to move directly to appleton it just calls the above howManyMoves method from the
     * location to appleton. If its not possible it checks if its possible to move to each landmark and if it is then
     * it calles howManyMoves to the landmark and then from the landmark to appleton.
     *
     * @param x The longlat location we are to move to appleton from.
     * @param y The longlat location of appleton.
     * @return An integer value of how many moves would be required to move from x to appleton
     */
    public int movesToAppleton(LongLat x, LongLat y){
        int total = 0;
            if (possible(x, y)){
                total = total + howManyMoves(x, y);
            } else {
               for (LongLat land : landmarks){
                    if (possible(land, y)){
                        total = total + howManyMoves(x, land) + howManyMoves(land, y);
                        break;
                    }
                }


            }
        return total;
    }


    /**
     * The movesForOrder method calculates how many moves would be required to complete an entire order. It first
     * retrieves the locations for the shops that the order will go to by calling getShops on the given order. It then
     * calculates the number of moves required to move to each shop in the same fashion as above, a simple howManyMoves
     * call if its possible to move straight to the shop from the current location and if not it calls howManyMoves first
     * to a landmark and then to the shop. It then does the same for the last shop to the deliverTo address and returns
     * the total.
     *
     * @param x The starting/current longlat location for the order.
     * @param order The Order object for the current order
     * @param orderDetails The orderDetails object instantiated in main in App.java
     * @param words  The words object instantiated in main in App.java
     * @param menus The menus object instantiated in main in App.java
     * @return An integer representing how many moves it would take to complete the whole order.
     */
    public int movesForOrder(LongLat x, Order order, OrderDetails orderDetails, Words words, Menus menus){
        LongLat current = x;
        int total = 0;
        List<String> shops;
        shops = orderDetails.getShops(order.orderNo, menus);
        for (String shop:shops){
            LongLat shopLocation = words.locationFromWords(shop);
            if (possible(current, shopLocation)){
                total = total + howManyMoves(current, shopLocation);
                current = shopLocation;
            } else {
                for (LongLat y : landmarks){
                    if (possible(current, y) & possible(y, shopLocation)){
                       total = total + howManyMoves(current, y) + howManyMoves(y, shopLocation);
                       current = shopLocation;
                       break;
                    }
                }
            }
        }
        LongLat deliveryAddress = words.locationFromWords(order.deliverTo);
        if (possible(current, deliveryAddress)){
            total = total + howManyMoves(current, deliveryAddress)+1;
        } else {
            for (LongLat y : landmarks){
                if (possible(current, y) & possible(y, deliveryAddress)){
                    total = total + howManyMoves(current, y) + howManyMoves(y, deliveryAddress);
                    current = deliveryAddress;
                    break;
                }
            }
        }
        return total;
    }

    /**
     * The getLandmark method uses the port variable instantiated in the constructor for Movement to connect to the
     * web server and retrieve the coordinates of the landmarks stored on the webserver.
     *
     * @return It returns a list containing LongLat objects representing the landmarks for the task
     */
    public List<LongLat> getLandmarks(){
        String httpString3 = String.format("http://localhost:%s/buildings/landmarks.geojson", port);
        HttpResponse<String> response = null;
        HttpRequest request;
        try {
            request = HttpRequest.newBuilder().uri(URI.create(httpString3)).build();
            response = Client.client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            throw new RuntimeException("Uncaught", e);
        }
        String noGoJson = response.body();
        FeatureCollection fc = FeatureCollection.fromJson(noGoJson);
        List<Feature> features = new ArrayList<>();
        features = fc.features();
        if (features != null) {
            for (Feature x : features){
                Geometry g = x.geometry();
                Point p = (Point)g;
                assert p != null;
                double xCo = p.coordinates().get(0);
                double yCo = p.coordinates().get(1);
                LongLat longlat = new LongLat(xCo, yCo);
                landmarks.add(longlat);
            }
        }
        return landmarks;
    }
}
