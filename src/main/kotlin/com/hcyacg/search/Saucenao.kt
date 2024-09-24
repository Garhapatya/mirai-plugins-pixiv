package com.hcyacg.search

import com.hcyacg.entity.SaucenaoItem
import com.hcyacg.initial.Command
import com.hcyacg.initial.Config
import com.hcyacg.utils.CacheUtil
import com.hcyacg.utils.DataUtil
import com.hcyacg.utils.DataUtil.Companion.getImageLinkFromImage
import com.hcyacg.utils.ImageUtil.Companion.getImage
import com.hcyacg.utils.ImageUtil.Companion.rotate
import com.hcyacg.utils.RequestUtil
import com.hcyacg.utils.logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import net.mamoe.mirai.utils.ExternalResource.Companion.uploadAsImage
import okhttp3.Headers
import okhttp3.RequestBody
import org.apache.commons.lang3.StringUtils
import java.io.File
import java.net.URL
import javax.imageio.ImageIO

object Saucenao {
    private val headers = Headers.Builder()
    private val requestBody: RequestBody? = null
    private val jsonObject: JsonElement? = null
    private val logger by logger()
    private val tracePath: File =
        File(System.getProperty("user.dir") + File.separator + "data" + File.separator + "trace")

    private val json = Json { ignoreUnknownKeys = true }
    /**
     * 通过图片来搜索信息
     */
    suspend fun picToSearch(event: GroupMessageEvent, picUri: String, eco: Boolean): List<Message> {
        val list = mutableListOf<Message>()

        try {

            /**
             * 未配置key通知用户
             */
            if (Config.token.saucenao.isBlank()) {
                event.subject.sendMessage(At(event.sender).plus("您还未配置saucenao的api_key,申请网址为https://saucenao.com/user.php?page=search-api"))
                return list
            }

            /**
             * 获取图片的代码
             */
//            val picUri = DataUtil.getSubString(messageChain.toString().replace(" ",""), "[mirai:image:{", "}.")!!
//                .replace("-", "")

            if (eco) {
                val imageData = getInfo(picUri)
                imageData?.let { mate(event, it)?.let { it1 -> list.add(it1.plus("当前为Saucenao搜索")) } }
            } else {
                //旋转三次
                val rotate90 = rotate(withContext(Dispatchers.IO) {
                    ImageIO.read(URL(picUri))
                }, 90).toByteArray().toExternalResource()
                val code90 = getImageLinkFromImage(rotate90.uploadAsImage(event.group))
                withContext(Dispatchers.IO) {
                    rotate90.close()
                }

                val rotate180 = rotate(withContext(Dispatchers.IO) {
                    ImageIO.read(URL(picUri))
                }, 180).toByteArray().toExternalResource()
                val code180 = getImageLinkFromImage(rotate180.uploadAsImage(event.group))
                withContext(Dispatchers.IO) {
                    rotate180.close()
                }

                val rotate270 = rotate(withContext(Dispatchers.IO) {
                    ImageIO.read(URL(picUri))
                }, 270).toByteArray().toExternalResource()
                val code270 = getImageLinkFromImage(rotate270.uploadAsImage(event.group))
                withContext(Dispatchers.IO) {
                    rotate270.close()
                }


                val imageData = getInfo(picUri)
                val imageData90 = getInfo(code90)
                val imageData180 = getInfo(code180)
                val imageData270 = getInfo(code270)


                /**
                 * 进行相似度对比，取最大值
                 */
                var similarity = 0.0
                var similarity90 = 0.0
                var similarity180 = 0.0
                var similarity270 = 0.0

                val double = mutableListOf<Double>()

                if (null != imageData) {
                    similarity = imageData.results?.get(0)?.header?.similarity.toString().toDouble()
                    double.add(similarity)
                }

                if (null != imageData90) {
                    similarity90 = imageData90.results?.get(0)?.header?.similarity.toString().toDouble()
                    double.add(similarity90)
                }

                if (null != imageData180) {
                    similarity180 = imageData180.results?.get(0)?.header?.similarity.toString().toDouble()
                    double.add(similarity180)
                }

                if (null != imageData270) {
                    similarity270 = imageData270.results?.get(0)?.header?.similarity.toString().toDouble()
                    double.add(similarity270)
                }

                when (double.max()) {
                    similarity -> {
                        imageData?.let { mate(event, it)?.let { it1 -> list.add(it1.plus("当前为Saucenao搜索")) } }
                    }
                    similarity90 -> {
                        imageData90?.let { mate(event, it)?.let { it1 -> list.add(it1.plus("当前为Saucenao搜索")) } }
                    }
                    similarity180 -> {
                        imageData180?.let { mate(event, it)?.let { it1 -> list.add(it1.plus("当前为Saucenao搜索")) } }
                    }
                    similarity270 -> {
                        imageData270?.let { mate(event, it)?.let { it1 -> list.add(it1.plus("当前为Saucenao搜索")) } }
                    }
                }
            }
            return list
        } catch (e: Exception) {
            logger.error { e.message }
            e.printStackTrace()
            event.subject.sendMessage("请输入正确的命令 ${Command.picToSearch}图片")
            list.clear()
            return list
        }

    }

