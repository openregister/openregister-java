package uk.gov.migration;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

public class TempDataSource implements DataSource {
	private Connection connection;

	private PrintWriter out;

	public TempDataSource(Connection connection) {
		this.connection = connection;
	}

	@Override
	public Connection getConnection() throws SQLException {
		return connection;
	}

	@Override
	public Connection getConnection(String username, String password) throws SQLException {
		return connection;
	}

	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException {
		return null;
	}

	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return false;
	}

	@Override
	public PrintWriter getLogWriter() throws SQLException {
		return out;
	}

	@Override
	public void setLogWriter(PrintWriter out) throws SQLException {
		this.out = out;
	}

	@Override
	public void setLoginTimeout(int seconds) throws SQLException {

	}

	@Override
	public int getLoginTimeout() throws SQLException {
		return 1;
	}

	@Override
	public Logger getParentLogger() throws SQLFeatureNotSupportedException {
		return null;
	}
}