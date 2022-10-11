package com.osfans.trime.data.db

import android.content.Context
import androidx.room.Room
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

object CollectionHelper : CoroutineScope by CoroutineScope(SupervisorJob() + Dispatchers.Default) {
    private lateinit var cltDb: Database
    private lateinit var cltDao: DatabaseDao

    fun init(context: Context) {
        cltDb = Room
            .databaseBuilder(context, Database::class.java, "collection.db")
            .addMigrations(Database.MIGRATION_3_4)
            .build()
        cltDao = cltDb.databaseDao()
    }

    suspend fun insert(bean: DatabaseBean) = cltDao.insert(bean)

    suspend fun getAll() = cltDao.getAll()

    suspend fun delete(id: Int) = cltDao.delete(id)

    suspend fun deleteAll() = cltDao.deleteAll()
}
