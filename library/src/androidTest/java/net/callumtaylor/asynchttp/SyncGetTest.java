package net.callumtaylor.asynchttp;

import android.test.AndroidTestCase;

import com.google.gson.JsonElement;

import junit.framework.Assert;

import net.callumtaylor.asynchttp.response.JsonResponseHandler;
import net.callumtaylor.asynchttp.response.StringResponseHandler;

/**
 * @author Callum Taylor
 */
public class SyncGetTest extends AndroidTestCase
{
	@Override protected void setUp() throws Exception
	{
		super.setUp();
	}

	/**
	 * Tests a basic GET request
	 */
	public void testGet()
	{
		JsonElement response = new SyncHttpClient<JsonElement>("http://httpbin.org/")
			.get("get", new JsonResponseHandler());

		Assert.assertNotNull(response);
	}

	/**
	 * Tests 404 response
	 */
	public void testGet404()
	{
		SyncHttpClient<JsonElement> client = new SyncHttpClient<>("http://httpbin.org/");
		JsonElement response = client.get("status/404", new JsonResponseHandler());

		Assert.assertNull(response);
		Assert.assertEquals(client.getConnectionInfo().responseCode, 404);
	}

	/**
	 * Tests gzip response
	 */
	public void testGetGzipJson()
	{
		SyncHttpClient<JsonElement> client = new SyncHttpClient<>("http://httpbin.org/");
		JsonElement response = client.get("gzip", new JsonResponseHandler());

		Assert.assertNotNull(response);
	}

	/**
	 * Tests SSL connection response
	 */
	public void testGetSslJson()
	{
		SyncHttpClient<JsonElement> client = new SyncHttpClient<>("https://httpbin.org/");
		JsonElement response = client.get("get", new JsonResponseHandler());

		Assert.assertNotNull(response);
	}

	/**
	 * Tests unsafe SSL connection response
	 */
	public void testGetUnsafeSslJson()
	{
		SyncHttpClient<String> client = new SyncHttpClient<>("https://cruxoft.com/");
		client.setAllowAllSsl(true);
		String response = client.get("get", new StringResponseHandler());

		Assert.assertNotNull(response);
	}

	/**
	 * Tests auto 302 redirect
	 */
	public void testGetRedirectJson()
	{
		SyncHttpClient<JsonElement> client = new SyncHttpClient<>("http://httpbin.org/");
		client.setAllowRedirect(true);
		JsonElement response = client.get("absolute-redirect/1", new JsonResponseHandler());

		Assert.assertNotNull(response);
		Assert.assertEquals(client.getConnectionInfo().responseCode, 200);
	}

	/**
	 * Tests no 302 redirect
	 */
	public void testGetNoRedirect()
	{
		SyncHttpClient<String> client = new SyncHttpClient<>("http://httpbin.org/");
		client.setAllowRedirect(false);
		String response = client.get("status/302", new StringResponseHandler());

		Assert.assertEquals(client.getConnectionInfo().responseCode, 302);
	}
}
