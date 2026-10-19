package com.appcloner.sandbox.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.appcloner.sandbox.data.model.ClonedAppGroup
import com.appcloner.sandbox.data.model.InstalledApp
import com.appcloner.sandbox.data.repository.AppRepository
import com.appcloner.sandbox.ui.components.ClonedAppCard
import com.appcloner.sandbox.ui.components.InstalledAppCard
import com.appcloner.sandbox.ui.components.InstanceManagementDialog
import com.appcloner.sandbox.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(repository: AppRepository) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var selectedTab by remember { mutableStateOf(0) } // 0: Cloned Apps, 1: All Installed Apps
    var searchQuery by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }

    var clonedApps by remember { mutableStateOf<List<ClonedAppGroup>>(emptyList()) }
    var installedApps by remember { mutableStateOf<List<InstalledApp>>(emptyList()) }
    var selectedAppForManagement by remember { mutableStateOf<ClonedAppGroup?>(null) }

    // بارگذاری داده‌ها
    fun refreshData() {
        scope.launch {
            isLoading = true
            try {
                clonedApps = repository.getClonedApps()
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
                                imageVector = Icons.Default.Layers,
                                contentDescription = null,
                                tint = TextPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "App Cloner Sandbox",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "ایزولاسیون و چندکاربرگی برنامه‌ها",
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
                .padding(horizontal = 16.dp)
        ) {
            // سرچ‌بار
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                placeholder = {
                    Text(
                        text = "جستجوی نام یا پکیج برنامه...",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = TextSecondary
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = null,
                                tint = TextSecondary
                            )
                        }
                    }
                },
                shape = RoundedCornerShape(14.dp),
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
                            Text(
                                text = "نسخه‌های کلون شده",
                                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == 0) TextPrimary else TextSecondary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (selectedTab == 0) PrimaryIndigo else DarkSurfaceLight)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "${clonedApps.sumOf { it.instances.size }}",
                                    fontSize = 10.sp,
                                    color = TextPrimary
                                )
                            }
                        }
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text(
                            text = "همه برنامه‌های گوشی",
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == 1) TextPrimary else TextSecondary
                        )
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
                        // لیست برنامه‌های کلون شده
                        val filteredClones = clonedApps.filter {
                            it.name.contains(searchQuery, ignoreCase = true) ||
                            it.packageName.contains(searchQuery, ignoreCase = true)
                        }

                        if (filteredClones.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(bottom = 60.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.FilterNone,
                                        contentDescription = null,
                                        tint = TextSecondary.copy(alpha = 0.5f),
                                        modifier = Modifier.size(64.dp)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "هیچ برنامه‌ای هنوز کلون نشده است",
                                        color = TextSecondary,
                                        fontSize = 15.sp
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Button(
                                        onClick = { selectedTab = 1 },
                                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text(text = "انتخاب برنامه برای کلون کردن")
                                    }
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(bottom = 20.dp)
                            ) {
                                items(filteredClones) { clonedApp ->
                                    ClonedAppCard(
                                        clonedApp = clonedApp,
                                        onManageInstances = { selectedAppForManagement = clonedApp },
                                        onQuickLaunch = { userId ->
                                            val launched = repository.launchInstance(clonedApp.packageName, userId)
                                            if (!launched) {
                                                Toast.makeText(context, "خطا در اجرای برنامه", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        onAddClone = {
                                            scope.launch {
                                                val res = repository.cloneApp(clonedApp.packageName)
                                                if (res.isSuccess) {
                                                    Toast.makeText(context, "نسخه جدید با موفقیت ایجاد شد", Toast.LENGTH_SHORT).show()
                                                    refreshData()
                                                } else {
                                                    Toast.makeText(context, "خطا در ساخت نسخه جدید", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }

                    1 -> {
                        // لیست همه برنامه‌های نصب شده روی گوشی
                        val filteredApps = installedApps.filter {
                            it.name.contains(searchQuery, ignoreCase = true) ||
                            it.packageName.contains(searchQuery, ignoreCase = true)
                        }

                        val clonedPackageNames = clonedApps.map { it.packageName }.toSet()

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
                                                Toast.makeText(context, "نسخه کپی با موفقیت ساخته شد", Toast.LENGTH_SHORT).show()
                                                refreshData()
                                                selectedTab = 0 // انتقال به تب کلون‌ها
                                            } else {
                                                Toast.makeText(context, "خطا در ایجاد کلون", Toast.LENGTH_SHORT).show()
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

    // دیالوگ مدیریت نسخه‌ها
    selectedAppForManagement?.let { appGroup ->
        InstanceManagementDialog(
            clonedApp = appGroup,
            onDismiss = { selectedAppForManagement = null },
            onLaunchInstance = { userId ->
                val launched = repository.launchInstance(appGroup.packageName, userId)
                if (!launched) {
                    Toast.makeText(context, "خطا در اجرای برنامه", Toast.LENGTH_SHORT).show()
                }
            },
            onDeleteInstance = { userId ->
                scope.launch {
                    val deleted = repository.deleteInstance(appGroup.packageName, userId)
                    if (deleted) {
                        Toast.makeText(context, "نسخه با موفقیت حذف شد", Toast.LENGTH_SHORT).show()
                        refreshData()
                        // بروزرسانی آیتم دیالوگ
                        val updated = repository.getClonedApps().find { it.packageName == appGroup.packageName }
                        if (updated == null || updated.instances.isEmpty()) {
                            selectedAppForManagement = null
                        } else {
                            selectedAppForManagement = updated
                        }
                    }
                }
            },
            onAddAnotherClone = {
                scope.launch {
                    val res = repository.cloneApp(appGroup.packageName)
                    if (res.isSuccess) {
                        Toast.makeText(context, "نسخه جدید اضافه شد", Toast.LENGTH_SHORT).show()
                        refreshData()
                    }
                }
            }
        )
    }
}
