<%@page import="Beans.Categories"%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <title>Edit Category</title>
</head>
<body>
<h1>Edit Category</h1>
<%
    Categories category = (Categories) request.getAttribute("category");
%>
<form action="CategoryServlet" method="post">
    <input type="hidden" name="action" value="update">
    <input type="hidden" name="categoryId" value="<%= category.getCategoryId() %>">
    <label for="cname">Category Name:</label>
    <input type="text" name="cname" value="<%= category.getCname() %>" required>
    <button type="submit">Update</button>
</form>
<a href="CategoryServlet?action=list">Back to List</a>
</body>
</html>