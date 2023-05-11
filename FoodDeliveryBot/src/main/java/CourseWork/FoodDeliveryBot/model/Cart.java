package CourseWork.FoodDeliveryBot.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Component
public class Cart {

    @Autowired
    private DishesRepository dishesRepository;

    private long chatId;
    private List<Dish> dishes;

    public Cart(DishesRepository dishesRepository) {
        this.dishesRepository = dishesRepository;
        this.dishes = new ArrayList<>();
    }

    public void addDishToCart(String dishName) {
        if (findDishInMenu(dishName) != null)
            dishes.add(findDishInMenu(dishName));
    }

    public Dish findDishInMenu(String dishName) {
        var dishes = dishesRepository.findAll();

        for (Dish dish : dishes) {
            if (dishName.equals(dish.getName()))
                return dish;
        }
        return null;
    }

    public float getTotalPrice() {
        float totalPrice = 0;

        if (!dishes.isEmpty()) {
            for (Dish dish : dishes) {
                totalPrice += dish.getPrice();
            }
        }

        return totalPrice;
    }

    public String getCartInfo() {
        StringBuilder message = new StringBuilder("Кошик порожній");

        if (!dishes.isEmpty()) {
            message = new StringBuilder("<b>Кошик:</b>\n\n");

            for (Dish dish : dishes) {
                message.append(dishes.indexOf(dish) + 1).append(". ").append(dish.getName()).append(" - ").append(dish.getPrice()).append(" грн.").append("\n");
            }
            message.append("\n<b> До сплати: ").append(getTotalPrice()).append(" грн.</b>");
        }

        return message.toString();
    }
}
