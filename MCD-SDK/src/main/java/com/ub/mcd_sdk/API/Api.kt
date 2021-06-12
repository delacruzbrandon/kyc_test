package com.ub.mcd_sdk.API

import com.google.gson.JsonObject
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.*

interface Api {

    @GET("billers/v1/list")
    fun getBillers(@HeaderMap headers: HashMap<String,String>): Call<JsonObject>

    @GET("billers/v1/{code}/references")
    fun getBillerReferences(@HeaderMap headers: HashMap<String,String>,
                            @Path( "code", encoded = true) code: String): Call<JsonObject>

    @POST("partners/ubp/v1/billers/{code}/references")
    fun validateBillerReferences(@HeaderMap headers: HashMap<String, String>,
                                 @Path( "code", encoded = true) code: String,
                                 @Body jsonObject: JsonObject
    ): Call<JsonObject>
    @FormUrlEncoded
    @POST("partners/v1/oauth2/token")
    fun getAccessToken(@FieldMap headers: HashMap<String, String>): Call<JsonObject>

    @Multipart
    @POST("partners/ubp/v1/mcd/checks/upload")
    fun validateImage(@HeaderMap headers: HashMap<String, String>,
                      @PartMap bodyForm: HashMap<String, String>,
                      @Part("isFrontImage") isFrontImage: Boolean?,
                      @Part image: MultipartBody.Part?
    ): Call<JsonObject>

    @POST("partners/ubp/v1/mcd/checks/validateubp")
    fun validateUBP(@HeaderMap headers: HashMap<String, String>,
                    @Body jsonObject: JsonObject
    ): Call<JsonObject>

    @POST("partners/ubp/v1/mcd/checks/submit")
    fun submitTransaction(@HeaderMap headers: HashMap<String, String>,
                          @Body jsonObject: JsonObject
    ): Call<JsonObject>

    @GET("partners/ubp/v1/mcd/banks")
    fun getBankList(@HeaderMap headers: HashMap<String,String>): Call<JsonObject>

}