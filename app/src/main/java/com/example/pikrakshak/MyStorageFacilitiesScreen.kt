
package com.example.pikrakshak

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.ListenerRegistration

@Composable
fun MyStorageFacilitiesScreen(
    onBack: () -> Unit
) {
    val repository = remember { StorageRepository() }

    var facilities by remember { mutableStateOf<List<StorageFacility>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }

    var editingFacility by remember { mutableStateOf<StorageFacility?>(null) }
    var deletingFacility by remember { mutableStateOf<StorageFacility?>(null) }

    DisposableEffect(Unit) {
        val listener: ListenerRegistration? =
            repository.listenToMyFacilities(
                onResult = {
                    facilities = it
                    isLoading = false
                },
                onError = {
                    errorMessage = it
                    isLoading = false
                }
            )

        onDispose {
            listener?.remove()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onBack) {
                Text("← Back")
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "My Storage Facilities",
                style = MaterialTheme.typography.titleLarge
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Manage the storage facilities registered under your account.",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (message.isNotBlank()) {
            Text(message, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (errorMessage.isNotBlank()) {
            Text(errorMessage, color = MaterialTheme.colorScheme.error)
        }

        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            facilities.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text("You have not added any storage facilities yet.")
                }
            }

            else -> {
                Text(
                    text = "Your facilities: ${facilities.size}",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(facilities, key = { it.id }) { facility ->
                        StorageFacilityOwnerCard(
                            facility = facility,
                            onEdit = {
                                editingFacility = facility
                                message = ""
                            },
                            onDelete = {
                                deletingFacility = facility
                                message = ""
                            }
                        )
                    }
                }
            }
        }
    }

    editingFacility?.let { facility ->
        EditStorageDialog(
            facility = facility,
            onDismiss = { editingFacility = null },
            onSave = { updatedFacility ->
                repository.updateFacility(
                    facility = updatedFacility,
                    onSuccess = {
                        message = "Storage facility updated successfully."
                        editingFacility = null
                    },
                    onError = {
                        message = it
                    }
                )
            }
        )
    }

    deletingFacility?.let { facility ->
        AlertDialog(
            onDismissRequest = { deletingFacility = null },
            title = { Text("Delete storage facility?") },
            text = {
                Text("Are you sure you want to permanently delete ${facility.name}?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        repository.deleteFacility(
                            facilityId = facility.id,
                            onSuccess = {
                                message = "Storage facility deleted successfully."
                                deletingFacility = null
                            },
                            onError = {
                                message = it
                                deletingFacility = null
                            }
                        )
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingFacility = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun StorageFacilityOwnerCard(
    facility: StorageFacility,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = facility.name,
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text("Type: ${facility.storageType}")
            Text("Location: ${facility.city}, ${facility.state}")
            Text("Address: ${facility.address}")
            Text("Available capacity: ${facility.availableCapacity} units")
            Text("Total capacity: ${facility.totalCapacity} units")
            Text("Price per unit: ₹${facility.pricePerUnit}")
            Text("Contact: ${facility.phone}")

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (facility.isActive) "Status: Active" else "Status: Inactive",
                color = if (facility.isActive) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.error
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onEdit) {
                    Text("Edit")
                }

                TextButton(onClick = onDelete) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
private fun EditStorageDialog(
    facility: StorageFacility,
    onDismiss: () -> Unit,
    onSave: (StorageFacility) -> Unit
) {
    var name by remember(facility.id) { mutableStateOf(facility.name) }
    var address by remember(facility.id) { mutableStateOf(facility.address) }
    var city by remember(facility.id) { mutableStateOf(facility.city) }
    var state by remember(facility.id) { mutableStateOf(facility.state) }
    var availableCapacity by remember(facility.id) {
        mutableStateOf(facility.availableCapacity.toString())
    }
    var price by remember(facility.id) {
        mutableStateOf(facility.pricePerUnit.toString())
    }
    var phone by remember(facility.id) { mutableStateOf(facility.phone) }
    var isActive by remember(facility.id) { mutableStateOf(facility.isActive) }
    var validationError by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Storage Details") },
        text = {
            Column(
                modifier = Modifier.heightIn(max = 450.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Update the facility information below.")

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Facility name") },
                    singleLine = true
                )

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Address") }
                )

                OutlinedTextField(
                    value = city,
                    onValueChange = { city = it },
                    label = { Text("City") },
                    singleLine = true
                )

                OutlinedTextField(
                    value = state,
                    onValueChange = { state = it },
                    label = { Text("State") },
                    singleLine = true
                )

                OutlinedTextField(
                    value = availableCapacity,
                    onValueChange = { availableCapacity = it },
                    label = { Text("Available capacity") },
                    singleLine = true
                )

                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Price per unit (₹)") },
                    singleLine = true
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Contact number") },
                    singleLine = true
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = isActive,
                        onCheckedChange = { isActive = it }
                    )
                    Text("Facility is active")
                }

                if (validationError.isNotBlank()) {
                    Text(
                        validationError,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val capacity = availableCapacity.toDoubleOrNull()
                    val unitPrice = price.toDoubleOrNull()

                    validationError = when {
                        name.isBlank() -> "Enter facility name."
                        address.isBlank() -> "Enter address."
                        city.isBlank() -> "Enter city."
                        state.isBlank() -> "Enter state."
                        capacity == null || capacity < 0 ||
                                capacity > facility.totalCapacity ->
                            "Enter valid available capacity."
                        unitPrice == null || unitPrice < 0 ->
                            "Enter a valid price."
                        else -> ""
                    }

                    if (validationError.isBlank()) {
                        onSave(
                            facility.copy(
                                name = name.trim(),
                                address = address.trim(),
                                city = city.trim(),
                                state = state.trim(),
                                availableCapacity = capacity!!,
                                pricePerUnit = unitPrice!!,
                                phone = phone.trim(),
                                isActive = isActive
                            )
                        )
                    }
                }
            ) {
                Text("Save Changes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
