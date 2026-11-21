<#
.SYNOPSIS
    Compila l'APK di AstroViaggio incrementando progressivamente la versione e verificando la firma crittografica.

.DESCRIPTION
    Aggiorna version.properties con il nuovo numero progressivo (versionCode/versionName),
    esegue Gradle per compilare l'APK (debug o release), verifica l'impronta della firma digitale (SHA-256)
    e salva l'APK generato nella cartella dist/ con un nome chiaro e versionato.

.PARAMETER BuildType
    Tipo di build: 'debug' (predefinito) oppure 'release'.

.PARAMETER Bump
    Strategia di incremento versione:
    - 'build': incrementa VERSION_BUILD e VERSION_CODE (+1) [Predefinito]
    - 'patch': incrementa VERSION_PATCH (es. 1.0.0 -> 1.0.1) e VERSION_CODE (+1)
    - 'minor': incrementa VERSION_MINOR (es. 1.0.0 -> 1.1.0) e VERSION_CODE (+1)
    - 'major': incrementa VERSION_MAJOR (es. 1.0.0 -> 2.0.0) e VERSION_CODE (+1)
    - 'none': non modifica la versione attuale

.PARAMETER Clean
    Esegue gradlew clean prima della compilazione.

.PARAMETER Install
    Installa/aggiorna automaticamente l'APK compilato su un dispositivo/emulatore collegato tramite ADB.

.PARAMETER ForceReinstall
    Se su un dispositivo è presente una vecchia versione firmata con una chiave diversa (es. cloud AI Studio),
    esegue un backup dei dati utente (DataStore), disinstalla la versione incompatibile, installa la nuova e ripristina i dati.

.EXAMPLE
    .\build-apk.ps1
    Compila APK debug incrementando versionCode e build.

.EXAMPLE
    .\build-apk.ps1 -Install
    Compila e aggiorna direttamente l'APK sullo smartphone o emulatore collegato.

.EXAMPLE
    .\build-apk.ps1 -Install -ForceReinstall
    Esegue la transizione iniziale salvando i dati utente e allineando la firma al keystore locale del progetto.
#>

[CmdletBinding()]
param(
    [ValidateSet("debug", "release")]
    [string]$BuildType = "debug",

    [ValidateSet("build", "patch", "minor", "major", "none")]
    [string]$Bump = "build",

    [switch]$Clean,

    [switch]$Install,

    [switch]$ForceReinstall,

    [string]$VersionNameOverride,

    [int]$VersionCodeOverride,

    [switch]$NoGitPush,

    [string]$GitCommitMessage
)

$ErrorActionPreference = "Stop"
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Definition
Set-Location $ScriptDir

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host " 🚀 AstroViaggio - Build APK & Verifica Firma Permanente" -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan

# 1. Gestione del file version.properties
$versionFile = Join-Path $ScriptDir "version.properties"
$major = 1
$minor = 0
$patch = 0
$build = 1
$code  = 1

if (Test-Path $versionFile) {
    Get-Content $versionFile | ForEach-Object {
        $line = $_.Trim()
        if ($line -and -not $line.StartsWith("#")) {
            $parts = $line -split "=", 2
            if ($parts.Count -eq 2) {
                $key = $parts[0].Trim()
                $val = $parts[1].Trim()
                switch ($key) {
                    "VERSION_MAJOR" { [int]::TryParse($val, [ref]$major) | Out-Null }
                    "VERSION_MINOR" { [int]::TryParse($val, [ref]$minor) | Out-Null }
                    "VERSION_PATCH" { [int]::TryParse($val, [ref]$patch) | Out-Null }
                    "VERSION_BUILD" { [int]::TryParse($val, [ref]$build) | Out-Null }
                    "VERSION_CODE"  { [int]::TryParse($val, [ref]$code)  | Out-Null }
                }
            }
        }
    }
}

# 2. Incremento progressivo
switch ($Bump) {
    "build" {
        $build++
        $code++
    }
    "patch" {
        $patch++
        $build++
        $code++
    }
    "minor" {
        $minor++
        $patch = 0
        $build++
        $code++
    }
    "major" {
        $major++
        $minor = 0
        $patch = 0
        $build++
        $code++
    }
    "none" {
        # Mantieni i valori correnti
    }
}

if ($PSBoundParameters.ContainsKey("VersionCodeOverride")) {
    $code = $VersionCodeOverride
}

