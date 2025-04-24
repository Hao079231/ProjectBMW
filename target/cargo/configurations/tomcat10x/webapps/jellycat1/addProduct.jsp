<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="Beans.Categories" %>
<%@ page import="Beans.Products" %>

<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Thêm Sản Phẩm</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.15.3/css/all.min.css" rel="stylesheet"/>
    <link href="https://fonts.googleapis.com/css2?family=Roboto:wght@400;700&display=swap" rel="stylesheet"/>
    <style>
        body {
            font-family: 'Roboto', sans-serif;
        }
    </style>
</head>
<body class="bg-gray-100 flex items-center justify-center min-h-screen">
    <div class="bg-white p-8 rounded-lg shadow-lg w-full max-w-4xl">
        <h1 class="text-3xl font-bold mb-6">Thêm Sản Phẩm</h1>
        <form class="flex flex-col md:flex-row" action="Product" method="post" enctype="multipart/form-data" onsubmit="return validateForm();">
            <input type="hidden" name="action" value="add">
            <div class="md:w-1/2 md:pr-4">
                <div class="mb-4">
                    <label class="block text-gray-700 mb-2" for="product-name">Tên Sản Phẩm</label>
                    <input type="text" id="product-name" name="name" class="w-full p-2 border border-gray-300 rounded" placeholder="Nhập tên sản phẩm" required>
                </div>
                <div class="mb-4">
                    <label class="block text-gray-700 mb-2" for="product-description">Mô Tả</label>
                    <textarea id="product-description" name="description" class="w-full p-2 border border-gray-300 rounded" placeholder="Nhập mô tả sản phẩm" required></textarea>
                </div>
                <div class="mb-4">
                    <label class="block text-gray-700 mb-2" for="product-price">Giá</label>
                    <input type="number" id="product-price" name="price" class="w-full p-2 border border-gray-300 rounded" placeholder="Nhập giá" required>
                </div>
                <div class="mb-4">
                    <label class="block text-gray-700 mb-2" for="product-size">Kích Cỡ</label>
                    <input type="text" id="product-size" name="size" class="w-full p-2 border border-gray-300 rounded" placeholder="Nhập kích cỡ" required>
                </div>
                <div class="mb-4">
                    <label class="block text-gray-700 mb-2" for="product-stock">Số Lượng Tồn Kho</label>
                    <input type="number" id="product-stock" name="stock" class="w-full p-2 border border-gray-300 rounded" placeholder="Nhập số lượng tồn kho" required>
                </div>
                
                <div class="mb-4">
                    <!-- Danh mục sản phẩm -->
                    <label class="block text-gray-700 mb-2" for="product-category">Danh Mục</label>
                    <select id="product-category" name="categoryId" class="w-full p-2 border border-gray-300 rounded" required>
                        <option value="">Chọn Danh Mục</option>
                        <%
                            List<Categories> categoriesList = (List<Categories>) request.getAttribute("categoriesList");
                            if (categoriesList != null && !categoriesList.isEmpty()) {
                                for (Categories category : categoriesList) {
                        %>
                        <option value="<%= category.getCategoryId() %>"><%= category.getCname() %></option>
                        <%
                                }
                            } else {
                        %>
                        <option value="">Không có danh mục nào</option>
                        <%
                            }
                        %>
                    </select>
                </div>

                <div class="mb-4">
                    <label class="block text-gray-700 mb-2" for="product-status">Trạng Thái</label>
                    <select id="product-status" name="status" class="w-full p-2 border border-gray-300 rounded" required>
                        <option value="true">Có sẵn</option>
                        <option value="false">Hết hàng</option>
                    </select>
                </div>
                <button type="submit" class="w-full bg-orange-500 text-white py-2 rounded">Thêm Sản Phẩm</button>
            </div>

            <div class="md:w-1/2 md:pl-4 flex flex-col items-center">
                <div class="mb-4 w-full">
                    <label class="block text-gray-700 mb-2" for="product-image">Ảnh Sản Phẩm</label>
                    <div class="border-2 border-dashed border-gray-300 rounded-lg p-4 flex justify-center items-center">
                        <input type="file" id="product-image" name="image" class="w-full text-center" required accept="image/*">
                    </div>
                </div>
                <div class="w-full h-64 border border-gray-300 rounded-lg flex justify-center items-center">
                    <img id="image-preview" class="max-h-full max-w-full" src="#" alt="Image Preview" style="display: none;">
                </div>
            </div>
        </form>
        <br>
        <a href="Product">Quay lại danh sách sản phẩm</a>
    </div>

    <script>
        // Hiển thị preview ảnh
        document.getElementById('product-image').addEventListener('change', function(event) {
            const file = event.target.files[0];
            if (file) {
                const reader = new FileReader();
                reader.onload = function(e) {
                    const img = document.getElementById('image-preview');
                    img.src = e.target.result;
                    img.style.display = 'block';
                };
                reader.readAsDataURL(file);
            } else {
                document.getElementById('image-preview').style.display = 'none';
            }
        });
    </script>
</body>
</html>
