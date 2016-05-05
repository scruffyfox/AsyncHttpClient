package net.callumtaylor.asynchttp;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import net.callumtaylor.asynchttp.obj.ConnectionInfo;
import net.callumtaylor.asynchttp.obj.NameValuePair;
import net.callumtaylor.asynchttp.obj.RequestMode;
import net.callumtaylor.asynchttp.obj.RequestUtil;
import net.callumtaylor.asynchttp.response.ByteArrayResponseHandler;
import net.callumtaylor.asynchttp.response.ResponseHandler;

import java.util.List;

import okhttp3.Headers;
import okhttp3.RequestBody;

/**
 * This class is a synchronous class which runs on any thread that the code was created
 * on. This will throw a {@link android.os.NetworkOnMainThreadException} if ran on the UI thread.
 *
 * <b>Note:</b> Because of the way SyncHttpClient works, only one instance can be created
 * at a time. If one client makes 2 requests, the first request is canceled for the new
 * request. You can either wait for the first to finish before making the second, or you
 * can create two separate instances.
 *
 * <b>Depends on</b>
 * <ul>
 * <li>{@link net.callumtaylor.asynchttp.response.ResponseHandler}</li>
 * <li>{@link RequestBody}</li>
 * <li>{@link NameValuePair}</li>
 * <li>{@link ConnectionInfo}</li>
 * <li>{@link net.callumtaylor.asynchttp.obj.Packet}</li>
 * <li>{@link RequestMode}</li>
 * <li>{@link RequestUtil}</li>
 * </ul>
 * <h1>Example GET</h1>
 *
 * SyncHttpClient is a paramitized class which means the type you infer to it, is the type
 * that gets returned when calling the method. When supplying a {@link net.callumtaylor.asynchttp.response.ResponseHandler}, that
 * processor must also paramitized with the same type as the SyncHttpClient instance.
 *
 * <pre>
 * SyncHttpClient<JsonElement> client = new SyncHttpClient<JsonElement>(&quot;http://example.com&quot;);
 * List&lt;NameValuePair&gt; params = new ArrayList&lt;NameValuePair&gt;();
 * params.add(new NameValuePair(&quot;key&quot;, &quot;value&quot;));
 *
 * JsonElement response = client.get(&quot;api/v1/&quot;, params, new JsonResponseHandler());
 * </pre>
 *
 * <h1>Example DELETE</h1>
 *
 * <pre>
 * SyncHttpClient<JsonElement> client = new SyncHttpClient<JsonElement>(&quot;http://example.com&quot;);
 * List&lt;NameValuePair&gt; params = new ArrayList&lt;NameValuePair&gt;();
 * params.add(new NameValuePair(&quot;key&quot;, &quot;value&quot;));
 *
 * JsonElement response = client.delete(&quot;api/v1/&quot;, params, new JsonResponseHandler());
 * </pre>
 *
 * Because of the nature of REST, GET and DELETE requests behave in the same
 * way, POST and PUT requests also behave in the same way.
 *
 * @author Callum Taylor &lt;callumtaylor.net&gt; &#064;scruffyfox
 */
public class SyncHttpClient<E>
{
	/**
	 * User agent to send with every request. Defaults to {@link RequestUtil#getDefaultUserAgent()}
	 */
	public static String userAgent = RequestUtil.getDefaultUserAgent();

	private Uri requestUri;
	private long requestTimeout = 0L;
	private boolean allowAllSsl = false;
	private boolean allowRedirect = false;
	private ClientExecutorTask<E> executor;

	/**
	 * Creates a new client using a base Url without a timeout
	 * @param baseUrl The base connection url
	 */
	public SyncHttpClient(String baseUrl)
	{
		this(baseUrl, 0);
	}

	/**
	 * Creates a new client using a base Uri without a timeout
	 * @param baseUri The base connection uri
	 */
	public SyncHttpClient(Uri baseUri)
	{
		this(baseUri, 0);
	}

	/**
	 * Creates a new client using a base Url with a timeout in MS
	 * @param baseUrl The base connection url
	 * @param timeout The timeout in MS
	 */
	public SyncHttpClient(String baseUrl, long timeout)
	{
		this(Uri.parse(baseUrl), timeout);
	}

	/**
	 * Creates a new client using a base Uri with a timeout in MS
	 * @param baseUri The base connection uri
	 * @param timeout The timeout in MS
	 */
	public SyncHttpClient(Uri baseUri, long timeout)
	{
		requestUri = baseUri;
		requestTimeout = timeout;
	}

	/**
	 * Cancels the current executor task
	 */
	public void cancel()
	{
		if (executor != null)
		{
			executor.cancel();
		}
	}

