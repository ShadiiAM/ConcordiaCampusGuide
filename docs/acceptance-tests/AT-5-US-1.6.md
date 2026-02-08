# AT-5: Main App Navigation

**Create this as a GitHub issue:**

**Title:** `[AT-5] Acceptance Test: Main App Navigation (US-1.6)`

**Labels:** `acceptance-test`, `sprint-2`

**Link to US:** Closes #97

**Body:**
```markdown
# Acceptance Test for US-1.6

## Acceptance Criteria from US-1.6

### Bottom Navigation
- [ ] Bottom navigation items (Map, Directions, Calendar, POI) are visible
- [ ] Tapping a bottom navigation item navigates to corresponding screen
- [ ] Currently selected tab is visually indicated
- [ ] Navigation between tabs doesn't crash or create duplicated screens
- [ ] Bottom navigation remains visible across all main screens

### Top Search Bar + Profile Button
- [ ] Search bar is displayed at the top of main screens
- [ ] Profile icon in top bar is tappable
- [ ] Tapping profile icon navigates to "User settings" screen
- [ ] From "User settings", back button returns to previous screen

### Accessibility
- [ ] All navigation items have meaningful labels/content descriptions
- [ ] Navigation controls are operable via accessibility services

## Automated Test File
`MainNavigationUITest.kt`

## Test Recording
See Wiki: Acceptance-Tests-GIFs

Record showing:
1. App launching with bottom navigation visible (4 tabs)
2. Clicking each tab (Directions, Calendar, POI) and back to Map
3. Search bar visible at top
4. Clicking profile button navigates to settings
5. Back navigation from settings returns to main screen
```
