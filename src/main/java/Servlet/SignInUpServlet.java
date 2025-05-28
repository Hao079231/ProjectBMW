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
import Utils.PasswordUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WebServlet("/SignInUp")
public class SignInUpServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(SignInUpServlet.class);
    private static final int MAX_LOGIN_ATTEMPTS = 5;
    private static final long LOCKOUT_DURATION = 15 * 60 * 1000; // 15 minutes in milliseconds

    public SignInUpServlet() {
        super();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String remoteIp = request.getRemoteAddr();
        String action = request.getParameter("action");
        logger.info("GET request received from IP: {}, action: {}", remoteIp, action);

        try (Connection conn = DBConnection.getConnection()) {
            List<Users> userList = DBUsers.getAllUsers(conn);
            request.setAttribute("userList", userList);
            request.getRequestDispatcher("signinup.jsp").forward(request, response);
        } catch (Exception e) {
            logger.error("Error retrieving user data from IP: {}, error: {}", remoteIp, e.getMessage());
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Không thể truy xuất dữ liệu người dùng");
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        String remoteIp = request.getRemoteAddr();

        logger.info("POST request received from IP: {}, action: {}", remoteIp, action);

        // Validate CSRF token
        String csrfToken = request.getParameter("csrfToken");
        String sessionCsrfToken = (String) request.getSession().getAttribute("csrfToken");
        if (csrfToken == null || !csrfToken.equals(sessionCsrfToken)) {
            logger.warn("Invalid CSRF token from IP: {}", remoteIp);
            response.sendRedirect("signinup.jsp?status=invalid_csrf");
            return;
        }

        if ("signup".equals(action)) {
            handleSignup(request, response);
        } else if ("signin".equals(action)) {
            handleSignin(request, response);
        } else if ("logout".equalsIgnoreCase(action)) {
            handleLogout(request, response);
        } else {
            logger.warn("Invalid action from IP: {}, action: {}", remoteIp, action);
            response.sendRedirect("signinup.jsp?status=invalid_action");
        }
    }

    private void handleSignin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String remoteIp = request.getRemoteAddr();
        HttpSession session = request.getSession();

        // Check for lockout
        Long lockoutTime = (Long) session.getAttribute("lockoutTime");
        if (lockoutTime != null && System.currentTimeMillis() < lockoutTime) {
            logger.warn("Account locked for IP: {}, username: {}", remoteIp, username);
            response.sendRedirect("signinup.jsp?status=account_locked");
            return;
        }

        // Check login attempts
        Integer attempts = (Integer) session.getAttribute("loginAttempts");
        if (attempts == null) {
            attempts = 0;
        }

        if (attempts >= MAX_LOGIN_ATTEMPTS) {
            session.setAttribute("lockoutTime", System.currentTimeMillis() + LOCKOUT_DURATION);
            logger.warn("Too many login attempts from IP: {}, username: {}", remoteIp, username);
            response.sendRedirect("signinup.jsp?status=too_many_attempts");
            return;
        }

        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            logger.warn("Empty fields in signin attempt from IP: {}, username: {}", remoteIp, username);
            session.setAttribute("loginAttempts", attempts + 1);
            response.sendRedirect("signinup.jsp?status=empty_fields");
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            Users user = DBUsers.login(conn, username, password);
            if (user != null) {
                // Regenerate session ID to prevent session fixation
                session.invalidate();
                session = request.getSession(true);
                session.setAttribute("user", user);
                session.setMaxInactiveInterval(30 * 60); // 30 minutes timeout
                session.setAttribute("loginAttempts", 0); // Reset attempts
                logger.info("Signin successful from IP: {}, username: {}, role: {}", remoteIp, username, user.getRole());
                if ("admin".equals(user.getRole())) {
                    response.sendRedirect("Product");
                } else {
                    response.sendRedirect("ProductList");
                }
            } else {
                session.setAttribute("loginAttempts", attempts + 1);
                logger.warn("Signin failed from IP: {}, username: {}", remoteIp, username);
                response.sendRedirect("signinup.jsp?status=signin_failed");
            }
        } catch (Exception e) {
            session.setAttribute("loginAttempts", attempts + 1);
            logger.error("Signin error from IP: {}, username: {}, error: {}", remoteIp, username, e.getMessage());
            response.sendRedirect("signinup.jsp?status=signin_error");
        }
    }

    private void handleLogout(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        HttpSession session = request.getSession(false);
        String remoteIp = request.getRemoteAddr();

        if (session != null) {
            String username = (session.getAttribute("user") != null) ? ((Users) session.getAttribute("user")).getUsername() : "unknown";
            session.invalidate();
            logger.info("Logout successful from IP: {}, username: {}", remoteIp, username);
        }

        response.sendRedirect("signinup.jsp?status=logout_success");
    }

    private void handleSignup(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String email = request.getParameter("email");
        String address = request.getParameter("address");
        String phone = request.getParameter("phone");
        String remoteIp = request.getRemoteAddr();

        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty() ||
                email == null || email.trim().isEmpty() || address == null || address.trim().isEmpty() ||
                phone == null || phone.trim().isEmpty()) {
            logger.warn("Empty fields in signup attempt from IP: {}, username: {}", remoteIp, username);
            response.sendRedirect("signinup.jsp?status=signup_error");
            return;
        }

        // Validate password strength
        if (!PasswordUtils.isPasswordStrong(password)) {
            logger.warn("Weak password in signup attempt from IP: {}, username: {}", remoteIp, username);
            response.sendRedirect("signinup.jsp?status=weak_password");
            return;
        }

        Users newUser = new Users(0, username, password, email, phone, address, "user", null);

        try (Connection conn = DBConnection.getConnection()) {
            Users existingUser = DBUsers.getUserByUsername(conn, username);
            if (existingUser != null) {
                logger.warn("Signup failed from IP: {}, username: {}, reason: user exists", remoteIp, username);
                response.sendRedirect("signinup.jsp?status=user_exists");
                return;
            }
            DBUsers.insert(conn, newUser);
            logger.info("Signup successful from IP: {}, username: {}", remoteIp, username);
            response.sendRedirect("signinup.jsp?status=signup_success");
        } catch (Exception e) {
            logger.error("Signup error from IP: {}, username: {}, error: {}", remoteIp, username, e.getMessage());
            response.sendRedirect("signinup.jsp?status=signup_error");
        }
    }
}