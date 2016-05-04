package net.callumtaylor.asynchttp;

import android.test.AndroidTestCase;

import com.google.gson.JsonElement;

import junit.framework.Assert;

import net.callumtaylor.asynchttp.response.JsonResponseHandler;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

/**
 * @author Callum Taylor
 */
public class AsyncPatchTest extends AndroidTestCase
{
	final CountDownLatch signal = new CountDownLatch(1);

	@Override protected void setUp() throws Exception
	{
		super.setUp();
	}

	/**
	 * Tests a basic PATCH request
	 * @throws InterruptedException
	 */
	public void testPatch() throws InterruptedException
	{
		new AsyncHttpClient("http://httpbin.org/")
			.patch("patch", new JsonResponseHandler()
			{
				@Override public void onFinish(boolean failed)
				{
					Assert.assertNotNull(getContent());

					signal.countDown();
				}
			});

		signal.await(1500, TimeUnit.SECONDS);
	}

	/**
	 * Tests the response handler publish methods are called in chunks
	 * @throws InterruptedException
	 */
	public void testPatchProgress() throws InterruptedException
	{
		byte[] patchData = new byte[16384];
		for (int index = 0; index < patchData.length; index++)
		{
			patchData[index] = (byte)index;
		}

		RequestBody patchBody = MultipartBody.create(MediaType.parse("application/octet-stream"), patchData);

		new AsyncHttpClient("http://httpbin.org/")
			.patch("patch", patchBody, new JsonResponseHandler()
			{
				@Override public void onPublishedUploadProgress(byte[] chunk, long chunkLength, long totalProcessed, long totalLength)
				{
					Assert.assertNotNull(chunk);
					Assert.assertTrue(chunkLength > 0);
					Assert.assertEquals(totalLength, 16384);
				}

				@Override public void onPublishedUploadProgressUI(long totalProcessed, long totalLength)
				{
					Assert.assertTrue(totalProcessed >= 0);
					Assert.assertEquals(totalLength, 16384);
				}

				@Override public void onFinish(boolean failed)
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
	public void testPatchJson() throws InterruptedException
	{
		RequestBody patchBody = RequestBody.create(MediaType.parse("application/json"), "{\"test\":\"hello world\"}");

		new AsyncHttpClient("http://httpbin.org/")
			.patch("patch", patchBody, new JsonResponseHandler()
			{
				@Override public void onFinish(boolean failed)
				{
					Assert.assertNotNull(getContent());
					Assert.assertTrue(getContent() instanceof JsonElement);

					signal.countDown();
				}
			});

		signal.await(1500, TimeUnit.SECONDS);
	}

	/**
	 * Tests 404 response
	 * @throws InterruptedException
	 */
	public void testGet404() throws InterruptedException
	{
		new AsyncHttpClient("http://httpbin.org/")
			.patch("status/404", new JsonResponseHandler()
			{
				@Override public void onFinish(boolean failed)
				{
					Assert.assertNull(getContent());
					Assert.assertTrue(failed);
					Assert.assertEquals(getConnectionInfo().responseCode, 404);

					signal.countDown();
				}
			});

		signal.await(1500, TimeUnit.SECONDS);
	}

	/**
	 * Tests SSL connection response
	 * @throws InterruptedException
	 */
	public void testPatchSslJson() throws InterruptedException
	{
		RequestBody patchBody = RequestBody.create(MediaType.parse("application/json"), "{\"test\":\"hello world\"}");

		new AsyncHttpClient("https://httpbin.org/")
			.patch("patch", patchBody, new JsonResponseHandler()
			{
				@Override public void onFinish(boolean failed)
				{
					Assert.assertNotNull(getContent());
					Assert.assertTrue(getContent() instanceof JsonElement);

					signal.countDown();
				}
			});

		signal.await(1500, TimeUnit.SECONDS);
	}
}
