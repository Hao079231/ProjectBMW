<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="Beans.Orders" %>
<%@ page import="Utils.DBOrder" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <script src="https://cdn.tailwindcss.com"></script>
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.15.3/css/all.min.css" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Roboto:wght@400;700&display=swap" rel="stylesheet">
    <title>Jellycat</title>
</head>
<body class="font-roboto bg-gray-100">
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

        // Function to add CSRF token to forms before submission
        function addCsrfTokenToForm(form) {
            form.addEventListener('submit', function(event) {
                event.preventDefault(); // Prevent default submission

                // Create a hidden input for the CSRF token
                const csrfInput = document.createElement('input');
                csrfInput.type = 'hidden';
                csrfInput.name = 'csrfToken';
                const csrfToken = '<%= session.getAttribute("csrfToken") %>';
                csrfInput.value = csrfToken;

                // Append the CSRF token to the form
                form.appendChild(csrfInput);

                // Submit the form
                form.submit();
            });
        }

        // Apply CSRF token handling to all forms
        document.addEventListener('DOMContentLoaded', function() {
            const orderForms = document.querySelectorAll('form[action="CustomerOrder"]');
            orderForms.forEach(form => addCsrfTokenToForm(form));
        });
    </script>
<!-- Slideshow Section -->
    <div class="slideshow-container">
        <%@ include file="slideHeader.jsp" %>
    </div>
    <!-- Header -->
    <header class="border-b">
        <div class="container mx-auto flex justify-between items-center py-4 px-6">
            <!-- Logo -->
            <div class="logo flex items-center">
                <img alt="Jellycat logo" class="mr-4" height="50" src="https://storage.googleapis.com/a1aa/image/FQHw3eMxpk2eakMv3O1MJ3DrSguzwJk8FAGfLnYFr6mlCFlnA.jpg" width="50">
                <span class="text-2xl font-bold text-blue-500">JELLYCAT</span>
            </div>
            <!-- Icons -->
            <div class="flex items-center space-x-6">
                <%
                    // Kiểm tra xem người dùng đã đăng nhập hay chưa
                    String profileLink;
                    if (session.getAttribute("user") != null) {
                        // Nếu đã đăng nhập, dẫn đến profile.jsp
                        profileLink = "profile.jsp";
                    } else {
                        // Nếu chưa đăng nhập, dẫn đến signinup.jsp
                        profileLink = "signinup.jsp";
                    }
                %>
                <a href="<%= profileLink %>">
                    <i class="fas fa-user text-gray-600 text-lg cursor-pointer"></i>
                </a>

                <i class="fas fa-heart text-gray-600 text-lg cursor-pointer"></i>

                <a href="cart.jsp">
                    <i class="fas fa-shopping-cart text-gray-600 text-lg cursor-pointer"></i>
                </a>
            </div>
        </div>
    </header>

    <!-- Navigation -->
    <nav class="bg-gray-100">
        <div class="container mx-auto flex items-center justify-between py-3">
            <!-- Các mục còn lại ở chính giữa -->
             <div class="flex justify-center space-x-8">
                <a class="text-gray-600 text-sm font-bold hover:text-black" href="about.jsp">GIỚI THIỆU</a>
                <a class="text-gray-600 text-sm font-bold hover:text-black" href="ProductList">SẢN PHẨM</a>
                <a class="text-gray-600 text-sm font-bold hover:text-black" href="CustomerOrder">ĐƠN HÀNG</a>
                <a class="text-gray-600 text-sm font-bold hover:text-black" href="contact.jsp">LIÊN HỆ</a>
            </div>
        </div>
    </nav>

    <div class="max-w-7xl mx-auto px-4 py-6" id="main-content">
        <!-- Orders Section -->
        <h1 class="text-3xl font-bold mb-6">Danh sách đơn hàng</h1>

        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6" id="order-list">
            <!-- Order Cards will be inserted here dynamically -->
            <%
                List<Orders> ordersList = (List<Orders>) request.getAttribute("ordersList"); // Lấy danh sách đơn hàng
                if (ordersList != null && !ordersList.isEmpty()) { // Kiểm tra danh sách không null và không rỗng
                    for (Orders order : ordersList) { // Duyệt qua danh sách đơn hàng
            %>
                        <div class="bg-white p-6 rounded-lg shadow-lg">
                            <h2 class="text-xl font-bold mb-2">Mã đơn hàng: <%= order.getOrderId() %></h2>
                            <p class="text-gray-700 mb-4">Tổng tiền: <%= order.getTotalAmount() %></p>
                            <p class="text-gray-700 mb-4">Trạng thái thanh toán: <%= order.isPaymentStatus() ? "Đã thanh toán" : "Chưa thanh toán" %></p>
                            <p class="text-gray-700 mb-4">Trạng thái giao hàng: <%= order.getDeliveryStatus() %></p>
                            <p class="text-gray-700 mb-4">Ngày tạo: <%= order.getCreatedAt() %></p>
                            <p class="text-gray-700 mb-4">Ngày cập nhật: <%= order.getUpdatedAt() %></p>
                             <!-- Cập nhật liên kết chi tiết đơn hàng -->
                        <a href="CustomerOrder?order_id=<%= order.getOrderId() %>" class="bg-blue-500 text-white px-3 py-1 rounded">Chi tiết</a>

                        <!-- Nút Đã nhận được hàng -->
                        <% if (!order.isPaymentStatus()) { %> <!-- Kiểm tra nếu chưa thanh toán -->
                                <form action="CustomerOrder" method="post" class="mt-4">
                                    <input type="hidden" name="orderId" value="<%= order.getOrderId() %>">
                                    <button type="submit" name="action" value="received" class="bg-green-500 text-white px-4 py-2 rounded">Đã nhận được hàng</button>
                                </form>
                            <% } %>
                    </div>
            <%
                    }
                } else {
            %>
                    <p class="text-center text-gray-500">Không có đơn hàng nào.</p>
            <%
                }
            %>
        </div>
    </div>

    <script>
        const btn = document.querySelector("button.mobile-menu-button");
        const menu = document.querySelector(".mobile-menu");

        btn.addEventListener("click", () => {
            menu.classList.toggle("hidden");
        });
    </script>
</body>
</html>