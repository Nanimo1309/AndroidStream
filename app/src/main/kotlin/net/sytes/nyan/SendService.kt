package net.sytes.nyan

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.IBinder
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import java.net.Socket

class SendService: Service()
{
    private var stopRecording: Boolean = false
    private var sendingThread: Thread? = null

    companion object {
        private var enabled: Boolean = false

        fun enabled(): Boolean
        {
            return enabled
        }
    }

    @SuppressLint("MissingPermission")
    override fun onCreate()
    {
        super.onCreate()

        enabled = true

        getSystemService(NotificationManager::class.java)
            .createNotificationChannel(NotificationChannel("SendService", "SendService", NotificationManager.IMPORTANCE_DEFAULT))

        startForeground(1, Notification.Builder(this, "SendService")
            .setContentTitle("Android Stream")
            .setSmallIcon(R.mipmap.app_icon)
            .build())

        sendingThread = Thread{
            try {
                val socket = Socket("192.168.1.254", 6969)

                val channelMask = AudioFormat.CHANNEL_IN_MONO
                        val audioFormat = AudioFormat.ENCODING_PCM_16BIT
                        val sampleRate = 32000
                        val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelMask, audioFormat)

                val recorder = AudioRecord.Builder()
                            .setAudioSource(MediaRecorder.AudioSource.MIC)
                            .setAudioFormat(
                                AudioFormat.Builder()
                                    .setSampleRate(sampleRate)
                                    .setChannelMask(channelMask)
                                    .setEncoding(audioFormat)
                                    .build()
                            )
                            .setBufferSizeInBytes(bufferSize)
                            .build()

                recorder.startRecording()

                val outputStream = socket.getOutputStream()
                        val buffer = ByteArray(bufferSize)

                LocalBroadcastManager.getInstance(this).sendBroadcast(Intent("StartStream"))

                        while (!stopRecording) {
                            val read = recorder.read(buffer, 0, buffer.size)
                            if (read > 0)
                                outputStream.write(buffer, 0, read)
                        }

                outputStream.close()
                        socket.close()
                        recorder.stop()
                        recorder.release()

                LocalBroadcastManager.getInstance(this).sendBroadcast(Intent("StopStream"))
            }
            catch(e: Exception)
            {
                LocalBroadcastManager.getInstance(this).sendBroadcast(Intent("ConnectionError"))
                stopSelf()
            }
        }

        sendingThread!!.start()
    }

    override fun onDestroy() {
        super.onDestroy()

        stopRecording = true
        sendingThread!!.join()

        enabled = false
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }
}