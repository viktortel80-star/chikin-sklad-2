package com.chickenwarehouse

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

data class Product(
    val name: String,
    val article: String,
    val barcode: String,
    val category: String,
    val unit: String,
    val quantity: Double,
    val minQuantity: Double,
    val purchasePrice: Double,
    val salePrice: Double,
    val supplier: String,
    val expiry: String,
    val note: String
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { ChickenWarehouseApp() }
    }
}

@Composable
fun ChickenWarehouseApp() {
    var products by remember { mutableStateOf(listOf<Product>()) }
    var showAdd by remember { mutableStateOf(false) }
    var search by remember { mutableStateOf("") }

    val filtered = products.filter {
        it.name.contains(search, ignoreCase = true) ||
        it.article.contains(search, ignoreCase = true) ||
        it.barcode.contains(search, ignoreCase = true)
    }

    MaterialTheme {
        Scaffold(
            topBar = {
                TopAppBar(title = { Text("🐔 Чікін склад") })
            },
            floatingActionButton = {
                FloatingActionButton(onClick = { showAdd = true }) {
                    Text("+")
                }
            }
        ) { padding ->
            Column(
                Modifier.padding(padding).padding(16.dp).fillMaxSize()
            ) {
                OutlinedTextField(
                    value = search,
                    onValueChange = { search = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Пошук товару") },
                    singleLine = true
                )
                Spacer(Modifier.height(12.dp))

                val total = products.sumOf { it.quantity * it.purchasePrice }
                Text("Товарів: ${products.size}")
                Text("Вартість складу: %.2f грн".format(total))
                Spacer(Modifier.height(12.dp))

                if (filtered.isEmpty()) {
                    Text("Поки що товарів немає. Натисни +, щоб додати.")
                } else {
                    LazyColumn {
                        items(filtered) { p ->
                            Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                                Column(Modifier.padding(12.dp)) {
                                    Text(p.name, style = MaterialTheme.typography.titleMedium)
                                    Text("Залишок: ${p.quantity} ${p.unit}")
                                    Text("Закупка: %.2f грн   Продаж: %.2f грн"
                                        .format(p.purchasePrice, p.salePrice))
                                    if (p.quantity <= p.minQuantity) {
                                        Text("⚠️ Малий залишок")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showAdd) {
            AddProductDialog(
                onDismiss = { showAdd = false },
                onAdd = {
                    products = products + it
                    showAdd = false
                }
            )
        }
    }
}

@Composable
fun AddProductDialog(
    onDismiss: () -> Unit,
    onAdd: (Product) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var article by remember { mutableStateOf("") }
    var barcode by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("шт.") }
    var quantity by remember { mutableStateOf("") }
    var minQuantity by remember { mutableStateOf("") }
    var purchase by remember { mutableStateOf("") }
    var sale by remember { mutableStateOf("") }
    var supplier by remember { mutableStateOf("") }
    var expiry by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Новий товар") },
        text = {
            Column(Modifier.heightIn(max = 520.dp)) {
                LazyColumn {
                    item { Field("Назва товару", name) { name = it } }
                    item { Field("Артикул", article) { article = it } }
                    item { Field("Штрихкод", barcode) { barcode = it } }
                    item { Field("Категорія", category) { category = it } }
                    item { Field("Одиниця (шт./кг/л)", unit) { unit = it } }
                    item { Field("Кількість", quantity) { quantity = it } }
                    item { Field("Мінімальний залишок", minQuantity) { minQuantity = it } }
                    item { Field("Ціна закупівлі", purchase) { purchase = it } }
                    item { Field("Ціна продажу", sale) { sale = it } }
                    item { Field("Постачальник", supplier) { supplier = it } }
                    item { Field("Термін придатності", expiry) { expiry = it } }
                    item { Field("Примітка", note) { note = it } }
                }
            }
        },
        confirmButton = {
            Button(
                enabled = name.isNotBlank(),
                onClick = {
                    onAdd(Product(
                        name, article, barcode, category, unit,
                        quantity.toDoubleOrNull() ?: 0.0,
                        minQuantity.toDoubleOrNull() ?: 0.0,
                        purchase.toDoubleOrNull() ?: 0.0,
                        sale.toDoubleOrNull() ?: 0.0,
                        supplier, expiry, note
                    ))
                }
            ) { Text("Додати") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Скасувати") } }
    )
}

@Composable
fun Field(label: String, value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
        singleLine = true
    )
}
