package com.maurizio.ReactivePrograming.entity;


import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;


@Table("products")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Product {

    @Id
    private int id;
    private String name;
    private float price;




    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", price=" + price +
                '}';
    }
}
