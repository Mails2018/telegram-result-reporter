package ru.invitro.automation.notification.telegram.keyboard;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Keyboard {

    private final InlineKeyboardMarkup inlineKeyboard = new InlineKeyboardMarkup();

    private final Map<String, String> keyboardButtons = new HashMap<>();

    Keyboard() {
    }

    public InlineKeyboardMarkup getInlineKeyboard() {
        return inlineKeyboard;
    }

    public Map<String, String> getKeyboardButtons() {
        return keyboardButtons;
    }

    public Keyboard makeKeyboard(List<String> buttons, int columnsNumbers) {
        List<List<InlineKeyboardButton>> keyboardButtonsList = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();
        int count = 0;
        InlineKeyboardButton keyboardButton;
        for (String button : buttons) {
            int key = (int) (Math.random() * ((1000000 - 1000) + 1)) + 1000;
            keyboardButton = new InlineKeyboardButton();
            keyboardButton.setCallbackData("" + key);
            keyboardButton.setText(button);
            row.add(keyboardButton);
            keyboardButtons.put("" + key, button);
            count++;
            if (count == columnsNumbers) {
                keyboardButtonsList.add(row);
                count = 0;
                row = new ArrayList<>();
            }
        }
        if (row.size() > 0) {
            keyboardButtonsList.add(row);
        }
        inlineKeyboard.setKeyboard(keyboardButtonsList);
        return this;
    }

    public Keyboard addButtons(List<String> buttons, int columnsNumbers) {
        List<List<InlineKeyboardButton>> keyboardButtonsList = inlineKeyboard.getKeyboard();
        List<InlineKeyboardButton> row = new ArrayList<>();
        int count = 0;
        InlineKeyboardButton keyboardButton;
        for (String button : buttons) {
            int key = (int) (Math.random() * ((1000000 - 1000) + 1)) + 1000;
            keyboardButton = new InlineKeyboardButton();
            keyboardButton.setCallbackData("" + key);
            keyboardButton.setText(button);
            row.add(keyboardButton);
            keyboardButtons.put("" + key, button);
            count++;
            if (count == columnsNumbers) {
                keyboardButtonsList.add(row);
                count = 0;
                row = new ArrayList<>();
            }
        }
        if (row.size() > 0) {
            keyboardButtonsList.add(row);
        }
        inlineKeyboard.setKeyboard(keyboardButtonsList);
        return this;
    }
}
