package CourseWork.FoodDeliveryBot;

import CourseWork.FoodDeliveryBot.config.BotConfig;
import CourseWork.FoodDeliveryBot.model.*;
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
import java.util.Objects;

@Component
//@AllArgsConstructor
public class FoodDeliveryBot extends TelegramLongPollingBot {

    public static final String START = "Старт";
    public static final String MAIN_MENU = "Головне меню";
    public static final String CATEGORY_OF_DISHES = "Категорії страв";
    public static final String CART = "Кошик";
    public static final String ALL_DISHES = "Все меню";
    public static final String MAKE_ORDER = "Оформити замовлення";
    public static final String CART_CHANGES = "Зміни у кошику";
    public static final String REMOVE_FROM_CART = "Видалення з кошику";
    public static final String CHANGE_NUMBER_OF_DISHES = "Змінити кількість страв";
    public static final String ADDED_TO_CART = "Додано в кошик";
    public static final String CART_REVIEW = "Перегляд кошику";
    public static final String ADD_TO_CART = "Додавання до кошику";
    public static final String DELIVERY_ADDRESS = "Вказати адресу доставки";
    public static final String PHONE_NUMBER = "Вказати номер телефону";
    public static final String PAYMENT_METHOD = "Вказати спосіб оплати";
    public static final String ADDITIONAL_INFO = "Вказати додаткову інформацію";
    public static final String CONFIRM_ORDER = "Підтвердити замовлення";
    public static final String ORDER_COMPLETED = "Замовлення оформлено";
    public static final String PREVIOUS_ORDERS_REVIEW = "Перегляд минулих замовлень";

    @Autowired
    private final DishesRepository dishesRepository;
    @Autowired
    private final OrdersRepository ordersRepository;
    Cart cart;
    Order order;
    private final BotConfig botConfig;
    private String state;

