package tw.shounenwind.kmnbottool.util

import android.content.Intent
import android.net.Uri
import android.os.Parcelable
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import tw.shounenwind.kmnbottool.R
import tw.shounenwind.kmnbottool.skeleton.BaseActivity
import java.util.*

object CommandExecutor {

    fun BaseActivity.botDraw() {
        val options = arrayOf(getString(R.string.bot_draw_normal), getString(R.string.bot_draw_ultra))
        AlertDialog.Builder(this)
                .setItems(options) { _, i ->
                    when (i) {
                        0 -> sendCommand(getString(R.string.command_draw))
                        1 -> sendCommand(getString(R.string.command_draw_ultra))
                    }
                }
                .show()
    }

    fun BaseActivity.expDraw(target: String) {
        val options = arrayOf(getString(R.string.bot_exp_normal), getString(R.string.bot_exp_ultra))
        AlertDialog.Builder(this)
                .setTitle(target)
                .setItems(options) { _, i ->
                    when (i) {
                        0 -> sendCommand(getString(R.string.command_exp, target))
                        1 -> sendCommand(getString(R.string.command_exp_ultra, target))
                    }
                }
                .show()
    }

    fun BaseActivity.battleNormal(target: String) {
        sendCommand(getString(R.string.command_battle, target))
    }

    fun BaseActivity.battleHell(target: String) {
        sendCommand(getString(R.string.command_hell_battle, target))
    }

    fun BaseActivity.battleUltraHell(target: String) {
        sendCommand(getString(R.string.command_ultra_hell_battle, target))
    }

    private fun BaseActivity.sendCommand(command: String) {

        val targetUri = Uri.parse(getString(R.string.command_prefix) + command + getString(R.string.command_append))
        val intent = Intent(Intent.ACTION_VIEW, targetUri)
        val targetedShareIntents = ArrayList<Intent>()

        val textSharedTarget = arrayOf(
                "com.plurk.android",
                "tw.anddev.aplurk",
                "com.skystar.plurk",
                "com.roguso.plurk",
                "com.nauj27.android.pifeb",
                "idv.brianhsu.maidroid.plurk"
        )

        textSharedTarget.asSequence().filter {
            val manager = packageManager
            val searchIntent = Intent().setPackage(it)
            val infoList = manager.queryIntentActivities(searchIntent, 0)
            (infoList != null && infoList.size > 0)
        }.forEach {
            val targeted = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name))
                putExtra(Intent.EXTRA_TEXT, command + " " + getString(R.string.bz))
                setPackage(it)
            }
            targetedShareIntents.add(targeted)
        }

        val resInfo = packageManager.queryIntentActivities(intent, 0)

        if (resInfo.isNotEmpty()) {
            resInfo.asSequence().map {
                it.activityInfo.packageName
            }.filterNot {
                it.equals("com.plurk.android", ignoreCase = true)
            }.forEach {
                val targeted = Intent(Intent.ACTION_VIEW, targetUri)
                targeted.setPackage(it)
                targetedShareIntents.add(targeted)
            }
        }

        try {
            if (targetedShareIntents.size == 0) {
                throw Exception()
            }
            val chooserIntent = Intent.createChooser(targetedShareIntents.removeAt(0), "Open...")
                    .apply {
                        putExtra(Intent.EXTRA_TITLE, "Open...")
                        putExtra(Intent.EXTRA_INITIAL_INTENTS, targetedShareIntents.toTypedArray<Parcelable>())
                    }
            startActivity(chooserIntent)
        } catch (ex: android.content.ActivityNotFoundException) {
            Toast.makeText(this, R.string.no_available_app, Toast.LENGTH_SHORT).show()
        }

    }

}