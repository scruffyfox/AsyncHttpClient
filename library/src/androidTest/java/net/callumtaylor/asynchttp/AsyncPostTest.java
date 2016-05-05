package net.callumtaylor.asynchttp;

import android.test.AndroidTestCase;

import com.google.gson.JsonElement;

import junit.framework.Assert;

import net.callumtaylor.asynchttp.obj.InputStreamBody;
import net.callumtaylor.asynchttp.response.BasicResponseHandler;
import net.callumtaylor.asynchttp.response.JsonResponseHandler;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

/**
 * @author Callum Taylor
 */
public class AsyncPostTest extends AndroidTestCase
{
	final CountDownLatch signal = new CountDownLatch(1);

	@Override protected void setUp() throws Exception
	{
		super.setUp();
	}

	/**
	 * Tests a basic POST request
	 * @throws InterruptedException
	 */
	public void testPost() throws InterruptedException
	{
		new AsyncHttpClient("http://httpbin.org/")
			.post("post", new JsonResponseHandler()
			{
				@Override public void onFinish()
				{
					Assert.assertNotNull(getContent());

					signal.countDown();
				}
			});

		signal.await(1500, TimeUnit.MILLISECONDS);
	}

	/**
	 * Tests the response handler publish methods are called in chunks
	 * @throws InterruptedException
	 */
	public void testPostProgress() throws InterruptedException
	{
		byte[] postData = new byte[16384];
		for (int index = 0; index < postData.length; index++)
		{
			postData[index] = (byte)index;
		}

		RequestBody postBody = MultipartBody.create(MediaType.parse("application/octet-stream"), postData);

		new AsyncHttpClient("http://httpbin.org/")
			.post("post", postBody, new JsonResponseHandler()
			{
				@Override public void onByteChunkSent(byte[] chunk, long chunkLength, long totalProcessed, long totalLength)
				{
					Assert.assertNotNull(chunk);
					Assert.assertTrue(chunkLength > 0);
					Assert.assertEquals(16384, totalLength);
				}

				@Override public void onByteChunkSentProcessed(long totalProcessed, long totalLength)
				{
					Assert.assertTrue(totalProcessed >= 0);
					Assert.assertEquals(16384, totalLength);
				}

				@Override public void onFinish()
				{
					Assert.assertNotNull(getContent());

					signal.countDown();
				}
			});

		signal.await(1500, TimeUnit.MILLISECONDS);
	}

	/**
	 * Tests the sending an inputstream correctly sends
	 * @throws InterruptedException
	 */
	public void testPostStream() throws InterruptedException
	{
		// Simulate an input stream
		InputStream is = new ByteArrayInputStream("hello world".getBytes());

		RequestBody postBody = new MultipartBody.Builder()
			.addFormDataPart("test", "test.bin", InputStreamBody.create(MediaType.parse("application/octet-stream"), is))
			.build();

		new AsyncHttpClient("http://httpbin.org/")
			.post("post", postBody, new JsonResponseHandler()
			{
				@Override public void onByteChunkSent(byte[] chunk, long chunkLength, long totalProcessed, long totalLength)
				{
					Assert.assertNotNull(chunk);
					Assert.assertTrue(chunkLength > 0);
				}

				@Override public void onByteChunkSentProcessed(long totalProcessed, long totalLength)
				{
					Assert.assertTrue(totalProcessed >= 0);
				}

				@Override public void onFinish()
				{
					Assert.assertNotNull(getContent());

					signal.countDown();
				}
			});

		signal.await(1500, TimeUnit.SECONDS);
	}

	/**
	 * Tests response parses correctly from json
	 * @throws InterruptedException
	 */
	public void testPostJson() throws InterruptedException
	{
		RequestBody postBody = RequestBody.create(MediaType.parse("application/json"), "{\"test\":\"hello world\"}");

		new AsyncHttpClient("http://httpbin.org/")
			.post("post", postBody, new JsonResponseHandler()
			{
				@Override public void onFinish()
				{
					Assert.assertNotNull(getContent());
					Assert.assertTrue(getContent() instanceof JsonElement);

					signal.countDown();
				}
			});

		signal.await(1500, TimeUnit.MILLISECONDS);
	}

	/**
	 * Tests 404 response
	 * @throws InterruptedException
	 */
	public void testGet404() throws InterruptedException
	{
		new AsyncHttpClient("http://httpbin.org/")
			.post("status/404", new JsonResponseHandler()
			{
				@Override public void onFinish()
				{
					Assert.assertNull(getContent());
					Assert.assertEquals(404, getConnectionInfo().responseCode);

					signal.countDown();
				}
			});

		signal.await(1500, TimeUnit.MILLISECONDS);
	}

	/**
	 * Tests SSL connection response
	 * @throws InterruptedException
	 */
	public void testPostSslJson() throws InterruptedException
	{
		RequestBody postBody = RequestBody.create(MediaType.parse("application/json"), "{\"test\":\"hello world\"}");

		new AsyncHttpClient("https://httpbin.org/")
			.post("post", postBody, new JsonResponseHandler()
			{
				@Override public void onFinish()
				{
					Assert.assertNotNull(getContent());
					Assert.assertTrue(getContent() instanceof JsonElement);

					signal.countDown();
				}
			});

		signal.await(1500, TimeUnit.MILLISECONDS);
	}

	/**
	 * Tests auto 302 redirect
	 * @throws InterruptedException
	 */
	public void testPostRedirectJson() throws InterruptedException
	{
		AsyncHttpClient client = new AsyncHttpClient("http://httpbin.org/");
		client.setAllowRedirect(true);
		client.post("status/302", new JsonResponseHandler()
		{
			@Override public void onFinish()
			{
				Assert.assertNotNull(getContent());
				Assert.assertTrue(getContent() instanceof JsonElement);
				Assert.assertEquals(200, getConnectionInfo().responseCode);

				signal.countDown();
			}
		});

		signal.await(1500, TimeUnit.MILLISECONDS);
	}

	/**
	 * Tests no 302 redirect
	 * @throws InterruptedException
	 */
	public void testPostNoRedirect() throws InterruptedException
	{
		AsyncHttpClient client = new AsyncHttpClient("http://httpbin.org/");
		client.setAllowRedirect(false);
		client.post("status/302", new BasicResponseHandler()
		{
			@Override public void onFinish()
			{
				Assert.assertEquals(302, getConnectionInfo().responseCode);

				signal.countDown();
			}
		});

		signal.await(1500, TimeUnit.MILLISECONDS);
	}
}
