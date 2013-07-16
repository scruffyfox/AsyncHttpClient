package net.callumtaylor.asynchttp.obj.entity;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

import org.apache.http.HttpEntity;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.entity.mime.content.StringBody;

public class MultiPartEntity extends MultipartEntity
{
	/**
	 * Adds a basic KV part to the entity
	 *
	 * @param key The new value
	 * @param value The value value
	 */
	public void addPart(String key, String value)
	{
		try
		{
			addPart(key, new StringBody(value, Charset.forName("UTF-8")));
		}
		catch (UnsupportedEncodingException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Adds a new {@link HttpEntity} value to the entity. <b>note:</b> if you're
	 * looking to add a {@link FileEntity}, use {@link addFilePart}
	 *
	 * @deprecated Use {@link addFilePart(String, ContentBody} instead
	 * @param key
	 *            The key to add
	 * @param value
	 *            The {@link HttpEntity} part to add
	 */
	@Deprecated public void addPart(String key, HttpEntity value)
	{
		try
		{
			addPart(key, new InputStreamBody(value.getContent(), key));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Adds a new file based {@link HttpEntity} to the entity.
	 *
	 * @deprecated Use {@link addFilePart(String, ContentBody} instead
	 * @param key
	 *            The key to add
	 * @param value
	 *            The {@link HttpEntity} part to add
	 */
	@Deprecated public void addFilePart(String key, FileEntity value)
	{
		try
		{
			addFilePart(key, "untitled", value);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Adds a new file based {@link HttpEntity} to the entity.
	 *
	 * @deprecated Use {@link addFilePart(String, ContentBody} instead
	 * @param key
	 *            The key to add
	 * @param value
	 *            The {@link HttpEntity} part to add
	 */
	@Deprecated public void addFilePart(String key, String filename, FileEntity value)
	{
		try
		{
			addPart(key, new InputStreamBody(value.getContent(), filename));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}