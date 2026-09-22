package com.example.chikinsklad

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.widget.*
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MainActivity : android.app.Activity() {

    private val prefsName = "chikin_sklad_data"
    private lateinit var prefs: android.content.SharedPreferences

    private val operationTypes = arrayOf(
        "Купівля корму",
        "Продаж яєць",
        "Продаж птиці",
        "Інші витрати",
        "Інший дохід",
        "Інкубація"
    )

    data class Operation(
        val id: Long,
        val date: String,
        val type: String,
        val description: String,
        val quantity: Double,
        val amount: Double
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        prefs = getSharedPreferences(prefsName, MODE_PRIVATE)

        showMainMenu()
    }

    // ------------------------------------------------------------
    // ОСНОВНІ ДОПОМІЖНІ ФУНКЦІЇ
    // ------------------------------------------------------------

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }

    private fun rootLayout(): LinearLayout {
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(dp(16), dp(70), dp(16), dp(20))
        layout.gravity = Gravity.TOP
        return layout
    }

    private fun scrollLayout(): ScrollView {
        val scroll = ScrollView(this)
        scroll.setPadding(0, 0, 0, 0)
        return scroll
    }

    private fun title(text: String): TextView {
        val view = TextView(this)
        view.text = text
        view.textSize = 24f
        view.gravity = Gravity.CENTER
        view.setPadding(dp(8), dp(8), dp(8), dp(18))
        return view
    }

    private fun subtitle(text: String): TextView {
        val view = TextView(this)
        view.text = text
        view.textSize = 16f
        view.setPadding(dp(4), dp(8), dp(4), dp(8))
        return view
    }

    private fun button(text: String, action: () -> Unit): Button {
        val b = Button(this)
        b.text = text
        b.textSize = 16f
        b.isAllCaps = false

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(0, dp(5), 0, dp(5))
        b.layoutParams = params

        b.setOnClickListener {
            action()
        }

        return b
    }

    private fun editText(
        hint: String,
        number: Boolean = false
    ): EditText {
        val e = EditText(this)
        e.hint = hint
        e.textSize = 16f
        e.setPadding(dp(12), dp(10), dp(12), dp(10))

        if (number) {
            e.inputType =
                InputType.TYPE_CLASS_NUMBER or
                        InputType.TYPE_NUMBER_FLAG_DECIMAL
        }

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(0, dp(4), 0, dp(4))
        e.layoutParams = params

        return e
    }

    private fun setContent(view: View) {
        setContentView(view)
    }

    private fun formatMoney(value: Double): String {
        return String.format(Locale.getDefault(), "%.2f грн", value)
    }

    private fun formatNumber(value: Double): String {
        return String.format(Locale.getDefault(), "%.2f", value)
    }

    private fun today(): String {
        return SimpleDateFormat(
            "dd.MM.yyyy",
            Locale.getDefault()
        ).format(Date())
    }

    private fun currentMonth(): String {
        return SimpleDateFormat(
            "MM.yyyy",
            Locale.getDefault()
        ).format(Date())
    }

    private fun monthFromDate(date: String): String {
        return try {
            val parts = date.split(".")
            if (parts.size == 3) {
                "${parts[1]}.${parts[2]}"
            } else {
                currentMonth()
            }
        } catch (_: Exception) {
            currentMonth()
        }
    }

    // ------------------------------------------------------------
    // ГОЛОВНЕ МЕНЮ
    // ------------------------------------------------------------

    private fun showMainMenu() {

        val root = rootLayout()

        root.addView(title("🐔 ЧІКІН СКЛАД"))

        root.addView(
            subtitle(
                "Облік курей, корму, яєць, витрат,\n" +
                        "доходів та окупності"
            )
        )

        root.addView(
            button("🐔 Поголів'я птиці") {
                birdsScreen()
            }
        )

        root.addView(
            button("🌾 Корм") {
                feedScreen()
            }
        )

        root.addView(
            button("🥚 Яйця та продаж") {
                eggsScreen()
            }
        )

        root.addView(
            button("🐣 Інкубація") {
                incubationScreen()
            }
        )

        root.addView(
            button("🐓 Півники / забій") {
                roostersScreen()
            }
        )

        root.addView(
            button("📝 Щоденний журнал") {
                journalScreen()
            }
        )

        root.addView(
            button("➕ Додати операцію") {
                addOperation()
            }
        )

        root.addView(
            button("📅 Таблиця по місяцях") {
                monthlyTableScreen()
            }
        )

        root.addView(
            button("📈 Графік по днях") {
                dailyGraphScreen()
            }
        )

        root.addView(
            button("📊 Графік по місяцях") {
                monthlyGraphScreen()
            }
        )

        root.addView(
            button("💰 Загальний звіт та окупність") {
                reportScreen()
            }
        )

        root.addView(
            button("🗑 Очистити всі дані") {
                confirmClear()
            }
        )

        setContent(root)
    }

    // ------------------------------------------------------------
    // ПОГОЛІВ'Я
    // ------------------------------------------------------------

    private fun birdsScreen() {

        val scroll = scrollLayout()
        val root = rootLayout()

        root.addView(title("🐔 Поголів'я"))

        val chickens = editText("Кількість курей", true)
        val roosters = editText("Кількість півнів", true)
        val young = editText("Молодняк", true)
        val age = editText("Вік птиці, місяців", true)

        chickens.setText(
            prefs.getInt("chickens", 0)
                .takeIf { it > 0 }?.toString() ?: ""
        )

        roosters.setText(
            prefs.getInt("roosters", 0)
                .takeIf { it > 0 }?.toString() ?: ""
        )

        young.setText(
            prefs.getInt("young", 0)
                .takeIf { it > 0 }?.toString() ?: ""
        )

        age.setText(
            prefs.getFloat("age", 0f)
                .takeIf { it > 0f }?.toString() ?: ""
        )

        root.addView(chickens)
        root.addView(roosters)
        root.addView(young)
        root.addView(age)

        root.addView(
            button("💾 Зберегти") {

                val c = chickens.text.toString().toIntOrNull() ?: 0
                val r = roosters.text.toString().toIntOrNull() ?: 0
                val y = young.text.toString().toIntOrNull() ?: 0
                val a = age.text.toString().toFloatOrNull() ?: 0f

                prefs.edit()
                    .putInt("chickens", c)
                    .putInt("roosters", r)
                    .putInt("young", y)
                    .putFloat("age", a)
                    .apply()

                Toast.makeText(
                    this,
                    "Дані збережено",
                    Toast.LENGTH_SHORT
                ).show()
            }
        )

        root.addView(
            button("⬅ Назад") {
                showMainMenu()
            }
        )

        scroll.addView(root)
        setContent(scroll)
    }

    // ------------------------------------------------------------
    // КОРМ
    // ------------------------------------------------------------

    private fun feedScreen() {

        val scroll = scrollLayout()
        val root = rootLayout()

        root.addView(title("🌾 Корм"))

        val birds = editText("Кількість птиці", true)
        val grams = editText("Корм на одну голову за день, грам", true)
        val price = editText("Ціна корму за 1 кг, грн", true)
        val monthly = editText("Місячна витрата корму, грн", true)

        birds.setText(
            prefs.getInt("feed_birds", 0)
                .takeIf { it > 0 }?.toString() ?: ""
        )

        grams.setText(
            prefs.getFloat("feed_grams", 0f)
                .takeIf { it > 0f }?.toString() ?: ""
        )

        price.setText(
            prefs.getFloat("feed_price", 0f)
                .takeIf { it > 0f }?.toString() ?: ""
        )

        monthly.setText(
            prefs.getFloat("feed_month_cost", 0f)
                .takeIf { it > 0f }?.toString() ?: ""
        )

        root.addView(birds)
        root.addView(grams)
        root.addView(price)
        root.addView(monthly)

        root.addView(
            button("🧮 Розрахувати місячну вартість") {

                val b = birds.text.toString().toDoubleOrNull() ?: 0.0
                val g = grams.text.toString().toDoubleOrNull() ?: 0.0
                val p = price.text.toString().toDoubleOrNull() ?: 0.0

                val kgPerDay = b * g / 1000.0
                val kgPerMonth = kgPerDay * 30.0
                val cost = kgPerMonth * p

                monthly.setText(formatNumber(cost))

                Toast.makeText(
                    this,
                    "На місяць: ${formatMoney(cost)}",
                    Toast.LENGTH_LONG
                ).show()
            }
        )

        root.addView(
            button("💾 Зберегти") {

                val b = birds.text.toString().toIntOrNull() ?: 0
                val g = grams.text.toString().toFloatOrNull() ?: 0f
                val p = price.text.toString().toFloatOrNull() ?: 0f
                val m = monthly.text.toString().toFloatOrNull() ?: 0f

                prefs.edit()
                    .putInt("feed_birds", b)
                    .putFloat("feed_grams", g)
                    .putFloat("feed_price", p)
                    .putFloat("feed_month_cost", m)
                    .apply()

                Toast.makeText(
                    this,
                    "Корм збережено",
                    Toast.LENGTH_SHORT
                ).show()
            }
        )

        root.addView(
            button("➕ Записати купівлю корму сьогодні") {
                addOperation("Купівля корму")
            }
        )

        root.addView(
            button("⬅ Назад") {
                showMainMenu()
            }
        )

        scroll.addView(root)
        setContent(scroll)
    }

    // ------------------------------------------------------------
    // ЯЙЦЯ
    // ------------------------------------------------------------

    private fun eggsScreen() {

        val scroll = scrollLayout()
        val root = rootLayout()

        root.addView(title("🥚 Яйця та продаж"))

        val eggs = editText("Кількість яєць", true)
        val price = editText("Ціна одного яйця, грн", true)
        val revenue = editText("Дохід від яєць за місяць, грн", true)

        eggs.setText(
            prefs.getInt("eggs", 0)
                .takeIf { it > 0 }?.toString() ?: ""
        )

        price.setText(
            prefs.getFloat("egg_price", 0f)
                .takeIf { it > 0f }?.toString() ?: ""
        )

        revenue.setText(
            prefs.getFloat("egg_revenue", 0f)
                .takeIf { it > 0f }?.toString() ?: ""
        )

        root.addView(eggs)
        root.addView(price)
        root.addView(revenue)

        root.addView(
            button("🧮 Розрахувати") {

                val e = eggs.text.toString().toDoubleOrNull() ?: 0.0
                val p = price.text.toString().toDoubleOrNull() ?: 0.0

                val result = e * p

                revenue.setText(formatNumber(result))

                Toast.makeText(
                    this,
                    "Дохід: ${formatMoney(result)}",
                    Toast.LENGTH_LONG
                ).show()
            }
        )

        root.addView(
            button("💾 Зберегти") {

                val e = eggs.text.toString().toIntOrNull() ?: 0
                val p = price.text.toString().toFloatOrNull() ?: 0f
                val r = revenue.text.toString().toFloatOrNull() ?: 0f

                prefs.edit()
                    .putInt("eggs", e)
                    .putFloat("egg_price", p)
                    .putFloat("egg_revenue", r)
                    .apply()

                Toast.makeText(
                    this,
                    "Дані яєць збережено",
                    Toast.LENGTH_SHORT
                ).show()
            }
        )

        root.addView(
            button("🥚 Записати продаж яєць сьогодні") {
                addOperation("Продаж яєць")
            }
        )

        root.addView(
            button("⬅ Назад") {
                showMainMenu()
            }
        )

        scroll.addView(root)
        setContent(scroll)
    }

    // ------------------------------------------------------------
    // ІНКУБАЦІЯ
    // ------------------------------------------------------------

    private fun incubationScreen() {

        val scroll = scrollLayout()
        val root = rootLayout()

        root.addView(title("🐣 Інкубація"))

        root.addView(
            subtitle(
                "Тут можна записувати витрати та доходи,\n" +
                        "пов'язані з інкубацією."
            )
        )

        root.addView(
            button("➕ Додати витрату на інкубацію") {
                addOperation("Інкубація")
            }
        )

        root.addView(
            button("📋 Переглянути журнал") {
                journalScreen()
            }
        )

        root.addView(
            button("⬅ Назад") {
                showMainMenu()
            }
        )

        scroll.addView(root)
        setContent(scroll)
    }

    // ------------------------------------------------------------
    // ПІВНИКИ
    // ------------------------------------------------------------

    private fun roostersScreen() {

        val scroll = scrollLayout()
        val root = rootLayout()

        root.addView(title("🐓 Півники / забій"))

        val young = prefs.getInt("young", 0)
        val age = prefs.getFloat("age", 0f)

        root.addView(
            subtitle(
                "Молодняк: $young гол.\n" +
                        "Вік: ${formatNumber(age.toDouble())} міс."
            )
        )

        root.addView(
            button("➕ Записати продаж птиці") {
                addOperation("Продаж птиці")
            }
        )

        root.addView(
            button("📋 Журнал продажу") {
                journalScreen()
            }
        )

        root.addView(
            button("⬅ Назад") {
                showMainMenu()
            }
        )

        scroll.addView(root)
        setContent(scroll)
    }

    // ------------------------------------------------------------
    // ДОДАВАННЯ ОПЕРАЦІЇ
    // ------------------------------------------------------------

    private fun addOperation(preselectedType: String? = null) {

        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.setPadding(dp(20), dp(20), dp(20), dp(10))

        val date = editText("Дата (дд.мм.рррр)")
        date.setText(today())

        val typeSpinner = Spinner(this)

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            operationTypes
        )

        adapter.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )

        typeSpinner.adapter = adapter

        if (preselectedType != null) {
            val index = operationTypes.indexOf(preselectedType)
            if (index >= 0) {
                typeSpinner.setSelection(index)
            }
        }

        val description = editText("Що саме? Наприклад: корм, яйця, кури")
        val quantity = editText("Кількість (кг / шт.)", true)
        val amount = editText("Сума, грн", true)

        root.addView(date)
        root.addView(typeSpinner)
        root.addView(description)
        root.addView(quantity)
        root.addView(amount)

        AlertDialog.Builder(this)
            .setTitle("➕ Нова операція")
            .setView(root)
            .setPositiveButton("Зберегти") { _, _ ->

                val d = date.text.toString().trim()
                val t = typeSpinner.selectedItem.toString()
                val desc = description.text.toString().trim()
                val q = quantity.text.toString().replace(",", ".")
                    .toDoubleOrNull() ?: 0.0
                val a = amount.text.toString().replace(",", ".")
                    .toDoubleOrNull() ?: 0.0

                if (d.isEmpty()) {
                    Toast.makeText(
                        this,
                        "Вкажи дату",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setPositiveButton
                }

                saveOperation(
                    Operation(
                        id = System.currentTimeMillis(),
                        date = d,
                        type = t,
                        description = desc,
                        quantity = q,
                        amount = a
                    )
                )

                Toast.makeText(
                    this,
                    "Операцію збережено",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .setNegativeButton("Скасувати", null)
            .show()
    }

    // ------------------------------------------------------------
    // ЗБЕРЕЖЕННЯ ОПЕРАЦІЙ
    // ------------------------------------------------------------

    private fun getOperations(): MutableList<Operation> {

        val result = mutableListOf<Operation>()

        val text = prefs.getString("operations", "[]") ?: "[]"

        try {

            val array = JSONArray(text)

            for (i in 0 until array.length()) {

                val obj = array.getJSONObject(i)

                result.add(
                    Operation(
                        id = obj.optLong("id"),
                        date = obj.optString("date"),
                        type = obj.optString("type"),
                        description = obj.optString("description"),
                        quantity = obj.optDouble("quantity", 0.0),
                        amount = obj.optDouble("amount", 0.0)
                    )
                )
            }

        } catch (_: Exception) {
        }

        return result
    }

    private fun saveOperation(operation: Operation) {

        val list = getOperations()

        list.add(operation)

        val array = JSONArray()

        for (item in list) {

            val obj = JSONObject()

            obj.put("id", item.id)
            obj.put("date", item.date)
            obj.put("type", item.type)
            obj.put("description", item.description)
            obj.put("quantity", item.quantity)
            obj.put("amount", item.amount)

            array.put(obj)
        }

        prefs.edit()
            .putString("operations", array.toString())
            .apply()
    }

    private fun deleteOperation(id: Long) {

        val list = getOperations()

        list.removeAll {
            it.id == id
        }

        val array = JSONArray()

        for (item in list) {

            val obj = JSONObject()

            obj.put("id", item.id)
            obj.put("date", item.date)
            obj.put("type", item.type)
            obj.put("description", item.description)
            obj.put("quantity", item.quantity)
            obj.put("amount", item.amount)

            array.put(obj)
        }

        prefs.edit()
            .putString("operations", array.toString())
            .apply()
    }

    // ------------------------------------------------------------
    // ЖУРНАЛ
    // ------------------------------------------------------------

    private fun journalScreen() {

        val scroll = scrollLayout()
        val root = rootLayout()

        root.addView(title("📝 Щоденний журнал"))

        val operations = getOperations()

        if (operations.isEmpty()) {

            root.addView(
                subtitle("Поки що немає жодної операції.")
            )

        } else {

            val sorted = operations.sortedByDescending {
                parseDateForSort(it.date)
            }

            var totalIncome = 0.0
            var totalExpense = 0.0

            for (op in operations) {

                if (isIncome(op.type)) {
                    totalIncome += op.amount
                } else {
                    totalExpense += op.amount
                }
            }

            root.addView(
                subtitle(
                    "Доходи: ${formatMoney(totalIncome)}\n" +
                            "Витрати: ${formatMoney(totalExpense)}\n" +
                            "Результат: ${formatMoney(totalIncome - totalExpense)}"
                )
            )

            for (op in sorted) {

                val card = LinearLayout(this)
                card.orientation = LinearLayout.VERTICAL
                card.setPadding(
                    dp(10),
                    dp(10),
                    dp(10),
                    dp(10)
                )

                val info = TextView(this)
                info.textSize = 15f

                val sign = if (isIncome(op.type)) "+" else "-"

                info.text = buildString {
                    append("${op.date}\n")
                    append("${op.type}\n")

                    if (op.description.isNotEmpty()) {
                        append("${op.description}\n")
                    }

                    if (op.quantity != 0.0) {
                        append("Кількість: ${formatNumber(op.quantity)}\n")
                    }

                    append("$sign ${formatMoney(op.amount)}")
                }

                card.addView(info)

                val delete = Button(this)
                delete.text = "🗑 Видалити"
                delete.isAllCaps = false

                delete.setOnClickListener {

                    AlertDialog.Builder(this)
                        .setTitle("Видалити запис?")
                        .setMessage(
                            "${op.date}\n${op.type}\n${formatMoney(op.amount)}"
                        )
                        .setPositiveButton("Так") { _, _ ->
                            deleteOperation(op.id)
                            journalScreen()
                        }
                        .setNegativeButton("Ні", null)
                        .show()
                }

                card.addView(delete)

                root.addView(card)

                val line = View(this)
                line.minimumHeight = dp(1)
                root.addView(line)
            }
        }

        root.addView(
            button("➕ Додати операцію") {
                addOperation()
            }
        )

        root.addView(
            button("⬅ Назад") {
                showMainMenu()
            }
        )

        scroll.addView(root)
        setContent(scroll)
    }

    // ------------------------------------------------------------
    // ВИЗНАЧЕННЯ ДОХОДУ
    // ------------------------------------------------------------

    private fun isIncome(type: String): Boolean {

        return type == "Продаж яєць" ||
                type == "Продаж птиці" ||
                type == "Інший дохід"
    }

    private fun parseDateForSort(date: String): Long {

        return try {

            val sdf = SimpleDateFormat(
                "dd.MM.yyyy",
                Locale.getDefault()
            )

            sdf.parse(date)?.time ?: 0L

        } catch (_: Exception) {
            0L
        }
    }

    // ------------------------------------------------------------
    // МІСЯЧНА ТАБЛИЦЯ
    // ------------------------------------------------------------

    private fun monthlyTableScreen() {

        val scroll = scrollLayout()
        val root = rootLayout()

        root.addView(title("📅 Таблиця по місяцях"))

        val operations = getOperations()

        val months = operations
            .map { monthFromDate(it.date) }
            .distinct()
            .sortedWith(
                compareBy {
                    parseMonthForSort(it)
                }
            )
            .toMutableList()

        if (months.isEmpty()) {
            months.add(currentMonth())
        }

        val table = TableLayout(this)
        table.isStretchAllColumns = true

        val header = TableRow(this)

        val headers = arrayOf(
            "Місяць",
            "Доходи",
            "Витрати",
            "Результат",
            "Накоп."
        )

        for (h in headers) {
            val tv = TextView(this)
            tv.text = h
            tv.textSize = 13f
            tv.gravity = Gravity.CENTER
            tv.setPadding(dp(3), dp(8), dp(3), dp(8))
            header.addView(tv)
        }

        table.addView(header)

        var cumulative = 0.0

        for (month in months) {

            val monthOperations = operations.filter {
                monthFromDate(it.date) == month
            }

            var income = 0.0
            var expense = 0.0

            for (op in monthOperations) {

                if (isIncome(op.type)) {
                    income += op.amount
                } else {
                    expense += op.amount
                }
            }

            val profit = income - expense

            cumulative += profit

            val row = TableRow(this)

            val values = arrayOf(
                month,
                formatMoney(income),
                formatMoney(expense),
                formatMoney(profit),
                formatMoney(cumulative)
            )

            for (value in values) {

                val tv = TextView(this)
                tv.text = value
                tv.textSize = 12f
                tv.gravity = Gravity.CENTER
                tv.setPadding(dp(3), dp(8), dp(3), dp(8))

                row.addView(tv)
            }

            table.addView(row)
        }

        root.addView(table)

        root.addView(
            button("➕ Додати операцію") {
                addOperation()
            }
        )

        root.addView(
            button("⬅ Назад") {
                showMainMenu()
            }
        )

        scroll.addView(root)
        setContent(scroll)
    }

    private fun parseMonthForSort(month: String): Long {

        return try {

            val sdf = SimpleDateFormat(
                "MM.yyyy",
                Locale.getDefault()
            )

            sdf.parse(month)?.time ?: 0L

        } catch (_: Exception) {
            0L
        }
    }

    // ------------------------------------------------------------
    // ЗВІТ
    // ------------------------------------------------------------

    private fun reportScreen() {

        val scroll = scrollLayout()
        val root = rootLayout()

        root.addView(title("💰 Загальний звіт"))

        val operations = getOperations()

        var income = 0.0
        var expense = 0.0

        var eggIncome = 0.0
        var birdIncome = 0.0
        var feedExpense = 0.0
        var otherExpense = 0.0

        for (op in operations) {

            if (isIncome(op.type)) {

                income += op.amount

                if (op.type == "Продаж яєць") {
                    eggIncome += op.amount
                }

                if (op.type == "Продаж птиці") {
                    birdIncome += op.amount
                }

            } else {

                expense += op.amount

                if (op.type == "Купівля корму") {
                    feedExpense += op.amount
                } else {
                    otherExpense += op.amount
                }
            }
        }

        val profit = income - expense

        val investment =
            prefs.getFloat("investment", 0f).toDouble()

        val paybackText = if (profit > 0.0 && investment > 0.0) {

            val months = investment / profit

            "${String.format(Locale.getDefault(), "%.1f", months)} міс."

        } else {
            "Недостатньо даних"
        }

        root.addView(
            subtitle(
                "📈 ПО ЖУРНАЛУ\n\n" +
                        "Всі доходи: ${formatMoney(income)}\n" +
                        "Всі витрати: ${formatMoney(expense)}\n" +
                        "Чистий результат: ${formatMoney(profit)}\n\n" +
                        "🥚 Продаж яєць: ${formatMoney(eggIncome)}\n" +
                        "🐓 Продаж птиці: ${formatMoney(birdIncome)}\n" +
                        "🌾 Корм: ${formatMoney(feedExpense)}\n" +
                        "💸 Інші витрати: ${formatMoney(otherExpense)}"
            )
        )

        root.addView(
            button("💵 Вказати початкові вкладення") {

                val input = editText(
                    "Сума вкладень, грн",
                    true
                )

                input.setText(
                    prefs.getFloat(
                        "investment",
                        0f
                    ).takeIf { it > 0f }?.toString() ?: ""
                )

                AlertDialog.Builder(this)
                    .setTitle("Початкові вкладення")
                    .setView(input)
                    .setPositiveButton("Зберегти") { _, _ ->

                        val value =
                            input.text.toString()
                                .replace(",", ".")
                                .toFloatOrNull() ?: 0f

                        prefs.edit()
                            .putFloat(
                                "investment",
                                value
                            )
                            .apply()

                        reportScreen()
                    }
                    .setNegativeButton("Скасувати", null)
                    .show()
            }
        )

        root.addView(
            subtitle(
                "💰 Початкові вкладення: ${formatMoney(investment)}\n" +
                        "⏱ Орієнтовна окупність: $paybackText"
            )
        )

        root.addView(
            button("📅 Переглянути місячну таблицю") {
                monthlyTableScreen()
            }
        )

        root.addView(
            button("📈 Графік по днях") {
                dailyGraphScreen()
            }
        )

        root.addView(
            button("📊 Графік по місяцях") {
                monthlyGraphScreen()
            }
        )

        root.addView(
            button("⬅ Назад") {
                showMainMenu()
            }
        )

        scroll.addView(root)
        setContent(scroll)
    }

    // ------------------------------------------------------------
    // ДАНІ ДЛЯ ГРАФІКА ПО ДНЯХ
    // ------------------------------------------------------------

    private fun dailyValues(): List<Pair<String, Double>> {

        val operations = getOperations()

        val map = linkedMapOf<String, Double>()

        val sortedDates = operations
            .map { it.date }
            .distinct()
            .sortedWith(
                compareBy {
                    parseDateForSort(it)
                }
            )

        for (date in sortedDates) {

            val dayOperations =
                operations.filter { it.date == date }

            var value = 0.0

            for (op in dayOperations) {

                if (isIncome(op.type)) {
                    value += op.amount
                } else {
                    value -= op.amount
                }
            }

            map[date] = value
        }

        return map.toList()
    }

    // ------------------------------------------------------------
    // ГРАФІК ПО ДНЯХ
    // ------------------------------------------------------------

    private fun dailyGraphScreen() {

        val scroll = scrollLayout()
        val root = rootLayout()

        root.addView(title("📈 Результат по днях"))

        val data = dailyValues()

        if (data.isEmpty()) {

            root.addView(
                subtitle(
                    "Ще немає операцій для побудови графіка."
                )
            )

        } else {

            val graph = SimpleGraphView(
                this,
                data
            )

            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(350)
            )

            graph.layoutParams = params

            root.addView(graph)

            for (item in data) {

                val sign =
                    if (item.second >= 0.0) "+" else ""

                root.addView(
                    subtitle(
                        "${item.first}: $sign${formatMoney(item.second)}"
                    )
                )
            }
        }

        root.addView(
            button("⬅ Назад") {
                showMainMenu()
            }
        )

        scroll.addView(root)
        setContent(scroll)
    }

    // ------------------------------------------------------------
    // ДАНІ ДЛЯ МІСЯЧНОГО ГРАФІКА
    // ------------------------------------------------------------

    private fun monthlyValues(): List<Pair<String, Double>> {

        val operations = getOperations()

        val months = operations
            .map { monthFromDate(it.date) }
            .distinct()
            .sortedWith(
                compareBy {
                    parseMonthForSort(it)
                }
            )

        val result = mutableListOf<Pair<String, Double>>()

        for (month in months) {

            var income = 0.0
            var expense = 0.0

            for (op in operations) {

                if (monthFromDate(op.date) == month) {

                    if (isIncome(op.type)) {
                        income += op.amount
                    } else {
                        expense += op.amount
                    }
                }
            }

            result.add(
                Pair(
                    month,
                    income - expense
                )
            )
        }

        return result
    }

    // ------------------------------------------------------------
    // МІСЯЧНИЙ ГРАФІК
    // ------------------------------------------------------------

    private fun monthlyGraphScreen() {

        val scroll = scrollLayout()
        val root = rootLayout()

        root.addView(title("📊 Результат по місяцях"))

        val data = monthlyValues()

        if (data.isEmpty()) {

            root.addView(
                subtitle(
                    "Ще немає операцій для побудови графіка."
                )
            )

        } else {

            val graph = SimpleGraphView(
                this,
                data
            )

            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(350)
            )

            graph.layoutParams = params

            root.addView(graph)

            for (item in data) {

                val sign =
                    if (item.second >= 0.0) "+" else ""

                root.addView(
                    subtitle(
                        "${item.first}: $sign${formatMoney(item.second)}"
                    )
                )
            }
        }

        root.addView(
            button("⬅ Назад") {
                showMainMenu()
            }
        )

        scroll.addView(root)
        setContent(scroll)
    }

    // ------------------------------------------------------------
    // ПРОСТИЙ ГРАФІК БЕЗ СТОРОННІХ БІБЛІОТЕК
    // ------------------------------------------------------------

    class SimpleGraphView(
        context: android.content.Context,
        private val data: List<Pair<String, Double>>
    ) : View(context) {

        private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)

            if (data.isEmpty()) {
                return
            }

            val width = width.toFloat()
            val height = height.toFloat()

            val left = 70f
            val right = width - 25f
            val top = 30f
            val bottom = height - 60f

            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 3f

            // Вісь Y
            canvas.drawLine(
                left,
                top,
                left,
                bottom,
                paint
            )

            // Вісь X
            canvas.drawLine(
                left,
                bottom,
                right,
                bottom,
                paint
            )

            var minValue = data.minOf { it.second }
            var maxValue = data.maxOf { it.second }

            if (minValue > 0.0) {
                minValue = 0.0
            }

            if (maxValue < 0.0) {
                maxValue = 0.0
            }

            if (maxValue == minValue) {
                maxValue += 1.0
                minValue -= 1.0
            }

            val range = maxValue - minValue

            val path = Path()

            for (index in data.indices) {

                val x: Float

                if (data.size == 1) {

                    x = (left + right) / 2f

                } else {

                    x =
                        left +
                                (index.toFloat() /
                                        (data.size - 1).toFloat()) *
                                (right - left)
                }

                val normalized =
                    ((data[index].second - minValue) / range)
                        .toFloat()

                val y =
                    bottom -
                            normalized *
                            (bottom - top)

                if (index == 0) {
                    path.moveTo(x, y)
                } else {
                    path.lineTo(x, y)
                }

                paint.style = Paint.Style.FILL

                canvas.drawCircle(
                    x,
                    y,
                    7f,
                    paint
                )
            }

            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 4f

            canvas.drawPath(
                path,
                paint
            )

            // Підписи
            paint.style = Paint.Style.FILL
            paint.textSize = 24f

            val firstLabel = data.first().first
            val lastLabel = data.last().first

            canvas.drawText(
                firstLabel,
                left,
                height - 15f,
                paint
            )

            if (data.size > 1) {

                val lastWidth =
                    paint.measureText(lastLabel)

                canvas.drawText(
                    lastLabel,
                    right - lastWidth,
                    height - 15f,
                    paint
                )
            }

            // Значення зверху
            val maxText =
                String.format(
                    Locale.getDefault(),
                    "%.0f",
                    maxValue
                )

            val minText =
                String.format(
                    Locale.getDefault(),
                    "%.0f",
                    minValue
                )

            canvas.drawText(
                maxText,
                5f,
                top + 10f,
                paint
            )

            canvas.drawText(
                minText,
                5f,
                bottom,
                paint
            )
        }
    }

    // ------------------------------------------------------------
    // ОЧИЩЕННЯ
    // ------------------------------------------------------------

    private fun confirmClear() {

        AlertDialog.Builder(this)
            .setTitle("🗑 Очистити всі дані?")
            .setMessage(
                "Буде видалено поголів'я, корм, яйця,\n" +
                        "операції, витрати та всі збережені розрахунки."
            )
            .setPositiveButton("Так, очистити") { _, _ ->

                prefs.edit()
                    .clear()
                    .apply()

                Toast.makeText(
                    this,
                    "Всі дані очищено",
                    Toast.LENGTH_SHORT
                ).show()

                showMainMenu()
            }
            .setNegativeButton("Скасувати", null)
            .show()
    }

    // ------------------------------------------------------------
    // НАТИСКАННЯ НАЗАД
    // ------------------------------------------------------------

    override fun onBackPressed() {

        showMainMenu()
    }
}