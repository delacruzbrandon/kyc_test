package com.quest.dao.model

import com.google.gson.annotations.SerializedName

data class CardHolder(
    @SerializedName("firstName")
    var firstName: String,
    @SerializedName("lastName")
    var lastName: String,
    @SerializedName("issuingCountry")
    var country: String,
    @SerializedName("dob")
    var birthDay: String,
    @SerializedName("number")
    var cardNumber: String
) {

    override fun toString(): String {
        return "Documents:" +
                "\nFirst Name: $firstName" +
                "\nLast Name: $lastName" +
                "\nBirth Day: $birthDay" +
                "\nCountry: $country" +
                "\nCard no.: $cardNumber"
    }
}