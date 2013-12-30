#AsyncHttpClient Example DELETE

###Example DELETE

```java
	SyncHttpClient<JsonElement> client = new SyncHttpClient<JsonElement>("http://example.com");
	JsonElement response = client.delete("api/v1/", new JsonProcessor());
```

###Example DELETE with parameters and headers

```java
	SyncHttpClient<JsonElement> client = new SyncHttpClient<JsonElement>("http://example.com");
	List<NameValuePair> params = new ArrayList<NameValuePair>();
	params.add(new BasicNameValuePair("key", "value"));
	
	List<Header> headers = new ArrayList<Header>();
	headers.add(new BasicHeader("1", "2"));
	 
	JsonElement response = client.delete("api/v1/", params, headers, new JsonProcessor());
```