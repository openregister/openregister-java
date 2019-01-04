package uk.gov.register.thymeleaf;

import io.dropwizard.views.View;
import org.thymeleaf.context.AbstractContext;
import org.thymeleaf.context.IWebContext;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ThymeleafContext extends AbstractContext implements IWebContext {
    private final HttpServletRequest request;
    private final HttpServletResponse response;
    private final ServletContext servletContext;

    public ThymeleafContext(ThymeleafView thymeleafView) {
        super();
        this.request = thymeleafView.getHttpServletRequest();
        this.response = thymeleafView.getHttpServletResponse();
        this.servletContext = thymeleafView.getServletContext();

        try {
            initVariableFromViewProperties(thymeleafView);
        } catch (IntrospectionException | InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public HttpServletRequest getRequest() {
        return this.request;
    }

    public HttpSession getSession() {
        return this.request.getSession(false);
    }

    public HttpServletResponse getResponse() {
        return this.response;
    }

    public ServletContext getServletContext() {
        return this.servletContext;
    }

    private void initVariableFromViewProperties(View view) throws IntrospectionException, InvocationTargetException, IllegalAccessException {

        PropertyDescriptor[] propertyDescs = Introspector.getBeanInfo(
                view.getClass(), View.class).getPropertyDescriptors();

        for (PropertyDescriptor desc : propertyDescs) {

            String propName = desc.getDisplayName();
            Method method = desc.getReadMethod();

            setVariable(propName, method.invoke(view));

        }
    }
}
