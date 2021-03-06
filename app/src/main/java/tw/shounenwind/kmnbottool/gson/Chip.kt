package tw.shounenwind.kmnbottool.gson

import android.os.Parcel
import android.os.Parcelable

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

open class Chip : Parcelable {

    @SerializedName("倍率")
    @Expose
    var magnification: Int = 0
    @SerializedName("編號")
    @Expose
    var number: Int = 0
    @SerializedName("組件")
    @Expose
    var component: String = ""
    @SerializedName("名稱")
    @Expose
    var name: String = ""

    protected constructor(`in`: Parcel) {
        this.magnification = `in`.readValue()
        this.number = `in`.readValue()
        this.component = `in`.readValue()
        this.name = `in`.readValue()
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
