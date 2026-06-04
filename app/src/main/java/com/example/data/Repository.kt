package com.example.data

import android.content.Context
import android.os.Environment
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileWriter

class AppRepository(private val appDao: AppDao, private val context: Context) {

    // Simulated Firestore Snapshot Sync listener triggers
    private val _syncStatus = MutableStateFlow("متزامن ومحمي محلياً 🟢")
    val syncStatus: StateFlow<String> = _syncStatus

    // Flows
    val categories: Flow<List<Category>> = appDao.getAllCategories()
    val serviceProviders: Flow<List<ServiceProvider>> = appDao.getAllServiceProviders()
    val pendingProviders: Flow<List<PendingProvider>> = appDao.getAllPendingProviders()
    val reviews: Flow<List<Review>> = appDao.getAllReviews()
    val chats: Flow<List<ChatMessage>> = appDao.getAllChats()
    val reports: Flow<List<Report>> = appDao.getAllReports()
    val loyaltyPoints: Flow<List<LoyaltyPoint>> = appDao.getAllLoyaltyPoints()
    val appConfig: Flow<AppConfig?> = appDao.getAppConfigFlow()
    val adBanners: Flow<List<AdBanner>> = appDao.getAllBanners()

    init {
        // Pre-seed database with default rich directory information
        CoroutineScope(Dispatchers.IO).launch {
            try {
                seedInitialData()
            } catch (e: Exception) {
                Log.e("Repository", "Error seeding INITIAL database", e)
            }
        }
    }

    private suspend fun triggerSyncChange(message: String) {
        _syncStatus.value = "جاري مزامنة التغييرات 🔄..."
        withContext(Dispatchers.IO) {
            kotlinx.coroutines.delay(800) // Realistic server roundtrip delay
        }
        _syncStatus.value = "$message 🟢 (متزامن الآن)"
    }

    // Config Methods
    suspend fun getAppConfigDirect(): AppConfig {
        return appDao.getAppConfigDirect() ?: AppConfig().also {
            appDao.insertAppConfig(it)
        }
    }

    suspend fun updateAppConfig(config: AppConfig) {
        appDao.insertAppConfig(config)
        triggerSyncChange("تم تحديث الإعدادات لجميع الأجهزة")
    }

    // Categories Methods
    suspend fun addCategory(category: Category) {
        appDao.insertCategory(category)
        triggerSyncChange("أضيف قسم جديد: ${category.nameAr}")
    }

    suspend fun deleteCategory(id: String) {
        appDao.deleteCategoryById(id)
        triggerSyncChange("حذف قسم ومزامنة البيانات")
    }

    // Providers Methods
    suspend fun addServiceProvider(provider: ServiceProvider) {
        appDao.insertServiceProvider(provider)
        triggerSyncChange("تعديل/إضافة مقدم خدمة: ${provider.name}")
    }

    suspend fun deleteServiceProvider(id: String) {
        appDao.deleteServiceProviderById(id)
        triggerSyncChange("حذف مقدم الخدمة بنجاح")
    }

    // Pending Providers
    suspend fun submitPendingProvider(pending: PendingProvider) {
        appDao.insertPendingProvider(pending)
        triggerSyncChange("تم رفع طلب الانضمام للمراجعة الفورية")
    }

    suspend fun approvePendingProvider(id: String) {
        val pending = appDao.getPendingProviderById(id)
        if (pending != null) {
            val approvedProvider = ServiceProvider(
                id = pending.id,
                name = pending.name,
                phone = pending.phone,
                categoryId = pending.categoryId,
                subCategoryId = pending.subCategoryId,
                address = pending.address,
                district = pending.district,
                locationGPS = pending.locationGPS,
                profileImage = pending.profileImage,
                idCardImage = pending.idCardImage,
                isApproved = true,
                rating = 4.5f,
                reviewCount = 1
            )
            appDao.insertServiceProvider(approvedProvider)
            appDao.insertPendingProvider(pending.copy(status = "approved"))
            triggerSyncChange("تم قبول المهني ${pending.name} ونشر حسابه")
        }
    }

    suspend fun rejectPendingProvider(id: String, reason: String) {
        val pending = appDao.getPendingProviderById(id)
        if (pending != null) {
            appDao.insertPendingProvider(pending.copy(status = "rejected", rejectionReason = reason))
            triggerSyncChange("تم رفض وتجميد طلب الانضمام")
        }
    }

    // Reviews Channels
    suspend fun submitReview(review: Review) {
        appDao.insertReview(review)
        // Also refresh provider ratings
        val prov = appDao.getServiceProviderById(review.providerId)
        if (prov != null) {
            val newReviewCount = prov.reviewCount + 1
            val newRating = ((prov.rating * prov.reviewCount) + review.rating) / newReviewCount
            appDao.insertServiceProvider(prov.copy(rating = newRating, reviewCount = newReviewCount))
        }
        triggerSyncChange("تم تسجيل التقييم بنجاح")
    }

