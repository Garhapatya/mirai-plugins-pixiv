package com.hcyacg.entity

import kotlinx.serialization.Serializable

import kotlinx.serialization.SerialName


@Serializable
data class PixivImageDetail(
    @SerialName("id")
    val id: Int? = null,
    @SerialName("title")
    val title: String? = "",
    @SerialName("type")
    val type: String? = "",
    @SerialName("image_urls")
    val imageUrls: ImageUrls? = ImageUrls(),
    @SerialName("caption")
    val caption: String? = "",
    @SerialName("restrict")
    val restrict: Int? = 0,
    @SerialName("user")
    val user: User? = User(),
    @SerialName("tags")
    val tags: List<Tag>? = listOf(),
    @SerialName("Tools")
    val tools: List<String>? = listOf(),
    @SerialName("create_date")
    val createDate: String? = "",
    @SerialName("page_count")
    val pageCount: Int? = 0,
    @SerialName("width")
    val width: Double ? = 0.0,
    @SerialName("height")
    val height: Double ? = 0.0,
    @SerialName("sanity_level")
    val sanityLevel: Int? = 0,
    @SerialName("x_restrict")
    val xRestrict: Int? = 0,
    @SerialName("series")
    val series: Series? = Series(),
    @SerialName("meta_single_page")
    val metaSinglePage: MetaSinglePage? = MetaSinglePage(),
    @SerialName("meta_pages")
    val metaPages: List<MetaPage>? = listOf(),
    @SerialName("total_view")
    val totalView: Int? = 0,
    @SerialName("total_bookmarks")
    val totalBookmarks: Int? = 0,
    @SerialName("is_bookmarked")
    val isBookmarked: Boolean? = false,
    @SerialName("visible")
    val visible: Boolean? = true,
    @SerialName("is_muted")
    val isMuted: Boolean? = false,
    @SerialName("total_comments")
    val totalComments: Int? = 0,
    @SerialName("illust_ai_type")
    val illustType: Int? = 0,
    @SerialName("illust_book_style")
    val illustStyle: Int? = 0,
    @SerialName("request")
    val request: Request? = Request(),
    @SerialName("comment_access_control")
    val commentAccessControl: Int? = 0,
    

)

@Serializable
data class ImageUrls(
    @SerialName("square_medium")
    val squareMedium: String? = null,
    @SerialName("medium")
    val medium: String? = null,
    @SerialName("large")
    val large: String? = null,
)

@Serializable
data class User(
    @SerialName("id")
    val id: Int? = null,
    @SerialName("name")
    val name: String? = "",
    @SerialName("account")
    val account: String? = "",
    @SerialName("profile_image_urls")
    val profileImageUrls: ProfileImageUrl? = ProfileImageUrl(),
    @SerialName("is_followed")
    val isFollowed: Boolean? = false,
    @SerialName("is_accept_request")
    val isAcceptRequest: Boolean? = false,
)

@Serializable
data class ProfileImageUrl(
    @SerialName("medium")
    val medium: String? = null,
)

@Serializable
data class Tag(
    @SerialName("name")
    val name: String? = "",
    @SerialName("translated_name")
    val translatedName: String? = "",
)

@Serializable
data class Series(
    @SerialName("id")
    val id: Int? = null,
    @SerialName("title")
    val title: String? = "",
)

@Serializable
data class MetaSinglePage(
    @SerialName("original_image_url")
    val originalImageUrl: String? = null,
)

@Serializable
data class MetaPage(
    @SerialName("image_urls")
    val imageUrls: ImageUrl? = ImageUrl(),
)

@Serializable
data class ImageUrl(
    @SerialName("square_medium")
    val squareMedium: String? = "",
    @SerialName("medium")
    val medium: String? = "",
    @SerialName("large")
    val large: String? = "",
    @SerialName("original")
    val original: String? = "",
)

@Serializable
data class Request(
    @SerialName("request_info")
    val requestInfo: RequestInfo? = RequestInfo(),
    @SerialName("request_users")
    val requestUsers: List<RequestUser>? = listOf(),
    )

@Serializable
data class RequestInfo(
    @SerialName("fan_user_id")
    val fanUserId: Int? = null,
    @SerialName("collaborate_status")
    val collaborateStatus: CollaborateStatus? = CollaborateStatus(),
    @SerialName("role")
    val role: String? = null,
)

@Serializable
data class CollaborateStatus(
    @SerialName("collaborating")
    val collaborating: Boolean? = false,
    @SerialName("collaborate_anonymous_flag")
    val collaborateAnonymousFlag: Boolean? = false,
    @SerialName("collaborate_user_samples")
    val collaborateUserSamples: List<String>? = listOf(),
)



@Serializable
data class RequestUser(
    @SerialName("id")
    val id: Int? = null,
    @SerialName("name")
    val name: String? = "",
    @SerialName("account")
    val account: String? = "",
    @SerialName("profile_image_urls")
    val profileImageUrls: ProfileImageUrl? = ProfileImageUrl(),
    @SerialName("is_followed")
    val isFollowed: Boolean? = false,
    @SerialName("is_access_blocking_user")
    val isAccessBlockingUser: Boolean? = false,
    @SerialName("is_accept_request")
    val isAcceptRequest: Boolean? = false,
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
