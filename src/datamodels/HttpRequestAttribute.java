package datamodels;

import jakarta.servlet.http.HttpServletRequest;

public class HttpRequestAttribute<T> {
    private final Class<T> type;
    private final String name;
    public HttpRequestAttribute(Class<T> type, String name) {
        this.type = type;
        this.name = name;
    }

    public T get(HttpServletRequest request){
        //PARAMETER, not attribute. Apparently attribute it server side stuff, and parameter is the stuff you see at the end or a URL
        return type.cast(request.getParameter(name));
    }
}
