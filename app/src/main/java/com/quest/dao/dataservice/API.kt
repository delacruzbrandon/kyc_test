package com.quest.dao.dataservice

import com.google.gson.JsonObject
import com.quest.dao.model.CardHolder
import retrofit2.Call
import retrofit2.http.*

interface API {

    @GET("api/netverify/v2/scans/{codeReference}/data/document")
    suspend fun getHolderDocuments(
        @Path("codeReference") codeReference: String
    ): CardHolder

    @FormUrlEncoded
    @POST("partners/v1/oauth2/token")
    fun getAccessToken(@FieldMap headers: HashMap<String, String>): Call<JsonObject>
}