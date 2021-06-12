package com.quest.dao

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.jumio.MobileSDK
import com.jumio.core.enums.JumioDataCenter
import com.quest.dao.databinding.ActivityMainBinding
import com.quest.dao.view.DaoFragment

class MainActivity : AppCompatActivity() {

    private lateinit var maiBinding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        maiBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        val nvFragment = DaoFragment()
        val bundle = Bundle()

        bundle.putString(KEY_API_TOKEN, DAO_API_TOKEN)
        bundle.putString(KEY_API_SECRET, DAO_API_SECRET)
        bundle.putSerializable(KEY_DATACENTER, DAO_DATACENTER)
        nvFragment.arguments = bundle
        switchFragment(nvFragment)
    }

    private fun switchFragment(fragment: Fragment) {
        val supportFragmentManager = supportFragmentManager
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragment_container, fragment)
        fragmentTransaction.commitAllowingStateLoss()
    }

    fun checkPermissions(requestCode: Int): Boolean {
        return if (!MobileSDK.hasAllRequiredPermissions(this)) {
            val missingPermissions = MobileSDK.getMissingPermissions(this)
            ActivityCompat.requestPermissions(this, missingPermissions, requestCode)
            false
        } else {
            true
        }
    }

    companion object {
        const val KEY_API_TOKEN = BuildConfig.KEY_API_TOKEN
        const val KEY_API_SECRET = BuildConfig.KEY_API_SECRET
        const val KEY_DATACENTER = "KEY_DATACENTER"
        /**
         * PUT YOUR NETVERIFY API TOKEN AND SECRET HERE
         * Do not store your credentials hardcoded within the app. Make sure to store them server-side and load your credentials during runtime.
         */
        private const val DAO_API_TOKEN = BuildConfig.KEY_API_TOKEN
        private const val DAO_API_SECRET = BuildConfig.KEY_API_SECRET
        private val DAO_DATACENTER = JumioDataCenter.US
    }

}