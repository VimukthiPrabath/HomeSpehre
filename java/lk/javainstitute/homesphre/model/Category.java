package lk.javainstitute.homesphre.model;

import java.util.List;

public class Category {

    private String categoryName;
    private List<Product> productList;

    public Category(String categoryName, List<Product> productList) {
        this.categoryName = categoryName;
        this.productList = productList;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public List<Product> getProductList() {
        return productList;
    }
}
