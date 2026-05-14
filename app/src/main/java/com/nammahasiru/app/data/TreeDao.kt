package com.nammahasiru.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TreeDao {
    @Insert
    suspend fun insert(tree: TreeEntity): Long

    @Update
    suspend fun update(tree: TreeEntity)

    @Query("SELECT * FROM trees ORDER BY plantedAtMillis DESC")
    fun observeAll(): Flow<List<TreeEntity>>

    @Query("SELECT * FROM trees WHERE id = :id")
    fun observeById(id: Long): Flow<TreeEntity?>

    @Query("SELECT * FROM trees WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): TreeEntity?

    @Query("SELECT * FROM trees")
    suspend fun getAllOnce(): List<TreeEntity>
}
