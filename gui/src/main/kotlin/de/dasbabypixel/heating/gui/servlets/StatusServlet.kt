package de.dasbabypixel.heating.gui.servlets

import jakarta.servlet.http.HttpServlet
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse

class StatusServlet : HttpServlet() {
    override fun doGet(
        req: HttpServletRequest,
        resp: HttpServletResponse
    ) {
        resp.status = HttpServletResponse.SC_OK
        resp.writer.write("Status OK")
    }
}
