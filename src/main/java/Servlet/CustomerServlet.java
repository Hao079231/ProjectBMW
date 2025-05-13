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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import Beans.DBConnection;
import Beans.Users;
import Utils.DBUsers;

/**
 * Servlet implementation class CustomerServlet
 */
@WebServlet("/Customer")
public class CustomerServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(CustomerServlet.class);

    public CustomerServlet() {
        super();
        logger.debug("CustomerServlet initialized");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        logger.info("Processing GET request for CustomerServlet");
        String searchQuery = request.getParameter("search");
        logger.debug("Search query: {}", searchQuery);

        Connection conn = null;

        try {
            logger.info("Establishing database connection");
            conn = DBConnection.getConnection();
            DBUsers dbUsers = new DBUsers();

            List<Users> usersList = null;

            if (searchQuery != null && !searchQuery.isEmpty()) {
                logger.info("Searching users by username: {}", searchQuery);
                // Tìm kiếm người dùng theo tên
                usersList = dbUsers.searchUsersByUsername(conn, searchQuery);
            } else {
                logger.info("Fetching all users");
                // Lấy tất cả người dùng
                usersList = dbUsers.getAllUsers(conn);
            }

            logger.debug("Retrieved {} users", usersList != null ? usersList.size() : 0);

            // Lưu danh sách vào request để chuyển tiếp tới trang JSP
            request.setAttribute("usersList", usersList);

            // Chuyển tiếp tới trang JSP để hiển thị kết quả
            logger.info("Forwarding to customerList.jsp");
            RequestDispatcher dispatcher = request.getRequestDispatcher("/customerList.jsp");
            dispatcher.forward(request, response);
        } catch (Exception e) {
            logger.error("Error occurred while processing GET request", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Có lỗi xảy ra: " + e.getMessage());
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                    logger.debug("Database connection closed");
                } catch (Exception e) {
                    logger.error("Error closing database connection", e);
                }
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        logger.info("Processing POST request for CustomerServlet, delegating to doGet");
        // Không có xử lý POST trong trường hợp này
        doGet(request, response);
    }
}