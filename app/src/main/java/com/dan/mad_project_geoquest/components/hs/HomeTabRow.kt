package com.dan.mad_project_geoquest.components.hs

import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.sp
import com.dan.mad_project_geoquest.ui.theme.Cream
import com.dan.mad_project_geoquest.ui.theme.DarkBrown
import com.dan.mad_project_geoquest.ui.theme.Gold
import com.dan.mad_project_geoquest.ui.theme.Sand

val HOME_TABS = listOf("Events", "Completed", "Ongoing", "Private")

@Composable
fun HomeTabRow(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    TabRow(
        selectedTabIndex = selectedTab,
        containerColor = DarkBrown,
        contentColor = Cream,
        indicator = { tabPositions ->
            TabRowDefaults.SecondaryIndicator(
                modifier = androidx.compose.ui.Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                color = Gold
            )
        }
    ) {
        HOME_TABS.forEachIndexed { index, title ->
            Tab(
                selected = selectedTab == index,
                onClick = { onTabSelected(index) },
                text = { Text(title, fontSize = 11.sp) },
                selectedContentColor = Cream,
                unselectedContentColor = Sand
            )
        }
    }
}