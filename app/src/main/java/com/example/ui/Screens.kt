package com.example.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.*
import com.example.ui.theme.ThemeManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(viewModel: AppViewModel) {
    val context = LocalContext.current
    val currentScreen by viewModel.currentScreen.collectAsState()
    val config by viewModel.appConfig.collectAsState()
    
    // Intercept native back presses
    BackHandler {
        viewModel.handleBackPress(context)
    }

    // Setup persistent login preferences on first run
    LaunchedEffect(Unit) {
        viewModel.loadRememberedPreferences(context)
    }

    val customColorScheme = ThemeManager.getColorScheme(config)

    MaterialTheme(
        colorScheme = customColorScheme
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(customColorScheme.background)
        ) {
            // Check Maintenance Mode
            if (config?.isMaintenanceMode == true && !viewModel.isAdminLoggedIn.collectAsState().value && !viewModel.isBackdoorUnlocked.collectAsState().value) {
                MaintenanceBlockScreen(config = config!!) {
                    // Admin quick backdoor door access from maintenance page
                    var passText by remember { mutableStateFlow("") }
                    var showDialog by remember { mutableStateOf(false) }
                    
                    Button(
                        onClick = { showDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("الولوج المباشر للمطورين", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                    
                    if (showDialog) {
                        AlertDialog(
                            onDismissRequest = { showDialog = false },
                            title = { Text("رمز التخطي للبوابة الخلفية") },
                            text = {
                                OutlinedTextField(
                                    value = passText,
                                    onValueChange = { passText = it },
                                    label = { Text("أدخل رمز المالك") },
                                    visualTransformation = PasswordVisualTransformation(),
                                    textStyle = TextStyle(color = Color.White, fontWeight = FontWeight.Bold),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = Color.Gray
                                    )
                                )
                            },
                            confirmButton = {
                                TextButton(onClick = {
                                    if (viewModel.unlockBackdoor(passText, context)) {
                                        showDialog = false
                                    } else {
                                        Toast.makeText(context, "الرمز خاطئ!", Toast.LENGTH_SHORT).show()
                                    }
                                }) {
                                    Text("تحقق")
                                }
                            }
                        )
                    }
                }
            } else {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    when (currentScreen) {
                        AppScreen.HOME -> ServiceHomeScreen(viewModel)
                        AppScreen.LOGIN -> LoginScreen(viewModel)
                        AppScreen.REGISTER_PROFESSIONAL -> RegisterProfessionalFormScreen(viewModel)
                        AppScreen.PROVIDER_DETAIL -> ProviderDetailScreen(viewModel)
                        AppScreen.ADMIN_DASHBOARD -> AdminDashboardScreen(viewModel)
                        AppScreen.BACKDOOR_DASHBOARD -> BackdoorDashboardScreen(viewModel)
                    }
                }
                
                // Overlay float buttons
                if (currentScreen == AppScreen.HOME) {
                    FloatingWidgetsEngine(viewModel)
                }
            }
        }
    }
}

// 1. Maintenance Screen
@Composable
fun MaintenanceBlockScreen(config: AppConfig, adminOverrideButton: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .background(Color(0xFF0B132B)),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Construction,
            contentDescription = "صيانة",
            tint = Color(0xFFD4AF37),
            modifier = Modifier.size(96.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "وضع الصيانة نشط حالياً",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "نقوم ببعض التحديثات الهامة لضمان أفضل جودة وموثوقية في تقديم الخدمات. سنعود للعمل قريباً جداً!",
            fontSize = 15.sp,
            color = Color.LightGray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(40.dp))
        adminOverrideButton()
    }
}

