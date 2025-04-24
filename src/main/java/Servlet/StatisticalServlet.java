package Servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;

import Beans.Statistical;
import Utils.DBStatistical;

/**
 * Servlet implementation class StatisticalServlet
 */
@WebServlet("/Statistical")
public class StatisticalServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    public StatisticalServlet() {
        super();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.getRequestDispatcher("thongke.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String tuNgay = request.getParameter("tuNgay");
        String denNgay = request.getParameter("denNgay");

        DBStatistical dbStatistical = new DBStatistical();
        ArrayList<Statistical> danhSachThongKe = dbStatistical.getThongKe(tuNgay, denNgay);

        request.setAttribute("danhSachThongKe", danhSachThongKe);
        request.getRequestDispatcher("thongke.jsp").forward(request, response);
    }
}

