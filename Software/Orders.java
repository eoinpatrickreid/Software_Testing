package uk.ac.ed.inf;
import java.sql.*;
import java.util.ArrayList;
import java.util.Date;

public class Orders {
    private final String jdbcString;
    private final java.sql.Date sqlDate;
    private final Menus localMenus;
    private final OrderDetails localDetails;

    /**
     * The constructor for this class creates local values to be used in the methods below.
     *
     * @param x The jdbc string we will use to connect to the database server.
     * @param date The date specified at the command line, for which we are to complete orders.
     * @param orderDetails The orderDetails object instantiated in the main method in App.java.
     * @param menus The menus object instantiated in the main method in App.java.
     */
    public Orders(String x, Date date, OrderDetails orderDetails , Menus menus){
        jdbcString = x;
        sqlDate = new java.sql.Date(date.getTime());
        localMenus = menus;
        localDetails = orderDetails;

    }

    /**
     * This getOrders method that retrieves all of the orders for the previously specified date, it does so by
     * connecting to the database server and creating Order objects with the data retrieved for each object. It then
     * adds these orders to an ArrayList and returns this list.
     *
     * @return      An ArrayList containing each order for the day specified in the orders constructor above.
     */
    public ArrayList<Order> getOrders() {
        Connection conn;
        final String ordersForDay = "select * from orders where deliveryDate=(?)";
        PreparedStatement psOrdersQuery;
        ResultSet rs;
        try {
            conn = DriverManager.getConnection(jdbcString);
            psOrdersQuery = conn.prepareStatement(ordersForDay);
            psOrdersQuery.setDate(1, sqlDate);
            rs = psOrdersQuery.executeQuery();
            ArrayList<Order> orderList = new ArrayList<>();
            String number;
            Date deliveryDate;
            String customer;
            String deliverTo;
            int totalValue;
            while(rs.next()){
                number = rs.getString("orderNo");
                deliveryDate = rs.getDate("deliveryDate");
                customer = rs.getString("customer");
                deliverTo = rs.getString("deliverTo");
                totalValue = localMenus.getDeliveryCost(localDetails.getOrderDetails(number));
                Order orderA = new Order(number, deliveryDate, customer, deliverTo, totalValue);
                orderList.add(orderA);
            }
            rs.close();
            psOrdersQuery.close();
            conn.close();
            return orderList;
        } catch (Exception e){
            throw new RuntimeException("Uncaught", e);
        }
    }

}
