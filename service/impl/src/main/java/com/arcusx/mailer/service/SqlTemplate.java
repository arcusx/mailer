
package com.arcusx.mailer.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.log4j.Logger;

public class SqlTemplate
{
	private static Logger logger = Logger.getLogger(SqlTemplate.class);

	private DataSource dataSource;

	public SqlTemplate(DataSource dataSource)
	{
		this.dataSource = dataSource;
	}

	public void update(String sql, Object[] args) throws SQLException
	{
		select(sql, args, null);
	}

	public <T> T select(String sql, Object[] args, ResultSetExtractor<T> extractor) throws SQLException
	{
		Connection conn = null;
		PreparedStatement pStmt = null;
		ResultSet rSet = null;
		try
		{
			conn = this.dataSource.getConnection();
			pStmt = conn.prepareStatement(sql);
			for (int i = 0; i < args.length; ++i)
			{
				if (args[i] instanceof String)
					pStmt.setString(i + 1, (String) args[i]);
				else if (args[i] instanceof Long)
					pStmt.setLong(i + 1, (Long) args[i]);
				else
					throw new IllegalArgumentException("Unhandled arg " + args[i] + ".");
			}
			T result = null;
			rSet = pStmt.executeQuery();
			if (extractor != null)
				result = extractor.extract(rSet);
			return result;
		}
		finally
		{
			if (rSet != null)
			{
				try
				{
					rSet.close();
				}
				catch (Exception ex)
				{
					logger.error("Closing failed.", ex);
				}
			}
			if (pStmt != null)
			{
				try
				{
					pStmt.close();
				}
				catch (Exception ex)
				{
					logger.error("Closing failed.", ex);
				}
			}
			if (conn != null)
			{
				try
				{
					conn.close();
				}
				catch (Exception ex)
				{
					logger.error("Closing failed.", ex);
				}
			}
		}
	}

	public static interface ResultSetExtractor<T>
	{
		T extract(ResultSet rSet) throws SQLException;
	}
}
