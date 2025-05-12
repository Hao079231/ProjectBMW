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
import java.util.UUID;

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

            // Tạo CSRF token và lưu vào session
            HttpSession session = request.getSession(true); // Đảm bảo session mới nếu cần
            String csrfToken = UUID.randomUUID().toString();
            session.setAttribute("csrfToken", csrfToken);
            request.setAttribute("csrfToken", csrfToken);

            // Chuyển hướng đến trang signinup.jsp
            request.getRequestDispatcher("signinup.jsp").forward(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Không thể truy xuất dữ liệu người dùng");
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        String action = request.getParameter("action");

        // Xử lý đăng xuất trước, không cần kiểm tra CSRF
        if ("logout".equals(action)) {
            handleLogout(request, response);
            return;
        }

        // Kiểm tra CSRF token cho các hành động khác
        String sessionCsrfToken = (String) session.getAttribute("csrfToken");
        String requestCsrfToken = request.getParameter("csrfToken");

        if (sessionCsrfToken == null || requestCsrfToken == null || !sessionCsrfToken.equals(requestCsrfToken)) {
            System.out.println("CSRF INVALID");
            response.sendRedirect("signinup.jsp?status=csrf_invalid");
            return;
        }

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

        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            response.sendRedirect("signinup.jsp?status=empty_fields");
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            Users user = DBUsers.login(conn, username, password);

            if (user != null) {
                HttpSession session = request.getSession();
                session.setAttribute("user", user);

                if ("admin".equals(user.getRole())) {
                    response.sendRedirect("Product");
                } else {
                    response.sendRedirect("ProductList");
                }
                session.removeAttribute("csrfToken");
            } else {
                response.sendRedirect("signinup.jsp?status=signin_failed");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("signinup.jsp?status=signin_error");
        }
    }

    private void handleLogout(HttpServletRequest request, HttpServletResponse response)
        throws IOException, ServletException {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        // Tạo session mới và CSRF token mới
        HttpSession newSession = request.getSession(true);
        String csrfToken = UUID.randomUUID().toString();
        newSession.setAttribute("csrfToken", csrfToken);
        request.setAttribute("csrfToken", csrfToken);
        // Chuyển hướng đến signinup.jsp
        request.setAttribute("status", "logout_success");
        request.getRequestDispatcher("signinup.jsp").forward(request, response);
    }

    private void handleSignup(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String email = request.getParameter("email");
        String address = request.getParameter("address");
        String phone = request.getParameter("phone");
        String role = "user";

        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty() ||
            email == null || email.trim().isEmpty() || address == null || address.trim().isEmpty() ||
            phone == null || phone.trim().isEmpty()) {
            response.sendRedirect("signinup.jsp?status=signup_error");
            return;
        }

        Users newUser = new Users(0, username, password, email, phone, address, role);

        try (Connection conn = DBConnection.getConnection()) {
            Users existingUser = DBUsers.getUserByUsername(conn, username);
            if (existingUser != null) {
                response.sendRedirect("signinup.jsp?status=user_exists");
                return;
            }

            DBUsers.insert(conn, newUser);
            response.sendRedirect("signinup.jsp?status=signup_success");
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("signinup.jsp?status=signup_error");
        }
    }
}