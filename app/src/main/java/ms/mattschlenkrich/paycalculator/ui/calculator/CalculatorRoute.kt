package ms.mattschlenkrich.paycalculator.ui.calculator

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavController
import ms.mattschlenkrich.paycalculator.common.CalculatorLogic
import ms.mattschlenkrich.paycalculator.common.NumberFunctions
import ms.mattschlenkrich.paycalculator.data.viewmodel.MainViewModel

@Composable
fun CalculatorRoute(
    mainViewModel: MainViewModel,
    navController: NavController
) {
    val nf = remember { NumberFunctions() }

    var displayValue by remember { mutableStateOf("0") }
    var isNewNumber by remember { mutableStateOf(true) }
    val formulaList = remember { mutableStateListOf("") }
    val operatorList = remember { mutableStateListOf("") }
    val prevNumberList = remember { mutableStateListOf(0.0) }
    val resultList = remember { mutableStateListOf(0.0) }
    var currentCounter by remember { mutableIntStateOf(0) }
    var transferResult by remember { mutableDoubleStateOf(0.0) }

    LaunchedEffect(Unit) {
        val transferNum = mainViewModel.getTransferNum()
        if (transferNum != 0.0 && !transferNum.isNaN()) {
            displayValue = nf.displayNumberFromDouble(transferNum)
            isNewNumber = true
        }
    }

    fun performMath() {
        val curNumber = if (displayValue == "0" || displayValue == "-0") {
            0.0
        } else {
            nf.getDoubleFromDollars(displayValue)
        }

        if (operatorList.isEmpty() || currentCounter >= operatorList.size || operatorList[currentCounter] == "") {
            val display = nf.displayNumberFromDouble(curNumber)
            if (currentCounter < formulaList.size) {
                formulaList[currentCounter] = display
                resultList[currentCounter] = curNumber
            } else {
                formulaList.add(display)
                resultList.add(curNumber)
                if (operatorList.size <= currentCounter) operatorList.add("")
                if (prevNumberList.size <= currentCounter) prevNumberList.add(0.0)
            }
        } else {
            val prev = prevNumberList[currentCounter]
            val op = operatorList[currentCounter]
            if (isNewNumber) {
                formulaList[currentCounter] = "${nf.displayNumberFromDouble(prev)} $op"
                resultList[currentCounter] = prev
            } else {
                val result = CalculatorLogic.calculate(prev, curNumber, op)
                resultList[currentCounter] = result
                formulaList[currentCounter] =
                    CalculatorLogic.formatFormula(prev, curNumber, op, result, nf)
            }
        }
        if (currentCounter < resultList.size) {
            transferResult = resultList[currentCounter]
        }
    }

    fun addDigit(digit: String) {
        if (isNewNumber) {
            displayValue = if (digit == ".") "0." else if (digit == "-") "-0" else digit
            isNewNumber = false
        } else {
            displayValue = CalculatorLogic.addDigit(displayValue, digit)
        }
        performMath()
    }

    fun performOperatorAction(operation: String) {
        if (isNewNumber) {
            if (operatorList.isNotEmpty() && currentCounter < operatorList.size) {
                operatorList[currentCounter] = operation
            }
        } else {
            if (operatorList.isNotEmpty() && currentCounter < operatorList.size && operatorList[currentCounter] != "") {
                val intermediateResult = resultList[currentCounter]
                currentCounter++
                prevNumberList.add(intermediateResult)
                operatorList.add(operation)
                formulaList.add("")
                resultList.add(intermediateResult)
                displayValue = nf.displayNumberFromDouble(intermediateResult)
            } else {
                val curVal = nf.getDoubleFromDollars(displayValue)
                if (currentCounter < prevNumberList.size) {
                    prevNumberList[currentCounter] = curVal
                } else {
                    prevNumberList.add(curVal)
                }

                if (currentCounter < operatorList.size) {
                    operatorList[currentCounter] = operation
                } else {
                    operatorList.add(operation)
                }
            }
            isNewNumber = true
        }
        performMath()
    }

    fun performEqualAction() {
        if (operatorList.isEmpty() || currentCounter >= operatorList.size || operatorList[currentCounter] == "") return

        val finalResult = resultList[currentCounter]
        currentCounter++

        displayValue = nf.displayNumberFromDouble(finalResult)
        isNewNumber = true

        prevNumberList.add(finalResult)
        operatorList.add("")
        formulaList.add("")
        resultList.add(finalResult)

        performMath()
    }

    fun clearAll() {
        formulaList.clear()
        operatorList.clear()
        prevNumberList.clear()
        resultList.clear()

        formulaList.add("")
        operatorList.add("")
        prevNumberList.add(0.0)
        resultList.add(0.0)

        currentCounter = 0
        displayValue = "0"
        isNewNumber = true
        performMath()
    }

    fun clear() {
        displayValue = "0"
        isNewNumber = true
        performMath()
    }

    fun performBackspace() {
        if (!isNewNumber) {
            displayValue = CalculatorLogic.backspace(displayValue)
            performMath()
        }
    }

    CalculatorScreen(
        displayValue = displayValue,
        formulaList = formulaList,
        transferResult = transferResult,
        onDigitClick = { addDigit(it) },
        onOperatorClick = { performOperatorAction(it) },
        onEqualClick = { performEqualAction() },
        onClearClick = { clear() },
        onClearAllClick = { clearAll() },
        onBackspaceClick = { performBackspace() },
        onTransferClick = {
            mainViewModel.setTransferNum(transferResult)
            navController.popBackStack()
        }
    )
}