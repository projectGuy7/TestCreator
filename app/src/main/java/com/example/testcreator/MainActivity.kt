package com.example.testcreator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.testcreator.ui.theme.AssignmentTheme

private var identifier = 0
val border = 2.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApp(modifier = Modifier.fillMaxSize())
        }
    }
}


enum class MediatorScreens {
    TERMTYPE, TERMEDIT, DONE
}

class Mediator() {
    val testUnits: ArrayList<TestUnit> = ArrayList<TestUnit>()
    var testTermFactory: TestTermFactory? = null

    @Composable
    fun notify(any: Any, onConfirmPressed: () -> Unit = {}) {
        val mediator = this
        var dialog by remember { mutableStateOf(MediatorScreens.TERMTYPE) }
        when(any) {
            is TestUnit -> {
                val radioButtonsTermType = remember {
                    mutableStateListOf(
                        Toggleable(true, "One Choice question", identifier++),
                        Toggleable(false, "Type In question", identifier++),
                        Toggleable(false, "Card With Hints question", identifier++)
                    )
                }
                when(dialog) {
                    MediatorScreens.TERMTYPE -> {
                        CustomDialog {
                            CustomRadioButtons(
                                radioButtons = radioButtonsTermType,
                                customComposable = { index, info, radioButtons, modifier ->
                                    RadioButtonWithText(index = index, info = info, radioButtons = radioButtons, modifier = modifier)
                                })
                            Spacer(Modifier.padding(8.dp))
                            Button(
                                modifier = Modifier.align(Alignment.CenterHorizontally),
                                onClick = {
                                    dialog = MediatorScreens.TERMEDIT
                                }
                            ) {
                                Text("Next")
                            }
                        }
                    }
                    MediatorScreens.TERMEDIT -> {
                        if (radioButtonsTermType[0].isChecked) {
                            val radioButtonsTermEdit = remember {
                                mutableStateListOf(
                                    Toggleable(true, "", identifier++),
                                    Toggleable(false, "", identifier++),
                                    Toggleable(false, "", identifier++)
                                )
                            }
                            var question by remember {
                                mutableStateOf("")
                            }
                            testTermFactory = ChooseOneTermFactory(radioButtonsTermEdit, mediator)
                            (testTermFactory as ChooseOneTermFactory).createTestTerm(fieldsContents = { fieldsContents ->
                                question = fieldsContents[0]
                            }) {
                                onConfirmPressed()
                                radioButtonsTermEdit.forEach {
                                    if(it.isChecked) {
                                        it.isChecked = false
                                        any.testTerms.add(ChooseOneTerm(question, radioButtonsTermEdit, it.text))
                                    }
                                }
                                dialog = MediatorScreens.DONE
                            }
                        } else if (radioButtonsTermType[1].isChecked) {
                            var question by remember {
                                mutableStateOf("")
                            }
                            var answer by remember {
                                mutableStateOf("")
                            }
                            testTermFactory = TypeInTermFactory()
                            (testTermFactory as TypeInTermFactory).createTestTerm(fieldsContents = { fieldsContents ->
                                question = fieldsContents[0]
                                answer = fieldsContents[1]
                            }) {
                                onConfirmPressed()
                                any.testTerms.add(TypeInTerm(question, answer))
                                dialog = MediatorScreens.DONE
                            }
                        } else {
                            val cards = remember {
                                mutableStateListOf("", "", "", "")
                            }
                            testTermFactory = CardTermFactory()
                            (testTermFactory as CardTermFactory).createTestTerm(fieldsContents ={ fieldsContents ->
                                cards[0] = fieldsContents[0]
                                cards[1] = fieldsContents[1]
                                cards[2] = fieldsContents[2]
                                cards[3] = fieldsContents[3]
                            } ) {
                                onConfirmPressed()
                                any.testTerms.add(CardTerm(cards[0], CardTerm(cards[1], CardTerm(cards[2], CardTerm(cards[3], null, ""), ""), ""), ""))
                                dialog = MediatorScreens.DONE
                            }
                        }
                    }
                    MediatorScreens.DONE -> {

                    }
                }
            }
        }
    }
}

@Composable
fun CustomDialog(modifier: Modifier = Modifier, onDismissRequest: () -> Unit = {}, content: @Composable ColumnScope.() -> Unit,) {
    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            modifier = modifier,
            shape = MaterialTheme.shapes.medium,
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Top,
                content = content
            )
        }
    }
}

