package com.hcyacg.sexy

import com.hcyacg.entity.Lolicon
import com.hcyacg.initial.Config
import com.hcyacg.initial.Setting
import com.hcyacg.lowpoly.LowPoly
import com.hcyacg.lowpoly.utils.CheckFileTypeUtil
import com.hcyacg.rank.TotalProcessing
import com.hcyacg.utils.CacheUtil
import com.hcyacg.utils.ImageUtil
import com.hcyacg.utils.RequestUtil
import com.hcyacg.utils.logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.*
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.QuoteReply
import net.mamoe.mirai.message.data.content
import net.mamoe.mirai.utils.ExternalResource
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import net.mamoe.mirai.utils.ExternalResource.Companion.uploadAsImage
import okhttp3.Headers
import okhttp3.RequestBody
import org.jsoup.HttpStatusException
import java.io.*
import java.net.ConnectException
import java.net.SocketException
import java.net.SocketTimeoutException
import java.text.SimpleDateFormat
import java.util.*
import javax.net.ssl.SSLHandshakeException

/**
 * @Author: Nekoer
 * @Desc: TODO
 * @Date: 2021/8/20 19:05
 */
object SexyCenter {
    private val headers = Headers.Builder()
    private var requestBody: RequestBody? = null
    private val sdf = SimpleDateFormat("yyyy-MM-dd")
    private val logger by logger()
    suspend fun init(event: GroupMessageEvent) {

        if (!Setting.groups.contains(event.group.id.toString())) {
            event.subject.sendMessage("该群无权限查看涩图")
            return
        }
        event.subject.sendMessage(At(event.sender).plus("正在获取中,请稍后"))

        val list = mutableListOf<String>()
        if (Config.enable.sexy.pixiv) {
            list.add("pixiv")
        }

        if (Config.enable.sexy.yande) {
            list.add("yande")
        }

        if (Config.enable.sexy.konachan) {
            list.add("konachan")
        }


        if (Config.enable.sexy.localImage) {
            list.add("localImage")
        }

        if (Config.enable.sexy.lolicon) {
            list.add("lolicon")
        }

        if (list.size <= 0) {
            event.subject.sendMessage("该群涩图来源已全部关闭")
            return
        }
        val num = (0 until list.size).random()

        when (list[num]) {
            "yande" -> {
                yande(event)
                return
            }
            "konachan" -> {
                konachan(event)
                return
            }
            "pixiv" -> {
                pixiv(event)
                return
            }
            "localImage" -> {
                localImage(event)
                return
            }
            "lolicon" -> {
                lolicon(event)
                return
            }
        }

    }

    suspend fun yandeTagSearch(event: GroupMessageEvent) {
        try {
            if (!Config.enable.sexy.yande) {
                return
            }

            val keys = event.message.content.split(" ")
            var tag = ""

            if (keys.size >= 2) {
                tag = keys[1]
            }

            val obj = RequestUtil.request(
                RequestUtil.Companion.Method.GET,
                "https://yande.re/post.json?limit=500&tags=${tag}",
                requestBody,
                headers.build()
            )

            if (null != obj && obj.jsonArray.size > 0) {
                val num: Int = (0 until (obj.jsonArray.size - 1)).random()

                val id = obj.jsonArray[num].jsonObject["id"]?.jsonPrimitive?.content
                val jpegUrl = obj.jsonArray[num].jsonObject["jpeg_url"]?.jsonPrimitive?.content


                val toExternalResource: ExternalResource
                if (Config.lowPoly){
                    val byte = ImageUtil.getImage(jpegUrl!!, CacheUtil.Type.YANDE).toByteArray()

                    toExternalResource = LowPoly.generate(
                        ByteArrayInputStream(byte),
                        200,
                        1F,
                        true,
                        "png",
                        false,
                        200
                    ).toByteArray().toExternalResource()
                }else{
                    toExternalResource = ImageUtil.getImage(jpegUrl!!, CacheUtil.Type.YANDE).toByteArray()
                        .toExternalResource()
                }

                val imageId: String = toExternalResource.uploadAsImage(event.group).imageId
                withContext(Dispatchers.IO) {
                    toExternalResource.close()
                }

                val quoteReply = QuoteReply(event.message)
                /**
                 * 判断是否配置了撤回时间
                 */

                if (Config.recall != 0L) {
                    event.subject.sendMessage(quoteReply.plus(Image(imageId))).recallIn(Config.recall)
                    event.subject.sendMessage("来源:YANDE($id)")
                } else {
                    event.subject.sendMessage(quoteReply.plus(Image(imageId)).plus("来源:YANDE($id)"))
                }
            } else {
                event.subject.sendMessage("内容为空")
            }

        } catch (e: IOException) {
            logger.warn { "连接至yande出现异常，请检查网络" }
            event.subject.sendMessage("网络异常")
        } catch (e: SSLHandshakeException) {
            logger.warn { "连接至yande出现异常，请检查网络" }
            event.subject.sendMessage("网络异常")
        } catch (e: HttpStatusException) {
            logger.warn { "连接至yande出现异常，请检查网络" }
            event.subject.sendMessage("网络异常")
        } catch (e: SocketTimeoutException) {
            logger.warn { "连接至yande出现异常，请检查网络" }
            event.subject.sendMessage("网络异常")
        } catch (e: ConnectException) {
            logger.warn { "连接至yande出现异常，请检查网络" }
            event.subject.sendMessage("网络异常")
        } catch (e: SocketException) {
            logger.warn { "连接至yande出现异常，请检查网络" }
            event.subject.sendMessage("网络异常")
        } catch (e: Exception) {
            logger.error { e.message }
            event.subject.sendMessage("发送图片失败")
        }
    }

