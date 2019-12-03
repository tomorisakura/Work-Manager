package grevi.msx.workmanager

import android.accessibilityservice.GestureDescription
import android.app.job.JobInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkInfo
import androidx.work.WorkManager
import grevi.msx.workmanager.Worker.myWorker
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnOneTimeTask.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.btnOneTimeTask -> startOneTimeTask()
        }
    }

    private fun startOneTimeTask() {
        textStatus.text = getString(R.string.status)
        val data = Data.Builder()
            .putString(myWorker.EXTRA_CITY, editCity.text.toString())
            .build()
        val oneTimeWorkRequest = OneTimeWorkRequest.Builder(myWorker::class.java)
            .setInputData(data)
            .build()
        WorkManager.getInstance().enqueue(oneTimeWorkRequest)
        WorkManager.getInstance().getWorkInfoByIdLiveData(oneTimeWorkRequest.id)
            .observe(this@MainActivity, object : Observer<WorkInfo> {
                override fun onChanged(t: WorkInfo?) {
                    val status = t?.state?.name
                    textStatus.append("\n" + status)
                }

            })
    }
}
