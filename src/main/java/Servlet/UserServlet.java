package Servlet;

import Beans.DBConnection;
import Beans.Users;
import Utils.DBUsers;
import jakarta.servlet.annotation.WebServlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

@WebServlet("/User")
public class UserServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    public UserServlet() {
        super();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("text/html; charset=UTF-8");

        // Nếu có tham số "setting" trong URL, chuyển đến trang chỉnh sửa thông tin
        if (req.getParameter("setting") != null) {
            req.getRequestDispatcher("/editProfile.jsp").forward(req, resp);
        } else {
            // Nếu không có, chuyển đến trang hiển thị thông tin người dùng
            req.getRequestDispatcher("/profile.jsp").forward(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/html; charset=UTF-8");

        // Lấy thông tin người dùng từ form
        HttpSession session = request.getSession();
        Users user = (Users) session.getAttribute("user");
        System.out.println("User ID: " + user.getUserId());
        String username = request.getParameter("username");
        String email = request.getParameter("email");
        String phone = request.getParameter("phone");
        String address = request.getParameter("address");

        // Tạo đối tượng user
        Users users = new Users(user.getUserId(), username, user.getPassword(), email, phone, address, user.getRole());

        // Kiểm tra xem action là sửa hay thêm mới
        String action = request.getParameter("action");
        
        try (Connection conn = DBConnection.getConnection()) {
        	if ("update".equals(action)) {
        	    DBUsers.updateUser(conn, users); // Sử dụng đối tượng users đã được tạo từ form
        	    request.getSession().setAttribute("user", users); // Cập nhật session với thông tin mới
        	    response.sendRedirect("profile.jsp");
        	} 

            else {
                response.sendRedirect("error.jsp");
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            response.sendRedirect("error.jsp");
        }
    }

}