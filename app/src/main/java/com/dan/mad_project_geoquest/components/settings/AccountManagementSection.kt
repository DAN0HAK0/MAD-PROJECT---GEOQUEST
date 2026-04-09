package com.dan.mad_project_geoquest.components.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dan.mad_project_geoquest.api.RetrofitClient
import com.dan.mad_project_geoquest.api.SessionManager
import com.dan.mad_project_geoquest.api.UserPayload
import kotlinx.coroutines.launch



@Composable
fun AccountManagementSection(modifier: Modifier = Modifier) {
    val user = SessionManager.currentUser ?: return
    val scope = rememberCoroutineScope()
    var editingField   by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var errorMessage   by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(Modifier.height(4.dp))

        successMessage?.let {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(it, fontSize = 13.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    TextButton(onClick = { successMessage = null }) { Text("OK") }
                }
            }
        }

        errorMessage?.let {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(it, fontSize = 13.sp, color = MaterialTheme.colorScheme.onErrorContainer)
                    TextButton(onClick = { errorMessage = null }) { Text("OK") }
                }
            }
        }

        SectionTitle("Your Details")
        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                SettingsInfoRow("First Name", user.UserFirstname.ifBlank { "—" })
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                SettingsInfoRow("Last Name",  user.UserLastname.ifBlank { "—" })
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                SettingsInfoRow("Username",   user.UserUsername)
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                SettingsInfoRow("Phone",      user.UserPhone.ifBlank { "—" })
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                SettingsInfoRow("Password",   "••••••••")
            }
        }

        SectionTitle("Edit Details")

        EditFieldCard(
            title = "Change Username",
            subtitle = "You'll need to enter your password",
            isOpen = editingField == "username",
            onToggle = { editingField = if (editingField == "username") null else "username" }
        ) {
            ChangeUsernameForm(
                currentPassword = user.UserPassword,
                onSave = { newUsername ->
                    scope.launch {
                        try {
                            val payload = UserPayload(
                                UserFirstname = user.UserFirstname,
                                UserLastname  = user.UserLastname,
                                UserPhone     = user.UserPhone,
                                UserUsername  = newUsername,
                                UserPassword  = user.UserPassword,
                                UserLatitude  = user.UserLatitude,
                                UserLongitude = user.UserLongitude,
                                UserTimestamp = user.UserTimestamp,
                                UserImageURL  = user.UserImageURL
                            )
                            val response = RetrofitClient.instance.updateUser(user.UserID, payload)
                            if (response.isSuccessful) {
                                SessionManager.currentUser = user.copy(UserUsername = newUsername)
                                successMessage = "Username updated successfully"
                                editingField = null
                            } else {
                                errorMessage = "Failed to update: HTTP ${response.code()}"
                            }
                        } catch (e: Exception) {
                            errorMessage = "Error: ${e.localizedMessage}"
                        }
                    }
                },
                onCancel = { editingField = null }
            )
        }

        EditFieldCard(
            title = "Change Password",
            subtitle = "You'll need to verify your phone number",
            isOpen = editingField == "password",
            onToggle = { editingField = if (editingField == "password") null else "password" }
        ) {
            ChangePasswordForm(
                currentPhone = user.UserPhone,
                onSave = { newPassword ->
                    scope.launch {
                        try {
                            val payload = UserPayload(
                                UserFirstname = user.UserFirstname,
                                UserLastname  = user.UserLastname,
                                UserPhone     = user.UserPhone,
                                UserUsername  = user.UserUsername,
                                UserPassword  = newPassword,
                                UserLatitude  = user.UserLatitude,
                                UserLongitude = user.UserLongitude,
                                UserTimestamp = user.UserTimestamp,
                                UserImageURL  = user.UserImageURL
                            )
                            val response = RetrofitClient.instance.updateUser(user.UserID, payload)
                            if (response.isSuccessful) {
                                SessionManager.currentUser = user.copy(UserPassword = newPassword)
                                successMessage = "Password updated successfully"
                                editingField = null
                            } else {
                                errorMessage = "Failed to update: HTTP ${response.code()}"
                            }
                        } catch (e: Exception) {
                            errorMessage = "Error: ${e.localizedMessage}"
                        }
                    }
                },
                onCancel = { editingField = null }
            )
        }

        EditFieldCard(
            title = "Change Phone Number",
            subtitle = "You'll need to enter your password",
            isOpen = editingField == "phone",
            onToggle = { editingField = if (editingField == "phone") null else "phone" }
        ) {
            ChangePhoneForm(
                currentPassword = user.UserPassword,
                onSave = { newPhone ->
                    scope.launch {
                        try {
                            val payload = UserPayload(
                                UserFirstname = user.UserFirstname,
                                UserLastname  = user.UserLastname,
                                UserPhone     = newPhone,
                                UserUsername  = user.UserUsername,
                                UserPassword  = user.UserPassword,
                                UserLatitude  = user.UserLatitude,
                                UserLongitude = user.UserLongitude,
                                UserTimestamp = user.UserTimestamp,
                                UserImageURL  = user.UserImageURL
                            )
                            val response = RetrofitClient.instance.updateUser(user.UserID, payload)
                            if (response.isSuccessful) {
                                SessionManager.currentUser = user.copy(UserPhone = newPhone)
                                successMessage = "Phone number updated successfully"
                                editingField = null
                            } else {
                                errorMessage = "Failed to update: HTTP ${response.code()}"
                            }
                        } catch (e: Exception) {
                            errorMessage = "Error: ${e.localizedMessage}"
                        }
                    }
                },
                onCancel = { editingField = null }
            )
        }

        Spacer(Modifier.height(32.dp))
    }
}


