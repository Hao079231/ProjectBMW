package Servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

import Beans.OrderItem;
import Beans.Orders;
import Beans.SQLServerConnection;
import Utils.DBOrder;
import Utils.DBOrderDetail;

/**
 * Servlet implementation class OrderDetailServlet
 */
@WebServlet("/OrderDetail")
public class OrderDetailServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    public OrderDetailServlet() {
        super();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Lấy orderId từ tham số request
        String orderIdParam = request.getParameter("orderId");

        if (orderIdParam != null) {
            try {
                // Chuyển đổi orderId thành int
                int orderId = Integer.parseInt(orderIdParam);

                try (Connection conn = SQLServerConnection.initializeDatabase()) {
                    // Khởi tạo đối tượng DBOrder để lấy thông tin đơn hàng
                    DBOrder dbOrder = new DBOrder(conn);
                    Orders order = dbOrder.getOrderById(orderId);  // Lấy thông tin đơn hàng

                    // Nếu tìm thấy đơn hàng, tiếp tục lấy chi tiết
                    if (order != null) {
                        // Lấy thông tin chi tiết đơn hàng
                        DBOrderDetail dbOrderDetail = new DBOrderDetail(conn);
                        List<OrderItem> orderItems = dbOrderDetail.getOrderItems(orderId);
                        
                        if (orderItems != null && !orderItems.isEmpty()) {
                            // Thiết lập các thuộc tính cho JSP
                            request.setAttribute("orderItems", orderItems);
                            request.setAttribute("orderId", orderId);
                            request.setAttribute("customerName", order.getCustomerName());
                            request.setAttribute("totalAmount", order.getTotalAmount());
                            request.setAttribute("paymentStatus", order.isPaymentStatus());
                            request.setAttribute("deliveryStatus", order.getDeliveryStatus());
                            request.setAttribute("createdAt", order.getCreatedAt());
                            request.setAttribute("updatedAt", order.getUpdatedAt());
                        } else {
                            request.setAttribute("error", "Không tìm thấy chi tiết đơn hàng.");
                        }
                    } else {
                        request.setAttribute("error", "Không tìm thấy đơn hàng với ID đã cho.");
                    }

                    // Chuyển hướng đến JSP để hiển thị chi tiết đơn hàng
                    request.getRequestDispatcher("/orderDetail.jsp").forward(request, response);
                } catch (SQLException | ClassNotFoundException e) {
                    e.printStackTrace();
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Lỗi kết nối cơ sở dữ liệu");
                }
            } catch (NumberFormatException e) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "orderId không hợp lệ");
            }
        } else {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Thiếu tham số orderId");
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "POST method not supported for this operation.");
    }
}