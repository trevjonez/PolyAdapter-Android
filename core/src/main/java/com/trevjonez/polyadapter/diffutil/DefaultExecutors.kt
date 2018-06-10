package com.trevjonez.polyadapter.diffutil

import android.os.Handler
import android.os.Looper
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicInteger

internal object MainThread : Executor {
  private val handler = Handler(Looper.getMainLooper())

  override fun execute(command: Runnable) {
    handler.post(command)
  }
}

internal object BackgroundPool : Executor {
  private val threadCount = AtomicInteger()
  private val executor = ThreadPoolExecutor(1, 4, 60L, TimeUnit.SECONDS,
      SynchronousQueue(), ThreadFactory { runnable ->
    Thread(runnable, "PolyAdapterBgPool-${threadCount.getAndIncrement()}")
  })

  override fun execute(command: Runnable) = executor.execute(command)
}