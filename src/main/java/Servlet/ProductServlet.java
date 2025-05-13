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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WebServlet("/Product")
@MultipartConfig
public class ProductServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(ProductServlet.class);

    public ProductServlet() {
        super();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        String action = request.getParameter("action");
        String productId = request.getParameter("productId");
        String searchQuery = request.getParameter("search");

        logger.info("Received GET request with action: {}, productId: {}, searchQuery: {}", action, productId, searchQuery);

        if (action == null) {
            action = "";
        }

        try (Connection conn = DBConnection.getConnection()) {
            DBProduct dbProduct = new DBProduct(conn);

            if (action.equals("add")) {
                logger.debug("Fetching categories for add product page");
                List<Categories> categoriesList = DBCategories.getAllCategories();
                request.setAttribute("categoriesList", categoriesList);
                logger.info("Retrieved {} categories for add product page", categoriesList.size());
                getServletContext().getRequestDispatcher("/addProduct.jsp").forward(request, response);

            } else if (action.equals("edit")) {
                if (productId != null && !productId.isEmpty()) {
                    logger.debug("Fetching product with ID: {}", productId);
                    Products product = dbProduct.getProductById(Integer.parseInt(productId));
                    request.setAttribute("product", product);
                    List<Categories> categoriesList = DBCategories.getAllCategories();
                    request.setAttribute("categoriesList", categoriesList);
                    logger.info("Retrieved product ID: {} and {} categories for edit page", productId, categoriesList.size());
                    getServletContext().getRequestDispatcher("/editProduct.jsp").forward(request, response);
                } else {
                    logger.warn("No product ID provided for edit action");
                    response.sendRedirect("Product");
                }

            } else if (action.equals("delete")) {
                if (productId != null && !productId.isEmpty()) {
                    logger.info("Deleting product with ID: {}", productId);
                    dbProduct.deleteProduct(Integer.parseInt(productId));
                    logger.info("Successfully deleted product ID: {}", productId);
                } else {
                    logger.warn("No product ID provided for delete action");
                }
                response.sendRedirect("Product");

            } else if (searchQuery != null && !searchQuery.isEmpty()) {
                logger.debug("Searching products with query: {}", searchQuery);
                List<Products> productList = dbProduct.searchProductsByName(searchQuery);
                request.setAttribute("productList", productList);
                logger.info("Retrieved {} products for search query: {}", productList.size(), searchQuery);
                getServletContext().getRequestDispatcher("/admin_home.jsp").forward(request, response);

            } else {
                logger.debug("Fetching all products");
                List<Products> productList = dbProduct.getAllProducts();
                request.setAttribute("productList", productList);
                logger.info("Retrieved {} products for product list", productList.size());
                getServletContext().getRequestDispatcher("/admin_home.jsp").forward(request, response);
            }

        } catch (Exception e) {
            logger.error("Error processing GET request with action: {}, productId: {}, searchQuery: {}", action, productId, searchQuery, e);
            throw new ServletException("Error processing request", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        String action = request.getParameter("action");
        String productId = request.getParameter("productId");

        logger.info("Received POST request with action: {}, productId: {}", action, productId);

        try (Connection conn = DBConnection.getConnection()) {
            DBProduct dbProduct = new DBProduct(conn);
            Products product = new Products();

            // Lấy thông tin từ form
            String name = request.getParameter("name");
            String description = request.getParameter("description");
            String priceStr = request.getParameter("price");
            String size = request.getParameter("size");
            String stockStr = request.getParameter("stock");
            String categoryIdStr = request.getParameter("categoryId");
            String statusStr = request.getParameter("status");

            logger.debug("Processing product data: name={}, price={}, categoryId={}", name, priceStr, categoryIdStr);

            try {
                product.setName(name);
                product.setDescription(description);
                product.setPrice(Integer.parseInt(priceStr));
                product.setSize(size);
                product.setStock(Integer.parseInt(stockStr));
                product.setCategoryId(Integer.parseInt(categoryIdStr));
                product.setStatus(Boolean.parseBoolean(statusStr));
            } catch (NumberFormatException e) {
                logger.warn("Invalid number format in form data: price={}, stock={}, categoryId={}", priceStr, stockStr, categoryIdStr);
                response.sendRedirect("addProduct.jsp?status=error");
                return;
            }

            // Kiểm tra và gán thuộc tính ảnh
            Part imagePart = request.getPart("image");
            if (imagePart != null && imagePart.getSize() > 0) {
                logger.debug("Processing image upload for product");
                InputStream imageInputStream = imagePart.getInputStream();
                byte[] imageBytes = imageInputStream.readAllBytes();
                product.setImage(imageBytes);
                logger.info("Image uploaded successfully, size: {} bytes", imageBytes.length);
            }

            if (productId != null && !productId.isEmpty()) {
                try {
                    product.setProductId(Integer.parseInt(productId));
                } catch (NumberFormatException e) {
                    logger.warn("Invalid product ID format: {}", productId);
                    response.sendRedirect("addProduct.jsp?status=error");
                    return;
                }
            }

            if (action.equals("add")) {
                logger.info("Adding new product: {}", name);
                dbProduct.addProduct(product);
                logger.info("Successfully added product: {}", name);
            } else if (action.equals("edit")) {
                logger.info("Updating product ID: {}", productId);
                dbProduct.updateProduct(product);
                logger.info("Successfully updated product ID: {}", productId);
            } else {
                logger.warn("Invalid action: {}", action);
                response.sendRedirect("addProduct.jsp?status=error");
                return;
            }

            response.sendRedirect("Product");

        } catch (Exception e) {
            logger.error("Error processing POST request with action: {}, productId: {}", action, productId, e);
            response.sendRedirect("addProduct.jsp?status=error");
        }
    }
}