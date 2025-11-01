package com.example.trelloclonemaster3.network

import com.example.trelloclonemaster3.model.PushNotification
import com.example.trelloclonemaster3.utils.FCMConstants
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface ApiInterface {

    @Headers("Content-Type:${FCMConstants.CONTENT_TYPE}","Authorization:key=${FCMConstants.SERVER_KEY}")
    @POST("fcm/send")
    fun sendNotification(
        @Body notification: PushNotification
    ): Call<ResponseBody>
}