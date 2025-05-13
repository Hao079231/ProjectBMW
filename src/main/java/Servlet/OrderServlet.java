package Servlet;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import Beans.Orders;
import Beans.SQLServerConnection;
import Utils.DBOrder;

/**
 * Servlet implementation class OrderServlet
 */
@WebServlet("/Order")
public class OrderServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(OrderServlet.class);

    public OrderServlet() {
        super();
        logger.debug("OrderServlet initialized");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        logger.info("Processing GET request for OrderServlet");
        String searchQuery = request.getParameter("search");
        logger.debug("Search query: {}", searchQuery);

        Connection conn = null;
        try {
            logger.info("Establishing database connection");
            conn = SQLServerConnection.initializeDatabase();
            DBOrder dbOrder = new DBOrder(conn);

            List<Orders> ordersList;

            if (searchQuery != null && !searchQuery.isEmpty()) {
                logger.info("Searching orders by query: {}", searchQuery);
                // Tìm kiếm đơn hàng theo tên khách hàng
                ordersList = dbOrder.searchOrders(searchQuery);
            } else {
                logger.info("Fetching all orders");
                // Hiển thị tất cả các đơn hàng
                ordersList = dbOrder.getAllOrders();
            }

            logger.debug("Retrieved {} orders", ordersList != null ? ordersList.size() : 0);

            // Lưu danh sách đơn hàng vào request
            request.setAttribute("ordersList", ordersList);

            // Chuyển tiếp tới trang JSP để hiển thị kết quả
            logger.info("Forwarding to ordersList.jsp");
            RequestDispatcher dispatcher = request.getRequestDispatcher("/ordersList.jsp");
            dispatcher.forward(request, response);

        } catch (SQLException | ClassNotFoundException e) {
            logger.error("Database error while processing GET request", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Có lỗi xảy ra: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error while processing GET request", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Có lỗi xảy ra: " + e.getMessage());
        } finally {
            // Đảm bảo đóng kết nối cơ sở dữ liệu
            if (conn != null) {
                try {
                    conn.close();
                    logger.debug("Database connection closed");
                } catch (SQLException e) {
                    logger.error("Error closing database connection", e);
                }
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        logger.info("Processing POST request for OrderServlet, delegating to doGet");
        // Chuyển tất cả yêu cầu POST về phương thức GET
        doGet(request, response);
    }
}