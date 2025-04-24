package Utils;

import Beans.Cart;
import Beans.CartItem;
import Beans.Products;
import Beans.DBConnection;

import java.sql.*;
import java.util.Map;

public class DBCart {

    // Add a product to the cart
    public static void addItemToCart(int userId, int productId, int quantity, String feature) throws SQLException, ClassNotFoundException {
        String checkQuery = "SELECT * FROM Cart WHERE user_id = ? AND product_id = ?";
        String updateQuery = "UPDATE Cart SET quantity = quantity + ? WHERE user_id = ? AND product_id = ?";
        String insertQuery = "INSERT INTO Cart (user_id, product_id, quantity, feature) VALUES (?, ?, ?, ?)";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement checkStmt = connection.prepareStatement(checkQuery)) {

            // Check if the product is already in the cart
            checkStmt.setInt(1, userId);
            checkStmt.setInt(2, productId);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                // If the product exists, update the quantity
                try (PreparedStatement updateStmt = connection.prepareStatement(updateQuery)) {
                    updateStmt.setInt(1, quantity);
                    updateStmt.setInt(2, userId);
                    updateStmt.setInt(3, productId);
                    updateStmt.executeUpdate();
                }
            } else {
                // If not, insert a new cart item
                try (PreparedStatement insertStmt = connection.prepareStatement(insertQuery)) {
                    insertStmt.setInt(1, userId);
                    insertStmt.setInt(2, productId);
                    insertStmt.setInt(3, quantity);
                    insertStmt.setString(4, feature);
                    insertStmt.executeUpdate();
                }
            }
        }
    }

    // Get all the cart items for a specific user
    public static Cart getCartByUserId(int userId) throws SQLException, ClassNotFoundException {
        Cart cart = new Cart();
        String sql = "SELECT c.cartID, c.user_id, c.product_id, c.quantity, c.feature, " +
                     "p.name AS productName, p.price, p.image " +
                     "FROM Cart c JOIN Products p ON c.product_id = p.product_id " +
                     "WHERE c.user_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int cartID = rs.getInt("cartID");
                int productId = rs.getInt("product_id");
                int quantity = rs.getInt("quantity");
                String feature = rs.getString("feature");
                String name = rs.getString("productName");
                int price = rs.getInt("price");
                byte[] image = rs.getBytes("image");
             // Create a Products object for the product
                Products product = new Products(productId, name, null, image, price, null, 0, 0, true);

                // Create a CartItem object and add it to the cart
                CartItem item = new CartItem(productId, quantity, product);
                cart.addItem(item);  // Add item to the cart

            }
        }
        return cart;
    }

    // Remove an item from the cart
    public static void removeItemFromCart(int userId, int productId) throws SQLException, ClassNotFoundException {
        String query = "DELETE FROM Cart WHERE user_id = ? AND product_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(query)) {

            ps.setInt(1, userId);
            ps.setInt(2, productId);
            ps.executeUpdate();
        }
    }

    // Update the quantity of a product in the cart
    public static void updateItemQuantity(int userId, int productId, int quantity) throws SQLException, ClassNotFoundException {
        String query = "UPDATE Cart SET quantity = ? WHERE user_id = ? AND product_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(query)) {

            ps.setInt(1, quantity);
            ps.setInt(2, userId);
            ps.setInt(3, productId);
            ps.executeUpdate();
        }
    }

    // Calculate the total price of the cart
    public static double calculateTotalPrice(Cart cart) {
        double total = 0.0;
        for (CartItem item : cart.getItems().values()) {
            total += item.getPrice() * item.getQuantity();
        }
        return total;
    }
    public static int addOrder(int customerId, double totalAmount, String shippingAddress) throws SQLException, ClassNotFoundException {
        String insertOrderQuery = "INSERT INTO Orders (customer_id, total_amount, shipping_address, payment_status, delivery_status, created_at, updated_at) " +
                                   "VALUES (?, ?, ?, 0, 'confirmed', GETDATE(), GETDATE())";
        int orderId = 0;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement insertStmt = connection.prepareStatement(insertOrderQuery, Statement.RETURN_GENERATED_KEYS)) {

            insertStmt.setInt(1, customerId);
            insertStmt.setDouble(2, totalAmount);
            insertStmt.setString(3, shippingAddress);

            insertStmt.executeUpdate();

            // Lấy ID của đơn hàng vừa được tạo
            ResultSet rs = insertStmt.getGeneratedKeys();
            if (rs.next()) {
                orderId = rs.getInt(1);
            }
            rs.close();
        }
        return orderId;
    }

    // Thêm chi tiết đơn hàng
 // Thêm chi tiết đơn hàng và giảm stock
    public static void addOrderItems(int orderId, Map<Integer, CartItem> cartItems) throws SQLException, ClassNotFoundException {
        String insertOrderItemQuery = "INSERT INTO OrderItems (order_id, product_id, quantity, price) VALUES (?, ?, ?, ?)";
        String updateStockQuery = "UPDATE Products SET stock = stock - ? WHERE product_id = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement insertStmt = connection.prepareStatement(insertOrderItemQuery);
             PreparedStatement updateStockStmt = connection.prepareStatement(updateStockQuery)) {

            for (CartItem item : cartItems.values()) {
                // Thêm chi tiết đơn hàng
                insertStmt.setInt(1, orderId);
                insertStmt.setInt(2, item.getProductId());
                insertStmt.setInt(3, item.getQuantity());
                insertStmt.setDouble(4, item.getPrice() * item.getQuantity());
                insertStmt.addBatch(); // Sử dụng batch để tăng hiệu suất

                // Giảm số lượng tồn kho
                updateStockStmt.setInt(1, item.getQuantity());
                updateStockStmt.setInt(2, item.getProductId());
                updateStockStmt.addBatch(); // Sử dụng batch để tăng hiệu suất
            }

            // Thực thi các lệnh batch
            insertStmt.executeBatch();
            updateStockStmt.executeBatch();
        }
    }

}