	/**
	 * Performs a GET request on the baseUri
	 * @param response The response handler for the request
	 *
	 * @return The response object, or null
	 */
	@Nullable
	public E get(@NonNull ResponseHandler<?> response)
	{
		return get("", null, null, response);
	}

	/**
	 * Performs a GET request on the baseUri
	 * @param path The path extended from the baseUri
	 *
	 * @return The response object, or null
	 */
	@Nullable
	public E get(String path)
	{
		return get(path, null, null, new ByteArrayResponseHandler());
	}

	/**
	 * Performs a GET request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param response The response handler for the request
	 *
	 * @return The response object, or null
	 */
	@Nullable
	public E get(String path, @NonNull ResponseHandler<?> response)
	{
		return get(path, null, null, response);
	}

	/**
	 * Performs a GET request on the baseUri
	 * @param headers The request headers for the connection
	 * @param response The response handler for the request
	 *
	 * @return The response object, or null
	 */
	@Nullable
	public E get(@Nullable Headers headers, @NonNull ResponseHandler<?> response)
	{
		return get("", null, headers, response);
	}

	/**
	 * Performs a GET request on the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param response The response handler for the request
	 *
	 * @return The response object, or null
	 */
	@Nullable
	public E get(@Nullable List<NameValuePair> params,  @NonNull ResponseHandler<?> response)
	{
		return get("", params, null, response);
	}

	/**
	 * Performs a GET request on the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param headers The request headers for the connection
	 * @param response The response handler for the request
	 *
	 * @return The response object, or null
	 */
	@Nullable
	public E get(@Nullable List<NameValuePair> params, @Nullable Headers headers, @NonNull ResponseHandler<?> response)
	{
		return get("", params, headers, response);
	}

	/**
	 * Performs a GET request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param params The request params for the connection
	 * @param response The response handler for the request
	 *
	 * @return The response object, or null
	 */
	@Nullable
	public E get(String path, @Nullable List<NameValuePair> params, @NonNull ResponseHandler<?> response)
	{
		return get(path, params, null, response);
	}

	/**
	 * Performs a GET request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param headers The request headers for the connection
	 * @param response The response handler for the request
	 *
	 * @return The response object, or null
	 */
	@Nullable
	public E get(String path, @Nullable Headers headers, @NonNull ResponseHandler<?> response)
	{
		return get(path, null, headers, response);
	}

	/**
	 * Performs a GET request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param headers The request headers for the connection
	 * @param response The response handler for the request
	 *
	 * @return The response object, or null
	 */
	@Nullable
	public E get(String path, @Nullable List<NameValuePair> params, @Nullable Headers headers, @NonNull ResponseHandler<?> response)
	{
		if (!TextUtils.isEmpty(path))
		{
			requestUri = Uri.withAppendedPath(requestUri, path);
		}

		requestUri = RequestUtil.appendParams(requestUri, params);
		return executeTask(RequestMode.GET, requestUri, headers, null, response);
	}

	/**
	 * Performs a OPTIONS request on the baseUri
	 * @param response The response handler for the request
	 *
	 * @return The response object, or null
	 */
	@Nullable
	public E options(@NonNull ResponseHandler<?> response)
	{
		return options("", null, null, response);
	}

	/**
	 * Performs a OPTIONS request on the baseUri
	 * @param path The path extended from the baseUri
	 */
	public E options(String path)
	{
		return options(path, null, null, new ByteArrayResponseHandler());
	}

	/**
	 * Performs a OPTIONS request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param response The response handler for the request
	 */
	public E options(String path, @NonNull ResponseHandler<?> response)
	{
		return options(path, null, null, response);
	}

	/**
	 * Performs a OPTIONS request on the baseUri
	 * @param headers The request headers for the connection
	 * @param response The response handler for the request
	 *
	 * @return The response object, or null
	 */
	@Nullable
	public E options(@Nullable Headers headers, @NonNull ResponseHandler<?> response)
	{
		return options("", null, headers, response);
	}

	/**
	 * Performs a OPTIONS request on the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param response The response handler for the request
	 *
	 * @return The response object, or null
	 */
	@Nullable
	public E options(@Nullable List<NameValuePair> params, @NonNull ResponseHandler<?> response)
	{
		return options("", params, null, response);
	}

	/**
	 * Performs a OPTIONS request on the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param headers The request headers for the connection
	 * @param response The response handler for the request
	 *
	 * @return The response object, or null
	 */
	@Nullable
	public E options(@Nullable List<NameValuePair> params, @Nullable Headers headers, @NonNull ResponseHandler<?> response)
	{
		return options("", params, headers, response);
	}

	/**
	 * Performs a OPTIONS request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param params The request params for the connection
	 * @param response The response handler for the request
	 *
	 * @return The response object, or null
	 */
	@Nullable
	public E options(String path, @Nullable List<NameValuePair> params, @NonNull ResponseHandler<?> response)
	{
		return options(path, params, null, response);
	}

