<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <title>Add Category</title>
</head>
<body>
<h1>Add New Category</h1>
<form action="CategoryServlet" method="post">
    <input type="hidden" name="action" value="add">
    <label for="cname">Category Name:</label>
    <input type="text" name="cname" required>
    <button type="submit">Add</button>
</form>
<a href="CategoryServlet?action=list">Back to List</a>
</body>
</html>