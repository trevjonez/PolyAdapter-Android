package com.trevjonez.polyadapter.sample

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.trevjonez.polyadapter.R
import com.trevjonez.polyadapter.databinding.SampleActivityBinding

class SampleActivity : AppCompatActivity() {

  private lateinit var viewBinding: SampleActivityBinding

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    viewBinding = DataBindingUtil.setContentView(this, R.layout.sample_activity)
  }
}