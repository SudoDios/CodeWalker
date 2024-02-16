package me.sudodios.codewalker.utils

import androidx.compose.runtime.snapshots.SnapshotStateList
import com.google.gson.*
import com.google.gson.reflect.TypeToken
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

object GsonUtils {

    val gson : Gson = GsonBuilder().registerTypeAdapter(SnapshotStateList::class.java, CustomListConverter()).create()

    fun <T> fromJson (json: String?, classOfT: Class<T>?): T {
        return gson.fromJson(json,classOfT)
    }

    fun <T> fromJson (json: JsonElement?, classOfT: Class<T>?): T {
        return gson.fromJson(json,classOfT)
    }

    fun <T> String?.getArray(type : Type) : ArrayList<T> {
        return gson.fromJson(if (this.isNullOrEmpty()) "[]" else this, TypeToken.getParameterized(ArrayList::class.java, type).type)
    }

}


internal class CustomListConverter : JsonDeserializer<SnapshotStateList<*>?> {
    override fun deserialize(json: JsonElement, typeOfT: Type, ctx: JsonDeserializationContext): SnapshotStateList<Any> {
        val valueType: Type = (typeOfT as ParameterizedType).actualTypeArguments[0]
        val list: SnapshotStateList<Any> = SnapshotStateList()
        for (item in json.getAsJsonArray()) { list.add(ctx.deserialize(item, valueType)) }
        return list
    }
}