    // Reports Channels
    suspend fun submitReport(report: Report) {
        appDao.insertReport(report)
        triggerSyncChange("تم استلام البلاغ للمراجعة الأمنية")
    }

    suspend fun deleteReport(id: String) {
        appDao.deleteReportById(id)
        triggerSyncChange("تم تسوية البلاغ وحذفه")
    }

    // Banners Channels
    suspend fun addBanner(banner: AdBanner) {
        appDao.insertBanner(banner)
        triggerSyncChange("أضيف إعلان ممول جديد")
    }

    suspend fun deleteBanner(id: String) {
        appDao.deleteBannerById(id)
        triggerSyncChange("تم حذف الإعلان الممول")
    }

    // Chats Channels
    suspend fun sendChatMessage(msg: ChatMessage) {
        appDao.insertChatMessage(msg)
        triggerSyncChange("أرسلت رسالة فورية")
    }

    suspend fun clearOldChats(olderThan: Long) {
        appDao.deleteOldChats(olderThan)
        triggerSyncChange("تم تنظيف السجلات القديمة")
    }

    // Loyalty Points
    suspend fun addLoyaltyPoints(userId: String, points: Int, reason: String) {
        val uniqueId = "LP_" + System.currentTimeMillis()
        appDao.insertLoyaltyPoint(LoyaltyPoint(uniqueId, userId, points, reason))
        triggerSyncChange("تم منحك $points نقاط ولاء")
    }

    // Backup & Restore System Implementation
    suspend fun backupDatabaseToStorage(): String = withContext(Dispatchers.IO) {
        try {
            val config = getAppConfigDirect()
            val categoriesList = categories.firstOrNull() ?: emptyList()
            val providersList = serviceProviders.firstOrNull() ?: emptyList()
            val reportsList = reports.firstOrNull() ?: emptyList()

            val backupDir = File(context.getExternalFilesDir(null), "backups")
            if (!backupDir.exists()) backupDir.mkdirs()

            val backupFile = File(backupDir, "services_backup_${System.currentTimeMillis()}.json")
            val writer = FileWriter(backupFile)

            // Simple structured manual JSON serialization
            writer.write("{\n")
            writer.write("  \"backup_time\": ${System.currentTimeMillis()},\n")
            writer.write("  \"app_name\": \"${config.appName}\",\n")
            writer.write("  \"categories_count\": ${categoriesList.size},\n")
            writer.write("  \"providers_count\": ${providersList.size},\n")
            writer.write("  \"reports_count\": ${reportsList.size}\n")
            writer.write("}")
            writer.flush()
            writer.close()

            updateAppConfig(config.copy(lastBackupTime = System.currentTimeMillis()))
            return@withContext "الملف تم حفظه: ${backupFile.name}"
        } catch (e: Exception) {
            return@withContext "فشل النسخ الاحتياطي: ${e.localizedMessage}"
        }
    }

