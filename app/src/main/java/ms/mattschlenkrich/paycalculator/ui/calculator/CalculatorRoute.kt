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
        }
    }

    fun performMath() {
        val curNumber = if (displayValue == "0" || displayValue == "-0") {
            0.0
        } else {
            nf.getDoubleFromDollars(displayValue)
        }

        if (operatorList.isEmpty() || operatorList[currentCounter] == "") {
            if (formulaList.isEmpty()) {
                formulaList.add(nf.displayNumberFromDouble(curNumber))
                resultList.add(curNumber)
                operatorList.add("")
                prevNumberList.add(0.0)
            } else {
                formulaList[currentCounter] = nf.displayNumberFromDouble(curNumber)
                resultList[currentCounter] = curNumber
            }
        } else {
            val prev = prevNumberList[currentCounter]
            val op = operatorList[currentCounter]
            val result = CalculatorLogic.calculate(prev, curNumber, op)
            resultList[currentCounter] = result
            formulaList[currentCounter] =
                CalculatorLogic.formatFormula(prev, curNumber, op, result, nf)
        }
        transferResult = resultList[currentCounter]
    }

    fun addDigit(digit: String) {
        displayValue = CalculatorLogic.addDigit(displayValue, digit)
        performMath()
    }

    fun performOperatorAction(operation: String) {
        if (operatorList.isEmpty() || operatorList[currentCounter] == "") {
            if (operatorList.isEmpty()) {
                operatorList.add(operation)
                prevNumberList.add(0.0)
                formulaList.add("")
                resultList.add(0.0)
            } else {
                operatorList[currentCounter] = operation
            }
            prevNumberList[currentCounter] = if (displayValue == "0" || displayValue == "-0") {
                0.0
            } else {
                nf.getDoubleFromDollars(displayValue)
            }
            displayValue = "0"
        } else {
            operatorList[currentCounter] = operation
        }
        performMath()
    }

    fun performEqualAction() {
        currentCounter += 1
        displayValue = nf.displayNumberFromDouble(resultList[currentCounter - 1])
        prevNumberList.add(0.0)
        operatorList.add("")
        formulaList.add("")
        resultList.add(0.0)
        performMath()
    }

    fun clearAll() {
        if (currentCounter < prevNumberList.size) {
            prevNumberList[currentCounter] = 0.0
            operatorList[currentCounter] = ""
        }
        displayValue = "0"
        performMath()
    }

    fun clear() {
        displayValue = "0"
        performMath()
    }

    fun performBackspace() {
        displayValue = CalculatorLogic.backspace(displayValue)
        performMath()
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