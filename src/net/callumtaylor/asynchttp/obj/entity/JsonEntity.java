package net.callumtaylor.asynchttp.obj.entity;

import java.io.UnsupportedEncodingException;

import org.apache.http.Header;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;

import com.google.gson.JsonElement;

/**
 * JsonEntity class used to create a StringEntity object with the content type
 * "application/json"
 *
 * @author callumtaylor
 */
public class JsonEntity extends StringEntity
{
	public JsonEntity(String s) throws UnsupportedEncodingException
	{
		super(s);
	}

	public JsonEntity(JsonElement e) throws UnsupportedEncodingException
	{
		super(e.toString());
	}

	@Override public Header getContentType()
	{
		return new BasicHeader("Content-Type", "application/json");
	}
}