$versionName = "$major.$minor.$patch"
if ($PSBoundParameters.ContainsKey("VersionNameOverride") -and $VersionNameOverride) {
    $versionName = $VersionNameOverride
}

# Salva version.properties aggiornato
$versionContent = @"
# AstroViaggio Version Configuration
# Aggiornato automaticamente dallo script di build
VERSION_MAJOR=$major
VERSION_MINOR=$minor
VERSION_PATCH=$patch
VERSION_BUILD=$build
VERSION_CODE=$code
VERSION_NAME=$versionName
"@

Set-Content -Path $versionFile -Value $versionContent -Encoding UTF8

Write-Host "📋 Versione configurata: v$versionName (Code: $code, Build: $build)" -ForegroundColor Green
Write-Host "🎯 Tipo di build:        $BuildType" -ForegroundColor Yellow

# 3. Verifica prerequisiti (local.properties e debug.keystore permanente)
$localProps = Join-Path $ScriptDir "local.properties"
if (-not (Test-Path $localProps)) {
    $defaultSdk = Join-Path $env:LOCALAPPDATA "Android\Sdk"
    if (Test-Path $defaultSdk) {
        $sdkEscaped = $defaultSdk -replace "\\", "\\"
        Set-Content -Path $localProps -Value "sdk.dir=$sdkEscaped" -Encoding UTF8
        Write-Host "✓ Creato local.properties con Android SDK: $defaultSdk" -ForegroundColor DarkGray
    }
}

$debugKeystore = Join-Path $ScriptDir "debug.keystore"
if (-not (Test-Path $debugKeystore)) {
    Write-Host "Inizializzazione keystore permanente debug.keystore in corso..." -ForegroundColor Yellow
    & keytool -genkey -v -keystore $debugKeystore -storepass android -alias androiddebugkey -keypass android -keyalg RSA -keysize 2048 -validity 10000 -dname "CN=Android Debug,O=Android,C=US" | Out-Null
    Write-Host "✓ Keystore permanente generato con successo." -ForegroundColor Green
}

$envFile = Join-Path $ScriptDir ".env"
$envExample = Join-Path $ScriptDir ".env.example"
if (-not (Test-Path $envFile) -and (Test-Path $envExample)) {
    Copy-Item $envExample $envFile
}

# 4. Compilazione con Gradle
$gradlew = Join-Path $ScriptDir "gradlew.bat"
if (-not (Test-Path $gradlew)) {
    $gradlew = "gradle"
}

$capitalizedType = (Get-Culture).TextInfo.ToTitleCase($BuildType.ToLower())
$taskName = "assemble$capitalizedType"

$gradleArgs = @()
if ($Clean) {
    $gradleArgs += "clean"
}
$gradleArgs += $taskName

Write-Host "`n⚙️  Esecuzione Gradle ($($gradleArgs -join ' '))..." -ForegroundColor Cyan
& $gradlew @gradleArgs

if ($LASTEXITCODE -ne 0) {
    Write-Error "❌ Compilazione fallita con codice di uscita $LASTEXITCODE"
    exit $LASTEXITCODE
}

# 5. Raccolta e rinomina dell'APK generato
$apkDir = Join-Path $ScriptDir "app\build\outputs\apk\$($BuildType.ToLower())"
$apkFiles = Get-ChildItem -Path $apkDir -Filter "*.apk" -ErrorAction SilentlyContinue | Where-Object { -not $_.Name.EndsWith("-unaligned.apk") }

if (-not $apkFiles) {
    Write-Error "❌ Impossibile trovare l'APK generato in: $apkDir"
    exit 1
}

$sourceApk = $apkFiles[0].FullName
$distDir = Join-Path $ScriptDir "dist"
if (-not (Test-Path $distDir)) {
    New-Item -ItemType Directory -Path $distDir | Out-Null
}

$destApkName = "AstroViaggio-v$($versionName)-b$($code)-$($BuildType.ToLower()).apk"
$destApkPath = Join-Path $distDir $destApkName
Copy-Item -Path $sourceApk -Destination $destApkPath -Force

# Crea anche una copia "latest" per comodità
$latestApkPath = Join-Path $distDir "AstroViaggio-$($BuildType.ToLower())-latest.apk"
Copy-Item -Path $sourceApk -Destination $latestApkPath -Force

