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

import Beans.Cart;
import Beans.CartItem;
import Beans.Orders;
import Beans.SQLServerConnection;
import Beans.Users;
import Utils.DBCart;
import Utils.DBOrder;

/**
 * Servlet implementation class PayOrderServlet
 */
@WebServlet("/PayOrder")
public class PayOrderServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Lấy thông tin hành động
        String action = request.getParameter("action");
        if ("submit".equals(action)) {
            handleSubmit(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Yêu cầu không hợp lệ.");
        }
    }

    private void handleSubmit(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();

        // Lấy thông tin người dùng từ session
        Users user = (Users) session.getAttribute("user");
        if (user == null) {
            session.setAttribute("error", "Bạn cần đăng nhập để thực hiện thanh toán.");
            response.sendRedirect("login.jsp");
            return;
        }

        // Lấy giỏ hàng từ session
        Cart cart = (Cart) session.getAttribute("cart");
        if (cart == null || cart.isEmpty()) {
            session.setAttribute("error", "Giỏ hàng của bạn hiện tại trống.");
            response.sendRedirect("cart.jsp");
            return;
        }

        // Lấy địa chỉ giao hàng từ yêu cầu
        String shippingAddress = request.getParameter("shippingAddress");
        if (shippingAddress == null || shippingAddress.trim().isEmpty()) {
            session.setAttribute("error", "Vui lòng nhập địa chỉ giao hàng.");
            response.sendRedirect("checkout.jsp");
            return;
        }

        try {
            // Thêm đơn hàng và chi tiết đơn hàng vào cơ sở dữ liệu
            double totalAmount = DBCart.calculateTotalPrice(cart);
            int orderId = DBCart.addOrder(user.getUserId(), totalAmount, shippingAddress);
            DBCart.addOrderItems(orderId, cart.getItems());

            // Xóa giỏ hàng sau khi đặt hàng thành công
            session.removeAttribute("cart");

            // Chuyển hướng đến trang thông báo thành công
            session.setAttribute("message", "Đặt hàng thành công!");
            response.sendRedirect("CustomerOrder");

        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            session.setAttribute("error", "Đặt hàng không thành công. Vui lòng thử lại.");
            response.sendRedirect("checkout.jsp");
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }
}
