package tw.shounenwind.kmnbottool.gson

import android.os.Parcel
import android.os.Parcelable

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

open class Player : Parcelable {

    @SerializedName("彩虹齒輪")
    @Expose
    var rainbowGear: Int = 0
    @SerializedName("蒐集完成度")
    @Expose
    var completedRate: String = "0%"
    @SerializedName("亂入率")
    @Expose
    var incomingRate: Float = 0f
    @SerializedName("_id")
    @Expose
    var id: String = ""
    @SerializedName("名稱")
    @Expose
    var name: String = ""

    protected constructor(`in`: Parcel) {
        this.rainbowGear = `in`.readValue(Int::class.java.classLoader) as Int
        this.completedRate = `in`.readValue(String::class.java.classLoader) as String
        this.incomingRate = `in`.readValue(Float::class.java.classLoader) as Float
        this.id = `in`.readValue(String::class.java.classLoader) as String
        this.name = `in`.readValue(String::class.java.classLoader) as String
    }

    constructor()

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.apply {
            writeValue(rainbowGear)
            writeValue(completedRate)
            writeValue(incomingRate)
            writeValue(id)
            writeValue(name)
        }
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<Player> = object : Parcelable.Creator<Player> {


            override fun createFromParcel(`in`: Parcel): Player {
                return Player(`in`)
            }

            override fun newArray(size: Int): Array<Player?> {
                return arrayOfNulls(size)
            }

        }
    }

}
