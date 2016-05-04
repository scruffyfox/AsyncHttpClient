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
	 * Tests custom user agent is sent with request
	 * @throws InterruptedException
	 */
	public void testCustomUserAgent() throws InterruptedException
	{
		SyncHttpClient.userAgent = "custom-user-agent";

		JsonElement response = new SyncHttpClient<JsonElement>("http://httpbin.org/")
			.get("user-agent", new JsonResponseHandler());

		Assert.assertNotNull(response);
		Assert.assertEquals(response.getAsJsonObject().get("user-agent").getAsString(), "custom-user-agent");
	}

	/**
	 * Tests a basic GET request
	 * @throws InterruptedException
	 */
	public void testGet() throws InterruptedException
	{
		JsonElement response = new SyncHttpClient<JsonElement>("http://httpbin.org/")
			.get("get", new JsonResponseHandler());

		Assert.assertNotNull(response);
	}

	/**
	 * Tests 404 response
	 * @throws InterruptedException
	 */
	public void testGet404() throws InterruptedException
	{
		SyncHttpClient<JsonElement> client = new SyncHttpClient<>("http://httpbin.org/");
		JsonElement response = client.get("status/404", new JsonResponseHandler());

		Assert.assertNull(response);
		Assert.assertEquals(client.getConnectionInfo().responseCode, 404);
	}

	/**
	 * Tests gzip response
	 * @throws InterruptedException
	 */
	public void testGetGzipJson() throws InterruptedException
	{
		SyncHttpClient<JsonElement> client = new SyncHttpClient<>("http://httpbin.org/");
		JsonElement response = client.get("gzip", new JsonResponseHandler());

		Assert.assertNotNull(response);
	}

	/**
	 * Tests SSL connection response
	 * @throws InterruptedException
	 */
	public void testGetSslJson() throws InterruptedException
	{
		SyncHttpClient<JsonElement> client = new SyncHttpClient<>("https://httpbin.org/");
		JsonElement response = client.get("get", new JsonResponseHandler());

		Assert.assertNotNull(response);
	}

	/**
	 * Tests unsafe SSL connection response
	 * @throws InterruptedException
	 */
	public void testGetUnsafeSslJson() throws InterruptedException
	{
		SyncHttpClient<String> client = new SyncHttpClient<>("https://cruxoft.com/");
		client.setAllowAllSsl(true);
		String response = client.get("get", new StringResponseHandler());

		Assert.assertNotNull(response);
	}

	/**
	 * Tests auto 302 redirect
	 * @throws InterruptedException
	 */
	public void testGetRedirectJson() throws InterruptedException
	{
		SyncHttpClient<JsonElement> client = new SyncHttpClient<>("http://httpbin.org/");
		client.setAllowRedirect(true);
		JsonElement response = client.get("absolute-redirect/1", new JsonResponseHandler());

		Assert.assertNotNull(response);
		Assert.assertEquals(client.getConnectionInfo().responseCode, 200);
	}

	/**
	 * Tests no 302 redirect
	 * @throws InterruptedException
	 */
	public void testGetNoRedirect() throws InterruptedException
	{
		SyncHttpClient<String> client = new SyncHttpClient<>("http://httpbin.org/");
		client.setAllowRedirect(false);
		String response = client.get("status/302", new StringResponseHandler());

		Assert.assertEquals(client.getConnectionInfo().responseCode, 302);
	}
}
