package net.callumtaylor.asynchttp

import android.net.Uri
import android.support.test.runner.AndroidJUnit4
import com.google.gson.JsonElement
import net.callumtaylor.TestHelper
import net.callumtaylor.asynchttp.obj.Request
import net.callumtaylor.asynchttp.processor.ByteProcessor
import net.callumtaylor.asynchttp.processor.JsonProcessor
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Tests the AsyncHttpClient GET requests
 */
@RunWith(AndroidJUnit4::class)
class AsyncGetTest
{
	/**
	 * Tests a simple GET request
	 */
	@Test
	fun testSimpleGet()
	{
		TestHelper.createAsyncLatch().also { latch ->
			AsyncHttpClient("https://httpbin.org/get")
				.get<JsonElement>(
					processor = JsonProcessor(),
					response = { response ->
						Assert.assertNotNull(response)

						Assert.assertNotNull(response.body)
						Assert.assertEquals(response.request.path, response.body!!.asJsonObject["url"].asString)

						TestHelper.stopLatch(latch)
					}
				)

			TestHelper.beginLatch(latch)
		}
	}

	/**
	 * Tests a bad ssl connection throws empty response
	 */
	@Test
	fun testBadSsl()
	{
		TestHelper.createAsyncLatch().also { latch ->
			AsyncHttpClient("https://expired.badssl.com/")
				.get(
					response = { response ->
						Assert.assertNotNull(response)

						Assert.assertEquals(0, response.code)

						TestHelper.stopLatch(latch)
					}
				)

			TestHelper.beginLatch(latch)
		}
	}

	/**
	 * Tests a bad ssl connection is ignored with arguments
	 */
	@Test
	fun testAcceptBadSsl()
	{
		TestHelper.createAsyncLatch().also { latch ->
			AsyncHttpClient("https://untrusted-root.badssl.com/")
				.get(
					request = Request(allowAllSSl = true),
					response = { response ->
						Assert.assertNotNull(response)

						Assert.assertNotNull(response.body)
						Assert.assertEquals(200, response.code)

						TestHelper.stopLatch(latch)
					}
				)

			TestHelper.beginLatch(latch)
		}
	}

	/**
	 * Tests overriding and sending extra headers with given request
	 */
	@Test
	fun testGetHeaders()
	{
		TestHelper.createAsyncLatch().also { latch ->
			AsyncHttpClient("https://httpbin.org/get")
				.get(
					request = Request(
						headers = mutableMapOf(
							Pair("X-Content-Type", "text/plain"),
							Pair("User-Agent", "AsyncHttpClient-Test")
						)
					),
					processor = JsonProcessor(),
					response = { response ->
						Assert.assertNotNull(response)

						Assert.assertNotNull(response.headers)
						Assert.assertNotNull(response.body)

						Assert.assertEquals("AsyncHttpClient-Test", response.body!!.asJsonObject["headers"].asJsonObject["User-Agent"].asString)
						Assert.assertEquals(response.request.headers["User-Agent"], response.body!!.asJsonObject["headers"].asJsonObject["User-Agent"].asString)

						Assert.assertEquals("text/plain", response.body!!.asJsonObject["headers"].asJsonObject["X-Content-Type"].asString)
						Assert.assertEquals(response.request.headers["X-Content-Type"], response.body!!.asJsonObject["headers"].asJsonObject["X-Content-Type"].asString)

						TestHelper.stopLatch(latch)
					}
				)

			TestHelper.beginLatch(latch)
		}
	}

	/**
	 * Tests overriding and sending extra headers with given request
	 */
	@Test
	fun testGetFromUri()
	{
		TestHelper.createAsyncLatch().also { latch ->
			AsyncHttpClient(Uri.parse("https://httpbin.org"))
				.get(
					request = Request(
						path = "get"
					),
					processor = JsonProcessor(),
					response = { response ->
						Assert.assertNotNull(response)

						Assert.assertNotNull(response.body)
						Assert.assertEquals(response.request.path, response.body!!.asJsonObject["url"].asString)

						TestHelper.stopLatch(latch)
					}
				)

			TestHelper.beginLatch(latch)
		}
	}

	/**
	 * Tests GET request with query parameters
	 */
	@Test
	fun testGetWithQuery()
	{
		TestHelper.createAsyncLatch().also { latch ->
			AsyncHttpClient(Uri.parse("https://httpbin.org"))
				.get(
					request = Request(
						path = "get",
						queryParams = mutableMapOf(
							Pair("first", "value"),
							Pair("second", "with spaces")
						)
					),
					processor = JsonProcessor(),
					response = { response ->
						Assert.assertNotNull(response)

						Assert.assertNotNull(response.body)
						Assert.assertEquals("value", response.body!!.asJsonObject["args"].asJsonObject["first"].asString)
						Assert.assertEquals("with spaces", response.body!!.asJsonObject["args"].asJsonObject["second"].asString)

						TestHelper.stopLatch(latch)
					}
				)

			TestHelper.beginLatch(latch)
		}
	}

