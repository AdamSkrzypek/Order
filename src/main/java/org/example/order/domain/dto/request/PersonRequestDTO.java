package org.example.order.domain.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record PersonRequestDTO(@NotBlank @Size(min = 3) String firstName, @NotBlank @Size(min = 3) String lastName,
                               @NotBlank @Email String email, @Pattern(regexp = "^\\+48\\s?\\d{9}$") String phoneNumber) {
}
