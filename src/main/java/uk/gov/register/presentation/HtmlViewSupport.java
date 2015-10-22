package uk.gov.register.presentation;

import org.markdownj.MarkdownProcessor;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.UriBuilder;
import java.util.Map;

@SuppressWarnings("unused, methods of this class are used from html")
public class HtmlViewSupport {
    private static final MarkdownProcessor markdownProcessor = new MarkdownProcessor();

    public static String representationLink(HttpServletRequest httpServletRequest, String representation) {
        String representationUrlSuffix = representation.equals("html") ? "" : "." + representation;
        String requestPath = httpServletRequest.getRequestURI().replaceAll("([^\\.]+)(\\.[a-z]+)?", "$1" + representationUrlSuffix);

        UriBuilder uriBuilder = UriBuilder.fromPath(requestPath);

        Map<String, String[]> parameterMap = httpServletRequest.getParameterMap();
        parameterMap.keySet().stream().forEach(key ->
                        uriBuilder.queryParam(key, parameterMap.get(key)[0])
        );

        return uriBuilder.build().toString();
    }

    public static String fieldLink(String fieldName) {
        return new LinkValue("field", fieldName).link();
    }

    public static String publicBodyLink(String publicBodyId){
        return new LinkValue("public-body", publicBodyId).link();
    }
}
