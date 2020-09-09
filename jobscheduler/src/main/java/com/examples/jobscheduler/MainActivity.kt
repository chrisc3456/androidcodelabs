package com.examples.jobscheduler

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*

private const val JOB_ID: Int = 0

class MainActivity : AppCompatActivity() {

    private var scheduler: JobScheduler? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        seekbar_override_deadline.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (progress > 0) {
                    text_view_override_value.text = "$progress s"
                } else {
                    text_view_override_value.text = "Not Set"
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        }
        )
    }

    /**
     * Builds a background job based on the selected parameters
     * Note - the 'view' parameter is unused but is required to link to the 'onClick' property of button_schedule in the activity layout
     */
    fun scheduleJob(view: View) {
        val selectedNetworkOption = when (radio_group_network.checkedRadioButtonId) {
            radio_button_none.id -> JobInfo.NETWORK_TYPE_NONE
            radio_button_any.id -> JobInfo.NETWORK_TYPE_ANY
            radio_button_wifi.id -> JobInfo.NETWORK_TYPE_UNMETERED
            else -> JobInfo.NETWORK_TYPE_NONE
        }

        val serviceName = ComponentName(packageName, NotificationJobService::class.java.name)
        val builder = JobInfo.Builder(JOB_ID, serviceName)
            .setRequiredNetworkType(selectedNetworkOption)
            .setRequiresDeviceIdle(switch_idle.isChecked)
            .setRequiresCharging(switch_charging.isChecked)

        val seekBarProgress = seekbar_override_deadline.progress
        val isSeekBarSet = seekBarProgress > 0
        if (isSeekBarSet) {
            builder.setOverrideDeadline((seekBarProgress * 1000).toLong())
        }

        scheduler = getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler

        if (selectedNetworkOption != JobInfo.NETWORK_TYPE_NONE || switch_idle.isChecked || switch_charging.isChecked || isSeekBarSet) {
            scheduler?.schedule(builder.build())
            Toast.makeText(this, "Job Scheduled, job will run when the constraints are met", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Cancels all jobs currently associated with the scheduler and notify the user
     */
    fun cancelJobs(view: View) {
        if (scheduler != null) {
            scheduler?.cancelAll()
            scheduler = null
            Toast.makeText(this, "Jobs cancelled", Toast.LENGTH_SHORT).show()
        }
    }
}