    private suspend fun yande(event: GroupMessageEvent) {
        if (!Config.enable.sexy.yande) {
            return
        }
        try {
            val list = arrayOf("topless", "nipples", "no_bra")
            val randoms: Int = (0 until (list.size - 1)).random()
            val obj = RequestUtil.request(
                RequestUtil.Companion.Method.GET,
                "https://yande.re/post.json?limit=500&tags=${list[randoms]}",
                requestBody,
                headers.build()
            )
            if (null != obj && obj.jsonArray.size > 0) {
                val num: Int = (0 until (obj.jsonArray.size - 1)).random()

                val id = obj.jsonArray[num].jsonObject["id"]?.jsonPrimitive?.content
                val jpegUrl = obj.jsonArray[num].jsonObject["jpeg_url"]?.jsonPrimitive?.content


                val toExternalResource: ExternalResource
                if (Config.lowPoly){
                    val byte = ImageUtil.getImage(jpegUrl!!, CacheUtil.Type.YANDE).toByteArray()

                    toExternalResource = LowPoly.generate(
                        ByteArrayInputStream(byte),
                        200,
                        1F,
                        true,
                        "png",
                        false,
                        200
                    ).toByteArray().toExternalResource()
                }else{
                    toExternalResource = ImageUtil.getImage(jpegUrl!!, CacheUtil.Type.YANDE).toByteArray()
                        .toExternalResource()
                }


                val imageId: String = toExternalResource.uploadAsImage(event.group).imageId
                withContext(Dispatchers.IO) {
                    toExternalResource.close()
                }

                val quoteReply = QuoteReply(event.message)
                /**
                 * 判断是否配置了撤回时间
                 */

                if (Config.recall != 0L) {
                    event.subject.sendMessage(quoteReply.plus(Image(imageId))).recallIn(Config.recall)
                    event.subject.sendMessage("来源:YANDE($id)")
                } else {
                    event.subject.sendMessage(quoteReply.plus(Image(imageId)).plus("来源:YANDE($id)"))
                }
            } else {
                event.subject.sendMessage("内容为空")
            }
        } catch (e: IOException) {
            logger.warn { "连接至yande出现异常，请检查网络" }
            event.subject.sendMessage("网络异常")
        } catch (e: SSLHandshakeException) {
            logger.warn { "连接至yande出现异常，请检查网络" }
            event.subject.sendMessage("网络异常")
        } catch (e: HttpStatusException) {
            logger.warn { "连接至yande出现异常，请检查网络" }
            event.subject.sendMessage("网络异常")
        } catch (e: SocketTimeoutException) {
            logger.warn { "连接至yande出现异常，请检查网络" }
            event.subject.sendMessage("网络异常")
        } catch (e: ConnectException) {
            logger.warn { "连接至yande出现异常，请检查网络" }
            event.subject.sendMessage("网络异常")
        } catch (e: SocketException) {
            logger.warn { "连接至yande出现异常，请检查网络" }
            event.subject.sendMessage("网络异常")
        } catch (e: Exception) {
            logger.error { e.message }
            event.subject.sendMessage("发送图片失败")
        }
    }

