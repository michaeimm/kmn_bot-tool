package tw.shounenwind.kmnbottool.gson

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

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
        this.maxClass = `in`.readValue()
        this.skill = `in`.readValue()
        this.series = `in`.readValue()
        this.maxLevel = `in`.readValue()
        this.image = `in`.readValue()
        this.rare = `in`.readValue()
        this.name = `in`.readValue()
        this.battleType = `in`.readValue()
        this.level = `in`.readValue()
        this.petClass = `in`.readValue()
        this.type = `in`.readValue()
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
        return name.toCharArray().contentHashCode()
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