@Preview
@Composable
fun MediatorPreview() {
    Mediator().notify(TestUnit("", Mediator()))
}

class TestsLazyRow {
    private constructor() {}
    var testUnits: SnapshotStateList<TestUnit> = SnapshotStateList<TestUnit>()

    companion object {
        @Volatile
        private var tests : TestsLazyRow? = null
        fun getInstance(): TestsLazyRow {
            return tests ?: synchronized(this) {
                tests ?: TestsLazyRow().also{ tests = it }
            }
        }
    }

    @Composable
    fun Display(testUnitsSnapshot: SnapshotStateList<TestUnit>, modifier: Modifier) {
        var testChosen by rememberSaveable { mutableStateOf(false) }
        var activeTest : TestUnit? by remember { mutableStateOf(null) }
        if(!testChosen) {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                items(testUnits) { testUnit ->
                    testUnit.Display() {
                        activeTest = testUnit
                        testChosen = true
                    }
                }
                item {
                    Spacer(modifier.fillParentMaxHeight())
                }
            }
        } else {
            activeTest!!.startTest() {
                testChosen = false
            }
        }
    }
}

class TestUnit(var title: String, val mediator: Mediator, var modifier: Modifier = Modifier) {
    val testTerms : ArrayList<TestTerm> = ArrayList<TestTerm>()

    @Composable
    fun Display(modifier: Modifier = this.modifier, onClick: () -> Unit) {
        var notifying by remember {
            mutableStateOf(false)
        }
        CustomSurface(modifier = modifier, color = MaterialTheme.colorScheme.background) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.tertiary)
                        .size(80.dp),
                ) {
                    Text(
                        color = MaterialTheme.colorScheme.onTertiary,
                        text = title.substring(0, 1),
                        style = MaterialTheme.typography.headlineLarge,
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surface)
                        .clickable(onClick = onClick)
                        .weight(1f),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        modifier = Modifier
                            .padding(start = 20.dp, end = 20.dp),
                        style = MaterialTheme.typography.titleLarge,
                        text = title,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface),
                    onClick = {
                        notifying = true
                    }
                ) {
                    Text(
                        color = MaterialTheme.colorScheme.tertiary,
                        text = "+",
                        style = MaterialTheme.typography.headlineLarge)
                }
            }
        }
        if(notifying) {
            mediator.notify(this) {
                notifying = false
            }
        }
    }

    @Composable
    fun startTest(modifier: Modifier = Modifier, onCloseClicked: () -> Unit) {
        var showResult by remember {
            mutableStateOf(false)
        }
        LazyColumn (
            modifier = Modifier
                .wrapContentHeight(),
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            item {
                Surface(
                    shape = MaterialTheme.shapes.medium
                ) {
                    Box(
                        modifier = modifier
                            .height(60.dp)
                            .fillParentMaxWidth()
                            .background(MaterialTheme.colorScheme.tertiary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            modifier = Modifier
                                .padding(start = 20.dp, end = 20.dp),
                            text = title,
                            style = MaterialTheme.typography.headlineLarge,
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 1,
                            color = MaterialTheme.colorScheme.onTertiary
                        )
                    }
                }
            }
            items(testTerms) { testTerm ->
                testTerm.Display(modifier = Modifier, showResult)
            }
            item {
                Row {
                    Button(
                        modifier = Modifier,
                        onClick = onCloseClicked
                    ) {
                        Text("Close")
                    }
                    Spacer(
                        modifier
                            .fillMaxWidth()
                            .weight(1f))
                    Button(
                        modifier = Modifier,
                        onClick = {
                            showResult = true
                        }
                    ) {
                        Text("Confirm")
                    }
                }
            }
            item {
                Spacer(modifier.fillParentMaxHeight())
            }
        }
    }
}

@Composable
fun NewTestUnit(mediator: Mediator, onConfirm : (TestUnit) -> Unit) {
    var text by remember {
        mutableStateOf("")
    }
    Dialog(onDismissRequest = {  }) {
        Surface(
            shape = MaterialTheme.shapes.medium,
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                TextField(
                    value = text,
                    onValueChange = {
                        text = it
                    },
                    label = { Text("Enter Name of Test") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        onConfirm(TestUnit(text, mediator, modifier = Modifier.padding(start = 16.dp, end = 16.dp)))
                    }
                ) {
                    Text("Confirm")
                }
            }
        }
    }
}


