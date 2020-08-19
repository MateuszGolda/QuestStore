package service;

import DAO.CookieDAO;
import DAO.UserDAO;
import com.sun.net.httpserver.HttpExchange;
import model.Cookie;
import model.users.User;

import java.net.HttpCookie;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class CookieService {
    private CookieDAO cookieDAO;
    private UserDAO userDAO;

    public CookieService(CookieDAO cookieDAO, UserDAO userDAO){
        this.cookieDAO = cookieDAO;
        this.userDAO = userDAO;
    }

    public boolean checkIfCookieIsActive(String cookieSessionId) throws SQLException, ClassNotFoundException {
        List<Cookie> cookies = cookieDAO.getObjects("session_id", cookieSessionId);
        if(cookies.size() != 0){
            Cookie cookie =  cookies.get(0);
            Date currentDate = getCurrentDate();
            if((cookie.getExpireDate().getTime() - currentDate.getTime()) >= 0){
                return true;
            }
        }
        return false;
    }

    public Date getCurrentDate() {
        return new Date(Calendar.getInstance().getTime().getTime());
    }

    public Date getExpireDate (Date currentDate){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(currentDate);
        calendar.add(Calendar.DATE, 1);
        return new Date(calendar.getTimeInMillis());
    }

    public void setCookieNewExpireDate(String cookieSessionId) throws SQLException, ClassNotFoundException {
        Date currentDate = getCurrentDate();
        Date expireDate = getExpireDate(currentDate);

        Cookie cookie = cookieDAO.getObjects("session_id", cookieSessionId).get(0);
        cookie.setExpireDate(expireDate);
        cookieDAO.update(cookie);
    }

    public User getUserByCookieSessionId(String cookieSessionId) throws SQLException, ClassNotFoundException {
        Cookie cookie =  cookieDAO.getObjects("session_id", cookieSessionId).get(0);
        String userId = String.valueOf(cookie.getUserId());
        User user = userDAO.getObjects("id", userId).get(0);
        return user;
    }

    public void addCookie(Cookie cookie) throws SQLException, ClassNotFoundException {
        cookieDAO.insert(cookie);
    }

    public String generateCookieSessionId(HttpExchange httpExchange, String sessionCookieName) {
        UUID uuid = UUID.randomUUID();
        HttpCookie cookie = new HttpCookie(sessionCookieName, uuid.toString());
        httpExchange.getResponseHeaders().add("Set-Cookie", cookie.toString());
        return uuid.toString();
    }
}