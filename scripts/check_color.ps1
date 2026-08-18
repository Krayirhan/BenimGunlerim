Add-Type -AssemblyName System.Drawing
$img = [System.Drawing.Bitmap]::FromFile('C:\Users\Acer\.gemini\antigravity-ide\brain\5dfd85ad-8295-4901-b375-20e981fca3c5\.user_uploaded\media_1787054331575.png')
$p1 = $img.GetPixel(150, 50)
$hex = [System.Drawing.ColorTranslator]::ToHtml($p1)
Write-Host "Logo background color hex: $hex"
$img.Dispose()
