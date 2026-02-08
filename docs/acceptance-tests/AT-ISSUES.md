# Acceptance Test Issues for Sprint 2

Copy each section below into a new GitHub issue. Label each with "acceptance-test" and "sprint-2".

---

## AT-1: View SGW and Loyola Campus Maps

**Title:** `[AT-1] Acceptance Test: View SGW and Loyola Campus Maps (US-1.1)`

**Labels:** `acceptance-test`, `sprint-2`, `epic-1-campus-map`

**Linked to:** Closes #11 (US-1.1)

**Body:**
```markdown
# Acceptance Test for US-1.1: View SGW and Loyola Campus Maps

## User Story
As a user, I want to view interactive maps of both SGW and Loyola campuses so that I can explore campus buildings and locations.

## Acceptance Criteria to Verify

- [ ] App launches and displays map screen by default
- [ ] SGW campus map loads and is visible
- [ ] Map is interactive (pan, zoom, rotate)
- [ ] Google Maps renders correctly
- [ ] Campus buildings are visible on the map
- [ ] User can zoom in/out on campus areas
- [ ] Map shows both campuses when zoomed out

## Automated Test

Espresso test: `ViewCampusMapsUITest.kt`

## Test Recording

See [Acceptance Test GIFs Wiki](../../wiki/Acceptance-Tests-GIFs)

## Product Owner Sign-off

- [ ] Accepted by Product Owner
- [ ] Date: ___________
```

---

## AT-2: Render Campus Building Shapes

**Title:** `[AT-2] Acceptance Test: Render Campus Building Shapes (US-1.2)`

**Labels:** `acceptance-test`, `sprint-2`, `epic-1-campus-map`

**Linked to:** Closes #12 (US-1.2)

**Body:**
```markdown
# Acceptance Test for US-1.2: Render Campus Building Shapes (Polygons)

## User Story
As a user, I want to see building shapes rendered as colored polygons on the map so that I can distinguish Concordia buildings from surrounding city buildings.

## Acceptance Criteria to Verify

- [ ] Campus buildings are rendered as colored polygons
- [ ] Building polygons are distinct from city buildings
- [ ] Polygons match actual building footprints
- [ ] Building shapes are visible at appropriate zoom levels
- [ ] Colors distinguish Concordia buildings from surroundings
- [ ] Polygons render for both SGW and Loyola campuses

## Automated Test

Espresso test: `BuildingPolygonsUITest.kt`

## Test Recording

See [Acceptance Test GIFs Wiki](../../wiki/Acceptance-Tests-GIFs)

## Product Owner Sign-off

- [ ] Accepted by Product Owner
- [ ] Date: ___________
```

---

## AT-3: Switch Between SGW and Loyola

**Title:** `[AT-3] Acceptance Test: Switch Between SGW and Loyola (US-1.3)`

**Labels:** `acceptance-test`, `sprint-2`, `epic-1-campus-map`

**Linked to:** Closes #13 (US-1.3)

**Body:**
```markdown
# Acceptance Test for US-1.3: Switch Between SGW and Loyola

## User Story
As a user, I want to toggle between SGW and Loyola campus views so that I can see building information for my current campus.

## Acceptance Criteria to Verify

- [ ] Campus toggle switch is visible on map screen
- [ ] User can switch from SGW to Loyola campus
- [ ] User can switch from Loyola to SGW campus
- [ ] Map overlays update correctly when switching campuses
- [ ] SGW buildings are hidden when viewing Loyola campus
- [ ] Loyola buildings are hidden when viewing SGW campus
- [ ] Campus selection persists across app sessions

## Automated Test

Espresso test: `CampusSwitchingUITest.kt`

## Test Recording

See [Acceptance Test GIFs Wiki](../../wiki/Acceptance-Tests-GIFs)

## Product Owner Sign-off

- [ ] Accepted by Product Owner
- [ ] Date: ___________
```

---

## AT-4: Main App Navigation

**Title:** `[AT-4] Acceptance Test: Main App Navigation (US-1.6)`

**Labels:** `acceptance-test`, `sprint-2`, `story`

**Linked to:** Closes #97 (US-1.6)

**Body:**
```markdown
# Acceptance Test for US-1.6: Implement Main App Navigation

## User Story
As a user, I want to navigate between different sections of the app (Map, Calendar, Profile) so that I can access all features easily.

## Acceptance Criteria to Verify

- [ ] Bottom navigation bar is visible on main screens
- [ ] Map tab opens map screen
- [ ] Calendar tab opens calendar screen
- [ ] Profile tab opens profile screen
- [ ] Active tab is highlighted
- [ ] Navigation transitions are smooth
- [ ] Back button works correctly from each screen
- [ ] Navigation state persists on rotation

## Automated Test

Espresso test: `MainNavigationUITest.kt`

## Test Recording

See [Acceptance Test GIFs Wiki](../../wiki/Acceptance-Tests-GIFs)

## Product Owner Sign-off

- [ ] Accepted by Product Owner
- [ ] Date: ___________
```

---

## AT-5: Access Profile Menu from Search Bar

**Title:** `[AT-5] Acceptance Test: Access Profile Menu from Search Bar (US-1.7)`

**Labels:** `acceptance-test`, `sprint-2`, `story`

**Linked to:** Closes #102 (US-1.7)

**Body:**
```markdown
# Acceptance Test for US-1.7: Access Profile Menu from Search Bar

## User Story
As a user, I want to access my profile and settings from the search bar area so that I can quickly manage my account.

## Acceptance Criteria to Verify

- [ ] Account icon (A) is visible in top bar on map screen
- [ ] Tapping account icon opens user settings page
- [ ] Settings page displays correctly
- [ ] User can access profile information
- [ ] User can access accessibility settings
- [ ] Back button returns to map screen
- [ ] Settings are accessible from all main screens

## Automated Test

Espresso test: `ProfileMenuAccessUITest.kt`

## Test Recording

See [Acceptance Test GIFs Wiki](../../wiki/Acceptance-Tests-GIFs)

## Product Owner Sign-off

- [ ] Accepted by Product Owner
- [ ] Date: ___________
```

---

## AT-6: Text Accessibility Features

**Title:** `[AT-6] Acceptance Test: Text Accessibility Features (US-1.10)`

**Labels:** `acceptance-test`, `sprint-2`, `feature`, `task`

**Linked to:** Closes #145 (US-1.10)

**Body:**
```markdown
# Acceptance Test for US-1.10: Implement Text Accessibility Features

## User Story
As a user with visual impairments, I want text accessibility features (font size adjustment, high contrast, color filters) so that I can read app content comfortably.

## Acceptance Criteria to Verify

- [ ] Accessibility settings page is accessible from profile menu
- [ ] Font size can be adjusted (small, medium, large, extra large)
- [ ] Font size changes apply across all screens
- [ ] High contrast mode can be enabled
- [ ] Color filter options are available (protanopia, deuteranopia, tritanopia)
- [ ] Color filters apply to map and UI elements
- [ ] Settings persist across app sessions
- [ ] Changes take effect immediately

## Automated Test

Espresso test: `TextAccessibilityUITest.kt`

## Test Recording

See [Acceptance Test GIFs Wiki](../../wiki/Acceptance-Tests-GIFs)

## Product Owner Sign-off

- [ ] Accepted by Product Owner
- [ ] Date: ___________
```

---

## Instructions

1. Create each issue on GitHub with the content above
2. Apply the specified labels
3. Link to the corresponding US issue using "Closes #XX"
4. After creating all issues, note down the AT issue numbers (they'll be assigned by GitHub)
5. These numbers will be used in the Wiki page
