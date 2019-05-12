package tw.shounenwind.kmnbottool.gson

import android.os.Parcel
import android.os.Parcelable

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

open class Chip : Parcelable {

    @SerializedName("倍率")
    @Expose
    var magnification: Int? = null
    @SerializedName("編號")
    @Expose
    var number: Int? = null
    @SerializedName("組件")
    @Expose
    var component: String? = null
    @SerializedName("名稱")
    @Expose
    var name: String? = null

    protected constructor(`in`: Parcel) {
        this.magnification = `in`.readValue(Int::class.java.classLoader) as Int
        this.number = `in`.readValue(String::class.java.classLoader) as Int
        this.component = `in`.readValue(Float::class.java.classLoader) as String
        this.name = `in`.readValue(String::class.java.classLoader) as String
    }

    constructor()

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.apply {
            writeValue(magnification)
            writeValue(number)
            writeValue(component)
            writeValue(name)
        }
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<Chip> = object : Parcelable.Creator<Chip> {


            override fun createFromParcel(`in`: Parcel): Chip {
                return Chip(`in`)
            }

            override fun newArray(size: Int): Array<Chip?> {
                return arrayOfNulls(size)
            }

        }
    }

}
