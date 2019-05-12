package tw.shounenwind.kmnbottool.gson

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

open class BoxData : Parcelable {

    @SerializedName("玩家")
    @Expose
    var player: Player? = null
    @SerializedName("寵物")
    @Expose
    var pets: List<Pet>? = null

    protected constructor(`in`: Parcel) {
        this.player = `in`.readValue(Player::class.java.classLoader) as Player
        pets = ArrayList()
        `in`.readList(this.pets, tw.shounenwind.kmnbottool.gson.Pet::class.java.classLoader)
    }

    constructor()

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeValue(player)
        dest.writeList(pets)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<BoxData> = object : Parcelable.Creator<BoxData> {


            override fun createFromParcel(`in`: Parcel): BoxData {
                return BoxData(`in`)
            }

            override fun newArray(size: Int): Array<BoxData?> {
                return arrayOfNulls(size)
            }

        }
    }

}