package com.example.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.rememberAsyncImagePainter
import com.example.data.*
import com.example.ui.theme.ThemeManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(viewModel: AppViewModel) {
    val context = LocalContext.current
    val currentScreen by viewModel.currentScreen.collectAsState()
    val config by viewModel.appConfig.collectAsState()
    val syncText by viewModel.syncStatus.collectAsState()
    val is2faEnabled = config?.is2faEnabled ?: false

    // Back door dialog trigger
    var showBackdoorLogin by remember { mutableStateOf(false) }

    // Maintenance Mode Page
    if (config?.isMaintenanceMode == true && !viewModel.isBackdoorUnlocked.collectAsState().value) {
        MaintenanceScreen(viewModel = viewModel, config = config)
        return
    }

    Scaffold(
        topBar = {
            Column {
                // Sync notification ticker
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = syncText,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }

                // Custom Header with 5 specific icons requested:
                // 🏠 , 🔐 , 👤 , 🌐 , 🔄
                Surface(
                    tonalElevation = 8.dp,
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left / Title Logo (Click 5 times registers backdoor attempt)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clickable {
                                    viewModel.registerAppTitleClick()
                                    if (viewModel.currentScreen.value != AppScreen.BACKDOOR_DASHBOARD) {
                                        // Quick click hint
                                        val attempts = 5
                                        // If clicked 5 times
                                        showBackdoorLogin = true
                                    }
                                }
                        ) {
                            Text(
                                "🏠",
                                fontSize = 24.sp,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text(
                                text = config?.appName ?: "دليل الخدمات",
                                color = ThemeManager.getFontColor(config),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Icons Strip arranged from Right to Left for Arab aesthetics
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 🏠 Home Icon
                            IconButton(
                                onClick = { viewModel.navigateTo(AppScreen.HOME) },
                                modifier = Modifier
                                    .size(38.dp)
                                    .background(
                                        if (currentScreen == AppScreen.HOME) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                        else Color.Transparent,
                                        CircleShape
                                    )
                                    .testTag("nav_home")
                            ) {
                                Text("🏠", fontSize = 20.sp)
                            }

                            // 🔐 Login Icon
                            IconButton(
                                onClick = {
                                    if (viewModel.isAdminLoggedIn.value) {
                                        viewModel.navigateTo(AppScreen.ADMIN_DASHBOARD)
                                    } else {
                                        viewModel.navigateTo(AppScreen.LOGIN)
                                    }
                                },
                                modifier = Modifier
                                    .size(38.dp)
                                    .background(
                                        if (currentScreen == AppScreen.LOGIN || currentScreen == AppScreen.ADMIN_DASHBOARD)
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                        else Color.Transparent,
                                        CircleShape
                                    )
                                    .testTag("nav_login")
                            ) {
                                Text("🔐", fontSize = 20.sp)
                            }

                            // 👤 New registration Icon
                            IconButton(
                                onClick = { viewModel.navigateTo(AppScreen.REGISTER_PROFESSIONAL) },
                                modifier = Modifier
                                    .size(38.dp)
                                    .background(
                                        if (currentScreen == AppScreen.REGISTER_PROFESSIONAL)
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                        else Color.Transparent,
                                        CircleShape
                                    )
                                    .testTag("nav_register")
                            ) {
                                Text("👤", fontSize = 20.sp)
                            }

                            // 🌐 Language switcher Icon
                            var showLangDialog by remember { mutableStateOf(false) }
                            IconButton(
                                onClick = { showLangDialog = true },
                                modifier = Modifier
                                    .size(38.dp)
                                    .testTag("nav_lang")
                            ) {
                                Text("🌐", fontSize = 20.sp)
                            }

                            // 🔄 Refresh Icon
                            IconButton(
                                onClick = { viewModel.triggerRefresh(context) },
                                modifier = Modifier
                                    .size(38.dp)
                                    .testTag("nav_refresh")
                            ) {
                                Text("🔄", fontSize = 20.sp)
                            }

                            if (showLangDialog) {
                                Dialog(onDismissRequest = { showLangDialog = false }) {
                                    Card(
                                        shape = RoundedCornerShape(16.dp),
                                        modifier = Modifier.padding(16.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(20.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                "اختر لغة التطبيق / Choose Language",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 16.sp,
                                                textAlign = TextAlign.Center
                                            )
                                            Spacer(modifier = Modifier.height(16.dp))
                                            Button(
                                                onClick = {
                                                    viewModel.changeLanguage("AR")
                                                    showLangDialog = false
                                                },
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text("العربية (AR)")
                                            }
                                            Spacer(modifier = Modifier.height(8.dp))
                                            OutlinedButton(
                                                onClick = {
                                                    viewModel.changeLanguage("EN")
                                                    showLangDialog = false
                                                },
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text("English (EN)")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            // Smart Assistant & Info Section floating on lower bounds
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                // Info icon to the left ( customizable size & visible )
                if (config?.appInfoIconVisible != false) {
                    var showAboutDialog by remember { mutableStateOf(false) }
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(bottom = 24.dp)
                    ) {
                        FloatingActionButton(
                            onClick = { showAboutDialog = true },
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            modifier = Modifier
                                .size((config?.appInfoIconSize ?: 46).dp)
                                .testTag("fab_info")
                        ) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = "معلومات التطبيق",
                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }

                        if (showAboutDialog) {
                            AboutAppDialog(config = config, onDismiss = { showAboutDialog = false })
                        }
                    }
                }

                // Smart Assistant floating button to the right
                if (config?.smartAssistantIconVisible != false) {
                    var showAssistantDialog by remember { mutableStateOf(false) }
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(bottom = 24.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        FloatingActionButton(
                            onClick = { showAssistantDialog = true },
                            containerColor = MaterialTheme.colorScheme.primary,
                            shape = CircleShape,
                            modifier = Modifier
                                .size((config?.smartAssistantIconSize ?: 46).dp)
                                .testTag("fab_assistant")
                        ) {
                            Text("خدمات", color = MaterialTheme.colorScheme.onPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        if (showAssistantDialog) {
                            SmartAssistantDialog(viewModel = viewModel, onDismiss = { showAssistantDialog = false })
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Primary screen container
                Box(modifier = Modifier.weight(1f)) {
                    when (currentScreen) {
                        AppScreen.HOME -> HomeScreen(viewModel = viewModel, config = config)
                        AppScreen.LOGIN -> LoginScreen(viewModel = viewModel, config = config)
                        AppScreen.REGISTER_PROFESSIONAL -> RegisterProfessionalScreen(viewModel = viewModel)
                        AppScreen.PROVIDER_DETAIL -> ProviderDetailScreen(viewModel = viewModel)
                        AppScreen.ADMIN_DASHBOARD -> AdminDashboardScreen(viewModel = viewModel)
                        AppScreen.BACKDOOR_DASHBOARD -> BackdoorDashboardScreen(viewModel = viewModel, config = config)
                    }
                }

                // Footer Area
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val promo = config?.promotionalFooter
                    if (!promo.isNullOrBlank()) {
                        Text(
                            text = promo,
                            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            }

            // Hidden backdoor Login dialog
            if (showBackdoorLogin) {
                var passVal by remember { mutableStateOf("") }
                var errorMsg by remember { mutableStateOf("") }

                Dialog(onDismissRequest = {
                    showBackdoorLogin = false
                    viewModel.resetBackdoorCounter()
                }) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "🔒 البوابة السرية للمالك",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 18.sp,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                "الرجاء كشط وإدخال كلمة المرور الخلفية للماهر للوصول للإعدادات السيادية الكاملة.",
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            OutlinedTextField(
                                value = passVal,
                                onValueChange = { passVal = it },
                                label = { Text("كلمة المرور الخلفية") },
                                visualTransformation = PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                modifier = Modifier.fillMaxWidth()
                            )
                            if (errorMsg.isNotEmpty()) {
                                Text(errorMsg, color = MaterialTheme.colorScheme.error, fontSize = 11.sp)
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        showBackdoorLogin = false
                                        viewModel.resetBackdoorCounter()
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("إلغاء")
                                }
                                Button(
                                    onClick = {
                                        if (viewModel.unlockBackdoor(passVal)) {
                                            showBackdoorLogin = false
                                        } else {
                                            errorMsg = "رمز سر خاطئ! أعد المحاولة"
                                        }
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("دخول سريع")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HomeScreen(viewModel: AppViewModel, config: AppConfig?) {
    val context = LocalContext.current
    val categories by viewModel.categories.collectAsState()
    val allProviders by viewModel.serviceProviders.collectAsState()
    val adBanners by viewModel.adBanners.collectAsState()

    // Filters
    val searchQuery by viewModel.searchQuery.collectAsState()
    val phoneQuery by viewModel.searchPhoneQuery.collectAsState()
    val nameQuery by viewModel.searchNameQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val selectedCity by viewModel.selectedCity.collectAsState()
    val isVoiceSearching by viewModel.isVoiceSearching.collectAsState()

    var showFilterSheet by remember { mutableStateOf(false) }

    // Filter providers
    val filteredProviders = remember(allProviders, searchQuery, phoneQuery, nameQuery, selectedCategory, selectedCity) {
        allProviders.filter { p ->
            val matchesCategory = if (selectedCategory != null) {
                p.categoryId == selectedCategory!!.id || p.subCategoryId == selectedCategory!!.id
            } else true

            val matchesCity = if (!selectedCity.isNullOrBlank()) {
                p.address.contains(selectedCity!!, ignoreCase = true) || p.district.contains(selectedCity!!, ignoreCase = true)
            } else true

            val matchesPhone = if (phoneQuery.isNotEmpty()) {
                p.phone.contains(phoneQuery)
            } else true

            val matchesName = if (nameQuery.isNotEmpty()) {
                p.name.contains(nameQuery, ignoreCase = true)
            } else true

            val matchesQuery = if (searchQuery.isNotEmpty()) {
                p.name.contains(searchQuery, ignoreCase = true) ||
                        p.address.contains(searchQuery, ignoreCase = true) ||
                        p.phone.contains(searchQuery)
            } else true

            p.isApproved && matchesCategory && matchesCity && matchesPhone && matchesName && matchesQuery
        }
    }

    val recommendedList = remember(allProviders) { allProviders.filter { it.isRecommended && it.isApproved } }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome Header Banner
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)
                        ),
                        RoundedCornerShape(16.dp)
                    )
                    .padding(20.dp)
            ) {
                Column {
                    Text(
                        text = config?.welcomeMessage ?: "مرحباً بك في دليل الخدمات!",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "ابحث وتواصل فوراً مع أفضل المزودين والمهنيين الموثقين بكل سهولة.",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Active Ad Banners Carousel
        if (adBanners.isNotEmpty()) {
            item {
                Text(
                    "📢 عروض وإعلانات ممولة",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = ThemeManager.getFontColor(config)
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(adBanners) { banner ->
                        Card(
                            onClick = {
                                if (banner.redirectUrl.startsWith("http")) {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(banner.redirectUrl))
                                    context.startActivity(intent)
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .width(if (banner.sizeType == "LARGE") 320.dp else 240.dp)
                                .height(if (banner.sizeType == "SMALL") 80.dp else 120.dp)
                        ) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                Image(
                                    painter = rememberAsyncImagePainter(banner.imageUrl),
                                    contentDescription = "عرض ممول",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Black.copy(alpha = 0.3f))
                                )
                                Text(
                                    text = "إعلان مميز",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .align(Alignment.TopStart)
                                        .background(Color.Red, RoundedCornerShape(bottomEnd = 8.dp))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Search Bar Area
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.setQuery(it) },
                        placeholder = { Text("بحث باسم المهني أو الخدمة...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "بحث") },
                        textStyle = LocalTextStyle.current.copy(color = ThemeManager.getFontColor(config)),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("home_search_input")
                    )

                    // Voice search button
                    IconButton(
                        onClick = { viewModel.triggerVoiceRecognition(context) },
                        modifier = Modifier
                            .background(
                                if (isVoiceSearching) Color.Red else MaterialTheme.colorScheme.primaryContainer,
                                CircleShape
                            )
                            .testTag("btn_voice_search")
                    ) {
                        Icon(
                            if (isVoiceSearching) Icons.Default.MicOff else Icons.Default.Mic,
                            contentDescription = "بحث صوتي",
                            tint = if (isVoiceSearching) Color.White else MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    // Advanced filters button
                    IconButton(
                        onClick = { showFilterSheet = !showFilterSheet },
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(8.dp))
                            .testTag("btn_advance_filters")
                    ) {
                        Icon(Icons.Default.Tune, contentDescription = "تصفية متقدمة")
                    }
                }

                // Expanded Advance Filters view
                if (showFilterSheet) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("📍 تصفية متقدمة حسب المنطقة والتفاصيل", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = nameQuery,
                        onValueChange = { viewModel.setNameQuery(it) },
                        label = { Text("الاسم الثلاثي") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = phoneQuery,
                        onValueChange = { viewModel.setPhoneQuery(it) },
                        label = { Text("رقم الهاتف") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Cities Select list
                    val cities = config?.citiesListJson?.split(",") ?: listOf("صنعاء", "عدن", "تعز")
                    Text("المدينة / المحافظة:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        item {
                            FilterChip(
                                selected = selectedCity == null,
                                onClick = { viewModel.selectCity(null) },
                                label = { Text("الكل") }
                            )
                        }
                        items(cities) { city ->
                            FilterChip(
                                selected = selectedCity == city,
                                onClick = { viewModel.selectCity(city) },
                                label = { Text(city) }
                            )
                        }
                    }
                }
            }
        }

        // Emojied primary categories list slider
        item {
            Text(
                "📂 تصفح حسب المجموعات",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = ThemeManager.getFontColor(config)
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    FilterChip(
                        selected = selectedCategory == null,
                        onClick = { viewModel.selectCategory(null) },
                        label = { Text("الكل") }
                    )
                }
                items(categories) { cat ->
                    FilterChip(
                        selected = selectedCategory?.id == cat.id,
                        onClick = { viewModel.selectCategory(cat) },
                        label = { Text("${cat.icon} ${cat.nameAr}") }
                    )
                }
            }
        }

        // Recommended Carousel (Mousa behm)
        if (recommendedList.isNotEmpty() && selectedCategory == null && searchQuery.isEmpty()) {
            item {
                Text(
                    "⭐ الخدمات الموصى بها",
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp,
                    color = ThemeManager.getFontColor(config)
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(recommendedList) { p ->
                        Card(
                            onClick = { viewModel.navigateTo(AppScreen.PROVIDER_DETAIL, p.id) },
                            border = BorderStroke(1.dp, Color(0xFFF3E5AB)),
                            modifier = Modifier
                                .width(200.dp)
                                .testTag("card_rec_${p.id}")
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Image(
                                    painter = rememberAsyncImagePainter(p.profileImage),
                                    contentDescription = p.name,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    p.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    p.address,
                                    fontSize = 10.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(Icons.Default.Star, "موصى به", tint = Color(0xFFD4AF37), modifier = Modifier.size(14.dp))
                                    Text(" ${p.rating}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Main Service Provider List
        item {
            Text(
                "💼 مزودي الخدمات المتاحة (${filteredProviders.size})",
                fontWeight = FontWeight.Black,
                fontSize = 16.sp,
                color = ThemeManager.getFontColor(config)
            )
        }

        if (filteredProviders.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🔍", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "لا توجد نتائج مطابقة لبحثك في هذا النطاق",
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        } else {
            items(filteredProviders) { p ->
                Card(
                    onClick = { viewModel.navigateTo(AppScreen.PROVIDER_DETAIL, p.id) },
                    border = if (p.isPinned) BorderStroke(2.dp, Color(0xFFD4AF37)) else null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("provider_card_${p.id}")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(p.profileImage),
                            contentDescription = p.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(70.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    p.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = ThemeManager.getFontColor(config)
                                )
                                if (p.isVerified) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("☑️", color = Color(0xFF1DA1F2), fontSize = 13.sp)
                                }
                                if (p.hasPremiumBadge) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("👑", fontSize = 13.sp)
                                }
                            }
                            Text(p.address, fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
                            Text("📱 ${p.phone}", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Star, "موصى به", tint = Color(0xFFD4AF37), modifier = Modifier.size(16.dp))
                                Text(" ${p.rating}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            if (p.isPinned) {
                                Badge(containerColor = Color(0xFFD4AF37)) {
                                    Text("مُثبت 📌", color = Color.Black, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Gap spacing at footer
        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

@Composable
fun ProviderDetailScreen(viewModel: AppViewModel) {
    val context = LocalContext.current
    val providers by viewModel.serviceProviders.collectAsState()
    val pid by viewModel.selectedProviderId.collectAsState()
    val p = remember(providers, pid) { providers.find { it.id == pid } }
    val reviewsList by viewModel.reviews.collectAsState()
    val provReviews = remember(reviewsList, pid) { reviewsList.filter { it.providerId == pid } }
    val config by viewModel.appConfig.collectAsState()

    var userRatingInput by remember { mutableStateOf(5) }
    var userCommentInput by remember { mutableStateOf("") }
    var userNameInput by remember { mutableStateOf("") }

    var reportReason by remember { mutableStateOf("") }
    var showReportDialog by remember { mutableStateOf(false) }

    var chatMessageText by remember { mutableStateOf("") }
    val chatsList by viewModel.chats.collectAsState()
    val provChats = remember(chatsList, pid) { chatsList.filter { it.providerId == pid } }

    if (p == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("مقدم الخدمة غير متوفر حالياً")
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("provider_detail_scroll"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Back Button
        item {
            OutlinedButton(
                onClick = { viewModel.navigateTo(AppScreen.HOME) },
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("⬅️ عودة للدليل الرئيسي")
            }
        }

        // Profile Card Heading
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(p.profileImage),
                        contentDescription = p.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(110.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            p.name,
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp,
                            color = ThemeManager.getFontColor(config)
                        )
                        if (p.isVerified) {
                            Text(" ☑️", color = Color(0xFF1DA1F2), fontSize = 16.sp)
                        }
                    }

                    Text("سكن وعمل: ${p.address} | ${p.district}", fontSize = 13.sp, color = MaterialTheme.colorScheme.secondary)
                    Spacer(modifier = Modifier.height(16.dp))

                    // Buttons Call / Whatsapp / Share side by side
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${p.phone}"))
                                context.startActivity(intent)
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("📞 اتصل الآن")
                        }
                        Button(
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/967${p.phone}"))
                                context.startActivity(intent)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("💬 واتساب")
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Share Action Button
                        OutlinedButton(
                            onClick = {
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(
                                        Intent.EXTRA_TEXT,
                                        "مقدم خدمة متميز في دليل وموسوعة اليمن للخدمات:\n" +
                                                "الاسم: ${p.name}\n" +
                                                "التخصص: ${p.address}\n" +
                                                "الهاتف: ${p.phone}\n" +
                                                "رابط تحميل التطبيق: ${config?.downloadUrl ?: "https://services-app.yemen"}"
                                    )
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "مشاركة المهني مع صديق"))
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("🔗 مشاركة الكرت")
                        }

                        // Report Button Action
                        OutlinedButton(
                            onClick = { showReportDialog = true },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("🚨 إبلاغ عن محتوى")
                        }
                    }
                }
            }
        }

        // Direct real-time Chat bubble history
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("💬 دردشة وتواصل فوري متزامن مع ${p.name}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))

                    // Chat scroll area
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .background(MaterialTheme.colorScheme.background, RoundedCornerShape(8.dp))
                            .verticalScroll(rememberScrollState())
                            .padding(8.dp)
                    ) {
                        if (provChats.isEmpty()) {
                            Text(
                                "لا توجد رسائل بينكما. أرسل استفسارك الأول ليبدأ التزامن اللحظي للدردشة.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.secondary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                for (chat in provChats) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = if (chat.senderName == "أنا") Arrangement.End else Arrangement.Start
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .background(
                                                    if (chat.senderName == "أنا") MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                                    else MaterialTheme.colorScheme.surfaceVariant,
                                                    RoundedCornerShape(8.dp)
                                                )
                                                .padding(8.dp)
                                        ) {
                                            Text("${chat.senderName}: ${chat.message}", fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Message input field
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = chatMessageText,
                            onValueChange = { chatMessageText = it },
                            placeholder = { Text("اكتب رسالة...") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = {
                                if (chatMessageText.isNotEmpty()) {
                                    viewModel.sendMessage(p.id, "أنا", chatMessageText)
                                    chatMessageText = ""
                                }
                            }
                        ) {
                            Icon(Icons.Default.Send, "إرسال")
                        }
                    }
                }
            }
        }

        // Rating and reviews List view
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("✍️ تعليقات وتقييم المهني", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(12.dp))

                    // Stars rating builder
                    Text("تقييمك الشخصي بالنجوم:", fontSize = 12.sp)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        for (i in 1..5) {
                            Icon(
                                if (i <= userRatingInput) Icons.Default.Star else Icons.Outlined.Star,
                                contentDescription = "$i star",
                                tint = if (i <= userRatingInput) Color(0xFFD4AF37) else Color.Gray,
                                modifier = Modifier
                                    .size(28.dp)
                                    .clickable { userRatingInput = i }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = userNameInput,
                        onValueChange = { userNameInput = it },
                        placeholder = { Text("اسمك الكريم الكامل") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = userCommentInput,
                        onValueChange = { userCommentInput = it },
                        placeholder = { Text("أكتب تعليقك وتجربتك المهنية هنا بكل أمانة...") },
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            if (userNameInput.isNotEmpty() && userCommentInput.isNotEmpty()) {
                                viewModel.addReview(p.id, userRatingInput, userCommentInput, userNameInput)
                                userCommentInput = ""
                                userNameInput = ""
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("تسجيل ونشر التقييم")
                    }
                }
            }
        }

        // Reviews Render
        item {
            Text("المراجعات السابقة (${provReviews.size}):", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }

        if (provReviews.isEmpty()) {
            item {
                Text("لا توجد مراجعات سابقة لهذا المهني. كن أول من يقيم!", color = MaterialTheme.colorScheme.secondary, fontSize = 12.sp)
            }
        } else {
            items(provReviews) { r ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(r.reviewerName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Row {
                                for (i in 1..5) {
                                    Icon(
                                        Icons.Default.Star,
                                        contentDescription = "stars",
                                        tint = if (i <= r.rating) Color(0xFFD4AF37) else Color.LightGray,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(r.comment, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        }

        // Bottom spacer
        item { Spacer(modifier = Modifier.height(30.dp)) }
    }

    // Report Dialog
    if (showReportDialog) {
        Dialog(onDismissRequest = { showReportDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("🚩 الإبلاغ عن مقدم الخدمة", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("يرجى شرح سبب الإبلاغ عن المهني (مثال: محتوى غير لائق، نصب، جودة مضللة):", fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = reportReason,
                        onValueChange = { reportReason = it },
                        placeholder = { Text("أكتب بالتفاصيل سبب البلاغ...") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 4
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(onClick = { showReportDialog = false }, modifier = Modifier.weight(1f)) {
                            Text("تراجع")
                        }
                        Button(
                            onClick = {
                                if (reportReason.isNotEmpty()) {
                                    viewModel.fileReport(p.id, p.name, "مستخدم مجهول", reportReason, context)
                                    showReportDialog = false
                                    reportReason = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("إرسال البلاغ")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RegisterProfessionalScreen(viewModel: AppViewModel) {
    val context = LocalContext.current
    val categories by viewModel.categories.collectAsState()
    val primaryCats = remember(categories) { categories.filter { it.parentId == null } }

    var fullName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var selectedCatId by remember { mutableStateOf("") }
    var locationAddress by remember { mutableStateOf("") }
    var residentialDistrict by remember { mutableStateOf("") }
    var gpsCoordinates by remember { mutableStateOf("15.3089,44.2056") }
    var profileImageMockUrl by remember { mutableStateOf("https://images.unsplash.com/photo-1540569014015-19a7be504e3a?w=400") }
    var idCardImageMockUrl by remember { mutableStateOf("") }

    // Init select
    LaunchedEffect(primaryCats) {
        if (primaryCats.isNotEmpty() && selectedCatId.isEmpty()) {
            selectedCatId = primaryCats.first().id
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("prof_register_scroll"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                "👤 استمارة تسجيل الكوادر والمهنيين الجدد",
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Black,
                fontSize = 18.sp
            )
            Text(
                "طلب الانضمام للمراجعة الفورية والتدقيق الأمني.",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.secondary
            )
        }

        item {
            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = { Text("الاسم الثلاثي الكامل (إجباري) *") },
                placeholder = { Text("مثال: ماهر محمد طاهر") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            OutlinedTextField(
                value = phoneNumber,
                onValueChange = { phoneNumber = it },
                label = { Text("رقم الهاتف المتكامل الفعال / واتساب (إجباري) *") },
                placeholder = { Text("مثال: 777644670") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            Text("القسم والخدمة الرئيسية (إجباري):", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            // Listing correct categories allowing professionals to select they industry perfectly!
            // No longer stuck on electrician
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                primaryCats.forEach { cat ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedCatId = cat.id }
                            .padding(8.dp)
                    ) {
                        RadioButton(
                            selected = (selectedCatId == cat.id),
                            onClick = { selectedCatId = cat.id }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("${cat.icon} ${cat.nameAr}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }

        item {
            OutlinedTextField(
                value = locationAddress,
                onValueChange = { locationAddress = it },
                label = { Text("عنوان مركز ومكتب العمل الحالي بالتفصيل *") },
                placeholder = { Text("مثال: شارع حدة - صنعاء") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            OutlinedTextField(
                value = residentialDistrict,
                onValueChange = { residentialDistrict = it },
                label = { Text("منطقة الدائرة السكنية والمديرية الحالية *") },
                placeholder = { Text("مثال: مديرية السبعين") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            OutlinedTextField(
                value = gpsCoordinates,
                onValueChange = { gpsCoordinates = it },
                label = { Text("إحداثيات وموقع الخريطة GPS (اختياري)") },
                placeholder = { Text("مثال: 15.3089,44.2056") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Simulated Image pickers
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("📁 صور الهوية والملف الشخصي:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = {
                                profileImageMockUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=400"
                                Toast.makeText(context, "تم تحميل الصورة الشخصية بنجاح!", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Text("رفع صورة شخصية *")
                        }
                        Text(
                            text = if (profileImageMockUrl.isNotEmpty()) "تم الرفع بنجاح ✅" else "لم يرفع بعد",
                            fontSize = 11.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = {
                                idCardImageMockUrl = "https://images.unsplash.com/photo-1554774853-aae0a22c8aa4?w=400"
                                Toast.makeText(context, "تم تحميل بطاقة الهوية بنجاح!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            Text("رفع بطاقة الهوية (اختياري)")
                        }
                        Text(
                            text = if (idCardImageMockUrl.isNotEmpty()) "تم الرفع بنجاح ✅" else "لم يرفع بعد",
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        item {
            Button(
                onClick = {
                    if (fullName.isNotEmpty() && phoneNumber.isNotEmpty() && locationAddress.isNotEmpty() && residentialDistrict.isNotEmpty()) {
                        viewModel.submitProfessionalRegistration(
                            fullName,
                            phoneNumber,
                            selectedCatId,
                            locationAddress,
                            residentialDistrict,
                            gpsCoordinates,
                            profileImageMockUrl,
                            if (idCardImageMockUrl.isEmpty()) null else idCardImageMockUrl,
                            context
                        )
                    } else {
                        Toast.makeText(context, "الرجاء تعبئة كافة الحقول الإجبارية الموشومة بالنجمة *", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("btn_submit_registration")
            ) {
                Text("تقديم طلب الانضمام للمراجعة الفورية", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun LoginScreen(viewModel: AppViewModel, config: AppConfig?) {
    val context = LocalContext.current
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var user2faCode by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf("") }
    val is2faEnabled = config?.is2faEnabled ?: false

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .testTag("admin_login_layout"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("🔐 تسجيل دخول لوحة التحكم", fontWeight = FontWeight.Black, fontSize = 22.sp)
        Text("هذه اللوحة مخصصة للأعمال الإدارية والمشرفين ومراجعة الطلبات.", fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("اسم المستخدم") },
            placeholder = { Text("مثال: WAM2026") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("كلمة المرور") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        if (is2faEnabled) {
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = user2faCode,
                onValueChange = { user2faCode = it },
                label = { Text("رمز التحقق بخطوتين (2FA)") },
                placeholder = { Text("أدخل رمز Authenticator") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (errorMsg.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(errorMsg, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (is2faEnabled && user2faCode.isEmpty()) {
                    errorMsg = "الرجاء إدخال رمز التحقق بخطوتين (2FA) للموثوقية"
                } else {
                    if (viewModel.performAdminLogin(username, password, config)) {
                        Toast.makeText(context, "تم الدخول بنجاح للمشرف!", Toast.LENGTH_SHORT).show()
                    } else {
                        errorMsg = "خطأ في اسم المستخدم أو كلمة المرور!"
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("btn_perform_login")
        ) {
            Text("دخول للوحة التحكم")
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = { viewModel.navigateTo(AppScreen.HOME) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("إلغاء والعودة للدليل")
        }
    }
}

@Composable
fun AdminDashboardScreen(viewModel: AppViewModel) {
    val context = LocalContext.current
    val pendingList by viewModel.pendingProviders.collectAsState()
    val allProviders by viewModel.serviceProviders.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val reportsList by viewModel.reports.collectAsState()

    var activeTab by remember { mutableStateOf(0) } // 0: Pending, 1: Categories, 2: Direct Add, 3: Active Providers

    Column(modifier = Modifier.fillMaxSize()) {
        // Tab Headers
        TabRow(selectedTabIndex = activeTab) {
            Tab(selected = activeTab == 0, onClick = { activeTab = 0 }) {
                Text("طلبات (${pendingList.size})", modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
            Tab(selected = activeTab == 1, onClick = { activeTab = 1 }) {
                Text("الأقسام", modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
            Tab(selected = activeTab == 2, onClick = { activeTab = 2 }) {
                Text("إضافة يدوية", modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
            Tab(selected = activeTab == 3, onClick = { activeTab = 3 }) {
                Text("المهنيين", modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }

        // Tab Contents
        Box(
            modifier = Modifier
                .weight(1f)
                .padding(16.dp)
        ) {
            when (activeTab) {
                0 -> PendingRequestsTab(viewModel = viewModel, pendingList = pendingList)
                1 -> ManageCategoriesTab(viewModel = viewModel, categories = categories)
                2 -> DirectAddProviderTab(viewModel = viewModel, categories = categories)
                3 -> ActiveProvidersTab(viewModel = viewModel, providers = allProviders)
            }
        }

        // Administrative Global logout button requested: "قم بإنشاء زر الخروج من داخل لوحة التحكم"
        Button(
            onClick = { viewModel.logout() },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("btn_admin_logout")
        ) {
            Text("🚪 تسجيل الخروج العادل والعودة للدليل", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun PendingRequestsTab(viewModel: AppViewModel, pendingList: List<PendingProvider>) {
    val context = LocalContext.current
    if (pendingList.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("لا توجد طلبات معلقة للمراجعة في الوقت الحالي 👍")
        }
        return
    }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(pendingList) { req ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("pending_request_card_${req.id}")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = rememberAsyncImagePainter(req.profileImage),
                            contentDescription = "الملف الشخصي",
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(req.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text("القسم: ${req.categoryId} | الهاتف: ${req.phone}", fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("سكن: ${req.district} | العنوان: ${req.address}", fontSize = 12.sp)
                    Text("جغرافي GPS: ${req.locationGPS}", fontSize = 11.sp, color = Color.Gray)

                    // Zoomable Document card view simulation
                    if (req.idCardImage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Card(border = BorderStroke(1.dp, Color.Gray)) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp)
                                    .clickable {
                                        Toast
                                            .makeText(context, "معاينة مكبرة لبطاقة هوية المهني: ${req.name}", Toast.LENGTH_SHORT)
                                            .show()
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = rememberAsyncImagePainter(req.idCardImage),
                                    contentDescription = "بطاقة الهوية لـ ${req.name}",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                                Text(
                                    "صورة الهوية الوطنية (انقر للتكبير 🔍)",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    modifier = Modifier
                                        .background(Color.Black.copy(alpha = 0.5f))
                                        .padding(4.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.rejectRequest(req.id, "لم يستوفِ الشروط الأمنية لبلدية صنعاء") },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("رفض الطلب ❌")
                        }
                        Button(
                            onClick = { viewModel.approveRequest(req.id) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("قبول الطلب ✅")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ManageCategoriesTab(viewModel: AppViewModel, categories: List<Category>) {
    val context = LocalContext.current
    var catNameAr by remember { mutableStateOf("") }
    var catIcon by remember { mutableStateOf("🔧") }
    var displayOrder by remember { mutableStateOf(1) }

    val mainCategories = remember(categories) { categories.filter { it.parentId == null } }

    Column(modifier = Modifier.fillMaxSize()) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("إضافة قسم رئيسي جديد لليمن:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = catNameAr,
                    onValueChange = { catNameAr = it },
                    label = { Text("اسم القسم بالعريية") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = catIcon,
                        onValueChange = { catIcon = it },
                        label = { Text("أيقونة/رمز") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = displayOrder.toString(),
                        onValueChange = { displayOrder = it.toIntOrNull() ?: 1 },
                        label = { Text("ترتيب الظهور") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        if (catNameAr.isNotEmpty()) {
                            val newCat = Category(
                                id = "cat_" + System.currentTimeMillis(),
                                nameAr = catNameAr,
                                nameEn = catNameAr,
                                parentId = null,
                                icon = catIcon,
                                displayOrder = displayOrder
                            )
                            viewModel.addCategory(newCat)
                            Toast.makeText(context, "تم حفظ القسم بنجاح وهيكلته بمزامنة فورية!", Toast.LENGTH_SHORT).show()
                            catNameAr = ""
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("إضافة القسم للسبورة")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("الأقسام النشطة حالياً:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(mainCategories) { cat ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("${cat.icon} ${cat.nameAr} (ترتيب: ${cat.displayOrder})", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun DirectAddProviderTab(viewModel: AppViewModel, categories: List<Category>) {
    val context = LocalContext.current
    val primaryCats = remember(categories) { categories.filter { it.parentId == null } }

    var dName by remember { mutableStateOf("") }
    var dPhone by remember { mutableStateOf("") }
    var dAddress by remember { mutableStateOf("") }
    var dDistrict by remember { mutableStateOf("") }
    var dSelectedCatId by remember { mutableStateOf("") }

    LaunchedEffect(primaryCats) {
        if (primaryCats.isNotEmpty() && dSelectedCatId.isEmpty()) {
            dSelectedCatId = primaryCats.first().id
        }
    }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Text("⚡ إضافة مهني فوري وشامل (يدوي للآدمن):", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text("هذا النموذج مبسط يتجاوز تدقيق ورفض المشرفين ويظهر مباشرة بالدليل المفتوح.", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
        }

        item {
            OutlinedTextField(
                value = dName,
                onValueChange = { dName = it },
                label = { Text("الاسم الثنائي/الثلاثي لمهنينا") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            OutlinedTextField(
                value = dPhone,
                onValueChange = { dPhone = it },
                label = { Text("رقم الهاتف اليمني") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            OutlinedTextField(
                value = dAddress,
                onValueChange = { dAddress = it },
                label = { Text("العنوان العام (مثال: المنصورة - عدن)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            OutlinedTextField(
                value = dDistrict,
                onValueChange = { dDistrict = it },
                label = { Text("المديرية والحي الفرعي") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            Text("القسم المهني:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(primaryCats) { cat ->
                    FilterChip(
                        selected = dSelectedCatId == cat.id,
                        onClick = { dSelectedCatId = cat.id },
                        label = { Text("${cat.icon} ${cat.nameAr}") }
                    )
                }
            }
        }

        item {
            Button(
                onClick = {
                    if (dName.isNotEmpty() && dPhone.isNotEmpty() && dAddress.isNotEmpty()) {
                        viewModel.submitDirectProvider(
                            dName, dPhone, dSelectedCatId, dAddress, dDistrict,
                            "https://images.unsplash.com/photo-1540569014015-19a7be504e3a?w=400"
                        )
                        Toast.makeText(context, "تم حفظ ونشر المهني يدوياً بنجاح!", Toast.LENGTH_SHORT).show()
                        dName = ""
                        dPhone = ""
                        dAddress = ""
                        dDistrict = ""
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("نشر المهني للجمهور")
            }
        }
    }
}

@Composable
fun ActiveProvidersTab(viewModel: AppViewModel, providers: List<ServiceProvider>) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(providers) { p ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(p.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("تلفون: ${p.phone} | ${p.address}", fontSize = 11.sp)

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Recommend Button
                        Button(
                            onClick = { viewModel.toggleRecommendProvider(p) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (p.isRecommended) Color(0xFFD4AF37) else MaterialTheme.colorScheme.secondaryContainer
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(if (p.isRecommended) "موصى به ⭐" else "توصية")
                        }

                        // Pin Button
                        Button(
                            onClick = { viewModel.togglePinProvider(p) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (p.isPinned) Color(0xFFE2E8F0) else MaterialTheme.colorScheme.secondaryContainer
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(if (p.isPinned) "مثبت 📌" else "تثبيت")
                        }

                        // Verify blue badge button
                        Button(
                            onClick = { viewModel.toggleVerifyProvider(p) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (p.isVerified) Color(0xFF1DA1F2) else MaterialTheme.colorScheme.secondaryContainer
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(if (p.isVerified) "حساب موثق ☑️" else "توثيق")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BackdoorDashboardScreen(viewModel: AppViewModel, config: AppConfig?) {
    val context = LocalContext.current
    var appNameVal by remember { mutableStateOf(config?.appName ?: "") }
    var welcomeMsgVal by remember { mutableStateOf(config?.welcomeMessage ?: "") }
    var promoVal by remember { mutableStateOf(config?.promotionalFooter ?: "") }
    var sPhoneVal by remember { mutableStateOf(config?.supportPhone ?: "") }
    var sWhatsappVal by remember { mutableStateOf(config?.supportWhatsapp ?: "") }
    var sEmailVal by remember { mutableStateOf(config?.supportEmail ?: "") }
    var sAdminPassVal by remember { mutableStateOf(config?.adminPassword ?: "") }

    var backdoorTheme by remember { mutableStateOf(config?.themeName ?: "COSMIC_SLATE") }
    var backdoorFontColor by remember { mutableStateOf(config?.fontColorName ?: "BRIGHT_WHITE") }
    var dataSaverMock by remember { mutableStateOf(config?.dataSaverMode ?: false) }
    var maintenanceModeState by remember { mutableStateOf(config?.isMaintenanceMode ?: false) }

    val reportsList by viewModel.reports.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("backdoor_scroll"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                "👑 الإعدادات السيادية (بوابة المالك)",
                fontWeight = FontWeight.Black,
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                "مرحباً بك يا ماهر. أنت تمتلك كامل صلاحيات تعديل البصمة الوراثية وتذييل وواجهة هذا التطبيق.",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.secondary
            )
        }

        // Banners & Promotional settings
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("💡 تغيير هوية واسم التطبيق الأساسي", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = appNameVal,
                        onValueChange = { appNameVal = it },
                        label = { Text("اسم التطبيق الرئيسي") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = welcomeMsgVal,
                        onValueChange = { welcomeMsgVal = it },
                        label = { Text("رسالة ترحيب الدليل") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = promoVal,
                        onValueChange = { promoVal = it },
                        label = { Text("التذييل الدعائي المخصص") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Color & Theme Choices Dashboard
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("🎨 الأنماط والألوان الفاخرة للواجهة", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(8.dp))

                    Text("تحديد نمط الألوان الكلي (مزامنة فورية):", fontSize = 12.sp)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Button(
                            onClick = { backdoorTheme = "COSMIC_SLATE" },
                            colors = ButtonDefaults.buttonColors(containerColor = if (backdoorTheme == "COSMIC_SLATE") Color(0xFFE2E8F0) else Color.Gray)
                        ) {
                            Text("🌌 كوزميك", fontSize = 11.sp, color = Color.Black)
                        }
                        Button(
                            onClick = { backdoorTheme = "CHARCOAL_GOLD" },
                            colors = ButtonDefaults.buttonColors(containerColor = if (backdoorTheme == "CHARCOAL_GOLD") Color(0xFFD4AF37) else Color.Gray)
                        ) {
                            Text("✨ ذهبي", fontSize = 11.sp, color = Color.Black)
                        }
                        Button(
                            onClick = { backdoorTheme = "ROYAL_EMERALD" },
                            colors = ButtonDefaults.buttonColors(containerColor = if (backdoorTheme == "ROYAL_EMERALD") Color(0xFF10B981) else Color.Gray)
                        ) {
                            Text("🟢 زمردي", fontSize = 11.sp, color = Color.Black)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text("تحديد لون خطوط التطبيق والكتابة في الحقول:", fontSize = 12.sp)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Button(
                            onClick = { backdoorFontColor = "BRIGHT_WHITE" },
                            colors = ButtonDefaults.buttonColors(containerColor = if (backdoorFontColor == "BRIGHT_WHITE") Color.White else Color.Gray)
                        ) {
                            Text("◽ أبيض ناصل", fontSize = 11.sp, color = Color.Black)
                        }
                        Button(
                            onClick = { backdoorFontColor = "LIGHT_GOLD" },
                            colors = ButtonDefaults.buttonColors(containerColor = if (backdoorFontColor == "LIGHT_GOLD") Color(0xFFFFF1C5) else Color.Gray)
                        ) {
                            Text("🟡 ذهبي فاتح", fontSize = 11.sp, color = Color.Black)
                        }
                        Button(
                            onClick = { backdoorFontColor = "VIBRANT_SILVER" },
                            colors = ButtonDefaults.buttonColors(containerColor = if (backdoorFontColor == "VIBRANT_SILVER") Color(0xFFD1D5DB) else Color.Gray)
                        ) {
                            Text("◽ فضي متوهج", fontSize = 11.sp, color = Color.Black)
                        }
                    }
                }
            }
        }

        // Support Coordinates and Password Manager
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("📞 أرقام وإيميلات الدعم والسيطرة", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = sPhoneVal,
                        onValueChange = { sPhoneVal = it },
                        label = { Text("رقم هاتف الدعم") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = sWhatsappVal,
                        onValueChange = { sWhatsappVal = it },
                        label = { Text("رقم واتساب الدعم") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = sEmailVal,
                        onValueChange = { sEmailVal = it },
                        label = { Text("بريد الدعم") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("🔒 تغيير كلمة مرور المدير المشرف الرئيسي (WAM2026):", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = sAdminPassVal,
                        onValueChange = { sAdminPassVal = it },
                        label = { Text("كلمة مرور المشرف الحالية") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // System Toggles & DB Actions
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("⚙️ وظائف سيادية ومحاكاة البيانات والتوقيت", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(12.dp))

                    // Maintenance mode switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("فعيل وضع الصيانة العام (صيانة التطبيق) 🛠️", fontSize = 12.sp)
                        Switch(
                            checked = maintenanceModeState,
                            onCheckedChange = { maintenanceModeState = it }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Data saver mode
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("وضع توفير البيانات (تقليل الرزولوشن) 📉", fontSize = 12.sp)
                        Switch(
                            checked = dataSaverMock,
                            onCheckedChange = { dataSaverMock = it }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // DB export trigger buttons
                    Button(
                        onClick = { viewModel.triggerDatabaseBackup(context) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("💾 نسخ احتياطي فوري لقاعدة البيانات (للذاكرة)")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = { viewModel.triggerScheduledTasks(context) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Text("🧹 جدولة وتنظيف المحاضرات وسجلات الكاش")
                    }
                }
            }
        }

        // Reports View dashboard (Managed exclusively from Backdoor for secure complaints audit)
        item {
            Text("🚨 البلاغات والتقارير الأمنية عن الكوادر المزعجة (${reportsList.size}):", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }

        if (reportsList.isEmpty()) {
            item {
                Text("لا توجد بلاغات مسجلة حالياً. سجلاتك نظيفة وآمنة 👍", fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
            }
        } else {
            items(reportsList) { rep ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("إرسال من: ${rep.reporterName} عن المهني: ${rep.providerName}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("السبب: ${rep.reason}", fontSize = 12.sp)

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = { viewModel.resolveReport(rep.id) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("حظر وإلغاء حساب المهني / تسوية البلاغ ❌")
                        }
                    }
                }
            }
        }

        // Global save changes button
        item {
            Button(
                onClick = {
                    val finalConfig = AppConfig(
                        appName = appNameVal,
                        welcomeMessage = welcomeMsgVal,
                        promotionalFooter = promoVal,
                        supportPhone = sPhoneVal,
                        supportWhatsapp = sWhatsappVal,
                        supportEmail = sEmailVal,
                        adminPassword = sAdminPassVal,
                        themeName = backdoorTheme,
                        fontColorName = backdoorFontColor,
                        dataSaverMode = dataSaverMock,
                        isMaintenanceMode = maintenanceModeState
                    )
                    viewModel.editAppConfig(finalConfig)
                    Toast.makeText(context, "تم حفظ الإعدادات السيادية وتدشينها للمزامنة!", Toast.LENGTH_SHORT).show()
                    viewModel.navigateTo(AppScreen.HOME)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("btn_save_backdoor_ Sovereign")
            ) {
                Text("تثبيت وتزامن التغييرات على جميع الأجهزة", fontWeight = FontWeight.Black)
            }
        }

        item { Spacer(modifier = Modifier.height(30.dp)) }
    }
}

@Composable
fun MaintenanceScreen(viewModel: AppViewModel, config: AppConfig?) {
    val context = LocalContext.current
    var inputPass by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("🛠️ وضع الصيانة المؤقت", color = Color.White, fontWeight = FontWeight.Black, fontSize = 24.sp)
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = config?.welcomeMessage ?: "التطبيق قيد التحديثات الجوهرية والبرمجة الآن. سنعود قريباً جداً!",
            color = Color.White.copy(alpha = 0.8f),
            textAlign = TextAlign.Center,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("للدعم والاستفسارات العاجلة:", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text("الهاتف: ${config?.supportPhone ?: "777644670"}", color = Color.Yellow, fontSize = 12.sp)
                Text("الواتساب: ${config?.supportWhatsapp ?: "777644670"}", color = Color.Green, fontSize = 12.sp)
                Text("البريد: ${config?.supportEmail ?: "support@mawdev.com"}", color = Color.White, fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Bypass backdoor lock for emergency admin tasks
        OutlinedTextField(
            value = inputPass,
            onValueChange = { inputPass = it },
            placeholder = { Text("رمز التخطي لصالح المالك", color = Color.LightGray) },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            textStyle = LocalTextStyle.current.copy(color = Color.White)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                if (viewModel.unlockBackdoor(inputPass)) {
                    Toast.makeText(context, "باي باس المالك مفعل", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "رمز تجاوز المالك خاطئ", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("تجاوز حظر الصيانة")
        }
    }
}

@Composable
fun AboutAppDialog(config: AppConfig?, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "ℹ️ حول تطبيق دليل الخدمات",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "دليل وموسوعة الخدمات والمهن والكوادر الأفضل في لبلديات اليمن الكبرى. يوفر الدليل الاتصال الفوري المباشر مع الحرفيين، ويدعم مزامنة فورية كاملة لقاعدة بيانات السيرفر الحية.",
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Divider()

                Spacer(modifier = Modifier.height(12.dp))
                Text("للتواصل والدعم الفني وإضافة كادر:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Text("📞 هاتف: ${config?.supportPhone ?: "777644670"}", fontSize = 12.sp)
                Text("💬 واتساب: ${config?.supportWhatsapp ?: "777644670"}", fontSize = 12.sp)
                Text("✉️ بريد: ${config?.supportEmail ?: "support@mawdev.com"}", fontSize = 12.sp)

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("إغلاق نافذة الدعم")
                }
            }
        }
    }
}

@Composable
fun SmartAssistantDialog(viewModel: AppViewModel, onDismiss: () -> Unit) {
    var queryText by remember { mutableStateOf("") }
    val assistantReplies = remember { mutableStateListOf<String>() }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "🤖 المساعد الذكي لموسوعة الخدمات",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Chat history
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        item {
                            Text("المساعد: أهلاً بك! كيف يمكنني مساعدتك في استكشاف هذا الدليل الفاخر اليوم؟ كهربائي؟ مدرس؟ طبيب؟", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        items(assistantReplies) { text ->
                            Text(text, fontSize = 11.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = queryText,
                    onValueChange = { queryText = it },
                    placeholder = { Text("اطرح استفسارك الآلي...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text("إغلاق")
                    }
                    Button(
                        onClick = {
                            if (queryText.isNotEmpty()) {
                                assistantReplies.add("أنت: $queryText")
                                val reply = when {
                                    queryText.contains("كهربائي") || queryText.contains("كهرباء") -> "المساعد: لدينا المهندس ماهر محمد طاهر في شارع حدة بصنعاء وهو موثق وموصى به لجميع أعمالك!"
                                    queryText.contains("طبيب") || queryText.contains("دكتور") -> "المساعد: ننصحك بالدكتور ياسين محمود السامعي في شارع جمال بتعز، تقييمه 5.0 من أصل 19 تعليق!"
                                    else -> "المساعد: تم استلام استفسارك! يمكنك تصفح الدليل الرئيسي أو البحث باسم المهني لإيجاد غايتك فوراً."
                                }
                                assistantReplies.add(reply)
                                queryText = ""
                            }
                        },
                        modifier = Modifier.weight(1.5f)
                    ) {
                        Text("استفسار ذكي")
                    }
                }
            }
        }
    }
}