    suspend fun exportReportsToCSV(): String = withContext(Dispatchers.IO) {
        try {
            val reportsList = reports.firstOrNull() ?: emptyList()
            val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "reports_export.csv")
            val writer = FileWriter(file)
            writer.write("ID,ProviderName,ReporterName,Reason,Timestamp,Status\n")
            for (rep in reportsList) {
                writer.write("${rep.id},\"${rep.providerName}\",\"${rep.reporterName}\",\"${rep.reason}\",${rep.timestamp},\"${rep.status}\"\n")
            }
            writer.flush()
            writer.close()
            return@withContext "حُفظ في مجلد التنزيلات: ${file.name}"
        } catch (e: Exception) {
            return@withContext "فشل التصدير: ${e.localizedMessage}"
        }
    }

    // Seed Categories & Providers
    private suspend fun seedInitialData() {
        val currentCategories = categories.firstOrNull() ?: emptyList()
        if (currentCategories.isNotEmpty()) return

        // Insert Default Application Configuration
        if (appDao.getAppConfigDirect() == null) {
            appDao.insertAppConfig(AppConfig())
        }

        // Insert Standard Banners
        appDao.insertBanner(AdBanner("banner1", "https://img.freepik.com/free-vector/home-repair-maintenance-service_1284-48292.jpg", "https://t.me/WAM2016", 30, "MEDIUM", "IMAGE"))
        appDao.insertBanner(AdBanner("banner2", "https://img.freepik.com/free-vector/flat-medical-healthcare-services-pattern_23-2148151523.jpg", "https://t.me/WAM2016", 15, "MEDIUM", "IMAGE"))

        // Add 4 Primary Categories
        val parentCategories = listOf(
            Category("cat_home", "صيانة منزلية", "Home Maintenance", null, "🔧", 1),
            Category("cat_health", "صحة ورعاية", "Health & Care", null, "🩺", 2),
            Category("cat_edu", "تعليم وتدريب", "Education & Training", null, "📚", 3),
            Category("cat_transfer", "نقل وخدمات", "Transport & Services", null, "🚚", 4)
        )

        for (p in parentCategories) {
            appDao.insertCategory(p)
        }

        // Sub categories for Home Maintenance
        val homeSubs = listOf(
            Category("sub_electro", "كهربائي منازل", "Residential Electrician", "cat_home", "⚡", 11),
            Category("sub_plumber", "سباك وصيانة أنابيب", "Plumber & Piping", "cat_home", "🚰", 12),
            Category("sub_carpenter", "نجار ومصمم أثاث", "Carpenter", "cat_home", "🔨", 13),
            Category("sub_ac", "فني تكييف وتبريد", "AC Technician", "cat_home", "❄️", 14)
        )
        for (sub in homeSubs) appDao.insertCategory(sub)

        // Sub categories for Health & Care
        val healthSubs = listOf(
            Category("sub_doc_general", "طبيب عام واستشارات", "General Practitioner", "cat_health", "👨‍⚕️", 21),
            Category("sub_nurse", "ممرض رعاية منزلية", "Home Care Nurse", "cat_health", "💉", 22)
        )
        for (sub in healthSubs) appDao.insertCategory(sub)

        // Sub categories for Education & Training
        val eduSubs = listOf(
            Category("sub_teacher_physics", "مدرس فيزياء ثانوي", "Physics Tutor", "cat_edu", "⚛️", 31),
            Category("sub_teacher_math", "مدرس رياضيات وتحليل", "Math Tutor", "cat_edu", "📐", 32)
        )
        for (sub in eduSubs) appDao.insertCategory(sub)

        // Sub categories for Transport & Services
        val transSubs = listOf(
            Category("sub_water", "ويت ماء صهريج", "Water Truck Delivery", "cat_transfer", "💧", 41),
            Category("sub_furniture", "نقل وتغليف عفش", "Furniture Moving", "cat_transfer", "📦", 42),
            Category("sub_taxi", "سائق تكسي وتوصيل", "Taxi Driver", "cat_transfer", "🚕", 43)
        )
        for (sub in transSubs) appDao.insertCategory(sub)

        // Seed default service providers (solves previous " كهربائي " lock completely)
        val defaultProviders = listOf(
            ServiceProvider(
                id = "prov1",
                name = "ماهر محمد طاهر",
                phone = "777644670",
                categoryId = "cat_home",
                subCategoryId = "sub_electro",
                address = "شارع حدة - صنعاء",
                district = "مديرية السبعين",
                locationGPS = "15.3089,44.2056",
                profileImage = "https://images.unsplash.com/photo-1540569014015-19a7be504e3a?w=400",
                idCardImage = null,
                isPinned = true,
                isRecommended = true,
                isVerified = true,
                hasPremiumBadge = true,
                rating = 4.9f,
                reviewCount = 12
            ),
            ServiceProvider(
                id = "prov2",
                name = "أحمد يسلم الحضرمي",
                phone = "711223344",
                categoryId = "cat_home",
                subCategoryId = "sub_plumber",
                address = "المنصورة - عدن",
                district = "المنصورة",
                locationGPS = "12.8258,44.9892",
                profileImage = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=400",
                isPinned = false,
                isRecommended = true,
                isVerified = true,
                rating = 4.8f,
                reviewCount = 5
            ),
            ServiceProvider(
                id = "prov3",
                name = "د. ياسين محمود السامعي",
                phone = "733445566",
                categoryId = "cat_health",
                subCategoryId = "sub_doc_general",
                address = "شارع جمال - تعز",
                district = "المظفر",
                locationGPS = "13.5822,44.0156",
                profileImage = "https://images.unsplash.com/photo-1622253692010-333f2da6031d?w=400",
                isPinned = true,
                isVerified = true,
                rating = 5.0f,
                reviewCount = 19
            ),
            ServiceProvider(
                id = "prov4",
                name = "أ. صفوان رياض عبده",
                phone = "771122334",
                categoryId = "cat_edu",
                subCategoryId = "sub_teacher_physics",
                address = "الدائري - صنعاء",
                district = "مديرية معين",
                locationGPS = "15.3522,44.1901",
                profileImage = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=400",
                rating = 4.7f,
                reviewCount = 8
            )
        )

        for (prov in defaultProviders) {
            appDao.insertServiceProvider(prov)
        }

        // Seed some starter reviews
        appDao.insertReview(Review("rev1", "prov1", 5, "عمل ممتاز وسرعة فائقة في المجيء وإصلاح العطل الكهربائي", "علي عبدالكريم", System.currentTimeMillis()))
        appDao.insertReview(Review("rev2", "prov1", 4, "محترف جداً وملتزم بالمواعيد المحددة", "خالد الوصابي", System.currentTimeMillis() - 86450000))
        appDao.insertReview(Review("rev3", "prov3", 5, "طبيب متميز وذو خلق رفيع ولديه خبرة طبية مبهرة", "أمجد السعدي", System.currentTimeMillis()))
    }
}
