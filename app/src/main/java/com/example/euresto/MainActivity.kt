package com.example.euresto

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.euresto.ui.theme.EURestoAndroidTheme

private const val EXCHANGE_RATE = 1.95583

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EURestoAndroidTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    RestoCalculator()
                }
            }
        }
    }
}

@Composable
fun RestoCalculator(modifier: Modifier = Modifier) {
    var euroAmountInput by remember { mutableStateOf("") }
    var paidBgnInput by remember { mutableStateOf("") }
    var paidEuroInput by remember { mutableStateOf("") }

    val euroAmount = euroAmountInput.toDoubleOrNull() ?: 0.0
    val paidBgn = paidBgnInput.toDoubleOrNull() ?: 0.0
    val paidEuro = paidEuroInput.toDoubleOrNull() ?: 0.0

    val totalBgn = euroAmount * EXCHANGE_RATE
    val totalPaidEuro = paidEuro + (paidBgn / EXCHANGE_RATE)
    val changeEuro = totalPaidEuro - euroAmount
    val changeBgn = changeEuro * EXCHANGE_RATE
    val context = LocalContext.current
    val logoResId = remember { context.resources.getIdentifier("delfi_logo", "drawable", context.packageName) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "EUR ➜ BGN калкулатор за ресто",
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            CurrencyTextField(
                label = "Сметка (EUR)",
                value = euroAmountInput,
                onValueChange = { euroAmountInput = sanitizeInput(it) },
                modifier = Modifier.fillMaxWidth()
            )

            LabeledAmount(
                label = "Сума в лева",
                amount = totalBgn,
                currency = "лв"
            )

            CurrencyTextField(
                label = "Платено (EUR)",
                value = paidEuroInput,
                onValueChange = { paidEuroInput = sanitizeInput(it) },
                modifier = Modifier.fillMaxWidth()
            )

            CurrencyTextField(
                label = "Платено (лв)",
                value = paidBgnInput,
                onValueChange = { paidBgnInput = sanitizeInput(it) },
                modifier = Modifier.fillMaxWidth()
            )

            LabeledAmount(label = "Ресто (EUR)", amount = changeEuro, currency = "€")
            LabeledAmount(label = "Ресто (лв)", amount = changeBgn, currency = "лв")
        }

        LogoSection(
            logoResId = logoResId,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(top = 16.dp)
        )
    }
}

@Composable
private fun CurrencyTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(text = label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = modifier
    )
}

@Composable
private fun LabeledAmount(label: String, amount: Double, currency: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(text = label, style = MaterialTheme.typography.labelLarge)
        Text(
            text = String.format("%.2f %s", amount, currency),
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
private fun LogoSection(logoResId: Int, modifier: Modifier = Modifier) {
    if (logoResId != 0) {
        Image(
            painter = painterResource(id = logoResId),
            contentDescription = "Delfi лого",
            modifier = modifier
                .fillMaxWidth()
                .sizeIn(maxHeight = 140.dp),
            contentScale = ContentScale.FillWidth
        )
    } else {
        Text(
            text = "Добавете delfi_logo.jpg в app/src/main/res/drawable/ за да се покаже логото.",
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            modifier = modifier.fillMaxWidth()
        )
    }
}

private fun sanitizeInput(raw: String): String {
    val filtered = raw.replace(',', '.')
    return filtered.filter { it.isDigit() || it == '.' }
}

@Preview(showBackground = true)
@Composable
fun RestoCalculatorPreview() {
    EURestoAndroidTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            RestoCalculator(Modifier.padding(PaddingValues(16.dp)))
        }
    }
}
