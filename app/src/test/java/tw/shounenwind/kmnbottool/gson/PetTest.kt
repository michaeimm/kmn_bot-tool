package tw.shounenwind.kmnbottool.gson

import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Test

class PetTest {

    @Test
    fun petParse() {
        val pet = Gson().fromJson<Pet>("{\n" +
                "    \"最大階級\":100,\n" +
                "    \"技能\":\"技能⓪：光輝質點\\r\\n→ 非Lv.Max時才能發動。\\r\\n減少1階級、尚需經驗值減半。\\r\\n技能①：王冠填充\\u2027I\\r\\n→ Lv.Max時、作為後衛才能發動。\\r\\n移除這個道具、獲得1隻季節TYPE的機器狼。\\r\\n迷霧與無季節時、上述TYPE改為隨機值。\\r\\n\",\n" +
                "    \"系列\":\"道具\",\n" +
                "    \"最大等級\":100,\n" +
                "    \"圖片\":\"http://i.imgur.com/IvIlzLS.png\",\n" +
                "    \"稀有度\":1,\n" +
                "    \"寵物名稱\":\"電子方塊\",\n" +
                "    \"下場TYPE\":\"[白]\",\n" +
                "    \"等級\":87,\n" +
                "    \"階級\":46,\n" +
                "    \"原TYPE\":\"[白]\"\n" +
                "}", Pet::class.java!!)

        assertEquals((pet.maxClass as Int).toLong(), 100)
        assertEquals(pet.skill, "技能⓪：光輝質點\r\n" +
                "→ 非Lv.Max時才能發動。\r\n" +
                "減少1階級、尚需經驗值減半。\r\n" +
                "技能①：王冠填充‧I\r\n" +
                "→ Lv.Max時、作為後衛才能發動。\r\n" +
                "移除這個道具、獲得1隻季節TYPE的機器狼。\r\n" +
                "迷霧與無季節時、上述TYPE改為隨機值。\r\n")
        assertEquals(pet.series, "道具")
        assertEquals((pet.maxLevel as Int).toLong(), 100)
        assertEquals(pet.image, "http://i.imgur.com/IvIlzLS.png")
        assertEquals((pet.rare as Int).toLong(), 1)
        assertEquals(pet.name, "電子方塊")
        assertEquals(pet.battleType, "[白]")
        assertEquals((pet.level as Int).toLong(), 87)
        assertEquals((pet.petClass as Int).toLong(), 46)
        assertEquals(pet.type, "[白]")
    }
}