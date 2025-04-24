<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="Beans.Products"%>
<%@page import="Beans.Review"%>
<%@page import="Utils.DBProduct"%>
<%@page import="Utils.BDReview"%>
<%@page import="java.sql.SQLException"%>
<%@page import="java.util.List"%>
<%@page import="java.sql.Connection"%>
<%@page import="Beans.DBConnection"%>

<%
    Products product = null;
    List<Review> reviews = null;
    String productIdStr = request.getParameter("productId");

    if (productIdStr != null) {
        try {
            int productId = Integer.parseInt(productIdStr);

            // Lấy kết nối từ DBConnection
            Connection connection = DBConnection.getConnection();

            // Lấy thông tin sản phẩm
            DBProduct dbProduct = new DBProduct(connection);
            product = dbProduct.getProductById(productId);

            // Lấy danh sách đánh giá
            BDReview dbReview = new BDReview(connection);
            reviews = dbReview.getReviewsByProductId(productId);

        } catch (NumberFormatException e) {
            out.println("<p class='text-red-600'>Invalid product ID.</p>");
        } catch (SQLException e) {
            out.println("<p class='text-red-600'>Database error: " + e.getMessage() + "</p>");
        } catch (Exception e) {
            out.println("<p class='text-red-600'>An error occurred: " + e.getMessage() + "</p>");
        }
    } else {
        out.println("<p class='text-red-600'>No product ID provided.</p>");
    }
%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><%= product != null ? product.getName() : "Product Detail" %></title>
    <link href="https://cdn.jsdelivr.net/npm/tailwindcss@1.9.6/dist/tailwind.min.css" rel="stylesheet">
</head>
<body class="bg-gray-100">

    <!-- Header -->
    <header class="bg-white shadow-lg">
        <div class="container mx-auto flex justify-between items-center py-4 px-6">
            <div class="logo flex items-center">
                <img alt="Jellycat logo" class="mr-4" height="50" src="https://storage.googleapis.com/a1aa/image/FQHw3eMxpk2eakMv3O1MJ3DrSguzwJk8FAGfLnYFr6mlCFlnA.jpg" width="50">
                <span class="text-2xl font-bold text-blue-500">JELLYCAT</span>
            </div>
            <div class="flex items-center space-x-6">
                <a href="ProductList" class="text-gray-600 hover:text-blue-500">Home</a>
            </div>
        </div>
    </header>

    <div class="container mx-auto py-8">
        <h1 class="text-4xl font-semibold text-center mb-8"><%= product != null ? product.getName() : "Product Not Found" %></h1>

        <% if (product != null) { %>
            <div class="flex justify-center items-start space-x-8">
                <div class="w-1/3">
                    <% if (product.getImage() != null) { %>
                        <img src="data:image/jpeg;base64,<%= java.util.Base64.getEncoder().encodeToString(product.getImage()) %>" 
                             alt="<%= product.getName() %>" class="w-full h-80 object-cover rounded-lg shadow-md"/>
                    <% } else { %>
                        <img src="default-product.jpg" alt="No image available" class="w-full h-80 object-cover rounded-lg shadow-md"/>
                    <% } %>
                </div>

                <div class="w-2/3">
                    <p class="text-black-700 text-lg mb-4"><%= product.getDescription() %></p>
                    <p class="text-green-600 font-bold text-xl mb-4">Price: <%= product.getPrice() %> VND</p>
                    <p class="text-black-500 text-sm mb-4">Stock: <%= product.getStock() %></p>

                    <form method="post" action="Cart" class="flex items-center">
                        <input type="hidden" name="action" value="add"/>
                        <input type="hidden" name="userId" value="<%= session.getAttribute("userId") %>" />
                        <input type="hidden" name="productId" value="<%= product.getProductId() %>" />
                        <input type="hidden" name="productName" value="<%= product.getName() %>">
                        <input type="hidden" name="price" value="<%= product.getPrice() %>">
                        <input type="hidden" name="productImage" value="<%= java.util.Base64.getEncoder().encodeToString(product.getImage()) %>">
                        <input type="hidden" name="quantity" value="1" /> 
                        <button type="submit" class="bg-blue-600 text-white py-2 px-4 rounded-lg hover:bg-blue-700 transition duration-300">
                            Thêm vào giỏ hàng
                        </button>
                    </form>
                </div>
            </div>

            <!-- Hiển thị danh sách đánh giá -->
            <h2 class="text-2xl font-bold mt-12 mb-4">Đánh giá của khách hàng</h2>
            <% if (reviews != null && !reviews.isEmpty()) { %>
                <div class="space-y-4">
                    <% for (Review review : reviews) { %>
                        <div class="bg-white p-4 rounded-lg shadow">
                        	<p class="text-sm text-black-400">Người đánh giá: <%= review.getUsername() %></p> <!-- Hiển thị tên người dùng -->
                            <p class="text-lg font-semibold"><%= review.getComment() %></p>
                            <p class="text-sm text-black-500">Rating: <%= review.getRating() %>/5</p>
                            <p class="text-sm text-black-400">Ngày: <%= review.getCreatedAt() %></p>
                        </div>
                    <% } %>
                </div>
            <% } else { %>
                <p class="text-black-500">Sản phẩm này chưa có đánh giá!</p>
            <% } %>
        <% } else { %>
            <p class="text-red-600 text-center mt-4">Product not found.</p>
        <% } %>
    </div>

    <footer class="bg-blue-500 text-white py-4">
        <div class="text-center">
            <p>&copy; 2024 Jellycat.</p>
        </div>
    </footer>

</body>
</html>
