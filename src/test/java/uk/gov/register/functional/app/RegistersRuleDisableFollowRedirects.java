package uk.gov.register.functional.app;

import org.glassfish.jersey.client.ClientProperties;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.skife.jdbi.v2.Handle;

public class RegistersRuleDisableFollowRedirects extends RegisterRule
{
   @Override
   public Statement apply(Statement base, Description description) {
       Statement me = new Statement() {
           @Override
           public void evaluate() throws Throwable {
               client = clientBuilder().build("test client")
                       .property(ClientProperties.FOLLOW_REDIRECTS, Boolean.FALSE);
               base.evaluate();
               handles.forEach(Handle::close);
           }
       };
       return wholeRule.apply(me, description);
   }
}
