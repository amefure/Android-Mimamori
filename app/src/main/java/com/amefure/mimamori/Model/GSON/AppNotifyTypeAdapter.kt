package com.amefure.mimamori.Model.GSON

import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import com.amefure.mimamori.Model.AppNotify
import com.google.gson.TypeAdapter
import java.text.SimpleDateFormat
import java.util.Date

class AppNotifyTypeAdapter : TypeAdapter<AppNotify>() {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")

    /** オブジェクトをJSONにエンコードする方法を定義 */
    override fun write(out: JsonWriter?, value: AppNotify?) {
        out ?: return
        value ?: return
        // オブジェクトのエンコード開始
        out.beginObject()
        out.name("id").value(value.id)
        out.name("title").value(value.title)
        out.name("msg").value(value.title)
        out.name("time").value(dateFormat.format(value.time))
        // オブジェクトのエンコード終了
        out.endObject()
    }

    /** JSONをオブジェクトにデコードする方法を定義 */
    override fun read(`in`: JsonReader): AppNotify {
        var id: String? = null
        var title: String? = null
        var msg: String? = null
        var time: Date = Date()

        // オブジェクトのデコード開始
        `in`.beginObject()
        // hasNext：未処理のデータがあるか
        while (`in`.hasNext()) {
            // nextName()：キー名を取得
            when (`in`.nextName()) {
                // nextString()：Stringとして読み取り
                "id" -> id = `in`.nextString()
                "title" -> title = `in`.nextString()
                "msg" -> msg = `in`.nextString()
                "time" -> {
                    // peek()：JSONの値を取得
                    time = when (`in`.peek()) {
                        JsonToken.STRING -> dateFormat.parse(`in`.nextString()) ?: Date()
                        JsonToken.NUMBER -> Date(`in`.nextLong() * 1000)  // assuming epoch seconds
                        else -> throw IllegalStateException("Key：timeが期待しないデータ型： " + `in`.peek())
                    }
                }
                // その他のキーはスキップ
                else -> `in`.skipValue()
            }
        }
        // オブジェクトのデコード終了
        `in`.endObject()

        return AppNotify(
            id = id ?: throw IllegalStateException("Missing 'id' field in JSON"),
            title = title ?: throw IllegalStateException("Missing 'title' field in JSON"),
            msg = msg ?: throw IllegalStateException("Missing 'msg' field in JSON"),
            time = time
        )
    }
}