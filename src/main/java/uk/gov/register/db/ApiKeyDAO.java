package uk.gov.register.db;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.stringtemplate.UseStringTemplate3StatementLocator;

@UseStringTemplate3StatementLocator
public interface ApiKeyDAO {
	@SqlUpdate("insert into public.api_key_usage(api_key, register, path, query_params, timestamp) values (:apiKey, :register, :path, :queryParams, :timestamp)")
	void insertApiKeyUsage(@Bind("apiKey") String apiKey, @Bind("register") String register, @Bind("path") String path, @Bind("queryParams") String queryParams, @Bind("timestamp") long timestamp);
}