# اسکریپت ساخت APK به صورت مستقل بدون نیاز به اندروید استودیو
# این اسکریپت نسخه سبک OpenJDK 17 را در صورت نیاز دریافت و پروژه را کامپایل می‌کند.

$ErrorActionPreference = "Stop"
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "   App Cloner - Standalone APK Builder    " -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan

$CurrentDir = Split-Path -Parent $MyInvocation.MyCommand.Definition
Set-Location $CurrentDir

# بررسی جاوا
$JavaInstalled = $false
try {
    $res = java -version 2>&1
    if ($LASTEXITCODE -eq 0 -or $res -match "version") {
        $JavaInstalled = $true
        Write-Host "[✓] جاوا در سیستم شناسایی شد." -ForegroundColor Green
    }
} catch {
    $JavaInstalled = $false
}

if (-not $JavaInstalled) {
    $JdkFolder = Join-Path $CurrentDir ".tools\jdk-17"
    $JavaBin = Join-Path $JdkFolder "bin\java.exe"
    
    if (-not (Test-Path $JavaBin)) {
        Write-Host "[!] جاوا یافت نشد. در حال دریافت OpenJDK 17 پرتابل و سبک (بدون نیاز به نصب)..." -ForegroundColor Yellow
        $ToolsDir = Join-Path $CurrentDir ".tools"
        if (-not (Test-Path $ToolsDir)) { New-Item -ItemType Directory -Path $ToolsDir | Out-Null }
        
        $JdkZip = Join-Path $ToolsDir "openjdk.zip"
        $JdkUrl = "https://aka.ms/download-jdk/microsoft-jdk-17.0.10-windows-x64.zip"
        
        Write-Host "در حال دانلود از: $JdkUrl" -ForegroundColor Gray
        Invoke-WebRequest -Uri $JdkUrl -OutFile $JdkZip
        
        Write-Host "در حال استخراج..." -ForegroundColor Gray
        Expand-Archive -Path $JdkZip -DestinationPath $ToolsDir -Force
        Remove-Item $JdkZip -Force
        
        # شناسایی پوشه اکسترکت شده
        $Extracted = Get-ChildItem -Path $ToolsDir -Directory | Where-Object { $_.Name -like "*jdk*" } | Select-Object -First 1
        if ($Extracted) {
            Rename-Item -Path $Extracted.FullName -NewName "jdk-17" -Force
        }
    }
    
    $env:JAVA_HOME = $JdkFolder
    $env:Path = "$JdkFolder\bin;" + $env:Path
    Write-Host "[✓] محیط پرتابل جاوا 17 فعال شد." -ForegroundColor Green
}

# اجرای گرادل جهت ساخت APK
Write-Host "[*] در حال کامپایل پروژه و ساخت APK..." -ForegroundColor Cyan
if (Test-Path ".\gradlew.bat") {
    cmd.exe /c "gradlew.bat assembleDebug"
} else {
    Write-Host "[!] فایل gradlew.bat یافت نشد." -ForegroundColor Red
    exit 1
}

$ApkPath = Join-Path $CurrentDir "app\build\outputs\apk\debug\app-debug.apk"
if (Test-Path $ApkPath) {
    Write-Host ""
    Write-Host "====================================================" -ForegroundColor Green
    Write-Host "[✓] فایل APK با موفقیت ساخته شد!" -ForegroundColor Green
    Write-Host "مسیر فایل: $ApkPath" -ForegroundColor Yellow
    Write-Host "====================================================" -ForegroundColor Green
} else {
    Write-Host "[!] فرآیند کامپایل به اتمام نرسید یا به دلیل عدم تنظیم بودن Android SDK متوقف شد." -ForegroundColor Red
}
