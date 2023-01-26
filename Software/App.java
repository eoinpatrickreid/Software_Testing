package uk.ac.ed.inf;
import com.mapbox.geojson.*;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;
import java.util.stream.Collectors;

public class App 
{
    public static OrderDetails orderDetails;
    public static ArrayList<Order> ordersList = new ArrayList<>();
    public static ArrayList<Menu> menusList = new ArrayList<>();
    private static int numberOfMoves = 1500;
    public static LongLat appleton = new LongLat(-3.186875, 55.944494);
    public static LongLat end = new LongLat(0,0);


    /**
     * The main method for my project. This method reads in the command line arguments and uses the to create objects of
     * and call on methods in the other classes. Once it has all the data it needs by calling other methods, it then
     * executes the drones movements for each order. If the drone has enough moves left to complete the next order it
     * will do so and if not it will move to appleton and end. For each successful order delivered it adds the info
     * for that order to the deliveries table in the database
     * @param args The list of strings given to the program at command line, representing the date (year, month, day)
     *             and the website port and database port,
     */
    public static void main(String[] args) {
        if (args.length==0){
            System.out.println("Invalid input");
            return;
        }
        String year = String.format("%s-%s-%s", args[2], args[1], args[0]);
        String year2 = args[2];
        String month = args[1];
        String day = args[0];
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Date date;
        try {
            date = formatter.parse(year);
        } catch(Exception e){
            throw new RuntimeException("Uncaught", e);
        }
        String wbPort = args[3];
        String dbPort = args[4];
        String jdbcString = String.format("jdbc:derby://localhost:%s/derbyDB", dbPort);

        orderDetails = new OrderDetails(dbPort);
        Menus menus = new Menus(wbPort);
        menus.getMenus();
        menusList = menus.getMenus();

        Orders orders = new Orders(jdbcString, date, orderDetails, menus);
        ordersList = orders.getOrders();
        ordersList = (ArrayList<Order>) ordersList.stream().sorted(
                Comparator.comparing(Order::getValue).reversed()).collect((Collectors.toList()));

        Words words = new Words(wbPort);

        Movement movement = new Movement(wbPort);
        movement.getLandmarks();
        movement.getFeatures();

        Connection conn = null;
        try {
            conn = DriverManager.getConnection(jdbcString);
        } catch (Exception e) {
            throw new RuntimeException("Uncaught", e);
        }
        createTables(conn);
        LongLat current = appleton;
        LongLat next;
        try {
            PreparedStatement psDeliveries = conn.prepareStatement("insert into deliveries values (?, ?, ?)");

            for (Order order : ordersList){
                if (movement.movesForOrder(current, order, orderDetails, words, menus)+
                        movement.movesToAppleton(words.locationFromWords(order.deliverTo), appleton)< numberOfMoves){
                    List<String> shops = orderDetails.getShops(order.orderNo, menus);
                    for (String shop :shops){
                        next = movement.moveTo(current, words.locationFromWords(shop), conn, order.customer);
                        current = next;
                    }
                    next = movement.moveTo(current, words.locationFromWords(order.deliverTo), conn, order.customer);
                    psDeliveries.setString(1, order.orderNo);
                    psDeliveries.setString(2, order.deliverTo);
                    psDeliveries.setInt(3, menus.getDeliveryCost(orderDetails.getOrderDetails(order.orderNo)));
                    psDeliveries.execute();
                    current = next;
                }
            }
            next = movement.moveTo(current, appleton, conn, "appleton");
        } catch (Exception e) {
            throw new RuntimeException("Uncaught", e);
        }
        try {
            conn.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        output(movement, year2, month, day);
        System.out.println(numberOfMoves);
        System.out.println("Orders for the day complete!");
        end = next;
        System.out.print(end.closeTo(appleton));

    }

    /**
     * The createTables method creates the flightpath and deliveries tables in our database. It first checks tables
     * with those names don't already exist and if they do it deletes them. It then creates the tables holding the
     * variables which we will later populate.
     * @param conn The connection to the database established in main
     */
    public static void createTables(Connection conn){
        DatabaseMetaData databaseMetadata = null;
        ResultSet resultSet = null;
        try {
            Statement statement = conn.createStatement();
            databaseMetadata = conn.getMetaData();
            resultSet = databaseMetadata.getTables(null, null, "FLIGHTPATH", null);
            if (resultSet.next()) {
                statement.execute("drop table flightpath");
            }
            statement.execute("create table flightpath(" + "orderNo char(8),"
                    + "fromLongitude double," + "fromLatitude double," +
                    "angle integer," + "toLongitude double," + "toLatitude double)");
            resultSet = databaseMetadata.getTables(null, null, "DELIVERIES", null);
            if (resultSet.next()) {
                statement.execute("drop table deliveries");
            }
            statement.execute( "create table deliveries(" + "orderNo char(8)," +
                    "deliveredTo varchar(19)," + "costInPence int)");
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    /**
     * The update moves method takes one away from our number of moves variable and is called when the drone
     * has made a move
     */
    public static void updateMoves(){
        numberOfMoves = numberOfMoves - 1;
    }

    /**
     * The output method writes our flightpath to a geo.json file. It does so by taking the list of points we've visited
     * and converting it into a feature collection containing the linestring representing our drones movement. It then
     * writes this to a file.
     * @param movement The Movement object instantiated in main.
     * @param year The year for the date of our orders
     * @param month The month for the date of our orders
     * @param day The day for the date of our orders
     */
    public static void output(Movement movement, String year, String month, String day) {
        String fcc = (FeatureCollection.fromFeature(Feature.fromGeometry(LineString.fromLngLats(movement.points)))).toJson();
            try(FileWriter writer = new FileWriter("drone-"+day+"-"+month+"-"+year+".geojson")) {
                writer.write(fcc);
            }
            catch(IOException e){
                throw new RuntimeException("Uncaught", e);
            }
    }

}
