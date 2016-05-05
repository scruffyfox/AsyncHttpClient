package net.callumtaylor.asynchttp;

import android.test.AndroidTestCase;

import com.google.gson.JsonElement;

import junit.framework.Assert;

import net.callumtaylor.asynchttp.response.JsonResponseHandler;
import net.callumtaylor.asynchttp.response.StringResponseHandler;

import okhttp3.MediaType;
import okhttp3.RequestBody;

/**
 * @author Callum Taylor
 */
public class SyncPostTest extends AndroidTestCase
{
	@Override protected void setUp() throws Exception
	{
		super.setUp();
	}

	/**
	 * Tests a basic POST request
	 */
	public void testPost()
	{
		JsonElement response = new SyncHttpClient<JsonElement>("http://httpbin.org/")
			.post("post", new JsonResponseHandler());

		Assert.assertNotNull(response);
	}

	/**
	 * Tests response parses correctly from json
	 */
	public void testPostJson()
	{
		RequestBody postBody = RequestBody.create(MediaType.parse("application/json"), "{\"test\":\"hello world\"}");

		SyncHttpClient<JsonElement> client = new SyncHttpClient<>("http://httpbin.org/");
		JsonElement response = client.post("post", postBody, new JsonResponseHandler());

		Assert.assertNotNull(response);
	}

	/**
	 * Tests 404 response
	 */
	public void testPost404()
	{
		SyncHttpClient<JsonElement> client = new SyncHttpClient<>("http://httpbin.org/");
		JsonElement response = client.post("status/404", new JsonResponseHandler());

		Assert.assertNull(response);
		Assert.assertEquals(404, client.getConnectionInfo().responseCode);
	}

	/**
	 * Tests auto 302 redirect
	 */
	public void testPostRedirectJson()
	{
		SyncHttpClient<JsonElement> client = new SyncHttpClient<>("http://httpbin.org/");
		client.setAllowRedirect(true);
		JsonElement response = client.post("status/302", new JsonResponseHandler());

		Assert.assertNotNull(response);
		Assert.assertEquals(200, client.getConnectionInfo().responseCode);
	}

	/**
	 * Tests no 302 redirect
	 */
	public void testPostNoRedirect()
	{
		SyncHttpClient<String> client = new SyncHttpClient<>("http://httpbin.org/");
		client.setAllowRedirect(false);
		String response = client.post("status/302", new StringResponseHandler());

		Assert.assertEquals(302, client.getConnectionInfo().responseCode);
	}
}
