package com.benimgunlerim.detekt

import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.RuleSet
import io.gitlab.arturbosch.detekt.api.RuleSetProvider

class BenimGunlerimRuleSetProvider : RuleSetProvider {
    override val ruleSetId = "BenimGunlerimRules"

    override fun instance(config: Config): RuleSet = RuleSet(
        ruleSetId,
        listOf(
            NoHardcodedDpRule(config),
            NoHardcodedColorRule(config),
        ),
    )
}
