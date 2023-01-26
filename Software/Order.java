package uk.ac.ed.inf;
import java.util.Date;


public class Order {
    String orderNo;
    Date date;
    String customer;
    String deliverTo;
    int value;
    public Order(String number, Date orderDate, String orderCustomer, String deliveryAd, int valueTotal){
        orderNo = number;
        date = orderDate;
        customer = orderCustomer;
        deliverTo = deliveryAd;
        value = valueTotal;
    }
    public int getValue(){
        return value;
    }
}
