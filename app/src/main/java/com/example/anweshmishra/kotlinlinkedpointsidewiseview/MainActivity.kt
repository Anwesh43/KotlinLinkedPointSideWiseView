package com.example.anweshmishra.kotlinlinkedpointsidewiseview

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.example.linkedpointsidewiseview.PointSideWiseView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PointSideWiseView.create(this)

    }
}
