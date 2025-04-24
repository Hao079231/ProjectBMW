package Servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import Beans.OrderItem;
import Beans.Review;
import Beans.SQLServerConnection;
import Beans.Users;
import Utils.BDReview;
import Utils.DBOrderDetail;

/**
 * Servlet implementation class ReviewServlet
 */
@WebServlet("/Review")
public class ReviewServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        if (action == null) {
            action = "check";  // Mặc định là "check"
        }

        switch (action) {
            case "check":
                // Kiểm tra xem sản phẩm đã được đánh giá chưa và chuyển hướng
                handleCheck(request, response);
                break;
            case "submit":
                // Xử lý form đánh giá
                handleSubmit(request, response);
                break;
            default:
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Yêu cầu không hợp lệ");
                break;
        }
    }

    private void handleCheck(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        Users user = (Users) session.getAttribute("user");
        System.out.println("User ID: " + user.getUserId());
        // Lấy thông tin productId và orderId từ request
        int productId = Integer.parseInt(request.getParameter("productId"));
        int orderId = Integer.parseInt(request.getParameter("orderId"));
        System.out.println("Product ID: " + productId);
        System.out.println("order ID: " + orderId);
        // Tạo kết nối và BDReview
        try (Connection conn = SQLServerConnection.initializeDatabase()) {
            BDReview bdReview = new BDReview(conn);
            if (!bdReview.isProductPaid(productId, user.getUserId(), orderId)) {
                request.setAttribute("error", "Sản phẩm này chưa được thanh toán, không thể đánh giá.");
                DBOrderDetail dbOrderDetail = new DBOrderDetail(conn);
                List<OrderItem> orderItems = dbOrderDetail.getOrderItems(orderId);
                request.setAttribute("orderItems", orderItems);
                request.getRequestDispatcher("customerOrderDetail.jsp").forward(request, response);
                return;
            }
            if (bdReview.hasUserReviewedProduct(productId, user.getUserId(), orderId)) {
                // Sản phẩm đã được đánh giá
                request.setAttribute("error", "Sản phẩm này đã được đánh giá.");
                DBOrderDetail dbOrderDetail = new DBOrderDetail(conn);
                List<OrderItem> orderItems = dbOrderDetail.getOrderItems(orderId);
                request.setAttribute("orderItems", orderItems);
                request.getRequestDispatcher("customerOrderDetail.jsp").forward(request, response);
            } else {
                // Chưa đánh giá, chuyển sang trang Review.jsp
                request.setAttribute("productId", productId);
                request.setAttribute("orderId", orderId);
                request.getRequestDispatcher("Review.jsp").forward(request, response);
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Lỗi kết nối cơ sở dữ liệu.");
        }
    }

    private void handleSubmit(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        Users user = (Users) session.getAttribute("user");
        
        // Lấy thông tin từ form đánh giá
        int rating = Integer.parseInt(request.getParameter("rating"));
        String comment = request.getParameter("comment");
        int productId = Integer.parseInt(request.getParameter("productId"));
        int orderId = Integer.parseInt(request.getParameter("orderId"));
        System.out.println("Product ID input: " + productId);
        System.out.println("order ID: " + orderId);
        // Tạo kết nối và BDReview
        try (Connection conn = SQLServerConnection.initializeDatabase()) {
            BDReview bdReview = new BDReview(conn);

            // Tạo Review mới và thêm vào cơ sở dữ liệu
            Review newReview = new Review(productId, user.getUserId(), orderId, rating, comment);
            boolean success = bdReview.addReview(newReview);

            if (success) {
                // Gửi thông báo thành công và quay lại chi tiết đơn hàng
                request.setAttribute("message", "Đánh giá đã được thêm thành công.");
            } else {
                // Gửi thông báo lỗi và quay lại chi tiết đơn hàng
                request.setAttribute("error", "Không thể thêm đánh giá. Vui lòng thử lại.");
            }

            // Lấy danh sách OrderItem của đơn hàng và gửi lại vào request
            DBOrderDetail dbOrderDetail = new DBOrderDetail(conn);
            List<OrderItem> orderItems = dbOrderDetail.getOrderItems(orderId);
            request.setAttribute("orderItems", orderItems);

            // Quay lại trang chi tiết đơn hàng (customerOrderDetail.jsp)
            request.getRequestDispatcher("customerOrderDetail.jsp").forward(request, response);
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Lỗi kết nối cơ sở dữ liệu.");
        }
    }
}