    public FoodDeliveryBot(DishesRepository dishesRepository, OrdersRepository ordersRepository, Cart cart, BotConfig botConfig) {
        this.dishesRepository = dishesRepository;
        this.ordersRepository = ordersRepository;
        this.cart = cart;
        this.order = new Order();
        this.botConfig = botConfig;
        this.state = START;
    }

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
        if (update.hasMessage() && update.getMessage().hasText()) {
            String textFromUser = update.getMessage().getText();
            Long chatId = update.getMessage().getChatId();
            Long userId = update.getMessage().getFrom().getId();
            String userFirstName = update.getMessage().getFrom().getFirstName();

            if ("/start".equals(textFromUser) || "Старт".equals(textFromUser)) {
                cart.setChatId(chatId);
                startCommandReceived(chatId, userFirstName);
            } else if ("Меню".equals(textFromUser) || "Повернутися до вибору категорій страв".equals(textFromUser)) {
                state = CATEGORY_OF_DISHES;
                sendMessage(chatId, "Оберіть категорію страв для перегляду: ");
            } else if ("Кошик".equals(textFromUser) || "Переглянути кошик".equals(textFromUser)) {
                state = CART;
                sendMessage(chatId, cart.getCartInfo());
            } else if (getDishCategories().contains(textFromUser)) {
                state = textFromUser;
                sendMessage(chatId, "Для отримання інформації про страву оберіть її з меню");
            } else if (getDishNames(getCategoryOfDish(textFromUser)).contains(textFromUser)) {
                getDishInfo(chatId, getCategoryOfDish(textFromUser), textFromUser);
            } else if ("Повернутися в головне меню".equals(textFromUser)) {
                state = MAIN_MENU;
                sendMessage(chatId, "Повернулися в головне меню. \nЩо бажаєте зробити?");
            } else if ("Все меню".equals(textFromUser)) {
                state = ALL_DISHES;
                sendMessage(chatId, "Для отримання інформації про страву оберіть її з меню");
            } else if ("Оформити замовлення".equals(textFromUser)) {
                if (cart.isEmpty()) {
                    sendMessage(chatId, "Кошик порожній, тому оформити замовлення неможливо!");
                } else {
                    state = MAKE_ORDER;
                    sendMessage(chatId, "Введіть ім'я одержувача замовлення або виберіть серед запропонованих варіантів:");
                }

            } else if (state.equals(MAKE_ORDER)) {
                order.setChatId(chatId);
                order.setUserName(textFromUser);
                order.setOrderList(cart.getOrderList());
                order.setOrderPrice(cart.getTotalPrice());
                state = DELIVERY_ADDRESS;
                sendMessage(chatId, "Вкажіть адресу доставки або виберіть серед запропонованих варіантів:");
            } else if(state.equals(DELIVERY_ADDRESS)){
                order.setDeliveryAddress(textFromUser);
                state = PHONE_NUMBER;
                sendMessage(chatId, "Вкажіть номер телефону для зв'язку або виберіть серед запропонованих варіантів:");
            } else if(state.equals(PHONE_NUMBER)){
                order.setPhoneNumber(Long.parseLong(textFromUser));
                state = PAYMENT_METHOD;
                sendMessage(chatId, "Оберіть спосіб оплати замовлення:");
            } else if(state.equals(ADDITIONAL_INFO)){
                order.setAdditionalInfo(textFromUser);
                state = CONFIRM_ORDER;
                String message = "<b>Замовлення: </b>\n\nОтримувач: " + order.getUserName() + "\nНомер телефону: " + order.getPhoneNumber() +
                        "\nАдреса доставки: " + order.getDeliveryAddress() + "\nСпосіб оплати: " + order.getPaymentMethod() +
                        "\nДодаткова інформація: " + order.getAdditionalInfo() + "\n\n Страви: \n" + order.getOrderList() +
                        "\n<b>До сплати: " + order.getOrderPrice()+ " грн. </b>";
                sendMessage(chatId, message);
            } else if ("Внести зміни до кошику".equals(textFromUser)) {
                state = CART_CHANGES;
                sendMessage(chatId, "Що бажаєте зробити?");
            } else if ("Видалити страву з кошику".equals(textFromUser)) {
                state = REMOVE_FROM_CART;
                sendMessage(chatId, "Оберіть страву, яку бажаєте видалити:");
            } else if (cart.getDishNamesToChange("Видалення з кошику").contains(textFromUser)) {
                state = CART;
                textFromUser = textFromUser.replaceFirst("Видалити ", "");
                cart.removeAllDishWithName(textFromUser);
                sendMessage(chatId, "Страву " + textFromUser + " видалено з кошику.");
            } else if ("Змінити кількість страв".equals(textFromUser)) {
                state = CHANGE_NUMBER_OF_DISHES;
                sendMessage(chatId, "Оберіть страву, кількість якої бажаєте змінити:");
            } else if (cart.getDishNamesToChange("Змінити кількість страв").contains(textFromUser)) {
                state = CART_CHANGES;
                textFromUser = textFromUser.replaceFirst("Змінити ", "");
                sendDishPhoto(chatId, cart.getDishPhoto(textFromUser), cart.getDishInfo(textFromUser), textFromUser);
            } else if ("Очистити кошик".equals(textFromUser)) {
                state = MAIN_MENU;
                cart.removeAllFromCart();
                sendMessage(chatId, "Всі страви з кошику було видалено.");
            } else if ("Повернутися до перегляду кошика".equals(textFromUser)) {
                state = CART;
                sendMessage(chatId, "Повернулися до перегляду кошика.");
            } else if("Перегляд минулих замовлень".equals(textFromUser)){
                state = PREVIOUS_ORDERS_REVIEW;
                sendMessage(chatId, "Оберіть замовлення, яке бажаєте переглянути:");
            } else if(state.equals(PREVIOUS_ORDERS_REVIEW) && getElementFromOrdersByChatId(chatId).contains(textFromUser)) {
                //було state.equals(PREVIOUS_ORDERS_REVIEW) && getIdOrdersByChatId(chatId).contains(textFromUser)
                sendMessage(chatId, getOrderInfo(Objects.requireNonNull(getOrderById(Long.parseLong(textFromUser)))));
            } else {
                state = MAIN_MENU;
                sendMessage(chatId, "Введена команда не підтримується.");
            }
        } else if (update.hasCallbackQuery()) {
            String callbackData = update.getCallbackQuery().getData();
            long chatId = update.getCallbackQuery().getMessage().getChatId();

            if (getDishNames(getCategoryOfDish(callbackData)).contains(callbackData) && state.equals(ADD_TO_CART)) {
                cart.addDishToCart(callbackData);
                String message = "Страва " + callbackData + " була додана до кошику!";
                state = ADDED_TO_CART;
                sendMessage(chatId, message);
            } else if (callbackData.equals("Кошик") && (state.equals(ADDED_TO_CART) || state.equals(CART_REVIEW))) {
                state = CART;
                sendMessage(chatId, cart.getCartInfo());
            } else if (cart.getDishNamesToChange("Decrease").contains(callbackData) && state.equals(CART_CHANGES)) {
                callbackData = callbackData.replaceFirst("Decrease ", "");
                cart.removeDish(callbackData);
                state = CART_REVIEW;
                sendMessage(chatId, "Кількість була зменшена!");
            } else if (cart.getDishNamesToChange("Increase").contains(callbackData) && state.equals(CART_CHANGES)) {
                callbackData = callbackData.replaceFirst("Increase ", "");
                cart.addDishToCart(callbackData);
                state = ADDED_TO_CART; //state = CART_REVIEW;
                sendMessage(chatId, "Кількість була збільшена!");
            } else if((callbackData.equals("Карта") || callbackData.equals("Готівка")) && state.equals(PAYMENT_METHOD)){
                order.setPaymentMethod(callbackData);
                state = ADDITIONAL_INFO;
                sendMessage(chatId, "Вкажіть додаткову інформацію до замовлення:");
            } else if(callbackData.equals("Пропустити") && state.equals(ADDITIONAL_INFO)){
                state = CONFIRM_ORDER;
                String message = "<b>Замовлення: </b>\n\nОтримувач: " + order.getUserName() + "\nНомер телефону: " + order.getPhoneNumber() +
                        "\nАдреса доставки: " + order.getDeliveryAddress() + "\nСпосіб оплати: " + order.getPaymentMethod() +
                        "\n\n Страви: \n" + order.getOrderList() +
                        "\n<b>До сплати: " + order.getOrderPrice()+ " грн. </b>";
                sendMessage(chatId, message);
            } else if(callbackData.equals("Оформити замовлення") && state.equals(CONFIRM_ORDER)){
                order.setStatus("В обробці");
                ordersRepository.save(order);
                String message = "Замовлення №" + order.getId() + " оформлено успішно!";
                cart.removeAllFromCart();
                order = new Order();
//                state = ORDER_COMPLETED;
                state = MAIN_MENU;
                sendMessage(chatId, message);
            }
        }
    }

    private void sendMessage(Long chatId, String textToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);
        message.setParseMode(ParseMode.HTML);
        keyboardBuilder(message);

        if (state.equals(ADDED_TO_CART) || state.equals(CART_REVIEW))
            message.setReplyMarkup(inlineKeyboardBuilder("Переглянути кошик", "Кошик"));
        else if (state.equals(PAYMENT_METHOD)) {
            message.setReplyMarkup(inlineKeyboardBuilder("", ""));
        } else if(state.equals(ADDITIONAL_INFO)){
            message.setReplyMarkup(inlineKeyboardBuilder("Пропустити", "Пропустити"));
        } else if(state.equals(CONFIRM_ORDER)){
            message.setReplyMarkup(inlineKeyboardBuilder("Оформити замовлення", "Оформити замовлення"));
        } //else if (state.equals(PREVIOUS_ORDERS_REVIEW)) {
