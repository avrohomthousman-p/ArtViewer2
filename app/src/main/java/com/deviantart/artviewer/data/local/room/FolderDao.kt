package com.deviantart.artviewer.data.local.room

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.Upsert


@Dao
interface FolderDao {
    @Query("SELECT * FROM folders ORDER BY displayName")
    suspend fun getAllFolders(): List<Folder>

    @Query("SELECT * FROM folders WHERE localId = :localId")
    suspend fun getFolder(localId: Int) : Folder

    @Delete
    suspend fun deleteFolder(folder: Folder)

    @Upsert
    suspend fun updateOrCreateFolder(folder: Folder)
}



@Database(entities = [Folder::class], version = 1)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun folderDao(): FolderDao
}
