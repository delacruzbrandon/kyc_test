package com.ub.mcd_sdk

import android.app.Activity
import android.content.Intent
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.JsonObject
import com.ub.mcd_sdk.API.Client
import com.ub.mcd_sdk.mcdScanner.OpenScanner_mcdsdk
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MCD_SDK {

    // Parameters
    // headers {x-ibm-client-id, x-ibm-client-secret, x-partner-id}/

    fun getBillers(headers: HashMap<String, String>): LiveData<Response<JsonObject>> {
        val liveData = MutableLiveData<Response<JsonObject>>()
        Client.getAPI.getBillers(headers).enqueue(object : Callback<JsonObject> {
            override fun onFailure(call: Call<JsonObject>?, t: Throwable?) {
                Log.e("MCD_SDK GetBillerList", "Throw ${t!!.message}")
            }

            override fun onResponse(call: Call<JsonObject>?, response: Response<JsonObject>?) {
                liveData.value = response
            }
        })
        return liveData
    }

    // Parameters
    // headers {x-ibm-client-id, x-ibm-client-secret, x-partner-id}

    //  String
    //  billerID

    fun getBillerReferences(
        headers: HashMap<String, String>,
        billerCode: String
    ): LiveData<Response<JsonObject>> {
        val liveData = MutableLiveData<Response<JsonObject>>()
        Client.getAPI.getBillerReferences(headers, billerCode).enqueue(object : Callback<JsonObject> {
            override fun onFailure(call: Call<JsonObject>?, t: Throwable?) {
                Log.e("MCD_SDK GetReference", "Throw ${t!!.message}")
            }

            override fun onResponse(call: Call<JsonObject>?, response: Response<JsonObject>?) {
                liveData.value = response
            }
        })
        return liveData
    }

    // Parameters
    // headers {x-ibm-client-id, x-ibm-client-secret, x-partner-id}

    //  String
    //  billerID

    // Json type
    // rawBody

    //    {
    //            "biller": {
    //            "id": "7690",
    //            "name": "BELLAGIO 3 CONDOMINIUM ASSOCIATION INC"
    //        },
    //            "date":"2017-11-17T07:08:03.123",
    //            "amount":{
    //            "currency":"PHP",
    //            "value":1
    //        },
    //            "references":[
    //            {
    //                "index":1,
    //                "name":"Name of Payors",
    //                "value":"Juan Dela Cruz"
    //            },
    //            {
    //                "index":2,
    //                "name":"Billing Numbers",
    //                "value":"1"
    //
    //            },
    //            {
    //                "index":3,
    //                "name":"Tenant Code",
    //                "value":"1"
    //
    //            },
    //            {
    //                "index":4,
    //                "name":"Charge Code",
    //                "value":"f"
    //            }
    //            ]
    //        }

    fun validateBillerReferences(
        headers: HashMap<String, String>,
        billerCode: String,
        rawBody: JsonObject
    ): LiveData<Response<JsonObject>> {
        val liveData = MutableLiveData<Response<JsonObject>>()
        Client.getAPI.validateBillerReferences(headers, billerCode, rawBody)
            .enqueue(object : Callback<JsonObject> {
                override fun onFailure(call: Call<JsonObject>?, t: Throwable?) {
                    Log.e("MCD_SDK Validate", "Throw ${t?.message}")
                }

                override fun onResponse(call: Call<JsonObject>?, response: Response<JsonObject>?) {
                    liveData.value = response
                }
            })
        return liveData
    }

    fun getAccessToken(bodyMap: HashMap<String, String>): LiveData<Response<JsonObject>> {
        val liveData = MutableLiveData<Response<JsonObject>>()
        Client.getAPI.getAccessToken(bodyMap).enqueue(object : Callback<JsonObject> {
            override fun onFailure(call: Call<JsonObject>?, t: Throwable?) {
                Log.e("MCD_SDK Access Token", "Throw ${t?.message}")
            }

            override fun onResponse(call: Call<JsonObject>?, response: Response<JsonObject>?) {
                liveData.value = response
            }

        })
        return liveData
    }

//    fun validateImage(headers: HashMap<String, String>,
//                      bodyMap: HashMap<String, String>,
//                      isFront: Boolean, image: MultipartBody.Part): LiveData<Response<ValidateImage>> {
//
//        val liveData = MutableLiveData<Response<ValidateImage>>()
//        Client.getBiller.validateImage(headers, bodyMap,isFront,image).enqueue(object : Callback<ValidateImage> {
//            override fun onFailure(call: Call<ValidateImage>?, t: Throwable?) {
//                Log.e("MCD_SDK Validate Image", "Throw ${t?.message}")
//            }
//            override fun onResponse(call: Call<ValidateImage>?, response: Response<ValidateImage>?) {
//               liveData.value = response
//            }
//        })
//        return liveData
//    }


    //  Parameters
    // headers {x-ibm-client-id, x-ibm-client-secret, x-partner-id, authorization}

    // Body
    // HashMap

    // body["senderRefId"] = "B198500011"
    // body["tranRequestDate"] = "2018-12-12T07:37:28.333"
    // body["deviceId"] = "12345"
    // body["isFrontImage"] = true (Boolean)
    // body["referenceId"] = "123456789"

    fun initializeCamera(
        headers: HashMap<String, String>,
        bodyMap: HashMap<String, Any>,
        activity: Activity
    ) {
        val intent = Intent(activity, OpenScanner_mcdsdk::class.java)
        intent.putExtra("headers", headers)
        intent.putExtra("bodyMap", bodyMap)
        activity.startActivityForResult(intent, 1)
    }

    fun validateUBPCheck(
        headers: HashMap<String, String>,
        rawBody: JsonObject
    ): LiveData<Response<JsonObject>> {
        val liveData = MutableLiveData<Response<JsonObject>>()
        Client.getAPI.validateUBP(headers, rawBody).enqueue(object : Callback<JsonObject> {
            override fun onFailure(call: Call<JsonObject>?, t: Throwable?) {
                Log.e("MCD_SDK ValidateUBP", "Throw ${t?.message}")
            }

            override fun onResponse(call: Call<JsonObject>?, response: Response<JsonObject>?) {
                liveData.value = response
            }
        })
        return liveData
    }

    fun submitCheck(
        headers: HashMap<String, String>,
        rawBody: JsonObject
    ): LiveData<Response<JsonObject>> {
        val liveData = MutableLiveData<Response<JsonObject>>()
        Client.getAPI.submitTransaction(headers, rawBody).enqueue(object : Callback<JsonObject> {
            override fun onFailure(call: Call<JsonObject>?, t: Throwable?) {
                Log.e("MCD_SDK Submit", "Throw ${t?.message}")
            }

            override fun onResponse(call: Call<JsonObject>?, response: Response<JsonObject>?) {
                liveData.value = response
            }
        })
        return liveData
    }

    fun getBanks(headers: HashMap<String, String>): LiveData<Response<JsonObject>> {
        val liveData = MutableLiveData<Response<JsonObject>>()
        Client.getAPI.getBankList(headers).enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>?, response: Response<JsonObject>?) {
                liveData.value = response
            }

            override fun onFailure(call: Call<JsonObject>?, t: Throwable?) {
                Log.e("MCD_SDK BankList", "Throw ${t?.message}")
            }
        })
        return liveData
    }

}