    /**
     * 匹配数据格式
     */
    private suspend fun mate(event: GroupMessageEvent, data: SaucenaoItem): Message? {

        try {
            var message: Message? = pixivSearch(event, data)
            if (null != message) {
                return message
            }

            message = danBooRuSearch(event, data)
            if (null != message) {
                return message
            }

            message = seiGaSearch(event, data)
            if (null != message) {
                return message
            }

            message = daSearch(event, data)
            if (null != message) {
                return message
            }

            message = bcySearch(event, data)
            if (null != message) {
                return message
            }

            message = maSearch(event, data)
            if (null != message) {
                return message
            }
            message = niJieSearch(event, data)
            if (null != message) {
                return message
            }
            message = drawrSearch(event, data)
            if (null != message) {
                return message
            }

        } catch (e: Exception) {
            logger.error { e.message }
            e.printStackTrace()
            event.subject.sendMessage("请输入正确的命令 ${Command.picToSearch}图片")
            return null
        }
        return null
    }

    /**
     * 获取四张照片的第一个搜索数据
     */
    private fun getInfo(picUri: String): SaucenaoItem? {
        val data: JsonElement?

        try {
            data = RequestUtil.request(
                RequestUtil.Companion.Method.GET,
                "https://saucenao.com/search.php?db=999&output_type=2&api_key=${Config.token.saucenao}&testmode=1&numres=16&url=${DataUtil.urlEncode(picUri)}",
                requestBody,
                headers.build()
            )

            val saucenao = data?.let { json.decodeFromJsonElement<SaucenaoItem>(it) }

            if (saucenao?.header?.status != 0) {
                return null
            }

            if (saucenao.results != null) {
                return saucenao
            }
            return null
        } catch (e: Exception) {
            logger.error { e.message }
            e.printStackTrace()
            return null
        }
    }

    /**
     * 以下皆为各个网站的搭配，不详解
     */

