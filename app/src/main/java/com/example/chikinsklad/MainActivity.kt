package com.example.chikinsklad

import android.app.Activity
import android.app.AlertDialog
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Typeface
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

class MainActivity : Activity() {

    private val prefs by lazy {
        getSharedPreferences("chikin_sklad_data", MODE_PRIVATE)
    }

    private lateinit var content: LinearLayout

    private val dateFormat =
        SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

    private val monthFormat =
        SimpleDateFormat("yyyy-MM", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        createMainScreen()
    }

    // =========================================================
    // ГОЛОВНИЙ ЕКРАН
    // =========================================================

    private fun createMainScreen() {

        val scroll = ScrollView(this)

        content = LinearLayout(this)
        content.orientation = LinearLayout.VERTICAL
        content.setPadding(
            dp(20),
            dp(70),
            dp(20),
            dp(30)
        )

        scroll.addView(content)

        setContentView(scroll)

        showHome()
    }

    private fun showHome() {

        content.removeAllViews()

        val title = TextView(this)
        title.text = "🐔 ЧІКІН СКЛАД"
        title.textSize = 30f
        title.gravity = Gravity.CENTER
        title.setTypeface(null, Typeface.BOLD)
        title.setPadding(0, 0, 0, dp(25))

        content.addView(title)

        val info = TextView(this)
        info.text =
            "Облік птиці • корму • яєць • доходів • витрат"
        info.textSize = 16f
        info.gravity = Gravity.CENTER
        info.setPadding(0, 0, 0, dp(20))

        content.addView(info)

        addMainButton("📅 Щоденні операції") {
            dailyOperations()
        }

        addMainButton("📒 Журнал операцій") {
            journal()
        }

        addMainButton("🐔 Поголів'я") {
            birds()
        }

        addMainButton("🌾 Корм") {
            feed()
        }

        addMainButton("🥚 Яйця та продаж") {
            eggs()
        }

        addMainButton("🐓 Півники / забій") {
            roosters()
        }

        addMainButton("📊 Місячна таблиця") {
            monthlyTable()
        }

        addMainButton("📈 Графіки") {
            graphs()
        }

        addMainButton("💰 Підсумок та окупність") {
            report()
        }

        addMainButton("🗑 Очистити всі дані") {
            clearAllData()
        }

        val status = TextView(this)
        status.text =
            "\n💾 Дані зберігаються автоматично на телефоні."
        status.textSize = 15f
        status.gravity = Gravity.CENTER

        content.addView(status)
    }

    private fun addMainButton(
        text: String,
        action: () -> Unit
    ) {

        val button = Button(this)

        button.text = text
        button.textSize = 18f

        val params =
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(58)
            )

        params.setMargins(
            0,
            dp(5),
            0,
            dp(5)
        )

        content.addView(button, params)

