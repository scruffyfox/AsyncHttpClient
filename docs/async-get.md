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

	List<NameValuePair> params = new ArrayList<>();
	params.add(new NameValuePair("key", "value"));

	Headers headers = Headers.of("Header", "value");

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