    private suspend fun pixivSearch(event: GroupMessageEvent, everything: SaucenaoItem): Message? {
        return try {
            val header = everything.results?.get(0)?.header
            val data = everything.results?.get(0)?.data
            val similarity = header?.similarity
            val thumbnail = header?.thumbnail
            val extUrls = data?.extUrls

            val title = data?.title
            val pixivId = data?.pixivId
            val memberName = data?.memberName
            val memberId = data?.memberId
            if (StringUtils.isBlank(pixivId.toString())) {
                throw RuntimeException("搜索到的插画id为空")
            }

            val toExternalResource = thumbnail?.let { getImage(it, CacheUtil.Type.NONSUPPORT).toByteArray().toExternalResource() }
            val imageId = toExternalResource?.uploadAsImage(event.group)?.imageId
            withContext(Dispatchers.IO) {
                toExternalResource?.close()
            }

            if (thumbnail != null &&
                (!thumbnail.contains("https://img3.saucenao.com/") || !thumbnail.contains("https://img1.saucenao.com/"))
            ) {
                At(event.sender).plus("\r\n").plus(Image(imageId!!)).plus("\r\n")
                    .plus("Title: $title").plus("\r\n")
                    .plus("ID: $pixivId").plus("\r\n")
                    .plus("AuthorID: $memberId").plus("\r\n")
                    .plus("AuthorName: $memberName").plus("\r\n")
                    .plus("Ratio: $similarity%").plus("\r\n")
                    .plus(getExtUrls(extUrls))
            } else {
                At(event.sender).plus("\r\n").plus("该图片因官方服务器奔溃而无法获取").plus("\r\n")
                    .plus("The image was unavailable due to a crash on the official server").plus("\r\n")
                    .plus("Title: $title").plus("\r\n")
                    .plus("ID: $pixivId").plus("\r\n")
                    .plus("AuthorID: $memberId").plus("\r\n")
                    .plus("AuthorName: $memberName").plus("\r\n")
                    .plus("Ratio: $similarity%").plus("\r\n")
                    .plus(getExtUrls(extUrls))
            }
        } catch (e: java.lang.Exception) {
            logger.error { e.message }
            e.printStackTrace()
            null
            //return At(event.sender).plus(e.message.toString())
        }
    }

    private suspend fun danBooRuSearch(event: GroupMessageEvent, everything: SaucenaoItem): Message? {

        return try {
            val header = everything.results?.get(0)?.header
            val data = everything.results?.get(0)?.data
            val similarity = header?.similarity
            val thumbnail = header?.thumbnail
            val extUrls = data?.extUrls

            val danbooruId = data?.danbooruId
            val memberName = data?.creator
            if (StringUtils.isBlank(danbooruId.toString())) {
                throw RuntimeException("搜索到的插画id为空")
            }
            val toExternalResource = getImage(thumbnail!!, CacheUtil.Type.NONSUPPORT).toByteArray().toExternalResource()
            val imageId = toExternalResource.uploadAsImage(event.group).imageId
            withContext(Dispatchers.IO) {
                toExternalResource.close()
            }
            if (!thumbnail.contains("https://img3.saucenao.com/") || !thumbnail.contains("https://img1.saucenao.com/")) {
                At(event.sender).plus("\r\n")
                    .plus(Image(imageId)).plus("\r\n")
                    .plus("ID: $danbooruId").plus("\r\n")
                    .plus("AuthorName: $memberName").plus("\r\n")
                    .plus("Ratio: $similarity%").plus("\r\n")
                    .plus(getExtUrls(extUrls))

            } else {
                At(event.sender).plus("\r\n")
                    .plus("该图片因官方服务器奔溃而无法获取").plus("\r\n")
                    .plus("The image was unavailable due to a crash on the official server").plus("\r\n")
                    .plus("ID: $danbooruId").plus("\r\n")
                    .plus("AuthorName: $memberName").plus("\r\n")
                    .plus("Ratio: $similarity%").plus("\r\n")
                    .plus(getExtUrls(extUrls))
            }
        } catch (e: java.lang.Exception) {
            logger.error { e.message }
            e.printStackTrace()
            null
        }
    }

