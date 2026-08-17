package com.benimgunlerim.detekt

import io.gitlab.arturbosch.detekt.api.CodeSmell
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Debt
import io.gitlab.arturbosch.detekt.api.Entity
import io.gitlab.arturbosch.detekt.api.Issue
import io.gitlab.arturbosch.detekt.api.Rule
import io.gitlab.arturbosch.detekt.api.Severity
import org.jetbrains.kotlin.psi.KtConstantExpression
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtPrefixExpression

/**
 * CLAUDE.md Demir Kural #1: ekran/bileşen dosyalarında DP literal yasak,
 * AppTokens.Spacing/Radius/IconSize kullanılmalı. Bu kural `16.dp` gibi
 * sabit dp değerlerini yakalar; AppTokens.* referansları zaten `.dp`
 * kullanmadığı için etkilenmez.
 */
class NoHardcodedDpRule(config: Config) : Rule(config) {
    override val issue = Issue(
        javaClass.simpleName,
        Severity.Style,
        "Hardcoded dp literal kullanılıyor. CLAUDE.md Demir Kural #1: AppTokens.Spacing/Radius/IconSize kullanın.",
        Debt.FIVE_MINS,
    )

    override fun visitDotQualifiedExpression(expression: KtDotQualifiedExpression) {
        super.visitDotQualifiedExpression(expression)
        if (expression.selectorExpression?.text != "dp") return
        val receiver = expression.receiverExpression
        val isLiteral = receiver is KtConstantExpression ||
            (receiver is KtPrefixExpression && receiver.baseExpression is KtConstantExpression)
        if (isLiteral) {
            report(CodeSmell(issue, Entity.from(expression), issue.description))
        }
    }
}
