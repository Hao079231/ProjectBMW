package Servlet;

import Beans.Categories;
import Utils.DBCategories;
import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;

@WebServlet(name = "UserCategoryServlet", urlPatterns = {"/UserCategory"})
public class UserCategoryServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    public UserCategoryServlet() {
        super();
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");

        if (action == null || action.isEmpty() || "list".equals(action)) {
            try {
                // Truy vấn danh sách categories từ cơ sở dữ liệu
                List<Categories> categoriesList = DBCategories.getAllCategories();
                if (categoriesList != null && !categoriesList.isEmpty()) {
                    // Đặt danh sách categories vào request
                    request.setAttribute("categoriesList", categoriesList);
                } else {
                    request.setAttribute("errorMessage", "No categories found.");
                }
                // Chuyển tiếp đến user_home.jsp
                request.getRequestDispatcher("user_home.jsp").forward(request, response);
            } catch (Exception e) {
                e.printStackTrace();
                request.setAttribute("errorMessage", "Error retrieving categories: " + e.getMessage());
                request.getRequestDispatcher("error.jsp").forward(request, response);
            }
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }
}