	/**
	 * Performs a OPTIONS request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param headers The request headers for the connection
	 * @param response The response handler for the request
	 *
	 * @return The response object, or null
	 */
	@Nullable
	public E options(String path, @Nullable List<NameValuePair> params, @Nullable Headers headers, @NonNull ResponseHandler<?> response)
	{
		if (!TextUtils.isEmpty(path))
		{
			requestUri = Uri.withAppendedPath(requestUri, path);
		}

		requestUri = RequestUtil.appendParams(requestUri, params);
		return executeTask(RequestMode.OPTIONS, requestUri, headers, null, response);
	}

	/**
	 * Performs a HEAD request on the baseUri
	 * @param response The response handler for the request
	 *
	 * @return The response object, or null
	 */
	@Nullable
	public E head(@NonNull ResponseHandler<?> response)
	{
		return head("", null, null, response);
	}

	/**
	 * Performs a HEAD request on the baseUri
	 * @param path The path extended from the baseUri
	 *
	 * @return The response object, or null
	 */
	@Nullable
	public E head(String path)
	{
		return head(path, null, null, new ByteArrayResponseHandler());
	}

	/**
	 * Performs a HEAD request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param response The response handler for the request
	 *
	 * @return The response object, or null
	 */
	@Nullable
	public E head(String path, @NonNull ResponseHandler<?> response)
	{
		return head(path, null, null, response);
	}

	/**
	 * Performs a HEAD request on the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param response The response handler for the request
	 *
	 * @return The response object, or null
	 */
	@Nullable
	public E head(@Nullable List<NameValuePair> params, @NonNull ResponseHandler<?> response)
	{
		return head("", params, null, response);
	}

	/**
	 * Performs a HEAD request on the baseUri
	 * @param headers The request headers for the connection
	 * @param response The response handler for the request
	 *
	 * @return The response object, or null
	 */
	@Nullable
	public E head(@Nullable Headers headers, @NonNull ResponseHandler<?> response)
	{
		return head("", null, headers, response);
	}

	/**
	 * Performs a HEAD request on the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param headers The request headers for the connection
	 * @param response The response handler for the request
	 *
	 * @return The response object, or null
	 */
	@Nullable
	public E head(@Nullable List<NameValuePair> params, @Nullable Headers headers, @NonNull ResponseHandler<?> response)
	{
		return head("", params, headers, response);
	}

	/**
	 * Performs a HEAD request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param params The request params for the connection
	 * @param response The response handler for the request
	 *
	 * @return The response object, or null
	 */
	@Nullable
	public E head(String path, @Nullable List<NameValuePair> params, @NonNull ResponseHandler<?> response)
	{
		return head(path, params, null, response);
	}

	/**
	 * Performs a HEAD request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param headers The request headers for the connection
	 * @param response The response handler for the request
	 *
	 * @return The response object, or null
	 */
	@Nullable
	public E head(String path, @Nullable Headers headers, @NonNull ResponseHandler<?> response)
	{
		return head(path, null, headers, response);
	}

	/**
	 * Performs a HEAD request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param headers The request headers for the connection
	 * @param response The response handler for the request
	 *
	 * @return The response object, or null
	 */
	@Nullable
	public E head(String path, @Nullable List<NameValuePair> params, @Nullable Headers headers, @NonNull ResponseHandler<?> response)
	{
		if (!TextUtils.isEmpty(path))
		{
			requestUri = Uri.withAppendedPath(requestUri, path);
		}

		requestUri = RequestUtil.appendParams(requestUri, params);
		return executeTask(RequestMode.HEAD, requestUri, headers, null, response);
	}

	/**
	 * Performs a DELETE request on the baseUri
	 * @param response The response handler for the request
	 *
	 * @return The response object, or null
	 */
	@Nullable
	public E delete(@NonNull ResponseHandler<?> response)
	{
		return delete("", null, null, null, response);
	}

	/**
	 * Performs a DELETE request on the baseUri
	 * @param path The path extended from the baseUri
	 */
	public E delete(String path)
	{
		return delete(path, null, null, null, new ByteArrayResponseHandler());
	}

	/**
	 * Performs a DELETE request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param response The response handler for the request
	 *
	 * @return The response object, or null
	 */
	@Nullable
	public E delete(String path, @NonNull ResponseHandler<?> response)
	{
		return delete(path, null, null, null, response);
	}

	/**
	 * Performs a DELETE request on the baseUri
	 * @param headers The request headers for the connection
	 * @param response The response handler for the request
	 *
	 * @return The response object, or null
	 */
	@Nullable
	public E delete(@Nullable Headers headers, @NonNull ResponseHandler<?> response)
	{
		return delete("", null, null, headers, response);
	}

