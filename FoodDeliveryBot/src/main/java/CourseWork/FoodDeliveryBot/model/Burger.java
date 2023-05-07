package CourseWork.FoodDeliveryBot.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity(name = "burger")
public class Burger {

    @Id
    private int id_burger;

    private String name;

    private String description;

    private float weight;

    private float price;

    private String image;
}
