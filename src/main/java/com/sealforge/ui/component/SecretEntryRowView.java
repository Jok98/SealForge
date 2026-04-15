package com.sealforge.ui.component;

import com.sealforge.ui.model.SecretEntryRowModel;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

public final class SecretEntryRowView extends HBox {

    private final TextField keyField = new TextField();
    private final SensitiveValueField valueField = new SensitiveValueField();
    private final Button removeButton = new Button("Remove");

    public SecretEntryRowView(SecretEntryRowModel model, Runnable removeAction) {
        setSpacing(8);
        setAlignment(Pos.CENTER_LEFT);

        keyField.setPromptText("Key");
        keyField.textProperty().bindBidirectional(model.keyProperty());
        valueField.textProperty().bindBidirectional(model.valueProperty());
        removeButton.setOnAction(event -> removeAction.run());

        HBox.setHgrow(keyField, Priority.SOMETIMES);
        HBox.setHgrow(valueField, Priority.ALWAYS);
        getChildren().addAll(keyField, valueField, removeButton);
    }

    public void clearSensitiveValue() {
        valueField.clear();
    }
}