$fileSizeMb = [math]::Round((Get-Item $destApkPath).Length / 1MB, 2)

# 6. Verifica dell'impronta crittografica della firma (SHA-256)
$certSha256 = ""
$certSha1 = ""
$apksigner = Get-ChildItem -Path "$env:LOCALAPPDATA\Android\Sdk\build-tools" -Filter "apksigner.bat" -Recurse -ErrorAction SilentlyContinue | Select-Object -Last 1 -ExpandProperty FullName
if ($apksigner) {
    $signerOutput = & $apksigner verify --print-certs $destApkPath 2>&1
    foreach ($line in $signerOutput) {
        if ($line -match "SHA-256 digest:\s*([a-fA-F0-9]+)") {
            $certSha256 = $Matches[1].ToUpper()
        } elseif ($line -match "SHA-1 digest:\s*([a-fA-F0-9]+)") {
            $certSha1 = $Matches[1].ToUpper()
        }
    }
}

Write-Host "`n==========================================================" -ForegroundColor Green
Write-Host " 🎉 BUILD COMPLETATA CON SUCCESSO!" -ForegroundColor Green
Write-Host "==========================================================" -ForegroundColor Green
Write-Host " 📦 APK:            $destApkName" -ForegroundColor White
Write-Host " 🏷️  Versione:       v$versionName (versionCode: $code)" -ForegroundColor White
Write-Host " 📏 Dimensione:     $fileSizeMb MB" -ForegroundColor White
Write-Host " 🔐 Firma SHA-256:  $certSha256" -ForegroundColor Cyan
Write-Host " 📁 Percorso:       $destApkPath" -ForegroundColor Yellow
Write-Host " 🔗 Link rapido:    $latestApkPath" -ForegroundColor DarkGray
Write-Host "==========================================================" -ForegroundColor Green

