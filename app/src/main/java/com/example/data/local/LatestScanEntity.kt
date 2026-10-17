package com.example.data.local

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "latest_scan")
data class LatestScanEntity(
    @PrimaryKey val id: Int = 1,
    val timestamp: Long,
    val totalApps: Int,
    val criticalCount: Int,
    val highCount: Int,
    val mediumCount: Int,
    val lowCount: Int,
    val riskScore: Int,
    val riskSummary: String,
    val reportJson: String
)

@Dao
interface LatestScanDao {
    @Query("SELECT * FROM latest_scan WHERE id = 1 LIMIT 1")
    fun getLatestScan(): Flow<LatestScanEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveLatestScan(scan: LatestScanEntity)

    @Query("DELETE FROM latest_scan")
    suspend fun clearScan()
}
