package model;

import lombok.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class Email {

    @NotBlank(message = "Email ID cannot be blank")
    @Pattern(regexp = "^[a-fA-F0-9]{64}$", message = "Email ID must be a valid SHA-256 hash")
    private String id;

    @NotBlank(message = "Sender cannot be blank")
    private String sender;

    @NotBlank(message = "Recipient cannot be blank")
    private String recipient;

    @NotBlank(message = "Subject cannot be blank")
    @Size(max = 255, message = "Subject must be less than 255 characters")
    private String subject;

    @NotBlank(message = "Content cannot be blank")
    private String content;

    @NotNull(message = "Timestamp cannot be null")
    private LocalDateTime timestamp;

    private boolean viewed;
}