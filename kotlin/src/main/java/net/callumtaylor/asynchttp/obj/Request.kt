package net.callumtaylor.asynchttp.obj

import okhttp3.RequestBody

/**
 * // TODO: Add class description
 */
data class Request(
	var path: String = "",
	var queryParams: ArrayList<kotlin.Pair<String, String>> = arrayListOf(),
	var headers: ArrayList<kotlin.Pair<String, String>> = arrayListOf(),
	var body: RequestBody? = null,
	var type: String = "",

	// request options
	var followRedirects: Boolean = true,
	var allowAllSSl: Boolean = false,

	// request params populated by request
	var time: Long = 0,
	var length: Long = 0
)
