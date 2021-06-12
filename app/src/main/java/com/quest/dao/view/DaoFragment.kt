package com.quest.dao.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.jumio.core.enums.JumioDataCenter
import com.jumio.core.exceptions.MissingPermissionException
import com.jumio.core.exceptions.PlatformNotSupportedException
import com.jumio.nv.NetverifyDeallocationCallback
import com.jumio.nv.NetverifyDocumentData
import com.jumio.nv.NetverifyMrzData
import com.jumio.nv.NetverifySDK
import com.jumio.nv.data.document.NVDocumentType
import com.jumio.nv.data.document.NVDocumentVariant
import com.quest.dao.DaoSDK
import com.quest.dao.MainActivity
import com.quest.dao.R
import com.quest.dao.databinding.FragmentDaoBinding
import com.quest.dao.viewmodel.DaoViewModel
import com.quest.dao.viewmodel.DaoViewModelFactory
import java.util.*

class DaoFragment : Fragment(), NetverifyDeallocationCallback {
    private lateinit var daoBinding: FragmentDaoBinding
    private lateinit var daoFactory: DaoViewModelFactory
    private lateinit var daoViewModel: DaoViewModel
    private lateinit var netverifySDK: NetverifySDK
    private lateinit var daoSDK: DaoSDK

    private var apiToken: String? = null
    private var apiSecret: String? = null
    private var dataCenter: JumioDataCenter? = null
    private var mrzData: NetverifyMrzData? = null

    companion object {
        private const val TAG = "NetverifyFragment"
        private const val PERMISSION_REQUEST_CODE_NETVERIFY = 303
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        daoBinding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_dao, container, false)

        daoFactory = DaoViewModelFactory(mrzData.toString())
        daoViewModel = ViewModelProvider(this)[DaoViewModel::class.java]

        daoBinding.clickHandler = this
        daoBinding.viewModel = daoViewModel
        daoViewModel.cardHolder.observe(viewLifecycleOwner, {
            Log.d(TAG, "onCreateView: $it")
            Toast.makeText(requireContext(), it.toString(), Toast.LENGTH_SHORT).show()
        })

        apiToken = arguments?.getString(MainActivity.KEY_API_TOKEN)
        apiSecret = arguments?.getString(MainActivity.KEY_API_SECRET)
        dataCenter = arguments?.getSerializable(MainActivity.KEY_DATACENTER) as JumioDataCenter

        return daoBinding.root
    }

    fun onVerify(view: View) {
        Log.d(TAG, "onClick: True")

        // TODO Initialize OAuth before DAO
        initializeDao()

        if ((activity as MainActivity).checkPermissions(PERMISSION_REQUEST_CODE_NETVERIFY)) {
            try {
                if (::netverifySDK.isInitialized) {
                    view.isEnabled = false
                    startActivityForResult(netverifySDK.intent, NetverifySDK.REQUEST_CODE)
                }
            } catch (e: MissingPermissionException) {
                Toast.makeText(activity, e.message, Toast.LENGTH_LONG).show()
                view.isEnabled = true
            }
        }
    }

    private fun initializeDao() {
        try {
            if (!NetverifySDK.isSupportedPlatform(activity))
                Log.w(TAG, "Device not supported")

            if (NetverifySDK.isRooted(activity))
                Log.w(TAG, "Device is rooted")

//            daoSDK = DaoSDK().create(activity)

            netverifySDK = NetverifySDK.create(activity, apiToken, apiSecret, dataCenter)
            netverifySDK.setEnableVerification(true)
            netverifySDK.setPreselectedCountry("PHL")

			val documentTypes = ArrayList<NVDocumentType>()
			documentTypes.add(NVDocumentType.IDENTITY_CARD)
			netverifySDK.setPreselectedDocumentTypes(documentTypes)
			netverifySDK.setPreselectedDocumentVariant(NVDocumentVariant.PLASTIC)
//            netverifySDK.setCustomerInternalReference("YOURSCANREFERENCE")
//            netverifySDK.setReportingCriteria("YOURREPORTINGCRITERIA")
//            netverifySDK.setCallbackUrl("YOURCALLBACKURL")

        } catch (e: PlatformNotSupportedException) {
            Log.e(TAG, "Error in initializeNetverifySDK: ", e)
            Toast.makeText(activity?.applicationContext, e.message, Toast.LENGTH_LONG).show()
        } catch (e1: NullPointerException) {
            Log.e(TAG, "Error in initializeNetverifySDK: ", e1)
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == NetverifySDK.REQUEST_CODE) {

            val scanReference = data?.getStringExtra(NetverifySDK.EXTRA_SCAN_REFERENCE)
            Log.d(TAG, "onActivityResult: $scanReference")

            if (resultCode == Activity.RESULT_OK) {
                //Handle the success case and retrieve scan data
                val documentData = data?.getParcelableExtra<Parcelable>(NetverifySDK.EXTRA_SCAN_DATA) as? NetverifyDocumentData
                mrzData = documentData?.mrzData

                Log.d(TAG, "onActivityResult: $mrzData")
                Log.d(TAG, "onActivityResult: ${documentData?.firstName}")
                Log.d(TAG, "onActivityResult: ${documentData?.gender}")
                Log.d(TAG, "onActivityResult: ${documentData?.idNumber}")

            } else if (resultCode == Activity.RESULT_CANCELED) {
                //Handle the error cases as highlighted in our documentation: https://github.com/Jumio/mobile-sdk-android/blob/master/docs/integration_faq.md#managing-errors
                val errorMessage = data?.getStringExtra(NetverifySDK.EXTRA_ERROR_MESSAGE)
                val errorCode = data?.getStringExtra(NetverifySDK.EXTRA_ERROR_CODE)
                Log.d(TAG, "onActivityResult: $errorMessage")
                Log.d(TAG, "onActivityResult: $errorCode")
            }

            //At this point, the SDK is not needed anymore. It is highly advisable to call destroy(), so that
            //internal resources can be freed.
            netverifySDK.destroy()
            netverifySDK.checkDeallocation(this)
        }
    }

    override fun onNetverifyDeallocated() {
        activity?.runOnUiThread {
//            netverifyBinding.buttonNetverifyFragmentVerify?.isEnabled = true
        }
    }

}