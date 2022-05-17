package com.endava.endabank.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserToApproveAccountDto {
    private String firstName;
    private String lastName;
    private String email;
    private Integer id;
    private boolean isApproved;
}