//            message.setReplyMarkup(keyboardBuilderForOrderElements(getIdOrdersByChatId(chatId)));
//        } else if (state.equals(MAKE_ORDER)){
//            message.setReplyMarkup(keyboardBuilderForOrderElements(getUserNamesFromOrdersByChatId(chatId)));
//        } else if (state.equals(PHONE_NUMBER)){
//            message.setReplyMarkup(keyboardBuilderForOrderElements(getPhoneNumberFromOrdersByChatId(chatId)));
//        } else if (state.equals(DELIVERY_ADDRESS)){
//            message.setReplyMarkup(keyboardBuilderForOrderElements(getAddressFromOrdersByChatId(chatId)));
//        }
        else if (state.equals(PREVIOUS_ORDERS_REVIEW) || state.equals(MAKE_ORDER) || state.equals(PHONE_NUMBER) || state.equals(DELIVERY_ADDRESS)){
            message.setReplyMarkup(keyboardBuilderForOrderElements(getElementFromOrdersByChatId(chatId)));
        }

        try {
            execute(message);
        } catch (TelegramApiException e) {

        }
    }

    private void sendDishPhoto(Long chatId, String imageToSend, String textToSend, String dishName) {
        SendPhoto photo = new SendPhoto();
        photo.setChatId(chatId);
        photo.setPhoto(new InputFile(imageToSend));
        photo.setParseMode(ParseMode.HTML);
        photo.setCaption(textToSend);

        if (state.equals(ADD_TO_CART)) {
            photo.setReplyMarkup(inlineKeyboardBuilder("Додати до кошику", dishName));
        } else if (state.equals(CART_CHANGES)) {
            photo.setReplyMarkup(inlineKeyboardBuilder("", dishName));
        }

        try {
            execute(photo);
        } catch (TelegramApiException e) {

        }
    }

    private InlineKeyboardMarkup inlineKeyboardBuilder(String ButtonText, String callbackData) {
        InlineKeyboardMarkup inlineMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> inlineRows = new ArrayList<>();
        List<InlineKeyboardButton> inlineRow = new ArrayList<>();


        if (state.equals(CART_CHANGES)) {
            InlineKeyboardButton decreaseButton = new InlineKeyboardButton();
            decreaseButton.setText("➖");
            decreaseButton.setCallbackData("Decrease " + callbackData);
            inlineRow.add(decreaseButton);

            InlineKeyboardButton increaseButton = new InlineKeyboardButton();
            increaseButton.setText("➕");
            increaseButton.setCallbackData("Increase " + callbackData);
            inlineRow.add(increaseButton);
        } else if(state.equals(PAYMENT_METHOD)){
            InlineKeyboardButton cardButton = new InlineKeyboardButton();
            cardButton.setText("Карта");
            cardButton.setCallbackData("Карта");
            inlineRow.add(cardButton);

            InlineKeyboardButton cashButton = new InlineKeyboardButton();
            cashButton.setText("Готівка");
            cashButton.setCallbackData("Готівка");
            inlineRow.add(cashButton);
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
        state = MAIN_MENU;
        String answer = "Вітаю, " + name + "!\uD83D\uDC4B" + "\n" +
                "Тут Ви можете замовити доставку своєї улюбленої страви!" + "\n" +
                "Обирайте дію з меню нижче⬇";
        sendMessage(chatId, answer);
    }

    private void keyboardBuilder(SendMessage message) {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboardRows = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();

        if (state.equals(START)) {
            row.add("Старт");
            keyboardRows.add(row);
        } else if (state.equals(MAIN_MENU)) {
            row = new KeyboardRow();
            row.add("Меню");
            row.add("Кошик");
            row.add("Перегляд минулих замовлень");
            keyboardRows.add(row);
        } else if (state.equals(CATEGORY_OF_DISHES)) {
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
        } else if (getDishCategories().contains(state) || state.equals(ALL_DISHES)) {
            categoryKeyboardBuilder(state, keyboardRows);
        } else if (state.equals(CART)) {
            row = new KeyboardRow();
            row.add("Оформити замовлення");
            row.add("Внести зміни до кошику");
            row.add("Переглянути кошик");
            keyboardRows.add(row);
            row = new KeyboardRow();
            row.add("Повернутися до вибору категорій страв");
            keyboardRows.add(row);
        } else if (state.equals(CART_CHANGES)) {
            row = new KeyboardRow();
            row.add("Змінити кількість страв");
            row.add("Видалити страву з кошику");
            row.add("Очистити кошик");
            keyboardRows.add(row);
            row = new KeyboardRow();
            row.add("Повернутися до перегляду кошика");
            keyboardRows.add(row);
        } else if (state.equals(REMOVE_FROM_CART) || state.equals(CHANGE_NUMBER_OF_DISHES)) {
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
            row.add("Повернутися до перегляду кошика");
            keyboardRows.add(row);

        }//else state = "mainMenu";
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
            if (dishCategory.equals(dish.getCategory()) || dishCategory.equals(ALL_DISHES))
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
        state = ADD_TO_CART;
        sendDishPhoto(chatId, photo, message, dishName);
    }

    private List<Order> getOrdersByChatId(long chatId){
        var orders = ordersRepository.findAll();

        List<Order> userOrders = new ArrayList<>();

        for (Order order : orders){
            if(order.getChatId() == chatId)
                userOrders.add(order);
        }

        return userOrders;
    }

    private Order getOrderById(long orderId){
        var orders = ordersRepository.findAll();

        for (Order order : orders){
            if(order.getId() == orderId)
                return order;
        }

        return null;
    }

    //TODO: Винести в один метод створення List<String> різних атрибутів Orders
//    private List<String> getIdOrdersByChatId(long chatId){
//        List<Order> orders = getOrdersByChatId(chatId);
//
//        List<String> ordersId = new ArrayList<>();
//
//        for (Order order : orders){
//            ordersId.add("" + order.getId());
//        }
//
//        return ordersId;
//    }
//
//    private List<String> getUserNamesFromOrdersByChatId(long chatId){
//        List<Order> orders = getOrdersByChatId(chatId);
//
//        List<String> userNames = new ArrayList<>();
//
//        for (Order order : orders){
//            if(!userNames.contains(order.getUserName()))
//                userNames.add(order.getUserName());
//        }
//
//        return userNames;
//    }
//
//    private List<String> getAddressFromOrdersByChatId(long chatId){
//        List<Order> orders = getOrdersByChatId(chatId);
//
//        List<String> address = new ArrayList<>();
//
//        for (Order order : orders){
//            if(!address.contains(order.getDeliveryAddress()))
//                address.add(order.getDeliveryAddress());
//        }
//
//        return address;
//    }
//
//    private List<String> getPhoneNumberFromOrdersByChatId(long chatId){
//        List<Order> orders = getOrdersByChatId(chatId);
//
//        List<String> phoneNumber = new ArrayList<>();
//
//        for (Order order : orders){
//            if (!phoneNumber.contains("" + order.getPhoneNumber()))
//                phoneNumber.add("" + order.getPhoneNumber());
//        }
//
//        return phoneNumber;
//    }

    private List<String> getElementFromOrdersByChatId(long chatId) {
        List<Order> orders = getOrdersByChatId(chatId);

        List<String> result = new ArrayList<>();

        switch (state) {
            case PREVIOUS_ORDERS_REVIEW:
                for (Order order : orders) {
                    result.add("" + order.getId());
                }
                break;
            case MAKE_ORDER:
                for (Order order : orders) {
                    if (!result.contains(order.getUserName()))
                        result.add(order.getUserName());
                }
                break;
            case PHONE_NUMBER:
                for (Order order : orders) {
                    if (!result.contains("" + order.getPhoneNumber()))
                        result.add("" + order.getPhoneNumber());
                }
                break;
            case DELIVERY_ADDRESS:
                for (Order order : orders) {
                    if (!result.contains(order.getDeliveryAddress()))
                        result.add(order.getDeliveryAddress());
                }
                break;
        }

        return result;
    }

    private String getOrderInfo(Order order){
        String orderInfo = "<b>Замовлення №"+ order.getId() + "</b>\n\nСтатус: " + order.getStatus() + "\nОтримувач: " + order.getUserName() +
                "\nНомер телефону: " + order.getPhoneNumber() + "\nАдреса доставки: " + order.getDeliveryAddress() +
                "\nСпосіб оплати: " + order.getPaymentMethod();

        if (order.getAdditionalInfo() != null){
            orderInfo = orderInfo + "\nДодаткова інформація: " + order.getAdditionalInfo();
        }
        orderInfo = orderInfo + "\n\n Страви: \n" + order.getOrderList() + "\n<b>До сплати: " + order.getOrderPrice()+ " грн. </b>";

        return orderInfo;
    }

    private ReplyKeyboardMarkup keyboardBuilderForOrderElements(List<String> list){
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboardRows = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();

        for (int i = 0; i < list.size(); i++) {
            row.add(list.get(i));
            if ((1 + i) % 3 == 0 || i == list.size() - 1) {
                keyboardRows.add(row);
                row = new KeyboardRow();
            }
        }
        row = new KeyboardRow();

        row.add("Повернутися в головне меню");
        keyboardRows.add(row);

        keyboardMarkup.setKeyboard(keyboardRows);
        return keyboardMarkup;
    }

}
