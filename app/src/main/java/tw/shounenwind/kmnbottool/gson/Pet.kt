package tw.shounenwind.kmnbottool.gson

import android.os.Parcel
import android.os.Parcelable

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.util.*

open class Pet : Parcelable {

    @SerializedName("最大階級")
    @Expose
    var maxClass: Int = 1
    @SerializedName("技能")
    @Expose
    var skill: String = ""
    @SerializedName("系列")
    @Expose
    var series: String = ""
    @SerializedName("最大等級")
    @Expose
    var maxLevel: Int = 1
    @SerializedName("圖片")
    @Expose
    var image: String = ""
    @SerializedName("稀有度")
    @Expose
    var rare: Int = 0
    @SerializedName("寵物名稱")
    @Expose
    var name: String = ""
    @SerializedName("下場TYPE")
    @Expose
    var battleType: String = ""
    @SerializedName("等級")
    @Expose
    var level: Int = 0
    @SerializedName("階級")
    @Expose
    var petClass: Int = 0
    @SerializedName("原TYPE")
    @Expose
    var type: String = ""

    protected constructor(`in`: Parcel) {
        this.maxClass = `in`.readValue(Int::class.java.classLoader) as Int
        this.skill = `in`.readValue(String::class.java.classLoader) as String
        this.series = `in`.readValue(String::class.java.classLoader) as String
        this.maxLevel = `in`.readValue(Int::class.java.classLoader) as Int
        this.image = `in`.readValue(String::class.java.classLoader) as String
        this.rare = `in`.readValue(Int::class.java.classLoader) as Int
        this.name = `in`.readValue(String::class.java.classLoader) as String
        this.battleType = `in`.readValue(String::class.java.classLoader) as String
        this.level = `in`.readValue(Int::class.java.classLoader) as Int
        this.petClass = `in`.readValue(Int::class.java.classLoader) as Int
        this.type = `in`.readValue(String::class.java.classLoader) as String
    }

    constructor()

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.apply {
            writeValue(maxClass)
            writeValue(skill)
            writeValue(series)
            writeValue(maxLevel)
            writeValue(image)
            writeValue(rare)
            writeValue(name)
            writeValue(battleType)
            writeValue(level)
            writeValue(petClass)
            writeValue(type)
        }
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun hashCode(): Int {
        return Arrays.hashCode(name.toCharArray())
    }

    override fun toString(): String {
        return name
    }

    override fun equals(other: Any?): Boolean {
        return other is Pet && other.name == this.name
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<Pet> = object : Parcelable.Creator<Pet> {


            override fun createFromParcel(`in`: Parcel): Pet {
                return Pet(`in`)
            }

            override fun newArray(size: Int): Array<Pet?> {
                return arrayOfNulls(size)
            }

        }
    }

}
