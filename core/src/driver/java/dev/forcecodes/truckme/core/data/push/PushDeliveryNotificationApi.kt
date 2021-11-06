package dev.forcecodes.truckme.core.data.push

import dev.forcecodes.truckme.core.BuildConfig
import dev.forcecodes.truckme.core.fcm.PushMessage
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface PushDeliveryNotificationApi {

  @Headers(
    "Authorization: key=${BuildConfig.FCM_SERVER_API}",
    "Content-Type:application/json"
  )
  @POST("fcm/send")
  suspend fun sendMessage(
    @Body pushMessage: PushMessage
  ): Response<ResponseBody>
}
