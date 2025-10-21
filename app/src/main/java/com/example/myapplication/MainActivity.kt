package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.theme.MyApplicationTheme
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

data class Contact(
    val id: Long,
    val name: String,
    val address: String,
    val phone: String,
    val email: String
)

object ContactManager {
    private val _contacts = mutableStateListOf(
        Contact(1, "contoh1", "jalan rawamangun muka raya no 1", "081234567890", "ayam@yahoo.com"),
        Contact(2, "contoh2", "jalan jakarta sini deket banget", "098765432", "bebek@gmail.com")
    )

    val contacts: List<Contact>
        get() = _contacts

    private var nextId = 3L

    fun addContact(contact: Contact) {
        _contacts.add(contact.copy(id = nextId++))
    }

    fun updateContact(updatedContact: Contact) {
        val index = _contacts.indexOfFirst { it.id == updatedContact.id }
        if (index != -1) {
            _contacts[index] = updatedContact
        }
    }
}

sealed class Screen(val route: String) {
    data object ContactList : Screen("contact_list")
    data object ContactDetail : Screen("contact_detail/{contactId}") {
        fun createRoute(contactId: Long?) = "contact_detail/${contactId ?: -1L}"
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                ContactApp()
            }
        }
    }
}

@Composable
fun ContactApp() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.ContactList.route,
        modifier = Modifier.fillMaxSize()
    ) {
        composable(Screen.ContactList.route) {
            ContactListScreen(
                onNavigateToAdd = {
                    navController.navigate(Screen.ContactDetail.createRoute(null))
                },
                onNavigateToEdit = { contact ->
                    navController.navigate(Screen.ContactDetail.createRoute(contact.id))
                }
            )
        }

        composable(
            route = Screen.ContactDetail.route,
            arguments = listOf(navArgument("contactId") {
                type = NavType.LongType
                defaultValue = -1L
            })
        ) { backStackEntry ->
            val contactId = backStackEntry.arguments?.getLong("contactId") ?: -1L
            val contactToEdit = if (contactId != -1L) {
                ContactManager.contacts.find { it.id == contactId }
            } else {
                null
            }

            ContactDetailScreen(
                contactToEdit = contactToEdit,
                onSave = { contact ->
                    if (contactToEdit != null) {
                        ContactManager.updateContact(contact)
                    } else {
                        ContactManager.addContact(contact)
                    }
                    navController.popBackStack()
                },
                onBack = { navController.popBackStack() }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ContactListScreen(
    onNavigateToAdd: () -> Unit,
    onNavigateToEdit: (Contact) -> Unit
) {
    val contacts = ContactManager.contacts

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dashboard") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAdd,
            ) {
                Icon(Icons.Filled.Add, "Tambah kontak baru")
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) { paddingValues ->

        if (contacts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("Daftar kontak kosong.")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                items(contacts, key = { it.id }) { contact ->
                    ContactListItem(
                        contact = contact,
                        onContactLongPress = onNavigateToEdit
                    )
                    Divider(modifier = Modifier.padding(horizontal = 8.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ContactListItem(
    contact: Contact,
    onContactLongPress: (Contact) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 8.dp)
            .combinedClickable(
                onClick = {  },
                onLongClick = { onContactLongPress(contact) }
            ),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = contact.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = contact.phone,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactDetailScreen(
    contactToEdit: Contact?,
    onSave: (Contact) -> Unit,
    onBack: () -> Unit
) {
    val isEditing = contactToEdit != null
    val title = if (isEditing) "Edit Kontak" else "Tambah Kontak Baru"

    var name by remember { mutableStateOf(contactToEdit?.name ?: "") }
    var address by remember { mutableStateOf(contactToEdit?.address ?: "") }
    var phone by remember { mutableStateOf(contactToEdit?.phone ?: "") }
    var email by remember { mutableStateOf(contactToEdit?.email ?: "") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                actions = {
                    val isFormValid = name.isNotBlank() && phone.isNotBlank()
                    IconButton(
                        onClick = {
                            val contactToSave = contactToEdit?.copy(
                                name = name,
                                address = address,
                                phone = phone,
                                email = email
                            ) ?: Contact(
                                id = -1L,
                                name = name,
                                address = address,
                                phone = phone,
                                email = email
                            )
                            onSave(contactToSave)
                        },
                        enabled = isFormValid
                    ) {
                        Icon(Icons.Filled.Check, contentDescription = "Simpan")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ContactInputField(label = "Nama", value = name, onValueChange = { name = it })
            ContactInputField(label = "Telepon", value = phone, onValueChange = { phone = it }, keyboardType = KeyboardType.Phone)
            ContactInputField(label = "Email", value = email, onValueChange = { email = it }, keyboardType = KeyboardType.Email)
            ContactInputField(label = "Alamat", value = address, onValueChange = { address = it })
        }
    }
}

@Composable
fun ContactInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        singleLine = true
    )
}

@Preview(showBackground = true)
@Composable
fun ContactAppPreview() {
    MyApplicationTheme {
        ContactListScreen(
            onNavigateToAdd = {},
            onNavigateToEdit = {}
        )
    }
}