package com.coconutchunks.app.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.coconutchunks.app.AppViewModel
import com.coconutchunks.app.data.*
import kotlinx.coroutines.launch

private enum class Tab { LIBRARY, REVIEW, GROUPS, OVERVIEW }
private sealed interface Overlay {
    data object Add : Overlay
    data class Detail(val id: Long) : Overlay
    data class Edit(val id: Long) : Overlay
    data object Settings : Overlay
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoconutApp(vm: AppViewModel) {
    var tab by remember { mutableStateOf(Tab.LIBRARY) }
    var overlay by remember { mutableStateOf<Overlay?>(null) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                title = {
                    Column {
                        Text(
                            overlayTitle(overlay, tab),
                            modifier = Modifier.semantics { heading() },
                            style = MaterialTheme.typography.titleLarge
                        )
                        if (overlay == null && tab == Tab.LIBRARY) {
                            Text(
                                "Your German chunks, kept simple.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    if (overlay != null) {
                        IconButton(onClick = { overlay = null }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                actions = {
                    if (overlay == null) {
                        IconButton(onClick = { overlay = Overlay.Settings }) {
                            Icon(Icons.Default.Settings, contentDescription = "Open settings")
                        }
                    }
                }
            )
        },
        bottomBar = {
            if (overlay == null) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 3.dp
                ) {
                    NavItem(Tab.LIBRARY, tab, "Library", Icons.Default.List) { tab = it }
                    NavItem(Tab.REVIEW, tab, "Review", Icons.Default.Refresh) { tab = it }
                    NavItem(Tab.GROUPS, tab, "Groups", Icons.Default.Folder) { tab = it }
                    NavItem(Tab.OVERVIEW, tab, "Overview", Icons.Default.Info) { tab = it }
                }
            }
        },
        floatingActionButton = {
            if (overlay == null && tab == Tab.LIBRARY) {
                ExtendedFloatingActionButton(
                    modifier = Modifier.testTag("add_chunk_fab"),
                    onClick = { overlay = Overlay.Add },
                    icon = { Icon(Icons.Default.Add, null) },
                    text = { Text("Add Chunk") },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    ) { padding ->
        Box(
            Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            when (val o = overlay) {
                Overlay.Add -> ChunkEditor(vm, null) { overlay = null }
                is Overlay.Detail -> ChunkDetail(
                    vm,
                    o.id,
                    onEdit = { overlay = Overlay.Edit(o.id) }
                ) { overlay = null }
                is Overlay.Edit -> ChunkEditor(vm, o.id) { overlay = Overlay.Detail(o.id) }
                Overlay.Settings -> SettingsScreen(vm)
                null -> when (tab) {
                    Tab.LIBRARY -> LibraryScreen(
                        vm,
                        onOpen = { overlay = Overlay.Detail(it) },
                        onAdd = { overlay = Overlay.Add }
                    )
                    Tab.REVIEW -> ReviewHome(vm)
                    Tab.GROUPS -> GroupsScreen(vm) { id ->
                        vm.setGroupFilter(id)
                        tab = Tab.LIBRARY
                    }
                    Tab.OVERVIEW -> OverviewScreen(vm)
                }
            }
        }
    }
}

@Composable
private fun RowScope.NavItem(tab: Tab, current: Tab, label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: (Tab) -> Unit) {
    NavigationBarItem(
        modifier = Modifier.testTag("nav_${label.lowercase()}"),
        selected = tab == current,
        onClick = { onClick(tab) },
        icon = { Icon(icon, contentDescription = label) },
        label = { Text(label) }
    )
}

private fun overlayTitle(o: Overlay?, tab: Tab) = when (o) {
    Overlay.Add -> "Add Chunk"
    is Overlay.Detail -> "Chunk"
    is Overlay.Edit -> "Edit Chunk"
    Overlay.Settings -> "Settings"
    null -> when (tab) {
        Tab.LIBRARY -> "Library"
        Tab.REVIEW -> "Review"
        Tab.GROUPS -> "Groups"
        Tab.OVERVIEW -> "Overview"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LibraryScreen(
    vm: AppViewModel,
    onOpen: (Long) -> Unit,
    onAdd: () -> Unit
) {
    val chunks by vm.library.collectAsStateWithLifecycle()
    val groups by vm.groups.collectAsStateWithLifecycle()
    val selectedGroup by vm.selectedGroupFilter.collectAsStateWithLifecycle()
    val selectedStatus by vm.selectedStatusFilter.collectAsStateWithLifecycle()
    val selectedSort by vm.selectedSort.collectAsStateWithLifecycle()

    var query by remember { mutableStateOf("") }
    var sortMenu by remember { mutableStateOf(false) }
    var statusMenu by remember { mutableStateOf(false) }
    var groupMenu by remember { mutableStateOf(false) }

    Column(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = {
                query = it
                vm.setSearch(it)
            },
            modifier = Modifier.fillMaxWidth().testTag("library_search"),
            label = { Text("Search") },
            placeholder = { Text("Chunks, examples, notes") },
            singleLine = true,
            leadingIcon = { Icon(Icons.Default.Search, null) },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = {
                        query = ""
                        vm.setSearch("")
                    }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear search")
                    }
                }
            },
            shape = MaterialTheme.shapes.large
        )

        Spacer(Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box {
                FilterChip(
                    selected = selectedGroup != null,
                    onClick = { groupMenu = true },
                    label = {
                        Text(
                            groups.firstOrNull { it.id == selectedGroup }?.name ?: "All groups"
                        )
                    },
                    leadingIcon = { Icon(Icons.Default.Folder, null, Modifier.size(18.dp)) }
                )
                DropdownMenu(groupMenu, { groupMenu = false }) {
                    DropdownMenuItem(
                        text = { Text("All groups") },
                        onClick = {
                            vm.setGroupFilter(null)
                            groupMenu = false
                        }
                    )
                    groups.forEach { group ->
                        DropdownMenuItem(
                            text = { Text(group.name) },
                            onClick = {
                                vm.setGroupFilter(group.id)
                                groupMenu = false
                            }
                        )
                    }
                }
            }

            Box {
                FilterChip(
                    selected = selectedStatus != null,
                    onClick = { statusMenu = true },
                    label = { Text(selectedStatus?.name?.pretty() ?: "Status") }
                )
                DropdownMenu(statusMenu, { statusMenu = false }) {
                    DropdownMenuItem(
                        text = { Text("All statuses") },
                        onClick = {
                            vm.setStatusFilter(null)
                            statusMenu = false
                        }
                    )
                    ReviewStatus.entries.forEach { status ->
                        DropdownMenuItem(
                            text = { Text(status.name.pretty()) },
                            onClick = {
                                vm.setStatusFilter(status)
                                statusMenu = false
                            }
                        )
                    }
                }
            }

            Box {
                AssistChip(
                    onClick = { sortMenu = true },
                    label = { Text(selectedSort.name.pretty()) },
                    leadingIcon = { Icon(Icons.Default.Sort, null, Modifier.size(18.dp)) }
                )
                DropdownMenu(sortMenu, { sortMenu = false }) {
                    ChunkSort.entries.forEach { sort ->
                        DropdownMenuItem(
                            text = { Text(sort.name.pretty()) },
                            onClick = {
                                vm.setSort(sort)
                                sortMenu = false
                            }
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        if (chunks.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                EmptyState(
                    title = if (query.isBlank() && selectedGroup == null && selectedStatus == null)
                        "Start your collection"
                    else
                        "Nothing found",
                    body = if (query.isBlank() && selectedGroup == null && selectedStatus == null)
                        "Save a useful German phrase, then add your own examples."
                    else
                        "Try another search or clear one of the filters.",
                    icon = if (query.isBlank()) Icons.Default.Spa else Icons.Default.Search,
                    actionLabel = if (query.isBlank() && selectedGroup == null && selectedStatus == null)
                        "Add your first chunk"
                    else null,
                    onAction = if (query.isBlank() && selectedGroup == null && selectedStatus == null)
                        onAdd
                    else null
                )
            }
        } else {
            Text(
                "${chunks.size} ${if (chunks.size == 1) "chunk" else "chunks"}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(9.dp),
                contentPadding = PaddingValues(bottom = 104.dp)
            ) {
                items(chunks, key = { it.id }) { chunk ->
                    ElevatedCard(
                        onClick = { onOpen(chunk.id) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(Modifier.padding(horizontal = 16.dp, vertical = 15.dp)) {
                            Text(
                                chunk.chunkText,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(Modifier.height(10.dp))
                            Row(
                                Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Folder,
                                    contentDescription = null,
                                    modifier = Modifier.size(15.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.width(5.dp))
                                Text(
                                    chunk.groupName,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.weight(1f)
                                )
                                StatusBadge(chunk.status, compact = true)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusPill(status: ReviewStatus) {
    StatusBadge(status)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChunkEditor(vm: AppViewModel, id: Long?, onDone: () -> Unit) {
    val groups by vm.groups.collectAsStateWithLifecycle()

    var loaded by remember(id) { mutableStateOf(id == null) }
    var text by remember(id) { mutableStateOf("") }
    var e1 by remember(id) { mutableStateOf("") }
    var e2 by remember(id) { mutableStateOf("") }
    var e3 by remember(id) { mutableStateOf("") }
    var note by remember(id) { mutableStateOf("") }
    var selectedGroupId by remember(id) { mutableStateOf<Long?>(null) }
    var status by remember(id) { mutableStateOf(ReviewStatus.REVIEW) }
    var groupMenu by remember { mutableStateOf(false) }
    var statusMenu by remember { mutableStateOf(false) }
    var newGroup by remember { mutableStateOf("") }
    var showCreateGroup by remember { mutableStateOf(false) }

    LaunchedEffect(id) {
        if (id != null) {
            vm.getChunk(id)?.let {
                text = it.chunkText
                e1 = it.example1
                e2 = it.example2
                e3 = it.example3
                note = it.note
                selectedGroupId = it.groupId
                status = it.status
                loaded = true
            }
        }
    }

    if (!loaded) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    LazyColumn(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("chunk_editor"),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        item {
            Text(
                if (id == null) "Save something worth reusing." else "Keep the chunk useful to future you.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            ElevatedCard(
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Chunk", style = MaterialTheme.typography.titleMedium)
                    OutlinedTextField(
                        value = text,
                        onValueChange = { text = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("chunk_text"),
                        label = { Text("German chunk") },
                        placeholder = { Text("z. B. Bescheid geben") },
                        supportingText = {
                            Text(
                                if (text.isBlank()) "Required" else "${text.length} characters"
                            )
                        },
                        minLines = 2,
                        shape = MaterialTheme.shapes.large
                    )
                }
            }
        }

        item {
            ElevatedCard(
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(11.dp)
                ) {
                    Text("Your examples", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Use sentences you can imagine yourself saying. One example is enough; three is optional.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    ExampleField("Example 1", e1, { e1 = it }, "example_1")
                    ExampleField("Example 2", e2, { e2 = it }, "example_2")
                    ExampleField("Example 3", e3, { e3 = it }, "example_3")

                    val filled = listOf(e1, e2, e3).count { it.isNotBlank() }
                    Text(
                        "$filled of 3 examples added",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (filled > 0)
                            MaterialTheme.colorScheme.secondary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item {
            ElevatedCard(
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Organize", style = MaterialTheme.typography.titleMedium)

                    Box {
                        OutlinedButton(
                            onClick = { groupMenu = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Folder, null, Modifier.size(18.dp))
                            Spacer(Modifier.width(7.dp))
                            Text(
                                groups.firstOrNull { it.id == selectedGroupId }?.name
                                    ?: "Ungrouped"
                            )
                            Spacer(Modifier.weight(1f))
                            Icon(Icons.Default.ArrowDropDown, null)
                        }
                        DropdownMenu(groupMenu, { groupMenu = false }) {
                            groups.forEach { group ->
                                DropdownMenuItem(
                                    text = { Text(group.name) },
                                    onClick = {
                                        selectedGroupId = group.id
                                        groupMenu = false
                                    }
                                )
                            }
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text("Create new group") },
                                leadingIcon = { Icon(Icons.Default.Add, null) },
                                onClick = {
                                    groupMenu = false
                                    showCreateGroup = true
                                }
                            )
                        }
                    }

                    Box {
                        OutlinedButton(
                            onClick = { statusMenu = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            StatusBadge(status, compact = true)
                            Spacer(Modifier.weight(1f))
                            Icon(Icons.Default.ArrowDropDown, null)
                        }
                        DropdownMenu(statusMenu, { statusMenu = false }) {
                            ReviewStatus.entries.forEach { candidate ->
                                DropdownMenuItem(
                                    text = { Text(candidate.name.pretty()) },
                                    onClick = {
                                        status = candidate
                                        statusMenu = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            ElevatedCard(
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Note", style = MaterialTheme.typography.titleMedium)
                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("chunk_note"),
                        label = { Text("Optional note") },
                        placeholder = { Text("Meaning, nuance, reminder…") },
                        minLines = 3,
                        shape = MaterialTheme.shapes.large
                    )
                }
            }
        }

        item {
            Button(
                onClick = {
                    vm.saveChunk(
                        id ?: 0,
                        text,
                        e1,
                        e2,
                        e3,
                        selectedGroupId,
                        note,
                        status
                    ) { onDone() }
                },
                enabled = text.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp)
                    .testTag("save_chunk")
            ) {
                Icon(Icons.Default.Check, null)
                Spacer(Modifier.width(7.dp))
                Text(if (id == null) "Save Chunk" else "Save Changes")
            }
        }
    }

    if (showCreateGroup) {
        TextInputDialog(
            title = "Create group",
            label = "Group name",
            initial = newGroup,
            onDismiss = {
                newGroup = ""
                showCreateGroup = false
            }
        ) { name ->
            newGroup = name
            vm.createGroup(name)
            showCreateGroup = false
        }
    }
}

@Composable
private fun ExampleField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    tag: String
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .testTag(tag),
        label = { Text(label) },
        minLines = 2,
        shape = MaterialTheme.shapes.large
    )
}

@Composable
private fun ChunkDetail(vm: AppViewModel, id: Long, onEdit: () -> Unit, onDeleted: () -> Unit) {
    var chunk by remember(id) { mutableStateOf<ChunkWithGroup?>(null) }
    var confirmDelete by remember { mutableStateOf(false) }
    LaunchedEffect(id) { chunk = vm.getChunk(id) }
    val c = chunk ?: run {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text(c.chunkText, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { StatusPill(c.status); Text(c.groupName) }
        listOf(c.example1, c.example2, c.example3).filter { it.isNotBlank() }.forEachIndexed { i, e ->
            Surface(tonalElevation = 1.dp, shape = MaterialTheme.shapes.medium, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp)) {
                    Text("Example ${i + 1}", style = MaterialTheme.typography.labelMedium)
                    Text(e)
                }
            }
        }
        if (c.note.isNotBlank()) {
            Text("Note", fontWeight = FontWeight.SemiBold); Text(c.note)
        }
        Spacer(Modifier.weight(1f))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(onClick = onEdit, modifier = Modifier.weight(1f)) { Text("Edit") }
            Button(onClick = { confirmDelete = true }, modifier = Modifier.weight(1f)) { Text("Delete") }
        }
    }
    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Delete this chunk?") },
            confirmButton = { TextButton(onClick = { vm.deleteChunk(id, onDeleted) }) { Text("Delete") } },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun GroupsScreen(vm: AppViewModel, onOpen: (Long) -> Unit) {
    val stats by vm.groupStats.collectAsStateWithLifecycle()
    val groups by vm.groups.collectAsStateWithLifecycle()

    var create by remember { mutableStateOf(false) }
    var rename by remember { mutableStateOf<GroupEntity?>(null) }
    var deleting by remember { mutableStateOf<GroupEntity?>(null) }
    var menuFor by remember { mutableStateOf<Long?>(null) }

    LazyColumn(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("groups_screen"),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(bottom = 28.dp)
    ) {
        item {
            Text(
                "Keep groups broad enough to stay useful.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            Button(
                onClick = { create = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 50.dp)
                    .testTag("create_group")
            ) {
                Icon(Icons.Default.CreateNewFolder, null)
                Spacer(Modifier.width(7.dp))
                Text("Create Group")
            }
        }

        items(stats, key = { it.id }) { group ->
            ElevatedCard(
                onClick = { onOpen(group.id) },
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = MaterialTheme.shapes.medium,
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Icon(
                                Icons.Default.Folder,
                                contentDescription = null,
                                modifier = Modifier.padding(9.dp).size(22.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                group.name,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                "${group.total} ${if (group.total == 1) "chunk" else "chunks"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (group.name != "Ungrouped") {
                            Box {
                                IconButton(onClick = { menuFor = group.id }) {
                                    Icon(
                                        Icons.Default.MoreVert,
                                        contentDescription = "Group actions"
                                    )
                                }
                                DropdownMenu(
                                    expanded = menuFor == group.id,
                                    onDismissRequest = { menuFor = null }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Rename") },
                                        leadingIcon = { Icon(Icons.Default.Edit, null) },
                                        onClick = {
                                            rename = groups.firstOrNull { it.id == group.id }
                                            menuFor = null
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Delete") },
                                        leadingIcon = { Icon(Icons.Default.Delete, null) },
                                        onClick = {
                                            deleting = groups.firstOrNull { it.id == group.id }
                                            menuFor = null
                                        }
                                    )
                                }
                            }
                        }
                    }

                    if (group.total > 0) {
                        Spacer(Modifier.height(14.dp))
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            GroupMiniMetric("Special", group.specialCount, Modifier.weight(1f))
                            GroupMiniMetric("Mastered", group.masteredCount, Modifier.weight(1f))
                            GroupMiniMetric(
                                "Review",
                                (group.total - group.specialCount - group.masteredCount).coerceAtLeast(0),
                                Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }

    if (create) {
        TextInputDialog(
            "Create group",
            "Group name",
            onDismiss = { create = false }
        ) {
            vm.createGroup(it)
            create = false
        }
    }

    rename?.let { group ->
        TextInputDialog(
            "Rename group",
            "Group name",
            initial = group.name,
            onDismiss = { rename = null }
        ) {
            vm.renameGroup(group.id, it)
            rename = null
        }
    }

    deleting?.let { group ->
        var destination by remember(group.id) {
            mutableStateOf<Long?>(
                groups.firstOrNull { it.name == "Ungrouped" }?.id
            )
        }

        AlertDialog(
            onDismissRequest = { deleting = null },
            icon = { Icon(Icons.Default.FolderOff, null) },
            title = { Text("Delete ${group.name}?") },
            text = {
                Column {
                    Text(
                        "Its chunks will stay safe. Choose where they should move."
                    )
                    Spacer(Modifier.height(10.dp))
                    groups.filter { it.id != group.id }.forEach { candidate ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .toggleable(destination == candidate.id) {
                                    if (it) destination = candidate.id
                                }
                                .padding(vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = destination == candidate.id,
                                onClick = { destination = candidate.id }
                            )
                            Spacer(Modifier.width(5.dp))
                            Text(candidate.name)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        vm.deleteGroup(group.id, destination)
                        deleting = null
                    }
                ) {
                    Text("Delete group")
                }
            },
            dismissButton = {
                TextButton(onClick = { deleting = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun GroupMiniMetric(label: String, value: Int, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
    ) {
        Column(
            Modifier.padding(horizontal = 10.dp, vertical = 9.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value.toString(), fontWeight = FontWeight.SemiBold)
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TextInputDialog(title: String, label: String, initial: String = "", onDismiss: () -> Unit, onSave: (String) -> Unit) {
    var value by remember { mutableStateOf(initial) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = value,
                onValueChange = { value = it },
                modifier = Modifier.testTag("dialog_text_input"),
                label = { Text(label) },
                singleLine = true
            )
        },
        confirmButton = { TextButton(enabled = value.isNotBlank(), onClick = { onSave(value) }) { Text("Save") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReviewHome(vm: AppViewModel) {
    val groups by vm.groups.collectAsStateWithLifecycle()
    val settings by vm.settings.collectAsStateWithLifecycle()

    var groupId by remember { mutableStateOf<Long?>(null) }
    var groupMenu by remember { mutableStateOf(false) }
    var specialOnly by remember { mutableStateOf(false) }
    var completeGroup by remember { mutableStateOf(false) }
    var session by remember { mutableStateOf<ReviewSessionState?>(null) }
    var loading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    suspend fun startSession(daily: Boolean = false) {
        loading = true
        val queue = vm.repository.buildReviewQueue(
            groupId = if (daily) null else groupId,
            specialOnly = if (daily) false else specialOnly,
            count = if (daily) settings.dailyTarget else null,
            completeGroup = if (daily) false else completeGroup,
            masteredWeight = settings.masteredWeight
        )
        session = ReviewSessionState(
            queue = queue,
            completeGroup = if (daily) false else completeGroup,
            daily = daily
        )
        loading = false
    }

    val activeSession = session
    if (activeSession != null) {
        ReviewSession(
            vm = vm,
            session = activeSession,
            swipeEnabled = settings.swipeRightEnabled,
            onReviewAgain = {
                session = null
                scope.launch { startSession(activeSession.daily) }
            },
            onFinish = { session = null }
        )
        return
    }

    LazyColumn(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 28.dp)
    ) {
        item {
            Text(
                "A few focused minutes is enough.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            ElevatedCard(
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = MaterialTheme.shapes.medium,
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Icon(
                                Icons.Default.Refresh,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("Start a review", style = MaterialTheme.typography.titleMedium)
                            Text(
                                "Recall first, then reveal your examples.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Box {
                        OutlinedButton(
                            onClick = { groupMenu = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Folder, null, Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(groups.firstOrNull { it.id == groupId }?.name ?: "All Chunks")
                        }
                        DropdownMenu(groupMenu, { groupMenu = false }) {
                            DropdownMenuItem(
                                text = { Text("All Chunks") },
                                onClick = {
                                    groupId = null
                                    completeGroup = false
                                    groupMenu = false
                                }
                            )
                            groups.forEach { group ->
                                DropdownMenuItem(
                                    text = { Text(group.name) },
                                    onClick = {
                                        groupId = group.id
                                        groupMenu = false
                                    }
                                )
                            }
                        }
                    }

                    Row(
                        Modifier
                            .fillMaxWidth()
                            .toggleable(
                                value = specialOnly,
                                onValueChange = { specialOnly = it }
                            )
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(specialOnly, null)
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text("Special only")
                            Text(
                                "Focus on difficult chunks.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Row(
                        Modifier
                            .fillMaxWidth()
                            .toggleable(
                                value = completeGroup,
                                enabled = groupId != null,
                                onValueChange = { completeGroup = it }
                            )
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(completeGroup, null, enabled = groupId != null)
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text("Review entire group")
                            Text(
                                "See every chunk once, in random order.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Button(
                        onClick = { scope.launch { startSession(false) } },
                        enabled = !loading,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (loading) {
                            CircularProgressIndicator(
                                Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Icon(Icons.Default.PlayArrow, null)
                            Spacer(Modifier.width(6.dp))
                            Text("Start Review")
                        }
                    }
                }
            }
        }

        item {
            ElevatedCard(
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Today,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(Modifier.width(9.dp))
                        Text("Daily Review", style = MaterialTheme.typography.titleMedium)
                    }
                    Text(
                        "${settings.dailyTarget} chunks selected by priority. Special appears more often; Mastered still comes back occasionally.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.height(4.dp))
                    Button(
                        onClick = { scope.launch { startSession(true) } },
                        enabled = !loading,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Start ${settings.dailyTarget}-Chunk Review")
                    }
                }
            }
        }
    }
}

private data class ReviewSessionState(
    val queue: List<Long>,
    val completeGroup: Boolean,
    val daily: Boolean,
    var index: Int = 0,
    var special: Int = 0,
    var mastered: Int = 0,
    var unchanged: Int = 0
)

@Composable
private fun ReviewSession(
    vm: AppViewModel,
    session: ReviewSessionState,
    swipeEnabled: Boolean,
    onReviewAgain: () -> Unit,
    onFinish: () -> Unit
) {
    var index by remember { mutableIntStateOf(0) }
    var revealed by remember(index) { mutableStateOf(false) }
    var chunk by remember(index) { mutableStateOf<ChunkWithGroup?>(null) }
    var special by remember { mutableIntStateOf(0) }
    var mastered by remember { mutableIntStateOf(0) }
    var unchanged by remember { mutableIntStateOf(0) }
    var dragTotal by remember(index) { mutableFloatStateOf(0f) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val done = index >= session.queue.size

    LaunchedEffect(index, done) {
        if (!done) {
            chunk = vm.getChunk(session.queue[index])
        }
    }

    fun recordAndAdvance(current: ChunkWithGroup, newStatus: ReviewStatus?) {
        when (newStatus) {
            ReviewStatus.SPECIAL -> special++
            ReviewStatus.MASTERED -> mastered++
            else -> unchanged++
        }

        vm.recordReview(current.id, newStatus)
        chunk = null
        index++

        scope.launch {
            val result = snackbarHostState.showSnackbar(
                message = when (newStatus) {
                    ReviewStatus.SPECIAL -> "Marked Special"
                    ReviewStatus.MASTERED -> "Marked Mastered"
                    null -> "Kept current status"
                    else -> "Review saved"
                },
                actionLabel = "Undo",
                withDismissAction = true,
                duration = SnackbarDuration.Short
            )

            if (result == SnackbarResult.ActionPerformed) {
                vm.undoReview(current)
                when (newStatus) {
                    ReviewStatus.SPECIAL -> special = (special - 1).coerceAtLeast(0)
                    ReviewStatus.MASTERED -> mastered = (mastered - 1).coerceAtLeast(0)
                    else -> unchanged = (unchanged - 1).coerceAtLeast(0)
                }
            }
        }
    }

    Box(Modifier.fillMaxSize()) {
        if (done) {
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(24.dp)
                    .testTag("review_complete"),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = MaterialTheme.shapes.extraLarge,
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(18.dp).size(34.dp)
                    )
                }
                Spacer(Modifier.height(20.dp))
                Text(
                    when {
                        session.completeGroup -> "Group review complete."
                        session.daily -> "Today you reviewed ${session.queue.size} chunks."
                        else -> "Review complete."
                    },
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Your review metadata is saved locally.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(24.dp))

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MetricCard("Special", special, Modifier.weight(1f))
                    MetricCard("Mastered", mastered, Modifier.weight(1f))
                    MetricCard("Unchanged", unchanged, Modifier.weight(1f))
                }

                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = onReviewAgain,
                    enabled = session.queue.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Refresh, null)
                    Spacer(Modifier.width(7.dp))
                    Text("Review Again")
                }
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = onFinish,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Finish")
                }
            }
        } else {
            val current = chunk

            if (current == null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "${index + 1} of ${session.queue.size}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.weight(1f))
                        StatusBadge(current.status, compact = true)
                    }

                    Spacer(Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = {
                            (index + 1f) / session.queue.size.coerceAtLeast(1)
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(16.dp))

                    ElevatedCard(
                        onClick = { revealed = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .testTag("review_card")
                            .pointerInput(revealed, swipeEnabled, current.id) {
                                if (revealed && swipeEnabled) {
                                    detectHorizontalDragGestures(
                                        onDragStart = { dragTotal = 0f },
                                        onHorizontalDrag = { _, amount ->
                                            dragTotal += amount
                                        },
                                        onDragEnd = {
                                            if (dragTotal > 140f) {
                                                recordAndAdvance(
                                                    current,
                                                    ReviewStatus.MASTERED
                                                )
                                            }
                                            dragTotal = 0f
                                        }
                                    )
                                }
                            },
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(
                            Modifier
                                .fillMaxSize()
                                .padding(horizontal = 24.dp, vertical = 28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Folder,
                                    contentDescription = null,
                                    modifier = Modifier.size(15.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.width(5.dp))
                                Text(
                                    current.groupName,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(Modifier.height(34.dp))
                            Text(
                                current.chunkText,
                                style = MaterialTheme.typography.headlineMedium
                            )
                            Spacer(Modifier.height(22.dp))

                            AnimatedContent(
                                targetState = revealed,
                                label = "review_reveal"
                            ) { isRevealed ->
                                if (!isRevealed) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            "Recall your examples and make your own sentences.",
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(Modifier.height(20.dp))
                                        Surface(
                                            shape = MaterialTheme.shapes.extraLarge,
                                            color = MaterialTheme.colorScheme.primaryContainer
                                        ) {
                                            Row(
                                                Modifier
                                                    .padding(
                                                        horizontal = 14.dp,
                                                        vertical = 8.dp
                                                    )
                                                    .testTag("reveal_hint"),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    Icons.Default.TouchApp,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(18.dp),
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                                Spacer(Modifier.width(7.dp))
                                                Text(
                                                    "Tap to reveal",
                                                    style = MaterialTheme.typography.labelLarge,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }
                                    }
                                } else {
                                    Column(
                                        Modifier.testTag("revealed_examples"),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        val examples = listOf(
                                            current.example1,
                                            current.example2,
                                            current.example3
                                        ).filter { it.isNotBlank() }

                                        if (examples.isEmpty()) {
                                            Text(
                                                "No saved examples for this chunk.",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        } else {
                                            examples.forEachIndexed { i, example ->
                                                if (i > 0) Spacer(Modifier.height(12.dp))
                                                Surface(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    color = MaterialTheme.colorScheme
                                                        .surfaceVariant
                                                        .copy(alpha = 0.55f),
                                                    shape = MaterialTheme.shapes.large
                                                ) {
                                                    Column(Modifier.padding(15.dp)) {
                                                        Text(
                                                            "Example ${i + 1}",
                                                            style = MaterialTheme.typography.labelMedium,
                                                            color = MaterialTheme.colorScheme.primary
                                                        )
                                                        Spacer(Modifier.height(4.dp))
                                                        Text(
                                                            example,
                                                            style = MaterialTheme.typography.bodyLarge
                                                        )
                                                    }
                                                }
                                            }
                                        }

                                        if (swipeEnabled) {
                                            Spacer(Modifier.height(16.dp))
                                            Text(
                                                "Swipe right to mark Mastered",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (revealed) {
                        Spacer(Modifier.height(14.dp))
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(9.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    recordAndAdvance(
                                        current,
                                        ReviewStatus.SPECIAL
                                    )
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("mark_special")
                            ) {
                                Icon(Icons.Default.Star, null, Modifier.size(18.dp))
                                Spacer(Modifier.width(5.dp))
                                Text("Special")
                            }
                            Button(
                                onClick = {
                                    recordAndAdvance(
                                        current,
                                        ReviewStatus.MASTERED
                                    )
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("mark_mastered")
                            ) {
                                Icon(Icons.Default.Check, null, Modifier.size(18.dp))
                                Spacer(Modifier.width(5.dp))
                                Text("Mastered")
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        TextButton(
                            onClick = {
                                recordAndAdvance(current, null)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("keep_status")
                        ) {
                            Text("Next — keep status")
                        }
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun OverviewScreen(vm: AppViewModel) {
    val stats by vm.overview.collectAsStateWithLifecycle()
    val groups by vm.groups.collectAsStateWithLifecycle()
    val today by vm.reviewedToday.collectAsStateWithLifecycle()

    LazyColumn(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            Text(
                "A quiet snapshot of your collection.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        item {
            ElevatedCard(
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    Modifier.fillMaxWidth().padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Spa,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(Modifier.width(14.dp))
                    Column {
                        Text(
                            stats.total.toString(),
                            style = MaterialTheme.typography.headlineMedium
                        )
                        Text(
                            "chunks in your library",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
        item {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricCard("Review", stats.reviewCount, Modifier.weight(1f))
                MetricCard("Special", stats.specialCount, Modifier.weight(1f))
            }
        }
        item {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricCard("Mastered", stats.masteredCount, Modifier.weight(1f))
                MetricCard("Groups", groups.size, Modifier.weight(1f))
            }
        }
        item {
            ElevatedCard(
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Row(
                    Modifier.fillMaxWidth().padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Today,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Reviewed today", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "No streaks, no pressure.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        today.toString(),
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
            }
        }
    }
}

@Composable
private fun StatRow(label: String, value: Int) {
    MetricCard(label, value, Modifier.fillMaxWidth())
}

@Composable
private fun SettingsScreen(vm: AppViewModel) {
    val settings by vm.settings.collectAsStateWithLifecycle()

    var importConfirm by remember { mutableStateOf<Uri?>(null) }
    var message by remember { mutableStateOf<String?>(null) }
    var customTarget by remember(settings.dailyTarget) {
        mutableStateOf(settings.dailyTarget.toString())
    }

    val exportBackup = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let {
            vm.exportBackup(it) { result ->
                message = result.fold(
                    { "Backup exported." },
                    { "Export failed: ${it.message}" }
                )
            }
        }
    }

    val exportCsv = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        uri?.let {
            vm.exportCsv(it) { result ->
                message = result.fold(
                    { "CSV exported." },
                    { "Export failed: ${it.message}" }
                )
            }
        }
    }

    val importBackup = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) importConfirm = uri
    }

    LazyColumn(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 28.dp)
    ) {
        item {
            SettingCard(
                title = "Daily Review",
                subtitle = "Choose a target that feels easy to start."
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    listOf(10, 20, 30, 50).forEach { target ->
                        FilterChip(
                            selected = settings.dailyTarget == target,
                            onClick = {
                                customTarget = target.toString()
                                vm.setDailyTarget(target)
                            },
                            label = { Text(target.toString()) }
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = customTarget,
                        onValueChange = { value ->
                            customTarget = value.filter { it.isDigit() }.take(3)
                        },
                        modifier = Modifier.weight(1f),
                        label = { Text("Custom target") },
                        singleLine = true
                    )
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            customTarget.toIntOrNull()?.let(vm::setDailyTarget)
                        },
                        enabled = (customTarget.toIntOrNull() ?: 0) in 1..500
                    ) {
                        Text("Set")
                    }
                }
            }
        }

        item {
            SettingCard(
                title = "Mastered frequency",
                subtitle = "Lower means Mastered chunks appear less often."
            ) {
                Slider(
                    value = settings.masteredWeight.toFloat(),
                    onValueChange = { vm.setMasteredWeight(it.toDouble()) },
                    valueRange = 0.1f..2f
                )
                Text(
                    "Weight ${String.format("%.1f", settings.masteredWeight)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            SettingCard(
                title = "Review gesture",
                subtitle = "Buttons always remain available."
            ) {
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("Swipe right for Mastered")
                        Text(
                            "Works only after examples are revealed.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = settings.swipeRightEnabled,
                        onCheckedChange = vm::setSwipeRight
                    )
                }
            }
        }

        item {
            SettingCard(
                title = "Your data",
                subtitle = "Everything stays on this device unless you export it."
            ) {
                Button(
                    onClick = { exportBackup.launch("coconut_chunks_backup.json") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.FileUpload, null)
                    Spacer(Modifier.width(7.dp))
                    Text("Export Backup")
                }
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = {
                        importBackup.launch(
                            arrayOf("application/json", "text/plain", "*/*")
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.FileDownload, null)
                    Spacer(Modifier.width(7.dp))
                    Text("Import Backup")
                }
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { exportCsv.launch("coconut_chunks.csv") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.TableChart, null)
                    Spacer(Modifier.width(7.dp))
                    Text("Export CSV")
                }
            }
        }

        item {
            SettingCard(
                title = "About",
                subtitle = "Coconut Chunks 1.0"
            ) {
                Text(
                    "Fully offline · No account · No ads · No analytics · No trackers",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        message?.let { msg ->
            item {
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(msg, Modifier.padding(12.dp))
                }
            }
        }
    }

    importConfirm?.let { uri ->
        AlertDialog(
            onDismissRequest = { importConfirm = null },
            title = { Text("Replace current data?") },
            text = {
                Text(
                    "Your current database will be replaced. " +
                        "A temporary safety backup is created first."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        vm.importBackup(uri) { result ->
                            message = result.fold(
                                { "Backup restored." },
                                { "Import failed: ${it.message}" }
                            )
                        }
                        importConfirm = null
                    }
                ) { Text("Import") }
            },
            dismissButton = {
                TextButton(onClick = { importConfirm = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SettingCard(
    title: String,
    subtitle: String,
    content: @Composable ColumnScope.() -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(Modifier.padding(17.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(3.dp))
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(14.dp))
            content()
        }
    }
}

private fun String.pretty(): String = lowercase().replace('_', ' ').replaceFirstChar { it.uppercase() }
