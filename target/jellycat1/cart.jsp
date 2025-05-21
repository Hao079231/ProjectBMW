<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="Beans.Cart"%>
<%@page import="Beans.CartItem"%>
<%@page import="java.util.List"%>
<%@page import="java.util.Map"%>
<%@ page import="Beans.Products" %>


<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Your Cart</title>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/tailwindcss@1.9.6/dist/tailwind.min.css">
</head>
<body class="bg-gray-100">
    	<% 
	    String message = (String) session.getAttribute("message");
	    String error = (String) session.getAttribute("error");
	
	    // Xóa thông báo khỏi session sau khi hiển thị
	    session.removeAttribute("message");
	    session.removeAttribute("error");
	%>
	
	<!-- Thông báo thành công -->
	<% if (message != null) { %>
	    <div id="success-toast" class="bg-green-600 text-white p-4 rounded-lg shadow-lg mb-6 flex items-center space-x-4">
	        <i class="fas fa-check-circle text-xl"></i>
	        <span><%= message %></span>
	        <button onclick="closeToast('success-toast')" class="ml-4 bg-transparent text-white font-bold py-2 px-4 border border-white rounded">OK</button>
	    </div>
	<% } %>
	
	<!-- Thông báo lỗi -->
	<% if (error != null) { %>
	    <div id="error-toast" class="bg-red-600 text-white p-4 rounded-lg shadow-lg mb-6 flex items-center space-x-4">
	        <i class="fas fa-exclamation-circle text-xl"></i>
	        <span><%= error %></span>
	        <button onclick="closeToast('error-toast')" class="ml-4 bg-transparent text-white font-bold py-2 px-4 border border-white rounded">OK</button>
	    </div>
	<% } %>
	
	<script>
	    // Hàm đóng thông báo
	    function closeToast(id) {
	        var toast = document.getElementById(id);
	        if (toast) {
	            toast.style.display = 'none';
	        }
	    }
	
	    // Tự động ẩn thông báo sau 5 giây
	    setTimeout(function() {
	        var successToast = document.getElementById('success-toast');
	        var errorToast = document.getElementById('error-toast');
	        if (successToast) successToast.style.display = 'none';
	        if (errorToast) errorToast.style.display = 'none';
	    }, 5000);
	</script>
    <header class="bg-white shadow-lg">
        <div class="container mx-auto flex justify-between items-center py-4 px-6">
            <div class="logo flex items-center">
                <img alt="Jellycat logo" class="mr-4" height="50" src="https://storage.googleapis.com/a1aa/image/FQHw3eMxpk2eakMv3O1MJ3DrSguzwJk8FAGfLnYFr6mlCFlnA.jpg" width="50">
                <span class="text-2xl font-bold text-blue-500">JELLYCAT</span>
            </div>
            <div class="flex items-center space-x-6">
                <!-- Thêm liên kết Home -->
                <a href="ProductList" class="text-gray-600 hover:text-blue-500">Home</a>
            </div>
        </div>
    </header>
    <div class="container mx-auto py-8">
        <h1 class="text-3xl font-bold mb-4">GIỎ HÀNG</h1>

        <%
            // Lấy giỏ hàng từ session
            Beans.Cart cart = (Beans.Cart) session.getAttribute("cart");
            if (cart != null && !cart.isEmpty()) {
        %>
        <table class="min-w-full bg-white border border-gray-300">
            <thead>
                <tr>
                    <th class="py-2 px-4 border-b">Image</th>
                    <th class="py-2 px-4 border-b">Product Name</th>
                    <th class="py-2 px-4 border-b">Price</th>
                    <th class="py-2 px-4 border-b">Quantity</th>
                    <th class="py-2 px-4 border-b">Total</th>
                    <th class="py-2 px-4 border-b">Action</th>
                </tr>
            </thead>
            <tbody>
                <%
                    // Hiển thị từng sản phẩm trong giỏ hàng
                    for (Map.Entry<Integer, CartItem> entry : cart.getItems().entrySet()) {
                        Integer productId = entry.getKey();
                        CartItem item = entry.getValue();
                        Products product = item.getProduct();
                %>
                <tr>
                    <td class="py-2 px-4 border-b">
                        <img src="data:image/jpeg;base64,<%= java.util.Base64.getEncoder().encodeToString(product.getImage()) %>" 
                             alt="<%= product.getName() %>" class="w-20 h-20 object-cover"/>
                    </td>
                    <td class="py-2 px-4 border-b"><%= product.getName() %></td>
                    <td class="py-2 px-4 border-b"><%= product.getPrice() %> VND</td>
                    <td class="py-2 px-4 border-b">
                        <form action="CartServlet" method="post" class="inline">
                            <input type="hidden" name="action" value="edit"/>
                            <input type="hidden" name="productId" value="<%= productId %>"/>
                            <input type="number" name="quantity" value="<%= item.getQuantity() %>" min="1" class="border border-gray-300 rounded px-2"/>
                            <input type="hidden" name="csrfToken" value="<%= session.getAttribute("csrfToken") %>" />
                            <button type="submit" class="ml-2 bg-blue-600 text-white rounded px-2 py-1">Update</button>
                        </form>
                    </td>
                    <td class="py-2 px-4 border-b"><%= item.getTotal() %> VND</td>
                    <td class="py-2 px-4 border-b">
                        <form action="CartServlet" method="post" class="inline">
                            <input type="hidden" name="action" value="remove"/>
                            <input type="hidden" name="productId" value="<%= productId %>"/>
                            <input type="hidden" name="csrfToken" value="<%= session.getAttribute("csrfToken") %>" />
                            <button type="submit" class="bg-red-600 text-white rounded px-2 py-1">Remove</button>
                        </form>
                    </td>
                </tr>
                <%
                    }
                %>
            </tbody>
        </table>

        <div class="mt-4">
            <h2 class="text-xl font-bold">Total Amount: <%= cart.getTotalPrice() %> VND</h2>
        </div>

        <div class="mt-4">
            <a href="ProductList" class="bg-green-600 text-white rounded px-4 py-2">TIẾP TỤC MUA SẮM</a>
            <a href="checkout.jsp" class="bg-blue-600 text-white rounded px-4 py-2 ml-2">ĐẶT HÀNG</a>
        </div>

        <%
            } else {
        %>
        <p class="text-gray-500">GIỎ HÀNG TRỐNG.</p>
        <a href="ProductList" class="bg-green-600 text-white rounded px-4 py-2">TIẾP TỤC MUA SẮM</a>
        <%
            }
        %>
    </div>
</body>
</html>
