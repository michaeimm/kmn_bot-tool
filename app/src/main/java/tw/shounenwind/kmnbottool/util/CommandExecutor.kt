package tw.shounenwind.kmnbottool.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Parcelable
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import tw.shounenwind.kmnbottool.R
import java.util.*

object CommandExecutor {

    fun botDraw(mContext: Context) {
        val options = arrayOf(mContext.getString(R.string.bot_draw_normal), mContext.getString(R.string.bot_draw_ultra))
        AlertDialog.Builder(mContext)
                .setItems(options) { _, i ->
                    when (i) {
                        0 -> sendCommand(mContext, mContext.getString(R.string.command_draw))
                        1 -> sendCommand(mContext, mContext.getString(R.string.command_draw_ultra))
                    }
                }
                .show()
    }

    fun expDraw(mContext: Context, target: String) {
        val options = arrayOf(mContext.getString(R.string.bot_exp_normal), mContext.getString(R.string.bot_exp_ultra))
        AlertDialog.Builder(mContext)
                .setTitle(target)
                .setItems(options) { _, i ->
                    when (i) {
                        0 -> sendCommand(mContext, mContext.getString(R.string.command_exp, target))
                        1 -> sendCommand(mContext, mContext.getString(R.string.command_exp_ultra, target))
                    }
                }
                .show()
    }

    fun battleNormal(mContext: Context, target: String) {
        sendCommand(mContext, mContext.getString(R.string.command_battle, target))
    }

    fun battleHell(mContext: Context, target: String) {
        sendCommand(mContext, mContext.getString(R.string.command_hell_battle, target))
    }

    fun battleUltraHell(mContext: Context, target: String) {
        sendCommand(mContext, mContext.getString(R.string.command_ultra_hell_battle, target))
    }

    private fun sendCommand(mContext: Context, command: String) {

        val targetUri = Uri.parse(mContext.getString(R.string.command_prefix) + command + mContext.getString(R.string.command_append))
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

        for (aTarget in textSharedTarget) {

            val manager = mContext.packageManager
            val searchIntent = Intent().setPackage(aTarget)
            val infoList = manager.queryIntentActivities(searchIntent, 0)

            if (infoList != null && infoList.size > 0) {
                val targeted = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_SUBJECT, mContext.getString(R.string.app_name))
                    putExtra(Intent.EXTRA_TEXT, command + " " + mContext.getString(R.string.bz))
                    setPackage(aTarget)
                }
                targetedShareIntents.add(targeted)
            }

        }

        val resInfo = mContext.packageManager.queryIntentActivities(intent, 0)

        if (!resInfo.isEmpty()) {
            for (info in resInfo) {
                val activityInfo = info.activityInfo
                LogUtil.d("packageName", activityInfo.packageName)
                if (!activityInfo.packageName.equals("com.plurk.android", ignoreCase = true)) {
                    val targeted = Intent(Intent.ACTION_VIEW, targetUri)
                    targeted.setPackage(activityInfo.packageName)
                    targetedShareIntents.add(targeted)
                }
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
            mContext.startActivity(chooserIntent)
        } catch (ex: android.content.ActivityNotFoundException) {
            Toast.makeText(mContext, R.string.no_available_app, Toast.LENGTH_SHORT).show()
        }

    }

}