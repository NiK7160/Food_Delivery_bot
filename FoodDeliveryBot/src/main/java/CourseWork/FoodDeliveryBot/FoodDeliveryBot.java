package CourseWork.FoodDeliveryBot;

import CourseWork.FoodDeliveryBot.config.BotConfig;
import CourseWork.FoodDeliveryBot.model.Cart;
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
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;

@Component
@AllArgsConstructor
public class FoodDeliveryBot extends TelegramLongPollingBot {

    @Autowired
    private DishesRepository dishesRepository;
    Cart cart;
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
                cart.setChatId(chatId);
                startCommandReceived(chatId, userFirstName);
            } else if ("Меню".equals(textFromUser) || "Повернутися до вибору категорій страв".equals(textFromUser)) {
                state = "categories";
                sendMessage(chatId, "Оберіть категорію страв для перегляду: ", state);
            } else if ("Кошик".equals(textFromUser) || "Переглянути кошик".equals(textFromUser)) {
                state = "Кошик";
                sendMessage(chatId, cart.getCartInfo(), state);
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
            } else if ("Оформити замовлення".equals(textFromUser)) {
                state = "Оформити замовлення";
                sendMessage(chatId, "Ця функція знаходиться в розробці", state);
            } else if ("Внести зміни до кошику".equals(textFromUser)) {
                state = "Зміни у кошику";
                sendMessage(chatId, "Що бажаєте зробити?", state);
            } else if ("Видалити страву з кошику".equals(textFromUser)) {
                state = "Видалення з кошику";
                sendMessage(chatId, "Оберіть страву, яку бажаєте видалити:", state);
            } else if (cart.getDishNamesToChange("Видалення з кошику").contains(textFromUser)) {
                state = "Кошик";
                textFromUser = textFromUser.replaceFirst("Видалити ", "");
                cart.removeAllDishWithName(textFromUser);
                sendMessage(chatId, "Страву " + textFromUser + " видалено з кошику.", state);
            } else if ("Змінити кількість страв".equals(textFromUser)) {
                state = "Змінити кількість страв";
                sendMessage(chatId, "Оберіть страву, кількість якої бажаєте змінити:", state);
            } else if (cart.getDishNamesToChange("Змінити кількість страв").contains(textFromUser)) {
                state = "Кошик";
                textFromUser = textFromUser.replaceFirst("Змінити ", "");
                sendDishPhoto(chatId, cart.getDishPhoto(textFromUser), cart.getDishInfo(textFromUser), textFromUser, "Зміна кошику");
            } else if ("Очистити кошик".equals(textFromUser)) {
                state = "mainMenu";
                cart.removeAllFromCart();
                sendMessage(chatId, "Всі страви з кошику було видалено.", state);
            } else if ("Повернутися до перегляду кошика".equals(textFromUser)) {
                state = "Кошик";
                sendMessage(chatId, "Повернулися до перегляду кошика.", state);
            } else {
                state = "mainMenu";
                sendMessage(chatId, "Введена команда не підтримується.", state);
            }
        } else if (update.hasCallbackQuery()) {
            String callbackData = update.getCallbackQuery().getData();
            long chatId = update.getCallbackQuery().getMessage().getChatId();

            if (getDishNames(getCategoryOfDish(callbackData)).contains(callbackData)) {
                cart.addDishToCart(callbackData);
                String message = "Страва " + callbackData + " була додана до кошику!";
                sendMessage(chatId, message, "Додано в кошик");
            } else if (callbackData.equals("Кошик")) {
                sendMessage(chatId, cart.getCartInfo(), "Кошик");
            } else if (cart.getDishNamesToChange("Decrease").contains(callbackData)) {
                callbackData = callbackData.replaceFirst("Decrease ", "");
                cart.removeDish(callbackData);
                String message = "Кількість була зменшена!";
                sendMessage(chatId, message, "Перегляд кошику");
            } else if (cart.getDishNamesToChange("Increase").contains(callbackData)) {
                callbackData = callbackData.replaceFirst("Increase ", "");
                cart.addDishToCart(callbackData);
                String message = "Кількість була збільшена!";
                sendMessage(chatId, message, "Додано в кошик");
            }
        }
    }

    private void sendMessage(Long chatId, String textToSend, String state) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);
        message.setParseMode(ParseMode.HTML);
        keyboardBuilder(message, state);

        if (state.equals("Додано в кошик") || state.equals("Перегляд кошику"))
            message.setReplyMarkup(inlineKeyboardBuilder("Переглянути кошик", "Кошик", "Перегляд кошику"));

        try {
            execute(message);
        } catch (TelegramApiException e) {

        }
    }

    private void sendDishPhoto(Long chatId, String imageToSend, String textToSend, String dishName, String state) {
        SendPhoto photo = new SendPhoto();
        photo.setChatId(chatId);
        photo.setPhoto(new InputFile(imageToSend));
        photo.setParseMode(ParseMode.HTML);
        photo.setCaption(textToSend);

        if (state.equals("Додавання до кошику")) {
            photo.setReplyMarkup(inlineKeyboardBuilder("Додати до кошику", dishName, state));
        } else if (state.equals("Зміна кошику")) {
            photo.setReplyMarkup(inlineKeyboardBuilder("", dishName, state));
        }

        try {
            execute(photo);
        } catch (TelegramApiException e) {

        }
    }

    private InlineKeyboardMarkup inlineKeyboardBuilder(String ButtonText, String callbackData, String state) {
        InlineKeyboardMarkup inlineMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> inlineRows = new ArrayList<>();
        List<InlineKeyboardButton> inlineRow = new ArrayList<>();


        if (state.equals("Зміна кошику")) {
            InlineKeyboardButton decreaseButton = new InlineKeyboardButton();
            decreaseButton.setText("➖");
            decreaseButton.setCallbackData("Decrease " + callbackData);
            inlineRow.add(decreaseButton);

            InlineKeyboardButton increaseButton = new InlineKeyboardButton();
            increaseButton.setText("➕");
            increaseButton.setCallbackData("Increase " + callbackData);
            inlineRow.add(increaseButton);
        } else {
            InlineKeyboardButton inlineButton = new InlineKeyboardButton();

            inlineButton.setText(ButtonText);
            inlineButton.setCallbackData(callbackData);
            inlineRow.add(inlineButton);
        }

        inlineRows.add(inlineRow);
        inlineMarkup.setKeyboard(inlineRows);

        return inlineMarkup;
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
        } else if (state.equals("Кошик")) {
            row = new KeyboardRow();
            row.add("Оформити замовлення");
            row.add("Внести зміни до кошику");
            row.add("Переглянути кошик");
            keyboardRows.add(row);
            row = new KeyboardRow();
            row.add("Повернутися до вибору категорій страв");
            keyboardRows.add(row);
        } else if (state.equals("Зміни у кошику")) {
            row = new KeyboardRow();
            row.add("Змінити кількість страв");
            row.add("Видалити страву з кошику");
            row.add("Очистити кошик");
            keyboardRows.add(row);
            row = new KeyboardRow();
            row.add("Повернутися до перегляду кошика");
            keyboardRows.add(row);
        } else if (state.equals("Видалення з кошику") || state.equals("Змінити кількість страв")) {
            //TODO: Оптимізувати створення клавіатури
            row = new KeyboardRow();
            for (int i = 0; i < cart.getDishNamesToChange(state).size(); i++) {
                row.add(cart.getDishNamesToChange(state).get(i));
                if ((1 + i) % 3 == 0 || i == cart.getDishNamesToChange(state).size() - 1) {
                    keyboardRows.add(row);
                    row = new KeyboardRow();
                }
            }
            row = new KeyboardRow();
            row.add("Повернутися до вибору категорій страв");
            keyboardRows.add(row);

        } else {
            state = "mainMenu";
        }
        keyboardMarkup.setKeyboard(keyboardRows);
        message.setReplyMarkup(keyboardMarkup);
    }

    private void categoryKeyboardBuilder(String category, List<KeyboardRow> keyboardRows) {
        KeyboardRow row = new KeyboardRow();
        for (int i = 0; i < getDishNames(category).size(); i++) {
            row.add(getDishNames(category).get(i));
            if ((1 + i) % 3 == 0 || i == getDishNames(category).size() - 1) {
                keyboardRows.add(row);
                row = new KeyboardRow();
            }
        }
        row = new KeyboardRow();
        row.add("Повернутися до вибору категорій страв");
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
        for (Dish dish : dishes) {
            if (dish.getName().equals(dishName))
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
        sendDishPhoto(chatId, photo, message, dishName, "Додавання до кошику");
    }
}
