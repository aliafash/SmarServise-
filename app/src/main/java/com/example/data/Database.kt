package com.example.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    // Categories
    @Query("SELECT * FROM categories ORDER BY displayOrder ASC")
    fun getAllCategories(): Flow<List<Category>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: Category)

    @Query("DELETE FROM categories WHERE id = :id")
    suspend fun deleteCategoryById(id: String)

    // Service Providers
    @Query("SELECT * FROM service_providers ORDER BY isPinned DESC, registrationDate DESC")
    fun getAllServiceProviders(): Flow<List<ServiceProvider>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertServiceProvider(provider: ServiceProvider)

    @Query("DELETE FROM service_providers WHERE id = :id")
    suspend fun deleteServiceProviderById(id: String)

    @Query("SELECT * FROM service_providers WHERE id = :id LIMIT 1")
    suspend fun getServiceProviderById(id: String): ServiceProvider?

    // Pending Providers
    @Query("SELECT * FROM pending_providers ORDER BY status ASC")
    fun getAllPendingProviders(): Flow<List<PendingProvider>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPendingProvider(provider: PendingProvider)

    @Query("DELETE FROM pending_providers WHERE id = :id")
    suspend fun deletePendingProviderById(id: String)

    @Query("SELECT * FROM pending_providers WHERE id = :id LIMIT 1")
    suspend fun getPendingProviderById(id: String): PendingProvider?

    // Reviews
    @Query("SELECT * FROM reviews ORDER BY timestamp DESC")
    fun getAllReviews(): Flow<List<Review>>

    @Query("SELECT * FROM reviews WHERE providerId = :providerId ORDER BY timestamp DESC")
    fun getReviewsForProvider(providerId: String): Flow<List<Review>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReview(review: Review)

    // Chat Message
    @Query("SELECT * FROM chats ORDER BY timestamp ASC")
    fun getAllChats(): Flow<List<ChatMessage>>

    @Query("SELECT * FROM chats WHERE providerId = :providerId ORDER BY timestamp ASC")
    fun getChatsForProvider(providerId: String): Flow<List<ChatMessage>>

    @Query("SELECT * FROM chats ORDER BY timestamp DESC LIMIT 10")
    fun getRecentChats(): Flow<List<ChatMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatMessage(message: ChatMessage)

    @Query("DELETE FROM chats WHERE timestamp < :olderThan")
    suspend fun deleteOldChats(olderThan: Long)

    // Reports
    @Query("SELECT * FROM reports ORDER BY timestamp DESC")
    fun getAllReports(): Flow<List<Report>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: Report)

    @Query("DELETE FROM reports WHERE id = :id")
    suspend fun deleteReportById(id: String)

    @Query("DELETE FROM reports")
    suspend fun clearAllReports()

    // Loyalty Points
    @Query("SELECT * FROM loyalty_points ORDER BY timestamp DESC")
    fun getAllLoyaltyPoints(): Flow<List<LoyaltyPoint>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLoyaltyPoint(item: LoyaltyPoint)

    // App Configuration (Single settings record)
    @Query("SELECT * FROM app_config WHERE id = 1 LIMIT 1")
    fun getAppConfigFlow(): Flow<AppConfig?>

    @Query("SELECT * FROM app_config WHERE id = 1 LIMIT 1")
    suspend fun getAppConfigDirect(): AppConfig?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppConfig(config: AppConfig)

    // Ad Banners
    @Query("SELECT * FROM ad_banners ORDER BY createdAt DESC")
    fun getAllBanners(): Flow<List<AdBanner>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBanner(banner: AdBanner)

    @Query("DELETE FROM ad_banners WHERE id = :id")
    suspend fun deleteBannerById(id: String)
}

@Database(
    entities = [
        Category::class,
        ServiceProvider::class,
        PendingProvider::class,
        Review::class,
        ChatMessage::class,
        Report::class,
        LoyaltyPoint::class,
        AppConfig::class,
        AdBanner::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "services_directory_db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
