package com.example.myapplication;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MySQLConnection {

    private static final String DB_URL = "jdbc:mysql://10.0.2.2:3306/atm";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "YOURPASSWORD";

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            throw new SQLException("Failed to establish database connection.");
        }
    }

    public static void addCard(String name, String qrValue, String password) {
        String sql = "INSERT INTO card (name, qr_value, password) VALUES (?, ?, ?)";

        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name);
            pstmt.setString(2, qrValue);
            pstmt.setString(3, password);

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("A new card has been inserted successfully!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static boolean validateCard(String qrValue, String password) {
        String sql = "SELECT * FROM card WHERE qr_value = ? AND password = ?";
        boolean isValid = false;

        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, qrValue);
            pstmt.setString(2, password);

            try (ResultSet rs = pstmt.executeQuery()) {
                isValid = rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return isValid;
    }

    public static String getBalanceByQrValue(String qrValue) {
        String sql = "SELECT balance FROM card WHERE qr_value = ?";
        String balance = null;

        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, qrValue);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    balance = rs.getString("balance");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return balance;
    }

    public static boolean subtractBalance(String qrValue, int amount) {
        String selectSql = "SELECT balance FROM card WHERE qr_value = ?";
        String updateSql = "UPDATE card SET balance = balance - ? WHERE qr_value = ?";
        boolean isSuccessful = false;

        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement selectStmt = conn.prepareStatement(selectSql);
             PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {

            // Select the current balance
            selectStmt.setString(1, qrValue);
            ResultSet rs = selectStmt.executeQuery();
            if (rs.next()) {
                int currentBalance = rs.getInt("balance");

                // Check if there are sufficient funds
                if (currentBalance >= amount) {
                    // Update the balance
                    updateStmt.setInt(1, amount);
                    updateStmt.setString(2, qrValue);
                    int rowsAffected = updateStmt.executeUpdate();
                    isSuccessful = rowsAffected > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return isSuccessful;
    }

    public static void addBalance(String qrValue, int amount) {
        String updateSql = "UPDATE card SET balance = balance + ? WHERE qr_value = ?";

        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {

            updateStmt.setInt(1, amount);
            updateStmt.setString(2, qrValue);
            updateStmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void addLoan(int loanAmount, int repaymentPeriod, String QRValue) {
        String sql = "INSERT INTO loan (loan_amount, repayment_period, qr_value) VALUES (?, ?, ?)";

        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, loanAmount);
            pstmt.setInt(2, repaymentPeriod);
            pstmt.setString(3, QRValue);

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("A new loan has been inserted successfully!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static List<Loan> getLoansByQRValue(String QRValue) {
        String sql = "SELECT * FROM loan WHERE qr_value = ?";
        List<Loan> loans = new ArrayList<>();

        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, QRValue);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("id");
                    int loanAmount = rs.getInt("loan_amount");
                    int repaymentPeriod = rs.getInt("repayment_period");
                    int installmentsPaid = rs.getInt("installments_paid");
                    String qrValue = rs.getString("qr_value");
                    loans.add(new Loan(id, loanAmount, repaymentPeriod, installmentsPaid, qrValue));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return loans;
    }

    public static void payLoan(int id) {
        String updateSql = "UPDATE loan SET installments_paid = installments_paid + 1 WHERE id = ?";

        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {

            updateStmt.setInt(1, id);
            updateStmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
