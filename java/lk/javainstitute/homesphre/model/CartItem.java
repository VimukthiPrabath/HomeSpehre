package lk.javainstitute.homesphre.model;

import com.google.firebase.firestore.PropertyName;

import java.io.Serializable;

public class CartItem  implements Serializable{
    @PropertyName("product")
    private Product product;
    private int quantity;

    public CartItem(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
    }

    @PropertyName("product")
    public void setProduct(Product product) {
        this.product = product;
    }

    @PropertyName("product")
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public Product getProduct() { return product; }
    public int getQuantity() { return quantity; }

}