package net.callumtaylor.asynchttp;

import android.test.AndroidTestCase;

import com.google.gson.JsonElement;

import junit.framework.Assert;

import net.callumtaylor.asynchttp.response.JsonResponseHandler;

import okhttp3.MediaType;
import okhttp3.RequestBody;

/**
 * @author Callum Taylor
 */
public class SyncDeleteTest extends AndroidTestCase
{
	@Override protected void setUp() throws Exception
	{
		super.setUp();
	}

	/**
	 * Tests a basic DELETE request
	 */
	public void testDelete()
	{
		JsonElement response = new SyncHttpClient<JsonElement>("http://httpbin.org/")
			.delete("delete", new JsonResponseHandler());

		Assert.assertNotNull(response);
	}

	/**
	 * Tests response parses correctly from json
	 */
	public void testDeleteJson()
	{
		RequestBody deleteBody = RequestBody.create(MediaType.parse("application/json"), "{\"test\":\"hello world\"}");

		SyncHttpClient<JsonElement> client = new SyncHttpClient<>("http://httpbin.org/");
		JsonElement response = client.delete("delete", deleteBody, new JsonResponseHandler());

		Assert.assertNotNull(response);
	}

	/**
	 * Tests 404 response
	 */
	public void testDelete404()
	{
		SyncHttpClient<JsonElement> client = new SyncHttpClient<>("http://httpbin.org/");
		JsonElement response = client.delete("status/404", new JsonResponseHandler());

		Assert.assertNull(response);
		Assert.assertEquals(404, client.getConnectionInfo().responseCode);
	}
}
