package com.example.campusguide.ui.screens

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.campusguide.ui.components.BottomCard
import com.example.campusguide.ui.directions.IndoorOutdoorRouteRequest
import com.example.campusguide.indoor.IndoorFloorGraph
import com.example.campusguide.indoor.IndoorNode
import com.example.campusguide.indoor.IndoorNodeType
import com.example.campusguide.indoor.IndoorRoomSearchService
import com.example.campusguide.ui.accessibility.LocalAccessibilityState
import com.example.campusguide.ui.screens.map.DirectionsTopBarState
import com.example.campusguide.ui.viewmodels.IndoorNavState
import com.example.campusguide.ui.viewmodels.IndoorNavigationViewModel

// ─── Concordia brand colours ──────────────────────────────────────────────────
private val ConcordiaRed    = Color(0xFF912338)
private val PathColor       = Color(0xFF912338)
private val OriginColor     = Color(0xFF4CAF50)
private val DestColor       = Color(0xFFE53935)
private val PoiColor        = Color(0xFF1565C0)
private val ElevatorColor   = Color(0xFF7B1FA2)
private val StairColor      = Color(0xFF6D4C41)
private val HallwayColor    = Color(0x00BDBDBD)
private val RoomColor       = Color(0xFF0288D1)
private val NodeRadius      = 12f
private val PathStrokeWidth = 8f

/**
 * Full-screen indoor map screen.
 *
 * Supports US-5.1 (view floor map), US-5.2 (select rooms), US-5.3 (shortest path),
 * US-5.4 (accessible routing), US-5.5 (POI highlighting), US-5.6 (cross-floor).
 *
 * @param buildingCode  Which building to open (e.g. "H").
 * @param onClose       Called when the user taps the close button.
 */
