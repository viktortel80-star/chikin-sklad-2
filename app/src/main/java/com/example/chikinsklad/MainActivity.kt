package com.example.chikinsklad

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import java.util.Locale

class MainActivity : Activity() {

    private lateinit var result: TextView

    private val prefs by lazy {
        getSharedPreferences("chikin_sklad_data", MODE_PRIVATE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        createMainScreen()
    }

    private fun createMainScreen() {

        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(30, 30, 30, 30)

        val title = TextView(this)
        title.text = "🐔 ЧІКІН СКЛАД"
        title.textSize = 28f
        title.setPadding(0, 0, 0, 25)

        layout.addView(title)

        addButton(layout, "🐔 Поголів'я") {
            birds()
        }

        addButton(layout, "🌾 Корм") {
            feed()
        }

        addButton(layout, "🥚 Яйця та продаж") {
            eggs()
        }

        addButton(layout, "💰 Витрати та окупність") {
            profit()
        }

        addButton(layout, "🐓 Півники / забій") {
            roosters()
        }

        addButton(layout, "📊 Загальний звіт") {
            report()
        }

        addButton(layout, "🗑 Очистити всі дані") {
            clearAllData()
        }

        result = TextView(this)
        result.textSize = 18f
        result.setPadding(0, 25, 0, 0)

        layout.addView(result)

        setContentView(layout)

        result.text = "Дані зберігаються автоматично на телефоні."
    }

    private fun addButton(
        layout: LinearLayout,
        text: String,
        action: () -> Unit
    ) {
        val button = Button(this)
        button.text = text
        button.textSize = 17f

        button.setOnClickListener {
            action()
        }

        layout.addView(button)
    }

    private fun createInput(
        hint: String,
        value: String = ""
    ): EditText {

        val edit = EditText(this)

        edit.hint = hint
        edit.setText(value)
        edit.inputType =
            InputType.TYPE_CLASS_NUMBER or
                    InputType.TYPE_NUMBER_FLAG_DECIMAL

        return edit
    }

    // --------------------------------------------------
    // ПОГОЛІВ'Я
    // --------------------------------------------------

    private fun birds() {

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
        box.setPadding(30, 20, 30, 20)

        box.addView(chickens)
        box.addView(roosters)

        showDialog(
            "🐔 Поголів'я",
            box,
            "Зберегти"
        ) {

            val c =
                chickens.text.toString().toIntOrNull() ?: 0

            val r =
                roosters.text.toString().toIntOrNull() ?: 0

            save("chickens", c.toString())
            save("roosters", r.toString())

            result.text =
                "🐔 ПОГОЛІВ'Я\n\n" +
                        "Кури: $c\n" +
                        "Півні: $r\n" +
                        "Разом: ${c + r} голів\n\n" +
                        "✅ Дані збережено"
        }
    }

    // --------------------------------------------------
    // КОРМ
    // --------------------------------------------------

    private fun feed() {

        val birds = createInput(
            "Кількість курей",
            getStringValue("feed_birds")
        )

        val grams = createInput(
            "Грамів корму на голову / день",
            getStringValue("feed_grams")
        )

        val price = createInput(
            "Ціна корму за 1 кг",
            getStringValue("feed_price")
        )

        val box = LinearLayout(this)
        box.orientation = LinearLayout.VERTICAL
        box.setPadding(30, 20, 30, 20)

        box.addView(birds)
        box.addView(grams)
        box.addView(price)

        showDialog(
            "🌾 Корм",
            box,
            "Зберегти і розрахувати"
        ) {

            val b =
                birds.text.toString().toDoubleOrNull() ?: 0.0

            val g =
                grams.text.toString().toDoubleOrNull() ?: 0.0

            val p =
                price.text.toString().toDoubleOrNull() ?: 0.0

            save("feed_birds", b.toString())
            save("feed_grams", g.toString())
            save("feed_price", p.toString())

            val kgDay = b * g / 1000.0
            val kgMonth = kgDay * 30
            val costMonth = kgMonth * p

            save("feed_month_cost", costMonth.toString())

            result.text =
                "🌾 КОРМ\n\n" +
                        "На день: ${format(kgDay)} кг\n" +
                        "На місяць: ${format(kgMonth)} кг\n" +
                        "Вартість корму: ${format(costMonth)} грн/місяць\n\n" +
                        "✅ Дані збережено"
        }
    }

    // --------------------------------------------------
    // ЯЙЦЯ
    // --------------------------------------------------

    private fun eggs() {

        val eggs = createInput(
            "Кількість яєць за місяць",
            getStringValue("eggs")
        )

        val price = createInput(
            "Ціна за 10 яєць",
            getStringValue("egg_price")
        )

        val box = LinearLayout(this)
        box.orientation = LinearLayout.VERTICAL
        box.setPadding(30, 20, 30, 20)

        box.addView(eggs)
        box.addView(price)

        showDialog(
            "🥚 Яйця та продаж",
            box,
            "Зберегти і розрахувати"
        ) {

            val e =
                eggs.text.toString().toDoubleOrNull() ?: 0.0

            val p =
                price.text.toString().toDoubleOrNull() ?: 0.0

            save("eggs", e.toString())
            save("egg_price", p.toString())

            val revenue =
                e / 10.0 * p

            save("egg_revenue", revenue.toString())

            result.text =
                "🥚 ПРОДАЖ ЯЄЦЬ\n\n" +
                        "Яєць: ${e.toInt()} шт.\n" +
                        "Ціна за 10 шт.: ${format(p)} грн\n" +
                        "Виручка: ${format(revenue)} грн/місяць\n\n" +
                        "✅ Дані збережено"
        }
    }

    // --------------------------------------------------
    // ОКУПНІСТЬ
    // --------------------------------------------------

    private fun profit() {

        val otherExpenses = createInput(
            "Інші витрати за місяць",
            getStringValue("other_expenses")
        )

        val investment = createInput(
            "Початкові вкладення",
            getStringValue("investment")
        )

        val box = LinearLayout(this)
        box.orientation = LinearLayout.VERTICAL
        box.setPadding(30, 20, 30, 20)

        box.addView(otherExpenses)
        box.addView(investment)

        showDialog(
            "💰 Витрати та окупність",
            box,
            "Зберегти і розрахувати"
        ) {

            val other =
                otherExpenses.text.toString().toDoubleOrNull() ?: 0.0

            val invest =
                investment.text.toString().toDoubleOrNull() ?: 0.0

            save("other_expenses", other.toString())
            save("investment", invest.toString())

            val feedCost =
                getDoubleValue("feed_month_cost")

            val eggRevenue =
                getDoubleValue("egg_revenue")

            val totalExpenses =
                feedCost + other

            val monthlyProfit =
                eggRevenue - totalExpenses

            val payback =
                if (monthlyProfit > 0) {
                    invest / monthlyProfit
                } else {
                    0.0
                }

            result.text =
                "💰 ОКУПНІСТЬ\n\n" +
                        "Виручка від яєць: ${format(eggRevenue)} грн\n" +
                        "Корм: ${format(feedCost)} грн\n" +
                        "Інші витрати: ${format(other)} грн\n" +
                        "Всього витрат: ${format(totalExpenses)} грн\n\n" +
                        "Прибуток: ${format(monthlyProfit)} грн/місяць\n\n" +
                        if (monthlyProfit > 0) {
                            "Окупність: ${format(payback)} місяців"
                        } else {
                            "Окупність поки не розраховується"
                        }
        }
    }

    // --------------------------------------------------
    // ПІВНИКИ
    // --------------------------------------------------

    private fun roosters() {

        val count = createInput(
            "Кількість півників",
            getStringValue("young_roosters")
        )

        val age = createInput(
            "Вік півників, місяців",
            getStringValue("rooster_age")
        )

        val box = LinearLayout(this)
        box.orientation = LinearLayout.VERTICAL
        box.setPadding(30, 20, 30, 20)

        box.addView(count)
        box.addView(age)

        showDialog(
            "🐓 Півники / забій",
            box,
            "Зберегти"
        ) {

            val c =
                count.text.toString().toIntOrNull() ?: 0

            val a =
                age.text.toString().toDoubleOrNull() ?: 0.0

            save("young_roosters", c.toString())
            save("rooster_age", a.toString())

            result.text =
                "🐓 ПІВНИКИ\n\n" +
                        "Кількість: $c\n" +
                        "Вік: ${format(a)} місяців\n\n" +
                        "Дані збережено.\n\n" +
                        "Вік для забою залежить від породи, " +
                        "живої маси та призначення птиці."
        }
    }

    // --------------------------------------------------
    // ЗВІТ
    // --------------------------------------------------

    private fun report() {

        val chickens =
            getStringValue("chickens", "0")

        val roosters =
            getStringValue("roosters", "0")

        val feedMonth =
            getDoubleValue("feed_month_cost")

        val eggRevenue =
            getDoubleValue("egg_revenue")

        val other =
            getDoubleValue("other_expenses")

        val investment =
            getDoubleValue("investment")

        val totalExpenses =
            feedMonth + other

        val profit =
            eggRevenue - totalExpenses

        result.text =
            "📊 ЗАГАЛЬНИЙ ЗВІТ\n\n" +

                    "🐔 Кури: $chickens\n" +
                    "🐓 Півні: $roosters\n\n" +

                    "🌾 Корм/місяць: " +
                    "${format(feedMonth)} грн\n" +

                    "🥚 Виручка/місяць: " +
                    "${format(eggRevenue)} грн\n\n" +

                    "💸 Витрати/місяць: " +
                    "${format(totalExpenses)} грн\n" +

                    "💰 Прибуток/місяць: " +
                    "${format(profit)} грн\n\n" +

                    "🏗 Початкові вкладення: " +
                    "${format(investment)} грн\n\n" +

                    "✅ Дані збережені на телефоні."
    }

    // --------------------------------------------------
    // ОЧИЩЕННЯ
    // --------------------------------------------------

    private fun clearAllData() {

        AlertDialog.Builder(this)
            .setTitle("Очистити всі дані?")
            .setMessage(
                "Усі збережені дані «Чікін склад» " +
                        "будуть видалені з телефона."
            )
            .setNegativeButton("Скасувати", null)
            .setPositiveButton("Очистити") { _, _ ->

                prefs.edit()
                    .clear()
                    .apply()

                result.text =
                    "🗑 Усі дані очищено."
            }
            .show()
    }

    // --------------------------------------------------
    // ЗБЕРЕЖЕННЯ
    // --------------------------------------------------

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
        return prefs.getString(key, default) ?: default
    }

    private fun getDoubleValue(
        key: String
    ): Double {
        return prefs
            .getString(key, "0")
            ?.toDoubleOrNull()
            ?: 0.0
    }

    // --------------------------------------------------
    // ДІАЛОГ
    // --------------------------------------------------

    private fun showDialog(
        title: String,
        view: LinearLayout,
        positiveText: String,
        action: () -> Unit
    ) {

        AlertDialog.Builder(this)
            .setTitle(title)
            .setView(view)
            .setPositiveButton(positiveText) { _, _ ->
                action()
            }
            .setNegativeButton("Скасувати", null)
            .show()
    }

    // --------------------------------------------------
    // ФОРМАТ ЧИСЕЛ
    // --------------------------------------------------

    private fun format(
        number: Double
    ): String {

        return String.format(
            Locale.US,
            "%.2f",
            number
        )
    }
}
