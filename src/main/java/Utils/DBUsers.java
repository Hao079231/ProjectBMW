package Utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import Beans.Users;

public class DBUsers {
    // Insert new user with hashed password and salt
    public static void insert(Connection conn, Users user) {
        String query = "INSERT INTO Users (username, password, email, phone, address, role, salt) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            String salt = PasswordUtils.generateSalt();
            String hashedPassword = PasswordUtils.hashPassword(user.getPassword(), salt);
            stmt.setString(1, user.getUsername());
            stmt.setString(2, hashedPassword);
            stmt.setString(3, user.getEmail());
            stmt.setString(4, user.getPhone());
            stmt.setString(5, user.getAddress());
            stmt.setString(6, user.getRole());
            stmt.setString(7, salt);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error inserting user: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Get user by ID
    public static Users getUserById(Connection conn, int userId) {
        String sql = "SELECT * FROM Users WHERE user_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Users(
                            rs.getInt("user_id"),
                            rs.getString("username"),
                            rs.getString("password"),
                            rs.getString("email"),
                            rs.getString("phone"),
                            rs.getString("address"),
                            rs.getString("role"),
                            rs.getString("salt")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving user by ID: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    // Get all users
    public static List<Users> getAllUsers(Connection conn) {
        List<Users> usersList = new ArrayList<>();
        String sql = "SELECT * FROM Users WHERE role = 'user'";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Users user = new Users(
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("email"),
                        rs.getString("phone"),
                        rs.getString("address"),
                        rs.getString("role"),
                        rs.getString("salt")
                );
                usersList.add(user);
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving users: " + e.getMessage());
            e.printStackTrace();
        }
        return usersList;
    }

    // Update user (excluding password)
    public static void updateUser(Connection conn, Users user) {
        String sql = "UPDATE Users SET username = ?, email = ?, phone = ?, address = ? WHERE user_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, user.getPhone());
            stmt.setString(4, user.getAddress());
            stmt.setInt(5, user.getUserId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating user: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Delete user
    public static void deleteUser(Connection conn, int userId) {
        String sql = "DELETE FROM Users WHERE user_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error deleting user: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Login with hashed password verification
    public static Users login(Connection conn, String username, String password) {
        String sql = "SELECT * FROM Users WHERE username = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String storedHash = rs.getString("password");
                    String salt = rs.getString("salt");
                    if (PasswordUtils.verifyPassword(password, storedHash, salt)) {
                        return new Users(
                                rs.getInt("user_id"),
                                rs.getString("username"),
                                storedHash,
                                rs.getString("email"),
                                rs.getString("phone"),
                                rs.getString("address"),
                                rs.getString("role"),
                                salt
                        );
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error during login: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    // Get user by username
    public static Users getUserByUsername(Connection conn, String username) {
        String sql = "SELECT * FROM Users WHERE username = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Users(
                            rs.getInt("user_id"),
                            rs.getString("username"),
                            rs.getString("password"),
                            rs.getString("email"),
                            rs.getString("phone"),
                            rs.getString("address"),
                            rs.getString("role"),
                            rs.getString("salt")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Error checking user: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    // Search users by username
    public static List<Users> searchUsersByUsername(Connection conn, String searchQuery) {
        List<Users> usersList = new ArrayList<>();
        String sql = "SELECT * FROM Users WHERE role = 'user' AND username LIKE ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "%" + searchQuery + "%");
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Users user = new Users(
                            rs.getInt("user_id"),
                            rs.getString("username"),
                            rs.getString("password"),
                            rs.getString("email"),
                            rs.getString("phone"),
                            rs.getString("address"),
                            rs.getString("role"),
                            rs.getString("salt")
                    );
                    usersList.add(user);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error searching users: " + e.getMessage());
            e.printStackTrace();
        }
        return usersList;
    }

    // Get all emails for validation
    public static List<String> getAllEmail(Connection conn) {
        List<String> emails = new ArrayList<>();
        String sql = "SELECT email FROM Users";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                emails.add(rs.getString("email"));
            }
        } catch (SQLException e) {
            System.err.println("Error getting emails: " + e.getMessage());
            e.printStackTrace();
        }
        return emails; // Corrected to return emails
    }

    // Update user password by email
    public static void updateUserPasswordByEmail(Connection conn, String password, String email) {
        String sql = "UPDATE Users SET password = ?, salt = ? WHERE email = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            String salt = PasswordUtils.generateSalt();
            String hashedPassword = PasswordUtils.hashPassword(password, salt);
            stmt.setString(1, hashedPassword);
            stmt.setString(2, salt);
            stmt.setString(3, email);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating password: " + e.getMessage());
            e.printStackTrace();
        }
    }
}