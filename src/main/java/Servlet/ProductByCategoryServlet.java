package Servlet;

import Beans.Products;
import Utils.DBProduct;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 *
 * @author HP
 */
@WebServlet("/ProductByCategory")
public class ProductByCategoryServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String categoryId = request.getParameter("categoryId");

        if (categoryId != null && !categoryId.isEmpty()) {
            try {
                // Lấy danh sách sản phẩm từ DBProduct theo categoryId
                int catId = Integer.parseInt(categoryId);
                ArrayList<Products> productList = DBProduct.findByCategoryId(catId);

                // Đưa danh sách sản phẩm vào request
                request.setAttribute("productList", productList);

                // Chuyển tiếp tới trang JSP để hiển thị sản phẩm
                request.getRequestDispatcher("products.jsp").forward(request, response);
            } catch (SQLException | ClassNotFoundException e) {
                e.printStackTrace();
                request.setAttribute("errorMessage", "There was an error retrieving the products.");
                request.getRequestDispatcher("error.jsp").forward(request, response);
            }
        } else {
            request.setAttribute("errorMessage", "Invalid category ID.");
            request.getRequestDispatcher("error.jsp").forward(request, response);
        }
    }
}


