package CourseWork.FoodDeliveryBot;

import CourseWork.FoodDeliveryBot.config.BotConfig;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;

@Component
@AllArgsConstructor
public class FoodDeliveryBot extends TelegramLongPollingBot {

    private final BotConfig botConfig;

    @Override
    public String getBotUsername() {
        return botConfig.getBotName();
    }

    @Override
    public String getBotToken() {
        return botConfig.getToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        String state = "start";

        if (update.hasMessage() && update.getMessage().hasText()) {
            String textFromUser = update.getMessage().getText();
            Long chatId = update.getMessage().getChatId();
            String userFirstName = update.getMessage().getFrom().getFirstName();

            if ("/start".equals(textFromUser) || "Старт".equals(textFromUser)) {
                state = "mainMenu";
                startCommandReceived(chatId, userFirstName);
            } else if ("Меню".equals(textFromUser)) {
                state = "categories";
                sendMessage(chatId, "Оберіть категорію страв для перегляду: ", state);
            } else if ("Кошик".equals(textFromUser)) {
                state = "mainMenu";
                sendMessage(chatId, "Ця функція знаходиться в розробці", state);
            } else if ("Бургери".equals(textFromUser)) {
                state = "burgers";
                sendMessage(chatId, "Для отримання інформації про страву оберіть її з меню", state);
            } else if ("Повернутися в головне меню".equals(textFromUser)) {
                state = "mainMenu";
                sendMessage(chatId, "Повернулися в головне меню. \nЩо бажаєте зробити?", state);
            } else {
                state = "mainMenu";
                sendMessage(chatId, "Введена команда не підтримується.", state);
            }
        }

    }

    private void sendMessage(Long chatId, String textToSend, String state) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);
        keyboardBuilder(message, state);
        try {
            execute(message);
        } catch (TelegramApiException e) {

        }
    }

    private void startCommandReceived(Long chatId, String name) {
        String state = "mainMenu";
        String answer = "Вітаю, " + name + "!\uD83D\uDC4B" + "\n" +
                "Тут Ви можете замовити доставку своєї улюбленої страви!" + "\n" +
                "Обирайте дію з меню нижче⬇";
        sendMessage(chatId, answer, state);
    }

    private void keyboardBuilder(SendMessage message, String state) {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboardRows = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();

        if (state.equalsIgnoreCase("start")) {
            row.add("Старт");
            keyboardRows.add(row);
            state = "mainMenu";
        } else if (state.equalsIgnoreCase("mainMenu")) {
            row = new KeyboardRow();
            row.add("Меню");
            row.add("Кошик");
            keyboardRows.add(row);
            state = "categories";
        } else if (state.equalsIgnoreCase("categories")) {
            row = new KeyboardRow();
            row.add("Бургери");
            row.add("Піца");
            row.add("Суші");
            keyboardRows.add(row);
            row = new KeyboardRow();
            row.add("Роли");
            row.add("Закуски");
            row.add("Салати");
            keyboardRows.add(row);
            row = new KeyboardRow();
            row.add("Напої");
            row.add("Десерти");
            row.add("Все меню");
            keyboardRows.add(row);
            row = new KeyboardRow();
            row.add("Повернутися в головне меню");
            keyboardRows.add(row);
        } else if (state.equalsIgnoreCase("burgers")) {
            row = new KeyboardRow();
            row.add("Бургер 1");
            row.add("Бургер 2");
            row.add("Бургер 3");
            keyboardRows.add(row);
        } else {
            state = "mainMenu";
        }
        keyboardMarkup.setKeyboard(keyboardRows);
        message.setReplyMarkup(keyboardMarkup);
    }
}
