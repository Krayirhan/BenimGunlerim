package com.benimgunlerim.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AchievementDefTest {

    @Test
    fun allAchievementIds_areUnique() {
        val ids = ALL_ACHIEVEMENTS.map { it.id }
        assertEquals("Achievement IDs must be unique", ids.size, ids.distinct().size)
    }

    @Test
    fun allAchievements_haveNonBlankFields() {
        ALL_ACHIEVEMENTS.forEach { def ->
            assertTrue("id must not be blank: $def", def.id.isNotBlank())
            assertTrue("titleRes must be a valid resource id: $def", def.titleRes != 0)
            assertTrue("emoji must not be blank: $def", def.emoji.isNotBlank())
            assertTrue("descriptionRes must be a valid resource id: $def", def.descriptionRes != 0)
        }
    }

    @Test
    fun allAchievements_xpRewardIsNonNegative() {
        ALL_ACHIEVEMENTS.forEach { def ->
            assertTrue("xpReward must be >= 0 for ${def.id}", def.xpReward >= 0)
        }
    }

    @Test
    fun achievementMap_containsAllIds() {
        val map = ALL_ACHIEVEMENTS.associateBy { it.id }
        ALL_ACHIEVEMENTS.forEach { def ->
            assertNotNull("Map must contain ${def.id}", map[def.id])
        }
    }

    @Test
    fun streak7Achievement_exists() {
        val found = ALL_ACHIEVEMENTS.firstOrNull { it.id == "streak_7" }
        assertNotNull(found)
        assertTrue(found!!.xpReward > 0)
    }
}
