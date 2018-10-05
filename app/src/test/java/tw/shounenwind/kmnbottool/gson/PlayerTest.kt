package tw.shounenwind.kmnbottool.gson

import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Test

class PlayerTest {

    @Test
    fun playerJsonParse() {
        val player = Gson().fromJson<Player>(
                "{\n" +
                        "\n" +
                        "    \"彩虹齒輪\":863,\n" +
                        "    \"蒐集完成度\":\"14.08%\",\n" +
                        "    \"亂入率\":0.8587,\n" +
                        "    \"_id\":\"6820974\",\n" +
                        "    \"名稱\":\"小風☆魔女公主天然荔枝 哥哥\"\n" +
                        "\n" +
                        "}",
                Player::class.java!!
        )

        assertEquals(player.rainbowGear as Long, 863)
        assertEquals(player.completedRate, "14.08%")
        assertEquals(player.incomingRate!!.toDouble(), 0.8537, 4.0)
        assertEquals(player.id, "6820974")
        assertEquals(player.name, "小風☆魔女公主天然荔枝 哥哥")
    }

}