package Servlet;

import Beans.Categories;
import Utils.DBCategories;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@WebServlet("/Category")
public class CategoryServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    public CategoryServlet() {
        super();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");

        if (action == null || action.isEmpty() || "list".equals(action)) {
            // Mặc định hiển thị danh sách loại sản phẩm
            try {
                List<Categories> categoriesList = DBCategories.getAllCategories();
                request.setAttribute("categoriesList", categoriesList);
                request.getRequestDispatcher("category_list.jsp").forward(request, response);
            } catch (Exception e) {
                e.printStackTrace();
                request.setAttribute("errorMessage", "Error retrieving categories: " + e.getMessage());
                request.getRequestDispatcher("error.jsp").forward(request, response);
            }
        } else if ("edit".equals(action)) {
            // Hiển thị thông tin loại sản phẩm để chỉnh sửa
            try {
                int categoryId = Integer.parseInt(request.getParameter("categoryId"));
                Categories category = DBCategories.getCategoryById(categoryId);
                request.setAttribute("category", category);
                request.getRequestDispatcher("category_edit.jsp").forward(request, response);
            } catch (Exception e) {
                e.printStackTrace();
                request.setAttribute("errorMessage", "Invalid category ID: " + e.getMessage());
                request.getRequestDispatcher("error.jsp").forward(request, response);
            }
        } else if ("delete".equals(action)) {
            // Xóa loại sản phẩm
            try {
                int categoryId = Integer.parseInt(request.getParameter("categoryId"));
                DBCategories.deleteCategoryById(categoryId);
                response.sendRedirect("CategoryServlet?action=list");
            } catch (Exception e) {
                e.printStackTrace();
                request.setAttribute("errorMessage", "Error deleting category: " + e.getMessage());
                request.getRequestDispatcher("error.jsp").forward(request, response);
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        try {
            if ("add".equals(action)) {
                // Thêm loại sản phẩm mới
                String cname = request.getParameter("cname");
                Categories newCategory = new Categories(0, cname);
                DBCategories.addCategory(newCategory);
                response.sendRedirect("CategoryServlet?action=list");
            } else if ("update".equals(action)) {
                // Cập nhật loại sản phẩm
                int categoryId = Integer.parseInt(request.getParameter("categoryId"));
                String cname = request.getParameter("cname");
                Categories updatedCategory = new Categories(categoryId, cname);
                DBCategories.updateCategory(updatedCategory);
                response.sendRedirect("CategoryServlet?action=list");
            }
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("errorMessage", "Error processing request: " + e.getMessage());
            request.getRequestDispatcher("error.jsp").forward(request, response);
        }
    }
}