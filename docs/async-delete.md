#AsyncHttpClient Example DELETE

###Example DELETE

```java
	AsyncHttpClient client = new AsyncHttpClient("http://example.com");
		
	client.delete("api/v1/", params, headers, new JsonResponseHandler()
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