@Preview
@Composable
fun TestUnitPreview() {
    TestUnit("Test", Mediator()).Display(modifier = Modifier
        .fillMaxWidth()
        .height(80.dp)) {}
}

abstract class TestTermFactory() {
    @Composable
    abstract fun createTestTerm(fieldsContents: (List<String>) -> Unit, onAddClicked: () -> Unit)
}

class ChooseOneTermFactory(val radioButtonsTermEdit: SnapshotStateList<Toggleable>, val mediator: Mediator) : TestTermFactory() {
    @Composable
    override fun createTestTerm(fieldsContents: (List<String>) -> Unit, onAddClicked: () -> Unit) {
        CustomDialog {
            var enabled by remember {
                mutableStateOf(false)
            }
            var text by remember {
                mutableStateOf("")
            }

            CustomTextField(
                Modifier
                    .padding(top = 8.dp, bottom = 8.dp)
                    .fillMaxWidth()
                    .height(50.dp),
                "Write Down the question"
            ) {
                fieldsContents(listOf(it))
            }
            CustomRadioButtons (
                radioButtons = radioButtonsTermEdit,
                customComposable = { index, info, radioButtons, modifier ->
                    RadioButtonWithFields(
                        index, info, radioButtons, Modifier
                    ) {
                        var noRepeating = true
                        for (i in 0..2) {
                            for (j in i+1..2) {
                                if (i == j) {
                                    continue
                                } else {
                                    if (radioButtonsTermEdit[i].text.equals(radioButtonsTermEdit[j].text)) {
                                        noRepeating = false
                                    }
                                }
                            }
                        }
                        enabled = noRepeating
                    }
                }
            )
            Button(
                enabled = enabled,
                modifier = Modifier.align(Alignment.CenterHorizontally),
                onClick = onAddClicked
            ) {
                Text("Add")
            }
        }
    }
}

@Composable
fun CustomTextField(modifier: Modifier = Modifier, placeHolderText: String = "", onValueChange: (String) -> Unit) {
    var text : String by remember {
        mutableStateOf("")
    }
    TextField(
        placeholder = {
            Text(placeHolderText)
        },
        modifier = modifier,
        value = text,
        onValueChange = { it ->
            text = it
            onValueChange(it)
        },
        textStyle = MaterialTheme.typography.titleMedium,
        minLines = 1,
        maxLines = 1
    )
}

@Preview
@Composable
fun ChooseOneTermFactoryPreview() {
    ChooseOneTermFactory(radioButtonsTermEdit = remember {
        mutableStateListOf(
            Toggleable(true, "", identifier++),
            Toggleable(false, "", identifier++),
            Toggleable(false, "", identifier++)
        )
    }, Mediator()).createTestTerm(fieldsContents = {str -> }) {

    }
}

class TypeInTermFactory() : TestTermFactory() {
    @Composable
    override fun createTestTerm(fieldsContents: (List<String>) -> Unit, onAddClicked: () -> Unit) {
        CustomDialog (Modifier.padding(start = 16.dp, end = 16.dp)) {
            var enabled by remember {
                 mutableStateOf(false)
            }
            var question by remember {
                mutableStateOf("")
            }
            var answer by remember {
                mutableStateOf("")
            }

            CustomTextField(
                Modifier
                    .padding(top = 8.dp, bottom = 8.dp)
                    .fillMaxWidth()
                    .height(50.dp),
                "Write Down the question"
            ) {
                enabled = !(answer == "" || question == "")
                question = it
                fieldsContents(listOf(it, answer))
            }
            CustomTextField(
                Modifier
                    .padding(top = 8.dp, bottom = 8.dp)
                    .fillMaxWidth()
                    .height(50.dp),
                "Write Down the answer"
            ) {
                enabled = !(answer == "" || question == "")
                answer = it
                fieldsContents(listOf(question, it))
            }

            Button(
                enabled = enabled,
                modifier = Modifier.align(Alignment.CenterHorizontally),
                onClick = onAddClicked
            ) {
                Text("Add")
            }
        }
    }
}

