package Servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import Beans.OrderItem;
import Beans.Orders;
import Beans.SQLServerConnection;
import Beans.Users;
import Utils.DBOrder;
import Utils.DBOrderDetail;

/**
 * Servlet implementation class CustomerOrderServlet
 */
@WebServlet("/CustomerOrder")
public class CustomerOrderServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String orderIdParam = request.getParameter("order_id");

        // Lấy thông tin user từ session
        HttpSession session = request.getSession();
        Users user = (Users) session.getAttribute("user"); // Lấy đối tượng người dùng từ session
        int customerId = user.getUserId();  // Lấy userId từ đối tượng người dùng

        try (Connection conn = SQLServerConnection.initializeDatabase()) {
            if (orderIdParam != null) {
                // Nếu có order_id, lấy chi tiết đơn hàng
                int orderId = Integer.parseInt(orderIdParam);
                DBOrderDetail dbOrderDetail = new DBOrderDetail(conn);
                List<OrderItem> orderItems = dbOrderDetail.getOrderItems(orderId); // Lấy chi tiết đơn hàng

                request.setAttribute("orderItems", orderItems);
                request.getRequestDispatcher("/customerOrderDetail.jsp").forward(request, response); // Chuyển đến trang chi tiết đơn hàng
            } else {
                // Nếu không có order_id, lấy danh sách đơn hàng của khách hàng
                DBOrder dbOrder = new DBOrder(conn);
                List<Orders> ordersList = dbOrder.getOrdersByCustomerId(customerId); // Lấy danh sách đơn hàng của khách hàng

                request.setAttribute("ordersList", ordersList);
                request.getRequestDispatcher("/customerOrders.jsp").forward(request, response); // Chuyển đến trang danh sách đơn hàng
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error occurred.");
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String orderIdParam = request.getParameter("orderId"); // Sửa "order_id" thành "orderId"
        System.out.print("orderId " + orderIdParam);
        if (orderIdParam != null) {
            try (Connection conn = SQLServerConnection.initializeDatabase()) {
                int orderId = Integer.parseInt(orderIdParam);

                // Cập nhật trạng thái thanh toán của đơn hàng
                DBOrder dbOrder = new DBOrder(conn);
                boolean updated = dbOrder.markOrderAsPaid(orderId);

                if (updated) {
                    // Thành công, chuyển đến trang danh sách đơn hàng
                    response.sendRedirect("CustomerOrder");
                } else {
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to update payment status.");
                }
            } catch (SQLException | ClassNotFoundException e) {
                e.printStackTrace();
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error occurred.");
            }
        } else {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing order ID.");
        }
    }

}