    private suspend fun lolicon(event: GroupMessageEvent) {
        val message = QuoteReply(event.message)
        if (!Config.enable.sexy.lolicon) {
            return
        }
        try {
            val data: JsonElement?
            val url = "https://api.lolicon.app/setu/v2?proxy=i.acgmx.com&size=original&r18=2"
            data = RequestUtil.request(
                RequestUtil.Companion.Method.GET,
                url,
                requestBody,
                headers.build()
            )

            val lolicon = data?.let { Json.decodeFromJsonElement<Lolicon>(it) }
            if (null == lolicon) {
                event.subject.sendMessage(message.plus("Lolicon数据为空"))
                return
            }

            if (lolicon.data.isNullOrEmpty()) {
                event.subject.sendMessage(message.plus("Lolicon数据为空"))
                return
            }


            if (null == lolicon.data[0].urls) {
                event.subject.sendMessage(message.plus("Lolicon数据为空"))
                return
            }

            val toExternalResource: ExternalResource
            if (Config.lowPoly){
                val byte = ImageUtil.getImage(lolicon.data[0].urls?.original!!, CacheUtil.Type.LOLICON).toByteArray()

                toExternalResource = LowPoly.generate(
                    ByteArrayInputStream(byte),
                    200,
                    1F,
                    true,
                    "png",
                    false,
                    200
                ).toByteArray().toExternalResource()
            }else{
                toExternalResource = ImageUtil.getImage(lolicon.data[0].urls?.original!!, CacheUtil.Type.LOLICON).toByteArray()
                    .toExternalResource()
            }


            val imageId: String = toExternalResource.uploadAsImage(event.group).imageId
            withContext(Dispatchers.IO) {
                toExternalResource.close()
            }

            if (Config.recall != 0L) {
                event.subject.sendMessage(message.plus(Image(imageId))).recallIn(Config.recall)
                event.subject.sendMessage("来源:Lolicon(${lolicon.data[0].pid})")
            } else {
                event.subject.sendMessage(message.plus(Image(imageId)).plus("来源:Lolicon(${lolicon.data[0].pid})"))
            }

        } catch (e: IOException) {
            logger.warn { "连接至Lolicon出现异常，请检查网络" }
            event.subject.sendMessage("网络异常")
        } catch (e: SSLHandshakeException) {
            logger.warn { "连接至Lolicon出现异常，请检查网络" }
            event.subject.sendMessage("网络异常")
        } catch (e: HttpStatusException) {
            logger.warn { "连接至Lolicon出现异常，请检查网络" }
            event.subject.sendMessage("网络异常")
        } catch (e: SocketTimeoutException) {
            logger.warn { "连接至Lolicon出现异常，请检查网络" }
            event.subject.sendMessage("网络异常")
        } catch (e: ConnectException) {
            logger.warn { "连接至Lolicon出现异常，请检查网络" }
            event.subject.sendMessage("网络异常")
        } catch (e: SocketException) {
            logger.warn { "连接至Lolicon出现异常，请检查网络" }
            event.subject.sendMessage("网络异常")
        } catch (e: Exception) {
            logger.error { e.message }
            event.subject.sendMessage("发送图片失败")
        }
    }

