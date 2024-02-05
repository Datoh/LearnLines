package com.datoh.learnlines

import android.app.Application
import com.datoh.learnlines.model.AppContainer
import com.datoh.learnlines.model.AppDataContainer

class LearnLinesApplication : Application() {

    /**
     * AppContainer instance used by the rest of classes to obtain dependencies
     */
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppDataContainer(this)
    }
}