@Composable
fun IndoorMapScreen(
    buildingCode: String = "H",
    focusNode: IndoorNode? = null,
    setStartNode: IndoorNode? = null,
    setDestNode: IndoorNode? = null,
    resetVersion: Int = 0,
    triggerVersion: Int = 0,
    hasExistingDestinationSelection: Boolean = false,
    goTrigger: Int = 0,
    clearTrigger: Int = 0,
    onDirectionsTopBarState: (DirectionsTopBarState) -> Unit = {},
    onTopCardActiveChanged: (Boolean) -> Unit = {},
    onCrossBuildingRouteRequested: (IndoorOutdoorRouteRequest) -> Unit = {},
    onTriggersConsumed: () -> Unit = {},
    onClose: () -> Unit = {},
    providedViewModel: IndoorNavigationViewModel? = null,
) {
    val clearVersion = maxOf(resetVersion, clearTrigger)
    val viewModel = providedViewModel ?: viewModel<IndoorNavigationViewModel>(
        key = "indoor-nav-$clearVersion",
    )
    val accessibilityState = LocalAccessibilityState.current

    LaunchedEffect(accessibilityState.avoidStairs, accessibilityState.avoidEscalators) {
        viewModel.setRoutingPreferences(
            avoidStairs = accessibilityState.avoidStairs,
            avoidEscalators = accessibilityState.avoidEscalators,
        )
        if (viewModel.canRoute && viewModel.navState != IndoorNavState.Idle) {
            viewModel.computePath()
        }
    }

    val graph = viewModel.currentGraph
    val navState = viewModel.navState
    val floors = viewModel.availableFloors
    val selectedFloor = viewModel.selectedFloor
    val originNode = viewModel.originNode
    val destNode = viewModel.destinationNode
    val highlightedNode = viewModel.highlightedNode

    // Tap-mode: first tap = origin, second tap = destination
    var selectionMode by remember { mutableStateOf(SelectionMode.ORIGIN) }
    var topCardEditMode by remember { mutableStateOf<SelectionMode?>(null) }
    var topCardQuery by remember { mutableStateOf("") }

    LaunchedEffect(resetVersion, clearTrigger) {
        if (viewModel.consumeClearTrigger(clearVersion)) {
            selectionMode = SelectionMode.ORIGIN
            topCardEditMode = null
            topCardQuery = ""
        }
    }

    var infoNode by remember { mutableStateOf<IndoorNode?>(null) }
    // POI info popup
    var poiInfoNode by remember { mutableStateOf<IndoorNode?>(null) }

    // Keep building state stable across overlay open/close and apply incoming triggers atomically.
    LaunchedEffect(buildingCode, focusNode, setStartNode, setDestNode, triggerVersion) {
        val requestedBuilding = buildingCode.uppercase()
        if (viewModel.buildingCode != requestedBuilding) {
            viewModel.openBuilding(requestedBuilding)
        }

        if (setStartNode != null && setDestNode != null) {
            viewModel.selectOrigin(setStartNode)
            viewModel.selectDestination(setDestNode)
            viewModel.focusNode(setDestNode)
            if (viewModel.canRoute) {
                viewModel.computePath()
            }
            topCardEditMode = null
            selectionMode = SelectionMode.ORIGIN
        } else if (focusNode != null) {
            viewModel.focusNode(focusNode)
        } else if (setStartNode != null) {
            viewModel.focusNode(setStartNode)
            viewModel.selectOrigin(setStartNode)
            topCardEditMode = null
        } else if (setDestNode != null) {
            viewModel.focusNode(setDestNode)
            viewModel.selectDestination(setDestNode)
            // Destination-first flow: next prompt should ask for start.
            selectionMode = SelectionMode.ORIGIN
            topCardEditMode = null
        }

        if (focusNode != null || setStartNode != null || setDestNode != null) {
            onTriggersConsumed()
        }
    }

    val topCardActive = originNode != null || destNode != null || navState != IndoorNavState.Idle
    LaunchedEffect(topCardActive) {
        onTopCardActiveChanged(topCardActive)
        if (!topCardActive) {
            topCardEditMode = null
            topCardQuery = ""
        }
    }

    LaunchedEffect(goTrigger) {
        if (goTrigger == 0) return@LaunchedEffect
        if (viewModel.canRoute) {
            viewModel.computePath()
        }
    }

    LaunchedEffect(originNode, destNode, navState, viewModel.canRoute) {
        val hasComputedRoute = navState is IndoorNavState.SameFloor || navState is IndoorNavState.CrossFloor
        val error = when (navState) {
            IndoorNavState.NoPath -> "No path found — try disabling accessibility filter"
            is IndoorNavState.NoAccessiblePath -> {
                if (navState.hasNonAccessibleAlternative) {
                    "Accessible route not available. Disable accessibility toggles to view a non-accessible route."
                } else {
                    "Accessible route not available."
                }
            }
            else -> null
        }

        val summary = when (navState) {
            is IndoorNavState.SameFloor -> {
                val n = navState.path.nodes.size
                "Route found · $n step${if (n != 1) "s" else ""}"
            }
            is IndoorNavState.CrossFloor -> navState.result.floorChangeInstruction
            else -> null
        }

        onDirectionsTopBarState(
            DirectionsTopBarState(
                active = topCardActive,
                originLabel = formatIndoorTopCardLabel(originNode) ?: "Tap a room to set start",
                destinationLabel = formatIndoorTopCardLabel(destNode) ?: "Tap a room to set destination",
                routeSummary = summary,
                errorMessage = error,
                isLoadingRoute = false,
                showActions = !hasComputedRoute,
                currentSteps = null,
                goEnabled = viewModel.canRoute,
                showTravelModes = false,
                goLabel = "Go",
                cancelLabel = "Cancel",
                indoorOriginNode = originNode,
                indoorDestinationNode = destNode,
            )
        )
    }

    val inlineSuggestions = remember(topCardQuery) {
        if (topCardQuery.isBlank()) {
            emptyList()
        } else {
            IndoorRoomSearchService.search(
                query = topCardQuery,
                scope = IndoorRoomSearchService.Scope.Global,
                limit = 8,
            )
        }
    }

    Box(Modifier.fillMaxSize()) {
        Scaffold(
            bottomBar = {
                Column {
                    NavStateBanner(navState)
                    if (floors.size > 1) {
                        FloorPicker(
                            floors        = floors,
                            selectedFloor = selectedFloor,
                            onFloorSelect = { viewModel.selectFloor(it) }
                        )
                    }
                }
            }
        ) { padding ->
            val mapPadding = PaddingValues(
                start = padding.calculateStartPadding(LocalLayoutDirection.current),
                top = padding.calculateTopPadding(),
                end = padding.calculateEndPadding(LocalLayoutDirection.current),
                // keep a small buffer above the bottom bar
                bottom = padding.calculateBottomPadding() + 16.dp,
            )

            if (graph == null || graph.floorPlanDrawableRes == 0) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        if (graph == null) "No indoor map available for building $buildingCode."
                        else "Floor plan image missing for floor ${graph.floor}. Add a floor plan PNG to res/drawable."
                    )
                }
            } else {
                val pathNodesOnFloor: List<IndoorNode> = when (navState) {
                    is IndoorNavState.SameFloor -> navState.path.nodes.filter { it.floor == selectedFloor }
                    is IndoorNavState.CrossFloor -> {
                        val legA = navState.result.legOrigin.nodes.filter { it.floor == selectedFloor }
                        val legB = navState.result.legDest.nodes.filter { it.floor == selectedFloor }
                        legA + legB
                    }
                    else -> emptyList()
                }

                val crossFloorInstruction: String? = when {
                    navState is IndoorNavState.CrossFloor && selectedFloor == (originNode?.floor ?: selectedFloor) ->
                        navState.result.floorChangeInstruction
                    else -> null
                }

                FloorMapContent(
                    modifier          = Modifier
                        .fillMaxSize()
                        .padding(mapPadding),
                    graph             = graph,
                    pathNodes         = pathNodesOnFloor,
                    originNode        = originNode,
                    destNode          = destNode,
                    highlightedNode   = highlightedNode,
                    selectionMode     = selectionMode,
                    crossFloorHint    = crossFloorInstruction,
                    onNodeTapped      = { node ->
                        if (node.type == IndoorNodeType.POI) {
                            poiInfoNode = node
                        } else {
                            if (!topCardActive) {
                                if (hasExistingDestinationSelection) {
                                    viewModel.selectOrigin(node)
                                    selectionMode = SelectionMode.DESTINATION
                                } else {
                                    viewModel.selectDestination(node)
                                    selectionMode = SelectionMode.ORIGIN
                                }
                            } else {
                                when (selectionMode) {
                                    SelectionMode.ORIGIN -> {
                                        viewModel.selectOrigin(node)
                                        selectionMode = SelectionMode.DESTINATION
                                    }
                                    SelectionMode.DESTINATION -> {
                                        viewModel.selectDestination(node)
                                        selectionMode = SelectionMode.ORIGIN
                                    }
                                }
                            }
                        }
                    },
                    onNodeLongPress   = { node -> infoNode = node }
                )
            }
        }

        // Single directions card is provided by MainActivity/MapScreen.
    }

    infoNode?.let { node ->
        NodeInfoDialog(node = node, onDismiss = { infoNode = null })
    }

    poiInfoNode?.let { node ->
        PoiInfoPopup(
            node = node,
            onDismiss = { poiInfoNode = null },
            onSetAsOrigin = {
                viewModel.selectOrigin(node)
                selectionMode = SelectionMode.DESTINATION
                poiInfoNode = null
            },
            onSetAsDestination = {
                viewModel.selectDestination(node)
                selectionMode = SelectionMode.ORIGIN
                poiInfoNode = null
            }
        )
    }
}

