package com.datoh.learnlines.model

import android.content.Context

/**
 * App container for Dependency injection.
 */
interface AppContainer {
    val context: Context
    val playsRepository: PlaysRepository
    val playInfoRepository: PlayInfoRepository
}

/**
 * [AppContainer] implementation that provides instance of [PlaysRepository] and [PlayInfoRepository]
 */
class AppDataContainer(override val context: Context) : AppContainer {
    /**
     * Implementation for [PlaysRepository]
     */
    override val playsRepository: PlaysRepository by lazy {
        OfflinePlaysRepository(PlayDatabase.getDatabase(context).playDao())
    }
    /**
     * Implementation for [PlayInfoRepository]
     */
    override val playInfoRepository: PlayInfoRepository by lazy {
        OfflinePlayInfoRepository(PlayDatabase.getDatabase(context).playInfoDao())
    }
}
