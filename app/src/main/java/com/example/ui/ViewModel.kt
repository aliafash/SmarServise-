package com.example.ui

import android.app.Activity
import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class AppScreen {
    HOME,
    LOGIN,
    REGISTER_PROFESSIONAL,
    PROVIDER_DETAIL,
    ADMIN_DASHBOARD,
    BACKDOOR_DASHBOARD
}

class AppViewModel(private val repository: AppRepository) : ViewModel() {

    // Observables from repo
    val categories: StateFlow<List<Category>> = repository.categories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val serviceProviders: StateFlow<List<ServiceProvider>> = repository.serviceProviders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingProviders: StateFlow<List<PendingProvider>> = repository.pendingProviders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val reviews: StateFlow<List<Review>> = repository.reviews
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val chats: StateFlow<List<ChatMessage>> = repository.chats
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val reports: StateFlow<List<Report>> = repository.reports
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val loyaltyPoints: StateFlow<List<LoyaltyPoint>> = repository.loyaltyPoints
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val appConfig: StateFlow<AppConfig?> = repository.appConfig
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val adBanners: StateFlow<List<AdBanner>> = repository.adBanners
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val syncStatus: StateFlow<String> = repository.syncStatus

    // Navigation and screen stack
    private val _currentScreen = MutableStateFlow(AppScreen.HOME)
    val currentScreen: StateFlow<AppScreen> = _currentScreen

    private val _selectedProviderId = MutableStateFlow<String?>(null)
    val selectedProviderId: StateFlow<String?> = _selectedProviderId

    private val screenStack = mutableListOf(AppScreen.HOME)

    // Back door states
    private val _backdoorClickCount = MutableStateFlow(0)
    private val backdoorClickCount: StateFlow<Int> = _backdoorClickCount

    // Search and Filters
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _selectedCategory = MutableStateFlow<Category?>(null)
    val selectedCategory: StateFlow<Category?> = _selectedCategory

    private val _selectedCity = MutableStateFlow<String?>(null)
    val selectedCity: StateFlow<String?> = _selectedCity

    private val _searchPhoneQuery = MutableStateFlow("")
    val searchPhoneQuery: StateFlow<String> = _searchPhoneQuery

    private val _searchNameQuery = MutableStateFlow("")
    val searchNameQuery: StateFlow<String> = _searchNameQuery

    private val _isVoiceSearching = MutableStateFlow(false)
    val isVoiceSearching: StateFlow<Boolean> = _isVoiceSearching

