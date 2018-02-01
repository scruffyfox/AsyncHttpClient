package net.callumtaylor.asynchttp.obj

/**
 * // TODO: Add class description
 */
data class Request(
	var path: String = "",
	var queryParams: ArrayList<kotlin.Pair<String, String>> = arrayListOf(),
	var headers: ArrayList<kotlin.Pair<String, String>> = arrayListOf(),
	var type: String = "GET",

	var followRedirects: Boolean = true,
	var allowAllSSl: Boolean = false,

	// request params populated by request
	var time: Long = 0,
	var length: Long = 0
)
