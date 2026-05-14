package com.nammahasiru.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [TreeEntity::class], version = 2, exportSchema = false)
abstract class TreeDatabase : RoomDatabase() {
    abstract fun treeDao(): TreeDao

    companion object {
        fun build(context: Context): TreeDatabase =
            Room.databaseBuilder(context, TreeDatabase::class.java, "namma_hasiru.db")
                .fallbackToDestructiveMigration()
                .build()
    }
}
