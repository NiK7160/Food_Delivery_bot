package CourseWork.FoodDeliveryBot;

import CourseWork.FoodDeliveryBot.config.BotConfig;
import CourseWork.FoodDeliveryBot.model.Dish;
import CourseWork.FoodDeliveryBot.model.DishesRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;

@Component
@AllArgsConstructor
public class FoodDeliveryBot extends TelegramLongPollingBot {

    @Autowired
    private DishesRepository dishesRepository;
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
        String state;
        if (update.hasMessage() && update.getMessage().hasText()) {
            String textFromUser = update.getMessage().getText();
            Long chatId = update.getMessage().getChatId();
            String userFirstName = update.getMessage().getFrom().getFirstName();

            if ("/start".equals(textFromUser) || "Старт".equals(textFromUser)) {
                startCommandReceived(chatId, userFirstName);
            } else if ("Меню".equals(textFromUser) || "Повернутися до меню".equals(textFromUser)) {
                state = "categories";
                sendMessage(chatId, "Оберіть категорію страв для перегляду: ", state);
            } else if ("Кошик".equals(textFromUser)) {
                state = "mainMenu";
                sendMessage(chatId, "Ця функція знаходиться в розробці", state);
            } else if (getDishCategories().contains(textFromUser)) {
                state = textFromUser;
                sendMessage(chatId, "Для отримання інформації про страву оберіть її з меню", state);
            } else if (getDishNames(getCategoryOfDish(textFromUser)).contains(textFromUser)) {
                getDishInfo(chatId, getCategoryOfDish(textFromUser), textFromUser);
            } else if ("Повернутися в головне меню".equals(textFromUser)) {
                state = "mainMenu";
                sendMessage(chatId, "Повернулися в головне меню. \nЩо бажаєте зробити?", state);
            } else if ("Все меню".equals(textFromUser)) {
                state = "All";
                sendMessage(chatId, "Для отримання інформації про страву оберіть її з меню", state);
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

    private void sendPhoto(Long chatId, String imageToSend, String textToSend, String state) {
        SendPhoto photo = new SendPhoto();
        photo.setChatId(chatId);
        photo.setPhoto(new InputFile(imageToSend));
        photo.setParseMode(ParseMode.HTML);
        photo.setCaption(textToSend);
        try {
            execute(photo);
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
        } else if (state.equalsIgnoreCase("mainMenu")) {
            row = new KeyboardRow();
            row.add("Меню");
            row.add("Кошик");
            keyboardRows.add(row);
        } else if (state.equalsIgnoreCase("categories")) {
            row = new KeyboardRow();
            row.add("Бургери");
            row.add("Піца");
            row.add("Суші та Роли");
            keyboardRows.add(row);
            row = new KeyboardRow();
            row.add("Страви на грилі");
            row.add("Гарячі страви");
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
        } else if (getDishCategories().contains(state) || state.equals("All")) {
            categoryKeyboardBuilder(state, keyboardRows);
        } else {
            state = "mainMenu";
        }
        keyboardMarkup.setKeyboard(keyboardRows);
        message.setReplyMarkup(keyboardMarkup);
    }

    private void categoryKeyboardBuilder(String category, List<KeyboardRow> keyboardRows) {
        KeyboardRow row = new KeyboardRow();
        int j = 1;
        for (int i = 0; i < getDishNames(category).size(); i++) {
            row.add(getDishNames(category).get(i));
            if (j % 3 == 0) {
                keyboardRows.add(row);
                row = new KeyboardRow();
            }
            j++;
        }
        row = new KeyboardRow();
        row.add("Повернутися до меню");
        keyboardRows.add(row);
    }

    private List<String> getDishNames(String dishCategory) {
        var dishes = dishesRepository.findAll();

        List<String> dishNames = new ArrayList<>();

        for (Dish dish : dishes) {
            if (dishCategory.equals(dish.getCategory()) || dishCategory.equals("All"))
                dishNames.add((dish.getName()).trim());
        }
        return dishNames;
    }

    private List<String> getDishCategories() {
        var dishes = dishesRepository.findAll();

        List<String> dishCategories = new ArrayList<>();

        for (Dish dish : dishes) {
            if (!dishCategories.contains(dish.getCategory()))
                dishCategories.add(dish.getCategory());
        }
        return dishCategories;
    }

    private String getCategoryOfDish(String dishName) {
        String category = "none";
        var dishes = dishesRepository.findAll();
        for (Dish dish : dishes){
            if(dish.getName().equals(dishName))
                category = dish.getCategory();
        }
        return category;
    }

    private void getDishInfo(Long chatId, String dishCategory, String dishName) {
        var dishes = dishesRepository.findAll();

        String message = "Помилка";
        String photo = null;

        for (Dish dish : dishes) {
            if (dishName.equals((dish.getName().trim())) && dishCategory.equals(dish.getCategory())) {
                if (!dish.getCategory().equals("Напої")) {
                    message = "<b>" + dish.getName() + " - " + dish.getPrice() + " грн.</b>\n\n<i>" + dish.getWeight() + " грам, " +
                            dish.getAdditional() + "\n" + dish.getDescription() + "</i>";
                } else {
                    message = "<b>" + dish.getName() + " - " + dish.getPrice() + " грн.</b>\n\n<i>" + dish.getWeight() + " л, " +
                            dish.getAdditional() + "\n" + dish.getDescription() + "</i>";
                }
                photo = dish.getImage();
            }
        }
        sendPhoto(chatId, photo, message, "mainMenu");
    }
}
