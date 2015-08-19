package uk.gov.register.thymeleaf;

import com.google.common.base.Throwables;
import io.dropwizard.views.View;
import org.thymeleaf.context.IContextExecutionInfo;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.context.WebContextExecutionInfo;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Calendar;

public class ThymeleafContext extends WebContext {
    public ThymeleafContext(ThymeleafView thymeleafView) {
        super(thymeleafView.getHttpServletRequest(), thymeleafView.getHttpServletResponse(), thymeleafView.getServletContext());

        try {
            initVariableFromViewProperties(thymeleafView);
        } catch (IntrospectionException | InvocationTargetException | IllegalAccessException e) {
            Throwables.propagate(e);
        }
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

    @Override
    protected IContextExecutionInfo buildContextExecutionInfo(
            final String templateName) {
        return new WebContextExecutionInfo(templateName, Calendar.getInstance());
    }

}
