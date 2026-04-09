package com.dan.mad_project_geoquest.components.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dan.mad_project_geoquest.api.RetrofitClient
import com.dan.mad_project_geoquest.api.SessionManager
import com.dan.mad_project_geoquest.api.User
import kotlinx.coroutines.launch

@Composable
fun AdminUsersTab() {
    val scope = rememberCoroutineScope()
    var users         by remember { mutableStateOf<List<User>>(emptyList()) }
    var isLoading     by remember { mutableStateOf(true) }
    var resultMessage by remember { mutableStateOf<String?>(null) }
    var deleteTarget  by remember { mutableStateOf<User?>(null) }

    LaunchedEffect(Unit) {
        try { users = RetrofitClient.instance.getUsers() } catch (_: Exception) {}
        isLoading = false
    }

    if (isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    deleteTarget?.let { user ->
        AdminDeleteUserDialog(
            user = user,
            onDismiss = { deleteTarget = null },
            onConfirmed = {
                scope.launch {
                    try {
                        val response = RetrofitClient.instance.deleteUser(user.UserID)
                        if (response.isSuccessful) {
                            users = users.filter { it.UserID != user.UserID }
                            resultMessage = "User '${user.UserUsername}' deleted"
                        } else {
                            resultMessage = "Failed: HTTP ${response.code()}"
                        }
                    } catch (e: Exception) {
                        resultMessage = "Error: ${e.localizedMessage}"
                    }
                    deleteTarget = null
                }
            }
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        resultMessage?.let { msg ->
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (msg.contains("deleted"))
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.errorContainer
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(msg, fontSize = 13.sp)
                        TextButton(onClick = { resultMessage = null }) { Text("OK") }
                    }
                }
            }
        }

        item {
            Text(
                "All Users (${users.size})",
                fontSize = 18.sp, fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }

        items(users) { user ->
            val isCurrentAdmin = user.UserID == SessionManager.currentUser?.UserID
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(1.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isCurrentAdmin)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.surface
                )
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            Icons.Filled.Person,
                            contentDescription = null,
                            tint = if (isCurrentAdmin)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(28.dp)
                        )
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(user.UserUsername, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                if (isCurrentAdmin) {
                                    Text(
                                        "YOU", fontSize = 9.sp,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                "${user.UserFirstname} ${user.UserLastname}".trim().ifBlank { "—" },
                                fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                "ID: ${user.UserID}",
                                fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    if (!isCurrentAdmin) {
                        IconButton(onClick = { deleteTarget = user }) {
                            Icon(
                                Icons.Filled.Delete, contentDescription = "Delete user",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }

        item { Spacer(Modifier.height(32.dp)) }
    }
}