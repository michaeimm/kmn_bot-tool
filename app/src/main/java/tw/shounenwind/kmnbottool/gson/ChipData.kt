package tw.shounenwind.kmnbottool.gson

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

open class ChipData : Parcelable {

    @SerializedName("玩家")
    @Expose
    lateinit var player: Player
    @SerializedName("晶片")
    @Expose
    var chips: List<Chip> = ArrayList()

    protected constructor(`in`: Parcel) {
        this.player = `in`.readValue(Player::class.java.classLoader) as Player
        `in`.readList(this.chips, Pet::class.java.classLoader)
    }

    constructor()

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeValue(player)
        dest.writeList(chips)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<ChipData> = object : Parcelable.Creator<ChipData> {


            override fun createFromParcel(`in`: Parcel): ChipData {
                return ChipData(`in`)
            }

            override fun newArray(size: Int): Array<ChipData?> {
                return arrayOfNulls(size)
            }

        }
    }

}