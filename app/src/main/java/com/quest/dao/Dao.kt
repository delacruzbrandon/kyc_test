

package com.quest.dao

import android.app.Activity
import android.util.Log
import android.widget.Toast
import com.jumio.core.enums.JumioDataCenter
import com.jumio.core.exceptions.PlatformNotSupportedException
import com.jumio.nv.NetverifySDK
import com.quest.dao.view.DaoFragment

class DaoSDK {

    private lateinit var daoSDK: NetverifySDK

    val apiToken = MainActivity.KEY_API_TOKEN
    val apiSecret = MainActivity.KEY_API_SECRET

    init {


    }

    fun create(activity: Activity): NetverifySDK {

        try {
            if (!NetverifySDK.isSupportedPlatform(activity))
                Log.w(DaoFragment.TAG, "Device not supported")

            if (NetverifySDK.isRooted(activity))
                Log.w(DaoFragment.TAG, "Device is rooted")

        } catch (e: PlatformNotSupportedException) {
            Log.e(DaoFragment.TAG, "Error in initializeNetverifySDK: ", e)
            Toast.makeText(activity?.applicationContext, e.message, Toast.LENGTH_LONG).show()
        } catch (e1: NullPointerException) {
            Log.e(DaoFragment.TAG, "Error in initializeNetverifySDK: ", e1)
        }

        daoSDK = NetverifySDK.create(activity, apiToken, apiSecret, JumioDataCenter.US)
        return daoSDK
    }
}