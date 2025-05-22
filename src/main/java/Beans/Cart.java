package Beans;

import java.util.HashMap;
import java.util.Map;

public class Cart {
    private int cartID;
    private int userId;
    private int productId;
    private int quantity;
    private String feature;
    private Products product;

    private Map<Integer, CartItem> items;

    public Cart() {
        items = new HashMap<>();
    }

    // Constructor khi có đầy đủ thông tin
    public Cart(int cartID, int userId, int productId, int quantity, String feature) {
        this.cartID = cartID;
        this.userId = userId;
        this.productId = productId; // Lưu product_id từ cơ sở dữ liệu
        this.quantity = quantity;
        this.feature = feature;
    }

    // Constructor khi có thông tin đầy đủ và đối tượng product
    public Cart(int cartID, int userId, int productId, int quantity, String feature, Products product) {
        this.cartID = cartID;
        this.userId = userId;
        this.productId = productId;
        this.quantity = quantity;
        this.feature = feature;
        this.product = product; // Khởi tạo Product
    }

    public int getCartID() {
        return cartID;
    }

    public void setCartID(int cartID) {
        this.cartID = cartID;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getProductId() {
        return productId; // Getter cho productId
    }

    public void setProductId(int productId) {
        this.productId = productId; // Setter cho productId
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getFeature() {
        return feature;
    }

    public void setFeature(String feature) {
        this.feature = feature;
    }

    public Products getProduct() {
        return product; // Getter cho Product
    }

    public void setProduct(Products product) {
        this.product = product; // Setter cho Product
    }

    // Thêm method để lấy giá từ Product
    public double getPrice() {
        return product != null ? product.getPrice() : 0.0; // Lấy giá từ Product
    }
    // Thêm sản phẩm vào giỏ hàng
    public void addItem(CartItem item) {
        if (items.containsKey(item.getProductId())) {
            items.get(item.getProductId()).incrementQuantity();
        } else {
            items.put(item.getProductId(), item);
        }
        System.out.println("Cart items: " + items);
    }

    // Xóa sản phẩm khỏi giỏ hàng
    public void removeItem(int productId) {
        items.remove(productId);
    }

    // Chỉnh sửa số lượng sản phẩm
    public void updateItemQuantity(int productId, int quantity) {
        CartItem item = items.get(productId);
        if (item != null && quantity > 0) {
            item.setQuantity(quantity);
        }
    }
    // Lấy danh sách các sản phẩm trong giỏ hàng
    public Map<Integer, CartItem> getItems() {
        return items;
    }

    // Tính tổng giá trị giỏ hàng
    public double getTotalPrice() {
        double total = 0;
        for (CartItem item : items.values()) {
            if (item.getQuantity() >= 1 && item.getQuantity() <= 100) {
                total += item.getPrice() * item.getQuantity();
            }
        }
        return total;
    }

    // Kiểm tra xem giỏ hàng có rỗng không
    public boolean isEmpty() {
        return items == null || items.isEmpty();
    }
    public void cleanCart() {
        items.entrySet().removeIf(entry -> entry.getValue().getQuantity() < 1 || entry.getValue().getQuantity() > 100);
    }
}