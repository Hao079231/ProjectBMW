package Servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import Beans.Users;
import java.io.IOException;
import java.sql.Connection;
import java.util.List;

import Utils.DBUsers;
import Beans.DBConnection;

@WebServlet("/SignInUp")
public class SignInUpServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    public SignInUpServlet() {
        super();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try (Connection conn = DBConnection.getConnection()) {
            // Lấy danh sách người dùng từ DB
            List<Users> userList = DBUsers.getAllUsers(conn);

            // Đưa danh sách người dùng vào request để truyền tới JSP
            request.setAttribute("userList", userList);

            // Chuyển hướng đến trang userlist.jsp để hiển thị
            request.getRequestDispatcher("signinup.jsp").forward(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Không thể truy xuất dữ liệu người dùng");
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");

        // Kiểm tra hành động đăng ký hay đăng nhập
        if ("signup".equals(action)) {
            handleSignup(request, response);
        } else if ("signin".equals(action)) {
            handleSignin(request, response);
        } else {
            response.sendRedirect("signinup.jsp?status=invalid_action");
        }
    }

    private void handleSignin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        // Kiểm tra các trường đăng nhập
        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            response.sendRedirect("signinup.jsp?status=empty_fields");
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            Users user = DBUsers.login(conn, username, password);

            if (user != null) {
                // Lưu thông tin người dùng vào session nếu đăng nhập thành công
                HttpSession session = request.getSession();
                session.setAttribute("user", user);

                // Kiểm tra vai trò người dùng
                if ("admin".equals(user.getRole())) {
                    response.sendRedirect("Product");
                } else {
                    response.sendRedirect("ProductList");
                    //response.sendRedirect("userlist.jsp");
                }
            } else {
                response.sendRedirect("signinup.jsp?status=signin_failed");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("signinup.jsp?status=signin_error");
        }
    }

    private void handleSignup(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String email = request.getParameter("email");
        String address = request.getParameter("address");
        String phone = request.getParameter("phone");
        String role = "user"; // Mặc định vai trò là user

        // Kiểm tra các trường bắt buộc
        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty() ||
            email == null || email.trim().isEmpty() || address == null || address.trim().isEmpty() ||
            phone == null || phone.trim().isEmpty()) {
            response.sendRedirect("signinup.jsp?status=signup_error");
            return;
        }

        // Tạo đối tượng người dùng mới với vai trò là "user"
        Users newUser = new Users(0, username, password, email, phone, address, role);

        try (Connection conn = DBConnection.getConnection()) {
            // Kiểm tra nếu người dùng đã tồn tại
            Users existingUser = DBUsers.getUserByUsername(conn, username);
            if (existingUser != null) {
                response.sendRedirect("signinup.jsp?status=user_exists");
                return;
            }

            // Thêm người dùng vào cơ sở dữ liệu
            DBUsers.insert(conn, newUser);
            response.sendRedirect("signinup.jsp?status=signup_success");
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("signinup.jsp?status=signup_error");
        }
    }
}
