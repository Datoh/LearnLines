package com.datoh.learnlines.model

import kotlinx.coroutines.flow.Flow

interface PlaysRepository {
    fun getAllPlaysNameStream(): Flow<List<String>>
    suspend fun getPlay(name: String): PlayItem?
    suspend fun insertPlay(play: PlayItem)
    suspend fun deletePlay(play: PlayItem)
}

class OfflinePlaysRepository(private val playDao: PlayDao) : PlaysRepository {
    override fun getAllPlaysNameStream(): Flow<List<String>> = playDao.getAllPlaysName()
    override suspend fun getPlay(name: String): PlayItem? = playDao.getPlay(name)
    override suspend fun insertPlay(play: PlayItem) = playDao.insert(play)
    override suspend fun deletePlay(play: PlayItem) = playDao.delete(play)
}

interface PlayInfoRepository {
    suspend fun getPlayInfo(name: String): PlayInfoItem?
    suspend fun insertPlayInfo(playInfo: PlayInfoItem)
    suspend fun deletePlayInfo(playInfo: PlayInfoItem)
}

class OfflinePlayInfoRepository(private val playInfoDao: PlayInfoDao) : PlayInfoRepository {
    override suspend fun getPlayInfo(name: String) = playInfoDao.getPlayInfo(name)
    override suspend fun insertPlayInfo(playInfo: PlayInfoItem) = playInfoDao.insert(playInfo)
    override suspend fun deletePlayInfo(playInfo: PlayInfoItem) = playInfoDao.delete(playInfo)
}