	/**
	 * Performs a DELETE request on the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param response The response handler for the request
	 *
	 * @return The response object, or null
	 */
	@Nullable
	public E delete(@Nullable List<NameValuePair> params, @NonNull ResponseHandler<?> response)
	{
		return delete("", params, null, null, response);
	}

	/**
	 * Performs a DELETE request on the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param headers The request headers for the connection
	 * @param response The response handler for the request
	 *
	 * @return The response object, or null
	 */
	@Nullable
	public E delete(@Nullable List<NameValuePair> params, @Nullable Headers headers, @NonNull ResponseHandler<?> response)
	{
		return delete("", params, headers, response);
	}

	/**
	 * Performs a DELETE request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param params The request params for the connection
	 * @param response The response handler for the request
	 *
	 * @return The response object, or null
	 */
	@Nullable
	public E delete(String path, @Nullable List<NameValuePair> params, @NonNull ResponseHandler<?> response)
	{
		return delete(path, params, null, null, response);
	}

	/**
	 * Performs a DELETE request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param headers The request headers for the connection
	 * @param response The response handler for the request
	 *
	 * @return The response object, or null
	 */
	@Nullable
	public E delete(String path, @Nullable Headers headers, @NonNull ResponseHandler<?> response)
	{
		return delete(path, null, null, headers, response);
	}

	/**
	 * Performs a DELETE request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param headers The request headers for the connection
	 * @param response The response handler for the request
	 *
	 * @return The response object, or null
	 */
	@Nullable
	public E delete(String path, @Nullable List<NameValuePair> params, @Nullable Headers headers, @NonNull ResponseHandler<?> response)
	{
		if (!TextUtils.isEmpty(path))
		{
			requestUri = Uri.withAppendedPath(requestUri, path);
		}

		requestUri = RequestUtil.appendParams(requestUri, params);
		return executeTask(RequestMode.DELETE, requestUri, headers, null, response);
	}

	/**
	 * Performs a DELETE request on the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param deleteData The delete data entity to delete to the server
	 * @param response The response handler for the request
	 *
	 * @return The response object, or null
	 */
	@Nullable
	public E delete(@Nullable List<NameValuePair> params, @Nullable RequestBody deleteData, @NonNull ResponseHandler<?> response)
	{
		return delete("", params, deleteData, null, response);
	}

	/**
	 * Performs a DELETE request on the baseUri
	 * @param deleteData The delete data entity to delete to the server
	 * @param headers The request headers for the connection
	 * @param response The response handler for the request
	 *
	 * @return The response object, or null
	 */
	@Nullable
	public E delete(RequestBody deleteData, @Nullable Headers headers, @NonNull ResponseHandler<?> response)
	{
		return delete("", null, deleteData, headers, response);
	}

	/**
	 * Performs a DELETE request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param deleteData The delete data entity to delete to the server
	 * @param response The response handler for the request
	 *
	 * @return The response object, or null
	 */
	@Nullable
	public E delete(String path, @Nullable RequestBody deleteData, @NonNull ResponseHandler<?> response)
	{
		return delete(path, null, deleteData, null, response);
	}

	/**
	 * Performs a DELETE request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param deleteData The delete data entity to delete to the server
	 * @param headers The request headers for the connection
	 * @param response The response handler for the request
	 *
	 * @return The response object, or null
	 */
	@Nullable
	public E delete(String path, @Nullable RequestBody deleteData, @Nullable Headers headers, @NonNull ResponseHandler<?> response)
	{
		return delete(path, null, deleteData, headers, response);
	}

	/**
	 * Performs a DELETE request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param deleteData The delete data entity to delete to the server
	 * @param response The response handler for the request
	 *
	 * @return The response object, or null
	 */
	@Nullable
	public E delete(String path, @Nullable List<NameValuePair> params, @Nullable RequestBody deleteData, @NonNull ResponseHandler<?> response)
	{
		return delete(path, params, deleteData, null, response);
	}

	/**
	 * Performs a DELETE request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param deleteData The delete data entity to delete to the server
	 * @param headers The request headers for the connection
	 * @param response The response handler for the request
	 *
	 * @return The response object, or null
	 */
	@Nullable
	public E delete(String path, @Nullable List<NameValuePair> params, @Nullable RequestBody deleteData, @Nullable Headers headers, @NonNull ResponseHandler<?> response)
	{
		if (!TextUtils.isEmpty(path))
		{
			requestUri = Uri.withAppendedPath(requestUri, path);
		}

		requestUri = RequestUtil.appendParams(requestUri, params);
		return executeTask(RequestMode.DELETE, requestUri, headers, deleteData, response);
	}

