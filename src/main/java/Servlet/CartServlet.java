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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WebServlet("/Cart")
public class CartServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(CartServlet.class);

    public CartServlet() {
        super();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        String remoteIp = request.getRemoteAddr();
        logger.info("GET request received from IP: {}, action: {}", remoteIp, request.getParameter("action"));

        // Lấy kết nối cơ sở dữ liệu với try-with-resources
        try (Connection connection = DBConnection.getConnection()) {
            DBProduct dbProduct = new DBProduct(connection);
            HttpSession session = request.getSession();
            String action = request.getParameter("action");
            String productIdStr = request.getParameter("productId");
            String quantityStr = request.getParameter("quantity");

            // Kiểm tra action và productIdStr không phải null
            if (action == null || productIdStr == null) {
                logger.warn("Invalid request from IP: {}, action: {}, productId: {}", remoteIp, action, productIdStr);
                response.sendRedirect("cart.jsp");
                return;
            }

            // Nhận giỏ hàng từ session
            Beans.Cart cart = (Beans.Cart) session.getAttribute("cart");
            if (cart == null) {
                cart = new Beans.Cart();
                session.setAttribute("cart", cart);
                logger.info("New cart created for session: {}, IP: {}", session.getId(), remoteIp);
            }

            try {
                int productId = Integer.parseInt(productIdStr);
                Products product = dbProduct.getProductById(productId);

                if (product == null) {
                    logger.warn("Product not found for ID: {}, IP: {}", productId, remoteIp);
                    response.sendRedirect("error.jsp");
                    return;
                }

                // Xử lý các hành động dựa trên action
                switch (action) {
                    case "add":
                        int addQuantity = (quantityStr != null) ? Integer.parseInt(quantityStr) : 1;
                        cart.addItem(new Beans.CartItem(productId, addQuantity, product));
                        logger.info("Added product ID: {} with quantity: {} to cart, IP: {}", productId, addQuantity, remoteIp);
                        break;

                    case "remove":
                        cart.removeItem(productId);
                        logger.info("Removed product ID: {} from cart, IP: {}", productId, remoteIp);
                        break;

                    case "edit":
                        int updatedQuantity = Integer.parseInt(quantityStr);
                        cart.updateItemQuantity(productId, updatedQuantity);
                        logger.info("Updated product ID: {} to quantity: {} in cart, IP: {}", productId, updatedQuantity, remoteIp);
                        break;
                    default:
                        logger.warn("Unknown action: {} from IP: {}", action, remoteIp);
                        break;
                }

                // Lưu giỏ hàng đã cập nhật vào session
                session.setAttribute("cart", cart);
            } catch (SQLException e) {
                logger.error("Database error while processing cart action: {}, productId: {}, IP: {}, error: {}",
                    action, productIdStr, remoteIp, e.getMessage());
                response.sendRedirect("error.jsp");
            } catch (NumberFormatException e) {
                logger.error("Invalid number format for productId: {} or quantity: {}, IP: {}, error: {}",
                    productIdStr, quantityStr, remoteIp, e.getMessage());
                response.sendRedirect("error.jsp");
            }

            // Chuyển hướng đến trang giỏ hàng
            response.sendRedirect("cart.jsp");

        } catch (SQLException | ClassNotFoundException e) {
            logger.error("Failed to connect to database from IP: {}, error: {}", remoteIp, e.getMessage());
            response.sendRedirect("error.jsp");
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        String remoteIp = request.getRemoteAddr();
        logger.info("POST request received from IP: {}, action: {}", remoteIp, request.getParameter("action"));
        doGet(request, response);
    }
}