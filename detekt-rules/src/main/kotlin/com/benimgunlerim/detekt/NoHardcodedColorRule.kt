package com.benimgunlerim.detekt

import io.gitlab.arturbosch.detekt.api.CodeSmell
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Debt
import io.gitlab.arturbosch.detekt.api.Entity
import io.gitlab.arturbosch.detekt.api.Issue
import io.gitlab.arturbosch.detekt.api.Rule
import io.gitlab.arturbosch.detekt.api.Severity
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtConstantExpression

private val HEX_LITERAL = Regex("0[xX][0-9a-fA-F]+L?")

/**
 * CLAUDE.md Demir Kural #2: ekran/bileşen dosyalarında hardcoded renk yasak,
 * MaterialTheme.colorScheme.* kullanılmalı. Bu kural `Color(0xFF1B2730)` gibi
 * ham hex literal ile oluşturulan Color çağrılarını yakalar; MaterialTheme
 * token'ları veya isimli Color sabitleri (Color.Red, BrandPrimary) etkilenmez.
 */
class NoHardcodedColorRule(config: Config) : Rule(config) {
    override val issue = Issue(
        javaClass.simpleName,
        Severity.Style,
        "Hardcoded hex renk literali kullanılıyor. CLAUDE.md Demir Kural #2: MaterialTheme.colorScheme.* kullanın.",
        Debt.FIVE_MINS,
    )

    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)
        if (expression.calleeExpression?.text != "Color") return
        val hasHexArgument = expression.valueArguments.any { arg ->
            val argExpr = arg.getArgumentExpression()
            argExpr is KtConstantExpression && HEX_LITERAL.matches(argExpr.text)
        }
        if (hasHexArgument) {
            report(CodeSmell(issue, Entity.from(expression), issue.description))
        }
    }
}
