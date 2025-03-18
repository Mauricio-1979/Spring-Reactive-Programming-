package com.maurizio.ReactivePrograming.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class ProductDTO {

    @NotBlank(message = "name is mandatory")
    private String name;
    @Min(value = 1, message = "price must be greater than zero")
    private float price;
}