    private suspend fun seiGaSearch(event: GroupMessageEvent, everything: SaucenaoItem): Message? {

        return try {
            val header = everything.results?.get(0)?.header
            val data = everything.results?.get(0)?.data
            val similarity = header?.similarity
            val thumbnail = header?.thumbnail
            val extUrls = data?.extUrls

            val title = data?.title
            val seigaId = data?.seigaId
            val memberName = data?.memberName
            val memberId = data?.memberId
            if (StringUtils.isBlank(seigaId.toString())) {
                throw RuntimeException("搜索到的插画id为空")
            }
            val toExternalResource = getImage(thumbnail!!, CacheUtil.Type.NONSUPPORT).toByteArray().toExternalResource()
            val imageId = toExternalResource.uploadAsImage(event.group).imageId
            withContext(Dispatchers.IO) {
                toExternalResource.close()
            }
            if (!thumbnail.contains("https://img3.saucenao.com/") || !thumbnail.contains("https://img1.saucenao.com/")) {

                At(event.sender).plus("\r\n")
                    .plus(Image(imageId)).plus("\r\n")
                    .plus("Title: $title").plus("\r\n")
                    .plus("ID: $seigaId").plus("\r\n")
                    .plus("AuthorID: $memberId").plus("\r\n")
                    .plus("AuthorName: $memberName").plus("\r\n")
                    .plus("Ratio: $similarity%").plus("\r\n")
                    .plus(getExtUrls(extUrls))

            } else {

                At(event.sender).plus("\r\n")
                    .plus("该图片因官方服务器奔溃而无法获取").plus("\r\n")
                    .plus("The image was unavailable due to a crash on the official server").plus("\r\n")
                    .plus("Title: $title").plus("\r\n")
                    .plus("ID: $seigaId").plus("\r\n")
                    .plus("AuthorID: $memberId").plus("\r\n")
                    .plus("AuthorName: $memberName").plus("\r\n")
                    .plus("Ratio: $similarity%").plus("\r\n")
                    .plus(getExtUrls(extUrls))

            }
        } catch (e: java.lang.Exception) {
            logger.error { e.message }
            e.printStackTrace()
            null
        }
    }

    private suspend fun daSearch(event: GroupMessageEvent, everything: SaucenaoItem): Message? {

        return try {
            val header = everything.results?.get(0)?.header
            val data = everything.results?.get(0)?.data
            val similarity = header?.similarity
            val thumbnail = header?.thumbnail
            val extUrls = data?.extUrls

            val title = data?.title
            val daId = data?.daId
            val memberName = data?.memberName
            if (StringUtils.isBlank(daId.toString())) {
                throw RuntimeException("搜索到的插画id为空")
            }
            val toExternalResource = getImage(thumbnail!!, CacheUtil.Type.NONSUPPORT).toByteArray().toExternalResource()
            val imageId = toExternalResource.uploadAsImage(event.group).imageId
            withContext(Dispatchers.IO) {
                toExternalResource.close()
            }
            if (!thumbnail.contains("https://img3.saucenao.com/") || !thumbnail.contains("https://img1.saucenao.com/")) {

                At(event.sender).plus("\r\n")
                    .plus(Image(imageId)).plus("\r\n")
                    .plus("Title: $title").plus("\r\n")
                    .plus("ID: $daId").plus("\r\n")
                    .plus("AuthorName: $memberName").plus("\r\n")
                    .plus("Ratio: $similarity%").plus("\r\n")
                    .plus(getExtUrls(extUrls))

            } else {

                At(event.sender).plus("\r\n")
                    .plus("该图片因官方服务器奔溃而无法获取").plus("\r\n")
                    .plus("The image was unavailable due to a crash on the official server").plus("\r\n")
                    .plus("Title: $title").plus("\r\n")
                    .plus("ID: $daId").plus("\r\n")
                    .plus("AuthorName: $memberName").plus("\r\n")
                    .plus("Ratio: $similarity%").plus("\r\n")
                    .plus(getExtUrls(extUrls))

            }
        } catch (e: java.lang.Exception) {
            logger.error { e.message }
            e.printStackTrace()
            null
        }
    }

