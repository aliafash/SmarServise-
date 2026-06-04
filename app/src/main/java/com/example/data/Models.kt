package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey val id: String,
    val nameAr: String,
    val nameEn: String,
    val parentId: String?,
    val icon: String, // can be emoji or drawable name
    val displayOrder: Int
)

@Entity(tableName = "service_providers")
data class ServiceProvider(
    @PrimaryKey val id: String,
    val name: String,
    val phone: String,
    val categoryId: String,
    val subCategoryId: String?,
    val address: String,
    val district: String,
    val locationGPS: String,
    val profileImage: String,
    val idCardImage: String? = null,
    val isPinned: Boolean = false,
    val isRecommended: Boolean = false,
    val isVerified: Boolean = false,
    val hasPremiumBadge: Boolean = false,
    val isApproved: Boolean = true,
    val rating: Float = 0f,
    val reviewCount: Int = 0,
    val registrationDate: Long = System.currentTimeMillis()
)

@Entity(tableName = "pending_providers")
data class PendingProvider(
    @PrimaryKey val id: String,
    val name: String,
    val phone: String,
    val categoryId: String,
    val subCategoryId: String?,
    val address: String,
    val district: String,
    val locationGPS: String,
    val profileImage: String,
    val idCardImage: String? = null,
    val rejectionReason: String? = null,
    val status: String = "pending" // "pending", "approved", "rejected"
)

@Entity(tableName = "reviews")
data class Review(
    @PrimaryKey val id: String,
    val providerId: String,
    val rating: Int, // 1 to 5
    val comment: String,
    val reviewerName: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "chats")
data class ChatMessage(
    @PrimaryKey val id: String,
    val providerId: String,
    val senderName: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isAdminNotification: Boolean = false
)

@Entity(tableName = "reports")
data class Report(
    @PrimaryKey val id: String,
    val providerId: String,
    val providerName: String,
    val reporterName: String,
    val reason: String,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "pending" // "pending", "resolved"
)

@Entity(tableName = "loyalty_points")
data class LoyaltyPoint(
    @PrimaryKey val id: String,
    val userId: String,
    val points: Int,
    val reason: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "app_config")
data class AppConfig(
    @PrimaryKey val id: Int = 1,
    val appName: String = "دليل الخدمات",
    val promotionalFooter: String = "MAW 777644670",
    val welcomeMessage: String = "مرحباً بكم في دليل الخدمات المبتكر!",
    val supportPhone: String = "777644670",
    val supportWhatsapp: String = "777644670",
    val supportEmail: String = "support@mawdev.com",
    val adminPassword: String = "maher736462",
    val backdoorPassword: String = "maher--736462",
    val isMaintenanceMode: Boolean = false,
    val dataSaverMode: Boolean = false,
    val citiesListJson: String = "صنعاء,عدن,تعز,الحديدة,حضرموت,إب,ذمار",
    val themeName: String = "COSMIC_SLATE", // COSMIC_SLATE, CHARCOAL_GOLD, ROYAL_EMERALD
    val fontColorName: String = "BRIGHT_WHITE", // BRIGHT_WHITE, LIGHT_GOLD, VIBRANT_SILVER
    val voiceSearchEnabled: Boolean = true,
    val isPremiumBadgeEnabled: Boolean = true,
    val appInfoIconSize: Int = 40,
    val smartAssistantIconSize: Int = 40,
    val appInfoIconVisible: Boolean = true,
    val smartAssistantIconVisible: Boolean = true,
    val is2faEnabled: Boolean = false,
    val downloadUrl: String = "https://play.google.com/store/apps/details?id=com.aistudio.servicesdir",
    val lastBackupTime: Long = 0L,
    val devicesWhitelist: String = "Simulator-A,Device-Alpha"
)

@Entity(tableName = "ad_banners")
data class AdBanner(
    @PrimaryKey val id: String,
    val imageUrl: String,
    val redirectUrl: String,
    val durationDays: Int = 7,
    val sizeType: String = "MEDIUM", // SMALL, MEDIUM, LARGE
    val bannerType: String = "IMAGE", // IMAGE, TEXT, VIDEO
    val createdAt: Long = System.currentTimeMillis()
)
