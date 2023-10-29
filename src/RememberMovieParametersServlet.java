import com.google.gson.JsonObject;
import datamodels.SessionMovieListParameters;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet(name = "RememberMovieParametersServlet", urlPatterns = "/api/remember-movie-parameters")
public class RememberMovieParametersServlet extends HttpServlet {
    public static final String TAG = "RememberMovieParametersServlet";
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession();
        String parameters = (String) session.getAttribute(SessionMovieListParameters.sessionKeyName);
        String backPage = (String) session.getAttribute(SessionMovieListParameters.sessionKeyBackPage);

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("parameters",parameters);
        jsonObject.addProperty("backpage",backPage);
        req.getServletContext().log(TAG +": The parameters that were saved were retrieved in GET are " + parameters);
        req.getServletContext().log(TAG +": The backpage that were saved were retrieved in GET are " + backPage);
        resp.getWriter().write(jsonObject.toString());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession();
        String result = SessionMovieListParameters.parseParametersStringFromRequest(req);
        session.setAttribute(SessionMovieListParameters.sessionKeyName,result);
        req.getServletContext().log(TAG +": The parameters that were saved were " + result);
    }
}
