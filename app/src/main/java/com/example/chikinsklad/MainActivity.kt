package com.example.chikinsklad

import android.app.Activity
import android.os.Bundle
import android.widget.*
import kotlin.math.roundToInt

class MainActivity : Activity() {

    private lateinit var result: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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

        result = TextView(this)
        result.textSize = 18f
        result.setPadding(0, 25, 0, 0)
        layout.addView(result)

        setContentView(layout)
    }

    private fun addButton(
        layout: LinearLayout,
        text: String,
        action: () -> Unit
    ) {
        val button = Button(this)
        button.text = text
        button.textSize = 17f
        button.setOnClickListener { action() }

        layout.addView(
            button,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )
    }

    private fun input(
        title: String,
        hint: String
    ): EditText {
        val e = EditText(this)
        e.hint = hint
        e.inputType = 2
        return e
    }

    private fun birds() {
        val chickens = input("Кури", "Кількість курей")
        val roosters = input("Півні", "Кількість півнів")

        val box = LinearLayout(this)
        box.orientation = LinearLayout.VERTICAL
        box.setPadding(30, 20, 30, 20)

        box.addView(chickens)
        box.addView(roosters)

        AlertDialogBuilder(
            "Поголів'я",
            box,
            "Розрахувати"
        ) {
            val c = chickens.text.toString().toIntOrNull() ?: 0
            val r = roosters.text.toString().toIntOrNull() ?: 0

            result.text =
                "Поголів'я:\n\n" +
                "Кури: $c\n" +
                "Півні: $r\n" +
                "Разом: ${c + r} голів"
        }
    }

    private fun feed() {
        val birds = input("Кількість", "Кількість курей")
        val grams = input("Корм", "грамів на голову на день")
        val price = input("Ціна", "грн за 1 кг")

        val box = LinearLayout(this)
        box.orientation = LinearLayout.VERTICAL
        box.setPadding(30, 20, 30, 20)

        box.addView(birds)
        box.addView(grams)
        box.addView(price)

        AlertDialogBuilder(
            "Розрахунок корму",
            box,
            "Розрахувати"
        ) {
            val b = birds.text.toString().toDoubleOrNull() ?: 0.0
            val g = grams.text.toString().toDoubleOrNull() ?: 0.0
            val p = price.text.toString().toDoubleOrNull() ?: 0.0

            val kgDay = b * g / 1000.0
            val kgMonth = kgDay * 30
            val cost = kgMonth * p

            result.text =
                "Корм:\n\n" +
                "На день: %.2f кг\n".format(kgDay) +
                "На місяць: %.2f кг\n".format(kgMonth) +
                "Вартість на місяць: %.2f грн".format(cost)
        }
    }

    private fun eggs() {
        val eggs = input("Яйця", "Кількість яєць за місяць")
        val price = input("Ціна", "Ціна за 10 яєць")

        val box = LinearLayout(this)
        box.orientation = LinearLayout.VERTICAL
        box.setPadding(30, 20, 30, 20)

        box.addView(eggs)
        box.addView(price)

        AlertDialogBuilder(
            "Продаж яєць",
            box,
            "Розрахувати"
        ) {
            val e = eggs.text.toString().toDoubleOrNull() ?: 0.0
            val p = price.text.toString().toDoubleOrNull() ?: 0.0

            val revenue = e / 10.0 * p

            result.text =
                "Продаж яєць:\n\n" +
                "Яєць: %.0f шт.\n".format(e) +
                "Виручка: %.2f грн".format(revenue)
        }
    }

    private fun profit() {
        val revenue = input("Виручка", "Виручка за місяць, грн")
        val expenses = input("Витрати", "Витрати за місяць, грн")
        val investment = input("Вкладення", "Початкові вкладення, грн")

        val box = LinearLayout(this)
        box.orientation = LinearLayout.VERTICAL
        box.setPadding(30, 20, 30, 20)

        box.addView(revenue)
        box.addView(expenses)
        box.addView(investment)

        AlertDialogBuilder(
            "Окупність",
            box,
            "Розрахувати"
        ) {
            val r = revenue.text.toString().toDoubleOrNull() ?: 0.0
            val e = expenses.text.toString().toDoubleOrNull() ?: 0.0
            val i = investment.text.toString().toDoubleOrNull() ?: 0.0

            val profit = r - e

            val payback =
                if (profit > 0) i / profit else 0.0

            result.text =
                "Окупність:\n\n" +
                "Виручка: %.2f грн\n".format(r) +
                "Витрати: %.2f грн\n".format(e) +
                "Прибуток: %.2f грн/місяць\n".format(profit) +
                "Окупність: %.1f місяців".format(payback)
        }
    }

    private fun roosters() {
        result.text =
            "🐓 ПІВНИКИ\n\n" +
            "Для м'ясного напряму часто орієнтуються " +
            "на вік та живу масу птиці.\n\n" +
            "У програмі можна вести:\n" +
            "• кількість півників\n" +
            "• дату народження\n" +
            "• вік\n" +
            "• планову дату забою"
    }

    private fun report() {
        result.text =
            "📊 ЗАГАЛЬНИЙ ЗВІТ\n\n" +
            "Поголів'я — розрахунок курей та півнів\n" +
            "Корм — витрата та місячна вартість\n" +
            "Яйця — місячна виручка\n" +
            "Окупність — прибуток та строк окупності\n" +
            "Півники — контроль віку та забою"
    }

    private fun AlertDialogBuilder(
        title: String,
        view: LinearLayout,
        positive: String,
        action: () -> Unit
    ) {
        val dialog = android.app.AlertDialog.Builder(this)
            .setTitle(title)
            .setView(view)
            .setPositiveButton(positive) { _, _ ->
                action()
            }
            .setNegativeButton("Скасувати", null)
            .create()

        dialog.show()
    }
}
