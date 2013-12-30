#AsyncHttpClient Example GET

###Example GET

```java
	AsyncHttpClient client = new AsyncHttpClient("http://example.com");
	client.get("api/v1/", new JsonResponseHandler()
	{
		@Override public void onSuccess()
		{
			JsonElement result = getContent();
		}
	});
```
	
###Example GET with parameters and headers

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

###Example GET - Downloading a large file directly to cache

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