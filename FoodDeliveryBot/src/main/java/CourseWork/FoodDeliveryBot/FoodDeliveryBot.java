package CourseWork.FoodDeliveryBot;

import CourseWork.FoodDeliveryBot.config.BotConfig;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

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

        if (update.hasMessage() && update.getMessage().hasText()){
            String textFromUser = update.getMessage().getText();
            Long chatId = update.getMessage().getChatId();
            String userFirstName = update.getMessage().getFrom().getFirstName();

            switch (textFromUser){
                case "/start":
                    startCommandReceived(chatId, userFirstName);
                    break;
                default:
                    sendMessage(chatId, "На поточний момент ця дія не доступна.");
                    break;
            }
        }

    }

    private void sendMessage(Long chatId, String textToSend){
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText(textToSend);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {

        }
    }

    private void startCommandReceived(Long chatId, String name) {
        String answer = "Вітаю, " + name + "!" + "\n" +
                "За допомогою цього чат-бота Ви зможете замовити доставку своєї улюбленої страви!" + "\n" +
                "Обирайте дію з меню нижче";
        sendMessage(chatId, answer);
    }
}