# 7. Installazione / Aggiornamento su dispositivo via ADB
if ($Install) {
    $packageName = "com.ricrug.astroviaggio"
    Write-Host "`n📱 Verifica dispositivo Android connesso tramite ADB..." -ForegroundColor Cyan
    $adbCmd = Get-Command "adb" -ErrorAction SilentlyContinue
    if (-not $adbCmd) {
        $fallbackAdb = Join-Path $env:LOCALAPPDATA "Android\Sdk\platform-tools\adb.exe"
        if (Test-Path $fallbackAdb) {
            $adbCmd = $fallbackAdb
        }
    }

    if (-not $adbCmd) {
        Write-Warning "ADB non trovato. Impossibile installare automaticamente."
        exit 0
    }

    $devices = & $adbCmd devices | Select-String -Pattern "device$"
    if (-not $devices) {
        Write-Warning "Nessun dispositivo Android collegato in modalità Debug USB."
        exit 0
    }

    Write-Host "Installazione/Aggiornamento dell'APK in corso..." -ForegroundColor Cyan
    $origEAP = $ErrorActionPreference
    $ErrorActionPreference = "Continue"
    $installOutput = & $adbCmd install -r $destApkPath 2>&1
    $installExit = $LASTEXITCODE
    $ErrorActionPreference = $origEAP

    if ($installExit -eq 0 -and ($installOutput -match "Success")) {
        Write-Host "✅ AGGIORNAMENTO COMPLETATO CON SUCCESSO!" -ForegroundColor Green
        Write-Host "L'applicazione è stata aggiornata sul dispositivo mantenendo tutti i dati esistenti." -ForegroundColor Green
    } else {
        $outStr = $installOutput -join "`n"
        $isSignatureMismatch = ($outStr -match "INSTALL_FAILED_UPDATE_INCOMPATIBLE")
        if ($isSignatureMismatch) {
            Write-Host "`n⚠️  INCOMPATIBILITÀ DI FIRMA RILEVATA!" -ForegroundColor Yellow
            Write-Host "L'applicazione attualmente installata sul dispositivo proviene da una firma diversa" -ForegroundColor Yellow
            Write-Host "(ad esempio dalla build cloud iniziale di Google AI Studio)." -ForegroundColor Yellow
            Write-Host "Perché tutti i futuri rilasci siano riconosciuti come normali aggiornamenti in-place," -ForegroundColor Yellow
            Write-Host "è necessario allineare una tantum il dispositivo alla firma di questo progetto." -ForegroundColor Yellow

            if ($ForceReinstall) {
                Write-Host "`n🔄 Procedura automatica di migrazione con salvataggio dati (-ForceReinstall):" -ForegroundColor Cyan
                
                # Backup preferenze
                $backupFile = Join-Path $env:TEMP "astro_prefs_backup.preferences_pb"
                Write-Host "1. Backup dati utente/preferenze in corso..." -ForegroundColor DarkGray
                & $adbCmd shell "run-as $packageName cat /data/data/$packageName/files/datastore/astro_explorer_prefs.preferences_pb" > $backupFile 2>$null
                
                # Disinstallazione versione incompatibile
                Write-Host "2. Disinstallazione della versione cloud precedente..." -ForegroundColor DarkGray
                & $adbCmd uninstall $packageName | Out-Null
                
                # Installazione nuova versione
                Write-Host "3. Installazione della nuova versione firmata con il keystore permanente..." -ForegroundColor DarkGray
                $origEAP = $ErrorActionPreference
                $ErrorActionPreference = "Continue"
                $reinstallOutput = & $adbCmd install $destApkPath 2>&1
                $reinstallExit = $LASTEXITCODE
                $ErrorActionPreference = $origEAP

                if ($reinstallExit -eq 0 -and ($reinstallOutput -match "Success")) {
                    Write-Host "✓ Applicazione installata con successo." -ForegroundColor Green
                    
                    # Ripristino preferenze se esistenti
                    if (Test-Path $backupFile -and (Get-Item $backupFile).Length -gt 0) {
                        Write-Host "4. Ripristino preferenze utente..." -ForegroundColor DarkGray
                        & $adbCmd push $backupFile "/data/local/tmp/prefs_tmp.pb" 2>$null | Out-Null
                        & $adbCmd shell "run-as $packageName mkdir -p /data/data/$packageName/files/datastore && run-as $packageName cp /data/local/tmp/prefs_tmp.pb /data/data/$packageName/files/datastore/astro_explorer_prefs.preferences_pb" 2>$null
                        & $adbCmd shell "rm -f /data/local/tmp/prefs_tmp.pb" 2>$null
                        Write-Host "✓ Dati utente e avatar ripristinati con successo." -ForegroundColor Green
                    }
                    
                    Write-Host "`n==========================================================" -ForegroundColor Green
                    Write-Host " ✅ MIGRAZIONE COMPLETATA CON SUCCESSO!" -ForegroundColor Green
                    Write-Host "==========================================================" -ForegroundColor Green
                    Write-Host "Tutti i dati e le configurazioni sono stati preservati." -ForegroundColor White
                    Write-Host "Da questo momento in poi, OGNI nuova build generata da questo progetto" -ForegroundColor White
                    Write-Host "sarà installata direttamente come AGGIORNAMENTO in-place!" -ForegroundColor White
                    Write-Host "==========================================================" -ForegroundColor Green
                } else {
                    Write-Host "`n⚠️  Avviso installazione: $($reinstallOutput -join "`n")" -ForegroundColor Yellow
                    Write-Host "Se compare un popup sullo schermo dello smartphone (MIUI/HyperOS), conferma l'installazione tramite USB." -ForegroundColor Cyan
                }
            } else {
                Write-Host "`n👉 Per eseguire la migrazione salvando i dati, esegui:" -ForegroundColor White
                Write-Host "   .\build-apk.ps1 -Install -ForceReinstall" -ForegroundColor Cyan
            }
        } else {
            Write-Warning "Errore durante l'installazione: $outStr"
        }
    }
}

# 8. Aggiornamento automatico del repository GitHub ad ogni build
if (-not $NoGitPush) {
    Write-Host "`n==========================================================" -ForegroundColor Cyan
    Write-Host " 🐙 Sincronizzazione Repository GitHub" -ForegroundColor Cyan
    Write-Host "==========================================================" -ForegroundColor Cyan
    
    $gitCmd = Get-Command "git" -ErrorAction SilentlyContinue
    if ($gitCmd) {
        try {
            $origEAP = $ErrorActionPreference
            $ErrorActionPreference = "Continue"
            $gitStatus = & git status --porcelain 2>&1
            if ($gitStatus) {
                Write-Host "Rilevate modifiche nel workspace. Preparazione commit..." -ForegroundColor DarkGray
                & git add -A
                $commitMsg = if ($GitCommitMessage) {
                    $GitCommitMessage
                } else {
                    "build: aggiornamento automatico v$versionName (code $code, build $build)"
                }
                Write-Host "Commit: '$commitMsg'..." -ForegroundColor DarkGray
                $commitOut = & git commit -m "$commitMsg" 2>&1
                if ($LASTEXITCODE -eq 0) {
                    Write-Host "✓ Commit locale creato con successo." -ForegroundColor Green
                } else {
                    Write-Host "Commit output: $commitOut" -ForegroundColor DarkGray
                }
            } else {
                Write-Host "Nessuna nuova modifica locale da committare." -ForegroundColor DarkGray
            }
            $ErrorActionPreference = $origEAP

            # Ottieni branch attivo
            $currentBranch = (& git rev-parse --abbrev-ref HEAD 2>$null)
            if ($currentBranch) {
                $currentBranch = $currentBranch.Trim()
            }
            if (-not $currentBranch -or $currentBranch -eq "HEAD") {
                $currentBranch = "main"
            }

            Write-Host "Invio modifiche al repository remoto origin/$currentBranch..." -ForegroundColor Cyan
            $origEAP = $ErrorActionPreference
            $ErrorActionPreference = "Continue"
            $pushOut = & git push origin $currentBranch 2>&1
            $pushExit = $LASTEXITCODE
            $ErrorActionPreference = $origEAP

            if ($pushExit -eq 0) {
                Write-Host "✅ Repository GitHub aggiornato con successo su origin/$currentBranch!" -ForegroundColor Green

                # 9. Pubblicazione automatica Release su GitHub con asset APK allegato
                try {
                    Write-Host "`n==========================================================" -ForegroundColor Cyan
                    Write-Host " 🚀 Pubblicazione Release su GitHub" -ForegroundColor Cyan
                    Write-Host "==========================================================" -ForegroundColor Cyan

                    # Recupero credenziali per API GitHub tramite Git Credential Manager
                    $credInput = "url=https://github.com/ricrug/AstroBloom.git`n`n"
                    $process = New-Object System.Diagnostics.Process
                    $process.StartInfo.FileName = "git"
                    $process.StartInfo.Arguments = "credential fill"
                    $process.StartInfo.UseShellExecute = $false
                    $process.StartInfo.RedirectStandardInput = $true
                    $process.StartInfo.RedirectStandardOutput = $true
                    $process.StartInfo.CreateNoWindow = $true
                    [void]$process.Start()
                    $writer = $process.StandardInput
                    $writer.Write($credInput)
                    $writer.Flush()
                    $writer.Close()
                    $gitCredOut = $process.StandardOutput.ReadToEnd()
                    $process.WaitForExit()

                    $ghToken = ""
                    foreach ($l in ($gitCredOut -split "`r?`n")) {
                        if ($l.StartsWith("password=")) {
                            $ghToken = $l.Substring(9).Trim()
                            break
                        }
                    }

                    if ($ghToken) {
                        $releaseTag = "v$versionName-b$build"
                        Write-Host "Creazione tag git e invio a GitHub: $releaseTag..." -ForegroundColor DarkGray
                        & git tag -f -a "$releaseTag" -m "Release $releaseTag" 2>&1 | Out-Null
                        $origEAP = $ErrorActionPreference
                        $ErrorActionPreference = "Continue"
                        & git push origin "$releaseTag" --force 2>&1 | Out-Null
                        $ErrorActionPreference = $origEAP

                        $ghHeaders = @{
                            "Authorization" = "token $ghToken"
                            "User-Agent" = "AstroBloom-BuildScript"
                            "Accept" = "application/vnd.github.v3+json"
                        }

                        $releaseName = "AstroViaggio v$versionName (Build $build)"
                        $releaseBody = @(
                            "## 🚀 AstroViaggio v$versionName (Build $build)",
                            "",
                            "### Novità dell'aggiornamento:",
                            "- **Pad di Pilotaggio della Discesa da 104dp**: Console di pilotaggio del lander completamente riprogettata con pad circolare identico per dimensioni (104dp) e grafica al controller di volo dell'astronave (cloche 3D con ritorno a molla al centro, mirino al neon, anelli concentrici e frecce direzionali).",
                            "- **Doppia Modalità di Controllo (CLOCHE / D-PAD)**: Selettore rapido per alternare il controllo fluido tramite cloche analogica o tramite D-pad tattico con pulsanti virtuali SX, DX e FRENO.",
                            "- **Dashboard di Assetto e Orizzonte Artificiale (104dp)**: Giroscopio centrale con inclinometro real-time (gradi di rollio), linea d'orizzonte rotante, tachimetro della velocità di discesa con codice colore di sicurezza e LED propulsori RCS.",
                            "- **Leva del Freno Verticale con Ritorno Automatico a Molla (104dp x 68dp)**: Manetta freno dedicata al pollice destro su binario graduato, fiammata di spinta attiva e spegnimento immediato del motore principale al rilascio del pollice.",
                            "",
                            "### 📦 Dettagli Pacchetto APK:",
                            "- **File:** $destApkName",
                            "- **Dimensione:** $("{0:N2} MB" -f ($apkFileSize / 1MB))",
                            "- **Firma Digitale SHA-256:** $certFingerprint"
                        ) -join "`n"

                        # Verifica release esistente
                        $existingRelease = $null
                        try {
                            $existingRelease = Invoke-RestMethod -Uri "https://api.github.com/repos/ricrug/AstroBloom/releases/tags/$releaseTag" -Headers $ghHeaders -Method Get
                        } catch {}

                        if ($existingRelease) {
                            Write-Host "Aggiornamento release esistente $releaseTag (ID: $($existingRelease.id))..." -ForegroundColor DarkGray
                            $releaseId = $existingRelease.id
                        } else {
                            Write-Host "Creazione nuova release su GitHub: $releaseTag..." -ForegroundColor Cyan
                            $createPayload = @{
                                tag_name = $releaseTag
                                target_commitish = $currentBranch
                                name = $releaseName
                                body = $releaseBody
                                draft = $false
                                prerelease = $false
                            } | ConvertTo-Json -Depth 4
                            $createBytes = [System.Text.Encoding]::UTF8.GetBytes($createPayload)
                            $newRelease = Invoke-RestMethod -Uri "https://api.github.com/repos/ricrug/AstroBloom/releases" -Headers $ghHeaders -Method Post -Body $createBytes -ContentType "application/json; charset=utf-8"
                            $releaseId = $newRelease.id
                        }

                        # Caricamento del file APK firmato come asset della release
                        if ($releaseId -and (Test-Path $destApkPath)) {
                            Write-Host "Caricamento APK su GitHub Release ($destApkName)..." -ForegroundColor Cyan
                            $uploadHeaders = @{
                                "Authorization" = "token $ghToken"
                                "User-Agent" = "AstroBloom-BuildScript"
                                "Content-Type" = "application/vnd.android.package-archive"
                            }

                            try {
                                $assets = Invoke-RestMethod -Uri "https://api.github.com/repos/ricrug/AstroBloom/releases/$releaseId/assets" -Headers $ghHeaders -Method Get
                                foreach ($a in $assets) {
                                    if ($a.name -eq $destApkName) {
                                        Invoke-RestMethod -Uri "https://api.github.com/repos/ricrug/AstroBloom/releases/assets/$($a.id)" -Headers $ghHeaders -Method Delete | Out-Null
                                    }
                                }
                            } catch {}

                            $apkBytes = [System.IO.File]::ReadAllBytes($destApkPath)
                            $uploadUri = "https://uploads.github.com/repos/ricrug/AstroBloom/releases/$releaseId/assets?name=$destApkName"
                            $uploadResult = Invoke-RestMethod -Uri $uploadUri -Headers $uploadHeaders -Method Post -Body $apkBytes
                            Write-Host "✓ APK caricato con successo sulla release GitHub!" -ForegroundColor Green
                            Write-Host "🔗 Link Download: $($uploadResult.browser_download_url)" -ForegroundColor Green
                        }
                    } else {
                        Write-Warning "Token GitHub non disponibile per la pubblicazione della release."
                    }
                } catch {
                    Write-Warning "Avviso durante la pubblicazione della release: $_"
                }
            } else {
                Write-Warning "Impossibile completare il push su GitHub (exit code $pushExit): $pushOut"
            }
        } catch {
            Write-Warning "Avviso durante la sincronizzazione con GitHub: $_"
        }
    } else {
        Write-Warning "Comando 'git' non trovato. Impossibile sincronizzare con GitHub."
    }
}
