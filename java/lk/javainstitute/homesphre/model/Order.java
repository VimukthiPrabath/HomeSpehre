package lk.javainstitute.homesphre.model;

import com.google.firebase.firestore.PropertyName;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class Order implements Serializable {
    private String orderId;
    private String userId;
    private String userName;
    private String userMobile;
    private String userAddress;

    private double deliveryLat;
    private double deliveryLng;

    @PropertyName("items")
    private List<CartItem> items;
    private double totalAmount;
    private Date orderDate;
    private String status;
    private boolean locationSelected;


    public Order() {
    }

    public Order(String orderId, String userId, String userName, String userMobile,
                 String userAddress, List<CartItem> items, double totalAmount) {
        this.orderId = orderId;
        this.userId = userId;
        this.userName = userName;
        this.userMobile = userMobile;
        this.userAddress = userAddress;
        this.items = items;
        this.totalAmount = totalAmount;
        this.orderDate = new Date();
        this.status = "PENDING";
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserMobile() {
        return userMobile;
    }

    public void setUserMobile(String userMobile) {
        this.userMobile = userMobile;
    }

    @PropertyName("items")
    public List<CartItem> getItems() {
        return items;
    }
    @PropertyName("items")
    public void setItems(List<CartItem> items) {
        this.items = items;
    }

    public String getUserAddress() {
        return userAddress;
    }

    public void setUserAddress(String userAddress) {
        this.userAddress = userAddress;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Date getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(Date orderDate) {
        this.orderDate = orderDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }


    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public double getDeliveryLat() { return deliveryLat; }
    public void setDeliveryLat(double deliveryLat) { this.deliveryLat = deliveryLat; }

    public double getDeliveryLng() { return deliveryLng; }
    public void setDeliveryLng(double deliveryLng) { this.deliveryLng = deliveryLng; }

    public boolean isLocationSelected() { return locationSelected; }
    public void setLocationSelected(boolean locationSelected) {
        this.locationSelected = locationSelected;
    }

}