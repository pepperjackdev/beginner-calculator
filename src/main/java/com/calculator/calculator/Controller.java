package com.calculator.calculator;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.List;
import java.util.Objects;
import java.util.function.BinaryOperator;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

public class Controller {

    private static final String ENTRY_TEXT_REGEX = "(?:[1-9][0-9]*|0)(?:[.][0-9]*)?";
    private Pattern pattern;
    private CalculatorEntryManager<BigDecimal> calcEntryMan;
    private boolean ghost = false;
    private final MathContext mc = new MathContext(32);
    private final String DARK_THEME_CSS = Objects.requireNonNull(getClass().getResource("dark_theme.css")).toExternalForm();
    private final String LIGHT_THEME_CSS = Objects.requireNonNull(getClass().getResource("light_theme.css")).toExternalForm();
    private String anglesUnit;

    @FXML private Pane root;

    @FXML private Button zero;
    @FXML private Button one;
    @FXML private Button two;
    @FXML private Button three;
    @FXML private Button four;
    @FXML private Button five;
    @FXML private Button six;
    @FXML private Button seven;
    @FXML private Button eight;
    @FXML private Button nine;

    @FXML private Button dot;

    @FXML private Button radOrDeg;

    @FXML private TextField entry;

    @FXML private void initialize() {

        List<Button> placers = List.of(
                dot, zero,
                one, two, three,
                four, five, six,
                seven, eight, nine
        );

        placers.forEach(b -> b.setOnAction(e -> appendTextToEntry(b.getText())));

        pattern = Pattern.compile(ENTRY_TEXT_REGEX);

        calcEntryMan = new CalculatorEntryManager<>();

        entry.setEditable(false);

        anglesUnit = radOrDeg.getText();

    }

    private String getEntryText() {
        return entry.getText();
    }

    private void setEntryText(String text) {

        entry.setPromptText("");

        if (ghost) {
            entry.clear();
            ghost = false;
        }

        entry.setText(text);
    }

    private void setEntryText(BigDecimal bg) {
        setEntryText(bg.toString());
    }

    private void setGhostEntryText(BigDecimal bg) {
        setEntryText(bg);
        ghost = true;
    }

    private BigDecimal getEntryAsBigDecimal() {
        return new BigDecimal(getEntryText());
    }

    private void appendTextToEntry(String character) {

        if (ghost) {
            entry.clear();
            ghost = false;
        }

        String string = getEntryText() + character;

        if (pattern.matcher(string).matches()) {
            setEntryText(string);
        }
    }

    @FXML private void backspace() {

        if (ghost) {
            entry.clear();
        }

        String entryText = getEntryText();

        if (!entryText.isEmpty()) {
            setEntryText(entryText.substring(0, entryText.length() - 1));
        }
    }

    @FXML private void cancel() {
        calcEntryMan.clearEntries();
        entry.setPromptText("");
        entry.clear();
    }

    @FXML private void cancelEntry() {
        calcEntryMan.clearLastEntry();
        entry.setPromptText("");
        entry.clear();
    }

    @FXML private void changeTheme() {

        String current = root.getStylesheets().get(0);

        root.getStylesheets().clear();

        if (current.equals(DARK_THEME_CSS)) {
            root.getStylesheets().add(LIGHT_THEME_CSS);
        } else {
            root.getStylesheets().add(DARK_THEME_CSS);
        }
    }

    private void appendOperation(BinaryOperator<BigDecimal> bo) {

        if (!getEntryText().isEmpty()) {
            if (calcEntryMan.retrieve() != null) {
                if (ghost && calcEntryMan.retrieve().getValue().equals(getEntryAsBigDecimal())) {
                    calcEntryMan.retrieve().setOperator(bo);
                    return;
                }
            }

            try {
                calcEntryMan.add(new Operation<>(getEntryAsBigDecimal(), bo));
                setGhostEntryText(calcEntryMan.retrieve().getValue());

            } catch (ArithmeticException e) {
                cancelEntry();
                entry.clear();
                entry.setPromptText(e.getMessage());
            }

        }

    }

    private void executeTransformation(UnaryOperator<BigDecimal> uo) {
        if (!getEntryText().isEmpty()) {
            setGhostEntryText(uo.apply(getEntryAsBigDecimal()));
        }

    }

    private double anglesOperations(double num, UnaryOperator<Double> uo) {
        if (anglesUnit.equals("DEG")) {
            return uo.apply(Math.toRadians(num));
        } else {
            return uo.apply(num);
        }
    }

    @FXML private void sum() {
        appendOperation((a, b) -> a.add(b, mc));
    }

    @FXML private void subtract() {
        appendOperation((a, b) -> a.subtract(b, mc));
    }

    @FXML private void multiply() {
        appendOperation((a, b) -> a.multiply(b, mc));
    }

    @FXML private void divide() {
        appendOperation((a, b) -> a.divide(b, mc));
    }

    @FXML private void changeSign() {
        executeTransformation(b -> b.negate(mc));
    }

    @FXML private void pow() {
        appendOperation((a, b) -> a.pow(b.intValue()));
    }

    @FXML private void sqrt() {
        executeTransformation(b -> b.sqrt(mc));
    }

    @FXML private void pi() {
        entry.clear();
        setGhostEntryText(BigDecimal.valueOf(Math.PI));
    }

    @FXML private void e() {
        entry.clear();
        setGhostEntryText(BigDecimal.valueOf(Math.E));
    }

    @FXML private void percentage() {
        appendOperation((a, b) -> a.multiply(b, mc).divide(BigDecimal.valueOf(100), mc));
    }

    @FXML private void sin() {
        executeTransformation(b -> BigDecimal.valueOf(anglesOperations(b.doubleValue(), Math::sin)));
    }

    @FXML private void asin() {
        executeTransformation(b -> BigDecimal.valueOf(Math.toDegrees(Math.asin(b.doubleValue()))));
    }

    @FXML private void cos() {
        executeTransformation(b -> BigDecimal.valueOf(anglesOperations(b.doubleValue(), Math::cos)));
    }

    @FXML private void radOrDeg() {
        if (anglesUnit.equals("RAD")) {
            anglesUnit = "DEG";
            radOrDeg.setText("DEG");
        } else {
            anglesUnit = "RAD";
            radOrDeg.setText("RAD");
        }
    }

    @FXML private void equal() {
        if (calcEntryMan.pending() && !getEntryText().isEmpty()) {
            appendOperation(null);
        }

        else if (calcEntryMan.retrieve() != null) {
            setGhostEntryText(calcEntryMan.retrieve().getValue());
        }
    }


}
