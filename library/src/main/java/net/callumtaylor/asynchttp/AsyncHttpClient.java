package net.callumtaylor.asynchttp;

import android.annotation.TargetApi;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Build;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import net.callumtaylor.asynchttp.obj.ConnectionInfo;
import net.callumtaylor.asynchttp.obj.NameValuePair;
import net.callumtaylor.asynchttp.obj.Packet;
import net.callumtaylor.asynchttp.obj.RequestMode;
import net.callumtaylor.asynchttp.obj.RequestUtil;
import net.callumtaylor.asynchttp.response.ResponseHandler;

import java.util.List;

import okhttp3.Headers;
import okhttp3.RequestBody;

/**
 * The client class used for initiating HTTP requests using an AsyncTask. It
 * follows a RESTful paradigm for the connections with the 7 possible methods,
 * GET, POST, PUT, DELETE, OPTIONS, PATCH, HEAD.
 *
 * <b>Note:</b> Because AsyncHttpClient uses
 * AsyncTask, only one instance can be created at a time. If one client makes 2
 * requests, the first request is canceled for the new request. You can either
 * wait for the first to finish before making the second, or you can create two
 * separate instances.
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
 *
 * The library uses OKHttp for its HTTP request client so follows the same patterns for request bodies and headers.
 * <p />
 * Example GET
 * <code>
 	new AsyncHttpClient("http://httpbin.org/")
		.get("get", new JsonResponseHandler()
		{
			@Override public void onFinish()
			{

			}
		});
 * </code>
 * <p />
 * Example POST
 * <code>
 	RequestBody postBody = RequestBody.create(MediaType.parse("application/json"), "{\"test\":\"hello world\"}");

	new AsyncHttpClient("http://httpbin.org/")
		.post("post", postBody, new JsonResponseHandler()
		{
			@Override public void onFinish()
			{

			}
		});
 * </code>
 * <p />
 * Example progress for large content respose/post
 * <code>
	RequestBody postBody = MultipartBody.create(MediaType.parse("application/octet-stream"), new byte[1024 * 12]);

	new AsyncHttpClient("http://httpbin.org/")
		.post("post", postBody, new JsonResponseHandler()
		{
			@Override public void onByteChunkSentProcessed(long totalProcessed, long totalLength)
			{
				// Post to UI progress dialog
			}

			@Override public void onFinish()
			{

			}
		});
 * </code>
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

	private AsyncClientExecutorTask executorTask;
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
	public void get(@Nullable ResponseHandler response)
	{
		get("", null, null, response);
	}

	/**
	 * Performs a GET request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param response The response handler for the request
	 */
	public void get(String path, @Nullable ResponseHandler response)
	{
		get(path, null, null, response);
	}

	/**
	 * Performs a GET request on the baseUri
	 * @param headers The request headers for the connection
	 * @param response The response handler for the request
	 */
	public void get(@Nullable Headers headers, @Nullable ResponseHandler response)
	{
		get("", null, headers, response);
	}

	/**
	 * Performs a GET request on the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param headers The request headers for the connection
	 * @param response The response handler for the request
	 */
	public void get(@Nullable List<NameValuePair> params, @Nullable Headers headers, @Nullable ResponseHandler response)
	{
		get("", params, headers, response);
	}

	/**
	 * Performs a GET request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param response The response handler for the request
	 */
	public void get(String path, @Nullable List<NameValuePair> params, @Nullable ResponseHandler response)
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
	public void get(String path, @Nullable List<NameValuePair> params, @Nullable Headers headers, @Nullable ResponseHandler response)
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
	public void options(@Nullable ResponseHandler response)
	{
		options("", null, null, response);
	}

	/**
	 * Performs a OPTIONS request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param response The response handler for the request
	 */
	public void options(String path, @Nullable ResponseHandler response)
	{
		options(path, null, null, response);
	}

	/**
	 * Performs a OPTIONS request on the baseUri
	 * @param headers The request headers for the connection
	 * @param response The response handler for the request
	 */
	public void options(@Nullable Headers headers, @Nullable ResponseHandler response)
	{
		options("", null, headers, response);
	}

	/**
	 * Performs a OPTIONS request on the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param headers The request headers for the connection
	 * @param response The response handler for the request
	 */
	public void options(@Nullable List<NameValuePair> params, @Nullable Headers headers, @Nullable ResponseHandler response)
	{
		options("", params, headers, response);
	}

	/**
	 * Performs a OPTIONS request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param response The response handler for the request
	 */
	public void options(String path, @Nullable List<NameValuePair> params, @Nullable ResponseHandler response)
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
	public void options(String path, @Nullable List<NameValuePair> params, @Nullable Headers headers, @Nullable ResponseHandler response)
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
	public void head(@Nullable ResponseHandler response)
	{
		head("", null, null, response);
	}

	/**
	 * Performs a HEAD request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param response The response handler for the request
	 */
	public void head(String path, @Nullable ResponseHandler response)
	{
		head(path, null, null, response);
	}

	/**
	 * Performs a HEAD request on the baseUri
	 * @param headers The request headers for the connection
	 * @param response The response handler for the request
	 */
	public void head(@Nullable Headers headers, @Nullable ResponseHandler response)
	{
		head("", null, headers, response);
	}

	/**
	 * Performs a HEAD request on the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param headers The request headers for the connection
	 * @param response The response handler for the request
	 */
	public void head(@Nullable List<NameValuePair> params, @Nullable Headers headers, @Nullable ResponseHandler response)
	{
		head("", params, headers, response);
	}

	/**
	 * Performs a HEAD request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param response The response handler for the request
	 */
	public void head(String path, @Nullable List<NameValuePair> params, @Nullable ResponseHandler response)
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
	public void head(String path, @Nullable List<NameValuePair> params, @Nullable Headers headers, @Nullable ResponseHandler response)
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
	public void delete(@Nullable ResponseHandler response)
	{
		delete("", null, null, null, response);
	}

	/**
	 * Performs a DELETE request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param response The response handler for the request
	 */
	public void delete(String path, @Nullable ResponseHandler response)
	{
		delete(path, null, null, null, response);
	}

	/**
	 * Performs a DELETE request on the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param response The response handler for the request
	 */
	public void delete(@Nullable List<NameValuePair> params, @Nullable ResponseHandler response)
	{
		delete("", params, null, null, response);
	}

	/**
	 * Performs a DELETE request on the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param headers The request headers for the connection
	 * @param response The response handler for the request
	 */
	public void delete(@Nullable List<NameValuePair> params, @Nullable Headers headers, @Nullable ResponseHandler response)
	{
		delete("", params, null, headers, response);
	}

	/**
	 * Performs a DELETE request on the baseUri
	 * @param postData The post data entity to post to the server
	 * @param response The response handler for the request
	 */
	public void delete(@Nullable RequestBody postData, @Nullable ResponseHandler response)
	{
		delete("", null, postData, null, response);
	}

	/**
	 * Performs a DELETE request on the baseUri
	 * @param postData The post data entity to post to the server
	 * @param headers The request headers for the connection
	 * @param response The response handler for the request
	 */
	public void delete(@Nullable RequestBody postData, @Nullable Headers headers, @Nullable ResponseHandler response)
	{
		delete("", null, postData, headers, response);
	}

	/**
	 * Performs a DELETE request on the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param postData The post data entity to post to the server
	 * @param response The response handler for the request
	 */
	public void delete(@Nullable List<NameValuePair> params, @Nullable RequestBody postData, @Nullable ResponseHandler response)
	{
		delete("", params, postData, null, response);
	}

	/**
	 * Performs a DELETE request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param response The response handler for the request
	 */
	public void delete(String path, @Nullable List<NameValuePair> params, @Nullable ResponseHandler response)
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
	public void delete(String path, @Nullable List<NameValuePair> params, @Nullable Headers headers, @Nullable ResponseHandler response)
	{
		delete(path, params, null, headers, response);
	}

	/**
	 * Performs a DELETE request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param postData The post data entity to post to the server
	 * @param response The response handler for the request
	 */
	public void delete(String path, @Nullable RequestBody postData, @Nullable ResponseHandler response)
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
	public void delete(String path, @Nullable RequestBody postData, @Nullable Headers headers, @Nullable ResponseHandler response)
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
	public void delete(String path, @Nullable List<NameValuePair> params, @Nullable RequestBody postData, @Nullable ResponseHandler response)
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
	public void delete(String path, @Nullable List<NameValuePair> params, @Nullable RequestBody postData, @Nullable Headers headers, @Nullable ResponseHandler response)
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
	public void post(@Nullable ResponseHandler response)
	{
		post("", null, null, null, response);
	}

	/**
	 * Performs a POST request on the baseUr
	 * @param path The path extended from the baseUri
	 * @param response The response handler for the request
	 */
	public void post(String path, @Nullable ResponseHandler response)
	{
		post(path, null, null, null, response);
	}

	/**
	 * Performs a POST request on the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param response The response handler for the request
	 */
	public void post(@Nullable List<NameValuePair> params, @Nullable ResponseHandler response)
	{
		post("", params, null, null, response);
	}

	/**
	 * Performs a POST request on the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param headers The request headers for the connection
	 * @param response The response handler for the request
	 */
	public void post(@Nullable List<NameValuePair> params, @Nullable Headers headers, @Nullable ResponseHandler response)
	{
		post("", params, null, headers, response);
	}

	/**
	 * Performs a POST request on the baseUri
	 * @param postData The post data entity to post to the server
	 * @param response The response handler for the request
	 */
	public void post(@Nullable RequestBody postData, @Nullable ResponseHandler response)
	{
		post("", null, postData, null, response);
	}

	/**
	 * Performs a POST request on the baseUri
	 * @param postData The post data entity to post to the server
	 * @param headers The request headers for the connection
	 * @param response The response handler for the request
	 */
	public void post(@Nullable RequestBody postData, @Nullable Headers headers, @Nullable ResponseHandler response)
	{
		post("", null, postData, headers, response);
	}

	/**
	 * Performs a POST request on the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param postData The post data entity to post to the server
	 * @param response The response handler for the request
	 */
	public void post(@Nullable List<NameValuePair> params, @Nullable RequestBody postData, @Nullable ResponseHandler response)
	{
		post("", params, postData, null, response);
	}

	/**
	 * Performs a POST request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param response The response handler for the request
	 */
	public void post(String path, @Nullable List<NameValuePair> params, @Nullable ResponseHandler response)
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
	public void post(String path, @Nullable List<NameValuePair> params, @Nullable Headers headers, @Nullable ResponseHandler response)
	{
		post(path, params, null, headers, response);
	}

	/**
	 * Performs a POST request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param postData The post data entity to post to the server
	 * @param response The response handler for the request
	 */
	public void post(String path, @Nullable RequestBody postData, @Nullable ResponseHandler response)
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
	public void post(String path, @Nullable RequestBody postData, @Nullable Headers headers, @Nullable ResponseHandler response)
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
	public void post(String path, @Nullable List<NameValuePair> params, @Nullable RequestBody postData, @Nullable ResponseHandler response)
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
	public void post(String path, @Nullable List<NameValuePair> params, @Nullable RequestBody postData, @Nullable Headers headers, @Nullable ResponseHandler response)
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
	public void put(@Nullable ResponseHandler response)
	{
		put("", null, null, null, response);
	}

	/**
	 * Performs a PUT request on the baseUr
	 * @param path The path extended from the baseUri
	 * @param response The response handler for the request
	 */
	public void put(String path, @Nullable ResponseHandler response)
	{
		put(path, null, null, null, response);
	}

	/**
	 * Performs a PUT request on the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param response The response handler for the request
	 */
	public void put(@Nullable List<NameValuePair> params, @Nullable ResponseHandler response)
	{
		put("", params, null, null, response);
	}

	/**
	 * Performs a PUT request on the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param headers The request headers for the connection
	 * @param response The response handler for the request
	 */
	public void put(@Nullable List<NameValuePair> params, @Nullable Headers headers, @Nullable ResponseHandler response)
	{
		put("", params, null, headers, response);
	}

	/**
	 * Performs a PUT request on the baseUri
	 * @param postData The post data entity to post to the server
	 * @param response The response handler for the request
	 */
	public void put(@Nullable RequestBody postData, @Nullable ResponseHandler response)
	{
		put("", null, postData, null, response);
	}

	/**
	 * Performs a PUT request on the baseUri
	 * @param postData The post data entity to post to the server
	 * @param headers The request headers for the connection
	 * @param response The response handler for the request
	 */
	public void put(@Nullable RequestBody postData, @Nullable Headers headers, @Nullable ResponseHandler response)
	{
		put("", null, postData, headers, response);
	}

	/**
	 * Performs a PUT request on the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param postData The post data entity to post to the server
	 * @param response The response handler for the request
	 */
	public void put(@Nullable List<NameValuePair> params, @Nullable RequestBody postData, @Nullable ResponseHandler response)
	{
		put("", params, postData, null, response);
	}

	/**
	 * Performs a PUT request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param response The response handler for the request
	 */
	public void put(String path, @Nullable List<NameValuePair> params, @Nullable ResponseHandler response)
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
	public void put(String path, @Nullable List<NameValuePair> params, @Nullable Headers headers, @Nullable ResponseHandler response)
	{
		put(path, params, null, headers, response);
	}

	/**
	 * Performs a PUT request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param postData The post data entity to post to the server
	 * @param response The response handler for the request
	 */
	public void put(String path, @Nullable RequestBody postData, @Nullable ResponseHandler response)
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
	public void put(String path, @Nullable RequestBody postData, @Nullable Headers headers, @Nullable ResponseHandler response)
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
	public void put(String path, @Nullable List<NameValuePair> params, @Nullable RequestBody postData, @Nullable ResponseHandler response)
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
	public void put(String path, @Nullable List<NameValuePair> params, @Nullable RequestBody postData, @Nullable Headers headers, @Nullable ResponseHandler response)
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
	public void patch(@Nullable ResponseHandler response)
	{
		patch("", null, null, null, response);
	}

	/**
	 * Performs a PATCH request on the baseUr
	 * @param path The path extended from the baseUri
	 * @param response The response handler for the request
	 */
	public void patch(String path, @Nullable ResponseHandler response)
	{
		patch(path, null, null, null, response);
	}

	/**
	 * Performs a PATCH request on the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param response The response handler for the request
	 */
	public void patch(@Nullable List<NameValuePair> params, @Nullable ResponseHandler response)
	{
		patch("", params, null, null, response);
	}

	/**
	 * Performs a PATCH request on the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param headers The request headers for the connection
	 * @param response The response handler for the request
	 */
	public void patch(@Nullable List<NameValuePair> params, @Nullable Headers headers, @Nullable ResponseHandler response)
	{
		patch("", params, null, headers, response);
	}

	/**
	 * Performs a PATCH request on the baseUri
	 * @param postData The post data entity to post to the server
	 * @param response The response handler for the request
	 */
	public void patch(@Nullable RequestBody postData, @Nullable ResponseHandler response)
	{
		patch("", null, postData, null, response);
	}

	/**
	 * Performs a PATCH request on the baseUri
	 * @param postData The post data entity to post to the server
	 * @param headers The request headers for the connection
	 * @param response The response handler for the request
	 */
	public void patch(@Nullable RequestBody postData, @Nullable Headers headers, @Nullable ResponseHandler response)
	{
		patch("", null, postData, headers, response);
	}

	/**
	 * Performs a PATCH request on the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param postData The post data entity to post to the server
	 * @param response The response handler for the request
	 */
	public void patch(@Nullable List<NameValuePair> params, @Nullable RequestBody postData, @Nullable ResponseHandler response)
	{
		patch("", params, postData, null, response);
	}

	/**
	 * Performs a PATCH request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param params The Query params to append to the baseUri
	 * @param response The response handler for the request
	 */
	public void patch(String path, @Nullable List<NameValuePair> params, @Nullable ResponseHandler response)
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
	public void patch(String path, @Nullable List<NameValuePair> params, @Nullable Headers headers, @Nullable ResponseHandler response)
	{
		patch(path, params, null, headers, response);
	}

	/**
	 * Performs a PATCH request on the baseUri
	 * @param path The path extended from the baseUri
	 * @param postData The post data entity to post to the server
	 * @param response The response handler for the request
	 */
	public void patch(String path, @Nullable RequestBody postData, @Nullable ResponseHandler response)
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
	public void patch(String path, @Nullable RequestBody postData, @Nullable Headers headers, @Nullable ResponseHandler response)
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
	public void patch(String path, @Nullable List<NameValuePair> params, @Nullable RequestBody postData, @Nullable ResponseHandler response)
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
	public void patch(String path, @Nullable List<NameValuePair> params, @Nullable RequestBody postData, @Nullable Headers headers, @Nullable ResponseHandler response)
	{
		if (!TextUtils.isEmpty(path))
		{
			requestUri = Uri.withAppendedPath(requestUri, path);
		}

		requestUri = RequestUtil.appendParams(requestUri, params);
		executeTask(RequestMode.PATCH, requestUri, headers, postData, response);
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void executeTask(RequestMode mode, Uri uri, @Nullable Headers headers, RequestBody sendData, @Nullable ResponseHandler response)
	{
		if (executorTask != null || (executorTask != null && (executorTask.getStatus() == Status.RUNNING || executorTask.getStatus() == Status.PENDING)))
		{
			executorTask.cancel(true);
			executorTask = null;
		}

		executorTask = new AsyncClientExecutorTask(mode, uri, headers, sendData, response, allowRedirect, allowAllSsl, requestTimeout);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
		{
			executorTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		}
		else
		{
			executorTask.execute();
		}
	}

	/**
	 * Delegate wrapper class for ClientExecutorTask inside an AsyncTask
	 */
	protected static class AsyncClientExecutorTask extends AsyncTask<Void, Packet, Void>
	{
		private ClientExecutorTask clientTask;

		public AsyncClientExecutorTask(RequestMode mode, Uri request, @Nullable Headers headers, @Nullable RequestBody postData, @Nullable ResponseHandler response, boolean allowRedirect, boolean allowAllSsl, long requestTimeout)
		{
			if (headers == null)
			{
				headers = new Headers.Builder().build();
			}

			headers = headers.newBuilder().add("User-Agent", userAgent).build();

			clientTask = new ClientExecutorTask(mode, request, headers, postData, response, allowRedirect, allowAllSsl, requestTimeout)
			{
				@Override public void transferProgress(Packet... values)
				{
					publishProgress(values);
				}
			};
		}

		@Override protected void onCancelled()
		{
			clientTask.cancel();
		}

		@Override protected void onPreExecute()
		{
			clientTask.preExecute();
		}

		@Override protected Void doInBackground(Void... params)
		{
			clientTask.executeTask();
			return null;
		}

		@Override protected void onPostExecute(Void aVoid)
		{
			clientTask.postExecute();
		}

		@Override protected void onProgressUpdate(Packet... values)
		{
			if (clientTask.response != null && !isCancelled())
			{
				if (values[0].isDownload)
				{
					clientTask.response.onByteChunkReceivedProcessed(values[0].length, values[0].total);
				}
				else
				{
					clientTask.response.onByteChunkSentProcessed(values[0].length, values[0].total);
				}
			}
		}
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
}
