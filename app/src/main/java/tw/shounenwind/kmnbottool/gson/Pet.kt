package tw.shounenwind.kmnbottool.gson

import android.os.Parcel
import android.os.Parcelable

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.util.*

open class Pet : Parcelable {

    @SerializedName("最大階級")
    @Expose
    var maxClass: Int? = null
    @SerializedName("技能")
    @Expose
    var skill: String? = null
    @SerializedName("系列")
    @Expose
    var series: String? = null
    @SerializedName("最大等級")
    @Expose
    var maxLevel: Int? = null
    @SerializedName("圖片")
    @Expose
    var image: String? = null
    @SerializedName("稀有度")
    @Expose
    var rare: Int? = null
    @SerializedName("寵物名稱")
    @Expose
    var name: String? = null
    @SerializedName("下場TYPE")
    @Expose
    var battleType: String? = null
    @SerializedName("等級")
    @Expose
    var level: Int? = null
    @SerializedName("階級")
    @Expose
    var petClass: Int? = null
    @SerializedName("原TYPE")
    @Expose
    var type: String? = null

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
        dest.writeValue(maxClass)
        dest.writeValue(skill)
        dest.writeValue(series)
        dest.writeValue(maxLevel)
        dest.writeValue(image)
        dest.writeValue(rare)
        dest.writeValue(name)
        dest.writeValue(battleType)
        dest.writeValue(level)
        dest.writeValue(petClass)
        dest.writeValue(type)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun hashCode(): Int {
        return Arrays.hashCode(name!!.toCharArray())
    }

    override fun toString(): String {
        return name!!
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
