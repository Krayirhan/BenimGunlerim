Add-Type -AssemblyName System.Drawing
$src = "C:\Users\Acer\.gemini\antigravity-ide\brain\5dfd85ad-8295-4901-b375-20e981fca3c5\feature_graphic_banner_1787057772630.jpg"
$dest = "d:\BenimGunlerim\feature_graphic_1024x500.png"

$img = [System.Drawing.Image]::FromFile($src)
$bmp = New-Object System.Drawing.Bitmap(1024, 500)
$g = [System.Drawing.Graphics]::FromImage($bmp)
$g.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
$g.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::HighQuality
$g.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::HighQuality

$g.DrawImage($img, 0, 0, 1024, 500)
$bmp.Save($dest, [System.Drawing.Imaging.ImageFormat]::Png)

$g.Dispose()
$bmp.Dispose()
$img.Dispose()

Write-Host "Feature graphic generated at: $dest"
