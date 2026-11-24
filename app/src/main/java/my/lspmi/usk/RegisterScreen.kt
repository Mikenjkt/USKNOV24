package my.lspmi.usk

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun RegisterScreen (
    modifier: Modifier = Modifier,
    navController: NavController
) {
    val context = LocalContext.current
    val dbHelper = remember { DatabaseHelper.getInstance(context) }

    var username by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    var errorMessage by remember { mutableStateOf("") }

    val scrollState = rememberScrollState()

    val passwordsMatch = password == confirmPassword
    val isFormValid = password.isNotBlank() && confirmPassword.isNotBlank() && passwordsMatch

    val usernameRegex = """^[a-z0-9_-]{3,15}""".toRegex()
    val phoneRegex = """^[\+]?[(]?[0-9]{3}[)]?[-\s\.]?[0-9]{3}[-\s\.]?[0-9]{4,6}$""".toRegex()

    Column(
        modifier = modifier
            .verticalScroll(scrollState)
            .navigationBarsPadding()
            .imePadding()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Silahkan registrasi",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        OutlinedTextField(
            value = name,
            onValueChange = {
                name = it
            },
            label = { Text("Nama") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )

        OutlinedTextField(
            value = username,
            onValueChange = {
                username = it
            },
            label = { Text("Username") },
            singleLine = true,
            isError = username.isNotBlank() && !username.matches(usernameRegex),
            supportingText = {
                if (username.isNotBlank() && !username.matches(usernameRegex)) {
                    Text(
                        text = "Username harus berupa huruf kecil dan angka",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )

        OutlinedTextField(
            value = phone,
            onValueChange = {
                phone = it
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
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Next
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
            },
            label = { Text("Password") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Next
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = {
                confirmPassword = it
            },
            label = { Text("Konfirmasi Password") },
            singleLine = true,
            isError = confirmPassword.isNotBlank() && !passwordsMatch,
            supportingText = {
                if (confirmPassword.isNotBlank() && !passwordsMatch) {
                    Text(
                        text = "Password tidak sama",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Next
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )

        if (errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        } else {
            Spacer(modifier = Modifier.height(8.dp))
        }

        Button(
            onClick = {
                if (isFormValid) {
                    val cleanUsername = username.trim()
                    if (dbHelper.isUsernameTaken(cleanUsername)) {
                        errorMessage = "Username '$cleanUsername' sudah diambil."
                    } else {
                        val newUser = User(
                            username = cleanUsername,
                            name = name,
                            password = password,
                            phone = phone,
                        )
                        val success = dbHelper.registerUser(newUser)
                        if (success) {
                            navController.navigate(Screen.Login.route)
                        } else {
                            errorMessage = "Formulir tidak valid. Silakan tulis kembali"
                        }
                    }
                }
            },
            enabled = username.isNotBlank() && password.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text(text = "Konfirmasi", style = MaterialTheme.typography.bodyLarge)
        }

        val annotatedString = buildAnnotatedString {
            append("Sudah punya akun? ")
            withStyle(
                style = SpanStyle(
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            ) {
                append("Login")
            }
        }
        Text(
            text = annotatedString,
            modifier = Modifier
                .clickable { navController.navigate(Screen.Login.route) }
                .padding(8.dp),
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}