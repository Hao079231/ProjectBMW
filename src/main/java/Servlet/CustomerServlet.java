package Servlet;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.util.List;

import Beans.DBConnection;
import Beans.SQLServerConnection;
import Beans.Users;
import Utils.DBUsers;

/**
 * Servlet implementation class CustomerServlet
 */
@WebServlet("/Customer")
public class CustomerServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    public CustomerServlet() {
        super();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String searchQuery = request.getParameter("search");

        Connection conn = null;

        try {
            conn = DBConnection.getConnection();
            DBUsers dbUsers = new DBUsers();

            List<Users> usersList = null;

            if (searchQuery != null && !searchQuery.isEmpty()) {
                // Tìm kiếm người dùng theo tên
                usersList = dbUsers.searchUsersByUsername(conn, searchQuery);
            } else {
                // Lấy tất cả người dùng
                usersList = dbUsers.getAllUsers(conn);
            }

            // Lưu danh sách vào request để chuyển tiếp tới trang JSP
            request.setAttribute("usersList", usersList);

            // Chuyển tiếp tới trang JSP để hiển thị kết quả
            RequestDispatcher dispatcher = request.getRequestDispatcher("/customerList.jsp");
            dispatcher.forward(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            response.getWriter().println("Có lỗi xảy ra: " + e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Không có xử lý POST trong trường hợp này
        doGet(request, response);
    }
}