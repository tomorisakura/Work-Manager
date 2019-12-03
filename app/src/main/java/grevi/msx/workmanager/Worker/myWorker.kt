package grevi.msx.workmanager.Worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.loopj.android.http.AsyncHttpResponseHandler
import com.loopj.android.http.SyncHttpClient
import cz.msebera.android.httpclient.Header
import grevi.msx.workmanager.R
import org.json.JSONObject
import java.lang.Exception
import java.text.DecimalFormat

class myWorker(context: Context, workerParameters: WorkerParameters) : Worker(context, workerParameters) {

    companion object {
        private val TAG = myWorker::class.java.simpleName
        const val APP_ID = "3d0639b525042456dd2ed6ce09505ee3"
        const val EXTRA_CITY = "city"
        const val NOTIF_ID = 1
        const val CHANNEL_ID = "channel_01"
        const val CHANNEL_NAME = "grep channel"
    }

    private var resultStatus : Result? = null

    override fun doWork(): Result {
        val dataCity = inputData.getString(EXTRA_CITY)
        val result : Result = getCurrentWeather(dataCity)
        return result
    }

    private fun getCurrentWeather(string: String?) : Result {
        Log.d(TAG, "get current weather : Mulai....")
        val client = SyncHttpClient()
        val url = "https://api.openweathermap.org/data/2.5/weather?q=$string&appid=$APP_ID"
        Log.d(TAG, "get current url : $url")
        client.post(url, object : AsyncHttpResponseHandler() {
            override fun onSuccess(
                statusCode: Int,
                headers: Array<out Header>?,
                responseBody: ByteArray?
            ) {
                val result = responseBody?.let { String(it) }
                Log.d(TAG, result)
                try {
                    val responseObject = JSONObject(result)
                    val currentWeather : String = responseObject.getJSONArray("weather").getJSONObject(0).getString("main")
                    val description : String = responseObject.getJSONArray("weather").getJSONObject(0).getString("description")
                    val tempKelvin = responseObject.getJSONObject("main").getDouble("temp")
                    val tempCelcius = tempKelvin - 273
                    val temparature : String = DecimalFormat("##.##").format(tempCelcius)
                    val title = "Current Weather in $string"
                    val message = "$currentWeather, $description with $temparature celcius"
                    showNotif(title, message)
                    Log.d(TAG, "on success : Selesai !...")
                    resultStatus = Result.success()

                }catch ( e : Exception) {
                    showNotif("Gagal Saat mengambil Informasi Cuaca", e.message)
                    Log.d(TAG, "on success : Gagal")
                    resultStatus = Result.failure()
                }
            }

            override fun onFailure(
                statusCode: Int,
                headers: Array<out Header>?,
                responseBody: ByteArray?,
                error: Throwable?
            ) {
                Log.d(TAG, "onFailure : Gagal.....")
                //if proses wrong or failed
                showNotif("Gagal", error?.message)
                resultStatus = Result.failure()
            }

        })
        return resultStatus as Result
    }

    private fun showNotif(title : String, description: String?) {
        val notificationManager = applicationContext.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val notification: NotificationCompat.Builder = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notifications_active_black_24dp)
            .setContentTitle(title)
            .setContentText(description)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH)
            notification.setChannelId(CHANNEL_ID)
            notificationManager.createNotificationChannel(channel)
        }
        notificationManager.notify(NOTIF_ID, notification.build())
    }
}