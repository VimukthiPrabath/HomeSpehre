public class product {
    private String productName;
    private String productDescription;
    private String productPrice;
    private String category;
    private String imageUrl;

    public product() {
    }

    // Constructor to initialize the Product object
    public product(String productName, String productDescription, String productPrice, String category, String imageUrl) {
        this.productName = productName;
        this.productDescription = productDescription;
        this.productPrice = productPrice;
        this.category = category;
        this.imageUrl = imageUrl;
    }

    // Getters and Setters
    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductDescription() {
        return productDescription;
    }

    public void setProductDescription(String productDescription) {
        this.productDescription = productDescription;
    }

    public String getProductPrice() {
        return productPrice;
    }

    public void setProductPrice(String productPrice) {
        this.productPrice = productPrice;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