    private suspend fun bcySearch(event: GroupMessageEvent, everything: SaucenaoItem): Message? {

        return try {
            val header = everything.results?.get(0)?.header
            val data = everything.results?.get(0)?.data
            val similarity = header?.similarity
            val thumbnail = header?.thumbnail
            val extUrls = data?.extUrls

            val title = data?.title
            val bcyId = data?.bcyId
            val memberName = data?.memberName
            val memberId = data?.memberId
            if (StringUtils.isBlank(bcyId.toString())) {
                throw RuntimeException("搜索到的插画id为空")
            }
            val toExternalResource = getImage(thumbnail!!, CacheUtil.Type.NONSUPPORT).toByteArray().toExternalResource()
            val imageId = toExternalResource.uploadAsImage(event.group).imageId
            withContext(Dispatchers.IO) {
                toExternalResource.close()
            }
            if (!thumbnail.contains("https://img3.saucenao.com/") || !thumbnail.contains("https://img1.saucenao.com/")) {

                At(event.sender).plus("\r\n")
                    .plus(Image(imageId)).plus("\r\n")
                    .plus("Title: $title").plus("\r\n")
                    .plus("ID: $bcyId").plus("\r\n")
                    .plus("AuthorID: $memberId").plus("\r\n")
                    .plus("AuthorName: $memberName").plus("\r\n")
                    .plus("Ratio: $similarity%").plus("\r\n")
                    .plus(getExtUrls(extUrls))

            } else {

                At(event.sender).plus("\r\n")
                    .plus("该图片因官方服务器奔溃而无法获取").plus("\r\n")
                    .plus("The image was unavailable due to a crash on the official server").plus("\r\n")
                    .plus("Title: $title").plus("\r\n")
                    .plus("ID: $bcyId").plus("\r\n")
                    .plus("AuthorID: $memberId").plus("\r\n")
                    .plus("AuthorName: $memberName").plus("\r\n")
                    .plus("Ratio: $similarity%").plus("\r\n")
                    .plus(getExtUrls(extUrls))

            }
        } catch (e: java.lang.Exception) {
            logger.error { e.message }
            e.printStackTrace()
            null
        }
    }

    private suspend fun maSearch(event: GroupMessageEvent, everything: SaucenaoItem): Message? {

        return try {
            val header = everything.results?.get(0)?.header
            val data = everything.results?.get(0)?.data
            val similarity = header?.similarity
            val thumbnail = header?.thumbnail
            val extUrls = data?.extUrls

            val mdId = data?.mdId
            val memberName = data?.author
            if (StringUtils.isBlank(mdId)) {
                throw RuntimeException("搜索到的插画id为空")
            }
            val toExternalResource = getImage(thumbnail!!, CacheUtil.Type.NONSUPPORT).toByteArray().toExternalResource()
            val imageId = toExternalResource.uploadAsImage(event.group).imageId
            withContext(Dispatchers.IO) {
                toExternalResource.close()
            }
            if (!thumbnail.contains("https://img3.saucenao.com/") || !thumbnail.contains("https://img1.saucenao.com/")) {

                At(event.sender).plus("\r\n")
                    .plus(Image(imageId)).plus("\r\n")
                    .plus("ID: $mdId").plus("\r\n")
                    .plus("AuthorName: $memberName").plus("\r\n")
                    .plus("Ratio: $similarity%").plus("\r\n")
                    .plus(getExtUrls(extUrls))

            } else {

                At(event.sender).plus("\r\n")
                    .plus("该图片因官方服务器奔溃而无法获取").plus("\r\n")
                    .plus("The image was unavailable due to a crash on the official server").plus("\r\n")
                    .plus("ID: $mdId").plus("\r\n")
                    .plus("AuthorName: $memberName").plus("\r\n")
                    .plus("Ratio: $similarity%").plus("\r\n")
                    .plus(getExtUrls(extUrls))

            }
        } catch (e: java.lang.Exception) {
            logger.error { e.message }
            e.printStackTrace()
            null
        }
    }

