package uk.gov.register.filters;

import uk.gov.register.configuration.DatabaseManager;
import uk.gov.register.db.ApiKeyDAO;

import javax.inject.Inject;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.time.Instant;

@Provider
public class ApiKeyRecorderFilter implements ContainerRequestFilter {
	private final DatabaseManager databaseManager;

	@Inject
	public ApiKeyRecorderFilter(DatabaseManager databaseManager) {
		this.databaseManager = databaseManager;
	}
	
	@Override
	public void filter(ContainerRequestContext requestContext) throws IOException {
		String register = requestContext.getHeaderString("Host");
		String apiKeyHeader = requestContext.getHeaderString("API_KEY");
		if (apiKeyHeader == null) {
			apiKeyHeader = "not_present";
		}
		
		String path = requestContext.getUriInfo().getPath().toString();
		String queryParams = requestContext.getUriInfo().getQueryParameters().toString();
		
		// Write to DB
		ApiKeyDAO apiKeyDAO = databaseManager.getDbi().onDemand(ApiKeyDAO.class);
		apiKeyDAO.insertApiKeyUsage(apiKeyHeader, register, path, queryParams, Instant.now().getEpochSecond());
	}
}
