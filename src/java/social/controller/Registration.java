package social.controller;

import java.io.IOException;
import java.io.PrintWriter;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.RollbackException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import social.data.DataUtils;
import social.model.Person;
import social.model.UserGroup;

/**
 *
 * @author Владимир
 */
@WebServlet(name = "Registration", urlPatterns = {"/registration"})
public class Registration extends HttpServlet {

    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html");
        getServletContext().getRequestDispatcher("/registration.jsp").forward(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        EntityManager manager = DataUtils.getEntityManager();
        manager.getTransaction().begin();

        UserGroup users;
        try {
            users = (UserGroup) manager.createQuery("from UserGroup ugroup where ugroup.name = 'Users'")
                    .getSingleResult();
        } catch (NoResultException exception) {
            users = new UserGroup("Users");
            manager.persist(users);
        }

        Person newcomer = new Person(request.getParameter("login"),
                request.getParameter("name"));

        newcomer.getGroups().add(users);

        String passwordError = DataUtils.validatePassword(request.getParameter("password"));
        if (passwordError == null) {
            newcomer.setPassword(DataUtils.encrypt(request.getParameter("password")));
        } else {
            request.setAttribute("passworderror", passwordError);
            manager.getTransaction().setRollbackOnly();
        }

        try {
            manager.persist(newcomer);
        } catch (ConstraintViolationException exception) {
            for (ConstraintViolation violation : exception.getConstraintViolations()) {
                request.setAttribute(violation.getPropertyPath().toString() + "error", violation.getMessage());
            }
        }

        try {
            manager.getTransaction().commit();
            if (request.getRemoteUser() != null) {
                request.logout();
            }
            request.login(request.getParameter("login"), request.getParameter("password"));
            response.sendRedirect(getServletContext().getContextPath());
        } catch (RollbackException exception) {
            if (exception.getCause() != null) {
                request.setAttribute("loginerror", "This login already exists.");
            }
            doGet(request, response);
        }
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Registration servlet";
    }

}