    private suspend fun konachan(event: GroupMessageEvent) {
        if (!Config.enable.sexy.konachan) {
            return
        }
        try {
            val list = arrayOf("topless", "nipples", "no_bra")
            val randoms: Int = (0 until (list.size - 1)).random()
            val obj = RequestUtil.request(
                RequestUtil.Companion.Method.GET,
                "https://konachan.com/post.json?limit=500&tags=${list[randoms]}",
                requestBody,
                headers.build()
            )
            if (null != obj && obj.jsonArray.size > 0) {
                val num: Int = (0 until (obj.jsonArray.size - 1)).random()

                val id = obj.jsonArray[num].jsonObject["id"]?.jsonPrimitive?.content
                val jpegUrl = obj.jsonArray[num].jsonObject["jpeg_url"]?.jsonPrimitive?.content

                val toExternalResource: ExternalResource
                if (Config.lowPoly){
                    val byte = ImageUtil.getImage(jpegUrl!!, CacheUtil.Type.KONACHAN).toByteArray()


                    toExternalResource = LowPoly.generate(
                        ByteArrayInputStream(byte),
                        200,
                        1F,
                        true,
                        "png",
                        false,
                        200
                    ).toByteArray().toExternalResource()
                }else{
                    toExternalResource = ImageUtil.getImage(jpegUrl!!, CacheUtil.Type.KONACHAN).toByteArray()
                        .toExternalResource()
                }



                val imageId: String = toExternalResource.uploadAsImage(event.group).imageId
                withContext(Dispatchers.IO) {
                    toExternalResource.close()
                }
                val quoteReply = QuoteReply(event.message)

                if (Config.recall != 0L) {
                    event.subject.sendMessage(quoteReply.plus(Image(imageId))).recallIn(Config.recall)
                    event.subject.sendMessage("来源:KONACHAN($id)")
                } else {
                    event.subject.sendMessage(quoteReply.plus(Image(imageId)).plus("来源:KONACHAN($id)"))
                }
            } else {
                event.subject.sendMessage("内容为空")
            }
        } catch (e: IOException) {
            logger.warn { "连接至konachan出现异常，请检查网络" }
            event.subject.sendMessage("网络异常")
        } catch (e: SSLHandshakeException) {
            logger.warn { "连接至konachan出现异常，请检查网络" }
            event.subject.sendMessage("网络异常")
        } catch (e: HttpStatusException) {
            logger.warn { "连接至konachan出现异常，请检查网络" }
            event.subject.sendMessage("网络异常")
        } catch (e: SocketTimeoutException) {
            logger.warn { "连接至konachan出现异常，请检查网络" }
            event.subject.sendMessage("网络异常")
        } catch (e: ConnectException) {
            logger.warn { "连接至konachan出现异常，请检查网络" }
            event.subject.sendMessage("网络异常")
        } catch (e: SocketException) {
            logger.warn { "连接至konachan出现异常，请检查网络" }
            event.subject.sendMessage("网络异常")
        } catch (e: Exception) {
            logger.error { e.message }
            event.subject.sendMessage("发送图片失败")
        }
    }

