package CourseWork.FoodDeliveryBot.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity(name = "dish")
public class Dish {

    @Id
    private int id;

    private String category;

    private String name;

    private String description;

    private String additional;

    private float weight;

    private float price;

    private String image;
}
