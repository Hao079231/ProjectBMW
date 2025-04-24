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

import Beans.Orders;
import Beans.SQLServerConnection;
import Utils.DBOrder;

/**
 * Servlet implementation class OrderServlet
 */
@WebServlet("/Order")
public class OrderServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    public OrderServlet() {
        super();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String searchQuery = request.getParameter("search");

        Connection conn = null;
        try {
            conn = SQLServerConnection.initializeDatabase();
            DBOrder dbOrder = new DBOrder(conn);

            List<Orders> ordersList;

            if (searchQuery != null && !searchQuery.isEmpty()) {
                // Tìm kiếm đơn hàng theo tên khách hàng
                ordersList = dbOrder.searchOrders(searchQuery);
            } else {
                // Hiển thị tất cả các đơn hàng
                ordersList = dbOrder.getAllOrders();
            }

            // Lưu danh sách đơn hàng vào request
            request.setAttribute("ordersList", ordersList);

            // Chuyển tiếp tới trang JSP để hiển thị kết quả
            RequestDispatcher dispatcher = request.getRequestDispatcher("/ordersList.jsp");
            dispatcher.forward(request, response);

        } catch (Exception e) {
            e.printStackTrace();
            response.getWriter().println("Có lỗi xảy ra: " + e.getMessage());
        } finally {
            // Đảm bảo đóng kết nối cơ sở dữ liệu
            if (conn != null) {
                try {
                    conn.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Chuyển tất cả yêu cầu POST về phương thức GET
        doGet(request, response);
    }
}