	/**
	 * Performs a POST request on the baseUri
	 * @param response The response handler for the request
	 *
	 * @return The response object, or null
	 */
	@Nullable
	public E post(@NonNull ResponseHandler<?> response)
	{
		return post("", null, null, null, response);
	}

	/**
	 * Performs a POST request on the baseUr
	 * @param path The path extended from the baseUri
	 *
	 * @return The response object, or null
	 */
	@Nullable
	public E post(String path)
	{
		return post(path, null, null, null, new ByteArrayResponseHandler());
	}

	/**
	 * Performs a POST request on the baseUr
	 * @param path The path extended from the baseUri
	 * @param response The response handler for the request
	 *
	 * @return The response object, or null
	 */
	@Nullable
	public E post(String path, @NonNull ResponseHandler<?> response)
	{
		return post(path, null, null, null, response);
	}

	/**
	 * Performs a POST request on the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param response The response handler for the request
	 *
	 * @return The response object, or null
	 */
	@Nullable
	public E post(@Nullable List<NameValuePair> params, @NonNull ResponseHandler<?> response)
	{
		return post("", params, null, null, response);
	}

	/**
	 * Performs a POST request on the baseUri
	 * @param headers The request headers for the connection
	 * @param response The response handler for the request
	 *
	 * @return The response object, or null
	 */
	@Nullable
	public E post(@Nullable Headers headers, @NonNull ResponseHandler<?> response)
	{
		return post("", null, null, headers, response);
	}

	/**
	 * Performs a POST request on the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param headers The request headers for the connection
	 * @param response The response handler for the request
	 *
	 * @return The response object, or null
	 */
	@Nullable
	public E post(@Nullable List<NameValuePair> params, @Nullable Headers headers, @NonNull ResponseHandler<?> response)
	{
		return post("", params, null, headers, response);
	}

	/**
	 * Performs a POST request on the baseUri
	 * @param postData The post data entity to post to the server
	 * @param response The response handler for the request
	 *
	 * @return The response object, or null
	 */
	@Nullable
	public E post(RequestBody postData, @NonNull ResponseHandler<?> response)
	{
		return post("", null, postData, null, response);
	}

	/**
	 * Performs a POST request on the baseUri
	 * @param postData The post data entity to post to the server
	 * @param headers The request headers for the connection
	 * @param response The response handler for the request
	 *
	 * @return The response object, or null
	 */
	@Nullable
	public E post(RequestBody postData, @Nullable Headers headers, @NonNull ResponseHandler<?> response)
	{
		return post("", null, postData, headers, response);
	}

	/**
	 * Performs a POST request on the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param postData The post data entity to post to the server
	 * @param response The response handler for the request
	 *
	 * @return The response object, or null
	 */
	@Nullable
	public E post(@Nullable List<NameValuePair> params, @Nullable RequestBody postData, @NonNull ResponseHandler<?> response)
	{
		return post("", params, postData, null, response);
	}

	/**
	 * Performs a POST request on the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param postData The post data entity to post to the server
	 * @param headers The request headers for the connection
	 * @param response The response handler for the request
	 *
	 * @return The response object, or null
	 */
	@Nullable
	public E post(@Nullable List<NameValuePair> params, @Nullable RequestBody postData, @Nullable Headers headers, @NonNull ResponseHandler<?> response)
	{
		return post("", params, postData, headers, response);
	}

	/**
	 * Performs a POST request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param response The response handler for the request
	 *
	 * @return The response object, or null
	 */
	@Nullable
	public E post(String path, @Nullable List<NameValuePair> params, @NonNull ResponseHandler<?> response)
	{
		return post(path, params, null, null, response);
	}

	/**
	 * Performs a POST request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param headers The request headers for the connection
	 * @param response The response handler for the request
	 *
	 * @return The response object, or null
	 */
	@Nullable
	public E post(String path, @Nullable Headers headers, @NonNull ResponseHandler<?> response)
	{
		return post(path, null, null, headers, response);
	}

	/**
	 * Performs a POST request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param headers The request headers for the connection
	 * @param response The response handler for the request
	 *
	 * @return The response object, or null
	 */
	@Nullable
	public E post(String path, @Nullable List<NameValuePair> params, @Nullable Headers headers, @NonNull ResponseHandler<?> response)
	{
		return post(path, params, null, headers, response);
	}

	/**
	 * Performs a POST request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param postData The post data entity to post to the server
	 * @param response The response handler for the request
	 *
	 * @return The response object, or null
	 */
	@Nullable
	public E post(String path, @Nullable RequestBody postData, @NonNull ResponseHandler<?> response)
	{
		return post(path, null, postData, null, response);
	}

