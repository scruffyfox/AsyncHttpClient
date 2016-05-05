#AsyncHttpClient Example DELETE

###Example DELETE

```java
	AsyncHttpClient client = new AsyncHttpClient("http://example.com");
	client.delete("api/v1/", new JsonResponseHandler()
	{
		@Override public void onSuccess()
		{
			JsonElement result = getContent();
		}
	});
```

###Example DELETE with parameters and headers

```java
	AsyncHttpClient client = new AsyncHttpClient("http://example.com");

	List<NameValuePair> params = new ArrayList<>();
	params.add(new NameValuePair("key", "value"));

	Headers headers = Headers.of("Header", "value");

	client.delete("api/v1/", params, headers, new JsonResponseHandler()
	{
		@Override public void onSuccess()
		{
			JsonElement result = getContent();
		}
	});
```