@Composable
fun EditFieldCard(
    title: String,
    subtitle: String,
    isOpen: Boolean,
    onToggle: () -> Unit,
    content: @Composable () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    Text(subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                TextButton(onClick = onToggle) {
                    Text(if (isOpen) "Close" else "Edit")
                }
            }
            if (isOpen) {
                Spacer(Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(Modifier.height(12.dp))
                content()
            }
        }
    }
}

// Form to change user name shoudl user wish to

@Composable
fun ChangeUsernameForm(
    currentPassword: String,
    onSave: (String) -> Unit,
    onCancel: () -> Unit
) {
    var newUsername     by remember { mutableStateOf("") }
    var password        by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var passwordError   by remember { mutableStateOf(false) }
    var usernameError   by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(
            value = newUsername,
            onValueChange = { newUsername = it; usernameError = false },
            label = { Text("New username") },
            isError = usernameError,
            supportingText = { if (usernameError) Text("Username cannot be empty") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
        )
        PasswordField(
            value = password,
            onValueChange = { password = it; passwordError = false },
            label = "Current password",
            visible = passwordVisible,
            onToggleVisibility = { passwordVisible = !passwordVisible },
            isError = passwordError,
            errorText = "Incorrect password"
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp)) { Text("Cancel") }
            Button(
                onClick = {
                    usernameError = newUsername.isBlank()
                    passwordError = password != currentPassword
                    if (!usernameError && !passwordError) onSave(newUsername.trim())
                },
                modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp)
            ) { Text("Save") }
        }
    }
}

//Form to change password shoul user wish to

@Composable
fun ChangePasswordForm(
    currentPhone: String,
    onSave: (String) -> Unit,
    onCancel: () -> Unit
) {
    var newPassword            by remember { mutableStateOf("") }
    var confirmPassword        by remember { mutableStateOf("") }
    var phone                  by remember { mutableStateOf("") }
    var newPasswordVisible     by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var phoneError             by remember { mutableStateOf(false) }
    var passwordError          by remember { mutableStateOf(false) }
    var confirmError           by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it; phoneError = false },
            label = { Text("Your phone number") },
            isError = phoneError,
            supportingText = { if (phoneError) Text("Phone number does not match our records") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
        )
        PasswordField(
            value = newPassword,
            onValueChange = { newPassword = it; passwordError = false },
            label = "New password",
            visible = newPasswordVisible,
            onToggleVisibility = { newPasswordVisible = !newPasswordVisible },
            isError = passwordError,
            errorText = "Password cannot be empty"
        )
        PasswordField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it; confirmError = false },
            label = "Confirm new password",
            visible = confirmPasswordVisible,
            onToggleVisibility = { confirmPasswordVisible = !confirmPasswordVisible },
            isError = confirmError,
            errorText = "Passwords do not match"
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp)) { Text("Cancel") }
            Button(
                onClick = {
                    phoneError    = phone.trim() != currentPhone.trim()
                    passwordError = newPassword.isBlank()
                    confirmError  = newPassword != confirmPassword
                    if (!phoneError && !passwordError && !confirmError) onSave(newPassword)
                },
                modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp)
            ) { Text("Save") }
        }
    }
}

// Form to change phone number

@Composable
fun ChangePhoneForm(
    currentPassword: String,
    onSave: (String) -> Unit,
    onCancel: () -> Unit
) {
    var newPhone        by remember { mutableStateOf("") }
    var password        by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var phoneError      by remember { mutableStateOf(false) }
    var passwordError   by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(
            value = newPhone,
            onValueChange = { newPhone = it; phoneError = false },
            label = { Text("New phone number") },
            isError = phoneError,
            supportingText = { if (phoneError) Text("Phone number cannot be empty") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
        )
        PasswordField(
            value = password,
            onValueChange = { password = it; passwordError = false },
            label = "Current password",
            visible = passwordVisible,
            onToggleVisibility = { passwordVisible = !passwordVisible },
            isError = passwordError,
            errorText = "Incorrect password"
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp)) { Text("Cancel") }
            Button(
                onClick = {
                    phoneError    = newPhone.isBlank()
                    passwordError = password != currentPassword
                    if (!phoneError && !passwordError) onSave(newPhone.trim())
                },
                modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp)
            ) { Text("Save") }
        }
    }
}