    // Current user context (mock)
    val currentUserId = "user_device_736462"
    val currentUserPoints: StateFlow<Int> = loyaltyPoints.map { list ->
        list.filter { it.userId == currentUserId }.sumOf { it.points }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Back Door & Login variables
    val secureBackdoorPwdMock = "maher--736462"
    var isBackdoorUnlocked = MutableStateFlow(false)
    var isAdminLoggedIn = MutableStateFlow(false)

    // Language switcher State (Default Arabic)
    private val _currentLang = MutableStateFlow("AR")
    val currentLang: StateFlow<String> = _currentLang

    // Last backpress timestamp
    private var lastBackPressTime = 0L

    fun changeLanguage(lang: String) {
        _currentLang.value = lang
    }

    // Top Bar Custom Ordering Configuration State
    // String contains comma separated top bar icons keys: "HOME,LOGIN,REGISTER,LANG,REFRESH"
    private val _topBarOrder = MutableStateFlow(listOf("HOME", "LOGIN", "REGISTER", "LANG", "REFRESH"))
    val topBarOrder: StateFlow<List<String>> = _topBarOrder

    fun updateTopBarOrder(newOrder: List<String>) {
        _topBarOrder.value = newOrder
    }

    // Navigation Action
    fun navigateTo(screen: AppScreen, providerId: String? = null) {
        if (providerId != null) {
            _selectedProviderId.value = providerId
        }
        _currentScreen.value = screen
        if (screenStack.lastOrNull() != screen) {
            screenStack.add(screen)
        }
    }

    fun handleBackPress(activity: Activity) {
        if (screenStack.size > 1) {
            screenStack.removeAt(screenStack.size - 1)
            val prevScreen = screenStack.lastOrNull() ?: AppScreen.HOME
            _currentScreen.value = prevScreen
        } else {
            // We are on HOME. Exit on consecutive clicks within 2 seconds
            val now = System.currentTimeMillis()
            if (now - lastBackPressTime < 2000) {
                activity.finish()
            } else {
                lastBackPressTime = now
                Toast.makeText(activity, "اضغط مرة أخرى للخروج من التطبيق", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Secret Portal Activation Handler
    fun registerAppTitleClick() {
        _backdoorClickCount.value = _backdoorClickCount.value + 1
        if (_backdoorClickCount.value >= 5) {
            _backdoorClickCount.value = 0
            // Unlock Door triggers in the view Layer
        }
    }

    fun resetBackdoorCounter() {
        _backdoorClickCount.value = 0
    }

    fun unlockBackdoor(password: String): Boolean {
        if (password == secureBackdoorPwdMock) {
            isBackdoorUnlocked.value = true
            isAdminLoggedIn.value = true
            navigateTo(AppScreen.BACKDOOR_DASHBOARD)
            return true
        }
        return false
    }

    fun performAdminLogin(user: String, pass: String, config: AppConfig?): Boolean {
        val configuredPass = config?.adminPassword ?: "maher736462"
        if (user == "WAM2026" && pass == configuredPass) {
            isAdminLoggedIn.value = true
            navigateTo(AppScreen.ADMIN_DASHBOARD)
            return true
        }
        return false
    }

    fun logout() {
        isAdminLoggedIn.value = false
        isBackdoorUnlocked.value = false
        navigateTo(AppScreen.HOME)
    }

    // Filters resets
    fun setQuery(q: String) { _searchQuery.value = q }
    fun setPhoneQuery(q: String) { _searchPhoneQuery.value = q }
    fun setNameQuery(q: String) { _searchNameQuery.value = q }
    fun selectCategory(cat: Category?) { _selectedCategory.value = cat }
    fun selectCity(city: String?) { _selectedCity.value = city }

    // Start Voice Search Simulation
    fun triggerVoiceRecognition(context: Context) {
        _isVoiceSearching.value = true
        viewModelScope.launch {
            kotlinx.coroutines.delay(2000)
            val keywords = listOf("كهربائي", "طبيب", "معين", "سباك", "صنعاء", "مدرس رياضيات", "سائق", "ماهر")
            val randomKey = keywords.random()
            setQuery(randomKey)
            _isVoiceSearching.value = false
            Toast.makeText(context, "البحث الصوتي الذكي حدد: $randomKey", Toast.LENGTH_SHORT).show()
        }
    }

    // Actions
    fun submitProfessionalRegistration(
        name: String,
        phone: String,
        catId: String,
        address: String,
        district: String,
        gps: String,
        profileImg: String,
        idCardImg: String?,
        context: Context
    ) {
        viewModelScope.launch {
            val pending = PendingProvider(
                id = "pending_" + System.currentTimeMillis(),
                name = name,
                phone = phone,
                categoryId = catId,
                subCategoryId = null,
                address = address,
                district = district,
                locationGPS = gps,
                profileImage = profileImg,
                idCardImage = idCardImg,
                status = "pending"
            )
            repository.submitPendingProvider(pending)
            repository.addLoyaltyPoints(currentUserId, 15, "تقديم طلب انضمام مهني جديد")
            Toast.makeText(context, "تم إرسال طلبك للتدقيق والمراجعة الفورية بنجاح!", Toast.LENGTH_LONG).show()
            navigateTo(AppScreen.HOME)
        }
    }

    fun submitDirectProvider(
        name: String,
        phone: String,
        catId: String,
        address: String,
        district: String,
        profileImg: String
    ) {
        viewModelScope.launch {
            val approved = ServiceProvider(
                id = "prov_" + System.currentTimeMillis(),
                name = name,
                phone = phone,
                categoryId = catId,
                subCategoryId = null,
                address = address,
                district = district,
                locationGPS = "15.3694,44.1910",
                profileImage = profileImg,
                idCardImage = null,
                isApproved = true,
                rating = 4.5f,
                reviewCount = 0
            )
            repository.addServiceProvider(approved)
        }
    }

    fun editAppConfig(config: AppConfig) {
        viewModelScope.launch {
            repository.updateAppConfig(config)
        }
    }

    fun triggerRefresh(context: Context) {
        viewModelScope.launch {
            Toast.makeText(context, "جاري تحديث واسترجاع البيانات اللحظية 🔄", Toast.LENGTH_SHORT).show()
            repository.backupDatabaseToStorage() // simple silent preserve sync
        }
    }

    // Add Review & score loyalty points
    fun addReview(providerId: String, rating: Int, comment: String, name: String) {
        viewModelScope.launch {
            val review = Review(
                id = "rev_" + System.currentTimeMillis(),
                providerId = providerId,
                rating = rating,
                comment = comment,
                reviewerName = name
            )
            repository.submitReview(review)
            repository.addLoyaltyPoints(currentUserId, 10, "تقييم مقدم خدمة ($rating نجوم)")
        }
    }

    // Generate Report
    fun fileReport(providerId: String, providerName: String, reporterName: String, reason: String, context: Context) {
        viewModelScope.launch {
            val rep = Report(
                id = "rep_" + System.currentTimeMillis(),
                providerId = providerId,
                providerName = providerName,
                reporterName = reporterName,
                reason = reason,
                status = "pending"
            )
            repository.submitReport(rep)
            Toast.makeText(context, "تم إرسال البلاغ وسياراجعه المشرفون في الحال", Toast.LENGTH_SHORT).show()
        }
    }

    fun resolveReport(id: String) {
        viewModelScope.launch {
            repository.deleteReport(id)
        }
    }

    // Add Banner
    fun publishBanner(banner: AdBanner) {
        viewModelScope.launch {
            repository.addBanner(banner)
        }
    }

    fun deleteBanner(id: String) {
        viewModelScope.launch {
            repository.deleteBanner(id)
        }
    }

    fun approveRequest(id: String) {
        viewModelScope.launch {
            repository.approvePendingProvider(id)
        }
    }

    fun rejectRequest(id: String, reason: String) {
        viewModelScope.launch {
            repository.rejectPendingProvider(id, reason)
        }
    }

    fun toggleRecommendProvider(provider: ServiceProvider) {
        viewModelScope.launch {
            repository.addServiceProvider(provider.copy(isRecommended = !provider.isRecommended))
        }
    }

    fun togglePinProvider(provider: ServiceProvider) {
        viewModelScope.launch {
            repository.addServiceProvider(provider.copy(isPinned = !provider.isPinned))
        }
    }

    fun toggleVerifyProvider(provider: ServiceProvider) {
        viewModelScope.launch {
            repository.addServiceProvider(provider.copy(isVerified = !provider.isVerified))
        }
    }

    fun togglePremiumSubscription(provider: ServiceProvider) {
        viewModelScope.launch {
            repository.addServiceProvider(provider.copy(hasPremiumBadge = !provider.hasPremiumBadge))
        }
    }

    // Messenger Chats
    fun sendMessage(providerId: String, senderName: String, text: String) {
        viewModelScope.launch {
            val msg = ChatMessage(
                id = "chat_" + System.currentTimeMillis(),
                providerId = providerId,
                senderName = senderName,
                message = text
            )
            repository.sendChatMessage(msg)
        }
    }

    fun triggerDatabaseBackup(context: Context) {
        viewModelScope.launch {
            val res = repository.backupDatabaseToStorage()
            Toast.makeText(context, res, Toast.LENGTH_LONG).show()
        }
    }

    fun triggerCSVReportsExport(context: Context) {
        viewModelScope.launch {
            val res = repository.exportReportsToCSV()
            Toast.makeText(context, res, Toast.LENGTH_LONG).show()
        }
    }

    fun addCategory(category: Category) {
        viewModelScope.launch {
            repository.addCategory(category)
        }
    }

    fun triggerScheduledTasks(context: Context) {
        viewModelScope.launch {
            repository.clearOldChats(System.currentTimeMillis() - 7 * 86400000L) // clear older than 7 days
            Toast.makeText(context, "تمت جدولة وتنظيف المحادثات وسجل الذاكرة المؤقتة بنجاح!", Toast.LENGTH_SHORT).show()
        }
    }
}

class AppViewModelFactory(private val repository: AppRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AppViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AppViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
