package com.example.data

import android.content.Context
import androidx.room.*
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
    @Query("SELECT * FROM service_providers WHERE isBlacklisted = 0")
    fun getAllActiveProviders(): Flow<List<ServiceProvider>>

    @Query("SELECT * FROM service_providers")
    fun getAllProvidersRaw(): Flow<List<ServiceProvider>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProvider(provider: ServiceProvider)

    @Query("UPDATE service_providers SET isVerified = :isVerified WHERE id = :id")
    suspend fun setProviderVerification(id: String, isVerified: Boolean)

    @Query("UPDATE service_providers SET hasPremiumBadge = :hasPremium, isPinned = :hasPremium WHERE id = :id")
    suspend fun setProviderPremium(id: String, hasPremium: Int)

    @Query("UPDATE service_providers SET isBlacklisted = :isBanned WHERE id = :id")
    suspend fun setProviderBlacklist(id: String, isBanned: Boolean)

    @Query("DELETE FROM service_providers WHERE id = :id")
    suspend fun deleteProviderById(id: String)

    // Pending registrations
    @Query("SELECT * FROM pending_registrations ORDER BY id DESC")
    fun getPendingRegistrations(): Flow<List<PendingRegistration>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPendingRegistration(pending: PendingRegistration)

    @Query("DELETE FROM pending_registrations WHERE id = :id")
    suspend fun deletePendingRegistrationById(id: String)

    // Configs
    @Query("SELECT * FROM app_config LIMIT 1")
    fun getAppConfigFlow(): Flow<AppConfig?>

    @Query("SELECT * FROM app_config LIMIT 1")
    suspend fun getAppConfigDirect(): AppConfig?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppConfig(config: AppConfig)

    // Reports
    @Query("SELECT * FROM reports ORDER BY timestamp DESC")
    fun getAllReports(): Flow<List<Report>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: Report)

    @Query("DELETE FROM reports WHERE id = :id")
    suspend fun deleteReportById(id: String)

    // Ad Banners
    @Query("SELECT * FROM ad_banners WHERE isActive = 1 ORDER BY id DESC")
    fun getAllActiveBanners(): Flow<List<AdBanner>>

    @Query("SELECT * FROM ad_banners ORDER BY id DESC")
    fun getAllBannersRaw(): Flow<List<AdBanner>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBanner(banner: AdBanner)

    @Query("DELETE FROM ad_banners WHERE id = :id")
    suspend fun deleteBannerById(id: String)

    // Subscriptions
    @Query("SELECT * FROM subscription_requests ORDER BY timestamp DESC")
    fun getAllSubscriptionRequests(): Flow<List<SubscriptionRequest>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubscriptionRequest(request: SubscriptionRequest)

    @Query("DELETE FROM subscription_requests WHERE id = :id")
    suspend fun deleteSubscriptionRequestById(id: String)

    // Direct Synchronized Chats
    @Query("SELECT * FROM chats ORDER BY timestamp ASC")
    fun getAllChatsFlow(): Flow<List<ChatMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatMessage(msg: ChatMessage)

    @Query("DELETE FROM chats WHERE timestamp < :olderThan")
    suspend fun clearOldChats(olderThan: Long)

    @Query("DELETE FROM chats")
    suspend fun clearAllChats()

    // Whitelisted Devices
    @Query("SELECT * FROM device_whitelist ORDER BY approvedTimestamp DESC")
    fun getWhitelistedDevices(): Flow<List<WhitelistedDevice>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWhitelistedDevice(device: WhitelistedDevice)

    @Query("DELETE FROM device_whitelist WHERE deviceId = :deviceId")
    suspend fun deleteWhitelistedDeviceById(deviceId: String)

    // Service requests history for User Dashboard
    @Query("SELECT * FROM service_requests ORDER BY timestamp DESC")
    fun getServiceRequestsHistory(): Flow<List<ServiceRequestTicket>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertServiceRequestTicket(ticket: ServiceRequestTicket)

    @Query("DELETE FROM service_requests WHERE id = :id")
    suspend fun deleteServiceRequestTicketById(id: String)

    // Real-time system alerts
    @Query("SELECT * FROM system_alerts ORDER BY timestamp DESC")
    fun getSystemAlertsFlow(): Flow<List<SystemAlert>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSystemAlert(alert: SystemAlert)

    @Query("UPDATE system_alerts SET isRead = 1")
    suspend fun markAllAlertsAsRead()

    @Query("DELETE FROM system_alerts")
    suspend fun clearAllSystemAlerts()

    // Service Provider Reviews queries
    @Query("SELECT * FROM provider_reviews ORDER BY timestamp DESC")
    fun getAllReviews(): Flow<List<ServiceProviderReview>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReview(review: ServiceProviderReview)

    @Query("DELETE FROM provider_reviews WHERE id = :id")
    suspend fun deleteReviewById(id: String)

    // Moderators queries
    @Query("SELECT * FROM moderators ORDER BY username ASC")
    fun getAllModerators(): Flow<List<Moderator>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertModerator(mod: Moderator)

    @Query("DELETE FROM moderators WHERE username = :username")
    suspend fun deleteModeratorByUsername(username: String)
}

@Database(
    entities = [
        Category::class,
        ServiceProvider::class,
        PendingRegistration::class,
        AppConfig::class,
        Report::class,
        AdBanner::class,
        SubscriptionRequest::class,
        ChatMessage::class,
        WhitelistedDevice::class,
        ServiceRequestTicket::class,
        SystemAlert::class,
        ServiceProviderReview::class,
        Moderator::class
    ],
    version = 4,
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
                    "yemen_services_system_db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
