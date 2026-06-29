package com.babalolaanjolaiyiola.calculator

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.*

class MainActivity : AppCompatActivity() {

    private lateinit var tvExpression: TextView
    private lateinit var tvResult: TextView

    private var expression = ""
    private var lastAnswer = "0"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvExpression = findViewById(R.id.tvExpression)
        tvResult = findViewById(R.id.tvResult)

        // Number buttons
        val numberButtons = mapOf(
            R.id.btn0 to "0", R.id.btn1 to "1", R.id.btn2 to "2",
            R.id.btn3 to "3", R.id.btn4 to "4", R.id.btn5 to "5",
            R.id.btn6 to "6", R.id.btn7 to "7", R.id.btn8 to "8",
            R.id.btn9 to "9"
        )
        for ((id, value) in numberButtons) {
            findViewById<Button>(id).setOnClickListener { appendToExpression(value) }
        }

        // Operator buttons
        findViewById<Button>(R.id.btnPlus).setOnClickListener { appendToExpression("+") }
        findViewById<Button>(R.id.btnMinus).setOnClickListener { appendToExpression("-") }
        findViewById<Button>(R.id.btnMultiply).setOnClickListener { appendToExpression("×") }
        findViewById<Button>(R.id.btnDivide).setOnClickListener { appendToExpression("÷") }
        findViewById<Button>(R.id.btnDot).setOnClickListener { appendToExpression(".") }
        findViewById<Button>(R.id.btnPercent).setOnClickListener { appendToExpression("%") }
        findViewById<Button>(R.id.btnOpen).setOnClickListener { appendToExpression("(") }
        findViewById<Button>(R.id.btnClose).setOnClickListener { appendToExpression(")") }
        findViewById<Button>(R.id.btnPi).setOnClickListener { appendToExpression("π") }
        findViewById<Button>(R.id.btnE).setOnClickListener { appendToExpression("e") }

        // Function buttons
        findViewById<Button>(R.id.btnSin).setOnClickListener { appendToExpression("sin(") }
        findViewById<Button>(R.id.btnCos).setOnClickListener { appendToExpression("cos(") }
        findViewById<Button>(R.id.btnTan).setOnClickListener { appendToExpression("tan(") }
        findViewById<Button>(R.id.btnLog).setOnClickListener { appendToExpression("log(") }
        findViewById<Button>(R.id.btnLn).setOnClickListener { appendToExpression("ln(") }
        findViewById<Button>(R.id.btnSqrt).setOnClickListener { appendToExpression("√(") }
        findViewById<Button>(R.id.btnSquare).setOnClickListener { appendToExpression("^2") }
        findViewById<Button>(R.id.btnPower).setOnClickListener { appendToExpression("^") }
        findViewById<Button>(R.id.btnFactorial).setOnClickListener { appendToExpression("!") }
        findViewById<Button>(R.id.btnInverse).setOnClickListener { appendToExpression("1/(") }

        // Negate
        findViewById<Button>(R.id.btnNegate).setOnClickListener {
            if (expression.isNotEmpty()) {
                expression = if (expression.startsWith("-")) {
                    expression.substring(1)
                } else {
                    "-$expression"
                }
                tvExpression.text = expression
            }
        }

        // Ans
        findViewById<Button>(R.id.btnAns).setOnClickListener {
            appendToExpression(lastAnswer)
        }

        // Clear
        findViewById<Button>(R.id.btnClear).setOnClickListener {
            expression = ""
            tvExpression.text = "0"
            tvResult.text = "0"
        }

        // Delete
        findViewById<Button>(R.id.btnDelete).setOnClickListener {
            if (expression.isNotEmpty()) {
                expression = expression.dropLast(1)
                tvExpression.text = if (expression.isEmpty()) "0" else expression
            }
        }

        // Equals
        findViewById<Button>(R.id.btnEqual).setOnClickListener {
            try {
                val result = evaluate(expression)
                val resultStr = if (result == result.toLong().toDouble()) {
                    result.toLong().toString()
                } else {
                    result.toString()
                }
                tvResult.text = resultStr
                lastAnswer = resultStr
                expression = resultStr
                tvExpression.text = resultStr
            } catch (e: Exception) {
                tvResult.text = "Error"
            }
        }
    }

    private fun appendToExpression(value: String) {
        if (expression == "0" && value != "." && !value.any { it.isLetter() || it == '(' }) {
            expression = value
        } else {
            expression += value
        }
        tvExpression.text = expression
    }

    private fun evaluate(expr: String): Double {
        val cleaned = expr
            .replace("×", "*")
            .replace("÷", "/")
            .replace("π", Math.PI.toString())
            .replace("e", Math.E.toString())

        return Parser(cleaned).parse()
    }

    // Simple recursive descent parser
    inner class Parser(private val input: String) {
        private var pos = 0

        fun parse(): Double {
            val result = parseExpression()
            return result
        }

        private fun parseExpression(): Double {
            var result = parseTerm()
            while (pos < input.length && (input[pos] == '+' || input[pos] == '-')) {
                val op = input[pos++]
                val term = parseTerm()
                result = if (op == '+') result + term else result - term
            }
            return result
        }

        private fun parseTerm(): Double {
            var result = parsePower()
            while (pos < input.length && (input[pos] == '*' || input[pos] == '/')) {
                val op = input[pos++]
                val factor = parsePower()
                result = if (op == '*') result * factor else result / factor
            }
            return result
        }

        private fun parsePower(): Double {
            var result = parseUnary()
            if (pos < input.length && input[pos] == '^') {
                pos++
                val exp = parseUnary()
                result = result.pow(exp)
            }
            return result
        }

        private fun parseUnary(): Double {
            if (pos < input.length && input[pos] == '-') {
                pos++
                return -parseFactorial()
            }
            return parseFactorial()
        }

        private fun parseFactorial(): Double {
            var result = parsePrimary()
            if (pos < input.length && input[pos] == '!') {
                pos++
                result = factorial(result.toInt()).toDouble()
            }
            return result
        }

        private fun parsePrimary(): Double {
            if (pos < input.length && input[pos] == '(') {
                pos++ // skip '('
                val result = parseExpression()
                if (pos < input.length && input[pos] == ')') pos++ // skip ')'
                return result
            }

            // Handle percentage
            if (pos + 1 < input.length && input[pos] == '%') {
                pos++
                return parseExpression() / 100.0
            }

            // Handle functions
            val functions = listOf("sin", "cos", "tan", "log", "ln", "√", "1/")
            for (func in functions) {
                if (input.startsWith(func, pos)) {
                    pos += func.length
                    if (pos < input.length && input[pos] == '(') pos++
                    val arg = parseExpression()
                    if (pos < input.length && input[pos] == ')') pos++
                    return when (func) {
                        "sin" -> sin(Math.toRadians(arg))
                        "cos" -> cos(Math.toRadians(arg))
                        "tan" -> tan(Math.toRadians(arg))
                        "log" -> log10(arg)
                        "ln" -> ln(arg)
                        "√" -> sqrt(arg)
                        "1/" -> 1.0 / arg
                        else -> arg
                    }
                }
            }

            // Parse number
            val start = pos
            if (pos < input.length && input[pos] == '-') pos++
            while (pos < input.length && (input[pos].isDigit() || input[pos] == '.')) pos++
            return input.substring(start, pos).toDouble()
        }

        private fun factorial(n: Int): Long {
            if (n <= 1) return 1
            return n * factorial(n - 1)
        }
    }
}