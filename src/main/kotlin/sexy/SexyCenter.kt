package com.hcyacg.sexy

import com.alibaba.fastjson.JSONObject
import com.hcyacg.initial.Setting
import com.hcyacg.rank.TotalProcessing
import com.hcyacg.utils.ImageUtil
import com.hcyacg.utils.RequestUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.QuoteReply
import net.mamoe.mirai.message.data.content
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import net.mamoe.mirai.utils.ExternalResource.Companion.uploadAsImage
import net.mamoe.mirai.utils.MiraiLogger
import okhttp3.Headers
import okhttp3.RequestBody
import java.text.SimpleDateFormat
import java.util.*

/**
 * @Author: Nekoer
 * @Desc: TODO
 * @Date: 2021/8/20 19:05
 */
object SexyCenter {
    private val headers = Headers.Builder()
    private var requestBody: RequestBody? = null
    private val sdf = SimpleDateFormat("yyyy-MM-dd")

    suspend fun init(event: GroupMessageEvent, logger: MiraiLogger) {

        if (!Setting.groups.contains(event.group.id.toString())) {
            event.subject.sendMessage("该群无权限查看涩图")
            return
        }


        val keys = event.message.content.split(" ")
        if (keys.size >= 2) {
            println(event.message)
            yandeTagSearch(event, keys[1], keys.equals("r18"), logger)
            return
        }


        when ((0..2).random()) {
            0 -> {
                yande(event, logger)
                return
            }
            1 -> {
                konachan(event, logger)
                return
            }
            2 -> {
                pixiv(event, logger)
                return
            }
        }

    }

    private suspend fun yandeTagSearch(event: GroupMessageEvent, tag: String, isR18: Boolean, logger: MiraiLogger) {
        try {

            val obj = RequestUtil.requestArray(
                RequestUtil.Companion.Method.GET,
                "https://yande.re/post.json?limit=500&tags=${tag}",
                requestBody,
                headers.build(),
                logger
            )

            if (null != obj && obj.size > 0 ){
                val num: Int = (0 until (obj.size - 1)).random()
                val id = JSONObject.parseObject(obj[num].toString()).getString("id")
                val jpegUrl = JSONObject.parseObject(obj[num].toString()).getString("jpeg_url")


                val toExternalResource = ImageUtil.getImage(jpegUrl).toByteArray().toExternalResource()
                val imageId: String = toExternalResource.uploadAsImage(event.group).imageId
                withContext(Dispatchers.IO) {
                    toExternalResource.close()
                }

                val quoteReply: QuoteReply = QuoteReply(event.message)
                /**
                 * 判断是否配置了撤回时间
                 */

                if (Setting.config.recall != 0L) {
                    event.subject.sendMessage(quoteReply.plus(Image(imageId)).plus("来源:YANDE($id)"))
                        .recallIn(Setting.config.recall)
                } else {
                    event.subject.sendMessage(quoteReply.plus(Image(imageId)).plus("来源:YANDE($id)"))
                }
            }else{
                event.subject.sendMessage("内容为空")
            }

        } catch (e: Exception) {
            logger.warning(e)
            event.subject.sendMessage("发送图片失败")
        }
    }

    private suspend fun yande(event: GroupMessageEvent, logger: MiraiLogger) {
        try {
            val list = arrayOf("topless", "nipples", "no_bra")
            val randoms: Int = (0 until (list.size - 1)).random()
            val obj = RequestUtil.requestArray(
                RequestUtil.Companion.Method.GET,
                "https://yande.re/post.json?limit=500&tags=${list[randoms]}",
                requestBody,
                headers.build(),
                logger
            )
            val num: Int = (0 until (obj!!.size - 1)).random()
            val id = JSONObject.parseObject(obj[num].toString()).getString("id")
            val jpegUrl = JSONObject.parseObject(obj[num].toString()).getString("jpeg_url")


            val toExternalResource = ImageUtil.getImage(jpegUrl).toByteArray().toExternalResource()
            val imageId: String = toExternalResource.uploadAsImage(event.group).imageId
            withContext(Dispatchers.IO) {
                toExternalResource.close()
            }

            val quoteReply: QuoteReply = QuoteReply(event.message)
            /**
             * 判断是否配置了撤回时间
             */

            if (Setting.config.recall != 0L) {
                event.subject.sendMessage(quoteReply.plus(Image(imageId)).plus("来源:YANDE($id)"))
                    .recallIn(Setting.config.recall)
            } else {
                event.subject.sendMessage(quoteReply.plus(Image(imageId)).plus("来源:YANDE($id)"))
            }
        } catch (e: Exception) {
            logger.warning(e)
            event.subject.sendMessage("发送图片失败")
        }
    }

    private suspend fun konachan(event: GroupMessageEvent, logger: MiraiLogger) {
        try {
            val list = arrayOf("topless", "nipples", "no_bra")
            val randoms: Int = (0 until (list.size - 1)).random()
            val obj = RequestUtil.requestArray(
                RequestUtil.Companion.Method.GET,
                "https://konachan.com/post.json?limit=500&tags=${list[randoms]}",
                requestBody,
                headers.build(),
                logger
            )
            val num: Int = (0 until (obj!!.size - 1)).random()
            val id = JSONObject.parseObject(obj[num].toString()).getString("id")
            val jpegUrl = JSONObject.parseObject(obj[num].toString()).getString("jpeg_url")

            val toExternalResource = ImageUtil.getImage(jpegUrl).toByteArray().toExternalResource()
            val imageId: String = toExternalResource.uploadAsImage(event.group).imageId
            withContext(Dispatchers.IO) {
                toExternalResource.close()
            }
            val quoteReply: QuoteReply = QuoteReply(event.message)

            if (Setting.config.recall != 0L) {
                event.subject.sendMessage(quoteReply.plus(Image(imageId)).plus("来源:KONACHAN($id)"))
                    .recallIn(Setting.config.recall)
            } else {
                event.subject.sendMessage(quoteReply.plus(Image(imageId)).plus("来源:KONACHAN($id)"))
            }
        } catch (e: Exception) {
            logger.warning(e)
            event.subject.sendMessage("发送图片失败")
        }
    }

    private suspend fun pixiv(event: GroupMessageEvent, logger: MiraiLogger) {
        try {
            //获取日本排行榜时间，当前天数-2
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_MONTH, -2)
            val date: String = sdf.format(calendar.time)
            val obj = TotalProcessing().dealWith("illust", "daily_r18", 3, 30, date, logger)

            val illusts = JSONObject.parseArray(obj?.getString("illusts"))
            val randoms: Int = (0 until (illusts.size - 1)).random()

            val tempData = JSONObject.parseObject(illusts?.get(randoms)?.toString())
            val id = tempData.getString("id")


            val image = JSONObject.parseObject(tempData.getString("image_urls")).getString("large")
            val toExternalResource =
                ImageUtil.getImage(image.replace("i.pximg.net", "i.acgmx.com")).toByteArray().toExternalResource()
            val imageId: String = toExternalResource.uploadAsImage(event.group).imageId
            withContext(Dispatchers.IO) {
                toExternalResource.close()
            }
            val quoteReply: QuoteReply = QuoteReply(event.message)
            if (Setting.config.recall != 0L) {
                event.subject.sendMessage(quoteReply.plus(Image(imageId)).plus("来源:Pixiv($id)"))
                    .recallIn(Setting.config.recall)
            } else {
                event.subject.sendMessage(quoteReply.plus(Image(imageId)).plus("来源:Pixiv($id)"))
            }
        } catch (e: Exception) {
            logger.warning(e)
            event.subject.sendMessage("发送图片失败")
        }
    }

}