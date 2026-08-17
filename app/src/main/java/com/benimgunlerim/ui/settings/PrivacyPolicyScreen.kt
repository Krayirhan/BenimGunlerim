package com.benimgunlerim.ui.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Analytics
import androidx.compose.material.icons.rounded.BugReport
import androidx.compose.material.icons.rounded.CloudDownload
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.OpenInBrowser
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.benimgunlerim.ui.components.core.AppButton
import com.benimgunlerim.ui.components.core.AppButtonVariant
import com.benimgunlerim.ui.components.core.AppSurface
import com.benimgunlerim.ui.theme.AppTokens
import com.benimgunlerim.ui.theme.BrandPrimary

data class PrivacySection(
    val title: String,
    val icon: ImageVector,
    val description: String,
    val bulletPoints: List<String> = emptyList(),
)

private val PRIVACY_SECTIONS = listOf(
    PrivacySection(
        title = "Yerel ve Çevrimdışı Depolama",
        icon = Icons.Rounded.Lock,
        description = "BenimGünlerim offline-first prensibiyle çalışır. Verileriniz tamamen cihazınızda saklanır.",
        bulletPoints = listOf(
            "Görevleriniz, rutinleriniz, günlük notlarınız ve ilerleme kayıtlarınız yalnızca telefonunuzdaki yerel veritabanında tutulur.",
            "Kişisel verileriniz harici herhangi bir sunucuya aktarılmaz veya satılmaz.",
        ),
    ),
    PrivacySection(
        title = "Anonim Kullanım Ölçümü (Analytics)",
        icon = Icons.Rounded.Analytics,
        description = "Deneyimi iyileştirmek için isteğe bağlı anonim telemetry kullanılabilir.",
        bulletPoints = listOf(
            "Görev başlıkları, rutin içerikleri, notlar veya kimlik bilgileri asla toplanmaz.",
            "Ayarlar menüsünden dilediğiniz zaman anonim ölçümü kapatabilirsiniz.",
        ),
    ),
    PrivacySection(
        title = "Hata Raporlama",
        icon = Icons.Rounded.BugReport,
        description = "Uygulama çökme/hata kayıtları tamamen cihazınızda tutulur.",
        bulletPoints = listOf(
            "Hiçbir çökme raporu veya teknik log sunuculara ya da üçüncü taraflara gönderilmez.",
        ),
    ),
    PrivacySection(
        title = "Yedekleme ve Veri Taşınabilirliği",
        icon = Icons.Rounded.CloudDownload,
        description = "Verileriniz üzerinde tam taşınabilirlik hakkınız vardır.",
        bulletPoints = listOf(
            "İstediğiniz an tüm verilerinizi JSON formatında cihazınıza dışa aktarabilirsiniz.",
            "Daha önce aldığınız JSON yedeğini kolayca içe aktararak geri yükleyebilirsiniz.",
        ),
    ),
    PrivacySection(
        title = "İzinler ve Kullanım Amaçları",
        icon = Icons.Rounded.NotificationsActive,
        description = "Uygulamanın ihtiyaç duyduğu izinler ve gerekçeleri:",
        bulletPoints = listOf(
            "Bildirim İzni (POST_NOTIFICATIONS): Rutin ve gün sonu hatırlatıcılarını göstermek için kullanılır.",
            "Tam Zamanlı Alarm (SCHEDULE_EXACT_ALARM): Hatırlatıcıların tam zamanında tetiklenmesini sağlar.",
        ),
    ),
    PrivacySection(
        title = "Veri Silme ve Kullanıcı Hakları",
        icon = Icons.Rounded.DeleteForever,
        description = "Verileriniz sizin kontrolünüzdedir.",
        bulletPoints = listOf(
            "Ayarlar ekranındaki 'Tüm verileri temizle' seçeneği ile tüm verilerinizi anında sıfırlayabilirsiniz.",
            "Uygulamayı kaldırdığınızda cihazdaki tüm veriler otomatik olarak tamamen silinir.",
        ),
    ),
    PrivacySection(
        title = "İletişim ve Destek",
        icon = Icons.Rounded.Email,
        description = "Gizlilikle ilgili her türlü soru veya öneriniz için:",
        bulletPoints = listOf(
            "E-posta: support@benimgunlerim.app",
        ),
    ),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen(
    onNavigateBack: () -> Unit,
    webPolicyUrl: String = "https://krayirhan.github.io/BenimGunlerim/privacy.html",
) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Gizlilik Politikası",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Geri",
                            tint = BrandPrimary,
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            runCatching {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(webPolicyUrl))
                                context.startActivity(intent)
                            }
                        },
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.OpenInBrowser,
                            contentDescription = "Web Sayfasında Aç",
                            tint = BrandPrimary,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                AppSurface(
                    radius = AppTokens.Radius.md,
                    padding = AppTokens.Spacing.cardInner,
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Security,
                            contentDescription = null,
                            tint = BrandPrimary,
                        )
                        Column {
                            Text(
                                text = "Gizliliğiniz Önceliğimizdir",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text = "Son Güncelleme: 17 Ağustos 2026",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }

            items(PRIVACY_SECTIONS) { section ->
                AppSurface(
                    radius = AppTokens.Radius.md,
                    padding = AppTokens.Spacing.cardInner,
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Icon(
                                imageVector = section.icon,
                                contentDescription = null,
                                tint = BrandPrimary,
                            )
                            Text(
                                text = section.title,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                        Text(
                            text = section.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        if (section.bulletPoints.isNotEmpty()) {
                            Column(
                                modifier = Modifier.padding(start = 8.dp, top = 4.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                section.bulletPoints.forEach { bullet ->
                                    Text(
                                        text = "• $bullet",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.9f),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