    private suspend fun pixiv(event: GroupMessageEvent) {
        if (!Config.enable.sexy.pixiv) {
            return
        }
        try {
            //获取日本排行榜时间，当前天数-2
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_MONTH, -2)
            val date: String = sdf.format(calendar.time)
            val obj = TotalProcessing().dealWith("illust", "daily_r18", 3, 30, date)


            val illusts = obj?.jsonObject?.get("illusts")?.jsonArray ?: return

            val randoms: Int = (0 until (illusts.size - 1)).random()

            val tempData = illusts[randoms].jsonObject
            val id = tempData["id"]?.jsonPrimitive?.content

            val image = tempData["image_urls"]?.jsonObject?.get("large")?.jsonPrimitive?.content


            val toExternalResource: ExternalResource
            if (Config.lowPoly){
                val byte = ImageUtil.getImage(image!!.replace("i.pximg.net", "i.acgmx.com"), CacheUtil.Type.PIXIV).toByteArray()


                toExternalResource = LowPoly.generate(
                    ByteArrayInputStream(byte),
                    200,
                    1F,
                    true,
                    "png",
                    false,
                    200
                ).toByteArray().toExternalResource()
            }else{
                toExternalResource = ImageUtil.getImage(image!!.replace("i.pximg.net", "i.acgmx.com"), CacheUtil.Type.PIXIV).toByteArray()
                    .toExternalResource()
            }

            val imageId: String = toExternalResource.uploadAsImage(event.group).imageId
            withContext(Dispatchers.IO) {
                toExternalResource.close()
            }
            val quoteReply = QuoteReply(event.message)
            if (Config.recall != 0L) {
                event.subject.sendMessage(quoteReply.plus(Image(imageId))).recallIn(Config.recall)
                event.subject.sendMessage("来源:Pixiv($id)")
            } else {
                event.subject.sendMessage(quoteReply.plus(Image(imageId)).plus("来源:Pixiv($id)"))
            }
        } catch (e: IOException) {
            logger.warn { "连接至pixiv出现异常，请检查网络" }
            event.subject.sendMessage("网络异常")
        } catch (e: SSLHandshakeException) {
            logger.warn { "连接至pixiv出现异常，请检查网络" }
            event.subject.sendMessage("网络异常")
        } catch (e: HttpStatusException) {
            logger.warn { "连接至pixiv出现异常，请检查网络" }
            event.subject.sendMessage("网络异常")
        } catch (e: SocketTimeoutException) {
            logger.warn { "连接至pixiv出现异常，请检查网络" }
            event.subject.sendMessage("网络异常")
        } catch (e: ConnectException) {
            logger.warn { "连接至pixiv出现异常，请检查网络" }
            event.subject.sendMessage("网络异常")
        } catch (e: SocketException) {
            logger.warn { "连接至pixiv出现异常，请检查网络" }
            event.subject.sendMessage("网络异常")
        } catch (e: Exception) {
            logger.error { e.message }
            event.subject.sendMessage("发送图片失败")
        }
    }

    /**
     * 本地图库
     */
    private suspend fun localImage(event: GroupMessageEvent) {
        if (!Config.enable.sexy.localImage) {
            return
        }
        try {
            val file = File(Config.localImagePath)

            val list = getAllImage(file)
            if(list.size == 0){
                return
            }
            val num = 0 until list.size
            logger.debug { num }
            val imageFile = File(list[num.random()])
            val input = withContext(Dispatchers.IO) {
                FileInputStream(imageFile)
            }
            val out = ByteArrayOutputStream()
            val buffer = ByteArray(2048)
            var len: Int
            while (withContext(Dispatchers.IO) {
                    input.read(buffer)
                }.also { len = it } > 0) {
                out.write(buffer, 0, len)
            }


            val toExternalResource: ExternalResource
            if (Config.lowPoly){


                toExternalResource = LowPoly.generate(
                    ByteArrayInputStream(out.toByteArray()),
                    200,
                    1F,
                    true,
                    "png",
                    false,
                    200
                ).toByteArray().toExternalResource()
            }else{
                toExternalResource =
                    out.toByteArray().toExternalResource()
            }

            val imageId: String = toExternalResource.uploadAsImage(event.group).imageId
            withContext(Dispatchers.IO) {
                toExternalResource.close()
            }
            if (Config.recall != 0L) {
                event.subject.sendMessage(At(event.sender).plus("\n").plus(Image(imageId)))
                    .recallIn(Config.recall)
            } else {
                event.subject.sendMessage(At(event.sender).plus("\n").plus(Image(imageId)))
            }

        } catch (e: IOException) {
            logger.warn { "连接至本地图库出现异常,请检查本地图库是否已经设置" }
            event.subject.sendMessage("本地图库出现异常")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 递归获取文件夹及其内部的文件
     */
    private fun getAllImage(file: File): MutableList<String> {
        val imageList = mutableListOf<String>()
        try {
            if (file.isDirectory) {
                file.list()?.forEach {
                    val tempFile = File(file.path + File.separator + it)
                    if (tempFile.isDirectory) {
                        imageList.addAll(getAllImage(tempFile))
                    } else {
                        val type = CheckFileTypeUtil.getFileType(tempFile.path)
                        if (tempFile.path.endsWith(".jpg") || tempFile.path.endsWith(".png") || tempFile.path.endsWith(".gif")){
                            if (arrayOf("jpg","png","gif").contains(type)){
                                imageList.add(tempFile.path)
                            }else{
                                logger.warn { "${tempFile.path}不是有效图片" }
                            }
                        }else{
                            logger.warn { "${tempFile.path}不是有效图片" }
                        }
                    }
                }
            } else {
                imageList.add(file.path)
            }
            return imageList
        } catch (e: Exception) {
            e.printStackTrace()
            return mutableListOf()
        }

    }
}