package tw.shounenwind.kmnbottool.activities

import android.os.Bundle
import android.os.Parcelable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.toast
import tw.shounenwind.kmnbottool.R
import tw.shounenwind.kmnbottool.skeleton.BaseActivity
import tw.shounenwind.kmnbottool.util.KmnBotDataLoader
import tw.shounenwind.kmnbottool.util.LogUtil

class PetsLinkActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            val params = intent.data?.pathSegments ?: return finish()
            LogUtil.d("params", params.toString())
            GlobalScope.launch {
                showProgressDialog(getString(R.string.loading))
                val userName = params[1].removeSuffix(".txt").removeSuffix(".json")
                getData(userName)
            }
        } catch (e: Exception) {
            LogUtil.printStackTrace(e)
            toast(R.string.load_data_failed)
            finish()
        }

    }

    private suspend fun getData(user: String) = withContext(Dispatchers.IO) {
        val kmnBotDataLoader = KmnBotDataLoader()
        kmnBotDataLoader.setUser(user)
                .setOnSuccessListener { data ->
                    if (isFinishing)
                        return@setOnSuccessListener
                    mainScope?.launch {
                        LogUtil.catchAndPrint {
                            dismissProgressDialog()
                            val boxData = data.boxData
                            startActivityWithTransition(intentFor<BoxActivity>(
                                    "boxData" to (boxData as Parcelable)
                            ))
                            finish()
                        }
                    }
                }
                .setOnFailedListener {
                    if (isFinishing)
                        return@setOnFailedListener
                    mainScope?.launch {
                        LogUtil.catchAndPrint {
                            dismissProgressDialog()
                            toast(R.string.load_data_failed)
                            finish()
                        }
                    }
                }
                .start()
    }
}