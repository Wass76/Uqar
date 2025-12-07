package com.Uqar.utils.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class PaginationResponse {
    private int page;
    private int perPage;
    private long total;
}
