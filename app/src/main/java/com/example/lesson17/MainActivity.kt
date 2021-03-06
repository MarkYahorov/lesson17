package com.example.lesson17

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.lang.StringBuilder
import java.util.*

class MainActivity : AppCompatActivity() {

    val lock = Object()

    private lateinit var thisText: TextView
    private lateinit var startBtn: Button

    private lateinit var firstThread: Thread
    private lateinit var secondThread: Thread
    private lateinit var thirdThread: Thread
    private lateinit var fourthThread: Thread
    private lateinit var fiveThread: Thread

    private val handler = Handler(Looper.getMainLooper())
    private val stringBuilder = StringBuilder()

    @Volatile
    private var intList = mutableListOf<String>()

    @Volatile
    var isRunning = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initAll()
    }

    private fun initAll() {
        thisText = findViewById(R.id.all_text)
        startBtn = findViewById(R.id.start_thread_btn)
    }

    override fun onStart() {
        super.onStart()
        setListener()
    }

    private fun setListener() {
        startBtn.setOnClickListener {
            startBtn.isEnabled = false
            isRunning = true
            openSecondThread()
            openFirstThread()
            openThirdThread()
        }
    }

    private fun openFirstThread() {
        firstThread = Thread {
            while (isRunning) {
                synchronized(lock) {
                    intList.forEach {
                        stringBuilder.append(it)
                        Log.d("koy", "first $it")
                    }
                    Thread.sleep(200)
                }
                handler.postDelayed(object : Runnable {
                    override fun run() {
                        thisText.text = stringBuilder.toString()
                    }

                }, 100)
            }
        }
        firstThread.start()

    }

    private fun openSecondThread() {
        secondThread = Thread {
            while (isRunning) {
                for (i in 0..20) {
                    appendMessage("$i")
                    Log.d("koy", "second $i")
                    Thread.sleep(500)
                }
            }
        }
        secondThread.start()
    }

    private fun openThirdThread() {
        thirdThread = Thread {
            var count = 0
            while (count < 10 || count == 10) {
                if (count < 10) {
                    appendMessage("${count++}")
                    Thread.sleep(500)
                } else {
                    appendMessage("${count++}")
                    isRunning = false
                    firstThread.join()
                    Log.d("key", "??????????1 ???????????????? ???????????? ")
                    secondThread.join()
                    Log.d("key", "??????????2 ???????????????? ???????????? ")
                    handler.post(object : Runnable {
                        override fun run() {
                            startBtn.isEnabled = true
                        }
                    }
                    )
                }
            }
        }
        thirdThread.start()
    }

    private fun openFourthThread() {
        fourthThread = Thread{
            appendMessage("YOP!")
            lock.wait()
        }
    }

    private fun appendMessage(message: String) {
        synchronized(lock) {
            intList.add(message)
        }
    }

    override fun onStop() {
        super.onStop()
        startBtn.setOnClickListener(null)
    }
}