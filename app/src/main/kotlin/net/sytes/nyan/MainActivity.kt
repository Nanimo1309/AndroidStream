package net.sytes.nyan

import androidx.core.app.ActivityCompat
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton

import android.os.Bundle
import android.content.pm.PackageManager
import android.view.ContextThemeWrapper

import android.media.MediaRecorder
import android.media.AudioRecord
import android.media.AudioFormat

import java.net.Socket

import net.sytes.nyan.databinding.MainActivityBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: MainActivityBinding
    private lateinit var button: AppCompatButton
    private var sendingThread: Thread? = null
    private var isRecording: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = MainActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        button = binding.streamButton
        button.setOnClickListener { _ -> stream() }
    }

    private fun checkPermission(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
            return true

        button.text = getString(R.string.streamButtonError)
        button.background =
            AppCompatButton(ContextThemeWrapper(this, R.style.AndroidTest_button_error), null, 0).background

        ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.RECORD_AUDIO), 0)

        return false
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            button.text = getString(R.string.streamButton)
            button.background =
                AppCompatButton(ContextThemeWrapper(this, R.style.AndroidTest_button), null, 0).background
        }
    }

    private fun stream() {
        if (checkPermission()) {
            if (!isRecording) {
                sendingThread = Thread {
                    isRecording = true

                    try {
                        val socket = Socket("192.168.1.254", 6969)

                        runOnUiThread {
                            button.text = getString(R.string.streamButtonStream)
                        }

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

                        while (isRecording) {
                            val read = recorder.read(buffer, 0, buffer.size)
                            if (read > 0)
                                outputStream.write(buffer, 0, read)
                        }

                        outputStream.close()
                        socket.close()
                        recorder.stop()
                        recorder.release()

                        runOnUiThread {
                            button.text = getString(R.string.streamButton)
                        }
                    } catch (e: Exception) {
                        runOnUiThread {
                            button.text = getString(R.string.streamButtonCannotConnect)
                        }
                        isRecording = false
                    }
                }

                sendingThread!!.start()
            } else {
                isRecording = false
                sendingThread!!.join()
            }
        }
    }
}
