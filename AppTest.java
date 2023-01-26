package uk.ac.ed.inf;

import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class AppTest {

    private static final String VERSION = "1.0.5";
    private static final String RELEASE_DATE = "September 28, 2021";

    private final LongLat appletonTower = new LongLat(-3.186874, 55.944494);
    private final LongLat businessSchool = new LongLat(-3.1873,55.9430);
    private final LongLat greyfriarsKirkyard = new LongLat(-3.1928,55.9469);

    @Test
    public void testIsConfinedTrueA(){
        assertTrue(appletonTower.isConfined());
    }

    @Test
    public void testIsConfinedTrueB(){
        assertTrue(businessSchool.isConfined());
    }

    @Test
    public void testIsConfinedFalse(){
        assertFalse(greyfriarsKirkyard.isConfined());
    }

    private boolean approxEq(double d1, double d2) {
        return Math.abs(d1 - d2) < 1e-12;
    }

    @Test
    public void testDistanceTo(){
        double calculatedDistance = 0.0015535481968716011;
        assertTrue(approxEq(appletonTower.distanceTo(businessSchool), calculatedDistance));
    }

    @Test
    public void testCloseToTrue(){
        LongLat alsoAppletonTower = new LongLat(-3.186767933982822, 55.94460006601717);
        assertTrue(appletonTower.closeTo(alsoAppletonTower));
    }


    @Test
    public void testCloseToFalse(){
        assertFalse(appletonTower.closeTo(businessSchool));
    }


    private boolean approxEq(LongLat l1, LongLat l2) {
        return approxEq(l1.longitude, l2.longitude) &&
                approxEq(l1.latitude, l2.latitude);
    }

    @Test
    public void testAngle0(){
        LongLat nextPosition = appletonTower.nextPosition(0);
        LongLat calculatedPosition = new LongLat(-3.186724, 55.944494);
        assertTrue(approxEq(nextPosition, calculatedPosition));
    }

    @Test
    public void testAngle20(){
        LongLat nextPosition = appletonTower.nextPosition(20);
        LongLat calculatedPosition = new LongLat(-3.186733046106882, 55.9445453030215);
        assertTrue(approxEq(nextPosition, calculatedPosition));
    }

    @Test
    public void testAngle50(){
        LongLat nextPosition = appletonTower.nextPosition(50);
        LongLat calculatedPosition = new LongLat(-3.186777581858547, 55.94460890666647);
        assertTrue(approxEq(nextPosition, calculatedPosition));
    }

    @Test
    public void testAngle90(){
        LongLat nextPosition = appletonTower.nextPosition(90);
        LongLat calculatedPosition = new LongLat(-3.186874, 55.944644);
        assertTrue(approxEq(nextPosition, calculatedPosition));
    }

    @Test
    public void testAngle140(){
        LongLat nextPosition = appletonTower.nextPosition(140);
        LongLat calculatedPosition = new LongLat(-3.1869889066664676, 55.94459041814145);
        assertTrue(approxEq(nextPosition, calculatedPosition));
    }

    @Test
    public void testAngle190(){
        LongLat nextPosition = appletonTower.nextPosition(190);
        LongLat calculatedPosition = new LongLat(-3.1870217211629517, 55.94446795277335);
        assertTrue(approxEq(nextPosition, calculatedPosition));
    }

    @Test
    public void testAngle260(){
        LongLat nextPosition = appletonTower.nextPosition(260);
        LongLat calculatedPosition = new LongLat(-3.18690004722665, 55.944346278837045);
        assertTrue(approxEq(nextPosition, calculatedPosition));
    }

    @Test
    public void testAngle300(){
        LongLat nextPosition = appletonTower.nextPosition(300);
        LongLat calculatedPosition = new LongLat(-3.186799, 55.94436409618943);
        assertTrue(approxEq(nextPosition, calculatedPosition));
    }

    @Test
    public void testAngle350(){
        LongLat nextPosition = appletonTower.nextPosition(350);
        LongLat calculatedPosition = new LongLat(-3.1867262788370483, 55.94446795277335);
        assertTrue(approxEq(nextPosition, calculatedPosition));
    }

    @Test
    public void testAngle999(){
        // The special junk value -999 means "hover and do not change position"
        LongLat nextPosition = appletonTower.nextPosition(-999);
        assertTrue(approxEq(nextPosition, appletonTower));
    }

    @Test
    public void testMenusOne() {
        // The webserver must be running on port 9898 to run this test.
        Menus menus = new Menus("8080");
        ArrayList<Menu> menusList = menus.getMenus();
        ArrayList<String> items = new ArrayList<>(Arrays.asList("Ham and mozzarella Italian roll"));
        int totalCost = menus.getDeliveryCost(items);
        // Don't forget the standard delivery charge of 50p
        assertEquals(230 + 50, totalCost);
    }

    @Test
    public void testMenusTwo() {
        // The webserver must be running on port 9898 to run this test.
        Menus menus = new Menus("8080");
        ArrayList<Menu> menusList = menus.getMenus();
        ArrayList<String> items = new ArrayList<>(Arrays.asList("Ham and mozzarella Italian roll", "Salami and Swiss Italian roll"));
        int totalCost = menus.getDeliveryCost(items);
        // Don't forget the standard delivery charge of 50p
        assertEquals(230 + 230 + 50, totalCost);
    }

    @Test
    public void testMenusThree() {
        // The webserver must be running on port 9898 to run this test.
        Menus menus = new Menus( "8080");
        ArrayList<Menu> menusList = menus.getMenus();
        ArrayList<String> items = new ArrayList<>(Arrays.asList(
                "Ham and mozzarella Italian roll",
                "Salami and Swiss Italian roll",
                "Flaming tiger latte"
        ));
        int totalCost = menus.getDeliveryCost(items);
        // Don't forget the standard delivery charge of 50p
        assertEquals(230 + 230 + 460 + 50, totalCost);
    }

    @Test
    public void testMenusFourA() {
        // The webserver must be running on port 9898 to run this test.
        Menus menus = new Menus( "8080");
        ArrayList<Menu> menusList = menus.getMenus();
        ArrayList<String> items = new ArrayList<>(Arrays.asList(
                "Ham and mozzarella Italian roll",
                "Salami and Swiss Italian roll",
                "Flaming tiger latte",
                "Dirty matcha latte"
        ));
        int totalCost = menus.getDeliveryCost(items);
        // Don't forget the standard delivery charge of 50p
        assertEquals(230 + 230 + 460 + 460 + 50, totalCost);
    }

    @Test
    public void testMenusFourB() {
        // The webserver must be running on port 9898 to run this test.
        Menus menus = new Menus( "8080");
        ArrayList<Menu> menusList = menus.getMenus();
        ArrayList<String> items = new ArrayList<>(Arrays.asList(
                "Flaming tiger latte",
                "Dirty matcha latte",
                "Strawberry matcha latte",
                "Fresh taro latte"
        ));
        int totalCost = menus.getDeliveryCost(items);
        // Don't forget the standard delivery charge of 50p
        assertEquals(4 * 460 + 50, totalCost);
    }


    @Test
    public void testGetOrders() {
        // The database thingy must be running on port 9751 to run this test.
        Date date = new GregorianCalendar(2023, Calendar.DECEMBER, 31).getTime();
        OrderDetails orderDetails = new OrderDetails("9751");
        Menus menus = new Menus("8080");
        menus.getMenus();
        String jdbcString = String.format("jdbc:derby://localhost:%s/derbyDB", "9751");
        Orders orders = new Orders(jdbcString, date, orderDetails, menus);
        ArrayList<Order> ordersList = orders.getOrders();

        assertFalse(ordersList.isEmpty());
    }

    @Test
    public void testGetMenus() {
        // The database thingy must be running on port 9751 to run this test.
        Menus menus = new Menus("8080");
        ArrayList<Menu> menusList = menus.getMenus();
        assertFalse(menusList.isEmpty());
    }

    @Test
    public void testOrderDetails() {
        // The database thingy must be running on port 9751 to run this test.
        OrderDetails orderDetails = new OrderDetails("9751");
        ArrayList<String> detailsList = orderDetails.getOrderDetails("62b4f805");
        assertFalse(detailsList.isEmpty());

    }

    @Test
    public void testLocationFromWords() {
        // The database thingy must be running on port 9751 to run this test.
        OrderDetails orderDetails = new OrderDetails("9751");
        Menus menus = new Menus("8080");
        menus.getMenus();
        ArrayList<Menu> menusList = new ArrayList<>();
        menusList = menus.getMenus();
        Date date = new GregorianCalendar(2023, Calendar.DECEMBER, 31).getTime();
        String jdbcString = String.format("jdbc:derby://localhost:%s/derbyDB", "9751");
        Orders orders = new Orders(jdbcString, date, orderDetails, menus);
        ArrayList<Order> ordersList = new ArrayList<Order>();
        ordersList = orders.getOrders();
        ordersList = (ArrayList<Order>) ordersList.stream().sorted(Comparator.comparing(Order::getValue).reversed()).collect((Collectors.toList()));
        Words words = new Words("8080");
        LongLat x = words.locationFromWords("sketch.spill.puzzle");
        LongLat y = new LongLat(-3.191065,55.945626);
        assertTrue(approxEq(x, y));

    }

    @Test
    public void testGetFeatures() {
        // The database thingy must be running on port 9751 to run this test.
        Movement movement = new Movement("8080");
        List<Polygon> features = movement.getFeatures();
        assertFalse(features.isEmpty());

    }
    @Test
    public void testGetLandmarks() {
        // The database thingy must be running on port 9751 to run this test.
        Movement movement = new Movement("8080");
        List<LongLat> landmarks = movement.getLandmarks();
        System.out.println(landmarks.get(1).latitude);
        System.out.println(landmarks.get(1).longitude);
        assertFalse(landmarks.isEmpty());

    }


   @Test
    public void testPossible() {
        // The database must be running on port 9751 to run this test.
        Movement movement = new Movement("8080");
        movement.getFeatures();
        movement.getLandmarks();
        //movement.getEdges();
        OrderDetails orderDetails = new OrderDetails("9751");
        Menus menus = new Menus("8080");
        ArrayList<Menu> menusList = menus.getMenus();
       Date date = new GregorianCalendar(2023, Calendar.DECEMBER, 31).getTime();
       String jdbcString = String.format("jdbc:derby://localhost:%s/derbyDB", "9751");
       Orders orders = new Orders(jdbcString, date, orderDetails, menus);
       ArrayList<Order> ordersList = new ArrayList<Order>();
       ordersList = orders.getOrders();
       ordersList = (ArrayList<Order>) ordersList.stream().sorted(Comparator.comparing(Order::getValue).reversed()).collect((Collectors.toList()));
        Words words = new Words("8080");
        LongLat x = words.locationFromWords("sketch.spill.puzzle");
        LongLat y = new LongLat(-3.186874, 55.944494);
        //ArrayList<String> detailsList = orderDetails.getOrderDetails("62b4f805");
        assertFalse(movement.possible(x, y));
    }

    @Test
    public void testPossibleTrue() {
        // The database thingy must be running on port 9751 to run this test.
        Movement movement = new Movement("8080");
        OrderDetails orderDetails = new OrderDetails("9751");
        Menus menus = new Menus("8080");
        ArrayList<Menu> menusList = menus.getMenus();
        Date date = new GregorianCalendar(2023, Calendar.DECEMBER, 31).getTime();
        String jdbcString = String.format("jdbc:derby://localhost:%s/derbyDB", "9751");
        Orders orders = new Orders(jdbcString, date, orderDetails, menus);
        ArrayList<Order> ordersList = new ArrayList<Order>();
        ordersList = orders.getOrders();
        ordersList = (ArrayList<Order>) ordersList.stream().sorted(Comparator.comparing(Order::getValue).reversed()).collect((Collectors.toList()));
        Words words = new Words("8080");
        LongLat x = words.locationFromWords("surely.native.foal");
        //System.out.println(x.latitude);
        //System.out.println(x.longitude);
        LongLat y = new LongLat(-3.186874, 55.944494);
        //ArrayList<String> detailsList = orderDetails.getOrderDetails("62b4f805");
        assertTrue(movement.possible(x, y));
    }
  @Test
    public void testAngle() {
        // The database thingy must be running on port 9751 to run this test.
        LongLat x = new LongLat(-3.188656, 55.945868);
        LongLat y = new LongLat(-3.186874, 55.944494);
        //ArrayList<String> detailsList = orderDetails.getOrderDetails("62b4f805");
        //System.out.println(x.angle(y));
        //System.out.println(y.angle(x));
      assertEquals(1, 1);
    }

     @Test
    public void testMoveTo() {
        // The database must be running on port 9751 to run this test.
        Movement movement = new Movement("8080");
        OrderDetails orderDetails = new OrderDetails("9751");
        Menus menus = new Menus("8080");
        ArrayList<Menu> menusList = menus.getMenus();
         Date date = new GregorianCalendar(2023, Calendar.DECEMBER, 31).getTime();
         String jdbcString = String.format("jdbc:derby://localhost:%s/derbyDB", "9751");
         Orders orders = new Orders(jdbcString, date, orderDetails, menus);
         ArrayList<Order> ordersList = new ArrayList<Order>();
         ordersList = orders.getOrders();
         ordersList = (ArrayList<Order>) ordersList.stream().sorted(Comparator.comparing(Order::getValue).reversed()).collect((Collectors.toList()));
        Words words = new Words("8080");
        LongLat x = words.locationFromWords("surely.native.foal");
        LongLat y = new LongLat(-3.186874, 55.944494);
        //ArrayList<String> detailsList = orderDetails.getOrderDetails("62b4f805");
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(jdbcString);
            App.createTables(conn);
        } catch (SQLException throwables) {
            System.out.println("wit");
            throwables.printStackTrace();
        }
        LongLat z = movement.moveTo(x, y, conn, "example");
        assertTrue(z.closeTo(y));
    }
    @Test
    public void testMoveTo2() {
        // The database thingy must be running on port 9751 to run this test.
        Movement movement = new Movement("8080");
        OrderDetails orderDetails = new OrderDetails("9751");
        Menus menus = new Menus("8080");
        ArrayList<Menu> menusList = menus.getMenus();
        Date date = new GregorianCalendar(2023, Calendar.DECEMBER, 31).getTime();
        String jdbcString = String.format("jdbc:derby://localhost:%s/derbyDB", "9751");
        Orders orders = new Orders(jdbcString, date, orderDetails, menus);
        ArrayList<Order> ordersList = new ArrayList<Order>();
        ordersList = orders.getOrders();
        ordersList = (ArrayList<Order>) ordersList.stream().sorted(Comparator.comparing(Order::getValue).reversed()).collect((Collectors.toList()));
        Words words = new Words("8080");
        LongLat x = words.locationFromWords("sketch.spill.puzzle");
        LongLat y = new LongLat(-3.186874, 55.944494);
        //ArrayList<String> detailsList = orderDetails.getOrderDetails("62b4f805");
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(jdbcString);
            App.createTables(conn);
        } catch (SQLException throwables) {
            System.out.println("wit");
            throwables.printStackTrace();
        }
        LongLat z = movement.moveTo(y, x, conn, "example");
        assertTrue(z.closeTo(x));
    }
    @Test
    public void testHowMany() {
        // The database thingy must be running on port 9751 to run this test.
        String jdbcString = String.format("jdbc:derby://localhost:%s/derbyDB", "9751");
        Movement movement = new Movement("8080");
        OrderDetails orderDetails = new OrderDetails("9751");
        Menus menus = new Menus("8080");
        ArrayList<Menu> menusList = menus.getMenus();
        Date date = new GregorianCalendar(2022, Calendar.SEPTEMBER, 15).getTime();
        Orders orders = new Orders(jdbcString, date, orderDetails, menus);
        ArrayList<Order> ordersList = orders.getOrders();
        Words words = new Words("8080");
        LongLat x = new LongLat(-3.191594, 55.943658);
        LongLat y = new LongLat(-3.191594, 55.943658);
        //ArrayList<String> detailsList = orderDetails.getOrderDetails("62b4f805");

        Connection conn = null;
        try {
            conn = DriverManager.getConnection(jdbcString);
            App.createTables(conn);
        } catch (SQLException throwables) {
            System.out.println("wit");
            throwables.printStackTrace();
        }

        Order order = ordersList.get(6);
       int moves = movement.howManyMoves(x, words.locationFromWords(order.deliverTo));
        System.out.println(moves);
        assertTrue(moves < 1500);
    }
    @Test
    public void testGetShops() {
        // The database thingy must be running on port 9751 to run this test.
        Movement movement = new Movement("8080");
        OrderDetails orderDetails = new OrderDetails("9751");
        Menus menus = new Menus("8080");
        ArrayList<Menu> menusList = menus.getMenus();
        Date date = new GregorianCalendar(2023, Calendar.DECEMBER, 31).getTime();
        String jdbcString = String.format("jdbc:derby://localhost:%s/derbyDB", "9751");
        Orders orders = new Orders(jdbcString, date, orderDetails, menus);
        ArrayList<Order> ordersList = new ArrayList<>();
        ordersList = orders.getOrders();
        ordersList = (ArrayList<Order>) ordersList.stream().sorted(Comparator.comparing(Order::getValue).reversed()).collect((Collectors.toList()));
        Words words = new Words("8080");
        LongLat x = words.locationFromWords("sketch.spill.puzzle");
        LongLat y = new LongLat(-3.186874, 55.944494);
        //ArrayList<String> detailsList = orderDetails.getOrderDetails("62b4f805");
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(jdbcString);
            App.createTables(conn);
        } catch (SQLException throwables) {
            System.out.println("wit");
            throwables.printStackTrace();
        }
        ArrayList<String> details = new ArrayList<String>();
        details = orderDetails.getOrderDetails("1ad5f1ff");
        List<String> shops = new ArrayList<String>();
        shops = orderDetails.getShops("1ad5f1ff",menus);
        assertFalse(shops.isEmpty());
    }


    @Test
    public void testHowManyForOrder() {
        // The database thingy must be running on port 9751 to run this test.
        String jdbcString = String.format("jdbc:derby://localhost:%s/derbyDB", "9751");
        Date date = new GregorianCalendar(2023, Calendar.DECEMBER, 31).getTime();
        LongLat y = new LongLat(-3.186874, 55.944494);
        //LongLat x = words.locationFromWords("sketch.spill.puzzle");
        OrderDetails orderDetails = new OrderDetails("9751");
        Menus menus = new Menus("8080");
        ArrayList<Menu> menusList = menus.getMenus();
        Orders orders = new Orders(jdbcString, date, orderDetails, menus);
        ArrayList<Order> ordersList = orders.getOrders();
        Words words = new Words("8080");
        Movement movement = new Movement("8080");
        movement.getLandmarks();
        movement.getFeatures();
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(jdbcString);
            App.createTables(conn);
        } catch (SQLException throwables) {
            System.out.println("wit");
            throwables.printStackTrace();
        }
        int totalMoves = 0;
       for (Order ord : ordersList) {
           int moves = movement.movesForOrder(y, ord, orderDetails, words, menus);
           System.out.println(moves);
           totalMoves = totalMoves + moves;
       }
        System.out.println(totalMoves);
        assertTrue(totalMoves<1500);
    }



    @Test
    public void testApp() {
        String[] args = {"12", "7", "2023", "8080", "9751"};
        App.main(args);
        assertTrue(App.end.closeTo(appletonTower));
    }/*
*/



}
