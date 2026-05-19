package com.deviantart.artviewer.data.local.room

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.Upsert
import com.deviantart.artviewer.common.StorageLocation


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
    //TODO: need the create to overwrite if there exists a folder with the same ID and username
}



@Database(entities = [Folder::class], version = 1)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun folderDao(): FolderDao
}



/**
 * Tools for converting StorageLocation instances to and from String so
 * it can be stored in the DB.
 */
class Converters {
    @TypeConverter
    fun fromFolderType(value: StorageLocation): String = value.name

    @TypeConverter
    fun toFolderType(value: String): StorageLocation = StorageLocation.valueOf(value)
}
