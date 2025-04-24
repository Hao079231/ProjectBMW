package Servlet;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Connection;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import Beans.Products;
import Beans.DBConnection;
import Utils.DBProduct;

@WebServlet("/Cart")
public class CartServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    public CartServlet() {
        super();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // Lấy kết nối cơ sở dữ liệu với try-with-resources
        try (Connection connection = DBConnection.getConnection()) {
            DBProduct dbProduct = new DBProduct(connection);
            HttpSession session = request.getSession();
            String action = request.getParameter("action");
            String productIdStr = request.getParameter("productId");
            
            String quantityStr = request.getParameter("quantity");

            // Kiểm tra action và productIdStr không phải null
            if (action == null || productIdStr == null) {
                response.sendRedirect("cart.jsp");
                return;
            }

            // Nhận giỏ hàng từ session
            Beans.Cart cart = (Beans.Cart) session.getAttribute("cart");
            if (cart == null) {
                cart = new Beans.Cart(); // Khởi tạo giỏ hàng mới nếu không tồn tại
                session.setAttribute("cart", cart); // Lưu giỏ hàng vào session
            }

            try {
                int productId = Integer.parseInt(productIdStr); // Chuyển đổi productIdStr thành số nguyên
                Products product = dbProduct.getProductById(productId); // Lấy thông tin sản phẩm từ DB

                if (product == null) {
                    // Nếu không tìm thấy sản phẩm, chuyển hướng tới trang lỗi
                    response.sendRedirect("error.jsp");
                    return;
                }

                // Xử lý các hành động dựa trên action
                switch (action) {
                    case "add":
                    	System.out.print("productId " + productIdStr );
                        int addQuantity = (quantityStr != null) ? Integer.parseInt(quantityStr) : 1;
                        cart.addItem(new Beans.CartItem(productId, addQuantity, product));
                        break;

                    case "remove":
                        cart.removeItem(productId);
                        break;

                    case "edit":
                        int updatedQuantity = Integer.parseInt(quantityStr);
                        cart.updateItemQuantity(productId, updatedQuantity);
                        break;
                    default:
                        break;
                }

                // Lưu giỏ hàng đã cập nhật vào session
                session.setAttribute("cart", cart);
            } catch (SQLException e) {
                e.printStackTrace(); 
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }

            // Chuyển hướng đến trang giỏ hàng
            response.sendRedirect("cart.jsp");

        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace(); // Lỗi kết nối DB
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}