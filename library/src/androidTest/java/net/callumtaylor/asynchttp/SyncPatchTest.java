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
public class SyncPatchTest extends AndroidTestCase
{
	@Override protected void setUp() throws Exception
	{
		super.setUp();
	}

	/**
	 * Tests a basic PATCH request
	 */
	public void testPatch()
	{
		JsonElement response = new SyncHttpClient<JsonElement>("http://httpbin.org/")
			.patch("patch", new JsonResponseHandler());

		Assert.assertNotNull(response);
	}

	/**
	 * Tests response parses correctly from json
	 */
	public void testPatchJson()
	{
		RequestBody patchBody = RequestBody.create(MediaType.parse("application/json"), "{\"test\":\"hello world\"}");

		SyncHttpClient<JsonElement> client = new SyncHttpClient<>("http://httpbin.org/");
		JsonElement response = client.patch("patch", patchBody, new JsonResponseHandler());

		Assert.assertNotNull(response);
	}

	/**
	 * Tests 404 response
	 */
	public void testPatch404()
	{
		SyncHttpClient<JsonElement> client = new SyncHttpClient<>("http://httpbin.org/");
		JsonElement response = client.patch("status/404", new JsonResponseHandler());

		Assert.assertNull(response);
		Assert.assertEquals(client.getConnectionInfo().responseCode, 404);
	}

	/**
	 * Tests auto 302 redirect
	 */
	public void testPatchRedirectJson()
	{
		SyncHttpClient<JsonElement> client = new SyncHttpClient<>("http://httpbin.org/");
		client.setAllowRedirect(true);
		JsonElement response = client.patch("absolute-redirect/1", new JsonResponseHandler());

		Assert.assertNotNull(response);
		Assert.assertEquals(client.getConnectionInfo().responseCode, 200);
	}

	/**
	 * Tests no 302 redirect
	 */
	public void testPatchNoRedirect()
	{
		SyncHttpClient<String> client = new SyncHttpClient<>("http://httpbin.org/");
		client.setAllowRedirect(false);
		String response = client.patch("status/302", new StringResponseHandler());

		Assert.assertEquals(client.getConnectionInfo().responseCode, 302);
	}
}
