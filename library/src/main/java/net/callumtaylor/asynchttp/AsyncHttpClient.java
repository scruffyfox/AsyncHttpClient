package net.callumtaylor.asynchttp;

import android.annotation.TargetApi;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Build;
import android.text.TextUtils;

import net.callumtaylor.asynchttp.obj.ConnectionInfo;
import net.callumtaylor.asynchttp.obj.NameValuePair;
import net.callumtaylor.asynchttp.obj.Packet;
import net.callumtaylor.asynchttp.obj.RequestMode;
import net.callumtaylor.asynchttp.obj.RequestUtil;
import net.callumtaylor.asynchttp.response.ResponseHandler;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * @mainpage
 *
 * The client class used for initiating HTTP requests using an AsyncTask. It
 * follows a RESTful paradigm for the connections with the 4 possible methods,
 * GET, POST, PUT and DELETE.
 *
 * <b>Note:</b> Because AsyncHttpClient uses
 * AsyncTask, only one instance can be created at a time. If one client makes 2
 * requests, the first request is canceled for the new request. You can either
 * wait for the first to finish before making the second, or you can create two
 * seperate instances.
 *
 * <b>Depends on</b>
 * <ul>
 * <li>{@link net.callumtaylor.asynchttp.response.ResponseHandler}</li>
 * <li>{@link RequestBody}</li>
 * <li>{@link NameValuePair}</li>
 * <li>{@link ConnectionInfo}</li>
 * <li>{@link Packet}</li>
 * <li>{@link RequestMode}</li>
 * <li>{@link RequestUtil}</li>
 * </ul>
 * <h1>Example GET</h1>
 *
 * <pre>
 * AsyncHttpClient client = new AsyncHttpClient(&quot;http://example.com&quot;);
 * List&lt;NameValuePair&gt; params = new ArrayList&lt;NameValuePair&gt;();
 * params.add(new BasicNameValuePair(&quot;key&quot;, &quot;value&quot;));
 *
 * List&lt;Header&gt; headers = new ArrayList&lt;Header&gt;();
 * headers.add(new BasicHeader(&quot;1&quot;, &quot;2&quot;));
 *
 * client.get(&quot;api/v1/&quot;, params, headers, new JsonResponseHandler()
 * {
 * 	&#064;Override public void onSuccess()
 * 	{
 * 		JsonElement result = getContent();
 * 	}
 * });
 * </pre>
 *
 * <h1>Example DELETE</h1>
 *
 * <pre>
 * AsyncHttpClient client = new AsyncHttpClient(&quot;http://example.com&quot;);
 * List&lt;NameValuePair&gt; params = new ArrayList&lt;NameValuePair&gt;();
 * params.add(new BasicNameValuePair(&quot;key&quot;, &quot;value&quot;));
 *
 * List&lt;Header&gt; headers = new ArrayList&lt;Header&gt;();
 * headers.add(new BasicHeader(&quot;1&quot;, &quot;2&quot;));
 *
 * client.delete(&quot;api/v1/&quot;, params, headers, new JsonResponseHandler()
 * {
 * 	&#064;Override public void onSuccess()
 * 	{
 * 		JsonElement result = getContent();
 * 	}
 * });
 * </pre>
 *
 * <h1>Example POST - Single Entity</h1>
 *
 * <pre>
 * AsyncHttpClient client = new AsyncHttpClient(&quot;http://example.com&quot;);
 * List&lt;NameValuePair&gt; params = new ArrayList&lt;NameValuePair&gt;();
 * params.add(new BasicNameValuePair(&quot;key&quot;, &quot;value&quot;));
 *
 * List&lt;Header&gt; headers = new ArrayList&lt;Header&gt;();
 * headers.add(new BasicHeader(&quot;1&quot;, &quot;2&quot;));
 *
 * JsonEntity data = new JsonEntity(&quot;{\&quot;key\&quot;:\&quot;value\&quot;}&quot;);
 * GzippedEntity entity = new GzippedEntity(data);
 *
 * client.post(&quot;api/v1/&quot;, params, entity, headers, new JsonResponseHandler()
 * {
 * 	&#064;Override public void onSuccess()
 * 	{
 * 		JsonElement result = getContent();
 * 	}
 * });
 * </pre>
 *
 * <h1>Example POST - Multiple Entity + file</h1>
 *
 * <pre>
 * AsyncHttpClient client = new AsyncHttpClient(&quot;http://example.com&quot;);
 * List&lt;NameValuePair&gt; params = new ArrayList&lt;NameValuePair&gt;();
 * params.add(new BasicNameValuePair(&quot;key&quot;, &quot;value&quot;));
 *
 * List&lt;Header&gt; headers = new ArrayList&lt;Header&gt;();
 * headers.add(new BasicHeader(&quot;1&quot;, &quot;2&quot;));
 *
 * MultiPartEntity entity = new MultiPartEntity();
 * FileEntity data1 = new FileEntity(new File(&quot;/IMG_6614.JPG&quot;), &quot;image/jpeg&quot;);
 * JsonEntity data2 = new JsonEntity(&quot;{\&quot;key\&quot;:\&quot;value\&quot;}&quot;);
 * entity.addFilePart(&quot;image1.jpg&quot;, data1);
 * entity.addPart(&quot;content1&quot;, data2);
 *
 * client.post(&quot;api/v1/&quot;, params, entity, headers, new JsonResponseHandler()
 * {
 * 	&#064;Override public void onSuccess()
 * 	{
 * 		JsonElement result = getContent();
 * 	}
 * });
 * </pre>
 *
 * <h1>Example PUT</h1>
 *
 * <pre>
 * AsyncHttpClient client = new AsyncHttpClient(&quot;http://example.com&quot;);
 * List&lt;NameValuePair&gt; params = new ArrayList&lt;NameValuePair&gt;();
 * params.add(new BasicNameValuePair(&quot;key&quot;, &quot;value&quot;));
 *
 * List&lt;Header&gt; headers = new ArrayList&lt;Header&gt;();
 * headers.add(new BasicHeader(&quot;1&quot;, &quot;2&quot;));
 *
 * JsonEntity data = new JsonEntity(&quot;{\&quot;key\&quot;:\&quot;value\&quot;}&quot;);
 * GzippedEntity entity = new GzippedEntity(data);
 *
 * client.post(&quot;api/v1/&quot;, params, entity, headers, new JsonResponseHandler()
 * {
 * 	&#064;Override public void onSuccess()
 * 	{
 * 		JsonElement result = getContent();
 * 	}
 * });
 * </pre>
 *
 * Because of the nature of REST, GET and DELETE requests behave in the same
 * way, POST and PUT requests also behave in the same way.
 *
 * @author Callum Taylor &lt;callumtaylor.net&gt; &#064;scruffyfox
 */
public class AsyncHttpClient
{
	/**
	 * User agent to send with every request. Defaults to {@link RequestUtil#getDefaultUserAgent()}
	 */
	public static String userAgent = RequestUtil.getDefaultUserAgent();

	private ClientExecutorTask executorTask;
	private Uri requestUri;
	private long requestTimeout = 0L;
	private boolean allowAllSsl = false;
	private boolean allowRedirect = true;

	/**
	 * Creates a new client using a base Url without a timeout
	 * @param baseUrl The base connection url
	 */
	public AsyncHttpClient(String baseUrl)
	{
		this(baseUrl, 0);
	}

	/**
	 * Creates a new client using a base Uri without a timeout
	 * @param baseUri The base connection uri
	 */
	public AsyncHttpClient(Uri baseUri)
	{
		this(baseUri, 0);
	}

	/**
	 * Creates a new client using a base Url with a timeout in MS
	 * @param baseUrl The base connection url
	 * @param timeout The timeout in MS
	 */
	public AsyncHttpClient(String baseUrl, long timeout)
	{
		this(Uri.parse(baseUrl), timeout);
	}

	/**
	 * Creates a new client using a base Uri with a timeout in MS
	 * @param baseUri The base connection uri
	 * @param timeout The timeout in MS
	 */
	public AsyncHttpClient(Uri baseUri, long timeout)
	{
		requestUri = baseUri;
		requestTimeout = timeout;
	}

	/**
	 * Cancels a request if it's running
	 */
	public void cancel()
	{
		if (executorTask != null && executorTask.getStatus() == Status.RUNNING)
		{
			executorTask.cancel(true);
		}
	}

	/**
	 * Performs a GET request on the baseUri
	 * @param response The response handler for the request
	 */
	public void get(ResponseHandler response)
	{
		get("", null, null, response);
	}

	/**
	 * Performs a GET request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param response The response handler for the request
	 */
	public void get(String path, ResponseHandler response)
	{
		get(path, null, null, response);
	}

	/**
	 * Performs a GET request on the baseUri
	 * @param headers The request headers for the connection
	 * @param response The response handler for the request
	 */
	public void get(Headers headers, ResponseHandler response)
	{
		get("", null, headers, response);
	}

	/**
	 * Performs a GET request on the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param headers The request headers for the connection
	 * @param response The response handler for the request
	 */
	public void get(List<NameValuePair> params, Headers headers, ResponseHandler response)
	{
		get("", params, headers, response);
	}

	/**
	 * Performs a GET request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param response The response handler for the request
	 */
	public void get(String path, List<NameValuePair> params, ResponseHandler response)
	{
		get(path, params, null, response);
	}

	/**
	 * Performs a GET request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param headers The request headers for the connection
	 * @param response The response handler for the request
	 */
	public void get(String path, List<NameValuePair> params, Headers headers, ResponseHandler response)
	{
		if (!TextUtils.isEmpty(path))
		{
			requestUri = Uri.withAppendedPath(requestUri, path);
		}

		requestUri = RequestUtil.appendParams(requestUri, params);
		executeTask(RequestMode.GET, requestUri, headers, null, response);
	}

	/**
	 * Performs a OPTIONS request on the baseUri
	 * @param response The response handler for the request
	 */
	public void options(ResponseHandler response)
	{
		options("", null, null, response);
	}

	/**
	 * Performs a OPTIONS request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param response The response handler for the request
	 */
	public void options(String path, ResponseHandler response)
	{
		options(path, null, null, response);
	}

	/**
	 * Performs a OPTIONS request on the baseUri
	 * @param headers The request headers for the connection
	 * @param response The response handler for the request
	 */
	public void options(Headers headers, ResponseHandler response)
	{
		options("", null, headers, response);
	}

	/**
	 * Performs a OPTIONS request on the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param headers The request headers for the connection
	 * @param response The response handler for the request
	 */
	public void options(List<NameValuePair> params, Headers headers, ResponseHandler response)
	{
		options("", params, headers, response);
	}

	/**
	 * Performs a OPTIONS request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param response The response handler for the request
	 */
	public void options(String path, List<NameValuePair> params, ResponseHandler response)
	{
		options(path, params, null, response);
	}

	/**
	 * Performs a OPTIONS request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param headers The request headers for the connection
	 * @param response The response handler for the request
	 */
	public void options(String path, List<NameValuePair> params, Headers headers, ResponseHandler response)
	{
		if (!TextUtils.isEmpty(path))
		{
			requestUri = Uri.withAppendedPath(requestUri, path);
		}

		requestUri = RequestUtil.appendParams(requestUri, params);
		executeTask(RequestMode.OPTIONS, requestUri, headers, null, response);
	}

	/**
	 * Performs a HEAD request on the baseUri
	 * @param response The response handler for the request
	 */
	public void head(ResponseHandler response)
	{
		head("", null, null, response);
	}

	/**
	 * Performs a HEAD request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param response The response handler for the request
	 */
	public void head(String path, ResponseHandler response)
	{
		head(path, null, null, response);
	}

	/**
	 * Performs a HEAD request on the baseUri
	 * @param headers The request headers for the connection
	 * @param response The response handler for the request
	 */
	public void head(Headers headers, ResponseHandler response)
	{
		head("", null, headers, response);
	}

	/**
	 * Performs a HEAD request on the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param headers The request headers for the connection
	 * @param response The response handler for the request
	 */
	public void head(List<NameValuePair> params, Headers headers, ResponseHandler response)
	{
		head("", params, headers, response);
	}

	/**
	 * Performs a HEAD request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param response The response handler for the request
	 */
	public void head(String path, List<NameValuePair> params, ResponseHandler response)
	{
		head(path, params, null, response);
	}

	/**
	 * Performs a HEAD request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param headers The request headers for the connection
	 * @param response The response handler for the request
	 */
	public void head(String path, List<NameValuePair> params, Headers headers, ResponseHandler response)
	{
		if (!TextUtils.isEmpty(path))
		{
			requestUri = Uri.withAppendedPath(requestUri, path);
		}

		requestUri = RequestUtil.appendParams(requestUri, params);
		executeTask(RequestMode.HEAD, requestUri, headers, null, response);
	}

	/**
	 * Performs a DELETE request on the baseUri
	 * @param response The response handler for the request
	 */
	public void delete(ResponseHandler response)
	{
		delete("", null, null, null, response);
	}

	/**
	 * Performs a DELETE request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param response The response handler for the request
	 */
	public void delete(String path, ResponseHandler response)
	{
		delete(path, null, null, null, response);
	}

	/**
	 * Performs a DELETE request on the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param response The response handler for the request
	 */
	public void delete(List<NameValuePair> params, ResponseHandler response)
	{
		delete("", params, null, null, response);
	}

	/**
	 * Performs a DELETE request on the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param headers The request headers for the connection
	 * @param response The response handler for the request
	 */
	public void delete(List<NameValuePair> params, Headers headers, ResponseHandler response)
	{
		delete("", params, null, headers, response);
	}

	/**
	 * Performs a DELETE request on the baseUri
	 * @param postData The post data entity to post to the server
	 * @param response The response handler for the request
	 */
	public void delete(RequestBody postData, ResponseHandler response)
	{
		delete("", null, postData, null, response);
	}

	/**
	 * Performs a DELETE request on the baseUri
	 * @param postData The post data entity to post to the server
	 * @param headers The request headers for the connection
	 * @param response The response handler for the request
	 */
	public void delete(RequestBody postData, Headers headers, ResponseHandler response)
	{
		delete("", null, postData, headers, response);
	}

	/**
	 * Performs a DELETE request on the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param postData The post data entity to post to the server
	 * @param response The response handler for the request
	 */
	public void delete(List<NameValuePair> params, RequestBody postData, ResponseHandler response)
	{
		delete("", params, postData, null, response);
	}

	/**
	 * Performs a DELETE request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param response The response handler for the request
	 */
	public void delete(String path, List<NameValuePair> params, ResponseHandler response)
	{
		delete(path, params, null, null, response);
	}

	/**
	 * Performs a DELETE request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param headers The request headers for the connection
	 * @param response The response handler for the request
	 */
	public void delete(String path, List<NameValuePair> params, Headers headers, ResponseHandler response)
	{
		delete(path, params, null, headers, response);
	}

	/**
	 * Performs a DELETE request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param postData The post data entity to post to the server
	 * @param response The response handler for the request
	 */
	public void delete(String path, RequestBody postData, ResponseHandler response)
	{
		delete(path, null, postData, null, response);
	}

	/**
	 * Performs a DELETE request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param postData The post data entity to post to the server
	 * @param headers The request headers for the connection
	 * @param response The response handler for the request
	 */
	public void delete(String path, RequestBody postData, Headers headers, ResponseHandler response)
	{
		delete(path, null, postData, headers, response);
	}

	/**
	 * Performs a DELETE request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param postData The post data entity to post to the server
	 * @param response The response handler for the request
	 */
	public void delete(String path, List<NameValuePair> params, RequestBody postData, ResponseHandler response)
	{
		delete(path, params, postData, null, response);
	}

	/**
	 * Performs a DELETE request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param postData The post data entity to post to the server
	 * @param headers The request headers for the connection
	 * @param response The response handler for the request
	 */
	public void delete(String path, List<NameValuePair> params, RequestBody postData, Headers headers, ResponseHandler response)
	{
		if (!TextUtils.isEmpty(path))
		{
			requestUri = Uri.withAppendedPath(requestUri, path);
		}

		requestUri = RequestUtil.appendParams(requestUri, params);
		executeTask(RequestMode.DELETE, requestUri, headers, postData, response);
	}

	/**
	 * Performs a POST request on the baseUri
	 * @param response The response handler for the request
	 */
	public void post(ResponseHandler response)
	{
		post("", null, null, null, response);
	}

	/**
	 * Performs a POST request on the baseUr
	 * @param path The path extended from the baseUri
	 * @param response The response handler for the request
	 */
	public void post(String path, ResponseHandler response)
	{
		post(path, null, null, null, response);
	}

	/**
	 * Performs a POST request on the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param response The response handler for the request
	 */
	public void post(List<NameValuePair> params, ResponseHandler response)
	{
		post("", params, null, null, response);
	}

	/**
	 * Performs a POST request on the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param headers The request headers for the connection
	 * @param response The response handler for the request
	 */
	public void post(List<NameValuePair> params, Headers headers, ResponseHandler response)
	{
		post("", params, null, headers, response);
	}

	/**
	 * Performs a POST request on the baseUri
	 * @param postData The post data entity to post to the server
	 * @param response The response handler for the request
	 */
	public void post(RequestBody postData, ResponseHandler response)
	{
		post("", null, postData, null, response);
	}

	/**
	 * Performs a POST request on the baseUri
	 * @param postData The post data entity to post to the server
	 * @param headers The request headers for the connection
	 * @param response The response handler for the request
	 */
	public void post(RequestBody postData, Headers headers, ResponseHandler response)
	{
		post("", null, postData, headers, response);
	}

	/**
	 * Performs a POST request on the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param postData The post data entity to post to the server
	 * @param response The response handler for the request
	 */
	public void post(List<NameValuePair> params, RequestBody postData, ResponseHandler response)
	{
		post("", params, postData, null, response);
	}

	/**
	 * Performs a POST request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param response The response handler for the request
	 */
	public void post(String path, List<NameValuePair> params, ResponseHandler response)
	{
		post(path, params, null, null, response);
	}

	/**
	 * Performs a POST request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param headers The request headers for the connection
	 * @param response The response handler for the request
	 */
	public void post(String path, List<NameValuePair> params, Headers headers, ResponseHandler response)
	{
		post(path, params, null, headers, response);
	}

	/**
	 * Performs a POST request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param postData The post data entity to post to the server
	 * @param response The response handler for the request
	 */
	public void post(String path, RequestBody postData, ResponseHandler response)
	{
		post(path, null, postData, null, response);
	}

	/**
	 * Performs a POST request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param postData The post data entity to post to the server
	 * @param headers The request headers for the connection
	 * @param response The response handler for the request
	 */
	public void post(String path, RequestBody postData, Headers headers, ResponseHandler response)
	{
		post(path, null, postData, headers, response);
	}

	/**
	 * Performs a POST request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param postData The post data entity to post to the server
	 * @param response The response handler for the request
	 */
	public void post(String path, List<NameValuePair> params, RequestBody postData, ResponseHandler response)
	{
		post(path, params, postData, null, response);
	}

	/**
	 * Performs a POST request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param postData The post data entity to post to the server
	 * @param headers The request headers for the connection
	 * @param response The response handler for the request
	 */
	public void post(String path, List<NameValuePair> params, RequestBody postData, Headers headers, ResponseHandler response)
	{
		if (!TextUtils.isEmpty(path))
		{
			requestUri = Uri.withAppendedPath(requestUri, path);
		}

		requestUri = RequestUtil.appendParams(requestUri, params);
		executeTask(RequestMode.POST, requestUri, headers, postData, response);
	}

	/**
	 * Performs a PUT request on the baseUr
	 * @param response The response handler for the request
	 */
	public void put(ResponseHandler response)
	{
		put("", null, null, null, response);
	}

	/**
	 * Performs a PUT request on the baseUr
	 * @param path The path extended from the baseUri
	 * @param response The response handler for the request
	 */
	public void put(String path, ResponseHandler response)
	{
		put(path, null, null, null, response);
	}

	/**
	 * Performs a PUT request on the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param response The response handler for the request
	 */
	public void put(List<NameValuePair> params, ResponseHandler response)
	{
		put("", params, null, null, response);
	}

	/**
	 * Performs a PUT request on the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param headers The request headers for the connection
	 * @param response The response handler for the request
	 */
	public void put(List<NameValuePair> params, Headers headers, ResponseHandler response)
	{
		put("", params, null, headers, response);
	}

	/**
	 * Performs a PUT request on the baseUri
	 * @param postData The post data entity to post to the server
	 * @param response The response handler for the request
	 */
	public void put(RequestBody postData, ResponseHandler response)
	{
		put("", null, postData, null, response);
	}

	/**
	 * Performs a PUT request on the baseUri
	 * @param postData The post data entity to post to the server
	 * @param headers The request headers for the connection
	 * @param response The response handler for the request
	 */
	public void put(RequestBody postData, Headers headers, ResponseHandler response)
	{
		put("", null, postData, headers, response);
	}

	/**
	 * Performs a PUT request on the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param postData The post data entity to post to the server
	 * @param response The response handler for the request
	 */
	public void put(List<NameValuePair> params, RequestBody postData, ResponseHandler response)
	{
		put("", params, postData, null, response);
	}

	/**
	 * Performs a PUT request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param response The response handler for the request
	 */
	public void put(String path, List<NameValuePair> params, ResponseHandler response)
	{
		put(path, params, null, null, response);
	}

	/**
	 * Performs a PUT request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param headers The request headers for the connection
	 * @param response The response handler for the request
	 */
	public void put(String path, List<NameValuePair> params, Headers headers, ResponseHandler response)
	{
		put(path, params, null, headers, response);
	}

	/**
	 * Performs a PUT request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param postData The post data entity to post to the server
	 * @param response The response handler for the request
	 */
	public void put(String path, RequestBody postData, ResponseHandler response)
	{
		put(path, null, postData, null, response);
	}

	/**
	 * Performs a PUT request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param postData The post data entity to post to the server
	 * @param headers The request headers for the connection
	 * @param response The response handler for the request
	 */
	public void put(String path, RequestBody postData, Headers headers, ResponseHandler response)
	{
		put(path, null, postData, headers, response);
	}

	/**
	 * Performs a PUT request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param postData The post data entity to post to the server
	 * @param response The response handler for the request
	 */
	public void put(String path, List<NameValuePair> params, RequestBody postData, ResponseHandler response)
	{
		put(path, params, postData, null, response);
	}

	/**
	 * Performs a PUT request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param postData The post data entity to post to the server
	 * @param headers The request headers for the connection
	 * @param response The response handler for the request
	 */
	public void put(String path, List<NameValuePair> params, RequestBody postData, Headers headers, ResponseHandler response)
	{
		if (!TextUtils.isEmpty(path))
		{
			requestUri = Uri.withAppendedPath(requestUri, path);
		}

		requestUri = RequestUtil.appendParams(requestUri, params);
		executeTask(RequestMode.PUT, requestUri, headers, postData, response);
	}

	/**
	 * Performs a PATCH request on the baseUr
	 * @param response The response handler for the request
	 */
	public void patch(ResponseHandler response)
	{
		patch("", null, null, null, response);
	}

	/**
	 * Performs a PATCH request on the baseUr
	 * @param path The path extended from the baseUri
	 * @param response The response handler for the request
	 */
	public void patch(String path, ResponseHandler response)
	{
		patch(path, null, null, null, response);
	}

	/**
	 * Performs a PATCH request on the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param response The response handler for the request
	 */
	public void patch(List<NameValuePair> params, ResponseHandler response)
	{
		patch("", params, null, null, response);
	}

	/**
	 * Performs a PATCH request on the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param headers The request headers for the connection
	 * @param response The response handler for the request
	 */
	public void patch(List<NameValuePair> params, Headers headers, ResponseHandler response)
	{
		patch("", params, null, headers, response);
	}

	/**
	 * Performs a PATCH request on the baseUri
	 * @param postData The post data entity to post to the server
	 * @param response The response handler for the request
	 */
	public void patch(RequestBody postData, ResponseHandler response)
	{
		patch("", null, postData, null, response);
	}

	/**
	 * Performs a PATCH request on the baseUri
	 * @param postData The post data entity to post to the server
	 * @param headers The request headers for the connection
	 * @param response The response handler for the request
	 */
	public void patch(RequestBody postData, Headers headers, ResponseHandler response)
	{
		patch("", null, postData, headers, response);
	}

	/**
	 * Performs a PATCH request on the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param postData The post data entity to post to the server
	 * @param response The response handler for the request
	 */
	public void patch(List<NameValuePair> params, RequestBody postData, ResponseHandler response)
	{
		patch("", params, postData, null, response);
	}

	/**
	 * Performs a PATCH request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param response The response handler for the request
	 */
	public void patch(String path, List<NameValuePair> params, ResponseHandler response)
	{
		patch(path, params, null, null, response);
	}

	/**
	 * Performs a PATCH request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param headers The request headers for the connection
	 * @param response The response handler for the request
	 */
	public void patch(String path, List<NameValuePair> params, Headers headers, ResponseHandler response)
	{
		patch(path, params, null, headers, response);
	}

	/**
	 * Performs a PATCH request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param postData The post data entity to post to the server
	 * @param response The response handler for the request
	 */
	public void patch(String path, RequestBody postData, ResponseHandler response)
	{
		patch(path, null, postData, null, response);
	}

	/**
	 * Performs a PATCH request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param postData The post data entity to post to the server
	 * @param headers The request headers for the connection
	 * @param response The response handler for the request
	 */
	public void patch(String path, RequestBody postData, Headers headers, ResponseHandler response)
	{
		patch(path, null, postData, headers, response);
	}

	/**
	 * Performs a PATCH request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param postData The post data entity to post to the server
	 * @param response The response handler for the request
	 */
	public void patch(String path, List<NameValuePair> params, RequestBody postData, ResponseHandler response)
	{
		patch(path, params, postData, null, response);
	}

	/**
	 * Performs a PATCH request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param postData The post data entity to post to the server
	 * @param headers The request headers for the connection
	 * @param response The response handler for the request
	 */
	public void patch(String path, List<NameValuePair> params, RequestBody postData, Headers headers, ResponseHandler response)
	{
		if (!TextUtils.isEmpty(path))
		{
			requestUri = Uri.withAppendedPath(requestUri, path);
		}

		requestUri = RequestUtil.appendParams(requestUri, params);
		executeTask(RequestMode.PATCH, requestUri, headers, postData, response);
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void executeTask(RequestMode mode, Uri uri, Headers headers, RequestBody sendData, ResponseHandler response)
	{
		if (executorTask != null || (executorTask != null && (executorTask.getStatus() == Status.RUNNING || executorTask.getStatus() == Status.PENDING)))
		{
			executorTask.cancel(true);
			executorTask = null;
		}

		executorTask = new ClientExecutorTask(mode, uri, headers, sendData, response, allowRedirect);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
		{
			executorTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		}
		else
		{
			executorTask.execute();
		}
	}

	public class ClientExecutorTask extends AsyncTask<Void, Packet, Void>
	{
		private static final int BUFFER_SIZE = 1 * 1024 * 8;

		private final ResponseHandler response;
		private final Uri requestUri;
		private final Headers requestHeaders;
		private final RequestBody postData;
		private final RequestMode requestMode;
		private boolean allowRedirect = true;

		public ClientExecutorTask(RequestMode mode, Uri request, Headers headers, RequestBody postData, ResponseHandler response, boolean allowRedirect)
		{
			this.response = response;
			this.requestUri = request;
			this.requestHeaders = headers;
			this.postData = postData;
			this.requestMode = mode;
			this.allowRedirect = allowRedirect;
		}

		@Override protected void onPreExecute()
		{
			super.onPreExecute();
			if (this.response != null)
			{
				this.response.getConnectionInfo().connectionUrl = requestUri.toString();
				this.response.getConnectionInfo().connectionTime = System.currentTimeMillis();
				this.response.getConnectionInfo().requestMethod = requestMode;
				this.response.getConnectionInfo().requestHeaders = requestHeaders;
				this.response.onSend();
			}
		}

		@Override protected Void doInBackground(Void... params)
		{
			OkHttpClient httpClient;

//			if (allowAllSsl)
//			{
//				SchemeRegistry schemeRegistry = new SchemeRegistry();
//				schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
//				schemeRegistry.register(new Scheme("https", new EasySSLSocketFactory(), 443));
//
//				HttpParams httpParams = new BasicHttpParams();
//				httpParams.setParameter(ConnManagerPNames.MAX_TOTAL_CONNECTIONS, 30);
//				httpParams.setParameter(ConnManagerPNames.MAX_CONNECTIONS_PER_ROUTE, new ConnPerRouteBean(30));
//				httpParams.setParameter(HttpProtocolParams.USE_EXPECT_CONTINUE, false);
//				HttpProtocolParams.setVersion(httpParams, HttpVersion.HTTP_1_1);
//
//				ClientConnectionManager cm = new ThreadSafeClientConnManager(httpParams, schemeRegistry);
//				httpClient = new DefaultHttpClient(cm, httpParams);
//			}
//			else
			{
				httpClient = new OkHttpClient();
			}

			try
			{
				System.setProperty("http.keepAlive", "false");
				Request.Builder request = new Request.Builder()
					.url(requestUri.toString());

				if (requestMode == RequestMode.GET)
				{
					request = request.get();
				}
//				else if (requestMode == RequestMode.POST)
//				{
//					request = new HttpPost(requestUri.toString());
//				}
//				else if (requestMode == RequestMode.PUT)
//				{
//					request = new HttpPut(requestUri.toString());
//				}
//				else if (requestMode == RequestMode.DELETE)
//				{
//					request = new HttpDeleteWithBody(requestUri.toString());
//				}
//				else if (requestMode == RequestMode.HEAD)
//				{
//					request = new HttpHead(requestUri.toString());
//				}
//				else if (requestMode == RequestMode.PATCH)
//				{
//					request = new HttpPatch(requestUri.toString());
//				}
//				else if (requestMode == RequestMode.OPTIONS)
//				{
//					request = new HttpOptions(requestUri.toString());
//				}

//				HttpParams p = httpClient.getParams();
//				HttpClientParams.setRedirecting(p, allowRedirect);
//				HttpConnectionParams.setConnectionTimeout(p, (int)requestTimeout);
//				HttpConnectionParams.setSoTimeout(p, (int)requestTimeout);

				request.header("Connection", "close");
				request.header("User-Agent", userAgent);

//				if (postData != null)
//				{
//					request.header(postData.getContentType().getName(), postData.getContentType().getValue());
//				}

				if (requestHeaders != null)
				{
					request.headers(requestHeaders);
				}

//				if ((requestMode == RequestMode.POST || requestMode == RequestMode.PUT || requestMode == RequestMode.DELETE || requestMode == RequestMode.PATCH) && postData != null)
//				{
//					final long contentLength = postData.getContentLength();
//					if (this.response != null && !isCancelled())
//					{
//						this.response.getConnectionInfo().connectionLength = contentLength;
//					}
//
//					((RequestBodyEnclosingRequestBase)request).setEntity(new ProgressEntityWrapper(postData, new ProgressListener()
//					{
//						@Override public void onBytesTransferred(byte[] buffer, int len, long transferred)
//						{
//							if (response != null)
//							{
//								response.onPublishedUploadProgress(buffer, len, contentLength);
//								response.onPublishedUploadProgress(buffer, len, transferred, contentLength);
//
//								publishProgress(new Packet(transferred, contentLength, false));
//							}
//						}
//					}));
//				}

				// Get the response
				Call call = httpClient.newCall(request.build());
				Response response = call.execute();

				int responseCode = response.code();

				if (response.headers() != null && this.response != null)
				{
					this.response.getConnectionInfo().responseHeaders = response.headers();
				}

				if (response.body() != null)
				{
//					String encoding = response.body().getContentEncoding() == null ? "" : response.getEntity().getContentEncoding().getValue();
					long contentLength = response.body().contentLength();
					InputStream responseStream;
					InputStream stream = response.body().byteStream();

//					if ("gzip".equals(encoding))
//					{
//						responseStream = new GZIPInputStream(new BufferedInputStream(stream, BUFFER_SIZE));
//					}
//					else
					{
						responseStream = new BufferedInputStream(stream, BUFFER_SIZE);
					}

					if (this.response != null && !isCancelled())
					{
						this.response.getConnectionInfo().responseCode = responseCode;
					}

					try
					{
						if (this.response != null && contentLength != 0 && !isCancelled())
						{
							this.response.onBeginPublishedDownloadProgress(responseStream, this, contentLength);
							this.response.generateContent();
						}
					}
					catch (SocketTimeoutException timeout)
					{
						responseCode = 0;
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
					finally
					{
						responseStream.close();
					}
				}

				if (this.response != null && !isCancelled())
				{
					this.response.getConnectionInfo().responseCode = responseCode;
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}

			if (this.response != null && !isCancelled())
			{
				this.response.getConnectionInfo().responseTime = System.currentTimeMillis();

				if (this.response.getConnectionInfo().responseCode < 400 && this.response.getConnectionInfo().responseCode > 100)
				{
					this.response.onSuccess();
				}
				else
				{
					this.response.onFailure();
				}
			}

			return null;
		}

		@Override protected void onProgressUpdate(Packet... values)
		{
			super.onProgressUpdate(values);

			if (this.response != null && !isCancelled())
			{
				if (values[0].isDownload)
				{
					this.response.onPublishedDownloadProgressUI(values[0].length, values[0].total);
				}
				else
				{
					this.response.onPublishedUploadProgressUI(values[0].length, values[0].total);
				}
			}
		}

		@Override protected void onPostExecute(Void result)
		{
			super.onPostExecute(result);

			if (this.response != null && !isCancelled())
			{
				this.response.beforeCallback();
				this.response.beforeFinish();
				this.response.onFinish();
				this.response.onFinish(this.response.getConnectionInfo().responseCode >= 400 || this.response.getConnectionInfo().responseCode == 0);
			}
		}

		public void postPublishProgress(Packet... values)
		{
			publishProgress(values);
		}
	}

	public void setAllowAllSsl(boolean allow)
	{
		this.allowAllSsl = allow;
	}

	public void setAllowRedirect(boolean allow)
	{
		this.allowRedirect = allow;
	}
}
