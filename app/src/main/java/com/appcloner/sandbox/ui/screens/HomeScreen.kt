package com.appcloner.sandbox.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.appcloner.sandbox.core.ShortcutHelper
import com.appcloner.sandbox.data.model.HomeScreenAppItem
import com.appcloner.sandbox.data.model.InstalledApp
import com.appcloner.sandbox.data.repository.AppRepository
import com.appcloner.sandbox.ui.components.*
import com.appcloner.sandbox.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(repository: AppRepository) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var selectedTab by remember { mutableStateOf(0) } // 0: Home Screen Desktop, 1: Add Apps
    var searchQuery by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }

    var homeScreenItems by remember { mutableStateOf<List<HomeScreenAppItem>>(emptyList()) }
    var installedApps by remember { mutableStateOf<List<InstalledApp>>(emptyList()) }
    var selectedActionItem by remember { mutableStateOf<HomeScreenAppItem?>(null) }

    fun refreshData() {
        scope.launch {
            isLoading = true
            try {
                homeScreenItems = repository.getHomeScreenClonedItems()
                installedApps = repository.getInstalledDeviceApps()
            } catch (e: Exception) {
                Toast.makeText(context, "خطا در بارگذاری: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        refreshData()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(PrimaryIndigo),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Apps,
                                contentDescription = null,
                                tint = TextPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "صفحه هوم برنامه‌ها (کلونر)",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "${homeScreenItems.size} برنامه فعال روی صفحه",
                                fontSize = 11.sp,
                                color = AccentCyan
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { refreshData() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "بروزرسانی",
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
            )
        },
        containerColor = DarkBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 14.dp)
        ) {
            // بنر بررسی و تنظیم دسترسی‌های سیستمی
            PermissionCheckBanner(onDismissBanner = {})

            // تب‌های ناوبری
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = DarkBackground,
                contentColor = PrimaryIndigo,
                divider = { Divider(color = CardBorder) }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Home, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "صفحه هوم",
                                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == 0) TextPrimary else TextSecondary
                            )
                        }
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.AddCircleOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "افزودن برنامه (+)",
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == 1) TextPrimary else TextSecondary
                            )
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PrimaryIndigo)
                }
            } else {
                when (selectedTab) {
                    0 -> {
                        // نمایش به صورت شبکه صفحه هوم گوشی (Home Screen 4-Column Grid)
                        if (homeScreenItems.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(bottom = 60.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.DashboardCustomize,
                                        contentDescription = null,
                                        tint = TextSecondary.copy(alpha = 0.5f),
                                        modifier = Modifier.size(64.dp)
                                    )
                                    Spacer(modifier = Modifier.height(14.dp))
                                    Text(
                                        text = "صفحه هوم خالی است",
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "از تب «افزودن برنامه» نرم‌افزارهای خود را کلون کنید",
                                        color = TextSecondary,
                                        fontSize = 12.sp
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Button(
                                        onClick = { selectedTab = 1 },
                                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Add, contentDescription = null)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(text = "افزودن اولین برنامه")
                                    }
                                }
                            }
                        } else {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(4),
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(top = 10.dp, bottom = 40.dp, start = 4.dp, end = 4.dp)
                            ) {
                                items(homeScreenItems) { item ->
                                    HomeAppIconItem(
                                        item = item,
                                        onClick = {
                                            selectedActionItem = item
                                        },
                                        onLongClick = {
                                            selectedActionItem = item
                                        }
                                    )
                                }

                                // تایل افزودن در انتهای صفحه هوم
                                item {
                                    AddAppHomeTile(
                                        onClick = { selectedTab = 1 }
                                    )
                                }
                            }
                        }
                    }

                    1 -> {
                        // تب افزودن از تمام برنامه‌های نصب شده روی گوشی
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            placeholder = {
                                Text(text = "جستجوی برنامه برای کلون...", color = TextSecondary, fontSize = 13.sp)
                            },
                            leadingIcon = {
                                Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = TextSecondary)
                            },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(imageVector = Icons.Default.Clear, contentDescription = null, tint = TextSecondary)
                                    }
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryIndigo,
                                unfocusedBorderColor = CardBorder,
                                focusedContainerColor = DarkSurface,
                                unfocusedContainerColor = DarkSurface,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            singleLine = true
                        )

                        val filteredApps = installedApps.filter {
                            it.name.contains(searchQuery, ignoreCase = true) ||
                            it.packageName.contains(searchQuery, ignoreCase = true)
                        }

                        val clonedPackageNames = homeScreenItems.map { it.packageName }.toSet()

                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 20.dp)
                        ) {
                            items(filteredApps) { app ->
                                val isCloned = clonedPackageNames.contains(app.packageName)
                                InstalledAppCard(
                                    app = app,
                                    isAlreadyCloned = isCloned,
                                    onCloneClick = {
                                        scope.launch {
                                            val res = repository.cloneApp(app.packageName)
                                            if (res.isSuccess) {
                                                Toast.makeText(context, "نسخه جدید ${app.name} به صفحه هوم اضافه شد", Toast.LENGTH_SHORT).show()
                                                refreshData()
                                                selectedTab = 0 // بازگشت به صفحه هوم
                                            } else {
                                                Toast.makeText(context, "خطا در ساخت کلون", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // دیالوگ اکشن‌های هر برنامه در صفحه هوم
    selectedActionItem?.let { item ->
        AppActionDialog(
            item = item,
            onDismiss = { selectedActionItem = null },
            onLaunchRawSandbox = {
                IsolatedSandboxActivity.start(context, item.packageName, item.originalName, item.userId)
            },
            onLaunchNormal = {
                val (launched, errorMsg) = repository.launchInstance(context, item.packageName, item.userId)
                if (!launched) {
                    Toast.makeText(context, "خطا در اجرا: ${errorMsg ?: "عدم پاسخ برنامه"}", Toast.LENGTH_LONG).show()
                }
            },
            onOpenSystemDualApp = {
                SystemDualAppHelper.openSystemDualAppSetting(context, item.packageName)
            },
            onPinToHomeScreen = {
                val pinned = ShortcutHelper.createHomeScreenShortcut(
                    context = context,
                    packageName = item.packageName,
                    appName = item.originalName,
                    instanceNumber = item.instanceNumber,
                    iconDrawable = item.icon
                )
                if (pinned) {
                    Toast.makeText(context, "میانبر ${item.displayName} به صفحه اصلی گوشی اضافه شد", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "لانچر گوشی شما اجازه ایجاد میانبر خودکار نداد", Toast.LENGTH_SHORT).show()
                }
            },
            onCloneNextInstance = {
                scope.launch {
                    val res = repository.cloneApp(item.packageName)
                    if (res.isSuccess) {
                        Toast.makeText(context, "نسخه جدید ایجاد و به صفحه هوم افزوده شد", Toast.LENGTH_SHORT).show()
                        refreshData()
                    }
                }
            },
            onDeleteInstance = {
                scope.launch {
                    val deleted = repository.deleteInstance(item.packageName, item.userId)
                    if (deleted) {
                        Toast.makeText(context, "${item.displayName} از صفحه هوم حذف شد", Toast.LENGTH_SHORT).show()
                        refreshData()
                    }
                }
            }
        )
    }

}
