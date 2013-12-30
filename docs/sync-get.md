#AsyncHttpClient Example GET

###Example GET

```java
	SyncHttpClient<JsonElement> client = new SyncHttpClient<JsonElement>("http://example.com");
	JsonElement response = client.get("api/v1/", new JsonProcessor());
```

###Example GET with parameters and headers

```java
	SyncHttpClient<JsonElement> client = new SyncHttpClient<JsonElement>("http://example.com");
	List<NameValuePair> params = new ArrayList<NameValuePair>();
	params.add(new BasicNameValuePair("key", "value"));
	
	List<Header> headers = new ArrayList<Header>();
	headers.add(new BasicHeader("1", "2"));
	 
	JsonElement response = client.get("api/v1/", params, headers, new JsonProcessor());
```