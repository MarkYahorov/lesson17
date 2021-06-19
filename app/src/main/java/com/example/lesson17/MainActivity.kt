package com.example.lesson17

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.lang.StringBuilder

class MainActivity : AppCompatActivity() {

    private val lock = Object()

    private lateinit var thisText: TextView
    private lateinit var startBtn: Button

    private lateinit var firstThread: Thread
    private lateinit var secondThread: Thread
    private lateinit var thirdThread: Thread
    private lateinit var fourthThread: Thread

    private val handler = Handler(Looper.getMainLooper())
    private val stringBuilder = StringBuilder()

    @Volatile
    private var messageList = mutableListOf<String>()

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
            openFourthThread()
        }
    }

    private fun openFirstThread() {
        firstThread = Thread {
            while (isRunning) {
                synchronized(lock) {
                    messageList.forEach {
                        stringBuilder.append(it)
                    }
                }
                handler.post { thisText.append(stringBuilder) }
                Thread.sleep(100)
            }
        }
        firstThread.start()
    }

    private fun openSecondThread() {
        secondThread = Thread {
            var nextPrimeNumber = 2
            while (isRunning) {
                var dividerCount = 0
                for (i in nextPrimeNumber..400) {
                    if (nextPrimeNumber % i == 0) {
                        dividerCount++
                    }
                }
                if (dividerCount < 2) {
                    synchronized(lock) {
                        addMessageToList("SECOND THREAD $nextPrimeNumber")
                        Log.d("key", "$nextPrimeNumber")
                        lock.notify()
                    }
                }
                nextPrimeNumber++
                Thread.sleep(200)
            }
        }
        secondThread.start()
    }

    private fun openThirdThread() {
        thirdThread = Thread {
            var count = 0
            while (count < 10 || count == 10) {
                if (count < 10) {
                    addMessageToList("ФЫВФЫВ${count++}")
                    Thread.sleep(500)
                } else {
                    addMessageToList("asd${count++}")
                    isRunning = false
                    firstThread.join()
                    Log.d("key", "Поток1 завершил работу ")
                    secondThread.join()
                    Log.d("key", "Поток2 завершил работу ")
                    synchronized(lock) {
                        lock.notify()
                    }
                    fourthThread.join()
                    Log.d("key", "Поток4 завершил работу ")
                    handler.post { startBtn.isEnabled = true }
                }
            }
        }
        thirdThread.start()
    }

    private fun openFourthThread() {
        fourthThread = Thread {
            synchronized(lock) {
                while (isRunning) {
                    lock.wait()
                    addMessageToList("YOP")
                }

            }
        }
        fourthThread.start()
    }

    private fun addMessageToList(message: String) {
        synchronized(lock) {
            messageList.add(message)
        }
    }

    override fun onStop() {
        super.onStop()
        startBtn.setOnClickListener(null)
    }
}