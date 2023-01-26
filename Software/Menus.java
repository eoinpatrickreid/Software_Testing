package uk.ac.ed.inf;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;

public class Menus {

    private final String httpString;
    ArrayList<Menu> menuList = new ArrayList<>();
    HashMap<String,Integer> itemPrice = new HashMap<>();
    HashMap<String,String> itemShop = new HashMap<>();
    static ArrayList<String> shops = new ArrayList<>();

    /**
     * The constructor for the Menus class accepts the port that the webserver is running on and formats it into a
     * httpString
     *
     * @param port The port which the webserver is running on.
     */
    public Menus(String port){
        httpString = String.format("http://localhost:%s/menus/menus.json", port);
    }

    /**
     * The getMenus method connects to the webserver and retrieves the data held in the menus folder. This includes the
     * price of each item and the location of each shop. It returns an ArrayList of Menu objects but also updates the
     * itemPrice and itemShop hashmaps allowing us to easily get the price of an item and the address of the shop
     * where an item comes from.
     *
     * @return ArrayList of menus containing objects of the Menu class
     */
    public ArrayList<Menu> getMenus() {
        HttpResponse<String> response;
        HttpRequest request;
        try {
            request = HttpRequest.newBuilder().uri(URI.create(httpString)).build();
            response = Client.client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            throw new RuntimeException("Uncaught", e);
        }
        String menus_json = response.body();
        Type listType = new TypeToken<ArrayList<Menu>>() {
        }.getType();
        menuList = new Gson().fromJson(menus_json, listType);
        for (Menu x : menuList){
            for (Menu.MenuItem y : x.menu){
                itemPrice.put(y.item, y.pence);
                itemShop.put(y.item, x.location);
                shops.add(x.location);
            }
        }
        shops = (ArrayList<String>) shops.stream().distinct().collect(Collectors.toList());
        return menuList;
    }

    /**
     * The getDeliveryCost takes a list of strings representing the items in an order and returns the total cost of the
     * order.
     * @param strings An ArrayList of strings each representing an item from an order.
     * @return      The integer representing the total value of the items passed in including 50p delivery charge.
     */
    public int getDeliveryCost(ArrayList<String> strings) {

        /*we initialise the cost, at 50 as delivery is 50p*/
        int total = 50;
        for (String string :strings){
            total = total + itemPrice.get(string);
        }
        /*finally we return our total.*/
        return total;
    }

    public HashMap<String,String> getItemShop(){
        return itemShop;
    }

}
