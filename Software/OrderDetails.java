package uk.ac.ed.inf;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class OrderDetails {
    private final String jdbcStringO;

    /**
     * The constructor for this class receives the port which we are to connect to the database on, it then formats
     * this into the jdbc string well use to connect
     *
     * @param dport The port that the database is running on
     */
    public OrderDetails(String dport){
        jdbcStringO = String.format("jdbc:derby://localhost:%s/derbyDB", dport);
    }

    /**
     * The getOrderDetail method returns the items in a given order. It receives a string representing the orderNumber for a specific order, it then queries
     * the database table orderDetails in order to receive the items present in that order. It then returns an ArrayList
     * containing the items in the order.
     *
     * @param number The string for the orderNumber for an order.
     * @return      An ArrayList containing the items in the given order.
     */
    public ArrayList<String> getOrderDetails(String number){
        Connection conne;
        final String orderDetailsQuery = "select * from orderDetails where orderNo=(?)";
        PreparedStatement psOrdersQueryO;
        ResultSet rso;
        ArrayList<String> orderDetails = new ArrayList<>();
        try {
            conne = DriverManager.getConnection(jdbcStringO);
            psOrdersQueryO = conne.prepareStatement(orderDetailsQuery);
            psOrdersQueryO.setString(1, number);
            rso = psOrdersQueryO.executeQuery();
            String orderNo;
            String item;
            while(rso.next()){
                orderNo = rso.getString("orderNo");
                item = rso.getString("item");

                orderDetails.add(item);
            }
            rso.close();
            psOrdersQueryO.close();
            conne.close();
            return orderDetails;
        } catch (Exception e){
            throw new RuntimeException("Uncaught", e);
        }
    }

    /**
     * The getShops method returns the list of shops visited in a specific order. It takes an orderNumber and gets
     * the list of items for that order before searching the itemshop hash map for each item to get the address of the
     * shops it will visit. It then returns a list of the adresses of the shops.
     *
     * @param orderNo A string for the orderNumber of a given order
     * @param menus The Menus object menus instantiated in App.java main
     * @return      It returns the list of what3words addresses that that order should visit.
     */
    public List<String> getShops(String orderNo, Menus menus){
        List<String> shops = new ArrayList<>();
        ArrayList<String> details = new ArrayList<>();
        details = getOrderDetails(orderNo);
        for (String item : details){
            HashMap<String,String> itemShopHash = new HashMap<>();
            itemShopHash = menus.getItemShop();
            shops.add(itemShopHash.get(item));
        }
        return shops.stream().distinct().collect(Collectors.toList());
    }
}
