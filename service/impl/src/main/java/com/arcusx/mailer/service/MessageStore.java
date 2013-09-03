
package com.arcusx.mailer.service;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.sql.DataSource;

import com.arcusx.mailer.MimeMessageData;

public class MessageStore
{
	private static final int MAX_FAILURE_COUNT = 10;

	private DataSource dataSource;

	public MessageStore(DataSource dataSource)
	{
		this.dataSource = dataSource;
	}

	public List<Long> selectUndeliveredMessageIds() throws SQLException
	{
		return getSqlTemplate().select(
				"select m.message_id from mailer.message m where sent_date is null and failure_count < ?",
				new Object[] { Long.valueOf(MAX_FAILURE_COUNT)}, new LongsResultSetExtractor());
	}

	public MimeMessageData selectMessage(Long messageId) throws IOException, SQLException
	{
		String messageBody = getSqlTemplate().select("select m.body from mailer.message m where m.message_id = ?",
				new Object[] { messageId}, new StringResultSetExtractor());

		byte[] mimeMessageBytes = messageBody.getBytes("ASCII");
		return new MimeMessageData(messageId, mimeMessageBytes);
	}

	public void markMessageSent(Long messageId) throws SQLException
	{
		if (messageId == null)
			throw new IllegalArgumentException("Message id may not be null.");

		getSqlTemplate().update("update mailer.message set sent_date = ? where message_id = ? and sent_date is null",
				new Object[] { new Date(), messageId});
	}

	public void countMessageSendFailure(Long messageId) throws SQLException
	{
		if (messageId == null)
			throw new IllegalArgumentException("Message id may not be null.");

		getSqlTemplate()
				.update("update mailer.message set failure_count = failure_count +1 where message_id = ? and sent_date is null",
						new Object[] { messageId});
	}

	public void storeMessage(byte[] mimeMessageBytes) throws IOException, SQLException
	{
		getSqlTemplate()
				.update("insert into mailer.message ( message_id, body, failure_count, sent_date ) values ( nextval('mailer.message_seq'), ?, 0, null )",
						new Object[] { new String(mimeMessageBytes, "ASCII")});

	}

	private SqlTemplate getSqlTemplate()
	{
		return new SqlTemplate(this.dataSource);
	}

	private static class LongsResultSetExtractor implements SqlTemplate.ResultSetExtractor<List<Long>>
	{
		public List<Long> extract(ResultSet rSet) throws SQLException
		{
			List<Long> values = new ArrayList<Long>();
			while (rSet.next())
			{
				long x = rSet.getLong(1);
				if (rSet.wasNull())
					values.add(null);
				else
					values.add(x);
			}

			return values;
		}
	}

	private static class StringResultSetExtractor implements SqlTemplate.ResultSetExtractor<String>
	{
		public String extract(ResultSet rSet) throws SQLException
		{
			if (!rSet.next())
				return null;

			String value = rSet.getString(1);

			if (rSet.next())
				throw new SQLException("Ambigious result set.");

			return value;
		}
	}

}