// ─── Selection mode ───────────────────────────────────────────────────────────
private enum class SelectionMode { ORIGIN, DESTINATION }

// ─── Top bar ─────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun IndoorTopBar(
    buildingCode: String,
    originNode: IndoorNode?,
    destNode: IndoorNode?,
    requireAccessible: Boolean,
    onToggleAccessible: () -> Unit,
    onSwap: () -> Unit,
    onClearRoute: () -> Unit,
    onClose: () -> Unit
) {
    TopAppBar(
        windowInsets = WindowInsets(0),
        modifier = Modifier.statusBarsPadding(),
        title = {
            Column {
                Text(
                    text = "Indoor — $buildingCode",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (originNode != null || destNode != null) {
                    Text(
                        text = "${originNode?.label ?: "—"} → ${destNode?.label ?: "—"}",
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                } else {
                    Text(
                        text = "Tap a room to set start / end",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        navigationIcon = {
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Close indoor map")
            }
        },
        actions = {
            // Accessibility toggle (US-5.4)
            IconButton(
                onClick = onToggleAccessible,
                modifier = Modifier.semantics { contentDescription = "Toggle accessible routing" }
            ) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    tint = if (requireAccessible) ConcordiaRed else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            // Swap origin / destination
            if (originNode != null || destNode != null) {
                IconButton(onClick = onSwap) {
                    Icon(Icons.Default.Refresh, contentDescription = "Swap origin and destination")
                }
                IconButton(onClick = onClearRoute) {
                    Icon(Icons.Default.Close, contentDescription = "Clear route")
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            titleContentColor = Color.Black,
            navigationIconContentColor = Color.Black,
            actionIconContentColor = Color.Black
        )
    )
}

// ─── Nav-state banner ────────────────────────────────────────────────────────
@Composable
private fun NavStateBanner(navState: IndoorNavState) {
    val (text, color) = when (navState) {
        IndoorNavState.Idle     -> null to null
        IndoorNavState.NoPath   -> "No path found — try disabling accessibility filter" to Color(0xFFB71C1C)
        is IndoorNavState.NoAccessiblePath -> {
            val suffix = if (navState.hasNonAccessibleAlternative) {
                " Disable accessibility toggles to view a non-accessible route."
            } else {
                ""
            }
            "Accessible route not available.$suffix" to Color(0xFFB71C1C)
        }
        is IndoorNavState.SameFloor  -> {
            val n = navState.path.nodes.size
            "Route found — $n step${if (n != 1) "s" else ""}" to Color(0xFF1B5E20)
        }
        is IndoorNavState.CrossFloor -> {
            navState.result.floorChangeInstruction to Color(0xFF0D47A1)
        }
    }
    if (text != null && color != null) {
        Surface(color = color, modifier = Modifier.fillMaxWidth()) {
            Text(
                text = text,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

// ─── Floor picker ────────────────────────────────────────────────────────────
@Composable
private fun FloorPicker(
    floors: List<Int>,
    selectedFloor: Int,
    onFloorSelect: (Int) -> Unit
) {
    // Build a full contiguous range from min..max and mark which floors are implemented.
    // Exclude floor 0 from the UI (there is no ground/0 floor in our indoor maps).
    val minFloor = floors.minOrNull() ?: selectedFloor
    val maxFloor = floors.maxOrNull() ?: selectedFloor
    val fullRange = (minFloor..maxFloor).filterNot { it == 0 }
    val implemented = remember(floors) { floors.toSet() }

    val itemWidth = 72.dp
    val itemHeight = 42.dp
    val itemSpacing = 12.dp

    // Compute an initial index into `fullRange` (which excludes floor 0).
    // This keeps the selected floor visible on first render even when floors span negatives.
    val initialIndex = remember(selectedFloor, minFloor, fullRange) {
        fullRange.indexOf(selectedFloor).let { if (it >= 0) it else 0 }
    }
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialIndex)
    val density = LocalDensity.current

    // Keep track of the container width so we can compute horizontal padding to center items
    var containerWidth by remember { mutableStateOf(0) }

    Surface(
        tonalElevation = 4.dp,
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(itemHeight + 24.dp)
                .onSizeChanged { containerWidth = it.width }
        ) {
            // Compute side padding so an item will be visually centered when scrolled to its index
            val sidePadding = remember(containerWidth) {
                if (containerWidth <= 0) 0.dp
                else {
                    val sidePx = (containerWidth.toFloat() - with(density) { itemWidth.toPx() }).coerceAtLeast(0f) / 2f
                    with(density) { sidePx.toDp() }
                }
            }

            LazyRow(
                state = listState,
                contentPadding = PaddingValues(horizontal = sidePadding),
                horizontalArrangement = Arrangement.spacedBy(itemSpacing),
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center)
            ) {
                items(fullRange) { floor ->
                    val isSelected = floor == selectedFloor
                    val isImplemented = floor in implemented

                    // Visual styling
                    val backgroundColor = if (isSelected) ConcordiaRed else Color.Transparent
                    val textColor = when {
                        isSelected -> Color.White
                        isImplemented -> Color.Black
                        else -> Color.Gray
                    }
                    val scale = if (isSelected) 1.12f else 1f

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .width(itemWidth)
                            .height(itemHeight)
                            .graphicsLayer { this.scaleX = scale; this.scaleY = scale }
                            .background(color = backgroundColor, shape = RoundedCornerShape(12.dp))
                            .padding(8.dp)
                            .then(
                                if (isImplemented) Modifier.clickable { onFloorSelect(floor) }
                                else Modifier
                            )
                    ) {
                        // Show only the floor number.
                        // Negative floors are displayed as S{abs(floor)} (e.g., -1 -> S1).
                        val label = if (floor < 0) "S${-floor}" else floor.toString()
                        Text(
                            text = label,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = textColor
                        )
                    }
                }
            }
        }
    }
}

// ─── Floor map content (zoomable / pannable) ──────────────────────────────────
@Composable
private fun FloorMapContent(
    modifier: Modifier,
    graph: IndoorFloorGraph,
    pathNodes: List<IndoorNode>,
    originNode: IndoorNode?,
    destNode: IndoorNode?,
    highlightedNode: IndoorNode?,
    selectionMode: SelectionMode,
    crossFloorHint: String?,
    onNodeTapped: (IndoorNode) -> Unit,
    onNodeLongPress: (IndoorNode) -> Unit
) {
    // Zoom / pan state
    var scale  by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    // The size of the outer container (pre-transform)
    var containerSize by remember { mutableStateOf(IntSize.Zero) }

    // Auto-center on highlighted node (search focus). We do this in screen space.
    LaunchedEffect(highlightedNode?.id, highlightedNode?.floor, containerSize) {
        val hn = highlightedNode
        if (hn == null) return@LaunchedEffect
        if (hn.floor != graph.floor) return@LaunchedEffect
        if (hn.type == IndoorNodeType.HALLWAY) return@LaunchedEffect
        if (containerSize.width <= 0 || containerSize.height <= 0) return@LaunchedEffect

        val cw = containerSize.width.toFloat()
        val ch = containerSize.height.toFloat()
        val center = Offset(cw / 2f, ch / 2f)

        // Compute where the node is in *content/screen* coordinates before zoom/pan.
        // This mirrors ContentScale.Fit used by the Image.
        val imgAspect  = graph.imageWidth.toFloat() / graph.imageHeight
        val contAspect = cw / ch
        val (drawW, drawH) = if (imgAspect > contAspect) {
            cw to cw / imgAspect
        } else {
            ch * imgAspect to ch
        }
        val drawLeft = (cw - drawW) / 2f
        val drawTop  = (ch - drawH) / 2f

        val nodeContent = Offset(
            x = drawLeft + (hn.x / graph.imageWidth) * drawW,
            y = drawTop + (hn.y / graph.imageHeight) * drawH,
        )

        // With a center pivot, translation needed to bring node to center is:
        // offset = center - ((node - center) * scale + center)
        offset = center - ((nodeContent - center) * scale + center)
    }

    val transformState = rememberTransformableState { zoomChange, panChange, _ ->
        scale  = (scale * zoomChange).coerceIn(0.5f, 5f)
        // Scale panning by zoom (and a small boost) so distance feels consistent.
        val panBoost = 1.25f
        offset = offset + (panChange * scale * panBoost)
    }

    // Path node set for O(1) look-up
    val pathNodeIds = remember(pathNodes) { pathNodes.map { it.id }.toSet() }
    val latestOnNodeTapped by rememberUpdatedState(onNodeTapped)
    val latestOnNodeLongPress by rememberUpdatedState(onNodeLongPress)

    Box(
        modifier = modifier
            .onSizeChanged { containerSize = it }
            // pointerInput lives here — BEFORE the graphicsLayer transform,
            // so tapOffset is in raw screen/container coordinates.
            .pointerInput(graph, scale, offset, containerSize) {
                detectTapGestures(
                    onTap = { tapOffset ->
                        val imgPx = screenToImage(tapOffset, scale, offset, containerSize, graph)
                        Log.d("IndoorCoords", "TAP  x=${imgPx.x.toInt()}  y=${imgPx.y.toInt()}")
                        val hit = findNearestNode(imgPx, graph.nodes, threshold = 60f / scale)
                        // Ignore hallway nodes — they should not be tappable
                        if (hit != null && hit.type != IndoorNodeType.HALLWAY) {
                            latestOnNodeTapped(hit)
                        }
                    },
                    onLongPress = { tapOffset ->
                        val imgPx = screenToImage(tapOffset, scale, offset, containerSize, graph)
                        val hit = findNearestNode(imgPx, graph.nodes, threshold = 80f / scale)
                        // Ignore hallway nodes for long-press info as well
                        if (hit != null && hit.type != IndoorNodeType.HALLWAY) {
                            latestOnNodeLongPress(hit)
                        }
                    }
                )
            }
    ) {
        // ── Zoomable/pannable layer ────────────────────────────────────────
        // graphicsLayer scales from the CENTER of the composable (pivotFractionX/Y default = 0.5)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX       = scale,
                    scaleY       = scale,
                    translationX = offset.x,
                    translationY = offset.y
                )
                .transformable(state = transformState)
        ) {
            // Floor plan PNG
            androidx.compose.foundation.Image(
                painter            = painterResource(graph.floorPlanDrawableRes),
                contentDescription = "Floor ${graph.floor} map of building ${graph.buildingCode}",
                contentScale       = ContentScale.Fit,
                modifier           = Modifier.fillMaxSize()
            )

            val poisOnFloor = graph.nodes.filter { it.type == IndoorNodeType.POI }
            val poiAccessibilityDesc = if (poisOnFloor.isEmpty()) {
                "Floor map overlay. No points of interest on this floor."
            } else {
                val names = poisOnFloor.joinToString(", ") { poiDisplayName(it.label) }
                "Floor map overlay. Points of interest: $names. Tap a marker to view details."
            }
            Canvas(modifier = Modifier
                .fillMaxSize()
                .semantics { contentDescription = poiAccessibilityDesc }) {
                // Fit the image into canvas: compute draw rect matching ContentScale.Fit
                val imgAspect  = graph.imageWidth.toFloat() / graph.imageHeight
                val canvAspect = size.width / size.height
                val (drawW, drawH) = if (imgAspect > canvAspect) {
                    size.width to size.width / imgAspect
                } else {
                    size.height * imgAspect to size.height
                }
                val drawLeft = (size.width  - drawW) / 2f
                val drawTop  = (size.height - drawH) / 2f

                val scaleX = drawW / graph.imageWidth
                val scaleY = drawH / graph.imageHeight

                // Draw path lines
                drawPathLines(pathNodes, scaleX, scaleY, drawLeft, drawTop)

                // Draw highlighted node (from search) as a ring, if it's on this floor.
                highlightedNode?.takeIf { it.floor == graph.floor && it.type != IndoorNodeType.HALLWAY }?.let { hn ->
                    val hx = drawLeft + hn.x * scaleX
                    val hy = drawTop + hn.y * scaleY
                    drawCircle(
                        color = Color(0xFFFFC107),
                        radius = NodeRadius * 2.2f,
                        center = Offset(hx, hy),
                        style = Stroke(width = 6f)
                    )
                }

                // Draw all nodes
                for (node in graph.nodes) {
                    val cx = drawLeft + node.x * scaleX
                    val cy = drawTop  + node.y * scaleY
                    val isOnPath = node.id in pathNodeIds
                    val isOrigin = node.id == originNode?.id
                    val isDest   = node.id == destNode?.id

                    // If a node is on the path but is a hallway, don't highlight it as part
                    // of the route — keep the regular hallway color and don't draw the
                    // white inner circle. Also, don't draw a marker for hallway nodes that
                    // are only on the path (we want the path to be just a line through hallways).
                    val isHallway = node.type == IndoorNodeType.HALLWAY

                    val color = when {
                        isOrigin -> OriginColor
                        isDest   -> DestColor
                        isOnPath && !isHallway -> PathColor
                        else     -> nodeColor(node.type)
                    }
                    val radius = if (isOrigin || isDest) NodeRadius * 1.5f else NodeRadius

                    // Only draw a node marker for non-hallway nodes, or if the node is an origin/destination
                    if (!isHallway || isOrigin || isDest) {
                        val isIconType = (node.type == IndoorNodeType.POI ||
                                node.type == IndoorNodeType.ELEVATOR ||
                                node.type == IndoorNodeType.ESCALATOR) && !isOrigin && !isDest
                        if (isIconType) {
                            drawPoiIcon(node.label, cx, cy, radius * 3.2f, node.type)
                        } else {
                            drawCircle(color = color, radius = radius, center = Offset(cx, cy))
                            if ((isOnPath && !isHallway) || isOrigin || isDest) {
                                drawCircle(color = Color.White, radius = radius * 0.5f, center = Offset(cx, cy))
                            }
                        }
                    }
                }
            }

            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val containerW = constraints.maxWidth.toFloat()
                val containerH = constraints.maxHeight.toFloat()
                val imgAspect  = graph.imageWidth.toFloat() / graph.imageHeight
                val canvAspect = if (containerH > 0f) containerW / containerH else 1f
                val (drawW, drawH) = if (imgAspect > canvAspect)
                    containerW to containerW / imgAspect
                else
                    containerH * imgAspect to containerH
                val drawLeft = (containerW - drawW) / 2f
                val drawTop  = (containerH - drawH) / 2f
                val pxScaleX = drawW / graph.imageWidth
                val pxScaleY = drawH / graph.imageHeight
                val hitPx    = NodeRadius * 1.8f * 2f

                val density = LocalDensity.current
                for (node in poisOnFloor) {
                    val cxPx = drawLeft + node.x * pxScaleX
                    val cyPx = drawTop  + node.y * pxScaleY
                    with(density) {
                        Box(
                            modifier = Modifier
                                .offset(
                                    x = (cxPx - hitPx / 2f).toDp(),
                                    y = (cyPx - hitPx / 2f).toDp()
                                )
                                .size(hitPx.toDp())
                                .semantics {
                                    contentDescription = "POI: ${poiDisplayName(node.label)}"
                                }
                                .clickable { latestOnNodeTapped(node) }
                        )
                    }
                }
            }
        }

        // ── Selection-mode hint overlay ────────────────────────────────────
        Surface(
            color  = Color.Black.copy(alpha = 0.55f),
            shape  = RoundedCornerShape(8.dp),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 8.dp)
        ) {
            Text(
                text = when (selectionMode) {
                    SelectionMode.ORIGIN      -> "Tap a room to set START"
                    SelectionMode.DESTINATION -> "Tap a room to set DESTINATION"
                },
                color    = Color.White,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                style    = MaterialTheme.typography.bodySmall
            )
        }

        // ── Cross-floor instruction chip ───────────────────────────────────
        crossFloorHint?.let { hint ->
            Surface(
                color  = Color(0xFF0D47A1),
                shape  = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = Color.White)
                    Spacer(Modifier.width(8.dp))
                    Text(hint, color = Color.White, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

    }
}

// ─── Path line drawing ────────────────────────────────────────────────────────
private fun DrawScope.drawPathLines(
    nodes: List<IndoorNode>,
    scaleX: Float,
    scaleY: Float,
    drawLeft: Float,
    drawTop: Float
) {
    if (nodes.size < 2) return
    val path = Path()
    val first = nodes.first()
    path.moveTo(drawLeft + first.x * scaleX, drawTop + first.y * scaleY)
    for (i in 1 until nodes.size) {
        path.lineTo(drawLeft + nodes[i].x * scaleX, drawTop + nodes[i].y * scaleY)
    }
    drawPath(
        path   = path,
        color  = PathColor,
        style  = Stroke(
            width  = PathStrokeWidth,
            cap    = StrokeCap.Round,
            join   = StrokeJoin.Round
        )
    )
}

// ─── Node info dialog ─────────────────────────────────────────────────────────
@Composable
private fun NodeInfoDialog(node: IndoorNode, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(node.label, fontWeight = FontWeight.Bold) },
        text  = {
            Column {
                Text("Type: ${node.type.name.lowercase().replaceFirstChar { it.uppercase() }}")
                Text("Floor: ${node.floor}")
                node.description?.let { Text(it) }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}

// POI info popup
@Composable
private fun PoiInfoPopup(
    node: IndoorNode,
    onDismiss: () -> Unit,
    onSetAsOrigin: () -> Unit,
    onSetAsDestination: () -> Unit
) {
    val details = node.description ?: poiInferredDescription(node.label)
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("poiInfoPopup"),
        containerColor = Color(0xFF6650A4),
        title = {
            Text(
                text = poiDisplayName(node.label),
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        },
        text = {
            Text(
                text = "Details: $details",
                color = Color.White
            )
        },
        confirmButton = {
            Row {
                TextButton(onClick = onSetAsOrigin) {
                    Text("Set as Start", color = Color.White)
                }
                TextButton(onClick = onSetAsDestination) {
                    Text("Set as Destination", color = Color.White)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = Color.White)
            }
        }
    )
}

// ─── Route confirmation card (sleeker, shared BottomCard) ───────────────────
@Composable
private fun IndoorRouteCard(
    originNode: IndoorNode?,
    destNode: IndoorNode?,
    canRoute: Boolean,
    bottomPadding: Dp,
    onRoute: () -> Unit,
    onClear: () -> Unit,
) {
    BottomCard(onDismiss = null, bottomPadding = bottomPadding) {
        Text(
            text = "Indoor directions",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(8.dp))

        Text(
            text = "Start: ${originNode?.label ?: "—"}",
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(
            text = "Destination: ${destNode?.label ?: "—"}",
            style = MaterialTheme.typography.bodyMedium,
        )

        Spacer(Modifier.height(12.dp))

        Button(
            modifier = Modifier
                .fillMaxWidth()
                .semantics {
                    contentDescription = if (canRoute) "Generate indoor directions" else "Generate indoor directions (disabled)"
                },
            enabled = canRoute,
            onClick = onRoute,
        ) {
            Text("Route")
        }

        Spacer(Modifier.height(8.dp))

        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = onClear,
        ) {
            Text("Clear")
        }
    }
}

// ─── Utilities ────────────────────────────────────────────────────────────────

/** Colour per node type for the default (non-path) state. */
private fun nodeColor(type: IndoorNodeType): Color = when (type) {
    IndoorNodeType.ROOM      -> RoomColor
    IndoorNodeType.HALLWAY   -> HallwayColor
    IndoorNodeType.ELEVATOR  -> ElevatorColor
    IndoorNodeType.STAIRCASE -> StairColor
    IndoorNodeType.ESCALATOR -> StairColor
    IndoorNodeType.RAMP      -> Color(0xFF00897B)
    IndoorNodeType.ENTRY     -> Color(0xFFFF8F00)
    IndoorNodeType.POI       -> PoiColor
}

/**
 * Converts a raw container-space tap [tapOffset] into image pixel coordinates.
 *
 * The inner Box uses `graphicsLayer(scaleX, scaleY, translationX, translationY)` which
 * scales from the CENTER of the composable (pivotFractionX/Y = 0.5 by default), then
 * translates. So the forward transform for a point p in content space is:
 *
 *   screen = (p - center) * scale + center + offset
 *
 * Inverting:
 *   content = (screen - center - offset) / scale + center
 *
 * Then we map content → image pixels using the ContentScale.Fit draw rect.
 */
private fun screenToImage(
    tapOffset: Offset,
    scale: Float,
    offset: Offset,
    containerSize: IntSize,
    graph: IndoorFloorGraph
): Offset {
    val cw = containerSize.width.toFloat()
    val ch = containerSize.height.toFloat()
    val cx = cw / 2f
    val cy = ch / 2f

    // Invert the center-pivot scale + translation
    val contentX = (tapOffset.x - cx - offset.x) / scale + cx
    val contentY = (tapOffset.y - cy - offset.y) / scale + cy

    // Compute the ContentScale.Fit draw rect inside the container
    val imgAspect  = graph.imageWidth.toFloat() / graph.imageHeight
    val contAspect = cw / ch
    val (drawW, drawH) = if (imgAspect > contAspect) {
        cw to cw / imgAspect
    } else {
        ch * imgAspect to ch
    }
    val drawLeft = (cw - drawW) / 2f
    val drawTop  = (ch - drawH) / 2f

    // Map content coords into image pixel space
    val imgX = (contentX - drawLeft) / drawW * graph.imageWidth
    val imgY = (contentY - drawTop)  / drawH * graph.imageHeight

    return Offset(imgX, imgY)
}

/**
 * Finds the nearest node within [threshold] pixels (image-space) of [imgPos].
 */
private fun findNearestNode(
    imgPos: Offset,
    nodes: List<IndoorNode>,
    threshold: Float
): IndoorNode? {
    var best: IndoorNode? = null
    var bestDist = threshold

    for (node in nodes) {
        val dx = node.x - imgPos.x
        val dy = node.y - imgPos.y
        val d  = kotlin.math.sqrt(dx * dx + dy * dy)
        if (d < bestDist) {
            bestDist = d
            best = node
        }
    }
    return best
}


/**
 * Draws a recognisable pictogram icon for a POI node directly on the canvas
 * (no circle background). Each icon is drawn twice — white outline first,
 * then PoiColor fill — so it stands out on any floor-plan background.
 *
 * - Washrooms       → person silhouette (circle head + triangle body)
 * - Water fountain  → teardrop / water-drop shape
 * - Emergency stair → three descending stair steps
 */
private fun DrawScope.drawPoiIcon(
    label: String,
    cx: Float,
    cy: Float,
    radius: Float,
    type: IndoorNodeType = IndoorNodeType.POI
) {
    val fill    = PoiColor
    val outline = Color.White
    val stroke  = Stroke(width = radius * 0.22f)

    when {
        type == IndoorNodeType.ELEVATOR -> {
            val boxH = radius * 0.9f
            val boxW = radius * 0.7f
            val boxLeft = cx - boxW / 2f
            val boxTop  = cy - boxH / 2f
            drawRect(color = outline, topLeft = Offset(boxLeft, boxTop), size = Size(boxW, boxH), style = stroke)
            drawRect(color = fill,    topLeft = Offset(boxLeft, boxTop), size = Size(boxW, boxH))
            val arrowW = boxW * 0.35f
            val upTip   = Offset(cx, boxTop + boxH * 0.18f)
            val upLeft  = Offset(cx - arrowW, boxTop + boxH * 0.40f)
            val upRight = Offset(cx + arrowW, boxTop + boxH * 0.40f)
            val downTip   = Offset(cx, boxTop + boxH * 0.82f)
            val downLeft  = Offset(cx - arrowW, boxTop + boxH * 0.60f)
            val downRight = Offset(cx + arrowW, boxTop + boxH * 0.60f)
            val upArrow = Path().apply { moveTo(upTip.x, upTip.y); lineTo(upLeft.x, upLeft.y); lineTo(upRight.x, upRight.y); close() }
            val downArrow = Path().apply { moveTo(downTip.x, downTip.y); lineTo(downLeft.x, downLeft.y); lineTo(downRight.x, downRight.y); close() }
            drawPath(path = upArrow,   color = outline)
            drawPath(path = downArrow, color = outline)
        }

        type == IndoorNodeType.ESCALATOR -> {
            val stepW  = radius * 0.34f
            val stepH  = radius * 0.28f
            val startX = cx - radius * 0.50f
            val startY = cy + radius * 0.20f
            for (i in 0..2) {
                val topLeft = Offset(startX + i * stepW, startY - i * stepH)
                val sz      = Size(stepW, stepH)
                drawRect(color = outline, topLeft = topLeft, size = Size(sz.width + stroke.width, sz.height + stroke.width), style = stroke)
                drawRect(color = fill,    topLeft = topLeft, size = sz)
            }
            val lineStart = Offset(startX, startY + stepH)
            val lineEnd   = Offset(startX + 3 * stepW, startY - 2 * stepH)
            drawLine(color = outline, start = lineStart, end = lineEnd, strokeWidth = stroke.width)
        }

        label.startsWith("BATHROOM") -> {
            // ── Person silhouette ────────────────────────────────────────
            val headR  = radius * 0.28f
            val headCy = cy - radius * 0.36f

            // White halo then filled head
            drawCircle(color = outline, radius = headR + stroke.width, center = Offset(cx, headCy))
            drawCircle(color = fill,    radius = headR,                 center = Offset(cx, headCy))

            val bodyPath = Path().apply {
                moveTo(cx,                  cy - radius * 0.05f)
                lineTo(cx - radius * 0.32f, cy + radius * 0.54f)
                lineTo(cx + radius * 0.32f, cy + radius * 0.54f)
                close()
            }
            // White outline stroke, then solid fill
            drawPath(path = bodyPath, color = outline, style = stroke)
            drawPath(path = bodyPath, color = fill)
        }

        label == "WATER-FOUNTAIN" -> {
            // ── Teardrop / water-drop ────────────────────────────────────
            val dropPath = Path().apply {
                moveTo(cx, cy - radius * 0.54f)
                cubicTo(
                    cx + radius * 0.52f, cy - radius * 0.05f,
                    cx + radius * 0.42f, cy + radius * 0.44f,
                    cx, cy + radius * 0.54f
                )
                cubicTo(
                    cx - radius * 0.42f, cy + radius * 0.44f,
                    cx - radius * 0.52f, cy - radius * 0.05f,
                    cx, cy - radius * 0.54f
                )
                close()
            }
            drawPath(path = dropPath, color = outline, style = stroke)
            drawPath(path = dropPath, color = fill)
        }

        label.startsWith("EMERGENCY-STAIR") -> {
            // ── Three descending stair steps ─────────────────────────────
            val stepW  = radius * 0.34f
            val stepH  = radius * 0.28f
            val startX = cx - radius * 0.50f
            val startY = cy - radius * 0.40f
            for (i in 0..2) {
                val topLeft = Offset(startX + i * stepW, startY + i * stepH)
                val sz      = Size(stepW, stepH * (3 - i).toFloat())
                drawRect(color = outline, topLeft = topLeft,
                    size = Size(sz.width + stroke.width, sz.height + stroke.width),
                    style = stroke)
                drawRect(color = fill, topLeft = topLeft, size = sz)
            }
        }
    }
}

private fun formatIndoorTopCardLabel(node: IndoorNode?): String? {
    if (node == null) return null

    val idParts = node.id.split("-").filter { it.isNotBlank() }
    val roomPart = idParts.lastOrNull()?.trim()
    val buildingPart = node.buildingCode.trim().uppercase()

    return if (!roomPart.isNullOrBlank() && roomPart.any { it.isDigit() } && buildingPart.isNotBlank()) {
        "$buildingPart.$roomPart"
    } else {
        node.label
    }
}