        button.setOnClickListener {
            action()
        }
    }

    private fun backButton() {

        val button = Button(this)

        button.text = "⬅ Назад"
        button.textSize = 17f

        content.addView(
            button,
            0,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(55)
            )
        )

        button.setOnClickListener {
            showHome()
        }
    }

    // =========================================================
    // ЩОДЕННА ОПЕРАЦІЯ
    // =========================================================

    private fun dailyOperations() {

        content.removeAllViews()
        backButton()

        val title = TextView(this)
        title.text = "📅 ЩОДЕННА ОПЕРАЦІЯ"
        title.textSize = 26f
        title.setTypeface(null, Typeface.BOLD)
        title.gravity = Gravity.CENTER
        title.setPadding(0, dp(20), 0, dp(20))

        content.addView(title)

        addMainButton("➕ Додати операцію") {
            addOperation()
        }

        addMainButton("📒 Переглянути журнал") {
            journal()
        }

        addMainButton("📊 Місячна таблиця") {
            monthlyTable()
        }

        addMainButton("📈 Графіки") {
            graphs()
        }
    }

    // =========================================================
    // ДОДАВАННЯ ОПЕРАЦІЇ
    // =========================================================

    private fun addOperation() {

        val box = LinearLayout(this)
        box.orientation = LinearLayout.VERTICAL
        box.setPadding(
            dp(25),
            dp(10),
            dp(25),
            dp(10)
        )

        val date = EditText(this)
        date.hint = "Дата"
        date.setText(dateFormat.format(Date()))
        date.isFocusable = false

        date.setOnClickListener {
            chooseDate(date)
        }

        val type = Spinner(this)

        val types = arrayOf(
            "Купівля корму",
            "Продаж яєць",
            "Продаж птиці",
            "Інші витрати",
            "Інший дохід",
            "Інкубація"
        )

        type.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            types
        )

        val description = EditText(this)
        description.hint = "Опис операції"

        val quantity = EditText(this)
        quantity.hint = "Кількість"
        quantity.inputType =
            InputType.TYPE_CLASS_NUMBER or
                    InputType.TYPE_NUMBER_FLAG_DECIMAL

        val amount = EditText(this)
        amount.hint = "Сума, грн"
        amount.inputType =
            InputType.TYPE_CLASS_NUMBER or
                    InputType.TYPE_NUMBER_FLAG_DECIMAL

        box.addView(date)
        box.addView(type)
        box.addView(description)
        box.addView(quantity)
        box.addView(amount)

        AlertDialog.Builder(this)
            .setTitle("➕ Нова операція")
            .setView(box)
            .setNegativeButton("Скасувати", null)
            .setPositiveButton("Зберегти") { _, _ ->

                val operation = JSONObject()

                operation.put(
                    "date",
                    date.text.toString()
                )

                operation.put(
                    "type",
                    type.selectedItem.toString()
                )

                operation.put(
                    "description",
                    description.text.toString()
                )

                operation.put(
                    "quantity",
                    quantity.text.toString()
                        .toDoubleOrNull() ?: 0.0
                )

                operation.put(
                    "amount",
                    amount.text.toString()
                        .toDoubleOrNull() ?: 0.0
                )

                addOperationToStorage(operation)

                Toast.makeText(
                    this,
                    "✅ Операцію збережено",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .show()
    }

    private fun chooseDate(edit: EditText) {

        val calendar = Calendar.getInstance()

        android.app.DatePickerDialog(
            this,
            { _, year, month, day ->

                val c = Calendar.getInstance()

                c.set(
                    year,
                    month,
                    day
                )

                edit.setText(
                    dateFormat.format(c.time)
                )

            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun addOperationToStorage(
        operation: JSONObject
    ) {

        val array = getOperations()

        array.put(operation)

        prefs.edit()
            .putString(
                "operations",
                array.toString()
            )
            .apply()
    }

    private fun getOperations(): JSONArray {

        val text =
            prefs.getString(
                "operations",
                "[]"
            ) ?: "[]"

        return try {
            JSONArray(text)
        } catch (e: Exception) {
            JSONArray()
        }
    }

    // =========================================================
    // ЖУРНАЛ
    // =========================================================

    private fun journal() {

        content.removeAllViews()
        backButton()

        val title = TextView(this)
        title.text = "📒 ЖУРНАЛ ОПЕРАЦІЙ"
        title.textSize = 26f
        title.setTypeface(null, Typeface.BOLD)
        title.gravity = Gravity.CENTER

        title.setPadding(
            0,
            dp(20),
            0,
            dp(20)
        )

        content.addView(title)

        addMainButton("➕ Додати операцію") {
            addOperation()
        }

        val operations = getOperations()

        if (operations.length == 0) {

            val empty = TextView(this)

            empty.text =
                "\nОперацій ще немає.\n\n" +
                        "Натисни «Додати операцію»."

            empty.textSize = 18f
            empty.gravity = Gravity.CENTER

            content.addView(empty)

            return
        }

        for (i in operations.length - 1 downTo 0) {

            val obj = operations.getJSONObject(i)

            val card = LinearLayout(this)

            card.orientation =
                LinearLayout.VERTICAL

            card.setPadding(
                dp(15),
                dp(15),
                dp(15),
                dp(15)
            )

            val date =
                obj.optString("date")

            val type =
                obj.optString("type")

            val description =
                obj.optString("description")

            val quantity =
                obj.optDouble(
                    "quantity",
                    0.0
                )

            val amount =
                obj.optDouble(
                    "amount",
                    0.0
                )

            val text = TextView(this)

            text.text =
                "$date\n" +
                        "$type\n" +
                        if (description.isNotBlank())
                            "$description\n"
                        else
                            "" +
                        "Кількість: ${format(quantity)}\n" +
                        "Сума: ${format(amount)} грн"

            text.textSize = 17f

            card.addView(text)

            val delete = Button(this)

            delete.text = "🗑 Видалити"

            card.addView(delete)

            delete.setOnClickListener {

                AlertDialog.Builder(this)
                    .setTitle("Видалити операцію?")
                    .setMessage(
                        "$date\n$type\n${format(amount)} грн"
                    )
                    .setNegativeButton(
                        "Скасувати",
                        null
                    )
                    .setPositiveButton(
                        "Видалити"
                    ) { _, _ ->

                        operations.remove(i)

                        prefs.edit()
                            .putString(
                                "operations",
                                operations.toString()
                            )
                            .apply()

                        journal()
                    }
                    .show()
            }

            content.addView(card)

            val line = TextView(this)
            line.text = "────────────────────"
            line.gravity = Gravity.CENTER

            content.addView(line)
        }
    }

    // =========================================================
    // ПОГОЛІВ'Я
    // =========================================================

    private fun birds() {

        content.removeAllViews()
        backButton()

        val chickens = createInput(
            "Кількість курей",
            getStringValue("chickens")
        )

        val roosters = createInput(
            "Кількість півнів",
            getStringValue("roosters")
        )

        val box = LinearLayout(this)
        box.orientation = LinearLayout.VERTICAL

        box.addView(chickens)
        box.addView(roosters)

        AlertDialog.Builder(this)
            .setTitle("🐔 Поголів'я")
            .setView(box)
            .setNegativeButton(
                "Скасувати",
                null
            )
            .setPositiveButton(
                "Зберегти"
            ) { _, _ ->

                val c =
                    chickens.text.toString()
                        .toIntOrNull() ?: 0

                val r =
                    roosters.text.toString()
                        .toIntOrNull() ?: 0

                save(
                    "chickens",
                    c.toString()
                )

                save(
                    "roosters",
                    r.toString()
                )

                birds()
            }
            .show()
    }

    // =========================================================
    // КОРМ
    // =========================================================

    private fun feed() {

        content.removeAllViews()
        backButton()

        val title = TextView(this)

        title.text = "🌾 КОРМ"
        title.textSize = 26f
        title.setTypeface(null, Typeface.BOLD)
        title.gravity = Gravity.CENTER

        content.addView(title)

        val birds =
            getDoubleValue("feed_birds")

        val grams =
            getDoubleValue("feed_grams")

        val price =
            getDoubleValue("feed_price")

        val kgDay =
            birds * grams / 1000.0

        val kgMonth =
            kgDay * 30

        val cost =
            kgMonth * price

        val info = TextView(this)

        info.text =
            "\nПоголів'я: ${format(birds)}\n" +
                    "Норма: ${format(grams)} г/голову/день\n\n" +
                    "Корм на день: ${format(kgDay)} кг\n" +
                    "Корм на місяць: ${format(kgMonth)} кг\n" +
                    "Вартість: ${format(cost)} грн/місяць"

        info.textSize = 18f

        content.addView(info)

        addMainButton("✏️ Змінити розрахунок") {

            val b = createInput(
                "Кількість курей",
                getStringValue("feed_birds")
            )

            val g = createInput(
                "Грамів на голову / день",
                getStringValue("feed_grams")
            )

            val p = createInput(
                "Ціна корму за кг",
                getStringValue("feed_price")
            )

            val box =
                LinearLayout(this)

            box.orientation =
                LinearLayout.VERTICAL

            box.addView(b)
            box.addView(g)
            box.addView(p)

            AlertDialog.Builder(this)
                .setTitle("🌾 Корм")
                .setView(box)
                .setNegativeButton(
                    "Скасувати",
                    null
                )
                .setPositiveButton(
                    "Зберегти"
                ) { _, _ ->

                    save(
                        "feed_birds",
                        b.text.toString()
                    )

                    save(
                        "feed_grams",
                        g.text.toString()
                    )

                    save(
                        "feed_price",
                        p.text.toString()
                    )

                    feed()
                }
                .show()
        }
    }

    // =========================================================
    // ЯЙЦЯ
    // =========================================================

    private fun eggs() {

        content.removeAllViews()
        backButton()

        val eggs =
            getDoubleValue("eggs")

        val price =
            getDoubleValue("egg_price")

        val revenue =
            eggs / 10.0 * price

        val title = TextView(this)

        title.text = "🥚 ЯЙЦЯ ТА ПРОДАЖ"
        title.textSize = 26f
        title.setTypeface(null, Typeface.BOLD)
        title.gravity = Gravity.CENTER

        content.addView(title)

        val info = TextView(this)

        info.text =
            "\nЯєць за місяць: ${format(eggs)} шт.\n" +
                    "Ціна за 10 шт.: ${format(price)} грн\n\n" +
                    "Виручка: ${format(revenue)} грн"

        info.textSize = 18f

        content.addView(info)

        addMainButton("✏️ Змінити") {

            val e =
                createInput(
                    "Кількість яєць",
                    getStringValue("eggs")
                )

            val p =
                createInput(
                    "Ціна за 10 яєць",
                    getStringValue("egg_price")
                )

            val box =
                LinearLayout(this)

            box.orientation =
                LinearLayout.VERTICAL

            box.addView(e)
            box.addView(p)

            AlertDialog.Builder(this)
                .setTitle("🥚 Яйця")
                .setView(box)
                .setNegativeButton(
                    "Скасувати",
                    null
                )
                .setPositiveButton(
                    "Зберегти"
                ) { _, _ ->

                    save(
                        "eggs",
                        e.text.toString()
                    )

                    save(
                        "egg_price",
                        p.text.toString()
                    )

                    eggs()
                }
                .show()
        }
    }

    // =========================================================
    // ПІВНИКИ
    // =========================================================

    private fun roosters() {

        content.removeAllViews()
        backButton()

        val count =
            getStringValue(
                "young_roosters",
                "0"
            )

        val age =
            getStringValue(
                "rooster_age",
                "0"
            )

        val title = TextView(this)

        title.text = "🐓 ПІВНИКИ / ЗАБІЙ"
        title.textSize = 26f
        title.setTypeface(null, Typeface.BOLD)
        title.gravity = Gravity.CENTER

        content.addView(title)

        val info = TextView(this)

        info.text =
            "\nКількість: $count\n" +
                    "Вік: $age місяців\n\n" +
                    "Вік забою залежить від породи,\n" +
                    "живої маси та призначення птиці."

        info.textSize = 18f

        content.addView(info)

        addMainButton("✏️ Змінити") {

            val c =
                createInput(
                    "Кількість півників",
                    count
                )

            val a =
                createInput(
                    "Вік, місяців",
                    age
                )

            val box =
                LinearLayout(this)

            box.orientation =
                LinearLayout.VERTICAL

            box.addView(c)
            box.addView(a)

            AlertDialog.Builder(this)
                .setTitle("🐓 Півники")
                .setView(box)
                .setNegativeButton(
                    "Скасувати",
                    null
                )
                .setPositiveButton(
                    "Зберегти"
                ) { _, _ ->

                    save(
                        "young_roosters",
                        c.text.toString()
                    )

                    save(
                        "rooster_age",
                        a.text.toString()
                    )

                    roosters()
                }
                .show()
        }
    }

    // =========================================================
    // МІСЯЧНА ТАБЛИЦЯ
    // =========================================================

    private fun monthlyTable() {

        content.removeAllViews()
        backButton()

        val title = TextView(this)

        title.text = "📊 МІСЯЧНА ТАБЛИЦЯ"
        title.textSize = 26f
        title.setTypeface(null, Typeface.BOLD)
        title.gravity = Gravity.CENTER

        title.setPadding(
            0,
            dp(20),
            0,
            dp(20)
        )

        content.addView(title)

        val horizontal =
            HorizontalScrollView(this)

        val table =
            TableLayout(this)

        table.setPadding(
            dp(5),
            dp(5),
            dp(5),
            dp(20)
        )

        addTableRow(
            table,
            arrayOf(
                "Місяць",
                "Дохід",
                "Витрати",
                "Прибуток",
                "Накопич."
            ),
            true
        )

        val months =
            getMonths()

        var accumulated = 0.0

        for (month in months) {

            val data =
                calculateMonth(month)

            val income =
                data["income"] ?: 0.0

            val expenses =
                data["expenses"] ?: 0.0

            val profit =
                income - expenses

            accumulated += profit

            addTableRow(
                table,
                arrayOf(
                    month,
                    format(income),
                    format(expenses),
                    format(profit),
                    format(accumulated)
                ),
                false
            )
        }

        horizontal.addView(table)

        content.addView(horizontal)

        val note = TextView(this)

        note.text =
            "\n💡 Таблиця формується автоматично " +
                    "з усіх щоденних операцій."

        note.textSize = 16f

        content.addView(note)
    }

    private fun addTableRow(
        table: TableLayout,
        values: Array<String>,
        header: Boolean
    ) {

        val row =
            TableRow(this)

        for (value in values) {

            val cell =
                TextView(this)

            cell.text = value
            cell.textSize =
                if (header) 16f else 15f

            cell.setPadding(
                dp(12),
                dp(12),
                dp(12),
                dp(12)
            )

            if (header) {
                cell.setTypeface(
                    null,
                    Typeface.BOLD
                )
            }

            row.addView(cell)
        }

        table.addView(row)
    }

    private fun getMonths(): List<String> {

        val set =
            mutableSetOf<String>()

        val operations =
            getOperations()

        for (i in 0 until operations.length) {

            val date =
                operations
                    .getJSONObject(i)
                    .optString("date")

            try {

                val d =
                    dateFormat.parse(date)

                if (d != null) {
                    set.add(
                        monthFormat.format(d)
                    )
                }

            } catch (_: Exception) {
            }
        }

        val current =
            monthFormat.format(Date())

        set.add(current)

        return set.sorted()
    }

    private fun calculateMonth(
        month: String
    ): MutableMap<String, Double> {

        var income = 0.0
        var expenses = 0.0

        val operations =
            getOperations()

        for (i in 0 until operations.length) {

            val obj =
                operations.getJSONObject(i)

            val date =
                obj.optString("date")

            val type =
                obj.optString("type")

            val amount =
                obj.optDouble(
                    "amount",
                    0.0
                )

            val operationMonth =
                try {
                    val d =
                        dateFormat.parse(date)

                    if (d != null)
                        monthFormat.format(d)
                    else
                        ""
                } catch (_: Exception) {
                    ""
                }

            if (operationMonth != month)
                continue

            when (type) {

                "Продаж яєць",
                "Продаж птиці",
                "Інший дохід" -> {
                    income += amount
                }

                "Купівля корму",
                "Інші витрати",
                "Інкубація" -> {
                    expenses += amount
                }
            }
        }

        return mutableMapOf(
            "income" to income,
            "expenses" to expenses
        )
    }

    // =========================================================
    // ГРАФІКИ
    // =========================================================

    private fun graphs() {

        content.removeAllViews()
        backButton()

        val title = TextView(this)

        title.text = "📈 ГРАФІКИ"
        title.textSize = 26f
        title.setTypeface(null, Typeface.BOLD)
        title.gravity = Gravity.CENTER

        content.addView(title)

        val dailyTitle =
            TextView(this)

        dailyTitle.text =
            "\n📅 Прибуток по днях"

        dailyTitle.textSize = 20f
        dailyTitle.setTypeface(
            null,
            Typeface.BOLD
        )

        content.addView(dailyTitle)

        val dailyData =
            getDailyGraphData()

        val dailyGraph =
            GraphView(
                this,
                dailyData
            )

        content.addView(
            dailyGraph,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(300)
            )
        )

        val monthlyTitle =
            TextView(this)

        monthlyTitle.text =
            "\n📊 Прибуток по місяцях"

        monthlyTitle.textSize = 20f
        monthlyTitle.setTypeface(
            null,
            Typeface.BOLD
        )

        content.addView(monthlyTitle)

        val monthlyData =
            mutableListOf<Pair<String, Double>>()

        for (month in getMonths()) {

            val data =
                calculateMonth(month)

            val profit =
                (data["income"] ?: 0.0) -
                        (data["expenses"] ?: 0.0)

            monthlyData.add(
                Pair(month, profit)
            )
        }

        val monthlyGraph =
            GraphView(
                this,
                monthlyData
            )

        content.addView(
            monthlyGraph,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(300)
            )
        )
    }

    private fun getDailyGraphData():
            List<Pair<String, Double>> {

        val map =
            mutableMapOf<String, Double>()

        val operations =
            getOperations()

        for (i in 0 until operations.length) {

            val obj =
                operations.getJSONObject(i)

            val date =
                obj.optString("date")

            val type =
                obj.optString("type")

            val amount =
                obj.optDouble(
                    "amount",
                    0.0
                )

            var value = 0.0

            when (type) {

                "Продаж яєць",
                "Продаж птиці",
                "Інший дохід" -> {
                    value = amount
                }

                "Купівля корму",
                "Інші витрати",
                "Інкубація" -> {
                    value = -amount
                }
            }

            map[date] =
                (map[date] ?: 0.0) + value
        }

        return map
            .toList()
            .sortedBy {
                try {
                    dateFormat.parse(it.first)
                } catch (_: Exception) {
                    Date(0)
                }
            }
    }

    // =========================================================
    // ПІДСУМОК
    // =========================================================

    private fun report() {

        content.removeAllViews()
        backButton()

        val operations =
            getOperations()

        var income = 0.0
        var expenses = 0.0

        for (i in 0 until operations.length) {

            val obj =
                operations.getJSONObject(i)

            val type =
                obj.optString("type")

            val amount =
                obj.optDouble(
                    "amount",
                    0.0
                )

            when (type) {

                "Продаж яєць",
                "Продаж птиці",
                "Інший дохід" -> {
                    income += amount
                }

                "Купівля корму",
                "Інші витрати",
                "Інкубація" -> {
                    expenses += amount
                }
            }
        }

        val profit =
            income - expenses

        val investment =
            getDoubleValue("investment")

        val title =
            TextView(this)

        title.text =
            "💰 ПІДСУМОК"

        title.textSize = 28f
        title.setTypeface(
            null,
            Typeface.BOLD
        )
        title.gravity =
            Gravity.CENTER

        content.addView(title)

        val info =
            TextView(this)

        info.text =
            "\n💰 Загальний дохід: " +
                    "${format(income)} грн\n\n" +

                    "💸 Загальні витрати: " +
                    "${format(expenses)} грн\n\n" +

                    "📈 Чистий результат: " +
                    "${format(profit)} грн\n\n" +

                    "🏗 Початкові вкладення: " +
                    "${format(investment)} грн\n\n" +

                    "📒 Кількість операцій: " +
                    operations.length

        info.textSize = 19f

        content.addView(info)

        addMainButton("✏️ Початкові вкладення") {

            val input =
                createInput(
                    "Сума вкладень",
                    investment.toString()
                )

            AlertDialog.Builder(this)
                .setTitle(
                    "🏗 Початкові вкладення"
                )
                .setView(input)
                .setNegativeButton(
                    "Скасувати",
                    null
                )
                .setPositiveButton(
                    "Зберегти"
                ) { _, _ ->

                    save(
                        "investment",
                        input.text.toString()
                    )

                    report()
                }
                .show()
        }
    }

    // =========================================================
    // ОЧИЩЕННЯ
    // =========================================================

    private fun clearAllData() {

        AlertDialog.Builder(this)
            .setTitle(
                "🗑 Очистити всі дані?"
            )
            .setMessage(
                "Будуть видалені всі дані " +
                        "та щоденні операції."
            )
            .setNegativeButton(
                "Скасувати",
                null
            )
            .setPositiveButton(
                "Очистити"
            ) { _, _ ->

                prefs.edit()
                    .clear()
                    .apply()

                showHome()

                Toast.makeText(
                    this,
                    "✅ Дані очищено",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .show()
    }

    // =========================================================
    // ДОПОМІЖНІ ФУНКЦІЇ
    // =========================================================

    private fun createInput(
        hint: String,
        value: String = ""
    ): EditText {

        val edit =
            EditText(this)

        edit.hint = hint
        edit.setText(value)

        edit.inputType =
            InputType.TYPE_CLASS_NUMBER or
                    InputType.TYPE_NUMBER_FLAG_DECIMAL

        edit.setPadding(
            dp(10),
            dp(10),
            dp(10),
            dp(10)
        )

        return edit
    }

    private fun save(
        key: String,
        value: String
    ) {

        prefs.edit()
            .putString(key, value)
            .apply()
    }

    private fun getStringValue(
        key: String,
        default: String = ""
    ): String {

        return prefs.getString(
            key,
            default
        ) ?: default
    }

    private fun getDoubleValue(
        key: String
    ): Double {

        return prefs
            .getString(
                key,
                "0"
            )
            ?.toDoubleOrNull()
            ?: 0.0
    }

    private fun format(
        number: Double
    ): String {

        return String.format(
            Locale.getDefault(),
            "%.2f",
            number
        )
    }

    private fun dp(
        value: Int
    ): Int {

        return (
                value *
                        resources.displayMetrics.density
                ).toInt()
    }

    // =========================================================
    // ГРАФІК
    // =========================================================

    private class GraphView(
        context: android.content.Context,
        private val data:
        List<Pair<String, Double>>
    ) : View(context) {

        private val paint =
            Paint(Paint.ANTI_ALIAS_FLAG)

        override fun onDraw(
            canvas: Canvas
        ) {

            super.onDraw(canvas)

            if (data.isEmpty()) {

                paint.textSize = 42f

                canvas.drawText(
                    "Немає даних",
                    40f,
                    120f,
                    paint
                )

                return
            }

            val width =
                width.toFloat()

            val height =
                height.toFloat()

            val left = 60f
            val right = width - 30f
            val top = 30f
            val bottom = height - 50f

            var max =
                data.maxOf {
                    kotlin.math.abs(it.second)
                }

            if (max == 0.0)
                max = 1.0

            paint.strokeWidth = 3f
            paint.style =
                Paint.Style.STROKE

            canvas.drawLine(
                left,
                top,
                left,
                bottom,
                paint
            )

            canvas.drawLine(
                left,
                bottom,
                right,
                bottom,
                paint
            )

            val zeroY =
                if (
                    data.any { it.second < 0 } &&
                    data.any { it.second > 0 }
                ) {
                    bottom -
                            (
                                    bottom - top
                                    ) / 2f
                } else {
                    bottom
                }

            paint.strokeWidth = 5f

            val path =
                Path()

            for (
                index in data.indices
            ) {

                val x =
                    if (data.size == 1) {
                        (left + right) / 2f
                    } else {
                        left +
                                index *
                                (right - left) /
                                (data.size - 1)
                    }

                val y =
                    zeroY -
                            (
                                    data[index].second /
                                            max
                                    ) *
                            (
                                    bottom - top
                                    ) / 2f

                if (index == 0)
                    path.moveTo(x, y)
                else
                    path.lineTo(x, y)

                paint.style =
                    Paint.Style.FILL

                canvas.drawCircle(
                    x,
                    y,
                    7f,
                    paint
                )

                if (
                    data.size <= 10 ||
                    index == 0 ||
                    index == data.lastIndex
                ) {

                    paint.textSize = 24f

                    canvas.drawText(
                        data[index].first,
                        x - 25f,
                        bottom + 35f,
                        paint
                    )
                }
            }

            paint.style =
                Paint.Style.STROKE

            canvas.drawPath(
                path,
                paint
            )

            paint.style =
                Paint.Style.FILL

            paint.textSize = 25f

            canvas.drawText(
                "грн",
                5f,
                top + 10f,
                paint
            )
        }
    }
}
