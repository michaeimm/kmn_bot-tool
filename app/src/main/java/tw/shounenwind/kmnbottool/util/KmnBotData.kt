package tw.shounenwind.kmnbottool.util

import tw.shounenwind.kmnbottool.gson.BoxData
import tw.shounenwind.kmnbottool.gson.ChipData

class KmnBotData(val boxData: BoxData? = null, val chipsData: ChipData? = null) {

    companion object {
        var cache: KmnBotData? = null
    }
}