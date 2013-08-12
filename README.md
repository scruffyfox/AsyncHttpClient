#AsyncHttpClient by Callum Taylor

This is the new and improved version of AsyncHttpClient taken from X-Library. It was long due a re-write.

In this version it allows a more flexible usage of posting files, http entities and GZIP handling.

Read the full documentation [http://scruffyfox.github.com/AsyncHttpClient](http://scruffyfox.github.com/AsyncHttpClient)

#Downloading large files

In order to download large files, you will need to subclass `AsyncHttpResponseHandler` and override the `onPublishedDownloadProgress()` method to write directly to cache instead of appending to a `ByteArrayOutputStream` which is what the standard `BinaryResponseHandler` does. This is to stop OOM due to a over-sized output stream.

#Usage example

**Note:** Because AsyncHttpClient uses AsyncTask, only one instance can be created at a time. If one client makes 2 requests, the first request is canceled for the new request. You can either wait for the first to finish before making the second, or you can create two seperate instances.

##Example GET

```java
	AsyncHttpClient client = new AsyncHttpClient("http://example.com");
	List<NameValuePair> params = new ArrayList<NameValuePair>();
	params.add(new BasicNameValuePair("key", "value"));
	
	List<Header> headers = new ArrayList<Header>();
	headers.add(new BasicHeader("1", "2"));
	
	client.get("api/v1/", params, headers, new JsonResponseHandler()
	{
		@Override public void onSuccess()
		{
			JsonElement result = getContent();
		}
	});
```

##Example GET - Downloading a large file directly to cache

```java
	AsyncHttpClient client = new AsyncHttpClient("http://example.com");
	
	client.get("api/v1/", new CacheResponseHandler("file.bin")
	{
		@Override public void onSuccess()
		{
			File result = getContent();
			boolean exists = result.exists();
		}
	});
```

##Example DELETE

```java
	AsyncHttpClient client = new AsyncHttpClient("http://example.com");
	List<NameValuePair> params = new ArrayList<NameValuePair>();
	params.add(new BasicNameValuePair("key", "value"));
	
	List<Header> headers = new ArrayList<Header>();
	headers.add(new BasicHeader("1", "2"));
	
	client.delete("api/v1/", params, headers, new JsonResponseHandler()
	{
		@Override public void onSuccess()
		{
			JsonElement result = getContent();
		}
	});
```

##Example POST - Single Entity

```java
	AsyncHttpClient client = new AsyncHttpClient("http://example.com");
	List<NameValuePair> params = new ArrayList<NameValuePair>();
	params.add(new BasicNameValuePair("key", "value"));
	
	List<Header> headers = new ArrayList<Header>();
	headers.add(new BasicHeader("1", "2"));
	
	JsonEntity data = new JsonEntity("{\"key\":\"value\"}");
	GzippedEntity entity = new GzippedEntity(data);
	
	client.post("api/v1/", params, entity, headers, new JsonResponseHandler()
	{
		@Override public void onSuccess()
		{
			JsonElement result = getContent();
		}
	});
```

##Example POST - URL Encoded post data

```java
	AsyncHttpClient client = new AsyncHttpClient("http://example.com");
	
	RequestEntity entity = new RequestEntity();
	entity.add("key", "value");
	
	client.post("api/v1/", entity, new JsonResponseHandler()
	{
		@Override public void onSuccess()
		{
			JsonElement result = getContent();
		}
	});
```

##Example POST - Multiple Entity + file

```java
	AsyncHttpClient client = new AsyncHttpClient("http://example.com");
	List<NameValuePair> params = new ArrayList<NameValuePair>();
	params.add(new BasicNameValuePair("key", "value"));
	
	List<Header> headers = new ArrayList<Header>();
	headers.add(new BasicHeader("1", "2"));
	
	MultiPartEntity entity = new MultiPartEntity();
	FileBody data1 = new FileBody(new File("/IMG_6614.JPG"), "image/jpeg");
	JsonEntity data2 = new JsonEntity("{\"key\":\"value\"}");
	entity.addPart("image1.jpg", data1);
	entity.addPart("content1", data2);
	
	client.post("api/v1/", params, entity, headers, new JsonResponseHandler()
	{
		@Override public void onSuccess()
		{
			JsonElement result = getContent();
		}
	});
```

##Example PUT

```java
	AsyncHttpClient client = new AsyncHttpClient("http://example.com");
	List<NameValuePair> params = new ArrayList<NameValuePair>();
	params.add(new BasicNameValuePair("key", "value"));
	
	List<Header> headers = new ArrayList<Header>();
	headers.add(new BasicHeader("1", "2"));
	
	JsonEntity data = new JsonEntity("{\"key\":\"value\"}");
	GzippedEntity entity = new GzippedEntity(data);
	
	client.post("api/v1/", params, entity, headers, new JsonResponseHandler()
	{
		@Override public void onSuccess()
		{
			JsonElement result = getContent();
		}
	});
```

Because of the nature of REST, GET and DELETE requests behave in the same
way, POST and PUT requests also behave in the same way.