	/**
	 * Performs a POST request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param postData The post data entity to post to the server
	 * @param headers The request headers for the connection
	 * @param response The response handler for the request
	 *
	 * @return The response object, or null
	 */
	@Nullable
	public E post(String path, @Nullable RequestBody postData, @Nullable Headers headers, @NonNull ResponseHandler<?> response)
	{
		return post(path, null, postData, headers, response);
	}

	/**
	 * Performs a POST request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param postData The post data entity to post to the server
	 * @param response The response handler for the request
	 *
	 * @return The response object, or null
	 */
	@Nullable
	public E post(String path, @Nullable List<NameValuePair> params, @Nullable RequestBody postData, @NonNull ResponseHandler<?> response)
	{
		return post(path, params, postData, null, response);
	}

	/**
	 * Performs a POST request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param postData The post data entity to post to the server
	 * @param headers The request headers for the connection
	 * @param response The response handler for the request
	 *
	 * @return The response object, or null
	 */
	@Nullable
	public E post(String path, @Nullable List<NameValuePair> params, @Nullable RequestBody postData, @Nullable Headers headers, @NonNull ResponseHandler<?> response)
	{
		if (!TextUtils.isEmpty(path))
		{
			requestUri = Uri.withAppendedPath(requestUri, path);
		}

		requestUri = RequestUtil.appendParams(requestUri, params);
		return executeTask(RequestMode.POST, requestUri, headers, postData, response);
	}

	/**
	 * Performs a PUT request on the baseUr
	 * @param response The response handler for the request
	 *
	 * @return The response object, or null
	 */
	@Nullable
	public E put(@NonNull ResponseHandler<?> response)
	{
		return put("", null, null, null, response);
	}

	/**
	 * Performs a PUT request on the baseUr
	 * @param path The path extended from the baseUri
	 *
	 * @return The response object, or null
	 */
	@Nullable
	public E put(String path)
	{
		return put(path, null, null, null, new ByteArrayResponseHandler());
	}

	/**
	 * Performs a PUT request on the baseUr
	 * @param path The path extended from the baseUri
	 * @param response The response handler for the request
	 *
	 * @return The response object, or null
	 */
	@Nullable
	public E put(String path, @NonNull ResponseHandler<?> response)
	{
		return put(path, null, null, null, response);
	}

	/**
	 * Performs a PUT request on the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param response The response handler for the request
	 *
	 * @return The response object, or null
	 */
	@Nullable
	public E put(@Nullable List<NameValuePair> params, @NonNull ResponseHandler<?> response)
	{
		return put("", params, null, null, response);
	}

	/**
	 * Performs a PUT request on the baseUri
	 * @param headers The request headers for the connection
	 * @param response The response handler for the request
	 *
	 * @return The response object, or null
	 */
	@Nullable
	public E put(@Nullable Headers headers, @NonNull ResponseHandler<?> response)
	{
		return put("", null, null, headers, response);
	}

	/**
	 * Performs a PUT request on the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param headers The request headers for the connection
	 * @param response The response handler for the request
	 *
	 * @return The response object, or null
	 */
	@Nullable
	public E put(@Nullable List<NameValuePair> params, @Nullable Headers headers, @NonNull ResponseHandler<?> response)
	{
		return put("", params, null, headers, response);
	}

	/**
	 * Performs a PUT request on the baseUri
	 * @param postData The post data entity to post to the server
	 * @param response The response handler for the request
	 *
	 * @return The response object, or null
	 */
	@Nullable
	public E put(RequestBody postData, @NonNull ResponseHandler<?> response)
	{
		return put("", null, postData, null, response);
	}

	/**
	 * Performs a PUT request on the baseUri
	 * @param postData The post data entity to post to the server
	 * @param headers The request headers for the connection
	 * @param response The response handler for the request
	 *
	 * @return The response object, or null
	 */
	@Nullable
	public E put(RequestBody postData, @Nullable Headers headers, @NonNull ResponseHandler<?> response)
	{
		return put("", null, postData, headers, response);
	}

	/**
	 * Performs a PUT request on the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param postData The post data entity to post to the server
	 * @param response The response handler for the request
	 *
	 * @return The response object, or null
	 */
	@Nullable
	public E put(@Nullable List<NameValuePair> params, @Nullable RequestBody postData, @NonNull ResponseHandler<?> response)
	{
		return put("", params, postData, null, response);
	}

	/**
	 * Performs a PUT request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param response The response handler for the request
	 *
	 * @return The response object, or null
	 */
	@Nullable
	public E put(String path, @Nullable List<NameValuePair> params, @NonNull ResponseHandler<?> response)
	{
		return put(path, params, null, null, response);
	}