class CardTermFactory() : TestTermFactory() {
    @Composable
    override fun createTestTerm(fieldsContents: (List<String>) -> Unit, onAddClicked: () -> Unit) {
        var enabled by remember {
            mutableStateOf(false)
        }
        var chainToAnswer = remember {
            mutableStateListOf("", "", "", "")
        }
        CustomDialog (Modifier.padding(start = 16.dp, end = 16.dp)) {
            CustomTextField(
                Modifier
                    .padding(top = 8.dp, bottom = 8.dp)
                    .fillMaxWidth()
                    .height(50.dp),
                "Initial question"
            ) {
                chainToAnswer[0] = it
                enabled = emptyFieldsCheck(chainToAnswer)
                fieldsContents(listOf(*chainToAnswer.toTypedArray()))
            }
            CustomTextField(
                Modifier
                    .padding(top = 8.dp, bottom = 8.dp)
                    .fillMaxWidth()
                    .height(50.dp),
                ""
            ) {
                chainToAnswer[1] = it
                enabled = emptyFieldsCheck(chainToAnswer)
                fieldsContents(listOf(*chainToAnswer.toTypedArray()))
            }
            CustomTextField(
                Modifier
                    .padding(top = 8.dp, bottom = 8.dp)
                    .fillMaxWidth()
                    .height(50.dp),
                ""
            ) {
                chainToAnswer[2] = it
                enabled = emptyFieldsCheck(chainToAnswer)
                fieldsContents(listOf(*chainToAnswer.toTypedArray()))
            }
            CustomTextField(
                Modifier
                    .padding(top = 8.dp, bottom = 8.dp)
                    .fillMaxWidth()
                    .height(50.dp),
                ""
            ) {
                chainToAnswer[3] = it
                enabled = emptyFieldsCheck(chainToAnswer)
                fieldsContents(listOf(*chainToAnswer.toTypedArray()))
            }
            Button(
                enabled = enabled,
                modifier = Modifier.align(Alignment.CenterHorizontally),
                onClick = onAddClicked
            ) {
                Text("Add")
            }
        }
    }
}

fun emptyFieldsCheck(chainToAnswer: SnapshotStateList<String>) : Boolean {
    var counter = 0
    chainToAnswer.forEach {
        it -> if(it == "") { counter ++ }
    }
    return counter <= 2
}

abstract class TestTerm(open val answer: String) {

    @Composable
    abstract fun Display(modifier : Modifier, showResult: Boolean)

    @Composable
    fun ShowResult(modifier: Modifier = Modifier, options: SnapshotStateList<Toggleable>, showResult: Boolean) {
        if(showResult) {
            for(option in options) {
                if (option.text == answer) {
                    if(option.isChecked) {
                        Image(
                            painterResource(R.drawable.tick),
                            contentDescription = null,
                            modifier = modifier
                        )
                    } else {
                        Image(
                            painterResource(R.drawable.cross),
                            contentDescription = null,
                            modifier = modifier
                        )
                    }
                    break
                }
            }
        }
    }

    @Composable
    fun ShowResult(modifier: Modifier = Modifier, input: String, showResult: Boolean) {
        if(showResult) {
            if(input == answer) {
                Image(
                    painterResource(R.drawable.tick),
                    contentDescription = null,
                    modifier = modifier
                )
            } else {
                Image(
                    painterResource(R.drawable.cross),
                    contentDescription = null,
                    modifier = modifier
                )
            }
        }
    }
}

data class Toggleable(var isChecked: Boolean, var text: String, val identifier: Int)

class ChooseOneTerm(val question: String, val options: SnapshotStateList<Toggleable>, override val answer: String) : TestTerm(answer) {

