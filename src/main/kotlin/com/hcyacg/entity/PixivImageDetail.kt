package com.hcyacg.entity

import kotlinx.serialization.Serializable

import kotlinx.serialization.SerialName

@Serializable
data class PixivImageDetail(
    @SerialName("illust")
    val illust: String? = "",
    @SerialName("title")
    val title: String? = "",
    @SerialName("type")
    val type: String? = "",
    @SerialName("caption")
    val caption: String? = "",
    @SerialName("restrict")
    val restrict: Int? = 0,
    @SerialName("createDate")
    val createDate: String? = "",
    @SerialName("pageCount")
    val pageCount: Int? = 0,
    @SerialName("width")
    val width: Int? = 0,
    @SerialName("height")
    val height: Int? = 0,
    @SerialName("sanityLevel")
    val sanityLevel: Int? = 0,
    @SerialName("xRestrict")
    val xRestrict: Int? = 0,
    @SerialName("totalView")
    val totalView: Int? = 0,
    @SerialName("totalBookmarks")
    val totalBookmarks: Int? = 0,
    @SerialName("isBookmarked")
    val isBookmarked: Boolean? = false,
    @SerialName("visible")
    val visible: Boolean? = true,
    @SerialName("isMuted")
    val isMuted: Boolean? = false,
    @SerialName("totalComments")
    val totalComments: Int? = 0,
    @SerialName("large")
    val large: String? = "",
    @SerialName("user")
    val user: User? = User(),
    @SerialName("tags")
    val tags: List<Tag>? = listOf(),
    @SerialName("originals")
    val originals: List<Original>? = listOf(),
)

@Serializable
data class User(
    @SerialName("id")
    val id: Int? = 0,
    @SerialName("name")
    val name: String? = "",
    @SerialName("account")
    val account: String? = "",
    @SerialName("profileImageUrls")
    val profileImageUrls: String? = "",
    @SerialName("is_followed")
    val isFollowed: Boolean? = false,
)

@Serializable
data class Tag(
    @SerialName("name")
    val name: String? = "",
)

@Serializable
data class Original(
    @SerialName("url")
    val url: String? = "",
)


//
//object MetaPagesSerializer : JsonTransformingSerializer<List<>>(List.serializer()) {
//    // If response is an array, then return a empty Icon
//    override fun transformDeserialize(element: JsonElement): JsonElement {
//        return if (element is JsonArray || element is List<*>) {
//            var name = ""
//            for (index in 0 until element.jsonArray.size) {
//                name = if (index == element.jsonArray.size - 1) {
//                    name.plus(element.jsonArray[index].jsonPrimitive.content)
//                } else {
//                    name.plus(element.jsonArray[index].jsonPrimitive.content).plus("和")
//                }
//            }
//            Json.encodeToJsonElement(name)
//        } else {
//            element
//        }
//    }
//}
