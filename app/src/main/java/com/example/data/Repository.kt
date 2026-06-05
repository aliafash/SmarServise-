package com.example.data

import android.content.Context
import android.os.Environment
import android.widget.Toast
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class AppRepository(private val appDao: AppDao, private val context: Context) {

    val categories: Flow<List<Category>> = appDao.getAllCategories()
    val activeProviders: Flow<List<ServiceProvider>> = appDao.getAllActiveProviders()
    val allProvidersRaw: Flow<List<ServiceProvider>> = appDao.getAllProvidersRaw()
    val pendingRegistrations: Flow<List<PendingRegistration>> = appDao.getPendingRegistrations()
    val appConfig: Flow<AppConfig?> = appDao.getAppConfigFlow()
    val reports: Flow<List<Report>> = appDao.getAllReports()
    val activeBanners: Flow<List<AdBanner>> = appDao.getAllActiveBanners()
    val allBannersRaw: Flow<List<AdBanner>> = appDao.getAllBannersRaw()
    val subscriptionRequests: Flow<List<SubscriptionRequest>> = appDao.getAllSubscriptionRequests()
    val chatsFlow: Flow<List<ChatMessage>> = appDao.getAllChatsFlow()
    val whitelistedDevices: Flow<List<WhitelistedDevice>> = appDao.getWhitelistedDevices()
    val serviceTickets: Flow<List<ServiceRequestTicket>> = appDao.getServiceRequestsHistory()
    val systemAlerts: Flow<List<SystemAlert>> = appDao.getSystemAlertsFlow()
    val allReviews: Flow<List<ServiceProviderReview>> = appDao.getAllReviews()
    val allModerators: Flow<List<Moderator>> = appDao.getAllModerators()

    // Seeds initial data if empty
    suspend fun tryPrepopulateData() {
        val existingConfig = appDao.getAppConfigDirect()
        if (existingConfig == null) {
            // 1. AppConfig
            appDao.insertAppConfig(AppConfig())

            // 2. Categories
            val seedCats = listOf(
                Category("cat_home", "صيانة منزلية", "Home Maintenance", null, "🔧", 1),
                Category("cat_health", "صحة ورعاية", "Health & Care", null, "🩺", 2),
                Category("cat_edu", "تعليم وتدريب", "Education & Training", null, "📚", 3),
                Category("cat_transfer", "نقل وخدمات", "Transport & Services", null, "🚚", 4),

                // Subcategories
                Category("sub_electro", "كهربائي منازل", "Electrician", "cat_home", "⚡", 5),
                Category("sub_plumber", "سباك وصيانة أنابيب", "Plumber", "cat_home", "🚰", 6),
                Category("sub_carpenter", "نجار ومصمم أثاث", "Carpenter", "cat_home", "🔨", 7),
                Category("sub_ac", "فني تكييف وتبريد", "AC Technician", "cat_home", "❄️", 8),

                Category("sub_doc_general", "طبيب عام واستشارات", "General Doctor", "cat_health", "👨‍⚕️", 9),
                Category("sub_nurse", "ممرض رعاية منزلية", "Home Care Nurse", "cat_health", "💉", 10),

                Category("sub_teacher_physics", "مدرس فيزياء ثانوي", "Physics Tutor", "cat_edu", "⚛️", 11),
                Category("sub_teacher_math", "مدرس رياضيات وتحليل", "Math Tutor", "cat_edu", "📐", 12),

                Category("sub_water", "ويت ماء صهريج", "Water Truck", "cat_transfer", "💧", 13),
                Category("sub_furniture", "نقل وتغليف عفش", "Furniture Moving", "cat_transfer", "📦", 14),
                Category("sub_taxi", "سائق تكسي وتوصيل", "Taxi Delivery", "cat_transfer", "🚕", 15)
            )
            for (c in seedCats) appDao.insertCategory(c)

            // 3. Service Providers
            val seedProviders = listOf(
                ServiceProvider(
                    id = "p1",
                    name = "م. يوسف الصنعاني",
                    phone = "777123456",
                    categoryId = "cat_home",
                    subCategoryId = "sub_electro",
                    rating = 4.9f,
                    address = "صنعاء",
                    district = "السبعين",
                    locationGPS = "15.3694,44.1910",
                    profileImage = "https://images.unsplash.com/photo-1540569014015-19a7be504e3a?w=120&auto=format&fit=crop",
                    isPinned = true,
                    isRecommended = true,
                    isVerified = true,
                    hasPremiumBadge = true
                ),
                ServiceProvider(
                    id = "p2",
                    name = "د. عاصم عدنان",
                    phone = "733221100",
                    categoryId = "cat_health",
                    subCategoryId = "sub_doc_general",
                    rating = 4.8f,
                    address = "عدن",
                    district = "كريتر",
                    locationGPS = "12.7855,45.0184",
                    profileImage = "https://images.unsplash.com/photo-1622253692010-333f2da6031d?w=120&auto=format&fit=crop",
                    isPinned = false,
                    isRecommended = true,
                    isVerified = true,
                    hasPremiumBadge = false
                ),
                ServiceProvider(
                    id = "p3",
                    name = "الأستاذ خالد تعز",
                    phone = "711554433",
                    categoryId = "cat_edu",
                    subCategoryId = "sub_teacher_math",
                    rating = 4.6f,
                    address = "تعز",
                    district = "صالة",
                    locationGPS = "13.5790,44.0200",
                    profileImage = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=120&auto=format&fit=crop",
                    isPinned = false,
                    isRecommended = false,
                    isVerified = false,
                    hasPremiumBadge = false
                )
            )
            for (p in seedProviders) appDao.insertProvider(p)

            // 4. Default Ad Banners
            val seedBanners = listOf(
                AdBanner(
                    id = "ad_b1",
                    imageUrl = "https://images.unsplash.com/photo-1621905251189-08b45d6a269e?w=600&auto=format&fit=crop",
                    redirectUrl = "https://wa.me/967777123456",
                    bannerText = "خصم 30% على خدمات التأسيس الكهربائي الشامل لشهر يونيو!",
                    bannerType = "IMAGE",
                    sizeType = "LARGE",
                    durationSeconds = 6,
                    isActive = true
                ),
                AdBanner(
                    id = "ad_b2",
                    imageUrl = "https://images.unsplash.com/photo-1517649763962-0c623066013b?w=600&auto=format&fit=crop",
                    redirectUrl = "https://services-app.yemen",
                    bannerText = "احصل الآن على اشتراك بريميوم لوضع كرتك في صدارة محركات البحث ومضاعفة مكالماتك اليومية!",
                    bannerType = "TEXT",
                    sizeType = "MEDIUM",
                    durationSeconds = 5,
                    isActive = true
                )
            )
            for (b in seedBanners) appDao.insertBanner(b)

            // 5. Default Whitelisted Device (This device sandbox)
            appDao.insertWhitelistedDevice(WhitelistedDevice("sandbox_dev_id", "محاكاة جهاز المطور"))

            // 6. Default Moderators
            val seedMods = listOf(
                Moderator("mod1", "123456", canEditCategories = true, canDeleteProviders = false, canModifyProviders = true, isActive = true),
                Moderator("mod_all", "777777", canEditCategories = true, canDeleteProviders = true, canModifyProviders = true, isActive = true)
            )
            for (m in seedMods) appDao.insertModerator(m)

            // 7. Default Professional Reviews
            val seedReviews = listOf(
                ServiceProviderReview("rev_1", "p1", "ماجد أحمد", 5f, "ممتاز جداً وسريع في العمل وأنصح به!"),
                ServiceProviderReview("rev_2", "p1", "سالم اليماني", 4.5f, "شغل نظيف ومرتب، تمنياتي له بالتوفيق."),
                ServiceProviderReview("rev_3", "p2", "د. ريم", 5f, "تشخيص ممتاز وطبيب خلوق جداً")
            )
            for (r in seedReviews) appDao.insertReview(r)
        }
    }

    // Reviews management helper
    suspend fun addReview(review: ServiceProviderReview) = appDao.insertReview(review)
    suspend fun deleteReview(id: String) = appDao.deleteReviewById(id)

    // Moderators permissions editing helpers
    suspend fun addModerator(mod: Moderator) = appDao.insertModerator(mod)
    suspend fun deleteModerator(username: String) = appDao.deleteModeratorByUsername(username)

    // Category CRUD
    suspend fun addCategory(category: Category) = appDao.insertCategory(category)
    suspend fun deleteCategory(id: String) = appDao.deleteCategoryById(id)

    // Provider Action Management
    suspend fun addProvider(provider: ServiceProvider) = appDao.insertProvider(provider)
    suspend fun deleteProvider(id: String) = appDao.deleteProviderById(id)
    suspend fun toggleVerification(id: String, isVerified: Boolean) = appDao.setProviderVerification(id, isVerified)
    suspend fun togglePremium(id: String, isPremium: Boolean) = appDao.setProviderPremium(id, if (isPremium) 1 else 0)
    suspend fun toggleBlacklist(id: String, isBanned: Boolean) = appDao.setProviderBlacklist(id, isBanned)

    // Registration Flow
    suspend fun submitProfessionalRegistration(pending: PendingRegistration) {
        appDao.insertPendingRegistration(pending)
        // Log Alert
        appDao.insertSystemAlert(
            SystemAlert(
                id = "alert_reg_" + System.currentTimeMillis(),
                title = "📌 طلب تسجيل كادر جديد",
                message = "قدم المهني: ${pending.name} (${pending.address}) طلباً للانضمام إلى الدليل.",
                type = "REGISTRATION"
            )
        )
    }

    suspend fun approveRegistration(id: String, provider: ServiceProvider) {
        appDao.insertProvider(provider)
        appDao.deletePendingRegistrationById(id)
    }

    suspend fun rejectRegistration(id: String) {
        appDao.deletePendingRegistrationById(id)
    }

    // Reports CRUD
    suspend fun submitReport(report: Report) {
        appDao.insertReport(report)
        // Log Alert
        appDao.insertSystemAlert(
            SystemAlert(
                id = "alert_rep_" + System.currentTimeMillis(),
                title = "🚨 بلاغ شكوى جديد",
                message = "تم الإبلاغ عن مقدم الخدمة: ${report.providerName} بسبب: ${report.reason}.",
                type = "REPORT"
            )
        )
    }
    suspend fun deleteReport(id: String) = appDao.deleteReportById(id)

    // Subscriptions CRUD
    suspend fun submitSubscription(request: SubscriptionRequest) {
        appDao.insertSubscriptionRequest(request)
        // Log Alert
        appDao.insertSystemAlert(
            SystemAlert(
                id = "alert_sub_" + System.currentTimeMillis(),
                title = "📅 طلب اشتراك مميز شهري",
                message = "طلب مقدم الخدمة: ${request.providerName} ترقية حسابه للشارة المميزة.",
                type = "SUBSCRIPTION"
            )
        )
    }
    suspend fun deleteSubscription(id: String) = appDao.deleteSubscriptionRequestById(id)

    // Ad Banners CRUD
    suspend fun addBanner(banner: AdBanner) = appDao.insertBanner(banner)
    suspend fun deleteBanner(id: String) = appDao.deleteBannerById(id)

    // Config updating
    suspend fun updateAppConfig(config: AppConfig) = appDao.insertAppConfig(config)

    // Chats
    suspend fun sendChatMessage(msg: ChatMessage) = appDao.insertChatMessage(msg)
    suspend fun clearOldChats(olderThan: Long) = appDao.clearOldChats(olderThan)
    suspend fun clearAllChats() = appDao.clearAllChats()

    // System alerts alerts flow
    suspend fun markAllAlertsAsRead() = appDao.markAllAlertsAsRead()
    suspend fun clearSystemAlerts() = appDao.clearAllSystemAlerts()
    suspend fun registerAlert(alert: SystemAlert) = appDao.insertSystemAlert(alert)

    // Authorized device Whitelist
    suspend fun addWhitelistedDevice(device: WhitelistedDevice) = appDao.insertWhitelistedDevice(device)
    suspend fun deleteWhitelistedDevice(id: String) = appDao.deleteWhitelistedDeviceById(id)

    // User historic tickets for User Dashboard
    suspend fun addServiceRequestTicket(ticket: ServiceRequestTicket) = appDao.insertServiceRequestTicket(ticket)
    suspend fun deleteServiceRequestTicket(id: String) = appDao.deleteServiceRequestTicketById(id)

    // Database Backup Systems (Import / Export SQLite File)
    fun exportDatabaseToStorage(folderChoice: String): File? {
        try {
            val dbFile = context.getDatabasePath("yemen_services_system_db")
            if (!dbFile.exists()) return null

            // Determine target directory
            val targetDir = when (folderChoice) {
                "SD" -> {
                    // SD Card / External storage direction
                    context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) ?: context.filesDir
                }
                "INTERNAL" -> {
                    // Internal memory folder
                    context.filesDir
                }
                else -> {
                    context.cacheDir
                }
            }

            val safetyDir = File(targetDir, "Backups")
            if (!safetyDir.exists()) safetyDir.mkdirs()

            val targetFile = File(safetyDir, "yemen_services_backup_${System.currentTimeMillis()}.db")

            FileInputStream(dbFile).use { input ->
                FileOutputStream(targetFile).use { output ->
                    input.copyTo(output)
                }
            }
            return targetFile
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    fun restoreDatabaseFromBackup(backupFile: File): Boolean {
        try {
            val dbFile = context.getDatabasePath("yemen_services_system_db")
            if (!backupFile.exists()) return false

            // Close existing database instance before copying
            AppDatabase.getDatabase(context).close()

            FileInputStream(backupFile).use { input ->
                FileOutputStream(dbFile).use { output ->
                    input.copyTo(output)
                }
            }
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }
}