    @Composable
    override fun Display(modifier: Modifier, showResult: Boolean) {
        val radioButtons = remember {
            options
        }
        CustomSurface(modifier.fillMaxWidth(), MaterialTheme.colorScheme.tertiary) {
            Column {
                questionHeader(question)
                CustomRadioButtons(
                    radioButtons = radioButtons,
                    customComposable = { index, info, radioButtons, modifier ->
                        RadioButtonWithText(index = index, info = info, radioButtons = radioButtons, modifier = modifier)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                )
                ShowResult(
                    modifier
                        .size(50.dp)
                        .align(Alignment.End) ,options, showResult)
            }
        }
    }
}

@Composable
fun CustomRadioButtons(radioButtons: SnapshotStateList<Toggleable>, customComposable : @Composable (Int, Toggleable, SnapshotStateList<Toggleable>, Modifier) -> Unit, modifier: Modifier = Modifier) {
    radioButtons.forEachIndexed() { index, info ->
        customComposable(index, info, radioButtons, modifier)
    }
}

@Composable
fun RadioButtonWithText(index: Int, info: Toggleable, radioButtons: SnapshotStateList<Toggleable>, modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clickable {
                radioButtons.replaceAll { it ->
                    it.copy(
                        isChecked = it.identifier.equals(info.identifier)
                    )
                }
            }
    ) {
        RadioButton(
            selected = info.isChecked,
            onClick = {
                radioButtons.replaceAll { it ->
                    it.copy(
                        isChecked = it.identifier.equals(info.identifier)
                    )
                }
            }
        )
        Text(
            modifier = Modifier.padding(start = 10.dp, end = 10.dp),
            text = info.text,
            style = MaterialTheme.typography.titleMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun RadioButtonWithFields(index: Int, info: Toggleable, radioButtons: SnapshotStateList<Toggleable>, modifier: Modifier = Modifier, onValueChange: () -> Unit = {}) {
    var text by rememberSaveable {
        mutableStateOf("")
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.padding(8.dp)
    ) {
        RadioButton(
            modifier = Modifier.clickable {
                radioButtons.replaceAll { it ->
                    it.copy(
                        isChecked = it.identifier.equals(info.identifier)
                    )
                }
            },
            selected = info.isChecked,
            onClick = {
                radioButtons.replaceAll { it ->
                    it.copy(
                        isChecked = it.identifier.equals(info.identifier)
                    )
                }
            }
        )
        TextField(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            value = text,
            onValueChange = {
                text = it
                info.text = text
                onValueChange()
            },
            textStyle = MaterialTheme.typography.titleMedium,
            minLines = 1,
            maxLines = 1
        )
    }
}

@Preview
@Composable
fun RadioButtonWithFieldsPreview() {
    RadioButtonWithFields(index = 0, info = Toggleable(false, "", identifier++), radioButtons = SnapshotStateList<Toggleable>())
}

class TypeInTerm(val question: String, override val answer: String) : TestTerm(answer) {
    var chosenText: String = ""
    @Composable
    override fun Display(modifier: Modifier, showResult: Boolean) {
        var text by remember {
            mutableStateOf("")
        }
        chosenText = text
        CustomSurface(modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.tertiary) {
            Column (
                Modifier.fillMaxWidth()
            ){
                questionHeader(question)
                TextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    value = text,
                    onValueChange = {
                        text = it
                    },
                    textStyle = MaterialTheme.typography.titleMedium,
                    minLines = 1,
                    maxLines = 1
                )
                ShowResult(modifier = Modifier
                    .size(50.dp)
                    .align(Alignment.End), input = text, showResult = showResult)
            }
        }
    }
}

@Preview
@Composable
fun CardTermPreview() {
    CardTerm("Some Crap", answer = "").Display(modifier = Modifier, showResult = false)
}

class CardTerm(val content: String, val card : CardTerm? = null, override val answer: String) : TestTerm(answer) {
    @Composable
    override fun Display(modifier: Modifier, showResult: Boolean) {
        var nextOpened by remember {
            mutableStateOf(false)
        }
        var kostyl by remember {
            mutableStateOf(true)
        }
        if(kostyl || (!nextOpened && !showResult)) {
            CustomSurface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.tertiary
            ) {
                Box(
                    modifier = Modifier
                        .height(150.dp)
                        .clickable {
                            if (card != null) {
                                nextOpened = true
                                kostyl = false
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        modifier = Modifier.padding(
                            start = 40.dp,
                            end = 40.dp,
                            top = 20.dp,
                            bottom = 20.dp
                        ),
                        text = content,
                        style = MaterialTheme.typography.titleLarge,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        } else {
            if(card!!.content == "") {
                kostyl = false
            } else {
                card!!.Display(Modifier, false)
            }
        }
    }
}

@Composable
fun questionHeader(question: String) {
    Box(
        modifier = Modifier.height(80.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            modifier = Modifier.padding(start = 10.dp, end = 10.dp),
            text = question,
            style = MaterialTheme.typography.titleLarge,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1
        )
    }
}

@Composable
fun CustomSurface(modifier: Modifier = Modifier, color: Color, content: @Composable () -> Unit) {
    Surface(
        modifier = modifier
            .border(
                BorderStroke(border, color = MaterialTheme.colorScheme.tertiary),
                shape = MaterialTheme.shapes.medium
            ),
        shape = MaterialTheme.shapes.medium,
        color = color,
        content = content
    )
}

enum class Screens {
    BOARDING, MAIN, EDITORY //TODO
}

data class Sprites(var darkModeIcon: Painter, var plus: Painter)

@Composable
fun MyApp(modifier: Modifier = Modifier) {
    var currScreen: Screens by rememberSaveable { mutableStateOf(Screens.BOARDING) }
    var darkMode: Boolean by rememberSaveable { mutableStateOf(false) }
    val mediator = Mediator()
    var sprites = Sprites(painterResource(R.drawable.lightmode), painterResource(R.drawable.lightmodeplus))
    if(darkMode) {
        sprites = sprites.copy(painterResource(R.drawable.darkmode), painterResource(R.drawable.darkmodeplus))
    }

    AssignmentTheme(darkTheme = darkMode) {
        when (currScreen) {
            Screens.BOARDING -> {
                StartApplication {
                    currScreen = Screens.MAIN
                }
            }
            Screens.MAIN -> {
                MainScreen(
                    mediator = mediator,
                    onDarkModeClicked = { bool ->
                        darkMode = bool
                    },
                    sprites = sprites
                )
            }
            Screens.EDITORY -> {

            }
        }
    }
}

@Composable
fun StartApplication(modifier : Modifier = Modifier, onContinueClicked : () -> Unit) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(stringResource(R.string.onBoardingText))
        Button(
            modifier = Modifier.padding(vertical = 24.dp),
            onClick = onContinueClicked
        ) {
            Text(stringResource(R.string.Start))
        }
    }
}

@Composable
fun MainScreen(modifier: Modifier = Modifier, mediator: Mediator, onDarkModeClicked: (Boolean) -> Unit, sprites: Sprites) {
    val testUnits = remember {
        TestsLazyRow.getInstance().testUnits
    }

    Column (
        Modifier
            .systemBarsPadding()
            .background(MaterialTheme.colorScheme.background)
    ){
        TopBar(
            mediator = mediator,
            testUnits = testUnits,
            onDarkModeClicked = onDarkModeClicked,
            sprites = sprites
        )
        TestsLazyRow.getInstance().Display(testUnits, modifier = Modifier)
    }
}


@Composable
fun TopBar(modifier: Modifier = Modifier, mediator: Mediator, testUnits: SnapshotStateList<TestUnit>, onDarkModeClicked: (Boolean) -> Unit, sprites: Sprites) {
    var newTest by remember {
        mutableStateOf(false)
    }
    Row(
        modifier
            .fillMaxWidth()
            .height(40.dp)
            .background(color = MaterialTheme.colorScheme.primary)
    ) {
        Button(
            modifier = Modifier.requiredSize(39.dp),
            contentPadding = PaddingValues(5.dp),
            onClick = {
                newTest = true
            }
        ) {
            Image(
                sprites.plus,
                contentDescription = null,
                modifier = Modifier
            )
        }
        Spacer(Modifier.weight(1f))
        DarkModeSwitch(modifier, onDarkModeClicked, sprites)
    }
    if(newTest) {
        NewTestUnit(mediator = mediator) { testUnit ->
            newTest = false
            testUnits.add(testUnit)
        }
    }
}


@Preview
@Composable
fun NewTestUnitPreview() {
    NewTestUnit(
        mediator = Mediator()
    ) {

    }
}

@Composable
fun DarkModeSwitch(modifier: Modifier = Modifier, onDarkModeClicked: (Boolean) -> Unit, sprites: Sprites) {
    var toggleable by rememberSaveable { mutableStateOf(false) }
    Row(
        modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = sprites.darkModeIcon,
            contentDescription = stringResource(id = R.string.darkModeSwitch)
        )
        Switch(
            modifier = modifier.scale(1f),
            checked = toggleable,

            onCheckedChange = { bool ->
                toggleable = bool
                onDarkModeClicked(bool)
            }
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 806)
@Composable
fun StartApplicationPreview() {
    StartApplication {

    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 806)
@Composable
fun MyAppPreview() {
    MyApp()
}