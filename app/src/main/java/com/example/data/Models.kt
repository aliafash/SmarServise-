package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "categories")
@Serializable
data class Category(
    @PrimaryKey val id: String,
    val nameAr: String,
    val nameEn: String,
    val parentId: String?, // Null if main category, holds parentId if subcategory
    val icon: String,
    val displayOrder: Int = 0
)

@Entity(tableName = "service_providers")
@Serializable
data class ServiceProvider(
    @PrimaryKey val id: String,
    val name: String,
    val phone: String,
    val categoryId: String,
    val subCategoryId: String,
    val rating: Float = 4.5f,
    val address: String,          // e.g. "صنعاء"
    val district: String,         // e.g. "السبعين"
    val locationGPS: String,      // Latitude,Longitude e.g. "15.3694,44.1910"
    val profileImage: String,     // File/URI path
    val idCardImage: String? = null,
    val isPinned: Boolean = false,
    val isRecommended: Boolean = false,
    val isVerified: Boolean = false,
    val hasPremiumBadge: Boolean = false,
    val isBlacklisted: Boolean = false,
    val isApproved: Boolean = true
)

@Entity(tableName = "pending_registrations")
@Serializable
data class PendingRegistration(
    @PrimaryKey val id: String,
    val name: String,
    val phone: String,
    val categoryId: String,
    val subCategoryId: String,
    val address: String,
    val district: String,
    val locationGPS: String,
    val profileImage: String,
    val idCardImage: String? = null,
    val status: String = "pending", // "pending", "approved", "rejected"
    val rejectionReason: String? = null
)

@Entity(tableName = "app_config")
@Serializable
data class AppConfig(
    @PrimaryKey val id: String = "main_config",
    val appName: String = "دليل الخدمات الشامل",
    val welcomeMessage: String = "مرحباً بك في دليل الخدمات اليمني الموثق!",
    val downloadUrl: String = "https://services-app.yemen",
    val isMaintenanceMode: Boolean = false,
    val is2faEnabled: Boolean = false,
    val promotionalFooter: String = "دليل الخدمات © 2026 - تواصل موثوق وآمن لجميع المهن",
    val citiesListJson: String = "صنعاء,عدن,تعز,الحديدة,إب,حضرموت,مأرب",
    
    // UI Visual Theme Configurations (كوزميك سيلفر، ذهبي فاخر، زمردي راقي)
    val colorThemeId: String = "silver", // "silver", "gold", "emerald"
    val fontColor: String = "#FFFFFF",
    val fontSizeOffset: Int = 0, // 0 standard, 2 medium, 4 large
    val fontTypeFace: String = "normal", // "normal", "bold", "monospace"
    
    // Floating Buttons configuration
    val smartAssistantIconVisible: Boolean = true,
    val smartAssistantIconSize: Int = 46,
    val appInfoIconVisible: Boolean = true,
    val appInfoIconSize: Int = 46,
    
    // Visitor / Owner Realtime Chat Button settings
    val adminChatIconVisible: Boolean = true,
    val adminChatIconSize: Int = 46,
    val adminChatBottomOffset: Int = 80, // Horizontal positioning
    val adminChatStartOffset: Int = 20, // Vertical positioning
    
    // Map radius search options
    val maxRadiusLimitKm: Float = 100f,
    val defaultRadiusKm: Float = 10f
)

@Entity(tableName = "reports")
@Serializable
data class Report(
    @PrimaryKey val id: String,
    val providerId: String,
    val providerName: String,
    val reporterPhone: String,
    val reason: String,
    val description: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "ad_banners")
@Serializable
data class AdBanner(
    @PrimaryKey val id: String,
    val imageUrl: String,
    val redirectUrl: String,
    val bannerText: String,
    val isVideo: Boolean = false,
    val bannerType: String = "IMAGE", // "IMAGE", "VIDEO", "TEXT"
    val sizeType: String = "MEDIUM", // "SMALL", "MEDIUM", "LARGE"
    val durationSeconds: Int = 5,
    val isActive: Boolean = true
)

@Entity(tableName = "subscription_requests")
@Serializable
data class SubscriptionRequest(
    @PrimaryKey val id: String,
    val providerId: String,
    val providerName: String,
    val phoneNumber: String,
    val transactionProof: String, // transaction Id or image path
    val status: String = "pending", // "pending", "approved", "rejected"
    val requestType: String = "PREMIUM_BADGE",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "chats")
@Serializable
data class ChatMessage(
    @PrimaryKey val id: String,
    val providerId: String, // Can be "admin" or dynamic professional Id
    val senderName: String,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isFromVisitor: Boolean = true
)

@Entity(tableName = "device_whitelist")
@Serializable
data class WhitelistedDevice(
    @PrimaryKey val deviceId: String,
    val labelName: String,
    val approvedTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "service_requests")
@Serializable
data class ServiceRequestTicket(
    @PrimaryKey val id: String,
    val providerId: String,
    val providerName: String,
    val providerPhone: String,
    val requestedCategory: String,
    val status: String, // "تم الاتصال", "في الانتظار", "مكتمل"
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "system_alerts")
@Serializable
data class SystemAlert(
    @PrimaryKey val id: String,
    val title: String,
    val message: String,
    val type: String, // "REPORT", "SUBSCRIPTION", "UNAUTHORIZED_LOGIN", "REGISTRATION"
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)