// 2. Main Service Home Screen Layout
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ServiceHomeScreen(viewModel: AppViewModel) {
    val context = LocalContext.current
    val config by viewModel.appConfig.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val providers by viewModel.serviceProviders.collectAsState()
    val banners by viewModel.adBanners.collectAsState()

    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchPhoneQuery by viewModel.searchPhoneQuery.collectAsState()
    val searchNameQuery by viewModel.searchNameQuery.collectAsState()
    val selectedCat by viewModel.selectedCategory.collectAsState()
    val selectedCity by viewModel.selectedCity.collectAsState()

    val isRadiusEnabled by viewModel.isRadiusFilterEnabled.collectAsState()
    val radiusKm by viewModel.radiusSearchKm.collectAsState()

    var showAdvancedSearch by remember { mutableStateOf(false) }
    var currentBannerIndex by remember { mutableIntStateOf(0) }
    var activeTabUserDashboard by remember { mutableStateOf(false) } // Floating pane for user history dashboard

    // Auto-cycling ad banner simulator
    LaunchedEffect(banners) {
        if (banners.isNotEmpty()) {
            while (true) {
                kotlinx.coroutines.delay((banners.getOrNull(currentBannerIndex)?.durationSeconds ?: 5) * 1000L)
                currentBannerIndex = (currentBannerIndex + 1) % banners.size
            }
        }
    }

    // Dynamic filtering execution
    val filteredProviders = remember(providers, searchQuery, searchPhoneQuery, searchNameQuery, selectedCat, selectedCity, isRadiusEnabled, radiusKm) {
        providers.filter { p ->
            val matchQuery = searchQuery.isBlank() || p.name.contains(searchQuery, true) || p.address.contains(searchQuery, true) || p.district.contains(searchQuery, true)
            val matchPhone = searchPhoneQuery.isBlank() || p.phone.contains(searchPhoneQuery)
            val matchName = searchNameQuery.isBlank() || p.name.contains(searchNameQuery, true)
            
            // Subcategory / Category match
            val matchCat = selectedCat == null || p.categoryId == selectedCat.id || p.subCategoryId == selectedCat.id
            val matchCity = selectedCity == null || p.address == selectedCity

            // GPS Distance search
            val matchRadius = !isRadiusEnabled || viewModel.isWithinSearchRadius(p.locationGPS, viewModel.userGPSCoordinates.value, radiusKm)

            matchQuery && matchPhone && matchName && matchCat && matchCity && matchRadius && p.isApproved && !p.isBlacklisted
        }.sortedWith(compareByDescending<ServiceProvider> { it.hasPremiumBadge }.thenByDescending { it.isPinned })
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .statusBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Quick Action Backdoor (Passcode overlay triggers if user double clicks application title)
                    Row(
                        modifier = Modifier.clickable {
                            viewModel.registerAppTitleClick()
                            Toast.makeText(context, "نقرة سرية للمالك", Toast.LENGTH_SHORT).show()
                        },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.VerifiedUser,
                            contentDescription = "لوغو",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = config?.appName ?: "دليل الخدمات",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Top Action bar
                    Row {
                        IconButton(onClick = { activeTabUserDashboard = !activeTabUserDashboard }) {
                            Icon(
                                imageVector = Icons.Outlined.History,
                                contentDescription = "طلباتي",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        
                        IconButton(onClick = { viewModel.navigateTo(AppScreen.LOGIN) }) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "الإدارة",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        },
        bottomBar = {
            // Slogan signature
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 4.dp
            ) {
                Text(
                    text = config?.promotionalFooter ?: "دليل الخدمات اليمني الشامل - تواصل مباشر",
                    fontSize = 11.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp)
            ) {
                // Promotional Ads banners carousel
                if (banners.isNotEmpty()) {
                    item {
                        val banner = banners.getOrNull(currentBannerIndex) ?: banners[0]
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(if (banner.sizeType == "LARGE") 160.dp else if (banner.sizeType == "SMALL") 90.dp else 125.dp)
                                .padding(vertical = 8.dp)
                                .clickable {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(banner.redirectUrl))
                                    context.startActivity(intent)
                                },
                            shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.cardElevation(6.dp)
                        ) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                AsyncImage(
                                    model = banner.imageUrl,
                                    contentDescription = "إعلان",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                                // Dark shade overlay banner text
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black))),
                                    contentAlignment = Alignment.BottomStart
                                ) {
                                    Text(
                                        text = banner.bannerText,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = if (banner.sizeType == "LARGE") 15.sp else 13.sp,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.padding(12.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Advanced search triggers with Radius Range Slider on maps representation
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(Color.Black.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Search, contentDescription = "بحث", tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(6.dp))
                                BasicTextFieldStyle(
                                    value = searchQuery,
                                    onValueChange = { viewModel.setQuery(it) },
                                    placeholder = "بحث باسم المهني أو العنوان...",
                                    config = config
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            TextButton(onClick = { showAdvancedSearch = !showAdvancedSearch }) {
                                Text(
                                    text = if (showAdvancedSearch) "إغلاق ❌" else "فلترة ⚙️",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        AnimatedVisibility(visible = showAdvancedSearch) {
                            Column(modifier = Modifier.padding(top = 12.dp)) {
                                Text("توسيع الفرز والبحث المتقدم", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 14.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                // Phone Filter field
                                BasicOutlinedTextField(
                                    value = searchPhoneQuery,
                                    onValueChange = { viewModel.setPhoneQuery(it) },
                                    label = "فلترة طبقاً لرقم الهاتف",
                                    keyboardType = KeyboardType.Phone,
                                    config = config
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                // Select City Location
                                Text("اختر المدينة:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.Gray)
                                val cities = config?.citiesListJson?.split(",") ?: emptyList()
                                LazyRow(modifier = Modifier.padding(vertical = 4.dp)) {
                                    item {
                                        FilterChip(
                                            selected = selectedCity == null,
                                            onClick = { viewModel.selectCity(null) },
                                            label = { Text("الكل") }
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                    }
                                    items(cities) { city ->
                                        FilterChip(
                                            selected = selectedCity == city,
                                            onClick = { viewModel.selectCity(city) },
                                            label = { Text(city) }
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                
                                // Radius Search on Map Section
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.3f)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("تفعيل فلترة النطاق الجغرافي (تحديد القطر)", fontSize = 12.sp, color = Color.White)
                                            Switch(
                                                checked = isRadiusEnabled,
                                                onCheckedChange = { viewModel.isRadiusFilterEnabled.value = it }
                                            )
                                        }
                                        if (isRadiusEnabled) {
                                            Text("نصف قطر البحث الذكي: ${radiusKm.toInt()} كم", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                            Slider(
                                                value = radiusKm,
                                                onValueChange = { viewModel.radiusSearchKm.value = it },
                                                valueRange = 1f..(config?.maxRadiusLimitKm ?: 100f)
                                            )
                                            Text("مركز البحث الافتراضي: صنعاء (تنسيق GPS)", fontSize = 10.sp, color = Color.Gray)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Horizontal Categories Scroller
                item {
                    Column(modifier = Modifier.padding(vertical = 6.dp)) {
                        Text(
                            text = "الأقسام المتاحة",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                        val mainCategories = categories.filter { it.parentId == null }
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            item {
                                FilterChip(
                                    selected = selectedCat == null,
                                    onClick = { viewModel.selectCategory(null) },
                                    label = { Text("الكل 🌐") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary
                                    )
                                )
                            }
                            items(mainCategories) { cat ->
                                FilterChip(
                                    selected = selectedCat?.id == cat.id,
                                    onClick = { viewModel.selectCategory(cat) },
                                    label = { Text("${cat.icon} ${cat.nameAr}") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary
                                    )
                                )
                            }
                        }

                        // Child subcategories horizontal chip feed
                        if (selectedCat != null) {
                            val subCats = categories.filter { it.parentId == selectedCat?.id }
                            if (subCats.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    items(subCats) { sCat ->
                                        ElevatedAssistChip(
                                            onClick = { viewModel.selectCategory(sCat) },
                                            label = { Text("${sCat.icon} ${sCat.nameAr}", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Register shortcut banner
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("انضم الآن كصاحب مهنة!", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                Text("اعرض بطاقتك وسجل نفسك مجاناً للتواصل مع الزبائن", fontSize = 12.sp, color = Color.Gray)
                            }
                            Button(
                                onClick = { viewModel.navigateTo(AppScreen.REGISTER_PROFESSIONAL) },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("تسجيل كادر", fontWeight = FontWeight.Bold, color = Color.Black)
                            }
                        }
                    }
                }

                // Providers Header list count
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp, bottom = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "الكوادر والمهنيين (${filteredProviders.size})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (selectedCat != null || searchQuery.isNotBlank()) {
                            TextButton(onClick = {
                                viewModel.selectCategory(null)
                                viewModel.setQuery("")
                                viewModel.setPhoneQuery("")
                            }) {
                                Text("إعادة تعيين", color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }

                // List of Providers
                if (filteredProviders.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(40.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.SearchOff, contentDescription = "لا نتيجة", modifier = Modifier.size(64.dp), tint = Color.Gray)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("عذراً، لم نجد نتائج مطابقة لفلتر البحث الجاري.", color = Color.Gray, fontSize = 14.sp, textAlign = TextAlign.Center)
                        }
                    }
                } else {
                    items(filteredProviders) { provider ->
                        ProviderItemCard(provider = provider, config = config) {
                            viewModel.navigateTo(AppScreen.PROVIDER_DETAIL, provider.id)
                        }
                    }
                }
            }

            // User dashboard (historic contact ticks overlay sliding pane)
            AnimatedVisibility(
                visible = activeTabUserDashboard,
                enter = slideInHorizontally(initialOffsetX = { it }) + fadeIn(),
                exit = slideOutHorizontally(targetOffsetX = { it }) + fadeOut(),
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(0.85f)
                    .align(Alignment.CenterEnd)
                    .background(Color(0xFF0F172A))
                    .padding(16.dp)
                    .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
            ) {
                UserDashboardLayout(viewModel) {
                    activeTabUserDashboard = false
                }
            }
        }
    }
}

// 2b. User dashboard contact ticket logs panel
@Composable
fun UserDashboardLayout(viewModel: AppViewModel, onClose: () -> Unit) {
    val tickets by viewModel.serviceTickets.collectAsState()
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("لوحة طلبات الخدمة والاتصالات", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 16.sp)
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "اغلاق", tint = Color.White)
            }
        }
        HorizontalDivider(color = Color.DarkGray, modifier = Modifier.padding(vertical = 8.dp))

        Text("مسجل تاريخ اتصالاتك وحالة تواصلك مع الكوادر اليمنية لضمان متابعة جودة خدماتك:", fontSize = 12.sp, color = Color.LightGray)
        Spacer(modifier = Modifier.height(12.dp))

        if (tickets.isEmpty()) {
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Text("لا توجد طلبات خدمة سابقة أو سجلات تواصل حالياً.", color = Color.Gray, fontSize = 13.sp, textAlign = TextAlign.Center)
            }
        } else {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(tickets) { t ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(t.providerName, fontWeight = FontWeight.Bold, color = Color.White)
                                Box(
                                    modifier = Modifier
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(t.status, color = MaterialTheme.colorScheme.primary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Text("رقم الكادر: ${t.providerPhone}", fontSize = 12.sp, color = Color.LightGray)
                            Text("القسم المطلق: ${t.requestedCategory}", fontSize = 12.sp, color = Color.LightGray)
                            if (t.notes.isNotBlank()) {
                                Text("تفاصيل الملاحظة: ${t.notes}", fontSize = 11.sp, color = Color.Yellow)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(onClick = {
                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${t.providerPhone}"))
                                    context.startActivity(intent)
                                }) {
                                    Text("اتصال مجدداً 📞", fontSize = 11.sp)
                                }
                                TextButton(
                                    onClick = { viewModel.clearServiceRequestLogs(t.id) },
                                    colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                                ) {
                                    Text("حذف السجل", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// 3. Provider Details View Screen
@Composable
fun ProviderDetailScreen(viewModel: AppViewModel) {
    val context = LocalContext.current
    val config by viewModel.appConfig.collectAsState()
    val providerId by viewModel.selectedProviderId.collectAsState()
    val allProviders by viewModel.allProvidersRaw.collectAsState()

    val provider = remember(allProviders, providerId) {
        allProviders.find { it.id == providerId }
    }

    // Report Dialog triggers
    var openReportDialog by remember { mutableStateOf(false) }
    var reporterPhone by remember { mutableStateOf("") }
    var reportReason by remember { mutableStateOf("") }
    var reportDesc by remember { mutableStateOf("") }

    // Premium upgrade requests triggers
    var openPremiumDialog by remember { mutableStateOf(false) }
    var premiumReceiptId by remember { mutableStateOf("") }

    if (provider == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("مقدم الخدمة المختار غير متاح حالياً.", color = Color.Red)
        }
        return
    }

    Scaffold(
        topBar = {
            OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = { Text("تفاصيل البطاقة المهنية") },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateTo(AppScreen.HOME) }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "رجوع")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Box(contentAlignment = Alignment.BottomEnd) {
                    AsyncImage(
                        model = provider.profileImage,
                        contentDescription = "صورة",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(120.dp)
                            .clip(RoundedCornerShape(60.dp))
                            .border(3.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(60.dp))
                    )
                    
                    if (provider.isVerified) {
                        Surface(
                            color = Color(0xFF1E88E5),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "موثق",
                                tint = Color.White,
                                modifier = Modifier.padding(4.dp)
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(provider.name, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    if (provider.hasPremiumBadge) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "👑 مميز",
                            fontSize = 12.sp,
                            color = Color.Yellow,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .background(Color.Yellow.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Text("الموقع: اليمن - ${provider.address} (${provider.district})", fontSize = 14.sp, color = Color.LightGray)
                Text("تقييم المهني العام: ⭐ ${provider.rating} / 5.0", fontSize = 14.sp, color = Color.Yellow)
                
                Spacer(modifier = Modifier.height(24.dp))
            }

            // CTA Call panel
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("متاح ومستعد لتلقي طلباتك الآن!", fontWeight = FontWeight.Bold, color = Color.White)
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                // Save ticket history in User Dashboard
                                viewModel.logUserBookingAction(
                                    providerId = provider.id,
                                    name = provider.name,
                                    phone = provider.phone,
                                    category = "اتصال هاتفي مباشر",
                                    notes = "تم إجراء اتصال بالرقم للتنسيق على مهمة."
                                )
                                val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${provider.phone}"))
                                context.startActivity(dialIntent)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Phone, contentDescription = "اتصال")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("اتصال مباشر: ${provider.phone}", fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Reports / Subscription CTAs
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { openReportDialog = true },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                        border = BorderStroke(1.dp, Color.Red),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Report, contentDescription = "إبلاغ", tint = Color.Red)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("الإبلاغ عن المهني")
                    }

                    Button(
                        onClick = { openPremiumDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
                        modifier = Modifier.weight(1.5f)
                    ) {
                        Text("جعل الكادر مميزاً 👑", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 1. Report Complaining dialogues
        if (openReportDialog) {
            AlertDialog(
                onDismissRequest = { openReportDialog = false },
                title = { Text("صندوق الإبلاغات وبلاغات الشكاوى") },
                text = {
                    Column {
                        Text("يتم تسجيل هذا البلاغ في قاعدة البيانات لضمان مراجعته يدوياً من الإدارة.", fontSize = 12.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(8.dp))
                        BasicOutlinedTextField(value = reporterPhone, onValueChange = { reporterPhone = it }, label = "رقم هاتفك للتأكيد", keyboardType = KeyboardType.Phone, config = config)
                        Spacer(modifier = Modifier.height(8.dp))
                        BasicOutlinedTextField(value = reportReason, onValueChange = { reportReason = it }, label = "سبب الشكوى الرئيسي", config = config)
                        Spacer(modifier = Modifier.height(8.dp))
                        BasicOutlinedTextField(value = reportDesc, onValueChange = { reportDesc = it }, label = "تفاصيل المشكلة التي حدثت", config = config)
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (reporterPhone.isNotBlank() && reportReason.isNotBlank()) {
                                viewModel.fileReport(
                                    providerId = provider.id,
                                    providerName = provider.name,
                                    phone = reporterPhone,
                                    reason = reportReason,
                                    desc = reportDesc
                                )
                                Toast.makeText(context, "تم إرسال البلاغ وسنراجع الشكوى فوراً!", Toast.LENGTH_SHORT).show()
                                openReportDialog = false
                            } else {
                                Toast.makeText(context, "يرجى تعبئة كافة الحقول للتسجيل", Toast.LENGTH_SHORT).show()
                            }
                        }
                    ) { Text("تقديم البلاغ") }
                },
                dismissButton = {
                    TextButton(onClick = { openReportDialog = false }) { Text("إلغاء") }
                }
            )
        }

        // 2. Premium upgrade subscription dialog billing verification
        if (openPremiumDialog) {
            AlertDialog(
                onDismissRequest = { openPremiumDialog = false },
                title = { Text("طلب خدمات الترقية بريميوم 👑") },
                text = {
                    Column {
                        Text("تمنحك العضوية المميزة شارة ذهبية براقة وظهوراً مجدداً دائم الصدارة في أعلى البحث لجميع الزوار وزيادة في المكالمات بنسبة 300%!", fontSize = 13.sp, color = Color.LightGray)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("القيمة: 10,000 ريال يمني شهرياً", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(12.dp))
                        BasicOutlinedTextField(value = premiumReceiptId, onValueChange = { premiumReceiptId = it }, label = "أدخل رقم حوّالة الاشتراك أو سند الرول الموثق", config = config)
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (premiumReceiptId.isNotBlank()) {
                                viewModel.requestPremiumSubscription(
                                    providerId = provider.id,
                                    name = provider.name,
                                    phone = provider.phone,
                                    billReceipt = premiumReceiptId
                                )
                                Toast.makeText(context, "تم تقديم طلب الترقية! سيقوم الأدمن بمراجعته الآن.", Toast.LENGTH_LONG).show()
                                openPremiumDialog = false
                            } else {
                                Toast.makeText(context, "يرجى إرفاق معلومات الحوّالة للتحقق", Toast.LENGTH_SHORT).show()
                            }
                        }
                    ) { Text("إرسال للتحقق") }
                },
                dismissButton = {
                    TextButton(onClick = { openPremiumDialog = false }) { Text("تراجع") }
                }
            )
        }
    }
}

// 4. Form Submission Page with dependents selection and camera selectors
@Composable
fun RegisterProfessionalFormScreen(viewModel: AppViewModel) {
    val context = LocalContext.current
    val config by viewModel.appConfig.collectAsState()
    val categories by viewModel.categories.collectAsState()

    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    
    // Dependent category / subcategory selection solve states
    var selectedParentCat by remember { mutableStateOf<Category?>(null) }
    var selectedChildCat by remember { mutableStateOf<Category?>(null) }
    
    var addressCity by remember { mutableStateOf("") }
    var districtArea by remember { mutableStateOf("") }
    var gpsCoords by remember { mutableStateOf("15.3694,44.1910") }

    // Image holders
    var selfieImagePath by remember { mutableStateOf<String?>(null) }
    var idCardPath by remember { mutableStateOf<String?>(null) }

    var expandedParentDropdown by remember { mutableStateOf(false) }
    var expandedChildDropdown by remember { mutableStateOf(false) }

    // Camera image taking intents
    val selfieLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            selfieImagePath = "سيلفي مباشرة ملتقطة بـكاميرا الهاتف"
            Toast.makeText(context, "تم التقاط الصورة الشخصية بنجاح!", Toast.LENGTH_SHORT).show()
        }
    }

    val idLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            idCardPath = "بطاقة هوية مباشرة ملتقطة"
            Toast.makeText(context, "تم التقاط بطاقة الهوية ميكانيكياً!", Toast.LENGTH_SHORT).show()
        }
    }

    // Gallery selective pickers
    val gallerySelfieLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            selfieImagePath = "تم اختيارها من معرض الصور"
            Toast.makeText(context, "تم تحميل الصورة من الذاكرة", Toast.LENGTH_SHORT).show()
        }
    }

    val galleryIdLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            idCardPath = "بطاقة هوية مستوردة من المعرض"
            Toast.makeText(context, "تم إرفاق مستند الهوية بنجاح!", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = { Text("تعبئة طلب تسجيل كادر") },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateTo(AppScreen.HOME) }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "رجوع")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            item {
                Text("سجل بياناتك المهنية كعضو جديد في الدليل الوطني للمهنيين:", fontSize = 14.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                BasicOutlinedTextField(value = name, onValueChange = { name = it }, label = "الاسم الكامل الثنائي أو الثلاثي", config = config)
                Spacer(modifier = Modifier.height(8.dp))
                BasicOutlinedTextField(value = phone, onValueChange = { phone = it }, label = "رقم الهاتف الفعال (مثال: 777112233)", keyboardType = KeyboardType.Phone, config = config)
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Category & Subcategory selection solved natively here
            item {
                Text("المجموعة والمهنة الرئيسية المطلوبة للاعتماد:", fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(modifier = Modifier.height(4.dp))
                
                // 1. Parent Category Dropdown Selector
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { expandedParentDropdown = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = selectedParentCat?.let { "${it.icon} ${it.nameAr}" } ?: "اختر القسم العام الرئيسي...",
                            color = Color.White
                        )
                    }
                    DropdownMenu(
                        expanded = expandedParentDropdown,
                        onDismissRequest = { expandedParentDropdown = false },
                        modifier = Modifier.fillMaxWidth(0.9f)
                    ) {
                        categories.filter { it.parentId == null }.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text("${cat.icon} ${cat.nameAr}", fontWeight = FontWeight.Bold) },
                                onClick = {
                                    selectedParentCat = cat
                                    selectedChildCat = null // Reset child selection when parent category swaps!
                                    expandedParentDropdown = false
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                // 2. Child Dependent Subcategory Dropdown Solved
                if (selectedParentCat != null) {
                    val dependentSubs = categories.filter { it.parentId == selectedParentCat!!.id }
                    
                    Text("اختر المهنة والخدمة الفرعية الدقيقة:", fontSize = 12.sp, color = Color.LightGray)
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { expandedChildDropdown = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = selectedChildCat?.let { "${it.icon} ${it.nameAr}" } ?: "انقر لاختيار مهنتك الخاصة بالقسم...",
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        DropdownMenu(
                            expanded = expandedChildDropdown,
                            onDismissRequest = { expandedChildDropdown = false },
                            modifier = Modifier.fillMaxWidth(0.9f)
                        ) {
                            dependentSubs.forEach { sCat ->
                                DropdownMenuItem(
                                    text = { Text("${sCat.icon} ${sCat.nameAr}") },
                                    onClick = {
                                        selectedChildCat = sCat
                                        expandedChildDropdown = false
                                    }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            item {
                BasicOutlinedTextField(value = addressCity, onValueChange = { addressCity = it }, label = "المحافظة السكنية (مثال: صنعاء, عدن)", config = config)
                Spacer(modifier = Modifier.height(8.dp))
                BasicOutlinedTextField(value = districtArea, onValueChange = { districtArea = it }, label = "المديرية أو المنطقة السكنية", config = config)
                Spacer(modifier = Modifier.height(8.dp))
                BasicOutlinedTextField(value = gpsCoords, onValueChange = { gpsCoords = it }, label = "تنسيق موقعك GPS (اختياري)", config = config)
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Image Capture Options: Camera Or Gallery selection
            item {
                Text("إثبات الهوية الشخصية وبطاقة التسجيل:", fontWeight = FontWeight.Bold, color = Color.White)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.04f))
                    ) {
                        Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("1. صورة سيلفي الكابتن", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.LightGray)
                            Spacer(modifier = Modifier.height(6.dp))
                            Row {
                                IconButton(onClick = { selfieLauncher.launch() }) {
                                    Icon(Icons.Default.CameraAlt, contentDescription = "كاميرا", tint = MaterialTheme.colorScheme.primary)
                                }
                                IconButton(onClick = { gallerySelfieLauncher.launch("image/*") }) {
                                    Icon(Icons.Default.PhotoLibrary, contentDescription = "معرض", tint = Color.LightGray)
                                }
                            }
                            if (selfieImagePath != null) {
                                Text("✅ جاهز", fontSize = 11.sp, color = Color.Green)
                            }
                        }
                    }

                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.04f))
                    ) {
                        Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("2. بطاقة الشخصية الشخصانية", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.LightGray)
                            Spacer(modifier = Modifier.height(6.dp))
                            Row {
                                IconButton(onClick = { idLauncher.launch() }) {
                                    Icon(Icons.Default.CameraAlt, contentDescription = "كاميرا", tint = MaterialTheme.colorScheme.primary)
                                }
                                IconButton(onClick = { galleryIdLauncher.launch("image/*") }) {
                                    Icon(Icons.Default.PhotoLibrary, contentDescription = "معرض", tint = Color.LightGray)
                                }
                            }
                            if (idCardPath != null) {
                                Text("✅ جاهز", fontSize = 11.sp, color = Color.Green)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Action submit
            item {
                Button(
                    onClick = {
                        if (name.isNotBlank() && phone.isNotBlank() && selectedParentCat != null && selectedChildCat != null) {
                            viewModel.submitNewProfessionalRegistration(
                                name = name,
                                phone = phone,
                                categoryId = selectedParentCat!!.id,
                                subCategoryId = selectedChildCat!!.id,
                                address = addressCity,
                                district = districtArea,
                                gps = gpsCoords,
                                profileImage = "",
                                idCardImage = idCardPath
                            )
                            Toast.makeText(context, "تم إرسال طلب انضمامك للجنة المراجعة وسيتم إشعارك خلال 24 ساعة!", Toast.LENGTH_LONG).show()
                            viewModel.navigateTo(AppScreen.HOME)
                        } else {
                            Toast.makeText(context, "يرجى ملء الاسم ورقم الهاتف واختيار القسم والمهنة بدقة!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text("إرسال طلب الانضمام والتسجيل", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// 5. Admin Logins and backdoor credentials overrides
@Composable
fun LoginScreen(viewModel: AppViewModel) {
    val context = LocalContext.current
    val config by viewModel.appConfig.collectAsState()
    
    var password by remember { mutableStateOf("") }
    var backdoorPass by remember { mutableStateOf("") }

    // Remember logic checkmark states
    var rememberAdminLogin by remember { mutableStateOf(viewModel.isRememberAdminLoginEnabled.value) }
    var rememberBackdoorPass by remember { mutableStateOf(viewModel.isBackdoorRemembered.value) }

    Scaffold(
        topBar = {
            OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = { Text("تسجيل دخول المالك والآدمن") },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateTo(AppScreen.HOME) }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "رجوع")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.LockOpen,
                contentDescription = "قفل",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(18.dp))

            Text("تسجيل الدخول للوحة التحكم الرئيسية", fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.height(12.dp))

            // Standard Admin login password box
            BasicOutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = "كلمة مرور الآدمن (admin)",
                visualTransformation = PasswordVisualTransformation(),
                config = config
            )
            Spacer(modifier = Modifier.height(6.dp))

            // Save login toggle checkbox
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = rememberAdminLogin,
                    onCheckedChange = {
                        rememberAdminLogin = it
                        viewModel.setRememberAdminLogin(it, context)
                    }
                )
                Text("حفظ تسجيل الدخول للأبد على الهاتف", fontSize = 12.sp, color = Color.White)
            }
            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    if (viewModel.attemptAdminLogin(password, context)) {
                        Toast.makeText(context, "مرحباً بالآدمن! تم فحص ترخيص الأمان ودخول لوحة التحكم والتحقق اللحظي", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "الرمز خاطئ أو الجهاز غير مصرح به!", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("دخول الآدمن", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(40.dp))
            HorizontalDivider(color = Color.DarkGray)
            Spacer(modifier = Modifier.height(30.dp))

            // Backdoor access section
            Text("الولوج الآمن للمالك المطور (البوابة الخلفية)", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))

            BasicOutlinedTextField(
                value = backdoorPass,
                onValueChange = { backdoorPass = it },
                label = "رمز تخطي المالك (7777)",
                visualTransformation = PasswordVisualTransformation(),
                config = config
            )
            Spacer(modifier = Modifier.height(6.dp))

            // Save backdoor credentials checkbox
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = rememberBackdoorPass,
                    onCheckedChange = {
                        rememberBackdoorPass = it
                        viewModel.setRememberBackdoorPass(it, context)
                    }
                )
                Text("حفظ كلمة المرور للبوابة الخلفية تلقائياً", fontSize = 12.sp, color = Color.White)
            }
            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    if (viewModel.unlockBackdoor(backdoorPass, context)) {
                        Toast.makeText(context, "تم تخطي الأمان وفتح بوابة المالك الخاصة!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "رمز تفويض المالك خاطئ!", Toast.LENGTH_SHORT).show()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("تخطي المالك", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

// 6. Administrator Center with White-lists and Alert Logs Tickings
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AdminDashboardScreen(viewModel: AppViewModel) {
    val context = LocalContext.current
    val config by viewModel.appConfig.collectAsState()
    val pending by viewModel.pendingRegistrations.collectAsState()
    val rawProviders by viewModel.allProvidersRaw.collectAsState()
    val alerts by viewModel.systemAlerts.collectAsState()
    val activeBanners by viewModel.allBannersRaw.collectAsState()
    val whitelist by viewModel.whitelistedDevices.collectAsState()

    var activeAdminTab by remember { mutableStateOf("NOTIFICATIONS") } // "NOTIFICATIONS", "WHITELIST", "PROVIDERS", "WIDGETS", "BANNERS"

    // Theme Config selection properties: Silver, Luxury Gold, Royal Emerald
    var activeThemeChoice by remember { mutableStateOf(config?.colorThemeId ?: "silver") }
    var activeFontSizeOffset by remember { mutableStateOf(config?.fontSizeOffset ?: 0) }
    var activeFontFaceChoice by remember { mutableStateOf(config?.fontTypeFace ?: "normal") }

    Scaffold(
        topBar = {
            OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = { Text("لوحة التحكم السيادية للأدمن") },
                actions = {
                    // Logout button inside core supervisor dashboard
                    IconButton(onClick = { viewModel.adminLogout(context) }) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "خروج", tint = Color.Red)
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Horizontal view choices bar
            ScrollableTabRow(
                selectedTabIndex = when (activeAdminTab) {
                    "NOTIFICATIONS" -> 0
                    "WHITELIST" -> 1
                    "PROVIDERS" -> 2
                    "WIDGETS" -> 3
                    "BANNERS" -> 4
                    else -> 0
                },
                edgePadding = 8.dp
            ) {
                Tab(selected = activeAdminTab == "NOTIFICATIONS", onClick = { activeAdminTab = "NOTIFICATIONS" }) {
                    Text("الإشعارات (${alerts.size})", modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold)
                }
                Tab(selected = activeAdminTab == "WHITELIST", onClick = { activeAdminTab = "WHITELIST" }) {
                    Text("الأجهزة البيضاء", modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold)
                }
                Tab(selected = activeAdminTab == "PROVIDERS", onClick = { activeAdminTab = "PROVIDERS" }) {
                    Text("الكوادر", modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold)
                }
                Tab(selected = activeAdminTab == "WIDGETS", onClick = { activeAdminTab = "WIDGETS" }) {
                    Text("الأزرار العائمة", modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold)
                }
                Tab(selected = activeAdminTab == "BANNERS", onClick = { activeAdminTab = "BANNERS" }) {
                    Text("اللافتات", modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                when (activeAdminTab) {
                    // TAB 1: Live system notifications logger
                    "NOTIFICATIONS" -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("أحدث الأنشطة والبلاغات الواردة", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            TextButton(onClick = { viewModel.dismissAllSystemAlerts() }) {
                                Text("تصفير التنبيهات 🧹", color = Color.Red)
                            }
                        }
                        
                        if (alerts.isEmpty()) {
                            Box(modifier = Modifier.height(200.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                                Text("لا توجد بلاغات أو إشعارات جديدة بانتظارك حالياً.", color = Color.Gray)
                            }
                        } else {
                            alerts.forEach { alert ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(alert.title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                        Text(alert.message, color = Color.White, fontSize = 13.sp)
                                        Text("نوع التنبيه: ${alert.type} • الوقت: الآن", color = Color.Gray, fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }

                    // TAB 2: Device Whitelist Settings
                    "WHITELIST" -> {
                        var inputDeviceName by remember { mutableStateOf("") }
                        var inputDeviceLabel by remember { mutableStateOf("") }

                        Text("جدران الأمان والأجهزة المصرحة لبوابة المشرف", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Text("الآرشاد: تفعيل لائحة الأجهزة تمنع المخترقين من تصفح لوحة التحكم حتى بامتلاك كلمة السر، ما لم يضاف عنوان جهازهم الفرعي هنا.", fontSize = 12.sp, color = Color.LightGray)
                        Spacer(modifier = Modifier.height(12.dp))

                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.04f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("تمكين جهاز جديد في اللائحة:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                BasicOutlinedTextField(value = inputDeviceName, onValueChange = { inputDeviceName = it }, label = "رقم المعرف الفرعي للجهاز (Device UUID)", config = config)
                                BasicOutlinedTextField(value = inputDeviceLabel, onValueChange = { inputDeviceLabel = it }, label = "اسم مالك الجهاز / الهاتف المعتمد", config = config)
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = {
                                        if (inputDeviceName.isNotBlank() && inputDeviceLabel.isNotBlank()) {
                                            viewModel.addDeviceToWhitelist(inputDeviceName.trim(), inputDeviceLabel.trim())
                                            Toast.makeText(context, "تم إدراج الجهاز باللائحة البيضاء بنجاح", Toast.LENGTH_SHORT).show()
                                            inputDeviceName = ""
                                            inputDeviceLabel = ""
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("ترخيص الجهاز ➕")
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("الأجهزة المصداق عليها الآن:", fontWeight = FontWeight.Bold)
                        
                        whitelist.forEach { dev ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(dev.labelName, fontWeight = FontWeight.Bold, color = Color.White)
                                    Text("معرف: ${dev.deviceId}", fontSize = 11.sp, color = Color.LightGray)
                                }
                                IconButton(onClick = { viewModel.removeDeviceFromWhitelist(dev.deviceId) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "حذف", tint = Color.Red)
                                }
                            }
                        }
                    }

                    // TAB 3: Co-workers control (Verified badges, Ban controls, and approvals)
                    "PROVIDERS" -> {
                        Text("التحقق والمصادقة وتعديل الكوادر المهنية", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(12.dp))

                        // Applicant approvals
                        if (pending.isNotEmpty()) {
                            Text("طلبات التراخيص الجديدة المعلقة (${pending.size}):", fontWeight = FontWeight.Bold, color = Color.Yellow)
                            pending.forEach { req ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.Yellow.copy(alpha = 0.06f))
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(req.name, fontWeight = FontWeight.Bold, color = Color.White)
                                        Text("الهاتف: ${req.phone} • الموقع: ${req.address} (${req.district})", fontSize = 12.sp, color = Color.LightGray)
                                        if (req.idCardImage != null) {
                                            Text("مستند الهوية المرفق: ${req.idCardImage}", fontSize = 11.sp, color = Color.Green)
                                        }
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.End
                                        ) {
                                            TextButton(onClick = { viewModel.rejectRegistrationRequest(req.id) }, colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)) {
                                                Text("رفض طلب")
                                            }
                                            Button(onClick = { viewModel.approveRegistrationRequest(req) }) {
                                                Text("الموافقة والإضافة ✅")
                                            }
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(20.dp))
                        }

                        // Existing active list with quick action verifications and blacklisting bans
                        Text("إدارة الكوادر وحظر المعرفات:", fontWeight = FontWeight.Bold)
                        rawProviders.forEach { prov ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.04f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(prov.name, fontWeight = FontWeight.Bold, color = if (prov.isBlacklisted) Color.Red else Color.White)
                                        IconButton(onClick = { viewModel.deleteProvider(prov.id) }) {
                                            Icon(Icons.Default.Delete, contentDescription = "حذف", tint = Color.Red)
                                        }
                                    }
                                    Text("الموقع: ${prov.address} (${prov.district})", fontSize = 12.sp, color = Color.LightGray)
                                    
                                    FlowRow(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        // 1. Unlocked premium crowns Highlight check
                                        FilterChip(
                                            selected = prov.hasPremiumBadge,
                                            onClick = { viewModel.toggleSovereignPremiumHighlight(prov.id, !prov.hasPremiumBadge) },
                                            label = { Text("تاج تمييز 👑") }
                                        )

                                        // 2. Authentication Blue badges check
                                        FilterChip(
                                            selected = prov.isVerified,
                                            onClick = { viewModel.toggleBadgeVerification(prov.id, !prov.isVerified) },
                                            label = { Text("شارة موثق ✔️") }
                                        )

                                        // 3. User block / Ban actions toggle
                                        FilterChip(
                                            selected = prov.isBlacklisted,
                                            onClick = { viewModel.toggleBlacklistAction(prov.id, !prov.isBlacklisted) },
                                            label = { Text(if (prov.isBlacklisted) "فك حظر المستخدم 🚫" else "حظر المستخدم") },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = Color.Red
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // TAB 4: Theme selections and active floating actions controls
                    "WIDGETS" -> {
                        Text("التحكم بثيم التطبيق والخطوط والأزرار العائمة", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(12.dp))

                        // A. Visual Style Selector Choice
                        Text("1. نظام الهوية البصرية للتطبيق (السمة):", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { viewModel.changeSystemTheme("silver") },
                                colors = ButtonDefaults.buttonColors(containerColor = if (activeThemeChoice == "silver") Color.White else Color.Gray),
                                modifier = Modifier.weight(1f)
                            ) { Text("كوزميك سيلفر", color = Color.Black) }

                            Button(
                                onClick = { viewModel.changeSystemTheme("gold") },
                                colors = ButtonDefaults.buttonColors(containerColor = if (activeThemeChoice == "gold") Color(0xFFD4AF37) else Color.Gray),
                                modifier = Modifier.weight(1f)
                            ) { Text("ذهبي فاخر", color = Color.Black) }

                            Button(
                                onClick = { viewModel.changeSystemTheme("emerald") },
                                colors = ButtonDefaults.buttonColors(containerColor = if (activeThemeChoice == "emerald") Color(0xFF50C878) else Color.Gray),
                                modifier = Modifier.weight(1f)
                            ) { Text("زمردي راقي", color = Color.Black) }
                        }
                        Spacer(modifier = Modifier.height(12.dp))

                        // B. Floating Trigger buttons sizes and visibility modifiers
                        Text("2. كواتم وإعدادات الزر المساعد والدردشة:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        
                        var fAssistantVisible by remember { mutableStateOf(config?.smartAssistantIconVisible ?: true) }
                        var fAssistantSize by remember { mutableIntStateOf(config?.smartAssistantIconSize ?: 46) }
                        var fInfoVisible by remember { mutableStateOf(config?.appInfoIconVisible ?: true) }
                        var fInfoSize by remember { mutableIntStateOf(config?.appInfoIconSize ?: 46) }
                        
                        var fChatVisible by remember { mutableStateOf(config?.adminChatIconVisible ?: true) }
                        var fChatSize by remember { mutableIntStateOf(config?.adminChatIconSize ?: 46) }
                        var fChatHoriz by remember { mutableIntStateOf(config?.adminChatBottomOffset ?: 80) }
                        var fChatVert by remember { mutableIntStateOf(config?.adminChatStartOffset ?: 20) }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White.copy(alpha = 0.04f), RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Text("المساعد الذكي الروبوت", fontWeight = FontWeight.Bold)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("عرض الزر")
                                Switch(checked = fAssistantVisible, onCheckedChange = { fAssistantVisible = it })
                            }
                            Text("حجم أيقونة الروبوت: ${fAssistantSize}dp")
                            Slider(value = fAssistantSize.toFloat(), onValueChange = { fAssistantSize = it.toInt() }, valueRange = 24f..96f)

                            Spacer(modifier = Modifier.height(10.dp))
                            Text("معلومات التطبيق الذكي", fontWeight = FontWeight.Bold)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("عرض الزر")
                                Switch(checked = fInfoVisible, onCheckedChange = { fInfoVisible = it })
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Text("أيقونة المحادثة الفورية وتحديد تموضع الإحداثيات", fontWeight = FontWeight.Bold)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("عرض أيقونة المحادثة للفريق")
                                Switch(checked = fChatVisible, onCheckedChange = { fChatVisible = it })
                            }
                            Text("حجم المحادثة الفورية: ${fChatSize}dp")
                            Slider(value = fChatSize.toFloat(), onValueChange = { fChatSize = it.toInt() }, valueRange = 24f..96f)

                            Text("موقع الإزاحة الرأسي (التعديل): ${fChatVert}dp")
                            Slider(value = fChatVert.toFloat(), onValueChange = { fChatVert = it.toInt() }, valueRange = 0f..250f)

                            Text("موقع الإزاحة الأفقي (التحريك): ${fChatHoriz}dp")
                            Slider(value = fChatHoriz.toFloat(), onValueChange = { fChatHoriz = it.toInt() }, valueRange = 10f..400f)

                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = {
                                    viewModel.updateFloatingWidgetsSettings(
                                        assistantVisible = fAssistantVisible,
                                        assistantSize = fAssistantSize,
                                        infoVisible = fInfoVisible,
                                        infoSize = fInfoSize,
                                        chatVisible = fChatVisible,
                                        chatSize = fChatSize,
                                        chatVertical = fChatVert,
                                        chatHorizontal = fChatHoriz
                                    )
                                    Toast.makeText(context, "تم حفظ وتطبيق التموضعات والأحجام بنجاح!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) { Text("حفظ تكوينات الأزرار الفورية") }
                        }
                    }

                    // TAB 5: Banners Management Editor
                    "BANNERS" -> {
                        var bannerUrl by remember { mutableStateOf("") }
                        var bannerDesc by remember { mutableStateOf("") }
                        var bannerRedirect by remember { mutableStateOf("https://") }
                        var bannerSize by remember { mutableStateOf("MEDIUM") }

                        Text("إشهار وبناء لافتات البانر في الواجهة الرئيسية", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(12.dp))

                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.04f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("إنشاء وإطلاق إعلان ممول جديد:", fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(8.dp))
                                BasicOutlinedTextField(value = bannerUrl, onValueChange = { bannerUrl = it }, label = "رابط صورة الإعلان (URL)", config = config)
                                BasicOutlinedTextField(value = bannerDesc, onValueChange = { bannerDesc = it }, label = "الرسالة والنص الترويجي الظاهر", config = config)
                                BasicOutlinedTextField(value = bannerRedirect, onValueChange = { bannerRedirect = it }, label = "رابط التوجيه (مثال: برقم الوتساب)", config = config)
                                
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("حجم البانر المطلوب:")
                                    Spacer(modifier = Modifier.width(10.dp))
                                    FilterChip(selected = bannerSize == "SMALL", onClick = { bannerSize = "SMALL" }, label = { Text("صغير") })
                                    Spacer(modifier = Modifier.width(4.dp))
                                    FilterChip(selected = bannerSize == "MEDIUM", onClick = { bannerSize = "MEDIUM" }, label = { Text("متوسط") })
                                    Spacer(modifier = Modifier.width(4.dp))
                                    FilterChip(selected = bannerSize == "LARGE", onClick = { bannerSize = "LARGE" }, label = { Text("كبير") })
                                }
                                
                                Button(
                                    onClick = {
                                        if (bannerDesc.isNotBlank()) {
                                            viewModel.createAdBanner(
                                                imageUrl = bannerUrl,
                                                redirectUrl = bannerRedirect,
                                                text = bannerDesc,
                                                bannerType = "IMAGE",
                                                sizeType = bannerSize,
                                                duration = 6
                                            )
                                            Toast.makeText(context, "تم إرسال ونشر الإعلان فوراً بمحرك الرول", Toast.LENGTH_SHORT).show()
                                            bannerUrl = ""
                                            bannerDesc = ""
                                            bannerRedirect = "https://"
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) { Text("إطلاق الإعلان وحفظه") }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                        Text("اللافتات المنشورة حالياً وحرقها:", fontWeight = FontWeight.Bold)

                        activeBanners.forEach { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .background(Color.White.copy(alpha = 0.04f), RoundedCornerShape(8.dp))
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(item.bannerText, color = Color.White, fontSize = 12.sp, maxLines = 1)
                                    Text("مقاس: ${item.sizeType} • بقاء: ${item.durationSeconds} ثواني", fontSize = 11.sp, color = Color.Gray)
                                }
                                IconButton(onClick = { viewModel.removeAdBanner(item.id) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "حذف إعلان", tint = Color.Red)
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

// 7. Sovereign Owner Portal (/backdoor dashboard screen)
@Composable
fun BackdoorDashboardScreen(viewModel: AppViewModel) {
    val context = LocalContext.current
    val config by viewModel.appConfig.collectAsState()

    var isMaintenanceModeToggled by remember { mutableStateOf(config?.isMaintenanceMode ?: false) }
    var exportFolderChoice by remember { mutableStateOf("SD") }
    var restorePathInput by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = { Text("بوابة تفريغ المالك والنسخ الشامل") },
                navigationIcon = {
                    IconButton(onClick = { viewModel.lockBackdoor(context) }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "تراجع")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Icon(
                imageVector = Icons.Default.Shield,
                contentDescription = "مالك",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(64.dp)
                    .align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(16.dp))

            Text("مرحباً بك في وحدة التحكم بالتخزين والنسخ الاحتياطي لقواعد البيانات المحلية.", fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.height(12.dp))

            // MAINTENANCE MODE TRIGGER
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.04f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("تمكين وضع صيانة التطبيق التام", fontWeight = FontWeight.Bold, color = Color.White)
                        Text("يغلق التطبيق بوجه الزوار لعمل تحسينات", fontSize = 11.sp, color = Color.Gray)
                    }
                    Switch(
                        checked = isMaintenanceModeToggled,
                        onCheckedChange = {
                            isMaintenanceModeToggled = it
                            viewModel.setMaintenanceMode(it)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // SQL BACKUP SYSTEMS
            Text("نظام النسخ الاحتياطي لقاعدة البيانات SQLite:", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(6.dp))
            Text("يمكنك أخذ نسخة احتياطية مشفرة لملفات قاعدة بيانات الكوادر كاملة وحفظها محلياً أو ترحيلها إلى بطاقة الذاكرة الخارجية SD.", fontSize = 12.sp, color = Color.LightGray)
            
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.04f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("اختر مسار التخزين المفضل لتصدير نسخة الحفظ:")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(selected = exportFolderChoice == "SD", onClick = { exportFolderChoice = "SD" }, label = { Text("بطاقة الذاكرة الخارجية/المستندات") })
                        FilterChip(selected = exportFolderChoice == "INTERNAL", onClick = { exportFolderChoice = "INTERNAL" }, label = { Text("الذاكرة الداخلية للتطبيق") })
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { viewModel.backupDataNow(exportFolderChoice, context) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("أخذ نسخة احتياطية فورية 💾", fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text("استرداد نسخة قديمة مسجلة:", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            BasicOutlinedTextField(value = restorePathInput, onValueChange = { restorePathInput = it }, label = "أدخل المسار الكامل لملف النسخة المستورد (.db)", config = config)
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    if (restorePathInput.isNotBlank()) {
                        viewModel.restoreDataNow(restorePathInput, context)
                        restorePathInput = ""
                    } else {
                        Toast.makeText(context, "الرجاء كتابة مسار الملف المستهدف", Toast.LENGTH_SHORT).show()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("استرجاع نسخة البيانات بالكامل", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

// 8. Floating actions dialog layout selector
@Composable
fun FloatingWidgetsEngine(viewModel: AppViewModel) {
    val context = LocalContext.current
    val config by viewModel.appConfig.collectAsState() ?: return

    var smartAssistantDialog by remember { mutableStateOf(false) }
    var helperInfoDialog by remember { mutableStateOf(false) }
    var chatDialogTrigger by remember { mutableStateOf(false) }

    val primaryColor = ThemeManager.getColorScheme(config).primary

    // UI configuration
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {} // non-blocking overlay
    ) {
        // Floating action column for widgets
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset {
                    IntOffset(
                        x = -(config.adminChatStartOffset.dp.toPx().toInt()),
                        y = -(config.adminChatBottomOffset.dp.toPx().toInt())
                    )
                }
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.End
        ) {
            // A. Interactive Chat button above Info (Active trigger)
            if (config.adminChatIconVisible) {
                FloatingActionButton(
                    onClick = { chatDialogTrigger = true },
                    containerColor = Color(0xFF1E88E5), // Beautiful azure messaging blue
                    modifier = Modifier.size(config.adminChatIconSize.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Chat,
                        contentDescription = "الدردشة الحية والمفتوحة",
                        tint = Color.White,
                        modifier = Modifier.size((config.adminChatIconSize * 0.55).dp)
                    )
                }
            }

            // B. App Info Floating Button popup
            if (config.appInfoIconVisible) {
                FloatingActionButton(
                    onClick = { helperInfoDialog = true },
                    containerColor = primaryColor,
                    modifier = Modifier.size(config.appInfoIconSize.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "معلومات عن التطبيق",
                        tint = Color.Black,
                        modifier = Modifier.size((config.appInfoIconSize * 0.55).dp)
                    )
                }
            }

            // C. Smart Robot assistant simulation
            if (config.smartAssistantIconVisible) {
                FloatingActionButton(
                    onClick = { smartAssistantDialog = true },
                    containerColor = Color(0xFF50C878),
                    modifier = Modifier.size(config.smartAssistantIconSize.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SmartToy,
                        contentDescription = "مساعد ذكي",
                        tint = Color.Black,
                        modifier = Modifier.size((config.smartAssistantIconSize * 0.55).dp)
                    )
                }
            }
        }

        // Float Window Dialog 1: Chat direct messaging system between professional, guest and admin
        if (chatDialogTrigger) {
            Dialog(onDismissRequest = { chatDialogTrigger = false }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.95f)
                        .height(450.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("دردشة تواصل مباشرة ومفتوحة", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                            IconButton(onClick = { chatDialogTrigger = false }) {
                                Icon(Icons.Default.Close, contentDescription = "اغلاق", tint = Color.LightGray)
                            }
                        }
                        HorizontalDivider(color = Color.DarkGray)
                        
                        val msgFlow by viewModel.chats.collectAsState()
                        var visitorMsgText by remember { mutableStateOf("") }
                        var visitorName by remember { mutableStateOf("") }

                        Text("دردشة تواصل حية بين الزوار، أصحاب المهن، والأدمن.", fontSize = 11.sp, color = Color.LightGray)
                        Spacer(modifier = Modifier.height(4.dp))

                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .background(Color.Black.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                .padding(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (msgFlow.isEmpty()) {
                                item {
                                    Text("أرسل أول رسالة للاتصال بالدعم الفني أو الكوادر!", color = Color.Gray, fontSize = 12.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(24.dp))
                                }
                            } else {
                                items(msgFlow) { msg ->
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalAlignment = if (msg.isFromVisitor) Alignment.End else Alignment.Start
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .background(
                                                    color = if (msg.isFromVisitor) Color(0xFF1E88E5) else Color(0xFF334155),
                                                    shape = RoundedCornerShape(8.dp)
                                                )
                                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                        ) {
                                            Column {
                                                Text(msg.senderName, fontSize = 10.sp, color = Color.Yellow, fontWeight = FontWeight.Bold)
                                                Text(msg.text, color = Color.White, fontSize = 13.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                BasicOutlinedTextField(value = visitorName, onValueChange = { visitorName = it }, label = "اسمك (الزائر / المهني)", config = config)
                                BasicOutlinedTextField(value = visitorMsgText, onValueChange = { visitorMsgText = it }, label = "اكتب رسالتك وتواصل لحفظها", config = config)
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            IconButton(
                                onClick = {
                                    if (visitorMsgText.isNotBlank() && visitorName.isNotBlank()) {
                                        viewModel.sendLiveChatMessage(
                                            providerId = "general",
                                            name = visitorName,
                                            text = visitorMsgText,
                                            isFromVisitor = true
                                        )
                                        visitorMsgText = ""
                                    } else {
                                        Toast.makeText(context, "الرجاء كتابة اسمك والرسالة", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(24.dp))
                            ) {
                                Icon(Icons.Default.Send, contentDescription = "أرسل", tint = Color.Black)
                            }
                        }
                    }
                }
            }
        }

        // Float Window Dialog 2: Robot smart assistant dialog setup
        if (smartAssistantDialog) {
            Dialog(onDismissRequest = { smartAssistantDialog = false }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(350.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🤖 المساعد الذكي اليمني", fontWeight = FontWeight.Bold, color = Color.White)
                            IconButton(onClick = { smartAssistantDialog = false }) {
                                Icon(Icons.Default.Close, contentDescription = "اغلاق", tint = Color.White)
                            }
                        }
                        HorizontalDivider(color = Color.DarkGray)
                        Spacer(modifier = Modifier.height(8.dp))

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "لوحة استعلامات تطبيق الخدمات: يمكنك الاستفسار عن كفاءة السباكين أو الأطباء، ومراجعة التوثيقات الحية عبر قاعدة البيانات المشفرة.",
                                fontSize = 13.sp,
                                color = Color.White
                            )
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                                modifier = Modifier.padding(top = 12.dp)
                            ) {
                                Text(
                                    "فكرة ذكية💡: يمكنك النقر فوق عنوان التطبيق بالأعلى 5 مرات متتالية لفك البوابة الخلفية السيادية للمالك مباشرة بمستند التحقق '7777'",
                                    modifier = Modifier.padding(8.dp),
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }

        // Float Window Dialog 3: App info dialog setup
        if (helperInfoDialog) {
            Dialog(onDismissRequest = { helperInfoDialog = false }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(280.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("ℹ️ عن دليل الخدمات الوطني", fontWeight = FontWeight.Bold, color = Color.White)
                            IconButton(onClick = { helperInfoDialog = false }) {
                                Icon(Icons.Default.Close, contentDescription = "اغلاق", tint = Color.White)
                            }
                        }
                        HorizontalDivider(color = Color.DarkGray)
                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            "الاصدار: v3.2.0 (مزامنة سيادية تامة)\nتطبيق دليل الخدمات اليمني يسعى لبناء ثقة راسخة للتواصل المباشر مع الكهربائيين، السباكين، أخصائيي التكييف والأطباء بالمحافظات اليمنية.",
                            color = Color.White,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

// 9. Extra beautiful components styles
@Composable
fun ProviderItemCard(provider: ServiceProvider, config: AppConfig?, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (provider.hasPremiumBadge) MaterialTheme.colorScheme.surface else Color.White.copy(alpha = 0.03f)
        ),
        border = if (provider.hasPremiumBadge) BorderStroke(1.5.dp, Color(0xFFFFD700)) else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile image with custom backup if offline
            AsyncImage(
                model = provider.profileImage,
                contentDescription = "الصورة",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(30.dp))
                    .border(2.dp, if (provider.hasPremiumBadge) Color(0xFFFFD700) else MaterialTheme.colorScheme.primary, RoundedCornerShape(30.dp))
            )
            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = provider.name,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = ThemeManager.getFontScale(config)
                    )
                    
                    if (provider.isVerified) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "موثق",
                            tint = Color(0xFF1E88E5),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Text("تلفون: ${provider.phone}", color = Color.LightGray, fontSize = 12.sp)
                Text("المحافظة: ${provider.address} (${provider.district})", color = Color.Gray, fontSize = 12.sp)
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text("⭐ ${provider.rating}", color = Color.Yellow, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                if (provider.hasPremiumBadge) {
                    Text("👑 مميز", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold, fontSize = 10.sp)
                }
            }
        }
    }
}

@Composable
fun BasicTextFieldStyle(value: String, onValueChange: (String) -> Unit, placeholder: String, config: AppConfig?) {
    Box(modifier = Modifier.fillMaxWidth()) {
        if (value.isBlank()) {
            Text(placeholder, color = Color.Gray, fontSize = 13.sp)
        }
        androidx.compose.foundation.text.BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = TextStyle(
                color = ThemeManager.getFontColor(config),
                fontWeight = ThemeManager.getFontWeight(config),
                fontFamily = ThemeManager.getFontFamily(config)
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun BasicOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    visualTransformation: androidx.compose.ui.text.input.VisualTransformation = androidx.compose.ui.text.input.VisualTransformation.None,
    config: AppConfig?
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = Color.LightGray) },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        visualTransformation = visualTransformation,
        textStyle = TextStyle(
            color = ThemeManager.getFontColor(config),
            fontWeight = ThemeManager.getFontWeight(config)
        ),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = Color.DarkGray
        ),
        modifier = Modifier.fillMaxWidth()
    )
}
