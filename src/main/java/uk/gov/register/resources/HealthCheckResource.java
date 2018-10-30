package uk.gov.register.resources;

import com.codahale.metrics.health.HealthCheck;
import com.codahale.metrics.health.HealthCheckRegistry;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/")
public class HealthCheckResource {
	private final HealthCheckRegistry healthCheckRegistry;
	
	@Inject
	public HealthCheckResource(HealthCheckRegistry healthCheckRegistry) {
		this.healthCheckRegistry = healthCheckRegistry;
	}
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/healthcheck")
	public Response getCurrentHealth() {
		HealthCheck.Result result = healthCheckRegistry.runHealthCheck("openregister_java");
		Response.Status status = result.isHealthy() ? Response.Status.OK : Response.Status.INTERNAL_SERVER_ERROR;
		
		return Response.status(status).entity(result).build();
	}
}
