Add-Type -AssemblyName System.Drawing

$srcPath = "C:\Users\Acer\.gemini\antigravity-ide\brain\5dfd85ad-8295-4901-b375-20e981fca3c5\.user_uploaded\media_1787054331575.png"
$resDir = "d:\BenimGunlerim\app\src\main\res"
$playStoreIconPath = "d:\BenimGunlerim\play_store_icon_512.png"
$appPlayStoreIconPath = "d:\BenimGunlerim\app\play_store_icon_512.png"

$srcImg = [System.Drawing.Bitmap]::FromFile($srcPath)
Write-Host "Source image loaded: $($srcImg.Width) x $($srcImg.Height)"

# The cream background color of the logo
$bgColor = [System.Drawing.Color]::FromArgb(255, 254, 247, 231) # #FEF7E7

# Function to flood outer white pixels with the cream background color
# so Google Play's dynamic squircle mask doesn't show outer white artifacts.
function Create-Clean-Logo {
    param([System.Drawing.Bitmap]$Source)
    $clean = New-Object System.Drawing.Bitmap($Source.Width, $Source.Height, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
    
    # Check if outer corners are white (> 245 in R,G,B while inside is cream R=254, G=247, B=231)
    # First fill entirely with $bgColor
    $g = [System.Drawing.Graphics]::FromImage($clean)
    $g.Clear($bgColor)
    $g.Dispose()

    # Draw the source over it
    for ($y = 0; $y -lt $Source.Height; $y++) {
        for ($x = 0; $x -lt $Source.Width; $x++) {
            $p = $Source.GetPixel($x, $y)
            # If pixel is outer white (R > 248, G > 248, B > 248), replace with $bgColor
            if ($p.R -gt 248 -and $p.G -gt 248 -and $p.B -gt 248) {
                $clean.SetPixel($x, $y, $bgColor)
            } else {
                $clean.SetPixel($x, $y, $p)
            }
        }
    }
    return $clean
}

# Function to extract only the foreground emblem (green + orange) on transparent background for adaptive icons
function Create-Emblem-Transparent {
    param([System.Drawing.Bitmap]$Source)
    $emblem = New-Object System.Drawing.Bitmap($Source.Width, $Source.Height, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
    $transparent = [System.Drawing.Color]::FromArgb(0, 0, 0, 0)

    for ($y = 0; $y -lt $Source.Height; $y++) {
        for ($x = 0; $x -lt $Source.Width; $x++) {
            $p = $Source.GetPixel($x, $y)
            # If pixel is background cream or white (R > 230 and G > 230 and B > 200)
            if ($p.R -gt 230 -and $p.G -gt 230 -and $p.B -gt 200) {
                $emblem.SetPixel($x, $y, $transparent)
            } else {
                $emblem.SetPixel($x, $y, $p)
            }
        }
    }
    return $emblem
}

Write-Host "Processing clean background and transparent emblem..."
$cleanImg = Create-Clean-Logo -Source $srcImg
$emblemImg = Create-Emblem-Transparent -Source $srcImg

# Function to resize bitmap with highest quality Bicubic interpolation
function Resize-Image {
    param(
        [System.Drawing.Image]$Image,
        [int]$Width,
        [int]$Height
    )
    $destRect = New-Object System.Drawing.Rectangle(0, 0, $Width, $Height)
    $destImage = New-Object System.Drawing.Bitmap($Width, $Height, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
    $graphics = [System.Drawing.Graphics]::FromImage($destImage)
    $graphics.CompositingMode = [System.Drawing.Drawing2D.CompositingMode]::SourceOver
    $graphics.CompositingQuality = [System.Drawing.Drawing2D.CompositingQuality]::HighQuality
    $graphics.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
    $graphics.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::HighQuality
    $graphics.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::HighQuality

    $wrapMode = New-Object System.Drawing.Imaging.ImageAttributes
    $wrapMode.SetWrapMode([System.Drawing.Drawing2D.WrapMode]::TileFlipXY)
    $graphics.DrawImage($Image, $destRect, 0, 0, $Image.Width, $Image.Height, [System.Drawing.GraphicsUnit]::Pixel, $wrapMode)
    $graphics.Dispose()
    $wrapMode.Dispose()
    return $destImage
}

# Function to create circular clipped icon
function Create-Round-Image {
    param(
        [System.Drawing.Image]$Image,
        [int]$Size
    )
    $destImage = New-Object System.Drawing.Bitmap($Size, $Size, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
    $graphics = [System.Drawing.Graphics]::FromImage($destImage)
    $graphics.Clear([System.Drawing.Color]::Transparent)
    $graphics.CompositingQuality = [System.Drawing.Drawing2D.CompositingQuality]::HighQuality
    $graphics.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
    $graphics.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
    $graphics.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::HighQuality

    $path = New-Object System.Drawing.Drawing2D.GraphicsPath
    $path.AddEllipse(0, 0, $Size, $Size)
    $graphics.SetClip($path)

    $destRect = New-Object System.Drawing.Rectangle(0, 0, $Size, $Size)
    $wrapMode = New-Object System.Drawing.Imaging.ImageAttributes
    $wrapMode.SetWrapMode([System.Drawing.Drawing2D.WrapMode]::TileFlipXY)
    $graphics.DrawImage($Image, $destRect, 0, 0, $Image.Width, $Image.Height, [System.Drawing.GraphicsUnit]::Pixel, $wrapMode)

    $graphics.Dispose()
    $path.Dispose()
    $wrapMode.Dispose()
    return $destImage
}

# Function to create adaptive foreground icon centered within safe zone (66% of total canvas)
function Create-Adaptive-Foreground {
    param(
        [System.Drawing.Image]$Image,
        [int]$TotalSize
    )
    $destImage = New-Object System.Drawing.Bitmap($TotalSize, $TotalSize, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
    $graphics = [System.Drawing.Graphics]::FromImage($destImage)
    $graphics.Clear([System.Drawing.Color]::Transparent)
    $graphics.CompositingQuality = [System.Drawing.Drawing2D.CompositingQuality]::HighQuality
    $graphics.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
    $graphics.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::HighQuality
    $graphics.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::HighQuality

    # Emblem safe zone: 65% of total size
    $innerSize = [int]($TotalSize * 0.65)
    $offset = [int](($TotalSize - $innerSize) / 2)
    $destRect = New-Object System.Drawing.Rectangle($offset, $offset, $innerSize, $innerSize)

    $wrapMode = New-Object System.Drawing.Imaging.ImageAttributes
    $wrapMode.SetWrapMode([System.Drawing.Drawing2D.WrapMode]::TileFlipXY)
    $graphics.DrawImage($Image, $destRect, 0, 0, $Image.Width, $Image.Height, [System.Drawing.GraphicsUnit]::Pixel, $wrapMode)

    $graphics.Dispose()
    $wrapMode.Dispose()
    return $destImage
}

# 1. Play Store 512x512 icon (clean background, sharp 512x512)
$playStoreIcon = Resize-Image -Image $cleanImg -Width 512 -Height 512
$playStoreIcon.Save($playStoreIconPath, [System.Drawing.Imaging.ImageFormat]::Png)
$playStoreIcon.Save($appPlayStoreIconPath, [System.Drawing.Imaging.ImageFormat]::Png)
$playStoreIcon.Dispose()
Write-Host "Saved Google Play Store 512x512 icon to: $playStoreIconPath"

# 2. Android Mipmap densities
$densities = @(
    @{ Name = "mipmap-mdpi";    LauncherSize = 48;  ForegroundSize = 108 },
    @{ Name = "mipmap-hdpi";    LauncherSize = 72;  ForegroundSize = 162 },
    @{ Name = "mipmap-xhdpi";   LauncherSize = 96;  ForegroundSize = 216 },
    @{ Name = "mipmap-xxhdpi";  LauncherSize = 144; ForegroundSize = 324 },
    @{ Name = "mipmap-xxxhdpi"; LauncherSize = 192; ForegroundSize = 432 }
)

foreach ($d in $densities) {
    $dir = Join-Path $resDir $d.Name
    if (-not (Test-Path $dir)) {
        New-Item -ItemType Directory -Path $dir -Force | Out-Null
    }

    # ic_launcher.png (legacy squircle/clean)
    $launcher = Resize-Image -Image $cleanImg -Width $d.LauncherSize -Height $d.LauncherSize
    $launcherPath = Join-Path $dir "ic_launcher.png"
    $launcher.Save($launcherPath, [System.Drawing.Imaging.ImageFormat]::Png)
    $launcher.Dispose()

    # ic_launcher_round.png (legacy round mask)
    $round = Create-Round-Image -Image $cleanImg -Size $d.LauncherSize
    $roundPath = Join-Path $dir "ic_launcher_round.png"
    $round.Save($roundPath, [System.Drawing.Imaging.ImageFormat]::Png)
    $round.Dispose()

    # ic_launcher_foreground.png (transparent emblem centered in safe zone)
    $foreground = Create-Adaptive-Foreground -Image $emblemImg -TotalSize $d.ForegroundSize
    $foregroundPath = Join-Path $dir "ic_launcher_foreground.png"
    $foreground.Save($foregroundPath, [System.Drawing.Imaging.ImageFormat]::Png)
    $foreground.Dispose()

    Write-Host "Generated $($d.Name): ic_launcher ($($d.LauncherSize)x$($d.LauncherSize)), ic_launcher_round, ic_launcher_foreground ($($d.ForegroundSize)x$($d.ForegroundSize))"
}

$srcImg.Dispose()
$cleanImg.Dispose()
$emblemImg.Dispose()
Write-Host "All assets generated and configured successfully!"