	/**
	 * Performs a PUT request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param headers The request headers for the connection
	 * @param response The response handler for the request
	 *
	 * @return The response object, or null
	 */
	@Nullable
	public E put(String path, @Nullable Headers headers, @NonNull ResponseHandler<?> response)
	{
		return put(path, null, null, headers, response);
	}

	/**
	 * Performs a PUT request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param headers The request headers for the connection
	 * @param response The response handler for the request
	 *
	 * @return The response object, or null
	 */
	@Nullable
	public E put(String path, @Nullable List<NameValuePair> params, @Nullable Headers headers, @NonNull ResponseHandler<?> response)
	{
		return put(path, params, null, headers, response);
	}

	/**
	 * Performs a PUT request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param postData The post data entity to post to the server
	 * @param response The response handler for the request
	 *
	 * @return The response object, or null
	 */
	@Nullable
	public E put(String path, @Nullable RequestBody postData, @NonNull ResponseHandler<?> response)
	{
		return put(path, null, postData, null, response);
	}

	/**
	 * Performs a PUT request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param postData The post data entity to post to the server
	 * @param headers The request headers for the connection
	 * @param response The response handler for the request
	 *
	 * @return The response object, or null
	 */
	@Nullable
	public E put(String path, @Nullable RequestBody postData, @Nullable Headers headers, @NonNull ResponseHandler<?> response)
	{
		return put(path, null, postData, headers, response);
	}

	/**
	 * Performs a PUT request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param postData The post data entity to post to the server
	 * @param response The response handler for the request
	 *
	 * @return The response object, or null
	 */
	@Nullable
	public E put(String path, @Nullable List<NameValuePair> params, @Nullable RequestBody postData, @NonNull ResponseHandler<?> response)
	{
		return put(path, params, postData, null, response);
	}

	/**
	 * Performs a PUT request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param postData The post data entity to post to the server
	 * @param headers The request headers for the connection
	 * @param response The response handler for the request
	 *
	 * @return The response object, or null
	 */
	@Nullable
	public E put(String path, @Nullable List<NameValuePair> params, @Nullable RequestBody postData, @Nullable Headers headers, @NonNull ResponseHandler<?> response)
	{
		if (!TextUtils.isEmpty(path))
		{
			requestUri = Uri.withAppendedPath(requestUri, path);
		}

		requestUri = RequestUtil.appendParams(requestUri, params);
		return executeTask(RequestMode.PUT, requestUri, headers, postData, response);
	}

	/**
	 * Performs a PATCH request on the baseUr
	 * @param response The response handler for the request
	 *
	 * @return The response object, or null
	 */
	@Nullable
	public E patch(@NonNull ResponseHandler<?> response)
	{
		return patch("", null, null, null, response);
	}

	/**
	 * Performs a PATCH request on the baseUr
	 * @param path The path extended from the baseUri
	 *
	 * @return The response object, or null
	 */
	@Nullable
	public E patch(String path)
	{
		return patch(path, null, null, null, new ByteArrayResponseHandler());
	}

	/**
	 * Performs a PATCH request on the baseUr
	 * @param path The path extended from the baseUri
	 * @param response The response handler for the request
	 *
	 * @return The response object, or null
	 */
	@Nullable
	public E patch(String path, @NonNull ResponseHandler<?> response)
	{
		return patch(path, null, null, null, response);
	}

	/**
	 * Performs a PATCH request on the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param response The response handler for the request
	 *
	 * @return The response object, or null
	 */
	@Nullable
	public E patch(@Nullable List<NameValuePair> params, @NonNull ResponseHandler<?> response)
	{
		return patch("", params, null, null, response);
	}

	/**
	 * Performs a PATCH request on the baseUri
	 * @param headers The request headers for the connection
	 * @param response The response handler for the request
	 *
	 * @return The response object, or null
	 */
	@Nullable
	public E patch(@Nullable Headers headers, @NonNull ResponseHandler<?> response)
	{
		return patch("", null, null, headers, response);
	}

	/**
	 * Performs a PATCH request on the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param headers The request headers for the connection
	 * @param response The response handler for the request
	 *
	 * @return The response object, or null
	 */
	@Nullable
	public E patch(@Nullable List<NameValuePair> params, @Nullable Headers headers, @NonNull ResponseHandler<?> response)
	{
		return patch("", params, null, headers, response);
	}

	/**
	 * Performs a PATCH request on the baseUri
	 * @param postData The post data entity to post to the server
	 * @param response The response handler for the request
	 *
	 * @return The response object, or null
	 */
	@Nullable
	public E patch(RequestBody postData, @NonNull ResponseHandler<?> response)
	{
		return patch("", null, postData, null, response);
	}