	/**
	 * Tests GET request with gziped response decodes correctly
	 */
	@Test
	fun testGetGzip()
	{
		TestHelper.createAsyncLatch().also { latch ->
			AsyncHttpClient(Uri.parse("https://httpbin.org"))
				.get(
					request = Request(
						path = "gzip",
						headers = mutableMapOf(
							Pair("Accept-Encoding", "gzip, deflate")
						)
					),
					processor = JsonProcessor(),
					response = { response ->
						Assert.assertNotNull(response)

						Assert.assertNotNull(response.body)
						Assert.assertTrue(response.body!!.asJsonObject["gzipped"].asBoolean)

						Assert.assertEquals("gzip", response.headers["Content-Encoding"])
						Assert.assertEquals("application/json", response.headers["Content-Type"])

						TestHelper.stopLatch(latch)
					}
				)

			TestHelper.beginLatch(latch)
		}
	}

	/**
	 * Tests GET request follows redirect
	 */
	@Test
	fun testGetFollowRedirect()
	{
		TestHelper.createAsyncLatch().also { latch ->
			AsyncHttpClient(Uri.parse("https://httpbin.org"))
				.get(
					request = Request(
						path = "redirect/1"
					),
					processor = JsonProcessor(),
					response = { response ->
						Assert.assertNotNull(response)

						Assert.assertNotNull(response.body)
						Assert.assertEquals("https://httpbin.org/get", response.body!!.asJsonObject["url"].asString)
						Assert.assertEquals(200, response.code)

						TestHelper.stopLatch(latch)
					}
				)

			TestHelper.beginLatch(latch)
		}
	}

	/**
	 * Tests GET request stops at 302
	 */
	@Test
	fun testGetNoFollowRedirect()
	{
		TestHelper.createAsyncLatch().also { latch ->
			AsyncHttpClient(Uri.parse("https://httpbin.org"))
				.get(
					request = Request(
						path = "redirect/1",
						followRedirects = false
					),
					response = { response ->
						Assert.assertNotNull(response)

						Assert.assertEquals(302, response.code)

						TestHelper.stopLatch(latch)
					}
				)

			TestHelper.beginLatch(latch)
		}
	}

	/**
	 * Tests request times out
	 */
	@Test
	fun testGetTimeout()
	{
		TestHelper.createAsyncLatch().also { latch ->
			AsyncHttpClient(Uri.parse("https://httpbin.org"), 1000L)
				.get(
					request = Request(
						path = "delay/5"
					),
					processor = JsonProcessor(),
					response = { response ->
						Assert.assertNotNull(response)

						Assert.assertNull(response.body)
						Assert.assertEquals(0, response.code)
						Assert.assertEquals(0, response.length)
						Assert.assertTrue(response.time - response.request.time < 5000L)

						TestHelper.stopLatch(latch)
					}
				)

			TestHelper.beginLatch(latch)
		}
	}

	/**
	 * Tests the client is able to perform multiple individual requests asynchronously
	 */
	@Test
	fun testMultipleRequest()
	{
		TestHelper.createAsyncLatch(2).also { latch ->
			AsyncHttpClient("https://httpbin.org/delay/5").get { response ->
				Assert.assertEquals(200, response.code)
				Assert.assertTrue((response.body ?: "").contains(response.request.path))

				TestHelper.stopLatch(latch)
			}

			AsyncHttpClient("https://httpbin.org/delay/4").get { response ->
				Assert.assertEquals(200, response.code)
				Assert.assertTrue((response.body ?: "").contains(response.request.path))

				TestHelper.stopLatch(latch)
			}

			TestHelper.beginLatch(latch)
		}
	}

	/**
	 * Tests the request is properly cancelled and returned instantly
	 */
	@Test
	fun testCancel()
	{
		TestHelper.createAsyncLatch().also { latch ->
			AsyncHttpClient("https://httpbin.org/stream-bytes/16384").let { client ->
				client.get<ByteArray>(
					processor = object : ByteProcessor()
					{
						override fun onChunkReceived(request: Request, length: Long, total: Long)
						{
							if (length > 1024)
							{
								client.cancel()
							}
						}
					},
					response = { response ->
						// cancel code = 0
						Assert.assertEquals(0, response.code)

						// read count < 16384
						Assert.assertTrue(response.length < 16384)

						TestHelper.stopLatch(latch)
					}
				)
			}

			TestHelper.beginLatch(latch)
		}
	}
}
