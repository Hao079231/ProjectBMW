package Servlet;

import Beans.Categories;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import Beans.Products;
import Beans.DBConnection;
import Utils.DBCategories;
import Utils.DBProduct;

@WebServlet("/ProductList")
public class ProductListServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String searchQuery = request.getParameter("search");
        List<Products> productList = null;
        List<Categories> categoriesList = DBCategories.getAllCategories();
        request.setAttribute("categoriesList", categoriesList);

        try (Connection connection = DBConnection.getConnection()) {
            DBProduct dbProduct = new DBProduct(connection);

            if (searchQuery != null && !searchQuery.isEmpty()) {
                // Tìm kiếm sản phẩm theo tên
                productList = dbProduct.searchProductsByName(searchQuery);
            } else {
                // Lấy tất cả sản phẩm nếu không có tìm kiếm
                productList = dbProduct.getAllProducts();
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            request.setAttribute("errorMessage", "Cannot retrieve products.");
        }

        // Đặt danh sách sản phẩm vào request
        request.setAttribute("productList", productList);

        // Chuyển hướng đến trang hiển thị
        request.getRequestDispatcher("user_home.jsp").forward(request, response);
    }
}