// Servlet/ProductServlet.java
package Servlet;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.util.List;

import Beans.Products;
import Beans.Categories;
import Beans.DBConnection;
import Utils.DBCategories;
import Utils.DBProduct;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

@WebServlet("/Product")
@MultipartConfig
public class ProductServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    public ProductServlet() {
        super();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        String productId = request.getParameter("productId");
        String searchQuery = request.getParameter("search");

        Connection conn = null;

        if (action == null) {
            action = "";
        }

        try {
            conn = DBConnection.getConnection();
            DBProduct dbProduct = new DBProduct(conn);

            if (action.equals("add")) {
            	List<Categories> categoriesList = DBCategories.getAllCategories();
	            request.setAttribute("categoriesList", categoriesList);
	            getServletContext().getRequestDispatcher("/addProduct.jsp").forward(request, response);

            } else if (action.equals("edit")) {
                if (productId != null && !productId.isEmpty()) {
                    Products product = dbProduct.getProductById(Integer.parseInt(productId));
                    request.setAttribute("product", product);
                    List<Categories> categoriesList = DBCategories.getAllCategories();
                    request.setAttribute("categoriesList", categoriesList);
                    getServletContext().getRequestDispatcher("/editProduct.jsp").forward(request, response);
                }

            } else if (action.equals("delete")) {
                if (productId != null && !productId.isEmpty()) {
                    dbProduct.deleteProduct(Integer.parseInt(productId));
                }
                response.sendRedirect("Product");

            } else if (searchQuery != null && !searchQuery.isEmpty()) {
                List<Products> productList = dbProduct.searchProductsByName(searchQuery);
                request.setAttribute("productList", productList);
                getServletContext().getRequestDispatcher("/admin_home.jsp").forward(request, response);

            } else {
                List<Products> productList = dbProduct.getAllProducts();
                request.setAttribute("productList", productList);
                getServletContext().getRequestDispatcher("/admin_home.jsp").forward(request, response);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        String productId = request.getParameter("productId");

        Connection conn = null;
        Products product = new Products();

        try {
            conn = DBConnection.getConnection();
            DBProduct dbProduct = new DBProduct(conn);

            // Lấy thông tin từ form
            String name = request.getParameter("name");
            String description = request.getParameter("description");
            String priceStr = request.getParameter("price");
            String size = request.getParameter("size");
            String stockStr = request.getParameter("stock");
            String categoryIdStr = request.getParameter("categoryId");
            String statusStr = request.getParameter("status");

            product.setName(name);
            product.setDescription(description);
            product.setPrice(Integer.parseInt(priceStr));
            product.setSize(size);
            product.setStock(Integer.parseInt(stockStr));
            product.setCategoryId(Integer.parseInt(categoryIdStr));
            product.setStatus(Boolean.parseBoolean(statusStr));

            // Kiểm tra và gán thuộc tính ảnh
            Part imagePart = request.getPart("image");
            if (imagePart != null && imagePart.getSize() > 0) {
                InputStream imageInputStream = imagePart.getInputStream();
                byte[] imageBytes = imageInputStream.readAllBytes();
                product.setImage(imageBytes);
            }

            if (productId != null && !productId.isEmpty()) {
                product.setProductId(Integer.parseInt(productId));
            }

            if (action.equals("add")) {
                dbProduct.addProduct(product);
            } else if (action.equals("edit")) {
                dbProduct.updateProduct(product);
            }

            response.sendRedirect("Product");

        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("addProduct.jsp?status=error");
        }
    }
}
