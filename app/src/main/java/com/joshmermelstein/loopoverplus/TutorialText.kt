package com.joshmermelstein.loopoverplus

import android.content.Context

/*
 * This file exists because I want to include helptext directly in level files but in a way that
 * isn't hostile to translating the app in the future. This file maps help text IDs to string
 * resources to ensure that user-visible strings always comes from string resources.
 */

val tutorialTextMap = mapOf(
    "vertical_swipe_helptext" to R.string.verticalHelptext,
    "horizontal_swipe_helptext" to R.string.horizontalHelptext,
    "three_dots_helptext" to R.string.threeDotsHelptext,
    "intro_size_helptext" to R.string.introSizeHelptext,
    "mix_horiz_vert_helptext" to R.string.MixHorizVertHelptext,
    "end_intro_helptext" to R.string.EndIntroHelptext,
    "arrows_overview" to R.string.ArrowsOverview,
    "bandaged_overview_helptext" to R.string.BandagedOverviewHelptext,
    "bandaged_transitive_helptext" to R.string.BandagedTransitiveHelptext,
    "carousel_overview_helptext" to R.string.CarouselOverviewHelptext,
    "carousel_directionality_helptext" to R.string.CarouselDirectionalityHelptext,
    "carousel_preview_helptext" to R.string.CarouselPreviewHelptext,
    "dynamic_overview_helptext" to R.string.DynamicOverviewHelptext,
    "enabler_overview_helptext" to R.string.EnablerOverviewHelptext,
    "enabler_multiplicity_helptext" to R.string.EnablerMultiplicityHelptext,
    "gear_overview_helptext" to R.string.GearOverviewHelptext,
    "gear_directionality_helptext" to R.string.GearDirectionalityHelptext,
    "static_overview_helptext" to R.string.StaticOverviewHelptext,
    "wide_overview_helptext" to R.string.WideOverviewHelptext,
    "wide_variety_helptext" to R.string.WideVarietyHelptext,
    "wide_three_dots_reminder_helptext" to R.string.WideThreeDotsReminderHelptext,
    "wide_within_level_variance_helptext" to R.string.WideWithinLevelVarianceHelptext,
    "wide_preview_helptext" to R.string.WidePreviewHelptext,
)

fun getTutorialText(id: String, context: Context): String {
    return if (tutorialTextMap.containsKey(id)) {
        context.getString(tutorialTextMap[id]!!)
    } else {
        // Fallback for when I mess up and forget update the map
        id
    }
}