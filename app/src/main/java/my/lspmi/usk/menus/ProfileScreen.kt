package my.lspmi.usk.menus

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import my.lspmi.usk.DatabaseHelper
import my.lspmi.usk.Screen
import my.lspmi.usk.User

@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    user: User?,
    onUserUpdated: () -> Unit
) {
    val context = LocalContext.current
    val dbHelper = remember { DatabaseHelper.getInstance(context) }

    var name by remember(user) { mutableStateOf(user?.name ?: "") }
    var phone by remember(user) { mutableStateOf(user?.phone ?: "") }

    var showAlert by remember { mutableStateOf(false) }

    var message by remember { mutableStateOf("") }
    var isSuccess by remember { mutableStateOf(false) }

    val phoneRegex = """^[\+]?[(]?[0-9]{3}[)]?[-\s\.]?[0-9]{3}[-\s\.]?[0-9]{4,6}$""".toRegex()

    val canSave = name.isNotBlank() && phone.isNotBlank() && phone.matches(phoneRegex)

    val scrollState = rememberScrollState()

    var editMode by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .navigationBarsPadding()
            .imePadding()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Profil",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Text(
            text = "@${user?.username ?: ""}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Full Name Field
        OutlinedTextField(
            value = name,
            onValueChange = {
                name = it
                message = ""
            },
            label = { Text("Nama") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            readOnly = !editMode
        )

        // Phone Field
        OutlinedTextField(
            value = phone,
            onValueChange = {
                phone = it
                message = ""
            },
            label = { Text("Nomer Telepon") },
            singleLine = true,
            isError = phone.isNotBlank() && !phone.matches(phoneRegex),
            supportingText = {
                if (phone.isNotBlank() && !phone.matches(phoneRegex)) {
                    Text(
                        text = "Nomer telepon tidak valid",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Phone,
                imeAction = ImeAction.Done
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            readOnly = !editMode
        )

        // Message Display
        if (message.isNotEmpty()) {
            Text(
                text = message,
                color = if (isSuccess) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        if (editMode) {
            Button(
                onClick = {
                    if (canSave && user != null) {
                        val updated = dbHelper.updateUser(
                            username = user.username,
                            newName = name,
                            newPhone = phone
                        )
                        if (updated) {
                            message = "Profill talah diperbarui"
                            isSuccess = true
                            onUserUpdated()
                            editMode = false
                        } else {
                            message = "Gagal memperbarui profil"
                            isSuccess = false
                        }
                    }
                    editMode = false
                },
                enabled = canSave,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .padding(bottom = 12.dp)
            ) {
                Text(text = "Simpan Perubahan", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            OutlinedButton(
                onClick = {
                    editMode = true
                },
                enabled = canSave,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .padding(bottom = 12.dp)
            ) {
                Text(text = "Edit Profil", style = MaterialTheme.typography.bodyLarge)
            }
        }


        Spacer(modifier = Modifier.height(32.dp))
    }
}