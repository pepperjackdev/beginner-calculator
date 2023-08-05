package com.calculator.calculator;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;

import java.util.List;
import java.util.Objects;
import java.util.function.BinaryOperator;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

public class Controller {

    private static final String ENTRY_TEXT_REGEX = "(?:[1-9][0-9]*|0)(?:[.][0-9]*)?";
    private Pattern pattern;
    private CalculatorEntryManager<Double> calcEntryMan;
    private boolean ghost = false;
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

    private void setEntryText(double num) {
        setEntryText(String.valueOf(num));
    }

    private void setGhostEntryText(double num) {
        setEntryText(num);
        ghost = true;
    }

    private double getEntryAsDouble() {
        return Double.parseDouble(getEntryText());
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

    private void appendOperation(BinaryOperator<Double> num) {

        if (!getEntryText().isEmpty()) {
            if (calcEntryMan.retrieve() != null) {
                if (ghost && calcEntryMan.retrieve().getValue() == getEntryAsDouble()) {
                    calcEntryMan.retrieve().setOperator(num);
                    return;
                }
            }

            try {
                calcEntryMan.add(new Operation<>(getEntryAsDouble(), num));
                setGhostEntryText(calcEntryMan.retrieve().getValue());

            } catch (ArithmeticException e) {
                cancelEntry();
                entry.clear();
                entry.setPromptText(e.getMessage());
            }

        }

    }

    private void executeTransformation(UnaryOperator<Double> uo) {
        if (!getEntryText().isEmpty()) {
            setGhostEntryText(uo.apply(getEntryAsDouble()));
        }
    }

    private void executeTransformation(Supplier<Double> sup) {
        setGhostEntryText(sup.get());
    }

    private double fromAngleUnit(double num, UnaryOperator<Double> uo) {
        if (anglesUnit.equals("DEG")) {
            return uo.apply(Math.toRadians(num));
        } else {
            return uo.apply(num);
        }
    }

    private double toAngleUnit(double num, UnaryOperator<Double> uo) {
        if (anglesUnit.equals("DEG")) {
            return Math.toDegrees(uo.apply(num));
        } else {
            return uo.apply(num);
        }
    }

    @FXML private void sum() {
        appendOperation(Double::sum);
    }

    @FXML private void subtract() {
        appendOperation((a, b) -> a - b);
    }

    @FXML private void multiply() {
        appendOperation((a, b) -> a * b);
    }

    @FXML private void divide() {
        appendOperation((a, b) -> a / b);
    }

    @FXML private void changeSign() {
        executeTransformation(b -> -b);
    }

    @FXML private void pow() {
        appendOperation(Math::pow);
    }

    @FXML private void sqrt() {
        executeTransformation(Math::sqrt);
    }

    @FXML private void pi() {
        executeTransformation(() -> Math.PI);
    }

    @FXML private void e() {
        executeTransformation(() -> Math.E);
    }

    @FXML private void percentage() {
        appendOperation((a, b) -> a * b / 100);
    }

    @FXML private void sin() {
        executeTransformation(b -> fromAngleUnit(b, Math::sin));
    }

    @FXML private void asin() {
        executeTransformation(b -> toAngleUnit(b, Math::asin));
    }

    @FXML private void cos() {
        executeTransformation(b -> fromAngleUnit(b, Math::cos));
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
