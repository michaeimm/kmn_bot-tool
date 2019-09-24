package tw.shounenwind.kmnbottool.gson

import android.os.Parcel
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.stream.JsonReader
import java.io.Reader

val gsonInstances: Gson by lazy {
    Gson()
}

inline fun <reified T> Gson.fromJson(json: String): T {
    return fromJson(json, T::class.java)
}

inline fun <reified T> Gson.fromJson(json: Reader): T {
    return fromJson(json, T::class.java)
}

inline fun <reified T> Gson.fromJson(json: JsonElement): T {
    return fromJson(json, T::class.java)
}

inline fun <reified T> Gson.fromJson(reader: JsonReader): T {
    return fromJson(reader, T::class.java)
}

inline fun <reified T> Parcel.readValue(): T {
    return readValue(T::class.java.classLoader) as T
}