    private suspend fun niJieSearch(event: GroupMessageEvent, everything: SaucenaoItem): Message? {
        return try {
            val header = everything.results?.get(0)?.header
            val data = everything.results?.get(0)?.data
            val similarity = header?.similarity
            val thumbnail = header?.thumbnail
            val extUrls = data?.extUrls

            val title = data?.title
            val nijieId = data?.nijieId
            val memberName = data?.memberName
            val memberId = data?.memberId
            if (StringUtils.isBlank(nijieId.toString())) {
                throw RuntimeException("搜索到的插画id为空")
            }
            val toExternalResource = getImage(thumbnail!!, CacheUtil.Type.NONSUPPORT).toByteArray().toExternalResource()
            val imageId = toExternalResource.uploadAsImage(event.group).imageId
            withContext(Dispatchers.IO) {
                toExternalResource.close()
            }
            if (!thumbnail.contains("https://img3.saucenao.com/") || !thumbnail.contains("https://img1.saucenao.com/")) {

                At(event.sender).plus("\r\n")
                    .plus(Image(imageId)).plus("\r\n")
                    .plus("Title: $title").plus("\r\n")
                    .plus("ID: $nijieId").plus("\r\n")
                    .plus("AuthorID: $memberId").plus("\r\n")
                    .plus("AuthorName: $memberName").plus("\r\n")
                    .plus("Ratio: $similarity%").plus("\r\n")
                    .plus(getExtUrls(extUrls))

            } else {

                At(event.sender).plus("\r\n")
                    .plus("该图片因官方服务器奔溃而无法获取").plus("\r\n")
                    .plus("The image was unavailable due to a crash on the official server").plus("\r\n")
                    .plus("Title: $title").plus("\r\n")
                    .plus("ID: $nijieId").plus("\r\n")
                    .plus("AuthorID: $memberId").plus("\r\n")
                    .plus("AuthorName: $memberName").plus("\r\n")
                    .plus("Ratio: $similarity%").plus("\r\n")
                    .plus(getExtUrls(extUrls))

            }
        } catch (e: java.lang.Exception) {
            logger.error { e.message }
            e.printStackTrace()
            null
        }
    }

    private suspend fun drawrSearch(event: GroupMessageEvent, everything: SaucenaoItem): Message? {
        return try {
            val header = everything.results?.get(0)?.header
            val data = everything.results?.get(0)?.data
            val similarity = header?.similarity
            val thumbnail = header?.thumbnail
            val extUrls = data?.extUrls

            val title = data?.title
            val drawrID = data?.drawrId
            val memberName = data?.memberName
            val memberId = data?.memberId
            if (StringUtils.isBlank(drawrID.toString())) {
                throw RuntimeException("搜索到的插画id为空")
            }
            val toExternalResource = getImage(thumbnail!!, CacheUtil.Type.NONSUPPORT).toByteArray().toExternalResource()
            val imageId = toExternalResource.uploadAsImage(event.group).imageId
            withContext(Dispatchers.IO) {
                toExternalResource.close()
            }
            if (!thumbnail.contains("https://img3.saucenao.com/") || !thumbnail.contains("https://img1.saucenao.com/")) {

                At(event.sender).plus("\r\n")
                    .plus(Image(imageId)).plus("\r\n")
                    .plus("Title$title").plus("\r\n")
                    .plus("ID: $drawrID").plus("\r\n")
                    .plus("AuthorID: $memberId").plus("\r\n")
                    .plus("AuthorName: $memberName").plus("\r\n")
                    .plus("Ratio: $similarity%").plus("\r\n")
                    .plus(getExtUrls(extUrls))

            } else {

                At(event.sender).plus("\r\n")
                    .plus("该图片因官方服务器奔溃而无法获取").plus("\r\n")
                    .plus("The image was unavailable due to a crash on the official server").plus("\r\n")
                    .plus("Title: $title").plus("\r\n")
                    .plus("ID: $drawrID").plus("\r\n")
                    .plus("AuthorID: $memberId").plus("\r\n")
                    .plus("AuthorName: $memberName").plus("\r\n")
                    .plus("Ratio: $similarity%").plus("\r\n")
                    .plus(getExtUrls(extUrls))

            }
        } catch (e: java.lang.Exception) {
            logger.error { e.message }
            e.printStackTrace()
            null
        }
    }


    private fun getExtUrls(extUrls: List<String>?): String {
        return try {
            val data = StringBuilder()
            for (extUrl in extUrls!!) {
                data.append(extUrl.replace("\"", "")).append("\r\n")
            }
            data.toString()
        } catch (e: Exception) {
            logger.error { e.message }
            e.printStackTrace()
            ""
        }
    }

    private fun getExtOneUrls(extUrls: List<String>?): String? {
        return try {
            val data = StringBuilder()
            data.append(extUrls!![0]).toString().replace("\"", "")
        } catch (e: Exception) {
            logger.error { e.message }
            e.printStackTrace()
            ""
        }
    }

}