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

            List<Dish> dishesWithoutRepeat = new ArrayList<>(getDishesWithoutRepeat());

            for (Dish dish : dishesWithoutRepeat) {
                message.append(dishesWithoutRepeat.indexOf(dish) + 1).append(". ").append(dish.getName()).append(" - ").append(dish.getPrice()).append(" грн. - ").append(getDishQuantity(dish)).append("шт.\n");
            }
            message.append("\n<b> До сплати: ").append(getTotalPrice()).append(" грн.</b>");
        }

        return message.toString();
    }

    public int getDishQuantity(Dish dish) {
        int dishQuantity = 0;

        for (Dish value : dishes) {
            if (value.getId() == dish.getId())
                dishQuantity++;
        }

        return dishQuantity;
    }

    public void removeDish(String dishName) {
//        if (findDishInMenu(dishName) != null)
//            dishes.remove(findDishInMenu(dishName));

        if (findDishInMenu(dishName) != null){
            dishes.removeIf(dish -> dish.getName().equals(dishName));
        }
    }

    public List<String> getDishNames() {
        List<String> dishNames = new ArrayList<>();

        for (Dish dish : getDishesWithoutRepeat()) {
            dishNames.add("Видалити " + dish.getName());
        }
        return dishNames;
    }

    public List<Dish> getDishesWithoutRepeat(){
        List<Dish> uniqueDishes = new ArrayList<>();
        for (Dish dish : dishes) {
            if (!uniqueDishes.contains(dish)) {
                uniqueDishes.add(dish);
            }
        }
        return uniqueDishes;
    }
}