	/**
	 * Performs a PATCH request on the baseUri
	 * @param postData The post data entity to post to the server
	 * @param headers The request headers for the connection
	 * @param response The response handler for the request
	 *
	 * @return The response object, or null
	 */
	@Nullable
	public E patch(RequestBody postData, @Nullable Headers headers, @NonNull ResponseHandler<?> response)
	{
		return patch("", null, postData, headers, response);
	}

	/**
	 * Performs a PATCH request on the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param postData The post data entity to post to the server
	 * @param response The response handler for the request
	 *
	 * @return The response object, or null
	 */
	@Nullable
	public E patch(@Nullable List<NameValuePair> params, @Nullable RequestBody postData, @NonNull ResponseHandler<?> response)
	{
		return patch("", params, postData, null, response);
	}

	/**
	 * Performs a PATCH request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param response The response handler for the request
	 *
	 * @return The response object, or null
	 */
	@Nullable
	public E patch(String path, @Nullable List<NameValuePair> params, @NonNull ResponseHandler<?> response)
	{
		return patch(path, params, null, null, response);
	}

	/**
	 * Performs a PATCH request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param headers The request headers for the connection
	 * @param response The response handler for the request
	 *
	 * @return The response object, or null
	 */
	@Nullable
	public E patch(String path, @Nullable Headers headers, @NonNull ResponseHandler<?> response)
	{
		return patch(path, null, null, headers, response);
	}

	/**
	 * Performs a PATCH request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param headers The request headers for the connection
	 * @param response The response handler for the request
	 *
	 * @return The response object, or null
	 */
	@Nullable
	public E patch(String path, @Nullable List<NameValuePair> params, @Nullable Headers headers, @NonNull ResponseHandler<?> response)
	{
		return patch(path, params, null, headers, response);
	}

	/**
	 * Performs a PATCH request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param postData The post data entity to post to the server
	 * @param response The response handler for the request
	 *
	 * @return The response object, or null
	 */
	@Nullable
	public E patch(String path, @Nullable RequestBody postData, @NonNull ResponseHandler<?> response)
	{
		return patch(path, null, postData, null, response);
	}

	/**
	 * Performs a PATCH request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param postData The post data entity to post to the server
	 * @param headers The request headers for the connection
	 * @param response The response handler for the request
	 *
	 * @return The response object, or null
	 */
	@Nullable
	public E patch(String path, @Nullable RequestBody postData, @Nullable Headers headers, @NonNull ResponseHandler<?> response)
	{
		return patch(path, null, postData, headers, response);
	}

	/**
	 * Performs a PATCH request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param postData The post data entity to post to the server
	 * @param response The response handler for the request
	 *
	 * @return The response object, or null
	 */
	@Nullable
	public E patch(String path, @Nullable List<NameValuePair> params, @Nullable RequestBody postData, @NonNull ResponseHandler<?> response)
	{
		return patch(path, params, postData, null, response);
	}

	/**
	 * Performs a PATCH request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param postData The post data entity to post to the server
	 * @param headers The request headers for the connection
	 * @param response The response handler for the request
	 *
	 * @return The response object, or null
	 */
	@Nullable
	public E patch(String path, @Nullable List<NameValuePair> params, @Nullable RequestBody postData, @Nullable Headers headers, @NonNull ResponseHandler<?> response)
	{
		if (!TextUtils.isEmpty(path))
		{
			requestUri = Uri.withAppendedPath(requestUri, path);
		}

		requestUri = RequestUtil.appendParams(requestUri, params);
		return executeTask(RequestMode.PATCH, requestUri, headers, postData, response);
	}

	private E executeTask(RequestMode mode, Uri uri, @Nullable Headers headers, @Nullable RequestBody sendData, ResponseHandler<?> requestProcessor)
	{
		if (headers == null)
		{
			headers = new Headers.Builder().build();
		}

		headers = headers.newBuilder().add("User-Agent", userAgent).build();

		executor = new ClientExecutorTask<E>(mode, uri, headers, sendData, requestProcessor, allowRedirect, allowAllSsl, requestTimeout);
		executor.preExecute();
		E response = executor.executeTask();
		executor.postExecute();

		return response;
	}

	/**
	 * Sets to allow all SSL. This is insecure, avoid using this method.
	 * @param allow Allow all SSL true/false
	 */
	public void setAllowAllSsl(boolean allow)
	{
		this.allowAllSsl = allow;
	}

	/**
	 * Sets to auto redirect on 302 responses
	 * @param allow Allow redirect true/false
	 */
	public void setAllowRedirect(boolean allow)
	{
		this.allowRedirect = allow;
	}

	/**
	 * Gets the connection info <b>after</b> a connection request has been made
	 * @return The connection info, or null
	 */
	public ConnectionInfo getConnectionInfo()
	{
		if (executor != null && executor.response != null)
		{
			return executor.response.getConnectionInfo();
		}

		return null;
	}
}
