package net.callumtaylor.asynchttp

import android.support.test.runner.AndroidJUnit4
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import net.callumtaylor.asynchttp.obj.Request
import net.callumtaylor.asynchttp.obj.asJsonBody
import net.callumtaylor.asynchttp.processor.JsonProcessor
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * // TODO: Add class description
 */
@RunWith(AndroidJUnit4::class)
class AsyncPostTest
{
	/**
	 * Tests basic post request
	 */
	@Test
	fun testJsonPost()
	{
		val signal = CountDownLatch(1)
		val bodyStr = "{\"key\": \"value\"}"

		AsyncHttpClient("https://httpbin.org/post").let { client ->
			client.post<JsonElement>(
				request = Request(
					body = bodyStr.asJsonBody()
				),
				processor = JsonProcessor(),
				response = { response ->
					Assert.assertEquals(200, response.code)

					val body = response.body as JsonObject
					Assert.assertEquals(body.get("json").asJsonObject.toString(), bodyStr)
					Assert.assertEquals(body.get("headers").asJsonObject.get("Content-Type"), "application/json")
					Assert.assertEquals(response.headers["Content-Type"], "application/json")

					signal.countDown()
				}
			)
		}

		signal.await(60, TimeUnit.SECONDS)

		if (signal.count != 0L)
		{
			Assert.fail()
		}
	}
}
